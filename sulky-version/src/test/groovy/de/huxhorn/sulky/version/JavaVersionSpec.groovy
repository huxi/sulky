/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2014 Joern Huxhorn
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
 * Copyright 2007-2014 Joern Huxhorn
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
import de.huxhorn.sulky.junit.JUnitTools;

@Subject(JavaVersion)
public class JavaVersionSpec extends Specification {

    @Unroll
    def 'parse("#versionString") returns #expectedVersion'(String versionString, JavaVersion expectedVersion) {
        when:
        JavaVersion version = JavaVersion.parse(versionString)

        then:
        version == expectedVersion

        where:
        versionString | expectedVersion
        '1.6'         | new JavaVersion(1, 6)
        '1.6.17'      | new JavaVersion(1, 6, 17)
        '1.6.17_42'   | new JavaVersion(1, 6, 17, 42)
        '1.4.2-02'    | new JavaVersion(1, 4, 2, 0, '02') // actual version
        '1.3.0'       | new JavaVersion(1, 3, 0)
        '1.3.1-beta'  | new JavaVersion(1, 3, 1, 0, 'beta')
        '1.3.1_05-ea' | new JavaVersion(1, 3, 1, 5, 'ea')
        '1.3.1_05'    | new JavaVersion(1, 3, 1, 5)
        '1.4.0_03-ea' | new JavaVersion(1, 4, 0, 3, 'ea')
        '1.4.0_03'    | new JavaVersion(1, 4, 0, 3)

    }

    @Unroll
    def 'parse("#versionString") throws an exception'(String versionString) {
        when:
        JavaVersion.parse(versionString)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "versionString '${versionString}' is invalid."

        where:
        versionString << ['1', '1x', '1.2x', '1.2.3x', '1.2.3.4', '1.2.3_4x', '1.2.3_4-', '-1.6']
    }

    def 'parse(null) throws an exception'() {
        when:
        JavaVersion.parse(null)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'versionString must not be null!'
    }

    def 'constructor throws an exceptions in case of empty string identifier'() {
        when:
        new JavaVersion(1, 2, 3, 4, '')

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'identifier must not be empty string!'
    }

    def 'constructor throws an exceptions in case of * character'() {
        when:
        new JavaVersion(1, 2, 3, 4, 'f*o')

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'identifier must not contain the \'*\' character!'
    }

    def 'constructor throws an exceptions in case of + character'() {
        when:
        new JavaVersion(1, 2, 3, 4, 'f+o')

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'identifier must not contain the \'+\' character!'
    }

    @Unroll('new JavaVersion(#huge, #major, #minor, #patch, #identifier) throws an exception since #failingPart is negative')
    def 'constructor throws an exceptions in case of negative values'(int huge, int major, int minor, int patch, String identifier, String failingPart) {
        when:
        new JavaVersion(huge, major, minor, patch, identifier)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "${failingPart} must not be negative!"

        where:
        // @formatter:off
        huge | major | minor | patch | identifier | failingPart
          -1 |     2 |     3 |     4 | null       | 'huge'
           1 |    -2 |     3 |     4 | null       | 'major'
           1 |     2 |    -3 |     4 | null       | 'minor'
           1 |     2 |     3 |    -4 | null       | 'patch'
        // @formatter:on
    }

    @Unroll('new JavaVersion(#huge, #major, #minor, #patch) throws an exception since #failingPart is negative')
    def 'constructor throws an exceptions in case of negative values'(int huge, int major, int minor, int patch, String failingPart) {
        when:
        new JavaVersion(huge, major, minor, patch)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "${failingPart} must not be negative!"

        where:
        // @formatter:off
        huge | major | minor | patch | failingPart
          -1 |     2 |     3 |     4 | 'huge'
           1 |    -2 |     3 |     4 | 'major'
           1 |     2 |    -3 |     4 | 'minor'
           1 |     2 |     3 |    -4 | 'patch'
        // @formatter:on
    }

    @Unroll('new JavaVersion(#huge, #major, #minor) throws an exception since #failingPart is negative')
    def 'constructor throws an exceptions in case of negative values'(int huge, int major, int minor, String failingPart) {
        when:
        new JavaVersion(huge, major, minor)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "${failingPart} must not be negative!"

        where:
        // @formatter:off
        huge | major | minor | failingPart
          -1 |     2 |     3 | 'huge'
           1 |    -2 |     3 | 'major'
           1 |     2 |    -3 | 'minor'
        // @formatter:on

    }

    @Unroll('new JavaVersion(#huge, #major) throws an exception since #failingPart is negative')
    def 'constructor throws an exceptions in case of negative values'(int huge, int major, String failingPart) {
        when:
        new JavaVersion(huge, major)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "${failingPart} must not be negative!"

        where:
        // @formatter:off
        huge | major | failingPart
          -1 |     2 | 'huge'
           1 |    -2 | 'major'
        // @formatter:on

    }

    @Unroll('new JavaVersion(#huge, #major, #minor, #patch, #identifier) is correctly initialized')
    def 'constructor correctly initializes attributes'(int huge, int major, int minor, int patch, String identifier) {
        when:
        JavaVersion result = new JavaVersion(huge, major, minor, patch, identifier)

        then:
        result.huge == huge
        result.major == major
        result.minor == minor
        result.patch == patch
        result.identifier == identifier

        where:
        // @formatter:off
        huge | major | minor | patch | identifier
           0 |     0 |     0 |     0 | null
           1 |     0 |     0 |     0 | null
           1 |     6 |     0 |     0 | null
           1 |     6 |    17 |     0 | null
           1 |     6 |    17 |     4 | null
           0 |     0 |     0 |     0 | 'foo'
           1 |     0 |     0 |     0 | 'foo'
           1 |     6 |     0 |     0 | 'foo'
           1 |     6 |    17 |     0 | 'foo'
           1 |     6 |    17 |     4 | 'foo'
        // @formatter:on
    }

    @Unroll('new JavaVersion(#huge, #major, #minor, #patch) is correctly initialized')
    def 'constructor correctly initializes attributes'(int huge, int major, int minor, int patch) {
        when:
        JavaVersion result = new JavaVersion(huge, major, minor, patch)

        then:
        result.huge == huge
        result.major == major
        result.minor == minor
        result.patch == patch
        result.identifier == null

        where:
        // @formatter:off
        huge | major | minor | patch
           0 |     0 |     0 |     0
           1 |     0 |     0 |     0
           1 |     6 |     0 |     0
           1 |     6 |    17 |     0
           1 |     6 |    17 |     4
        // @formatter:on
    }

    @Unroll('new JavaVersion(#huge, #major, #minor) is correctly initialized')
    def 'constructor correctly initializes attributes'(int huge, int major, int minor) {
        when:
        JavaVersion result = new JavaVersion(huge, major, minor)

        then:
        result.huge == huge
        result.major == major
        result.minor == minor
        result.patch == 0
        result.identifier == null

        where:
        // @formatter:off
        huge | major | minor
           0 |     0 |     0
           1 |     0 |     0
           1 |     6 |     0
           1 |     6 |    17
        // @formatter:on
    }

    @Unroll('new JavaVersion(#huge, #major) is correctly initialized')
    def 'constructor correctly initializes attributes'(int huge, int major) {
        when:
        JavaVersion result = new JavaVersion(huge, major)

        then:
        result.huge == huge
        result.major == major
        result.minor == 0
        result.patch == 0
        result.identifier == null

        where:
        // @formatter:off
        huge | major
           0 |     0
           1 |     0
           1 |     6
        // @formatter:on
    }

    @Unroll('#objectString #compareString #otherString')
    def 'comparing instances works'() {
        when:
        int result = object.compareTo(other)

        then:
        result == expectedResult
        if(expectedResult == 0) {
            assert object.equals(other)
            assert object.hashCode() == other.hashCode()
        } else {
            assert !object.equals(other)
        }

        where:
        // @formatter:off
        object                       | other                        | expectedResult
        JavaVersion.MIN_VALUE        | JavaVersion.MIN_VALUE        |              0
        JavaVersion.MIN_VALUE        | new JavaVersion(0,0,0,0,"!") |              0
        new JavaVersion(0,0,0,0)     | JavaVersion.MIN_VALUE        |              1
        JavaVersion.MIN_VALUE        | new JavaVersion(0,0,0,0)     |             -1
        new JavaVersion(0,0,0,1)     | JavaVersion.MIN_VALUE        |              1
        JavaVersion.MIN_VALUE        | new JavaVersion(0,0,0,1)     |             -1

        new JavaVersion(0,0,0,1)     | new JavaVersion(0,0,0,1)     |              0
        new JavaVersion(0,0,0,2)     | new JavaVersion(0,0,0,1)     |              1
        new JavaVersion(0,0,0,1)     | new JavaVersion(0,0,0,2)     |             -1

        new JavaVersion(0,0,1,0)     | new JavaVersion(0,0,1,0)     |              0
        new JavaVersion(0,0,1,0)     | new JavaVersion(0,0,0,1)     |              1
        new JavaVersion(0,0,0,1)     | new JavaVersion(0,0,1,0)     |             -1
        new JavaVersion(0,0,2,0)     | new JavaVersion(0,0,1,0)     |              1
        new JavaVersion(0,0,1,0)     | new JavaVersion(0,0,2,0)     |             -1

        new JavaVersion(0,1,0,0)     | new JavaVersion(0,1,0,0)     |              0
        new JavaVersion(0,1,0,0)     | new JavaVersion(0,0,0,1)     |              1
        new JavaVersion(0,0,0,1)     | new JavaVersion(0,1,0,0)     |             -1
        new JavaVersion(0,2,0,0)     | new JavaVersion(0,1,0,0)     |              1
        new JavaVersion(0,1,0,0)     | new JavaVersion(0,2,0,0)     |             -1

        new JavaVersion(1,0,0,0)     | new JavaVersion(1,0,0,0)     |              0
        new JavaVersion(1,0,0,0)     | new JavaVersion(0,0,0,1)     |              1
        new JavaVersion(0,0,0,1)     | new JavaVersion(1,0,0,0)     |             -1
        new JavaVersion(2,0,0,0)     | new JavaVersion(1,0,0,0)     |              1
        new JavaVersion(1,0,0,0)     | new JavaVersion(2,0,0,0)     |             -1

        // 1.3.0 < 1.3.0_01 < 1.3.1 < 1.3.1_01
        new JavaVersion(1,3,0,0)     | new JavaVersion(1,3,0,1)     |             -1
        new JavaVersion(1,3,0,1)     | new JavaVersion(1,3,1,0)     |             -1
        new JavaVersion(1,3,1,0)     | new JavaVersion(1,3,1,1)     |             -1

        new JavaVersion(1,3,1,1)     | new JavaVersion(1,3,1,0)     |              1
        new JavaVersion(1,3,1,0)     | new JavaVersion(1,3,0,1)     |              1
        new JavaVersion(1,3,0,1)     | new JavaVersion(1,3,0,0)     |              1

        // with identifier is less than without
        new JavaVersion(1,3,0,0,'x') | new JavaVersion(1,3,0,0,'x') |              0
        new JavaVersion(1,3,0,0)     | new JavaVersion(1,3,0,0,'x') |              1
        new JavaVersion(1,3,0,0,'x') | new JavaVersion(1,3,0,0)     |             -1

        new JavaVersion(1,3,1,0,'x') | new JavaVersion(1,3,0,0,'x') |              1
        new JavaVersion(1,3,0,0,'x') | new JavaVersion(1,3,1,0,'x') |             -1
        new JavaVersion(1,3,0,0,'a') | new JavaVersion(1,3,0,0,'b') |             -1
        new JavaVersion(1,3,0,0,'b') | new JavaVersion(1,3,0,0,'a') |              1
        // @formatter:on

        compareString = compareString(expectedResult)
        objectString = object.toVersionString()
        otherString = other.toVersionString()
    }

    @Unroll('JVM #jvmString is#compareString at least #versionString')
    def 'isAtLeast works'() {
        when:
        boolean result = JavaVersion.isAtLeast(versionString)

        then:
        result == expectedResult

        where:
        // @formatter:off
        versionString                       | expectedResult
        "1.0.0"                             | true
        JavaVersion.JVM.toVersionString()   | true
        "17.0"                              | false
        // @formatter:on

        compareString = expectedResult? '' : 'n\'t'
        jvmString = JavaVersion.JVM.toVersionString()
    }

    @Unroll
    def 'isAtLeast("#versionString") throws an exception'(String versionString) {
        when:
        JavaVersion.isAtLeast(versionString)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "versionString '${versionString}' is invalid."

        where:
        versionString << ['1', '1x', '1.2x', '1.2.3x', '1.2.3.4', '1.2.3_4x', '1.2.3_4-', '-1.6']
    }

    def 'isAtLeast(null) throws an exception'() {
        when:
        JavaVersion.isAtLeast(null)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'versionString must not be null!'
    }

    @Unroll('toVersionString() returns #expectedResult for #object')
    def 'toVersionString() works'() {
        when:
        String result = object.toVersionString()

        then:
        result == expectedResult

        where:
        // @formatter:off
        object                         | expectedResult
        new JavaVersion(0,0,0,0)       | '0.0.0'
        new JavaVersion(0,0,0,0,'x')   | '0.0.0-x'
        new JavaVersion(1,2,3,4,'x')   | '1.2.3_04-x'
        new JavaVersion(1,2,3,14,'x')  | '1.2.3_14-x'
        new JavaVersion(1,2,3,114,'x') | '1.2.3_114-x'
        // @formatter:on
    }

    @Unroll('toString() returns #expectedResult for #objectString')
    def 'toString() works'() {
        when:
        String result = object.toString()

        then:
        result == expectedResult

        where:
        // @formatter:off
        object                         | expectedResult
        new JavaVersion(0,0,0,0)       | 'JavaVersion{huge=0, major=0, minor=0, patch=0, identifier=null}'
        new JavaVersion(0,0,0,0,'x')   | 'JavaVersion{huge=0, major=0, minor=0, patch=0, identifier="x"}'
        new JavaVersion(1,2,3,4,'x')   | 'JavaVersion{huge=1, major=2, minor=3, patch=4, identifier="x"}'
        new JavaVersion(1,2,3,14,'x')  | 'JavaVersion{huge=1, major=2, minor=3, patch=14, identifier="x"}'
        new JavaVersion(1,2,3,114,'x') | 'JavaVersion{huge=1, major=2, minor=3, patch=114, identifier="x"}'
        // @formatter:on

        objectString = object.toVersionString()
    }

    def 'compareTo(null) throws exception'() {
        when:
        JavaVersion object = new JavaVersion(1,6)
        object.compareTo(null)

        then:
        NullPointerException ex = thrown()
        ex.getMessage() == 'other must not be null!'
    }

    def 'equals(null) returns false'() {
        when:
        JavaVersion object = new JavaVersion(1,6)

        then:
        !object.equals(null)
    }

    def 'equals(someOtherClass) returns false'() {
        when:
        JavaVersion object = new JavaVersion(1,6)

        then:
        !object.equals(1)
    }

    private static String compareString(int value)
    {
        if(value < 0) {
            return 'is less than'
        }

        if(value > 0) {
            return 'is greater than'
        }

        return 'is equal to'
    }

    def 'JVM constant is initialized'() {

        expect:
        JavaVersion.JVM != null
        JavaVersion.JVM != JavaVersion.MIN_VALUE
        JavaVersion.JVM.compareTo(JavaVersion.MIN_VALUE) > 0
    }

    def 'serializable'() {
        expect:
        JUnitTools.testSerialization(object)

        where:
        object << [new JavaVersion(0,0,0,0,'foo'), new JavaVersion(1,2,3,4,'bar'), new JavaVersion(1,2,3,4)]
    }

    /*
    Adding XML serialization would require default constructor and setters. So: nope.
    def 'serializable as XML'() {
        expect:
        JUnitTools.testXmlSerialization(object)

        where:
        object << [new JavaVersion(0,0,0,0,'foo'), new JavaVersion(1,2,3,4,'bar'), new JavaVersion(1,2,3,4)]
    }
    */
}
