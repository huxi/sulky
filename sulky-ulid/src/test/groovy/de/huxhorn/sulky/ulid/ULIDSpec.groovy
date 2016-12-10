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

import spock.lang.Specification
import spock.lang.Unroll

class ULIDSpec extends Specification {
    @Unroll
    def 'internalAppendCrockford(.., #inputValue, #length) returns "#expectedResult".'() {
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
        0xFFFF_FFFF_FFFF_FFFFL | 13     | 'FZZZZZZZZZZZZ'
        0x1FL                  | 0      | ''
        1481195424879L         | 10     | '01B3F2133F'
        0xFFFF_FFFF_FFFFL      | 10     | '7ZZZZZZZZZ'
    }

    def 'internalAppendULID(..) with 0 as random values returns expected result.'() {
        given:
        long timeStamp = 1481195424879L
        Random random = Mock(Random)
        StringBuilder builder = new StringBuilder()

        when:
        ULID.internalAppendULID(builder, timeStamp, random)
        String result = builder.toString()

        then:
        2 * random.nextLong() >> 0
        result == '01B3F2133F0000000000000000'
    }

    def 'internalAppendULID(..) with -1 as random values returns expected result.'() {
        given:
        long timeStamp = 1481195424879L
        Random random = Mock(Random)
        StringBuilder builder = new StringBuilder()

        when:
        ULID.internalAppendULID(builder, timeStamp, random)
        String result = builder.toString()

        then:
        2 * random.nextLong() >> -1
        result == '01B3F2133FZZZZZZZZZZZZZZZZ'
    }

    def 'nextULID() with 0 as random values returns expected result.'() {
        given:
        Random random = Mock(Random)
        ULID ulid = new ULID(random)

        when:
        String result = ulid.nextULID()

        then:
        2 * random.nextLong() >> 0

        '01B3F2133F' < result.substring(0, 10)
        result.substring(10) == '0000000000000000'
    }

    def 'nextULID() with -1 as random values returns expected result.'() {
        given:
        Random random = Mock(Random)
        ULID ulid = new ULID(random)

        when:
        String result = ulid.nextULID()

        then:
        2 * random.nextLong() >> -1

        '01B3F2133F' < result.substring(0, 10)
        result.substring(10) == 'ZZZZZZZZZZZZZZZZ'
    }

    def 'nextULID() with real random values returns sane result.'() {
        given:
        ULID ulid = new ULID()

        when:
        String result = ulid.nextULID()

        then:
        result.length() == 26
        '01B3F2133F' < result.substring(0, 10)
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
}
