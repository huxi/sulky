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

class SimpleXmlSpec extends Specification {

    def escapedXmlCharacters() {
        [
                ['&' as char, '&amp;'],
                ['<' as char, '&lt;'],
                ['>' as char, '&gt;'],
                ['\"' as char, '&quot;'],
        ]
    }

    @Unroll
    def "escape(character=#character) should return #escaped"(char character, String escaped) {
        when:
        String result = SimpleXml.escape(''+character)

        then:
        result == escaped

        where:
        [character, escaped] << escapedXmlCharacters()
    }

    def "special case escape(character=0) should return space"() {
        expect:
        ' ' == SimpleXml.escape(''+((char)0))
    }

    @Unroll
    def "unescape(escaped=#escaped) should return #character"(char character, String escaped) {
        when:
        String result = SimpleXml.unescape(escaped)

        then:
        result == ''+character

        where:
        [character, escaped] << escapedXmlCharacters()
    }


    /**
     * Char	   ::=   	#x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
     * (any Unicode character, excluding the surrogate blocks, FFFE, and FFFF.)
     *
     * See http://www.w3.org/TR/REC-xml#charsets
     */
    def validXmlEdgeCaseChars() {
        [
                // @formatter:off
                [false,      0x8],
                [true ,      0x9],
                [true ,      0xA],
                [false,      0xB],
                [false,      0xC],
                [true ,      0xD],
                [false,      0xE],
                [false,     0x19],
                [true,      0x20],
                [true,    0xD7FF],
                [false,   0xD800],
                [false,   0xDFFF],
                [true,    0xE000],
                [true,    0xFFFD],
                [false,   0xFFFE],
                [false,   0xFFFF],
                // @formatter:on
        ]
    }

    /**
     * Char	   ::=   	#x9 | #xA | #xD | [#x20-#xD7FF] | [#xE000-#xFFFD] | [#x10000-#x10FFFF]
     * (any Unicode character, excluding the surrogate blocks, FFFE, and FFFF.)
     *
     * See http://www.w3.org/TR/REC-xml#charsets
     */
    def validXmlEdgeCaseIntegers() {
        [
                // @formatter:off
                [true,   0x10000],
                [true,  0x10FFFF],
                [true,  0x10FFFF],
                [false, 0x110000],
                // @formatter:on
        ]
    }

    @Unroll
    def "char edge case: isValidXMLCharacter((int)codePoint=#codePoint) should return #valid"(boolean valid, int codePoint) {
        when:
        boolean result = SimpleXml.isValidXMLCharacter(codePoint)

        then:
        result == valid

        where:
        [valid, codePoint] << validXmlEdgeCaseChars()
    }

    @Unroll
    def "integer edge case: isValidXMLCharacter((int)codePoint=#codePoint) should return #valid"(boolean valid, int codePoint) {
        when:
        boolean result = SimpleXml.isValidXMLCharacter(codePoint)

        then:
        result == valid

        where:
        [valid, codePoint] << validXmlEdgeCaseIntegers()
    }

    @Unroll
    def "isValidXMLCharacter((char)codePoint=#codePoint) should return #valid"(boolean valid, int codePoint) {
        when:
        boolean result = SimpleXml.isValidXMLCharacter((char)codePoint)

        then:
        result == valid || codePoint != (codePoint & 0xFFFF)

        where:
        [valid, codePoint] << validXmlEdgeCaseChars()
    }

    @Unroll
    def "replaceNonValidXMLCharacters((char)codePoint=#codePoint)"(boolean valid, int codePoint) {
        when:
        char replacement = '#'
        String input = new StringBuilder().appendCodePoint(codePoint).toString();
        String result = SimpleXml.replaceNonValidXMLCharacters(input, replacement)

        then:
        (valid && result == input) || (!valid && result.codePointAt(0) == (int)replacement)

        where:
        [valid, codePoint] << validXmlEdgeCaseChars()
    }

    def "replaceNonValidXMLCharacters with invalid replacement character"() {
        when:
        SimpleXml.replaceNonValidXMLCharacters('foo', (char)0xFFFF)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'Replacement character 0xFFFF is invalid itself!'
    }

    def "replaceNonValidXMLCharacters with valid input"() {
        when:
        String input = 'foo'
        String result = SimpleXml.replaceNonValidXMLCharacters('foo', ' ' as char)

        then:
        input == result
        input.is(result)
    }

    def "replaceNonValidXMLCharacters with some replacement"() {
        when:
        String result = SimpleXml.replaceNonValidXMLCharacters('foo\u0019\uD800bar', ' ' as char)

        then:
        'foo  bar' == result
    }
}
