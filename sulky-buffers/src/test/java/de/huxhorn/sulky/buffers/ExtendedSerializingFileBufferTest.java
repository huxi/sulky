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

import de.huxhorn.sulky.generics.io.Serializer;
import de.huxhorn.sulky.generics.io.Deserializer;
import de.huxhorn.sulky.generics.io.SerializableSerializer;
import de.huxhorn.sulky.generics.io.SerializableDeserializer;

import org.junit.After;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.ArrayList;

public class ExtendedSerializingFileBufferTest
{
	private final Logger logger = LoggerFactory.getLogger(ExtendedSerializingFileBufferTest.class);

	private File tempOutputPath;
	private File serializeFile;
	private File serializeIndexFile;

	private String[] values;
	private Integer magicValue;
	private Map<String, String> metaData;
	private Serializer<String> serializer;
	private Deserializer<String> deserializer;

	@Before
	public void setUp()
		throws Exception
	{
		tempOutputPath = File.createTempFile("sfb-testing", "rulez");
		tempOutputPath.delete();
		tempOutputPath.mkdirs();
		serializeFile = new File(tempOutputPath, "dump");
		serializeIndexFile = new File(tempOutputPath, "dump.index");

		serializer=new SerializableSerializer<String>();
		deserializer=new SerializableDeserializer<String>();

		values = new String[]
			{
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

		magicValue = 0xDEADBEEF;
		metaData = new HashMap<String, String>();
		metaData.put("foo1", "bar1");
		metaData.put("foo2", "bar2");
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
	public void readWriteNoMagicNoMetaAdd()
	{
		ExtendedSerializingFileBuffer<String> instance = new ExtendedSerializingFileBuffer<String>(null, null, serializer, deserializer, serializeFile, serializeIndexFile);
		for(String current : values)
		{
			instance.add(current);
		}
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

		ExtendedSerializingFileBuffer<String> otherInstance = new ExtendedSerializingFileBuffer<String>(null, null, serializer, deserializer, serializeFile, serializeIndexFile);

		assertEquals(values.length, (int) otherInstance.getSize());

		assertNull(otherInstance.getMagicValue());

		assertNull(otherInstance.getMetaData());
	}

	@Test
	public void readWriteMagicNoMetaAdd()
	{
		ExtendedSerializingFileBuffer<String> instance = new ExtendedSerializingFileBuffer<String>(magicValue, null, serializer, deserializer, serializeFile, serializeIndexFile);
		for(String current : values)
		{
			instance.add(current);
		}
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

		ExtendedSerializingFileBuffer<String> otherInstance = new ExtendedSerializingFileBuffer<String>(magicValue, null, serializer, deserializer, serializeFile, serializeIndexFile);

		assertEquals(values.length, (int) otherInstance.getSize());

		assertEquals(magicValue, otherInstance.getMagicValue());

		assertNull(otherInstance.getMetaData());
	}

	@Test
	public void readWriteNoMagicMetaAdd()
	{
		ExtendedSerializingFileBuffer<String> instance = new ExtendedSerializingFileBuffer<String>(null, metaData, serializer, deserializer, serializeFile, serializeIndexFile);
		for(String current : values)
		{
			instance.add(current);
		}
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

		ExtendedSerializingFileBuffer<String> otherInstance = new ExtendedSerializingFileBuffer<String>(null, metaData, serializer, deserializer, serializeFile, serializeIndexFile);

		assertEquals(values.length, (int) otherInstance.getSize());

		assertNull(otherInstance.getMagicValue());

		assertEquals(metaData, otherInstance.getMetaData());
	}

	@Test
	public void readWriteMagicMetaAdd()
	{
		ExtendedSerializingFileBuffer<String> instance = new ExtendedSerializingFileBuffer<String>(magicValue, metaData, serializer, deserializer, serializeFile, serializeIndexFile);
		for(String current : values)
		{
			instance.add(current);
		}
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

		ExtendedSerializingFileBuffer<String> otherInstance = new ExtendedSerializingFileBuffer<String>(magicValue, metaData, serializer, deserializer, serializeFile, serializeIndexFile);

		assertEquals(values.length, (int) otherInstance.getSize());

		assertEquals(magicValue, otherInstance.getMagicValue());

		assertEquals(metaData, otherInstance.getMetaData());
	}

	@Test
	public void readWriteNoMagicNoMetaAddAll()
	{
		ExtendedSerializingFileBuffer<String> instance = new ExtendedSerializingFileBuffer<String>(null, null, serializer, deserializer, serializeFile, serializeIndexFile);
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

		ExtendedSerializingFileBuffer<String> otherInstance = new ExtendedSerializingFileBuffer<String>(null, null, serializer, deserializer, serializeFile, serializeIndexFile);

		assertEquals(values.length, (int) otherInstance.getSize());

		assertNull(otherInstance.getMagicValue());

		assertNull(otherInstance.getMetaData());
	}

	@Test
	public void readWriteMagicNoMetaAddAll()
	{
		ExtendedSerializingFileBuffer<String> instance = new ExtendedSerializingFileBuffer<String>(magicValue, null, serializer, deserializer, serializeFile, serializeIndexFile);
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

		ExtendedSerializingFileBuffer<String> otherInstance = new ExtendedSerializingFileBuffer<String>(magicValue, null, serializer, deserializer, serializeFile, serializeIndexFile);

		assertEquals(values.length, (int) otherInstance.getSize());

		assertEquals(magicValue, otherInstance.getMagicValue());

		assertNull(otherInstance.getMetaData());
	}

	@Test
	public void readWriteNoMagicMetaAddAll()
	{
		ExtendedSerializingFileBuffer<String> instance = new ExtendedSerializingFileBuffer<String>(null, metaData, serializer, deserializer, serializeFile, serializeIndexFile);
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

		ExtendedSerializingFileBuffer<String> otherInstance = new ExtendedSerializingFileBuffer<String>(null, metaData, serializer, deserializer, serializeFile, serializeIndexFile);

		assertEquals(values.length, (int) otherInstance.getSize());

		assertNull(otherInstance.getMagicValue());

		assertEquals(metaData, otherInstance.getMetaData());
	}

	@Test
	public void readWriteMagicMetaAddAll()
	{
		ExtendedSerializingFileBuffer<String> instance = new ExtendedSerializingFileBuffer<String>(magicValue, metaData, serializer, deserializer, serializeFile, serializeIndexFile);
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

		ExtendedSerializingFileBuffer<String> otherInstance = new ExtendedSerializingFileBuffer<String>(magicValue, metaData, serializer, deserializer, serializeFile, serializeIndexFile);

		assertEquals(values.length, (int) otherInstance.getSize());

		assertEquals(magicValue, otherInstance.getMagicValue());

		assertEquals(metaData, otherInstance.getMetaData());
	}

	@Test
	public void magicMeta()
	{
		ExtendedSerializingFileBuffer<String> instance = new ExtendedSerializingFileBuffer<String>(magicValue, metaData, serializer, deserializer, serializeFile, serializeIndexFile);

		assertEquals(0, (int) instance.getSize());

		ExtendedSerializingFileBuffer<String> otherInstance = new ExtendedSerializingFileBuffer<String>(magicValue, metaData, serializer, deserializer, serializeFile, serializeIndexFile);

		assertEquals(0, (int) instance.getSize());

		assertEquals(magicValue, otherInstance.getMagicValue());

		assertEquals(metaData, otherInstance.getMetaData());
	}

	@Test
	public void magic()
	{
		ExtendedSerializingFileBuffer<String> instance = new ExtendedSerializingFileBuffer<String>(magicValue, null, serializer, deserializer, serializeFile, serializeIndexFile);

		assertEquals(0, (int) instance.getSize());

		ExtendedSerializingFileBuffer<String> otherInstance = new ExtendedSerializingFileBuffer<String>(magicValue, null, serializer, deserializer, serializeFile, serializeIndexFile);

		assertEquals(0, (int) instance.getSize());

		assertEquals(magicValue, otherInstance.getMagicValue());

		assertNull(otherInstance.getMetaData());
	}

	@Test
	public void meta()
	{
		ExtendedSerializingFileBuffer<String> instance = new ExtendedSerializingFileBuffer<String>(null, metaData, serializer, deserializer, serializeFile, serializeIndexFile);

		assertEquals(0, (int) instance.getSize());

		ExtendedSerializingFileBuffer<String> otherInstance = new ExtendedSerializingFileBuffer<String>(null, metaData, serializer, deserializer, serializeFile, serializeIndexFile);

		assertEquals(0, (int) instance.getSize());

		assertNull(otherInstance.getMagicValue());

		assertEquals(metaData, otherInstance.getMetaData());
	}

	@Test
	public void resetMagicMeta()
	{
		ExtendedSerializingFileBuffer<String> instance = new ExtendedSerializingFileBuffer<String>(magicValue, metaData, serializer, deserializer, serializeFile, serializeIndexFile);

		instance.addAll(values);
		assertEquals(values.length, (int) instance.getSize());

		instance.reset();
		assertEquals(0, instance.getSize());

		ExtendedSerializingFileBuffer<String> otherInstance = new ExtendedSerializingFileBuffer<String>(magicValue, metaData, serializer, deserializer, serializeFile, serializeIndexFile);

		assertEquals(0, otherInstance.getSize());

		assertEquals(magicValue, otherInstance.getMagicValue());

		assertEquals(metaData, otherInstance.getMetaData());

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
	public void resetNoMagicMeta()
	{
		ExtendedSerializingFileBuffer<String> instance = new ExtendedSerializingFileBuffer<String>(null, metaData, serializer, deserializer, serializeFile, serializeIndexFile);

		instance.addAll(values);
		assertEquals(values.length, (int) instance.getSize());

		instance.reset();
		assertEquals(0, instance.getSize());

		ExtendedSerializingFileBuffer<String> otherInstance = new ExtendedSerializingFileBuffer<String>(null, metaData, serializer, deserializer, serializeFile, serializeIndexFile);

		assertEquals(0, otherInstance.getSize());

		assertNull(otherInstance.getMagicValue());

		assertEquals(metaData, otherInstance.getMetaData());

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
	public void resetMagicNoMeta()
	{
		ExtendedSerializingFileBuffer<String> instance = new ExtendedSerializingFileBuffer<String>(magicValue, null, serializer, deserializer, serializeFile, serializeIndexFile);

		instance.addAll(values);
		assertEquals(values.length, (int) instance.getSize());

		instance.reset();
		assertEquals(0, instance.getSize());

		ExtendedSerializingFileBuffer<String> otherInstance = new ExtendedSerializingFileBuffer<String>(magicValue, null, serializer, deserializer, serializeFile, serializeIndexFile);

		assertEquals(0, otherInstance.getSize());

		assertEquals(magicValue, otherInstance.getMagicValue());

		assertNull(otherInstance.getMetaData());

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
	public void resetNoMagicNoMeta()
	{
		ExtendedSerializingFileBuffer<String> instance = new ExtendedSerializingFileBuffer<String>(null, null, serializer, deserializer, serializeFile, serializeIndexFile);

		instance.addAll(values);
		assertEquals(values.length, (int) instance.getSize());

		instance.reset();
		assertEquals(0, instance.getSize());

		ExtendedSerializingFileBuffer<String> otherInstance = new ExtendedSerializingFileBuffer<String>(null, null, serializer, deserializer, serializeFile, serializeIndexFile);

		assertEquals(0, otherInstance.getSize());

		assertNull(otherInstance.getMagicValue());

		assertNull(otherInstance.getMetaData());

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
	public void elementProcessorsAdd()
	{
		ExtendedSerializingFileBuffer<String> instance = new ExtendedSerializingFileBuffer<String>(magicValue, metaData, serializer, deserializer, serializeFile, serializeIndexFile);
		List<ElementProcessor<String>> elementProcessors=new ArrayList<ElementProcessor<String>>();
		CapturingElementProcessor capture=new CapturingElementProcessor();
		elementProcessors.add(capture);
		instance.setElementProcessors(elementProcessors);
		instance.add("Example");
		assertEquals(1, capture.list.size());
		assertEquals("Example", capture.list.get(0));
	}

	@Test
	public void elementProcessorsAddAll()
	{
		ExtendedSerializingFileBuffer<String> instance = new ExtendedSerializingFileBuffer<String>(magicValue, metaData, serializer, deserializer, serializeFile, serializeIndexFile);
		List<ElementProcessor<String>> elementProcessors=new ArrayList<ElementProcessor<String>>();
		CapturingElementProcessor capture=new CapturingElementProcessor();
		elementProcessors.add(capture);
		instance.setElementProcessors(elementProcessors);
		instance.addAll(values);
		assertEquals(values.length, capture.list.size());
		int index = 0;
		for(String value : capture.list)
		{
			if(logger.isInfoEnabled()) logger.info("Element #{}={}", index, value);
			assertEquals("Element #" + index + " differs!", values[index], value);
			index++;
		}
	}

	private static class CapturingElementProcessor
		implements ElementProcessor<String>
	{
		public List<String> list=new ArrayList<String>();

		public void processElement(String element)
		{
			list.add(element);
		}

		public void processElements(List<String> element)
		{
			list.addAll(element);
		}
	}
}