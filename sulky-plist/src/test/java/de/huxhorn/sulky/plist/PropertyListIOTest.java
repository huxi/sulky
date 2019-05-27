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

package de.huxhorn.sulky.plist;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class PropertyListIOTest
{
	private final Logger logger = LoggerFactory.getLogger(PropertyListIOTest.class);

	private PropertyListEncoder encoder;
	private PropertyListDecoder decoder;

	@Before
	public void createInstances()
	{
		encoder=new PropertyListEncoder();
		decoder=new PropertyListDecoder();
	}

	@Test
	public void empty()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		check(list, true);
	}

	@Test
	public void longRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		list.setRoot(10L);
		check(list, true);
	}

	@Test
	public void integerRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		list.setRoot(10);
		PropertyList read = check(list, false);
		assertEquals((long) 10, read.getRoot());
	}

	@Test
	public void shortRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		list.setRoot((short)10);
		PropertyList read = check(list, false);
		assertEquals((long) 10, read.getRoot());
	}

	@Test
	public void charRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		list.setRoot('x');
		PropertyList read = check(list, false);
		assertEquals("x", read.getRoot());
	}

	@Test
	public void byteRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		list.setRoot((byte)10);
		PropertyList read = check(list, false);
		assertEquals((long) 10, read.getRoot());
	}

	@Test
	public void doubleRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		list.setRoot(3.141d);
		check(list, true);
	}

	@Test
	public void floatRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		float value = 3.141f;
		list.setRoot(value);
		PropertyList read = check(list, false);
		assertTrue(Float.compare(3.141f, ((Double)read.getRoot()).floatValue())==0); // NOPMD
	}

	@Test
	public void stringRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		list.setRoot("Foo");
		check(list, true);
	}

	@Test
	public void trueRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		list.setRoot(true);
		check(list, true);
	}

	@Test
	public void falseRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		list.setRoot(true);
		check(list, true);
	}

	@Test
	public void longArrayRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		long[] value = {17, 18, 19};
		list.setRoot(value);
		List<Long> expected=new ArrayList<>();
		expected.add(17L);
		expected.add(18L);
		expected.add(19L);
		PropertyList read = check(list, false);
		assertEquals(expected, read.getRoot());
	}

	@Test
	public void intArrayRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		int[] value = {17, 18, 19};
		list.setRoot(value);
		List<Long> expected=new ArrayList<>();
		expected.add(17L);
		expected.add(18L);
		expected.add(19L);
		PropertyList read = check(list, false);
		assertEquals(expected, read.getRoot());
	}

	@Test
	public void shortArrayRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		short[] value = {17, 18, 19};
		list.setRoot(value);
		List<Long> expected=new ArrayList<>();
		expected.add(17L);
		expected.add(18L);
		expected.add(19L);
		PropertyList read = check(list, false);
		assertEquals(expected, read.getRoot());
	}

	@Test
	public void charArrayRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		char[] value = {'F', 'o', 'o'};
		list.setRoot(value);
		PropertyList read = check(list, false);
		assertEquals("Foo", read.getRoot());
	}

	@Test
	public void booleanArrayRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		boolean[] value = {true, false, true};
		list.setRoot(value);
		List<Boolean> expected=new ArrayList<>();
		expected.add(true);
		expected.add(false);
		expected.add(true);
		PropertyList read = check(list, false);
		assertEquals(expected, read.getRoot());
	}

	@Test
	public void doubleArrayRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		double[] value = {1.0, 2.5, 13.37};
		list.setRoot(value);
		List<Double> expected=new ArrayList<>();
		expected.add(1.0);
		expected.add(2.5);
		expected.add(13.37);
		PropertyList read = check(list, false);
		assertEquals(expected, read.getRoot());
	}

	@SuppressWarnings({"unchecked"})
	@Test
	public void floatArrayRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		float[] value = {1.0f, 2.5f, 13.37f};
		list.setRoot(value);

		PropertyList read = check(list, false);
		List<Double> readValues = (List<Double>) read.getRoot();
		for(int i = 0;i<value.length;i++)
		{
			float v1=value[i];
			float v2=readValues.get(i).floatValue();
			assertTrue("Index "+i+" differs!", Float.compare(v1, v2)==0); // NOPMD
		}
	}

	@Test
	public void objectArrayRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		Object[] value = {"Foo", 17, true};
		list.setRoot(value);

		PropertyList read = check(list, false);
		List<Object> expected=new ArrayList<>();
		expected.add("Foo");
		expected.add(17L);
		expected.add(true);
		assertEquals(expected, read.getRoot());
	}

	@Test
	public void dateRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		Date value=new Date(1_234_567_890_000L);
		list.setRoot(value);
		check(list, true);
	}

	@Test
	public void dataRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		byte[] value="Foobar Snafu".getBytes(StandardCharsets.UTF_8);
		list.setRoot(value);
		PropertyList read = check(list, false);
		assertArrayEquals(value, (byte[])read.getRoot());
	}


	@Test
	public void emptyArrayRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		List<String> array=new ArrayList<>();
		list.setRoot(array);
		check(list, true);
	}

	@Test
	public void arrayRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		List<String> array=new ArrayList<>();
		array.add("foo");
		array.add("bar");
		array.add("foobar");
		list.setRoot(array);
		check(list, true);
	}

	@Test
	public void emptyMapRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		Map<String,String> map=new HashMap<>();
		list.setRoot(map);
		check(list, true);
	}

	@Test
	public void mapRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		Map<String,String> map=new HashMap<>();
		map.put("foo", "bar");
		map.put("foobar", "snafu");
		list.setRoot(map);
		check(list, true);
	}

	@Test
	public void toStringRoot()
		throws Throwable
	{
		PropertyList list=new PropertyList();
		list.setRoot(new ToStringExample());
		PropertyList read = check(list, false);
		assertEquals("ToString", read.getRoot());
	}

	private PropertyList check(PropertyList list, boolean equal)
		throws Throwable
	{
		if(logger.isDebugEnabled()) logger.debug("Processing PropertyList:\n{}", list);
		byte[] bytes;
		PropertyList readList;

		bytes = write(list);
		logData(bytes);
		if(logger.isInfoEnabled()) logger.info("PropertyList written. (size={})", bytes.length);
		readList = read(bytes);
		if(equal)
		{
			assertEquals(list, readList);
		}
		return readList;
	}

	private void logData(byte[] bytes)
	{
		if(logger.isDebugEnabled())
		{
			String output = bytes == null ? null : new String(bytes, StandardCharsets.UTF_8);
			logger.debug("Encoded PropertyList: {}", output);
		}
	}

	private PropertyList read(byte[] bytes)
		throws IOException
	{
		ByteArrayInputStream bis=new ByteArrayInputStream(bytes);
		return decoder.decode(bis);
	}

	private byte[] write(PropertyList list)
		throws IOException
	{
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		encoder.encode(list, bos);
		return bos.toByteArray();
	}

	private static class ToStringExample
	{
		@Override
		public String toString()
		{
			return "ToString";
		}
	}
}
