/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2018 Joern Huxhorn
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
 * Copyright 2007-2018 Joern Huxhorn
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

import spock.lang.IgnoreIf
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

@Subject(JavaVersion)
@SuppressWarnings("GrEqualsBetweenInconvertibleTypes")
class JavaVersionSpec extends Specification {

	private static final String CURRENT_VERSION_STRING = JavaVersion.systemJavaVersion.toVersionString()

	/**
	 * A JEP 223 version is always "bigger" than an old version.
	 * Huge old versions, i.e. an old version with "huge" version != 1, will never actually exist
	 * because !startsWith("1.") is the condition to parse a version as a JEP 223 version.
	 */
	private static final boolean CURRENT_VERSION_IS_JEP223 = JavaVersion.systemJavaVersion instanceof Jep223JavaVersion

	@Unroll
	'parse("#versionString") throws an exception'(String versionString) {
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
	'isAtLeast(String) works'() {
		when:
		boolean result = JavaVersion.isAtLeast(versionString)

		then:
		result == expectedResult

		where:
		versionString                                   | expectedResult
		"1.0.0"                                         | true
		JavaVersion.systemJavaVersion.toVersionString() | true
		"17.0"                                          | false

		compareString = expectedResult ? '' : 'n\'t'
		jvmString = JavaVersion.systemJavaVersion.toVersionString()
	}

	@Unroll('JVM #jvmString is#compareString at least #version')
	'isAtLeast(JavaVersion) works'() {
		expect:
		expectedResult == JavaVersion.isAtLeast(version)

		where:
		version                                                            | expectedResult
		new YeOldeJavaVersion(1, 0, 0)                                     | true
		JavaVersion.parse(JavaVersion.systemJavaVersion.toVersionString()) | true
		new Jep223JavaVersion([42, 7, 9] as int[], null, 0, null)          | false

		compareString = expectedResult ? '' : 'n\'t'
		jvmString = JavaVersion.systemJavaVersion.toVersionString()
	}

	@Unroll('JVM #jvmString is#compareString at least #version for huge old version')
	'isAtLeast(JavaVersion) works for huge old version'() {
		expect:
		expectedResult == JavaVersion.isAtLeast(version)

		where:
		version                      | expectedResult
		new YeOldeJavaVersion(17, 0) | CURRENT_VERSION_IS_JEP223

		compareString = expectedResult ? '' : 'n\'t'
		jvmString = JavaVersion.systemJavaVersion.toVersionString()
	}

	@Unroll('JVM #jvmString is#compareString at least #version - ignoring pre-release identifier')
	'isAtLeast(JavaVersion) works without pre-release'() {
		expect:
		expectedResult == JavaVersion.isAtLeast(version, true)

		where:
		version                                                            | expectedResult
		new YeOldeJavaVersion(1, 0, 0)                                     | true
		JavaVersion.parse(JavaVersion.systemJavaVersion.toVersionString()) | true
		new Jep223JavaVersion([42, 7, 9] as int[], null, 0, null)          | false

		compareString = expectedResult ? '' : 'n\'t'
		jvmString = JavaVersion.systemJavaVersion.toVersionString()
	}

	@Unroll('JVM #jvmString is#compareString at least #version for huge old version - ignoring pre-release identifier')
	'isAtLeast(JavaVersion) works for huge old version without pre-release'() {
		expect:
		expectedResult == JavaVersion.isAtLeast(version, true)

		where:
		version                      | expectedResult
		new YeOldeJavaVersion(17, 0) | CURRENT_VERSION_IS_JEP223

		compareString = expectedResult ? '' : 'n\'t'
		jvmString = JavaVersion.systemJavaVersion.toVersionString()
	}

	@IgnoreIf({ CURRENT_VERSION_STRING.contains('-') })
	@Unroll('JVM #jvmString is#compareString at least #version')
	'isAtLeast(JavaVersion) works - special'() {
		when:
		boolean result = JavaVersion.isAtLeast(version)

		then:
		result == expectedResult

		where:
		version                                                                    | expectedResult
		JavaVersion.parse(JavaVersion.systemJavaVersion.toVersionString() + '-ea') | true

		compareString = expectedResult ? '' : 'n\'t'
		jvmString = JavaVersion.systemJavaVersion.toVersionString()
	}

	@IgnoreIf({ CURRENT_VERSION_STRING.contains('-') })
	@Unroll('JVM #jvmString is#compareString at least #version - ignoring pre-release identifier')
	'isAtLeast(JavaVersion) works without pre-release - special'() {
		when:
		boolean result = JavaVersion.isAtLeast(version, true)

		then:
		result == expectedResult

		where:
		version                                                                    | expectedResult
		JavaVersion.parse(JavaVersion.systemJavaVersion.toVersionString() + '-ea') | true

		compareString = expectedResult ? '' : 'n\'t'
		jvmString = JavaVersion.systemJavaVersion.toVersionString()
	}

	@Unroll
	'isAtLeast("#versionString") throws an exception'(String versionString) {
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
		JavaVersion.isAtLeast((String) null)

		then:
		NullPointerException ex = thrown()
		ex.message == 'versionString must not be null!'
	}

	def 'isAtLeast((JavaVersion)null) throws an exception'() {
		when:
		JavaVersion.isAtLeast((JavaVersion) null)

		then:
		NullPointerException ex = thrown()
		ex.message == 'version must not be null!'
	}

	@Unroll('COMPARATOR works for #versionA and #versionB')
	'comparator'(JavaVersion versionA, JavaVersion versionB, int expectedResult) {
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
			extends JavaVersion {

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
		int getEmergencyPatch() {
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

		@Override
		JavaVersion withoutPreReleaseIdentifier() {
			return this
		}
	}
}
