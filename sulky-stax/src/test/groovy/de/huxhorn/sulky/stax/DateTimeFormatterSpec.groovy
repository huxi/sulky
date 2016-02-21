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

package de.huxhorn.sulky.stax;

import spock.lang.Specification
import spock.lang.Unroll

public class DateTimeFormatterSpec
	extends Specification
{

    @Unroll
    def "parse and default format #input"(String input, String expectedResult) {
        given:
        DateTimeFormatter formatter = new DateTimeFormatter()

        when:
        Date date = formatter.parse(input)
        String result = formatter.format(date)

        then:
        result == expectedResult

        where:
        input                            | expectedResult
        '2009-11-15T00:00:00.000+0100'   | '2009-11-15T01:00:00.000+00:00'
        '2009-11-15T00:00:00.000+01:00'  | '2009-11-15T01:00:00.000+00:00'
        '2009-11-15T00:00:00.000+0000'   | '2009-11-15T00:00:00.000+00:00'
        '2009-11-15T00:00:00.000+00:00'  | '2009-11-15T00:00:00.000+00:00'
        '2009-11-15T00:00:00.000-0800'   | '2009-11-14T16:00:00.000+00:00'
        '2009-11-15T00:00:00.000-08:00'  | '2009-11-14T16:00:00.000+00:00'
        '2009-11-15T00:00:00.000Z'       | '2009-11-15T00:00:00.000+00:00'
        '2009-11-15T00:00:00Z'           | '2009-11-15T00:00:00.000+00:00'
        '2009-11-15T00:00:00.017+0100'   | '2009-11-15T01:00:00.017+00:00'
        '2009-11-15T00:00:00.017+01:00'  | '2009-11-15T01:00:00.017+00:00'
        '2009-11-15T00:00:00+0100'       | '2009-11-15T01:00:00.000+00:00'
        '2009-11-15T00:00:00+01:00'      | '2009-11-15T01:00:00.000+00:00'
    }

    @Unroll
    def "parse and format #input with millis"(String input, String expectedResult) {
        given:
        DateTimeFormatter formatter = new DateTimeFormatter()

        when:
        Date date = formatter.parse(input)
        String result = formatter.format(date, true)

        then:
        result == expectedResult

        where:
        input                            | expectedResult
        '2009-11-15T00:00:00.000+0100'   | '2009-11-15T01:00:00.000+00:00'
        '2009-11-15T00:00:00.000+01:00'  | '2009-11-15T01:00:00.000+00:00'
        '2009-11-15T00:00:00.000+0000'   | '2009-11-15T00:00:00.000+00:00'
        '2009-11-15T00:00:00.000+00:00'  | '2009-11-15T00:00:00.000+00:00'
        '2009-11-15T00:00:00.000-0800'   | '2009-11-14T16:00:00.000+00:00'
        '2009-11-15T00:00:00.000-08:00'  | '2009-11-14T16:00:00.000+00:00'
        '2009-11-15T00:00:00.000Z'       | '2009-11-15T00:00:00.000+00:00'
        '2009-11-15T00:00:00Z'           | '2009-11-15T00:00:00.000+00:00'
        '2009-11-15T00:00:00.017+0100'   | '2009-11-15T01:00:00.017+00:00'
        '2009-11-15T00:00:00.017+01:00'  | '2009-11-15T01:00:00.017+00:00'
        '2009-11-15T00:00:00+0100'       | '2009-11-15T01:00:00.000+00:00'
        '2009-11-15T00:00:00+01:00'      | '2009-11-15T01:00:00.000+00:00'
    }

    @Unroll
    def "parse and format #input without millis"(String input, String expectedResult) {
        given:
        DateTimeFormatter formatter = new DateTimeFormatter()

        when:
        Date date = formatter.parse(input)
        String result = formatter.format(date, false)

        then:
        result == expectedResult

        where:
        input                            | expectedResult
        '2009-11-15T00:00:00.000+0100'   | '2009-11-15T01:00:00+00:00'
        '2009-11-15T00:00:00.000+01:00'  | '2009-11-15T01:00:00+00:00'
        '2009-11-15T00:00:00.000+0000'   | '2009-11-15T00:00:00+00:00'
        '2009-11-15T00:00:00.000+00:00'  | '2009-11-15T00:00:00+00:00'
        '2009-11-15T00:00:00.000-0800'   | '2009-11-14T16:00:00+00:00'
        '2009-11-15T00:00:00.000-08:00'  | '2009-11-14T16:00:00+00:00'
        '2009-11-15T00:00:00.000Z'       | '2009-11-15T00:00:00+00:00'
        '2009-11-15T00:00:00Z'           | '2009-11-15T00:00:00+00:00'
        '2009-11-15T00:00:00.017+0100'   | '2009-11-15T01:00:00+00:00'
        '2009-11-15T00:00:00.017+01:00'  | '2009-11-15T01:00:00+00:00'
        '2009-11-15T00:00:00+0100'       | '2009-11-15T01:00:00+00:00'
        '2009-11-15T00:00:00+01:00'      | '2009-11-15T01:00:00+00:00'
    }
}
