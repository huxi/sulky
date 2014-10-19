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

import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

/**
 * Tests internal method responsible for init of JavaVersion.JVM
 */
@Subject(JavaVersion)
class JavaVersionInitSpec extends Specification {

    @Shared
    TestSecurityManager manager = new TestSecurityManager()

    @Shared
    JavaVersion version

    def setupSpec() {
        version = JavaVersion.JVM // ensures that this test doesn't influence init of JVM
        System.setSecurityManager(manager);
    }

    def cleanupSpec() {
        System.setSecurityManager(null);
    }

    def 'no property access'() {
        when:
        manager.unreadableProperties = ['java.version', 'java.specification.version']
        def version = JavaVersion.systemJavaVersion

        then:
        version == null
    }

    def 'some property access'() {
        when:
        manager.unreadableProperties = ['java.version']
        def version = JavaVersion.systemJavaVersion

        then:
        version != null
        version == JavaVersion.parse(System.getProperty('java.specification.version'))
    }

    def 'full property access'() {
        when:
        manager.unreadableProperties = null
        def version = JavaVersion.systemJavaVersion

        then:
        version != null
        version == JavaVersion.parse(System.getProperty('java.version'))
    }
}
