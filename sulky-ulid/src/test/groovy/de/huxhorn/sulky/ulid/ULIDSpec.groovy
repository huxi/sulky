/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2016 Joern Huxhorn
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
 * Copyright 2007-2016 Joern Huxhorn
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

package de.huxhorn.sulky.ulid

import de.huxhorn.sulky.junit.JUnitTools
import spock.lang.Specification
import spock.lang.Unroll

@SuppressWarnings("ChangeToOperator")
class ULIDSpec extends Specification {

	private static final long PAST_TIMESTAMP = 1481195424879L
	private static final String PAST_TIMESTAMP_PART = '01B3F2133F'

	private static final long MIN_TIMESTAMP = 0x0L
	private static final String MIN_TIMESTAMP_PART = '0000000000'

	private static final long MAX_TIMESTAMP = 0xFFFF_FFFF_FFFFL
	private static final String MAX_TIMESTAMP_PART = '7ZZZZZZZZZ'

	private static final String MIN_RANDOM_PART = '0000000000000000'
	private static final String MAX_RANDOM_PART = 'ZZZZZZZZZZZZZZZZ'

	private static final byte[] ZERO_BYTES = new byte[16]
	private static final byte[] FULL_BYTES = [
			0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF,
			0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF
	] as byte[]

	private static final byte[] PATTERN_BYTES = [
			0x00, 0x11, 0x22, 0x33, 0x44, 0x55, 0x66, 0x77,
			0x88, 0x99, 0xAA, 0xBB, 0xCC, 0xDD, 0xEE, 0xFF
	] as byte[]
	private static final long PATTERN_MOST_SIGNIFICANT_BITS = 0x0011_2233_4455_6677L
	private static final long PATTERN_LEAST_SIGNIFICANT_BITS = 0x8899_AABB_CCDD_EEFFL

	private static final long ALL_BITS_SET = 0xFFFF_FFFF_FFFF_FFFFL

	@Unroll
	def 'internalAppendCrockford(StringBuilder, #inputValue, #length) returns "#expectedResult".'() {
		given:
		StringBuilder builder = new StringBuilder()

		when:
		ULID.internalAppendCrockford(builder, inputValue, length)

		then:
		builder.toString() == expectedResult

		where:
		inputValue             | length | expectedResult
		0L                     | 13     | '0000000000000'
		0L                     | 1      | '0'
		1L                     | 1      | '1'
		194L                   | 2      | '62'
		45_678L                | 4      | '1CKE'
		393_619L               | 4      | 'C0CK'
		398_373L               | 4      | 'C515'
		421_562L               | 4      | 'CVNT'
		456_789L               | 4      | 'DY2N'
		519_571L               | 4      | 'FVCK'
		3_838_385_658_376_483L | 11     | '3D2ZQ6TVC93'
		0x1FL                  | 1      | 'Z'
		0x1FL << 5             | 1      | '0'
		0x1FL << 5             | 2      | 'Z0'
		0x1FL << 10            | 1      | '0'
		0x1FL << 10            | 2      | '00'
		0x1FL << 10            | 3      | 'Z00'
		0x1FL << 15            | 3      | '000'
		0x1FL << 15            | 4      | 'Z000'
		0x1FL << 55            | 13     | '0Z00000000000'
		0x1FL << 60            | 13     | 'F000000000000'
		ALL_BITS_SET           | 13     | 'FZZZZZZZZZZZZ'
		0x1FL                  | 0      | ''
		PAST_TIMESTAMP         | 10     | PAST_TIMESTAMP_PART
		MAX_TIMESTAMP          | 10     | MAX_TIMESTAMP_PART
	}

	@Unroll
	def 'internalWriteCrockford(char[#bufferSize], #inputValue, #length, #offset) returns "#expectedResult".'() {
		given:
		char[] buffer = new char[bufferSize]
		for (int i = 0; i < buffer.length; i++) {
			buffer[i] = (char) '#'
		}

		when:
		ULID.internalWriteCrockford(buffer, inputValue, length, offset)
		String result = new String(buffer)

		then:
		result == expectedResult

		where:
		inputValue             | bufferSize | length | offset | expectedResult
		0L                     | 13         | 13     | 0      | '0000000000000'
		0L                     | 1          | 1      | 0      | '0'
		1L                     | 1          | 1      | 0      | '1'
		194L                   | 2          | 2      | 0      | '62'
		45_678L                | 4          | 4      | 0      | '1CKE'
		393_619L               | 4          | 4      | 0      | 'C0CK'
		398_373L               | 4          | 4      | 0      | 'C515'
		421_562L               | 4          | 4      | 0      | 'CVNT'
		456_789L               | 4          | 4      | 0      | 'DY2N'
		519_571L               | 4          | 4      | 0      | 'FVCK'
		3_838_385_658_376_483L | 11         | 11     | 0      | '3D2ZQ6TVC93'
		0x1FL                  | 1          | 1      | 0      | 'Z'
		0x1FL << 5             | 1          | 1      | 0      | '0'
		0x1FL << 5             | 2          | 2      | 0      | 'Z0'
		0x1FL << 10            | 1          | 1      | 0      | '0'
		0x1FL << 10            | 2          | 2      | 0      | '00'
		0x1FL << 10            | 3          | 3      | 0      | 'Z00'
		0x1FL << 15            | 3          | 3      | 0      | '000'
		0x1FL << 15            | 4          | 4      | 0      | 'Z000'
		0x1FL << 55            | 13         | 13     | 0      | '0Z00000000000'
		0x1FL << 60            | 13         | 13     | 0      | 'F000000000000'
		ALL_BITS_SET           | 13         | 13     | 0      | 'FZZZZZZZZZZZZ'
		0x1FL                  | 0          | 0      | 0      | ''
		PAST_TIMESTAMP         | 10         | 10     | 0      | PAST_TIMESTAMP_PART
		MAX_TIMESTAMP          | 10         | 10     | 0      | MAX_TIMESTAMP_PART
		45_678L                | 8          | 4      | 3      | '###1CKE#'
		45_678L                | 8          | 4      | 4      | '####1CKE'
	}

	def 'internalAppendULID(..) with 0 as random values returns expected result.'() {
		given:
		Random random = Mock(Random)
		StringBuilder builder = new StringBuilder()

		when:
		ULID.internalAppendULID(builder, PAST_TIMESTAMP, random)
		String result = builder.toString()

		then:
		2 * random.nextLong() >> 0

		result.length() == 26
		String timePart = result.substring(0, 10)
		String randomPart = result.substring(10)
		timePart == PAST_TIMESTAMP_PART
		randomPart == '0000000000000000'
	}

	def 'internalAppendULID(..) with -1 as random values returns expected result.'() {
		given:
		Random random = Mock(Random)
		StringBuilder builder = new StringBuilder()

		when:
		ULID.internalAppendULID(builder, PAST_TIMESTAMP, random)
		String result = builder.toString()

		then:
		2 * random.nextLong() >> -1

		result.length() == 26
		String timePart = result.substring(0, 10)
		String randomPart = result.substring(10)
		timePart == PAST_TIMESTAMP_PART
		randomPart == 'ZZZZZZZZZZZZZZZZ'
	}

	def 'internalUIDString(..) with 0 as random values returns expected result.'() {
		given:
		Random random = Mock(Random)

		when:
		String result = ULID.internalUIDString(PAST_TIMESTAMP, random)

		then:
		2 * random.nextLong() >> 0

		result.length() == 26
		String timePart = result.substring(0, 10)
		String randomPart = result.substring(10)
		timePart == PAST_TIMESTAMP_PART
		randomPart == '0000000000000000'
	}

	def 'internalUIDString(..) with -1 as random values returns expected result.'() {
		given:
		Random random = Mock(Random)

		when:
		String result = ULID.internalUIDString(PAST_TIMESTAMP, random)

		then:
		2 * random.nextLong() >> -1

		result.length() == 26
		String timePart = result.substring(0, 10)
		String randomPart = result.substring(10)
		timePart == PAST_TIMESTAMP_PART
		randomPart == 'ZZZZZZZZZZZZZZZZ'
	}

	def 'nextULID() with 0 as random values returns expected result.'() {
		given:
		Random random = Mock(Random)
		ULID ulid = new ULID(random)

		when:
		String result = ulid.nextULID()

		then:
		2 * random.nextLong() >> 0

		result.length() == 26
		String timePart = result.substring(0, 10)
		String randomPart = result.substring(10)
		PAST_TIMESTAMP_PART.compareTo(timePart) < 0
		MAX_TIMESTAMP_PART.compareTo(timePart) >= 0
		randomPart == '0000000000000000'
	}

	def 'nextULID() with -1 as random values returns expected result.'() {
		given:
		Random random = Mock(Random)
		ULID ulid = new ULID(random)

		when:
		String result = ulid.nextULID()

		then:
		2 * random.nextLong() >> -1

		result.length() == 26
		String timePart = result.substring(0, 10)
		String randomPart = result.substring(10)
		PAST_TIMESTAMP_PART.compareTo(timePart) < 0
		MAX_TIMESTAMP_PART.compareTo(timePart) >= 0
		randomPart == 'ZZZZZZZZZZZZZZZZ'
	}

	def 'nextULID() with real random values returns sane result.'() {
		given:
		ULID ulid = new ULID()

		when:
		String result = ulid.nextULID()

		then:
		result.length() == 26
		String timePart = result.substring(0, 10)
		String randomPart = result.substring(10)
		PAST_TIMESTAMP_PART.compareTo(timePart) < 0
		MAX_TIMESTAMP_PART.compareTo(timePart) >= 0
		MIN_RANDOM_PART.compareTo(randomPart) <= 0
		MAX_RANDOM_PART.compareTo(randomPart) >= 0
	}
// xxxxxxx
	def 'nextValue() with 0 as random values returns expected result.'() {
		given:
		Random random = Mock(Random)
		ULID ulid = new ULID(random)

		when:
		String result = ulid.nextValue().toString()

		then:
		2 * random.nextLong() >> 0

		result.length() == 26
		String timePart = result.substring(0, 10)
		String randomPart = result.substring(10)
		PAST_TIMESTAMP_PART.compareTo(timePart) < 0
		MAX_TIMESTAMP_PART.compareTo(timePart) >= 0
		randomPart == '0000000000000000'
	}

	def 'nextValue() with -1 as random values returns expected result.'() {
		given:
		Random random = Mock(Random)
		ULID ulid = new ULID(random)

		when:
		String result = ulid.nextValue().toString()

		then:
		2 * random.nextLong() >> -1

		result.length() == 26
		String timePart = result.substring(0, 10)
		String randomPart = result.substring(10)
		PAST_TIMESTAMP_PART.compareTo(timePart) < 0
		MAX_TIMESTAMP_PART.compareTo(timePart) >= 0
		randomPart == 'ZZZZZZZZZZZZZZZZ'
	}

	def 'nextValue() with real random values returns sane result.'() {
		given:
		ULID ulid = new ULID()

		when:
		String result = ulid.nextValue().toString()

		then:
		result.length() == 26
		String timePart = result.substring(0, 10)
		String randomPart = result.substring(10)
		PAST_TIMESTAMP_PART.compareTo(timePart) < 0
		MAX_TIMESTAMP_PART.compareTo(timePart) >= 0
		MIN_RANDOM_PART.compareTo(randomPart) <= 0
		MAX_RANDOM_PART.compareTo(randomPart) >= 0
	}

	def 'appendULID(StringBuilder) with 0 as random values appends expected result.'() {
		given:
		Random random = Mock(Random)
		ULID ulid = new ULID(random)
		StringBuilder stringBuilder = new StringBuilder()

		when:
		ulid.appendULID(stringBuilder)
		String result = stringBuilder.toString()

		then:
		2 * random.nextLong() >> 0

		result.length() == 26
		String timePart = result.substring(0, 10)
		String randomPart = result.substring(10)
		PAST_TIMESTAMP_PART.compareTo(timePart) < 0
		MAX_TIMESTAMP_PART.compareTo(timePart) >= 0
		randomPart == '0000000000000000'
	}

	def 'appendULID(StringBuilder) with -1 as random values appends expected result.'() {
		given:
		Random random = Mock(Random)
		ULID ulid = new ULID(random)
		StringBuilder stringBuilder = new StringBuilder()

		when:
		ulid.appendULID(stringBuilder)
		String result = stringBuilder.toString()

		then:
		2 * random.nextLong() >> -1

		result.length() == 26
		String timePart = result.substring(0, 10)
		String randomPart = result.substring(10)
		PAST_TIMESTAMP_PART.compareTo(timePart) < 0
		MAX_TIMESTAMP_PART.compareTo(timePart) >= 0
		randomPart == 'ZZZZZZZZZZZZZZZZ'
	}

	def 'appendULID(StringBuilder) with real random values appends sane result.'() {
		given:
		ULID ulid = new ULID()
		StringBuilder stringBuilder = new StringBuilder()

		when:
		ulid.appendULID(stringBuilder)
		String result = stringBuilder.toString()

		then:
		result.length() == 26
		String timePart = result.substring(0, 10)
		String randomPart = result.substring(10)
		PAST_TIMESTAMP_PART.compareTo(timePart) < 0
		MAX_TIMESTAMP_PART.compareTo(timePart) >= 0
		MIN_RANDOM_PART.compareTo(randomPart) <= 0
		MAX_RANDOM_PART.compareTo(randomPart) >= 0
	}

	def 'ULID(null) fails as expected.'() {
		when:
		new ULID(null)

		then:
		NullPointerException ex = thrown()
		ex.message == 'random must not be null!'
	}

	def 'appendULID(null) fails as expected.'() {
		when:
		def instance = new ULID()
		instance.appendULID(null)

		then:
		NullPointerException ex = thrown()
		ex.message == 'stringBuilder must not be null!'
	}

	@Unroll
	def 'ULID.Value(#most, #least).toString() returns expected #expectedResult.'() {
		when:
		String result = (new ULID.Value(most, least)).toString()

		then:
		result == expectedResult

		where:

		most                   | least                  | expectedResult
		0x0L                   | 0x0L                   | '00000000000000000000000000'
		ALL_BITS_SET           | ALL_BITS_SET           | '7ZZZZZZZZZZZZZZZZZZZZZZZZZ'
		0xFFFF_FFFF_FFFF_0000L | 0x0L                   | '7ZZZZZZZZZ0000000000000000'
		0xFFFFL                | ALL_BITS_SET           | '0000000000ZZZZZZZZZZZZZZZZ'
		0x1L                   | ALL_BITS_SET           | '0000000000000ZZZZZZZZZZZZZ'
		0x3L                   | ALL_BITS_SET           | '0000000000001ZZZZZZZZZZZZZ'
		0xFFFFL                | 0x0L                   | '0000000000ZZZG000000000000'
		0x0886L                | 0x4298_E84A_96C6_B9F0L | '0000000000123456789ABCDEFG'
	}

	@Unroll
	def 'internalParseCrockford("#input") returns expected result #expectedResult.'() {
		when:
		long result = ULID.internalParseCrockford(input)

		then:
		result == expectedResult

		where:
		input               | expectedResult
		'0'                 | 0L
		'O'                 | 0L
		'o'                 | 0L
		'1'                 | 1L
		'i'                 | 1L
		'I'                 | 1L
		'l'                 | 1L
		'L'                 | 1L
		'2'                 | 2L
		'3'                 | 3L
		'4'                 | 4L
		'5'                 | 5L
		'6'                 | 6L
		'7'                 | 7L
		'8'                 | 8L
		'9'                 | 9L
		'A'                 | 10L
		'a'                 | 10L
		'B'                 | 11L
		'b'                 | 11L
		'C'                 | 12L
		'c'                 | 12L
		'D'                 | 13L
		'd'                 | 13L
		'E'                 | 14L
		'e'                 | 14L
		'F'                 | 15L
		'f'                 | 15L
		'G'                 | 16L
		'g'                 | 16L
		'H'                 | 17L
		'h'                 | 17L
		'J'                 | 18L
		'j'                 | 18L
		'K'                 | 19L
		'k'                 | 19L
		'M'                 | 20L
		'm'                 | 20L
		'N'                 | 21L
		'n'                 | 21L
		'P'                 | 22L
		'p'                 | 22L
		'Q'                 | 23L
		'q'                 | 23L
		'R'                 | 24L
		'r'                 | 24L
		'S'                 | 25L
		's'                 | 25L
		'T'                 | 26L
		't'                 | 26L
		'V'                 | 27L
		'v'                 | 27L
		'W'                 | 28L
		'w'                 | 28L
		'X'                 | 29L
		'x'                 | 29L
		'Y'                 | 30L
		'y'                 | 30L
		'Z'                 | 31L
		'z'                 | 31L
		'EDNA3444'          | 0x73_6AA1_9084L
		'ZZZZZZZZZZZZ'      | 0xFFF_FFFF_FFFF_FFFFL
		PAST_TIMESTAMP_PART | PAST_TIMESTAMP
		''                  | 0L
	}

	def 'internalParseCrockford with input that is too long fails as expected.'() {
		when:
		ULID.internalParseCrockford('ZZZZZZZZZZZZZ')

		then:
		IllegalArgumentException ex = thrown()
		ex.message == 'input length must not exceed 12 but was 13!'
	}

	def 'internalParseCrockford with null input fails as expected.'() {
		when:
		ULID.internalParseCrockford(null)

		then:
		NullPointerException ex = thrown()
		ex.message == 'input must not be null!'
	}

	@Unroll
	def 'internalParseCrockford with illegal input "#input" fails as expected.'() {
		when:
		ULID.internalParseCrockford(input)

		then:
		IllegalArgumentException ex = thrown()
		ex.message == 'Illegal character \'' + illegalCharacter + '\'!'

		where:
		input  | illegalCharacter
		'fuck' | 'u'
		'©opy' | '©'
	}

	@Unroll
	def 'parseULID("#input") and toString() works as expected.'() {
		when:
		ULID.Value value = ULID.parseULID(input)

		then:
		value.toString() == input

		and:
		value.timestamp() == expectedTimestamp

		where:
		input                                    | expectedTimestamp
		PAST_TIMESTAMP_PART + '0000000000000000' | PAST_TIMESTAMP
		PAST_TIMESTAMP_PART + 'ZZZZZZZZZZZZZZZZ' | PAST_TIMESTAMP
		PAST_TIMESTAMP_PART + '123456789ABCDEFG' | PAST_TIMESTAMP
		PAST_TIMESTAMP_PART + '1000000000000000' | PAST_TIMESTAMP
		PAST_TIMESTAMP_PART + '1000000000000001' | PAST_TIMESTAMP
		PAST_TIMESTAMP_PART + '0001000000000001' | PAST_TIMESTAMP
		PAST_TIMESTAMP_PART + '0100000000000001' | PAST_TIMESTAMP
		PAST_TIMESTAMP_PART + '0000000000000001' | PAST_TIMESTAMP
		MIN_TIMESTAMP_PART + '123456789ABCDEFG'  | MIN_TIMESTAMP
		MAX_TIMESTAMP_PART + '123456789ABCDEFG'  | MAX_TIMESTAMP
	}

	@Unroll
	def 'parseULID("#input") fails as expected.'() {
		when:
		ULID.parseULID(input)

		then:
		IllegalArgumentException ex = thrown()
		ex.message == 'ulidString must be exactly 26 chars long.'

		where:
		input << ['0000000000000000000000000', '000000000000000000000000000']
	}

	def 'parseULID(null) fails as expected.'() {
		when:
		ULID.parseULID(null)

		then:
		NullPointerException ex = thrown()
		ex.message == 'ulidString must not be null!'
	}

	@Unroll
	def 'fromBytes(#data) works as expected.'() {
		when:
		ULID.Value value = ULID.fromBytes(data)

		then:
		value.mostSignificantBits == mostSignificantBits
		value.leastSignificantBits == leastSignificantBits

		where:
		data          | mostSignificantBits           | leastSignificantBits
		ZERO_BYTES    | 0L                            | 0L
		FULL_BYTES    | ALL_BITS_SET                  | ALL_BITS_SET
		PATTERN_BYTES | PATTERN_MOST_SIGNIFICANT_BITS | PATTERN_LEAST_SIGNIFICANT_BITS
	}

	@Unroll
	def 'fromBytes(#data) fails as expected.'() {
		when:
		ULID.fromBytes(data)

		then:
		IllegalArgumentException ex = thrown()
		ex.message == 'data must be 16 bytes in length!'

		where:
		data << [new byte[15], new byte[17]]
	}

	def 'fromBytes(null) fails as expected.'() {
		when:
		ULID.fromBytes(null)

		then:
		NullPointerException ex = thrown()
		ex.message == 'data must not be null!'
	}

	@Unroll
	def 'ULID.Value(#mostSignificantBits, #leastSignificantBits).toBytes() works as expected.'() {
		given:
		ULID.Value value = new ULID.Value(mostSignificantBits, leastSignificantBits)

		when:
		byte[] data = value.toBytes()

		then:
		data == expectedData

		where:
		mostSignificantBits           | leastSignificantBits           | expectedData
		0L                            | 0L                             | ZERO_BYTES
		ALL_BITS_SET                  | ALL_BITS_SET                   | FULL_BYTES
		PATTERN_MOST_SIGNIFICANT_BITS | PATTERN_LEAST_SIGNIFICANT_BITS | PATTERN_BYTES
	}

	def 'compareTo, equals and hashCode work as expected.'() {
		given:
		def value1 = new ULID.Value(mostSignificantBits1, leastSignificantBits1)
		def value2 = new ULID.Value(mostSignificantBits2, leastSignificantBits2)

		when:
		boolean equals12 = value1.equals(value2)
		boolean equals21 = value2.equals(value1)
		int compare12 = value1.compareTo(value2)
		int compare21 = value2.compareTo(value1)
		int hash1 = value1.hashCode()
		int hash2 = value2.hashCode()

		then:
		equals12 == equals21
		compare12 == compare21 * -1
		if (compare == 0) {
			assert hash1 == hash2
		} else {
			assert compare12 == compare
			assert !equals12
		}

		where:
		mostSignificantBits1          | leastSignificantBits1          | mostSignificantBits2          | leastSignificantBits2          | compare
		0L                            | 0L                             | 0L                            | 0L                             | 0
		ALL_BITS_SET                  | ALL_BITS_SET                   | ALL_BITS_SET                  | ALL_BITS_SET                   | 0
		PATTERN_MOST_SIGNIFICANT_BITS | PATTERN_LEAST_SIGNIFICANT_BITS | PATTERN_MOST_SIGNIFICANT_BITS | PATTERN_LEAST_SIGNIFICANT_BITS | 0
		0L                            | 1L                             | 0L                            | 0L                             | 1
		1 << 16                       | 0L                             | 0L                            | 0L                             | 1
	}

	def 'ULID.Value is equal to itself.'() {
		given:
		def value = new ULID.Value(0, 0)

		expect:
		value.equals(value)
	}

	def 'ULID.Value is not equal to special cases.'() {
		given:
		def value = new ULID.Value(0, 0)

		expect:
		!value.equals(null)
		!value.equals('')
	}

	@Unroll
	def 'ULID.Value(#mostSignificantBits, #leastSignificantBits) serialization works as expected.'() {
		given:
		ULID.Value value = new ULID.Value(mostSignificantBits, leastSignificantBits)

		expect:
		JUnitTools.testSerialization(value)

		where:
		mostSignificantBits           | leastSignificantBits
		0L                            | 0L
		ALL_BITS_SET                  | ALL_BITS_SET
		PATTERN_MOST_SIGNIFICANT_BITS | PATTERN_LEAST_SIGNIFICANT_BITS
	}
}
