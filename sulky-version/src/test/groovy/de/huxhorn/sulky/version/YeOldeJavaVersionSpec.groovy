/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2019 Joern Huxhorn
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
 * Copyright 2007-2019 Joern Huxhorn
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
import spock.lang.Unroll

@Subject(YeOldeJavaVersion)
@SuppressWarnings("GrEqualsBetweenInconvertibleTypes")
class YeOldeJavaVersionSpec extends Specification {

	@Unroll
	'parse("#versionString") returns #expectedVersion'(String versionString, YeOldeJavaVersion expectedVersion) {
		when:
		YeOldeJavaVersion version = YeOldeJavaVersion.parse(versionString)

		then:
		version == expectedVersion

		where:
		versionString | expectedVersion
		'1.6'         | new YeOldeJavaVersion(1, 6)
		'1.6.17'      | new YeOldeJavaVersion(1, 6, 17)
		'1.6.17_42'   | new YeOldeJavaVersion(1, 6, 17, 42)
		'1.4.2-02'    | new YeOldeJavaVersion(1, 4, 2, 0, '02') // actual version
		'1.3.0'       | new YeOldeJavaVersion(1, 3, 0)
		'1.3.1-beta'  | new YeOldeJavaVersion(1, 3, 1, 0, 'beta')
		'1.3.1_05-ea' | new YeOldeJavaVersion(1, 3, 1, 5, 'ea')
		'1.3.1_05'    | new YeOldeJavaVersion(1, 3, 1, 5)
		'1.4.0_03-ea' | new YeOldeJavaVersion(1, 4, 0, 3, 'ea')
		'1.4.0_03'    | new YeOldeJavaVersion(1, 4, 0, 3)

	}

	@Unroll
	'parse("#versionString") throws an exception'(String versionString) {
		when:
		YeOldeJavaVersion.parse(versionString)

		then:
		IllegalArgumentException ex = thrown()
		ex.message == "versionString '${versionString}' is invalid."

		where:
		versionString << ['1', '1x', '1.2x', '1.2.3x', '1.2.3.4', '1.2.3_4x', '1.2.3_4-', '-1.6']
	}

	def 'parse(null) throws an exception'() {
		when:
		YeOldeJavaVersion.parse(null)

		then:
		NullPointerException ex = thrown()
		ex.message == 'versionString must not be null!'
	}

	def 'constructor throws an exceptions in case of empty string identifier'() {
		when:
		new YeOldeJavaVersion(1, 2, 3, 4, '')

		then:
		IllegalArgumentException ex = thrown()
		ex.message == 'preReleaseIdentifier must not be empty string!'
	}

	def 'constructor throws an exceptions in case of * character'() {
		when:
		new YeOldeJavaVersion(1, 2, 3, 4, 'f*o')

		then:
		IllegalArgumentException ex = thrown()
		ex.message == 'preReleaseIdentifier must not contain the \'*\' character!'
	}

	def 'constructor throws an exceptions in case of + character'() {
		when:
		new YeOldeJavaVersion(1, 2, 3, 4, 'f+o')

		then:
		IllegalArgumentException ex = thrown()
		ex.message == 'preReleaseIdentifier must not contain the \'+\' character!'
	}

	@SuppressWarnings("GroovyAssignabilityCheck")
	@Unroll('new YeOldeJavaVersion(#huge, #major, #minor, #patch, #identifier) throws an exception since #failingPart is negative')
	'constructor throws an exceptions in case of negative values'(int huge, int major, int minor, int patch, String identifier, String failingPart) {
		when:
		new YeOldeJavaVersion(huge, major, minor, patch, identifier)

		then:
		IllegalArgumentException ex = thrown()
		ex.message == "${failingPart} must not be negative!"

		where:
		huge | major | minor | patch | identifier | failingPart
		-1   | 2     | 3     | 4     | null       | 'huge'
		1    | -2    | 3     | 4     | null       | 'major'
		1    | 2     | -3    | 4     | null       | 'minor'
		1    | 2     | 3     | -4    | null       | 'patch'
	}

	@Unroll('new YeOldeJavaVersion(#huge, #major, #minor, #patch) throws an exception since #failingPart is negative')
	@SuppressWarnings("GroovyAssignabilityCheck")
	'constructor throws an exceptions in case of negative values'(int huge, int major, int minor, int patch, String failingPart) {
		when:
		new YeOldeJavaVersion(huge, major, minor, patch)

		then:
		IllegalArgumentException ex = thrown()
		ex.message == "${failingPart} must not be negative!"

		where:
		huge | major | minor | patch | failingPart
		-1   | 2     | 3     | 4     | 'huge'
		1    | -2    | 3     | 4     | 'major'
		1    | 2     | -3    | 4     | 'minor'
		1    | 2     | 3     | -4    | 'patch'
	}

	@Unroll('new YeOldeJavaVersion(#huge, #major, #minor) throws an exception since #failingPart is negative')
	@SuppressWarnings("GroovyAssignabilityCheck")
	'constructor throws an exceptions in case of negative values'(int huge, int major, int minor, String failingPart) {
		when:
		new YeOldeJavaVersion(huge, major, minor)

		then:
		IllegalArgumentException ex = thrown()
		ex.message == "${failingPart} must not be negative!"

		where:
		huge | major | minor | failingPart
		-1   | 2     | 3     | 'huge'
		1    | -2    | 3     | 'major'
		1    | 2     | -3    | 'minor'
	}

	@Unroll('new YeOldeJavaVersion(#huge, #major) throws an exception since #failingPart is negative')
	@SuppressWarnings("GroovyAssignabilityCheck")
	'constructor throws an exceptions in case of negative values'(int huge, int major, String failingPart) {
		when:
		new YeOldeJavaVersion(huge, major)

		then:
		IllegalArgumentException ex = thrown()
		ex.message == "${failingPart} must not be negative!"

		where:
		huge | major | failingPart
		-1   | 2     | 'huge'
		1    | -2    | 'major'
	}

	@Unroll('new YeOldeJavaVersion(#huge, #major, #minor, #patch, #identifier) is correctly initialized')
	@SuppressWarnings("GroovyAssignabilityCheck")
	'constructor correctly initializes attributes'(int huge, int major, int minor, int patch, String identifier) {
		when:
		YeOldeJavaVersion result = new YeOldeJavaVersion(huge, major, minor, patch, identifier)

		then:
		result.huge == huge
		result.major == major
		result.minor == minor
		result.patch == patch
		result.preReleaseIdentifier == identifier

		result.feature == major
		result.interim == minor
		result.update == patch
		result.emergencyPatch == 0

		where:
		huge | major | minor | patch | identifier
		0    | 0     | 0     | 0     | null
		1    | 0     | 0     | 0     | null
		1    | 6     | 0     | 0     | null
		1    | 6     | 17    | 0     | null
		1    | 6     | 17    | 4     | null
		0    | 0     | 0     | 0     | 'foo'
		1    | 0     | 0     | 0     | 'foo'
		1    | 6     | 0     | 0     | 'foo'
		1    | 6     | 17    | 0     | 'foo'
		1    | 6     | 17    | 4     | 'foo'
	}

	@Unroll('new YeOldeJavaVersion(#huge, #major, #minor, #patch) is correctly initialized')
	'constructor correctly initializes attributes'(int huge, int major, int minor, int patch) {
		when:
		YeOldeJavaVersion result = new YeOldeJavaVersion(huge, major, minor, patch)

		then:
		result.huge == huge
		result.major == major
		result.minor == minor
		result.patch == patch
		result.preReleaseIdentifier == null

		result.feature == major
		result.interim == minor
		result.update == patch
		result.emergencyPatch == 0

		where:
		huge | major | minor | patch
		0    | 0     | 0     | 0
		1    | 0     | 0     | 0
		1    | 6     | 0     | 0
		1    | 6     | 17    | 0
		1    | 6     | 17    | 4
	}

	@Unroll('new YeOldeJavaVersion(#huge, #major, #minor) is correctly initialized')
	'constructor correctly initializes attributes'(int huge, int major, int minor) {
		when:
		YeOldeJavaVersion result = new YeOldeJavaVersion(huge, major, minor)

		then:
		result.huge == huge
		result.major == major
		result.minor == minor
		result.patch == 0
		result.preReleaseIdentifier == null

		result.feature == major
		result.interim == minor
		result.update == 0
		result.emergencyPatch == 0

		where:
		huge | major | minor
		0    | 0     | 0
		1    | 0     | 0
		1    | 6     | 0
		1    | 6     | 17
	}

	@Unroll('new YeOldeJavaVersion(#huge, #major) is correctly initialized')
	'constructor correctly initializes attributes'(int huge, int major) {
		when:
		YeOldeJavaVersion result = new YeOldeJavaVersion(huge, major)

		then:
		result.huge == huge
		result.major == major
		result.minor == 0
		result.patch == 0
		result.preReleaseIdentifier == null

		result.feature == major
		result.interim == 0
		result.update == 0
		result.emergencyPatch == 0

		where:
		huge | major
		0    | 0
		1    | 0
		1    | 6
	}

	@Unroll('#objectString #compareString #otherString')
	'comparing instances works'() {
		when:
		//noinspection ChangeToOperator
		int result = object.compareTo(other)

		then:
		result == expectedResult
		if (expectedResult == 0) {
			//noinspection ChangeToOperator
			assert object.equals(other)
			assert object.hashCode() == other.hashCode()
		} else {
			//noinspection ChangeToOperator
			assert !object.equals(other)
		}

		where:
		object                                 | other                                  | expectedResult
		YeOldeJavaVersion.MIN_VALUE            | YeOldeJavaVersion.MIN_VALUE            | 0
		YeOldeJavaVersion.MIN_VALUE            | new YeOldeJavaVersion(0, 0, 0, 0, "!") | 0
		new YeOldeJavaVersion(0, 0, 0, 0)      | YeOldeJavaVersion.MIN_VALUE            | 1
		YeOldeJavaVersion.MIN_VALUE            | new YeOldeJavaVersion(0, 0, 0, 0)      | -1
		new YeOldeJavaVersion(0, 0, 0, 1)      | YeOldeJavaVersion.MIN_VALUE            | 1
		YeOldeJavaVersion.MIN_VALUE            | new YeOldeJavaVersion(0, 0, 0, 1)      | -1

		new YeOldeJavaVersion(0, 0, 0, 1)      | new YeOldeJavaVersion(0, 0, 0, 1)      | 0
		new YeOldeJavaVersion(0, 0, 0, 2)      | new YeOldeJavaVersion(0, 0, 0, 1)      | 1
		new YeOldeJavaVersion(0, 0, 0, 1)      | new YeOldeJavaVersion(0, 0, 0, 2)      | -1

		new YeOldeJavaVersion(0, 0, 1, 0)      | new YeOldeJavaVersion(0, 0, 1, 0)      | 0
		new YeOldeJavaVersion(0, 0, 1, 0)      | new YeOldeJavaVersion(0, 0, 0, 1)      | 1
		new YeOldeJavaVersion(0, 0, 0, 1)      | new YeOldeJavaVersion(0, 0, 1, 0)      | -1
		new YeOldeJavaVersion(0, 0, 2, 0)      | new YeOldeJavaVersion(0, 0, 1, 0)      | 1
		new YeOldeJavaVersion(0, 0, 1, 0)      | new YeOldeJavaVersion(0, 0, 2, 0)      | -1

		new YeOldeJavaVersion(0, 1, 0, 0)      | new YeOldeJavaVersion(0, 1, 0, 0)      | 0
		new YeOldeJavaVersion(0, 1, 0, 0)      | new YeOldeJavaVersion(0, 0, 0, 1)      | 1
		new YeOldeJavaVersion(0, 0, 0, 1)      | new YeOldeJavaVersion(0, 1, 0, 0)      | -1
		new YeOldeJavaVersion(0, 2, 0, 0)      | new YeOldeJavaVersion(0, 1, 0, 0)      | 1
		new YeOldeJavaVersion(0, 1, 0, 0)      | new YeOldeJavaVersion(0, 2, 0, 0)      | -1

		new YeOldeJavaVersion(1, 0, 0, 0)      | new YeOldeJavaVersion(1, 0, 0, 0)      | 0
		new YeOldeJavaVersion(1, 0, 0, 0)      | new YeOldeJavaVersion(0, 0, 0, 1)      | 1
		new YeOldeJavaVersion(0, 0, 0, 1)      | new YeOldeJavaVersion(1, 0, 0, 0)      | -1
		new YeOldeJavaVersion(2, 0, 0, 0)      | new YeOldeJavaVersion(1, 0, 0, 0)      | 1
		new YeOldeJavaVersion(1, 0, 0, 0)      | new YeOldeJavaVersion(2, 0, 0, 0)      | -1

		new YeOldeJavaVersion(1, 3, 0, 0)      | new YeOldeJavaVersion(1, 3, 0, 1)      | -1
		new YeOldeJavaVersion(1, 3, 0, 1)      | new YeOldeJavaVersion(1, 3, 1, 0)      | -1
		new YeOldeJavaVersion(1, 3, 1, 0)      | new YeOldeJavaVersion(1, 3, 1, 1)      | -1

		new YeOldeJavaVersion(1, 3, 1, 1)      | new YeOldeJavaVersion(1, 3, 1, 0)      | 1
		new YeOldeJavaVersion(1, 3, 1, 0)      | new YeOldeJavaVersion(1, 3, 0, 1)      | 1
		new YeOldeJavaVersion(1, 3, 0, 1)      | new YeOldeJavaVersion(1, 3, 0, 0)      | 1

		new YeOldeJavaVersion(1, 3, 0, 0, 'x') | new YeOldeJavaVersion(1, 3, 0, 0, 'x') | 0
		new YeOldeJavaVersion(1, 3, 0, 0)      | new YeOldeJavaVersion(1, 3, 0, 0, 'x') | 1
		new YeOldeJavaVersion(1, 3, 0, 0, 'x') | new YeOldeJavaVersion(1, 3, 0, 0)      | -1

		new YeOldeJavaVersion(1, 3, 1, 0, 'x') | new YeOldeJavaVersion(1, 3, 0, 0, 'x') | 1
		new YeOldeJavaVersion(1, 3, 0, 0, 'x') | new YeOldeJavaVersion(1, 3, 1, 0, 'x') | -1
		new YeOldeJavaVersion(1, 3, 0, 0, 'a') | new YeOldeJavaVersion(1, 3, 0, 0, 'b') | -1
		new YeOldeJavaVersion(1, 3, 0, 0, 'b') | new YeOldeJavaVersion(1, 3, 0, 0, 'a') | 1

		compareString = compareDescription(expectedResult)
		objectString = object.toVersionString()
		otherString = other.toVersionString()
	}

	@Unroll('toVersionString() returns #expectedResult for #object')
	'toVersionString() works'() {
		when:
		String result = object.toVersionString()

		then:
		result == expectedResult

		where:
		object                                   | expectedResult
		new YeOldeJavaVersion(0, 0, 0, 0)        | '0.0.0'
		new YeOldeJavaVersion(0, 0, 0, 0, 'x')   | '0.0.0-x'
		new YeOldeJavaVersion(1, 2, 3, 4, 'x')   | '1.2.3_04-x'
		new YeOldeJavaVersion(1, 2, 3, 14, 'x')  | '1.2.3_14-x'
		new YeOldeJavaVersion(1, 2, 3, 114, 'x') | '1.2.3_114-x'
	}

	@Unroll('toShortVersionString() returns #expectedResult for #object')
	'toShortVersionString() works'() {
		when:
		String result = object.toShortVersionString()

		then:
		result == expectedResult

		where:
		object                                   | expectedResult
		new YeOldeJavaVersion(0, 0, 0, 0)        | '0.0'
		new YeOldeJavaVersion(0, 0, 0, 0, 'x')   | '0.0'
		new YeOldeJavaVersion(1, 2, 3, 4, 'x')   | '1.2'
		new YeOldeJavaVersion(1, 2, 3, 14, 'x')  | '1.2'
		new YeOldeJavaVersion(1, 2, 3, 114, 'x') | '1.2'
	}

	@Unroll('toString() returns #expectedResult for #objectString')
	'toString() works'() {
		when:
		String result = object.toString()

		then:
		result == expectedResult

		where:
		object                                   | expectedResult
		new YeOldeJavaVersion(0, 0, 0, 0)        | 'YeOldeJavaVersion{huge=0, major=0, minor=0, patch=0, preReleaseIdentifier=null}'
		new YeOldeJavaVersion(0, 0, 0, 0, 'x')   | 'YeOldeJavaVersion{huge=0, major=0, minor=0, patch=0, preReleaseIdentifier="x"}'
		new YeOldeJavaVersion(1, 2, 3, 4, 'x')   | 'YeOldeJavaVersion{huge=1, major=2, minor=3, patch=4, preReleaseIdentifier="x"}'
		new YeOldeJavaVersion(1, 2, 3, 14, 'x')  | 'YeOldeJavaVersion{huge=1, major=2, minor=3, patch=14, preReleaseIdentifier="x"}'
		new YeOldeJavaVersion(1, 2, 3, 114, 'x') | 'YeOldeJavaVersion{huge=1, major=2, minor=3, patch=114, preReleaseIdentifier="x"}'

		objectString = object.toVersionString()
	}

	def 'compareTo(null) throws exception'() {
		when:
		YeOldeJavaVersion object = new YeOldeJavaVersion(1, 6)
		//noinspection ChangeToOperator
		object.compareTo(null)

		then:
		NullPointerException ex = thrown()
		ex.getMessage() == 'other must not be null!'
	}

	def 'equals(null) returns false'() {
		when:
		YeOldeJavaVersion object = new YeOldeJavaVersion(1, 6)

		then:
		//noinspection ChangeToOperator
		!object.equals(null)
	}

	def 'equals(someOtherClass) returns false'() {
		when:
		YeOldeJavaVersion object = new YeOldeJavaVersion(1, 6)

		then:
		//noinspection ChangeToOperator
		!object.equals(1)
	}

	private static String compareDescription(int value) {
		if (value < 0) {
			return 'is less than'
		}

		if (value > 0) {
			return 'is greater than'
		}

		return 'is equal to'
	}

	def 'serializable'() {
		expect:
		JUnitTools.testSerialization(object)

		where:
		object << [new YeOldeJavaVersion(0, 0, 0, 0, 'foo'), new YeOldeJavaVersion(1, 2, 3, 4, 'bar'), new YeOldeJavaVersion(1, 2, 3, 4)]
	}

	@Unroll
	'withoutPreReleaseIdentifier() works as expected for #input'(YeOldeJavaVersion input, YeOldeJavaVersion expectedResult) {
		when:
		YeOldeJavaVersion result = input.withoutPreReleaseIdentifier()

		then:
		result == expectedResult
		if (input.preReleaseIdentifier == null) {
			assert result.is(input)
		}

		where:
		input                                    | expectedResult
		new YeOldeJavaVersion(0, 0, 0, 0, 'foo') | new YeOldeJavaVersion(0, 0, 0, 0)
		new YeOldeJavaVersion(1, 2, 3, 4, 'foo') | new YeOldeJavaVersion(1, 2, 3, 4)
		new YeOldeJavaVersion(1, 2, 3, 4)        | new YeOldeJavaVersion(1, 2, 3, 4)
	}

	/*
    Adding XML serialization would require default constructor and setters. So: nope.
    def 'serializable as XML'() {
        expect:
        JUnitTools.testXmlSerialization(object)

        where:
        object << [new YeOldeJavaVersion(0,0,0,0,'foo'), new YeOldeJavaVersion(1,2,3,4,'bar'), new YeOldeJavaVersion(1,2,3,4)]
    }
    */
}
