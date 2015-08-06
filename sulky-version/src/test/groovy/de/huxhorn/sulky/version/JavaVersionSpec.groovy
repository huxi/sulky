/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2015 Joern Huxhorn
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright 2007-2015 Joern Huxhorn
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.huxhorn.sulky.version

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll;

@Subject(JavaVersion)
@SuppressWarnings("GrEqualsBetweenInconvertibleTypes")
public class JavaVersionSpec extends Specification {

    @Unroll
    def 'parse("#versionString") throws an exception'(String versionString) {
        when:
        JavaVersion.parse(versionString)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "versionString '${versionString}' is invalid."

        where:
        versionString << ['1x', '1.2x', '1.2.3x', '1.2.3.4', '1.2.3_4x', '1.2.3_4-', '-1.6']
    }

    def 'parse(null) throws an exception'() {
        when:
        JavaVersion.parse(null)

        then:
        NullPointerException ex = thrown()
        ex.message == 'versionString must not be null!'
    }

    @Unroll('JVM #jvmString is#compareString at least #versionString')
    def 'isAtLeast(String) works'() {
        when:
        boolean result = JavaVersion.isAtLeast(versionString)

        then:
        result == expectedResult

        where:
        // @formatter:off
        versionString                                   | expectedResult
        "1.0.0"                                         | true
        JavaVersion.systemJavaVersion.toVersionString() | true
        "17.0"                                          | false
        // @formatter:on

        compareString = expectedResult? '' : 'n\'t'
        jvmString = JavaVersion.systemJavaVersion.toVersionString()
    }

    @Unroll('JVM #jvmString is#compareString at least #version')
    def 'isAtLeast(JavaVersion) works'() {
        when:
        boolean result = JavaVersion.isAtLeast(version)

        then:
        result == expectedResult

        where:
        // @formatter:off
        version                                                            | expectedResult
        new YeOldeJavaVersion(1, 0, 0)                                     | true
        JavaVersion.parse(JavaVersion.systemJavaVersion.toVersionString()) | true
        new YeOldeJavaVersion(17, 0)                                       | false
        new Jep223JavaVersion([42, 7, 9] as int[], null, 0, null)          | false
        // @formatter:on

        compareString = expectedResult? '' : 'n\'t'
        jvmString = JavaVersion.systemJavaVersion.toVersionString()
    }

    @Unroll
    def 'isAtLeast("#versionString") throws an exception'(String versionString) {
        when:
        JavaVersion.isAtLeast(versionString)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "versionString '${versionString}' is invalid."

        where:
        versionString << ['1x', '1.2x', '1.2.3x', '1.2.3.4', '1.2.3_4x', '1.2.3_4-', '-1.6']
    }

    def 'isAtLeast((String)null) throws an exception'() {
        when:
        JavaVersion.isAtLeast((String)null)

        then:
        NullPointerException ex = thrown()
        ex.message == 'versionString must not be null!'
    }

    def 'isAtLeast((JavaVersion)null) throws an exception'() {
        when:
        JavaVersion.isAtLeast((JavaVersion)null)

        then:
        NullPointerException ex = thrown()
        ex.message == 'version must not be null!'
    }

    @Unroll('COMPARATOR works for #versionA and #versionB')
    def 'comparator'(JavaVersion versionA, JavaVersion versionB, int expectedResult) {
        when:
        int result1 = JavaVersion.COMPARATOR.compare(versionA, versionB)
        int result2 = JavaVersion.COMPARATOR.compare(versionB, versionA)

        then:
        expectedResult == result1
        expectedResult == result2 * -1

        where:
        versionA                                                  | versionB                                                  | expectedResult
        new YeOldeJavaVersion(1, 8, 0, 45)                        | new YeOldeJavaVersion(1, 8, 0, 45)                        | 0
        new YeOldeJavaVersion(1, 8, 0, 45)                        | new Jep223JavaVersion([8, 0, 45] as int[], null, 0, null) | -1
        new Jep223JavaVersion([8, 0, 45] as int[], null, 0, null) | new Jep223JavaVersion([8, 0, 45] as int[], null, 0, null) | 0
        null                                                      | null                                                      | 0
        new YeOldeJavaVersion(1, 8, 0, 45)                        | null                                                      | 1
        new Jep223JavaVersion([8, 0, 45] as int[], null, 0, null) | null                                                      | 1
    }

    def 'comparator exception'(JavaVersion versionA, JavaVersion versionB) {
        when:
        JavaVersion.COMPARATOR.compare(versionA, versionB)

        then:
        ClassCastException ex = thrown()
        ex.message == 'Unexpected JavaVersion of class de.huxhorn.sulky.version.JavaVersionSpec$FooVersion!'

        where:
        versionA                                                  | versionB
        new YeOldeJavaVersion(1, 8, 0, 45)                        | new FooVersion()
        new Jep223JavaVersion([8, 0, 45] as int[], null, 0, null) | new FooVersion()
        new FooVersion()                                          | new YeOldeJavaVersion(1, 8, 0, 45)
        new FooVersion()                                          | new Jep223JavaVersion([8, 0, 45] as int[], null, 0, null)
    }


    class FooVersion
        extends JavaVersion
    {

        @Override
        int getMajor() {
            return 0
        }

        @Override
        int getMinor() {
            return 0
        }

        @Override
        int getPatch() {
            return 0
        }

        @Override
        String getPreReleaseIdentifier() {
            return null
        }

        @Override
        String toVersionString() {
            return null
        }

        @Override
        String toShortVersionString() {
            return null
        }
    }
}
