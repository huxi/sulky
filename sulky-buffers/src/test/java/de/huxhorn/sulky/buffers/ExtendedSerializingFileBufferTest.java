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
package de.huxhorn.sulky.buffers;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.Map;
import java.util.HashMap;

public class ExtendedSerializingFileBufferTest
{
	private final Logger logger = LoggerFactory.getLogger(ExtendedSerializingFileBufferTest.class);

	private File tempOutputPath;
	private File serializeFile;
	private File serializeIndexFile;

	@Before
	public void setUp()
		throws Exception
	{
		tempOutputPath = File.createTempFile("sfb-testing", "rulez");
		tempOutputPath.delete();
		tempOutputPath.mkdirs();
		serializeFile = new File(tempOutputPath, "dump");
		serializeIndexFile = new File(tempOutputPath, "dump.index");
	}

	@After
	public void tearDown()
		throws Exception
	{
		serializeFile.delete();
		serializeIndexFile.delete();
		tempOutputPath.delete();
	}

	@Test
	public void readWriteNoMagicNoMeta()
	{
		String[] values = {
			"Null, sozusagen ganix",
			"Eins",
			"Zwei",
			"Drei",
			"Vier",
			"Fuenef",
			"Sechse",
			"Siebene",
			"Achtele",
			"Neune",
			"Zehne"
		};

		ExtendedSerializingFileBuffer<String> instance = new ExtendedSerializingFileBuffer<String>(null, null,serializeFile, serializeIndexFile);
		instance.addAll(values);
		assertEquals(values.length, (int) instance.getSize());

		for(int i = 0; i < values.length; i++)
		{
			String value = instance.get(i);
			if(logger.isInfoEnabled()) logger.info("Element #{}={}", i, value);
			assertEquals("Element #" + i + " differs!", values[i], value);
		}

		int index = 0;
		for(String value : instance)
		{
			if(logger.isInfoEnabled()) logger.info("Element #{}={}", index, value);
			assertEquals("Element #" + index + " differs!", values[index], value);
			index++;
		}
	}

	@Test
	public void readWriteMagicNoMeta()
	{
		String[] values = {
			"Null, sozusagen ganix",
			"Eins",
			"Zwei",
			"Drei",
			"Vier",
			"Fuenef",
			"Sechse",
			"Siebene",
			"Achtele",
			"Neune",
			"Zehne"
		};

		ExtendedSerializingFileBuffer<String> instance = new ExtendedSerializingFileBuffer<String>(0xDEADBEEF, null,serializeFile, serializeIndexFile);
		instance.addAll(values);
		assertEquals(values.length, (int) instance.getSize());

		for(int i = 0; i < values.length; i++)
		{
			String value = instance.get(i);
			if(logger.isInfoEnabled()) logger.info("Element #{}={}", i, value);
			assertEquals("Element #" + i + " differs!", values[i], value);
		}

		int index = 0;
		for(String value : instance)
		{
			if(logger.isInfoEnabled()) logger.info("Element #{}={}", index, value);
			assertEquals("Element #" + index + " differs!", values[index], value);
			index++;
		}
	}

	@Test
	public void readWriteNoMagicMeta()
	{
		String[] values = {
			"Null, sozusagen ganix",
			"Eins",
			"Zwei",
			"Drei",
			"Vier",
			"Fuenef",
			"Sechse",
			"Siebene",
			"Achtele",
			"Neune",
			"Zehne"
		};

		Map<String, String> meta=new HashMap<String, String>();
		meta.put("foo", "bar");
		
		ExtendedSerializingFileBuffer<String> instance = new ExtendedSerializingFileBuffer<String>(null, meta, serializeFile, serializeIndexFile);
		instance.addAll(values);
		assertEquals(values.length, (int) instance.getSize());

		for(int i = 0; i < values.length; i++)
		{
			String value = instance.get(i);
			if(logger.isInfoEnabled()) logger.info("Element #{}={}", i, value);
			assertEquals("Element #" + i + " differs!", values[i], value);
		}

		int index = 0;
		for(String value : instance)
		{
			if(logger.isInfoEnabled()) logger.info("Element #{}={}", index, value);
			assertEquals("Element #" + index + " differs!", values[index], value);
			index++;
		}
	}
}