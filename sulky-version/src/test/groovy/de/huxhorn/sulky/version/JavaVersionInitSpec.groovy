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

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

/**
 * Tests internal method responsible for init of JavaVersion.JVM
 */
@Subject(JavaVersion)
class JavaVersionInitSpec extends Specification {

    private static final String JAVA_VERSION = 'java.version'
    private static final String JAVA_SPEC_VERSION = 'java.specification.version'

    @Shared
    TestSecurityManager manager = new TestSecurityManager()

    @Shared
    JavaVersion version

    @Shared
    String javaVersion

    @Shared
    String javaSpecificationVersion

    def setupSpec() {
        javaVersion = System.getProperty(JAVA_VERSION)
        javaSpecificationVersion = System.getProperty(JAVA_SPEC_VERSION)
        System.setSecurityManager(manager);
    }

    def cleanupSpec() {
        System.setSecurityManager(null);
        System.setProperty(JAVA_VERSION, javaVersion)
        System.setProperty(JAVA_SPEC_VERSION, javaSpecificationVersion)
    }

    def 'no property access'() {
        when:
        manager.unreadableProperties = [JAVA_VERSION, JAVA_SPEC_VERSION]
        System.setProperty(JAVA_VERSION, '1.6.1_25')
        System.setProperty(JAVA_SPEC_VERSION, '1.6')
        def version = JavaVersion.systemJavaVersion

        then:
        version == YeOldeJavaVersion.MIN_VALUE
    }

    def 'some property access'() {
        when:
        manager.unreadableProperties = [JAVA_VERSION]
        System.setProperty(JAVA_VERSION, '1.6.1_25')
        System.setProperty(JAVA_SPEC_VERSION, '1.6')
        def version = JavaVersion.systemJavaVersion

        then:
        version != null
        version == new YeOldeJavaVersion(1,6)
    }

    def 'full property access'() {
        when:
        manager.unreadableProperties = null
        System.setProperty(JAVA_VERSION, '1.6.1_25')
        System.setProperty(JAVA_SPEC_VERSION, '1.6')
        def version = JavaVersion.systemJavaVersion

        then:
        version != null
        version == new YeOldeJavaVersion(1,6,1,25)
    }

    def 'full property access with broken java version'() {
        when:
        manager.unreadableProperties = null
        System.setProperty(JAVA_VERSION, '1.6.x_25')
        System.setProperty(JAVA_SPEC_VERSION, '1.6')
        def version = JavaVersion.systemJavaVersion

        then:
        version != null
        version == new YeOldeJavaVersion(1,6)
    }

    def 'full property access with broken java and spec version'() {
        when:
        manager.unreadableProperties = null
        System.setProperty(JAVA_VERSION, '1.6.x_25')
        System.setProperty(JAVA_SPEC_VERSION, '1.x')
        def version = JavaVersion.systemJavaVersion

        then:
        version == YeOldeJavaVersion.MIN_VALUE
    }

    def 'full property access with missing java version'() {
        when:
        manager.unreadableProperties = null
        System.clearProperty(JAVA_VERSION)
        System.setProperty(JAVA_SPEC_VERSION, '1.6')
        def version = JavaVersion.systemJavaVersion

        then:
        version != null
        version == new YeOldeJavaVersion(1,6)
    }

    def 'full property access with missing java and spec version'() {
        when:
        manager.unreadableProperties = null
        System.clearProperty(JAVA_VERSION)
        System.clearProperty(JAVA_SPEC_VERSION)
        def version = JavaVersion.systemJavaVersion

        then:
        version == YeOldeJavaVersion.MIN_VALUE
    }

    def 'minimal JEP 223 version'() {
        when:
        manager.unreadableProperties = null
        System.setProperty(JAVA_VERSION, '9')
        System.setProperty(JAVA_SPEC_VERSION, '9')
        def version = JavaVersion.systemJavaVersion

        then:
        version != null
        version == new Jep223JavaVersion([9] as int[], null, 0, null)
    }


    def 'ignoring pre-release identifier works'() {
        when:
        manager.unreadableProperties = null
        System.setProperty(JAVA_VERSION, '1.8.0_66-internal')
        System.setProperty(JAVA_SPEC_VERSION, '1.8')

        then:
        JavaVersion.isAtLeast('1.8.0_66', true)
        !JavaVersion.isAtLeast('1.8.0_66', false)
    }
}
