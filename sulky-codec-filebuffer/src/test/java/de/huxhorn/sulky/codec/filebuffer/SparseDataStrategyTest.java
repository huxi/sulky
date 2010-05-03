/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2010 Joern Huxhorn
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
 * Copyright 2007-2010 Joern Huxhorn
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

package de.huxhorn.sulky.codec.filebuffer;

import de.huxhorn.sulky.codec.Codec;
import de.huxhorn.sulky.codec.SerializableCodec;

import org.junit.After;
import static org.junit.Assert.*;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class SparseDataStrategyTest
{
	private SparseDataStrategy<String> instance;
	private File indexFile;
	private File dataFile;
	private Codec<String> codec;
	private IndexStrategy indexStrategy;

	@Before
	public void setUp()
		throws IOException
	{
		indexFile = File.createTempFile("index", "tst");
		indexFile.delete();
		dataFile = File.createTempFile("data", "tst");
		dataFile.delete();
		codec = new SerializableCodec<String>();
		indexStrategy = new DefaultIndexStrategy();
		instance = new SparseDataStrategy<String>();
	}

	@After
	public void tearDown()
	{
		indexFile.delete();
		dataFile.delete();
	}

	@Test
	public void addGet()
		throws IOException, ClassNotFoundException
	{
		String value1 = "Foo";
		String value2 = "Bar";
		RandomAccessFile randomIndexFile = null;
		RandomAccessFile randomDataFile = null;
		try
		{
			randomIndexFile = new RandomAccessFile(indexFile, "rw");
			randomDataFile = new RandomAccessFile(dataFile, "rw");
			instance.add(value1, randomIndexFile, randomDataFile, codec, indexStrategy);
			instance.add(value2, randomIndexFile, randomDataFile, codec, indexStrategy);
			String readValue1 = instance.get(0, randomIndexFile, randomDataFile, codec, indexStrategy);
			String readValue2 = instance.get(1, randomIndexFile, randomDataFile, codec, indexStrategy);
			assertEquals(value1, readValue1);
			assertEquals(value2, readValue2);
		}
		finally
		{
			closeQuietly(randomIndexFile);
			closeQuietly(randomDataFile);
		}
	}

	@Test
	public void addAllGet()
		throws IOException, ClassNotFoundException
	{
		String value1 = "Foo";
		String value2 = "Bar";
		List<String> values = new ArrayList<String>();
		values.add(value1);
		values.add(value2);

		RandomAccessFile randomIndexFile = null;
		RandomAccessFile randomDataFile = null;
		try
		{
			randomIndexFile = new RandomAccessFile(indexFile, "rw");
			randomDataFile = new RandomAccessFile(dataFile, "rw");
			instance.addAll(values, randomIndexFile, randomDataFile, codec, indexStrategy);
			String readValue1 = instance.get(0, randomIndexFile, randomDataFile, codec, indexStrategy);
			String readValue2 = instance.get(1, randomIndexFile, randomDataFile, codec, indexStrategy);
			assertEquals(value1, readValue1);
			assertEquals(value2, readValue2);
		}
		finally
		{
			closeQuietly(randomIndexFile);
			closeQuietly(randomDataFile);
		}
	}

	@Test
	public void set()
		throws IOException, ClassNotFoundException
	{
		String value1 = "Foo";
		String value2 = "Bar";
		long index1 = 17;
		long index2 = 42;
		RandomAccessFile randomIndexFile = null;
		RandomAccessFile randomDataFile = null;
		try
		{
			randomIndexFile = new RandomAccessFile(indexFile, "rw");
			randomDataFile = new RandomAccessFile(dataFile, "rw");
			boolean result1 = instance.set(index1, value1, randomIndexFile, randomDataFile, codec, indexStrategy);
			boolean result2 = instance.set(index2, value2, randomIndexFile, randomDataFile, codec, indexStrategy);
			String readValue1 = instance.get(index1, randomIndexFile, randomDataFile, codec, indexStrategy);
			String readValue2 = instance.get(index2, randomIndexFile, randomDataFile, codec, indexStrategy);
			assertEquals(index2 + 1, indexStrategy.getSize(randomIndexFile));
			assertEquals(value1, readValue1);
			assertEquals(value2, readValue2);
			assertTrue(result1);
			assertTrue(result2);
			for(int i = 0; i < index2; i++)
			{
				if(i != index1)
				{
					assertNull(instance.get(i, randomIndexFile, randomDataFile, codec, indexStrategy));
				}
			}
		}
		finally
		{
			closeQuietly(randomIndexFile);
			closeQuietly(randomDataFile);
		}
	}

	@Test
	public void setOverwrite()
		throws IOException, ClassNotFoundException
	{
		String value1 = "Foo";
		String value2 = "Bar";
		long index1 = 17;
		long index2 = 42;
		RandomAccessFile randomIndexFile = null;
		RandomAccessFile randomDataFile = null;
		try
		{
			randomIndexFile = new RandomAccessFile(indexFile, "rw");
			randomDataFile = new RandomAccessFile(dataFile, "rw");
			boolean result1 = instance.set(index1, value1, randomIndexFile, randomDataFile, codec, indexStrategy);
			boolean result2 = instance.set(index2, value2, randomIndexFile, randomDataFile, codec, indexStrategy);
			boolean result3 = instance.set(index2, value1, randomIndexFile, randomDataFile, codec, indexStrategy);
			String readValue1 = instance.get(index1, randomIndexFile, randomDataFile, codec, indexStrategy);
			String readValue2 = instance.get(index2, randomIndexFile, randomDataFile, codec, indexStrategy);
			assertEquals(index2 + 1, indexStrategy.getSize(randomIndexFile));
			assertEquals(value1, readValue1);
			assertEquals(value1, readValue2);
			assertTrue(result1);
			assertTrue(result2);
			assertTrue(result3);
			for(int i = 0; i < index2; i++)
			{
				if(i != index1)
				{
					assertNull(instance.get(i, randomIndexFile, randomDataFile, codec, indexStrategy));
				}
			}
		}
		finally
		{
			closeQuietly(randomIndexFile);
			closeQuietly(randomDataFile);
		}
		// TODO: overwrite + set to null
	}

	@Test
	public void setNoOverwrite()
		throws IOException, ClassNotFoundException
	{
		instance.setSupportingOverwrite(false);
		String value1 = "Foo";
		String value2 = "Bar";
		long index1 = 17;
		long index2 = 42;
		RandomAccessFile randomIndexFile = null;
		RandomAccessFile randomDataFile = null;
		try
		{
			randomIndexFile = new RandomAccessFile(indexFile, "rw");
			randomDataFile = new RandomAccessFile(dataFile, "rw");
			boolean result1 = instance.set(index1, value1, randomIndexFile, randomDataFile, codec, indexStrategy);
			boolean result2 = instance.set(index2, value2, randomIndexFile, randomDataFile, codec, indexStrategy);
			boolean result3 = instance.set(index2, value1, randomIndexFile, randomDataFile, codec, indexStrategy);
			String readValue1 = instance.get(index1, randomIndexFile, randomDataFile, codec, indexStrategy);
			String readValue2 = instance.get(index2, randomIndexFile, randomDataFile, codec, indexStrategy);
			assertEquals(index2 + 1, indexStrategy.getSize(randomIndexFile));
			assertEquals(value1, readValue1);
			assertEquals(value2, readValue2);
			assertTrue(result1);
			assertTrue(result2);
			assertFalse(result3);
			for(int i = 0; i < index2; i++)
			{
				if(i != index1)
				{
					assertNull(instance.get(i, randomIndexFile, randomDataFile, codec, indexStrategy));
				}
			}
		}
		finally
		{
			closeQuietly(randomIndexFile);
			closeQuietly(randomDataFile);
		}
	}

	@Test
	public void setOverwriteNull()
		throws IOException, ClassNotFoundException
	{
		String value1 = "Foo";
		String value2 = "Bar";
		long index1 = 17;
		long index2 = 42;
		RandomAccessFile randomIndexFile = null;
		RandomAccessFile randomDataFile = null;
		try
		{
			randomIndexFile = new RandomAccessFile(indexFile, "rw");
			randomDataFile = new RandomAccessFile(dataFile, "rw");
			boolean result1 = instance.set(index1, value1, randomIndexFile, randomDataFile, codec, indexStrategy);
			boolean result2 = instance.set(index2, value2, randomIndexFile, randomDataFile, codec, indexStrategy);
			boolean result3 = instance.set(index2, null, randomIndexFile, randomDataFile, codec, indexStrategy);
			String readValue1 = instance.get(index1, randomIndexFile, randomDataFile, codec, indexStrategy);
			String readValue2 = instance.get(index2, randomIndexFile, randomDataFile, codec, indexStrategy);
			assertEquals(index2 + 1, indexStrategy.getSize(randomIndexFile));
			assertEquals(value1, readValue1);
			assertNull(readValue2);
			assertTrue(result1);
			assertTrue(result2);
			assertTrue(result3);
			for(int i = 0; i < index2; i++)
			{
				if(i != index1)
				{
					assertNull(instance.get(i, randomIndexFile, randomDataFile, codec, indexStrategy));
				}
			}
		}
		finally
		{
			closeQuietly(randomIndexFile);
			closeQuietly(randomDataFile);
		}
	}

	@Test
	public void isSetSupported()
	{
		assertTrue(instance.isSetSupported());
	}

	private static void closeQuietly(RandomAccessFile raf)
	{
		if(raf != null)
		{
			try
			{
				raf.close();
			}
			catch(IOException e)
			{
				e.printStackTrace();
			}
		}
	}
}
