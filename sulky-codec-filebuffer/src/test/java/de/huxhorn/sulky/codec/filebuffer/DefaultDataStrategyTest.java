/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2009 Joern Huxhorn
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
package de.huxhorn.sulky.codec.filebuffer;

import de.huxhorn.sulky.codec.Codec;
import de.huxhorn.sulky.codec.SerializableCodec;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class DefaultDataStrategyTest
{
	private DefaultDataStrategy<String> instance;
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
		instance = new DefaultDataStrategy<String>();
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

	@Test(expected = UnsupportedOperationException.class)
	public void set()
		throws IOException
	{
		instance.set(0, null, null, null, null, null);
	}

	@Test
	public void isSetSupported()
	{
		assertFalse(instance.isSetSupported());
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
