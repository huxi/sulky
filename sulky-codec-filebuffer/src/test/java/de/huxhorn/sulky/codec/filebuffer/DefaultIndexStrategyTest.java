/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2011 Joern Huxhorn
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
 * Copyright 2007-2011 Joern Huxhorn
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

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;


public class DefaultIndexStrategyTest
{
	private File testFile;
	private DefaultIndexStrategy instance;

	@Before
	public void setUp()
		throws IOException
	{
		testFile = File.createTempFile("index", "tst");
		testFile.delete();
		instance = new DefaultIndexStrategy();
	}

	@After
	public void tearDown()
	{
		testFile.delete();
	}

	@Test
	public void emptyGetSize()
		throws IOException
	{
		RandomAccessFile raf = null;
		try
		{
			raf = new RandomAccessFile(testFile, "rw");
			assertEquals(0, instance.getSize(raf));
		}
		finally
		{
			if(raf != null)
			{
				raf.close();
			}
		}
	}

	@Test
	public void emptyGetOffset()
		throws IOException
	{
		RandomAccessFile raf = null;
		try
		{
			raf = new RandomAccessFile(testFile, "rw");
			assertEquals(-1, instance.getOffset(raf, 17));
		}
		finally
		{
			if(raf != null)
			{
				raf.close();
			}
		}
	}

	@Test
	public void firstOffset()
		throws IOException
	{
		RandomAccessFile raf = null;
		try
		{
			raf = new RandomAccessFile(testFile, "rw");
			instance.setOffset(raf, 0, 17);
			assertEquals(1, instance.getSize(raf));
			assertEquals(17, instance.getOffset(raf, 0));
		}
		finally
		{
			if(raf != null)
			{
				raf.close();
			}
		}
	}

	@Test
	public void anyOffset()
		throws IOException
	{
		RandomAccessFile raf = null;
		try
		{
			raf = new RandomAccessFile(testFile, "rw");
			long index = 17;
			long value = 42;
			instance.setOffset(raf, index, value);
			assertEquals(index + 1, instance.getSize(raf));
			assertEquals(value, instance.getOffset(raf, index));
			for(int i = 0; i < index; i++)
			{
				assertEquals(-1, instance.getOffset(raf, i));
			}
		}
		finally
		{
			if(raf != null)
			{
				raf.close();
			}
		}
	}

	@Test
	public void anyOffsetTwice()
		throws IOException
	{
		RandomAccessFile raf = null;
		try
		{
			raf = new RandomAccessFile(testFile, "rw");
			long index = 17;
			long index2 = 42;
			long value = 1;
			long value2 = 2;
			instance.setOffset(raf, index, value);
			instance.setOffset(raf, index2, value2);
			assertEquals(index2 + 1, instance.getSize(raf));
			assertEquals(value, instance.getOffset(raf, index));
			assertEquals(value2, instance.getOffset(raf, index2));
			for(int i = 0; i < index2; i++)
			{
				if(i == index)
				{
					continue;
				}
				assertEquals(-1, instance.getOffset(raf, i));
			}
		}
		finally
		{
			if(raf != null)
			{
				raf.close();
			}
		}
	}
}
