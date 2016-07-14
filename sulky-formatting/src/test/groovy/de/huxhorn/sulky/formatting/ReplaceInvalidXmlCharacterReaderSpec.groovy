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

package de.huxhorn.sulky.formatting

import spock.lang.Specification

class ReplaceInvalidXmlCharacterReaderSpec extends Specification {
	def 'Default replacement char is used in case of single arg constructor.'() {
		when:
		def instance = new ReplaceInvalidXmlCharacterReader(new StringReader('foo'))

		then:
		instance.replacementChar == ReplaceInvalidXmlCharacterReader.DEFAULT_REPLACEMENT_CHARACTER
	}

	def 'Expected replacement char is used in case of given replacement char.'() {
		when:
		def instance = new ReplaceInvalidXmlCharacterReader(new StringReader('foo'), replacement)

		then:
		instance.replacementChar == replacement

		where:
		replacement << ['!' as char, ' ' as char]

	}

	def 'Invalid replacement character throws expected exception.'() {
		when:
		new ReplaceInvalidXmlCharacterReader(new StringReader('foo'), 0xFFFF as char)

		then:
		IllegalArgumentException ex = thrown()
		ex.message == 'Replacement character 0xFFFF is invalid itself!'
	}

	def 'Invalid chars are replaced as expected in read().'() {
		setup:
		def instance = new ReplaceInvalidXmlCharacterReader(new StringReader('foo' + (0 as char) + (0xFFFF as char) + 'bar'), '#' as char)

		expect:
		instance.read() as char == 'f' as char
		instance.read() as char == 'o' as char
		instance.read() as char == 'o' as char
		instance.read() as char == '#' as char
		instance.read() as char == '#' as char
		instance.read() as char == 'b' as char
		instance.read() as char == 'a' as char
		instance.read() as char == 'r' as char
	}

	def 'Invalid chars are replaced as expected in read(char cbuf[], int off, int len).'() {
		setup:
		def instance = new ReplaceInvalidXmlCharacterReader(new StringReader('foo' + (0 as char) + (0xFFFF as char) + 'bar'), '#' as char)
		char[] buffer = new char[8]

		when:
		instance.read(buffer, 0, 8)

		then:
		buffer == ['f', 'o', 'o', '#', '#', 'b', 'a', 'r'] as char[]
	}
}
