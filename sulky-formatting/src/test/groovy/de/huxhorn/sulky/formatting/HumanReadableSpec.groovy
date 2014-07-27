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

package de.huxhorn.sulky.formatting

import spock.lang.Specification
import spock.lang.Unroll

class HumanReadableSpec extends Specification {

    def data() {
        [
                // @formatter:off
                [                  -1, false, false, '-1 '],
                [                  -1, false, true,  '-1 '],
                [                  -1, true,  false, '-1 '],
                [                  -1, true,  true,  '-1 '],

                [                   0, false, false, '0 '],
                [                   0, false, true,  '0 '],
                [                   0, true,  false, '0 '],
                [                   0, true,  true,  '0 '],

                [                   1, false, false, '1 '],
                [                   1, false, true,  '1 '],
                [                   1, true,  false, '1 '],
                [                   1, true,  true,  '1 '],

                [                 999, false, false, '999 '],
                [                 999, false, true,  '999 '],
                [                 999, true,  false, '999 '],
                [                 999, true,  true,  '999 '],

                [                1000, false, false, '1.00 kilo'],
                [                1000, false, true,  '1.00 k'],
                [                1000, true,  false, '1000 '],
                [                1000, true,  true,  '1000 '],

                [                1001, false, false, '1.00 kilo'],
                [                1001, false, true,  '1.00 k'],
                [                1001, true,  false, '1001 '],
                [                1001, true,  true,  '1001 '],

                [                1023, false, false, '1.02 kilo'],
                [                1023, false, true,  '1.02 k'],
                [                1023, true,  false, '1023 '],
                [                1023, true,  true,  '1023 '],

                [                1024, false, false, '1.02 kilo'],
                [                1024, false, true,  '1.02 k'],
                [                1024, true,  false, '1.00 kibi'],
                [                1024, true,  true,  '1.00 Ki'],

                [                1337, false, false, '1.34 kilo'],
                [                1337, false, true,  '1.34 k'],
                [                1337, true,  false, '1.31 kibi'],
                [                1337, true,  true,  '1.31 Ki'],

                [               -1337, false, false, '-1.34 kilo'],
                [               -1337, false, true,  '-1.34 k'],
                [               -1337, true,  false, '-1.31 kibi'],
                [               -1337, true,  true,  '-1.31 Ki'],

                [            2000000L, false, false, '2.00 mega'],
                [            2000000L, false, true,  '2.00 M'],
                [            2000000L, true,  false, '1.91 mebi'],
                [            2000000L, true,  true,  '1.91 Mi'],

                [            2097152L, false, false, '2.10 mega'],
                [            2097152L, false, true,  '2.10 M'],
                [            2097152L, true,  false, '2.00 mebi'],
                [            2097152L, true,  true,  '2.00 Mi'],

                [1000000000000000000L, false, false, '1.00 exa'],
                [1000000000000000000L, false, true,  '1.00 E'],
                [1000000000000000000L, true,  false, '888.18 pebi'],
                [1000000000000000000L, true,  true,  '888.18 Pi'],

                [1152921504606846976L, false, false, '1.15 exa'],
                [1152921504606846976L, false, true,  '1.15 E'],
                [1152921504606846976L, true,  false, '1.00 exbi'],
                [1152921504606846976L, true,  true,  '1.00 Ei'],

                [Short.MAX_VALUE,      false, false, '32.77 kilo'],
                [Short.MAX_VALUE,      false, true,  '32.77 k'],
                [Short.MAX_VALUE,      true,  false, '32.00 kibi'],
                [Short.MAX_VALUE,      true,  true,  '32.00 Ki'],

                [Short.MIN_VALUE,      false, false, '-32.77 kilo'],
                [Short.MIN_VALUE,      false, true,  '-32.77 k'],
                [Short.MIN_VALUE,      true,  false, '-32.00 kibi'],
                [Short.MIN_VALUE,      true,  true,  '-32.00 Ki'],

                [Integer.MAX_VALUE,    false, false, '2.15 giga'],
                [Integer.MAX_VALUE,    false, true,  '2.15 G'],
                [Integer.MAX_VALUE,    true,  false, '2.00 gibi'],
                [Integer.MAX_VALUE,    true,  true,  '2.00 Gi'],

                [Integer.MIN_VALUE,    false, false, '-2.15 giga'],
                [Integer.MIN_VALUE,    false, true,  '-2.15 G'],
                [Integer.MIN_VALUE,    true,  false, '-2.00 gibi'],
                [Integer.MIN_VALUE,    true,  true,  '-2.00 Gi'],

                [Long.MAX_VALUE,       false, false, '9.22 exa'],
                [Long.MAX_VALUE,       false, true,  '9.22 E'],
                [Long.MAX_VALUE,       true,  false, '8.00 exbi'],
                [Long.MAX_VALUE,       true,  true,  '8.00 Ei'],

                [Long.MIN_VALUE,       false, false, '-9.22 exa'],
                [Long.MIN_VALUE,       false, true,  '-9.22 E'],
                [Long.MIN_VALUE,       true,  false, '-8.00 exbi'],
                [Long.MIN_VALUE,       true,  true,  '-8.00 Ei'],
                // @formatter:on
        ]
    }

    @Unroll
    def "getHumanReadableSize(size=#size, useBinaryUnits=#useBinaryUnits, useSymbol=#useSymbol) should return '#expectedResult'."(long size, boolean useBinaryUnits, boolean useSymbol, String expectedResult) {
        when:
        String result = HumanReadable.getHumanReadableSize(size, useBinaryUnits, useSymbol)

        then:
        result == expectedResult

        where:
        [size, useBinaryUnits, useSymbol, expectedResult] << data()
    }
}
