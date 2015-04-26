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

package de.huxhorn.sulky.formatting;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class SafeStringTest
{
	private final Logger logger = LoggerFactory.getLogger(SafeStringTest.class);

	@SuppressWarnings({"unchecked"})
	@Test(expected = StackOverflowError.class)
	public void showMapRecursionProblem()
	{
		Map a = new HashMap();
		Map b = new HashMap();
		b.put("bar", a);
		a.put("foo", b);
		// the following line will throw an java.lang.StackOverflowError!
		//noinspection ResultOfMethodCallIgnored
		a.toString();
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void verifyMapRecursionWorks()
	{
		Map a = new HashMap();
		Map b = new HashMap();
		b.put("bar", a);
		a.put("foo", b);

		String expected = "{foo={bar=" + SafeString.RECURSION_PREFIX +
			SafeString.identityToString(a) + SafeString.RECURSION_SUFFIX + "}}";

		evaluate(expected, a);
	}

	@SuppressWarnings({"unchecked"})
	@Test(expected = java.lang.StackOverflowError.class)
	public void showCollectionRecursionProblem()
	{
		List a = new ArrayList();
		List b = new ArrayList();
		b.add(a);
		a.add(b);
		// the following line will throw an java.lang.StackOverflowError!
		//noinspection ResultOfMethodCallIgnored
		a.toString();
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void verifyCollectionRecursionWorks()
	{
		List a = new ArrayList();
		List b = new ArrayList();
		b.add(a);
		a.add(b);

		String expected = "[[" + SafeString.RECURSION_PREFIX +
				SafeString.identityToString(a) + SafeString.RECURSION_SUFFIX + "]]";

		evaluate(expected, a);
	}

	@SuppressWarnings({"ResultOfMethodCallIgnored"})
	@Test(expected = FooThrowable.class)
	public void showExceptionInToStringProblem()
	{
		ProblematicToString problem=new ProblematicToString(null);
		// the following line will throw a FooThrowable
		String.valueOf(problem);
	}

	@Test
	public void verifyExceptionWithNullMessageInToStringWorks()
	{
		ProblematicToString o = new ProblematicToString(null);
		String expected = SafeString.ERROR_PREFIX + SafeString.identityToString(o)
			+ SafeString.ERROR_SEPARATOR + FooThrowable.class.getName()
			+ SafeString.ERROR_SUFFIX;

		evaluate(expected, o);
	}

	@Test
	public void verifyExceptionWithClassNameMessageInToStringWorks()
	{
		ProblematicToString o = new ProblematicToString(FooThrowable.class.getName());
		String expected = SafeString.ERROR_PREFIX + SafeString.identityToString(o)
			+ SafeString.ERROR_SEPARATOR + FooThrowable.class.getName()
			+ SafeString.ERROR_SUFFIX;

		evaluate(expected, o);
	}

	@Test
	public void verifyExceptionWithMessageInToStringWorks()
	{
		ProblematicToString o = new ProblematicToString("BarMessage");
		String expected = SafeString.ERROR_PREFIX + SafeString.identityToString(o)
			+ SafeString.ERROR_SEPARATOR + FooThrowable.class.getName()
			+ SafeString.ERROR_MSG_SEPARATOR
			+ "BarMessage"
			+ SafeString.ERROR_SUFFIX;

		evaluate(expected, o);
	}

	@Test
	public void foo()
	{
		evaluate("UnproblematicToString", new UnproblematicToString());
	}

	@Test
	public void verifyRecursiveObjectArray()
	{
		Object[] array= new Object[2];
		array[1]=array;
		evaluate("[null, "+SafeString.RECURSION_PREFIX
			+ SafeString.identityToString(array)
			+SafeString.RECURSION_SUFFIX + "]", array);
	}

	@Test
	public void deepMapList()
	{
		List<String> list = new ArrayList<>();
		list.add("One");
		list.add("Two");
		Map<String, List<String>> map = new TreeMap<>();
		map.put("foo", list);
		map.put("bar", list);

		evaluate("{bar=[One, Two], foo=[One, Two]}", map);
	}

	@Test
	public void deepMapArray()
	{
		String[] array = new String[]{"One", "Two"};
		Map<String, String[]> map = new TreeMap<>();
		map.put("foo", array);
		map.put("bar", array);

		evaluate("{bar=[One, Two], foo=[One, Two]}", map);
	}

	@Test
	public void deepListList()
	{
		List<String> list = new ArrayList<>();
		list.add("One");
		list.add("Two");
		List<List<String>> outer = new ArrayList<>();
		outer.add(list);
		outer.add(list);

		evaluate("[[One, Two], [One, Two]]", outer);
	}

	@Test
	public void deepListArray()
	{
		String[] array = new String[]{"One", "Two"};
		List<String[]> list = new ArrayList<>();
		list.add(array);
		list.add(array);

		evaluate("[[One, Two], [One, Two]]", list);
	}

	@Test
	public void date()
	{
		String result;
		String expected;
		Object o;

		{
			o = new Date(1234567890000L);
			expected = "2009-02-13T23:31:30.000Z";
		}
		if(logger.isInfoEnabled()) logger.info("Evaluating {}...", o);
		result = SafeString.toString(o);
		if(logger.isInfoEnabled()) logger.info("Result of {} is {}.", o, result);
		assertEquals(expected, result);
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void verifyNullString()
	{
		evaluate(null, null);
	}

	@Test
	public void byteArray()
	{
		byte[] foo = new byte[]{1, 2, 3, Byte.MAX_VALUE, Byte.MIN_VALUE};
		evaluate("[1, 2, 3, 127, -128]", foo);
	}

	@Test
	public void shortArray()
	{
		short[] foo = new short[]{1, 2, 3, Short.MAX_VALUE, Short.MIN_VALUE};
		evaluate("[1, 2, 3, 32767, -32768]", foo);
	}

	@Test
	public void intArray()
	{
		int[] foo = new int[]{1, 2, 3, Integer.MAX_VALUE, Integer.MIN_VALUE};
		evaluate("[1, 2, 3, 2147483647, -2147483648]", foo);
	}

	@Test
	public void longArray()
	{
		long[] foo = new long[]{1, 2, 3, Long.MAX_VALUE, Long.MIN_VALUE};
		evaluate("[1, 2, 3, 9223372036854775807, -9223372036854775808]", foo);
	}

	@Test
	public void floatArray()
	{
		float[] foo = new float[]{3.14159265f, 42.0f, -3.14159265f, Float.NaN};
		evaluate("[3.1415927, 42.0, -3.1415927, NaN]", foo);
	}

	@Test
	public void doubleArray()
	{
		double[] foo = new double[]{3.14159265d, 42.0d, -3.14159265d, Double.NaN};
		evaluate("[3.14159265, 42.0, -3.14159265, NaN]", foo);
	}

	@Test
	public void booleanArray()
	{
		boolean[] foo = new boolean[]{true, false};
		evaluate("[true, false]", foo);
	}

	@Test
	public void charArray()
	{
		char[] foo = new char[]{'b', 'a', 'r', '!'};
		evaluate("[b, a, r, !]", foo);
	}

	@Test
	public void nullInList()
	{
		List<String> list = new ArrayList<>();
		list.add(null);
		evaluate("[null]", list);
	}

	@Test
	public void identityToStringNull()
	{
		assertNull(SafeString.identityToString(null));
	}

	private void evaluate(String expected, Object o)
	{
		String result;
		if(logger.isInfoEnabled()) logger.info("Evaluating {}...", o);
		result = SafeString.toString(o);
		if(logger.isInfoEnabled()) logger.info("Result of {} is {}.", o, result);
		assertEquals(expected, result);
	}

	private static class UnproblematicToString
	{
		public String toString()
		{
			return "UnproblematicToString";
		}
	}

	private static class ProblematicToString
	{
		private String message;

		public ProblematicToString(String message)
		{
			this.message=message;
		}

		public String toString()
		{
			throw new FooThrowable(message);
		}
	}

	private static class FooThrowable
		extends RuntimeException
	{
		private static final long serialVersionUID = 9140989200041952994L;

		public FooThrowable(String s)
		{
			super(s);
		}

		@Override
		public String toString()
		{
			return "" + getMessage();
		}
	}
}
