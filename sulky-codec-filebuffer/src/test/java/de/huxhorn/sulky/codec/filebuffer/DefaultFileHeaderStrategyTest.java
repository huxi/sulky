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

import static junit.framework.Assert.assertEquals;
import org.junit.After;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import org.junit.Before;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class DefaultFileHeaderStrategyTest
{
	private DefaultFileHeaderStrategy instance;
	private File file;
	private int magicValue;
	private MetaData metaData;
	private HashMap<String, String> metaDataMap;

	@Before
	public void setUp()
		throws IOException
	{
		instance = new DefaultFileHeaderStrategy();
		file = File.createTempFile("DFHS", "test");
		file.deleteOnExit();
		magicValue = 0xDEADBEEF;
		metaDataMap = new HashMap<String, String>();
		metaDataMap.put("foo", "bar");
		metaData=new MetaData(metaDataMap);
	}

	@After
	public void tearDown()
	{
		file.delete();
	}

	@Test
	public void missingMetaData()
		throws IOException
	{
		FileHeader fileHeader=instance.writeFileHeader(file, magicValue, null);
		assertNotNull(fileHeader);
		assertNull(fileHeader.getMetaData());
		assertEquals(magicValue, fileHeader.getMagicValue());

		Integer readMagicValue=instance.readMagicValue(file);
		assertNotNull(readMagicValue);
		assertEquals(magicValue, (int)readMagicValue);

		FileHeader readFileHeader=instance.readFileHeader(file);
		assertEquals(fileHeader, readFileHeader);
	}


	public void includingMetaData()
		throws IOException
	{
		FileHeader fileHeader=instance.writeFileHeader(file, magicValue, metaDataMap);
		assertNotNull(fileHeader);
		assertEquals(metaData, fileHeader.getMetaData());
		assertEquals(magicValue, fileHeader.getMagicValue());

		Integer readMagicValue=instance.readMagicValue(file);
		assertNotNull(readMagicValue);
		assertEquals(magicValue, (int)readMagicValue);

		FileHeader readFileHeader=instance.readFileHeader(file);
		assertEquals(fileHeader, readFileHeader);
	}
}
