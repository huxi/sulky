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
 * but WITHOUT ANY WARRANTY without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Copyright 2007-2016 Joern Huxhorn
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
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

import java.time.DayOfWeek
import java.time.Instant

class SafeStringSpec extends Specification {
	private static final Map RECURSIVE_MAP
	private static final String RECURSIVE_MAP_EXPECTED_RESULT

	private static final List RECURSIVE_LIST
	private static final String RECURSIVE_LIST_EXPECTED_RESULT

	private static final Object[] RECURSIVE_OBJECT_ARRAY
	private static final String RECURSIVE_OBJECT_ARRAY_EXPECTED_RESULT

	private static final ProblematicToString PROBLEMATIC_1
	private static final String PROBLEMATIC_1_EXPECTED_RESULT

	private static final ProblematicToString PROBLEMATIC_2
	private static final String PROBLEMATIC_2_EXPECTED_RESULT

	private static final ProblematicToString PROBLEMATIC_3
	private static final String PROBLEMATIC_3_EXPECTED_RESULT

	private static final Map LIST_INSIDE_TREE_MAP
	private static final Map ARRAY_INSIDE_TREE_MAP

	private static final List LIST_INSIDE_LIST
	private static final List ARRAY_INSIDE_LIST

	static
	{
		Map<String, Map> aMap = new HashMap()
		Map<String, Map> bMap = new HashMap()
		bMap.put('bar', aMap)
		aMap.put('foo', bMap)

		RECURSIVE_MAP = aMap
		RECURSIVE_MAP_EXPECTED_RESULT =
				'{foo={bar=' + SafeString.RECURSION_PREFIX + SafeString.identityToString(aMap) + SafeString.RECURSION_SUFFIX + '}}'

		List aList = new ArrayList()
		List bList = new ArrayList()
		bList.add(aList)
		aList.add(bList)

		RECURSIVE_LIST = aList
		RECURSIVE_LIST_EXPECTED_RESULT =
				'[[' + SafeString.RECURSION_PREFIX + SafeString.identityToString(aList) + SafeString.RECURSION_SUFFIX + ']]'

		Object[] array = new Object[2]
		Object[] innerArray = new Object[2]
		innerArray[1] = array
		array[1] = innerArray
		RECURSIVE_OBJECT_ARRAY = array
		RECURSIVE_OBJECT_ARRAY_EXPECTED_RESULT =
				'[null, [null, '+SafeString.RECURSION_PREFIX + SafeString.identityToString(array) + SafeString.RECURSION_SUFFIX + ']]'

		PROBLEMATIC_1=new ProblematicToString(null)
		PROBLEMATIC_1_EXPECTED_RESULT =
				SafeString.ERROR_PREFIX + SafeString.identityToString(PROBLEMATIC_1) + SafeString.ERROR_SEPARATOR + FooThrowable.class.getName() + SafeString.ERROR_SUFFIX

		PROBLEMATIC_2=new ProblematicToString(FooThrowable.getName())
		// not a bug. both should result in very similar messages.
		PROBLEMATIC_2_EXPECTED_RESULT =
				SafeString.ERROR_PREFIX + SafeString.identityToString(PROBLEMATIC_2) + SafeString.ERROR_SEPARATOR + FooThrowable.class.getName() + SafeString.ERROR_SUFFIX

		def message = 'Message'
		PROBLEMATIC_3=new ProblematicToString(message)
		PROBLEMATIC_3_EXPECTED_RESULT =
				SafeString.ERROR_PREFIX + SafeString.identityToString(PROBLEMATIC_3) + SafeString.ERROR_SEPARATOR + FooThrowable.class.getName() + SafeString.ERROR_MSG_SEPARATOR + message + SafeString.ERROR_SUFFIX

		def mapList = ['One', 'Two'] as ArrayList
		LIST_INSIDE_TREE_MAP = ['foo': mapList, 'bar': mapList] as TreeMap

		def mapArray = ['One', 'Two'] as String[]
		ARRAY_INSIDE_TREE_MAP = ['foo': mapArray, 'bar': mapArray] as TreeMap

		def listList = ['One', 'Two'] as ArrayList
		LIST_INSIDE_LIST = [listList, listList] as ArrayList

		def listArray = ['One', 'Two'] as String[]
		ARRAY_INSIDE_LIST = [listArray, listArray] as ArrayList
	}

	static def validValues() {
		HashMap nullKeyMap = new HashMap()
		nullKeyMap.put(null, 'foo')

		HashMap objectKeyMap = new HashMap()
		objectKeyMap.put(new UnproblematicToString(), 'foo')

		[
				null,
				'foo',
				new UnproblematicToString(),
				new Date(1234567890000L),
				new Date(1234567890017L),
				Instant.ofEpochMilli(1234567890000L),
				Instant.ofEpochMilli(1234567890017L),
				DayOfWeek.SATURDAY,
				Byte.valueOf((byte)0),
				Byte.valueOf((byte)-1),
				Byte.MIN_VALUE,
				Byte.MAX_VALUE,
				[1, 2, 3, 0, Byte.MAX_VALUE, Byte.MIN_VALUE, -1, 0xCA, 0xFE, 0xBA, 0xBE] as byte[],
				[1, 2, 3, 0, Byte.MAX_VALUE, Byte.MIN_VALUE, -1, 0xCA, 0xFE, 0xBA, 0xBE] as Byte[],
				[1, 2, 3, 0, Short.MAX_VALUE, Short.MIN_VALUE] as short[],
				[1, 2, 3, 0, Short.MAX_VALUE, Short.MIN_VALUE] as Short[],
				[1, 2, 3, 0, Integer.MAX_VALUE, Integer.MIN_VALUE] as int[],
				[1, 2, 3, 0, Integer.MAX_VALUE, Integer.MIN_VALUE] as Integer[],
				[1, 2, 3, 0, Long.MAX_VALUE, Long.MIN_VALUE] as long[],
				[1, 2, 3, 0, Long.MAX_VALUE, Long.MIN_VALUE] as Long[],
				[3.14159265f, 42.0f, -3.14159265f, 0.0f, Float.NaN, Float.MAX_VALUE, Float.MIN_VALUE, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY] as float[],
				[3.14159265f, 42.0f, -3.14159265f, 0.0f, Float.NaN, Float.MAX_VALUE, Float.MIN_VALUE, Float.POSITIVE_INFINITY, Float.NEGATIVE_INFINITY] as Float[],

				[3.14159265d, 42.0d, -3.14159265d, 0.0d, Double.NaN, Double.MAX_VALUE, Double.MIN_VALUE, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY] as double[],
				[3.14159265d, 42.0d, -3.14159265d, 0.0d, Double.NaN, Double.MAX_VALUE, Double.MIN_VALUE, Double.POSITIVE_INFINITY, Double.NEGATIVE_INFINITY] as Double[],
				[true, false] as boolean[],
				[true, false] as Boolean[],
				['b', 'a', 'r', 0, '!'] as char[],
				['b', 'a', 'r', 0, '!'] as Character[],
				LIST_INSIDE_TREE_MAP,
				ARRAY_INSIDE_TREE_MAP,
				LIST_INSIDE_LIST,
				ARRAY_INSIDE_LIST,
				[null] as ArrayList,
				new HashMap(['foo': null]),
				new HashMap(['bar': 'null']),
				nullKeyMap,
				new HashMap(['null': 'bar']),
				objectKeyMap,
				[] as Object[],
				[] as ArrayList,
				[] as HashSet,
				[''] as Object[],
				[''] as ArrayList,
				[''] as HashSet,
				['a', 'b', 'c'] as String[],
				['a', null, 'c'] as String[],
				['a', 'null', 'c'] as String[],
		] as Object[]
	}

	@SuppressWarnings("GroovyAssignabilityCheck") // stfu, IDEA.
	static def validValuesClasses() {
		[
				null,
				String,
				UnproblematicToString,
				Date,
				Date,
				Instant,
				Instant,
				DayOfWeek,
				Byte,
				Byte,
				Byte,
				Byte,
				byte[],
				Byte[],
				short[],
				Short[],
				int[],
				Integer[],
				long[],
				Long[],
				float[],
				Float[],
				double[],
				Double[],
				boolean[],
				Boolean[],
				char[],
				Character[],
				TreeMap,
				TreeMap,
				ArrayList,
				ArrayList,
				ArrayList,
				HashMap,
				HashMap,
				HashMap,
				HashMap,
				HashMap,
				Object[],
				ArrayList,
				HashSet,
				Object[],
				ArrayList,
				HashSet,
				String[],
				String[],
				String[],
		] as Class[]
	}

	static def validValuesExpectedResults() {
		[
				'null',
				'foo',
				'UnproblematicToString',
				'2009-02-13T23:31:30.000Z',
				'2009-02-13T23:31:30.017Z',
				'2009-02-13T23:31:30.000Z',
				'2009-02-13T23:31:30.017Z',
				'SATURDAY',
				'0x00',
				'0xFF',
				'0x80',
				'0x7F',
				'[0x01, 0x02, 0x03, 0x00, 0x7F, 0x80, 0xFF, 0xCA, 0xFE, 0xBA, 0xBE]',
				'[0x01, 0x02, 0x03, 0x00, 0x7F, 0x80, 0xFF, 0xCA, 0xFE, 0xBA, 0xBE]',
				'[1, 2, 3, 0, 32767, -32768]',
				'[1, 2, 3, 0, 32767, -32768]',
				'[1, 2, 3, 0, 2147483647, -2147483648]',
				'[1, 2, 3, 0, 2147483647, -2147483648]',
				'[1, 2, 3, 0, 9223372036854775807, -9223372036854775808]',
				'[1, 2, 3, 0, 9223372036854775807, -9223372036854775808]',
				'[3.1415927, 42.0, -3.1415927, 0.0, NaN, 3.4028235E38, 1.4E-45, Infinity, -Infinity]',
				'[3.1415927, 42.0, -3.1415927, 0.0, NaN, 3.4028235E38, 1.4E-45, Infinity, -Infinity]',
				'[3.14159265, 42.0, -3.14159265, 0.0, NaN, 1.7976931348623157E308, 4.9E-324, Infinity, -Infinity]',
				'[3.14159265, 42.0, -3.14159265, 0.0, NaN, 1.7976931348623157E308, 4.9E-324, Infinity, -Infinity]',
				'[true, false]',
				'[true, false]',
				'[b, a, r, \00, !]',
				'[b, a, r, \00, !]',
				'{bar=[One, Two], foo=[One, Two]}',
				'{bar=[One, Two], foo=[One, Two]}',
				'[[One, Two], [One, Two]]',
				'[[One, Two], [One, Two]]',
				'[null]',
				'{foo=null}',
				'{bar=null}',
				'{null=foo}',
				'{null=bar}',
				'{UnproblematicToString=foo}',
				'[]',
				'[]',
				'[]',
				'[]',
				'[]',
				'[]',
				'[a, b, c]',
				'[a, null, c]',
				'[a, null, c]',
		]
	}

	static def validValuesExpectedQuotedResults() {
		[
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				'{"bar"=["One", "Two"], "foo"=["One", "Two"]}',
				'{"bar"=["One", "Two"], "foo"=["One", "Two"]}',
				'[["One", "Two"], ["One", "Two"]]',
				'[["One", "Two"], ["One", "Two"]]',
				null,
				'{"foo"=null}',
				'{"bar"="null"}',
				'{null="foo"}',
				'{"null"="bar"}',
				'{UnproblematicToString="foo"}',
				null,
				null,
				null,
				'[""]',
				'[""]',
				'[""]',
				'["a", "b", "c"]',
				'["a", null, "c"]',
				'["a", "null", "c"]',
		]
	}

	static def validValuesExpectedQuotedResults2() {
		[
				null,
				'\'foo\'',
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				null,
				'[\'bar\':[\'One\', \'Two\'], \'foo\':[\'One\', \'Two\']]',
				'[\'bar\':[\'One\', \'Two\'], \'foo\':[\'One\', \'Two\']]',
				'[[\'One\', \'Two\'], [\'One\', \'Two\']]',
				'[[\'One\', \'Two\'], [\'One\', \'Two\']]',
				null,
				'[\'foo\':null]',
				'[\'bar\':\'null\']',
				'[null:\'foo\']',
				'[\'null\':\'bar\']',
				'[UnproblematicToString:\'foo\']',
				null,
				null,
				null,
				'[\'\']',
				'[\'\']',
				'[\'\']',
				'[\'a\', \'b\', \'c\']',
				'[\'a\', null, \'c\']',
				'[\'a\', \'null\', \'c\']',
		]
	}

	static def invalidValues() {
		[
				RECURSIVE_MAP,
				RECURSIVE_LIST,
				RECURSIVE_OBJECT_ARRAY,
				PROBLEMATIC_1,
				PROBLEMATIC_2,
				PROBLEMATIC_3,
		]
	}

	static def invalidValuesExpectedResults() {
		[
				RECURSIVE_MAP_EXPECTED_RESULT,
				RECURSIVE_LIST_EXPECTED_RESULT,
				RECURSIVE_OBJECT_ARRAY_EXPECTED_RESULT,
				PROBLEMATIC_1_EXPECTED_RESULT,
				PROBLEMATIC_2_EXPECTED_RESULT,
				PROBLEMATIC_3_EXPECTED_RESULT,
		]
	}

	static def expectedExceptionClasses() {
		[
				StackOverflowError,
				StackOverflowError,
				StackOverflowError,
				FooThrowable,
				FooThrowable,
				FooThrowable,
		]
	}

	private static class UnproblematicToString {
		public String toString() {
			return 'UnproblematicToString'
		}
	}

	private static class ProblematicToString {
		private String message

		private ProblematicToString(String message) {
			this.message=message
		}

		public String toString() {
			throw new FooThrowable(message)
		}
	}

	private static class FooThrowable
			extends RuntimeException {

		private FooThrowable(String s) {
			super((String)s)
		}
	}

	static def workaround() {
		// Could not determine failure message for exception of type org.spockframework.runtime.SpockComparisonFailure: java.lang.StackOverflowError
		//
		// java.lang.StackOverflowError
		//         at java.lang.StringBuilder.append(StringBuilder.java:136)
		//         at java.lang.StringBuilder.<init>(StringBuilder.java:113)
		//         at org.codehaus.groovy.runtime.InvokerHelper.formatMap(InvokerHelper.java:645)
		//         at org.codehaus.groovy.runtime.InvokerHelper.format(InvokerHelper.java:601)
		def result = []

		invalidValues().each {
			result << SafeString.toString(it)
		}

		return result
	}

	@Unroll
	def 'explodingValue.toString() throws #expectedException'() {
		when:
		value.toString()

		then:
		thrown(expectedException)

		where:
		value << invalidValues()
		expectedException << expectedExceptionClasses()
	}

	@Unroll
	def 'validValues sanity check'() {
		expect:
		if(value == null) {
			assert valueClass == null
		} else {
			assert value.getClass() == valueClass
		}

		where:
		value << validValues()
		valueClass << validValuesClasses()
	}

	@Unroll
	def 'SafeString.toString(explodingValue) returns #expectedResult'() {
		expect:
		// SafeString.toString(value) == expectedValue
		result == expectedResult

		where:
		// using workaround to prevent StackOverflowError in Spock in case of failed comparison
		// value << invalidValues()
		result << workaround()
		expectedResult << invalidValuesExpectedResults()
	}

	@Unroll
	def 'SafeString.toString(validValue) for #valueClass returns #expectedResult#andQuoted#andQuoted2'() {
		when:
		def result = SafeString.toString(value)
		def quotedResult = SafeString.toString(value, SafeString.StringWrapping.CONTAINED, SafeString.StringStyle.JAVA, SafeString.MapStyle.JAVA)
		def quotedResult2 = SafeString.toString(value, SafeString.StringWrapping.ALL, SafeString.StringStyle.GROOVY, SafeString.MapStyle.GROOVY)

		then:
		result == expectedResult

		and:
		if(valueClass == String) {
			assert value.is(result)
		}

		and:
		if(expectedQuotedResult == null) {
			assert quotedResult == expectedResult
		} else {
			assert quotedResult == expectedQuotedResult
		}

		and:
		if(expectedQuotedResult2 == null) {
			assert quotedResult2 == expectedResult
		} else {
			assert quotedResult2 == expectedQuotedResult2
		}

		where:
		value << validValues()
		valueClass << validValuesClasses()
		expectedResult << validValuesExpectedResults()
		expectedQuotedResult << validValuesExpectedQuotedResults()
		expectedQuotedResult2 << validValuesExpectedQuotedResults2()

		andQuoted = expectedQuotedResult ? " and ${expectedQuotedResult}" : ''
		andQuoted2 = expectedQuotedResult2 ? " and ${expectedQuotedResult2}" : ''
	}

	@Unroll
	def 'SafeString.append(validValue) for #valueClass appends #expectedResult#andQuoted#andQuoted2'() {
		setup:
		StringBuilder resultBuilder = new StringBuilder()
		StringBuilder quotedResultBuilder = new StringBuilder()
		StringBuilder quotedResultBuilder2 = new StringBuilder()

		when:
		SafeString.append(value, resultBuilder)
		SafeString.append(value, quotedResultBuilder, SafeString.StringWrapping.CONTAINED, SafeString.StringStyle.JAVA, SafeString.MapStyle.JAVA)
		SafeString.append(value, quotedResultBuilder2, SafeString.StringWrapping.ALL, SafeString.StringStyle.GROOVY, SafeString.MapStyle.GROOVY)

		and:
		def result = resultBuilder.toString()
		def quotedResult = quotedResultBuilder.toString()
		def quotedResult2 = quotedResultBuilder2.toString()

		then:
		result == expectedResult

		and:
		if(expectedQuotedResult == null) {
			assert quotedResult == expectedResult
		} else {
			assert quotedResult == expectedQuotedResult
		}

		and:
		if(expectedQuotedResult2 == null) {
			assert quotedResult2 == expectedResult
		} else {
			assert quotedResult2 == expectedQuotedResult2
		}

		where:
		value << validValues()
		valueClass << validValuesClasses()
		expectedResult << validValuesExpectedResults()
		expectedQuotedResult << validValuesExpectedQuotedResults()
		expectedQuotedResult2 << validValuesExpectedQuotedResults2()

		andQuoted = expectedQuotedResult ? " and ${expectedQuotedResult}" : ''
		andQuoted2 = expectedQuotedResult2 ? " and ${expectedQuotedResult2}" : ''
	}

	@Unroll
	def 'SafeString.append throws expected exceptions.'() {
		when:
		SafeString.append(null, stringBuilder, stringWrapping, stringStyle, mapStyle)

		then:
		NullPointerException ex = thrown()
		ex.message == messagePart + ' must not be null!'

		where:
		stringBuilder       | stringWrapping                | stringStyle                 | mapStyle                 | messagePart
		null                | SafeString.StringWrapping.ALL | SafeString.StringStyle.JAVA | SafeString.MapStyle.JAVA | 'stringBuilder'
		new StringBuilder() | null                          | SafeString.StringStyle.JAVA | SafeString.MapStyle.JAVA | 'stringWrapping'
		new StringBuilder() | SafeString.StringWrapping.ALL | null                        | SafeString.MapStyle.JAVA | 'stringStyle'
		new StringBuilder() | SafeString.StringWrapping.ALL | SafeString.StringStyle.JAVA | null                     | 'mapStyle'
	}

	def 'SafeString.identityToString(null) returns null.'() {
		expect:
		SafeString.identityToString(null) == null
	}

	def 'bow to coverage report'() {
		expect:
		SafeString.StringWrapping.ALL == SafeString.StringWrapping.valueOf('ALL')
		SafeString.StringStyle.JAVA == SafeString.StringStyle.valueOf('JAVA')
		SafeString.MapStyle.GROOVY == SafeString.MapStyle.valueOf('GROOVY')

		SafeString.StringWrapping.values().contains(SafeString.StringWrapping.CONTAINED)
		SafeString.StringStyle.values().contains(SafeString.StringStyle.GROOVY)
		SafeString.MapStyle.values().contains(SafeString.MapStyle.JAVA)
	}
}
