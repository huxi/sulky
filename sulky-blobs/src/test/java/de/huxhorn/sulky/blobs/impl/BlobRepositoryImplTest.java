/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2018 Joern Huxhorn
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
 * Copyright 2007-2018 Joern Huxhorn
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

package de.huxhorn.sulky.blobs.impl;

import de.huxhorn.sulky.blobs.AmbiguousIdException;
import de.huxhorn.sulky.junit.LoggingTestBase;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;
import org.apache.commons.io.IOUtils;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assume.assumeTrue;

public class BlobRepositoryImplTest
	extends LoggingTestBase
{
	private static final String FOO = "foo";
	private final Logger logger = LoggerFactory.getLogger(BlobRepositoryImplTest.class);

	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	private static final String TEST_DATA = "Foo\nBar";
	private static final String TEST_DATA_ID = "a044399675c6f9d097735c6eb2075d18f5e3cc56";
	private static final String WRONG_DATA_ID = "a044399675c6f9d097735c6eb2075d18f5e3cc17";

	public BlobRepositoryImplTest(Boolean logging)
	{
		super(logging);
	}

	@Test
	public void caseSensitiveHandling()
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		assertTrue(instance.isCaseSensitive());

		instance.setCaseSensitive(false);
		assertFalse(instance.isCaseSensitive());
	}

	@Test
	public void validatingHandling()
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		assertFalse(instance.isValidating());

		instance.setValidating(true);
		assertTrue(instance.isValidating());

	}

	@Test
	public void put() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFolder(FOO));

		String id=instance.put(TEST_DATA.getBytes(StandardCharsets.UTF_8));
		assertEquals(TEST_DATA_ID, id);
	}

	@Test
	public void putDuplicate() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFolder(FOO));

		String id=instance.put(TEST_DATA.getBytes(StandardCharsets.UTF_8));
		assertEquals(TEST_DATA_ID, id);
		String otherId=instance.put(TEST_DATA.getBytes(StandardCharsets.UTF_8));
		assertEquals(id, otherId);
	}

	@Test
	public void putHashClash() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		File baseDirectory = folder.newFolder(FOO);
		instance.setBaseDirectory(baseDirectory);

		File dataParent = new File(baseDirectory, TEST_DATA_ID.substring(0,2));
		if(!dataParent.mkdirs())
		{
			fail("Couldn't create directory '"+dataParent.getAbsolutePath()+"'!");
		}
		File f=new File(dataParent, TEST_DATA_ID.substring(2));
		if(!f.createNewFile())
		{
			fail("Couldn't create file '"+f.getAbsolutePath()+"'!");
		}

		String id=instance.put(TEST_DATA.getBytes(StandardCharsets.UTF_8));
		assertNull(id);
	}

	@Test
	public void putContains() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFolder(FOO));

		assertFalse(instance.contains(TEST_DATA_ID));
		instance.put(TEST_DATA.getBytes(StandardCharsets.UTF_8));
		assertTrue(instance.contains(TEST_DATA_ID));
	}

	@Test
	public void containsAmbiguous() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		File baseDirectory = folder.newFolder(FOO);
		instance.setBaseDirectory(baseDirectory);
		File dataParent = new File(baseDirectory, "aa");
		if(!dataParent.mkdirs())
		{
			fail("Couldn't create directory '"+dataParent.getAbsolutePath()+"'!");
		}
		File f1=new File(dataParent, "aa");
		if(!f1.createNewFile())
		{
			fail("Couldn't create file '"+f1.getAbsolutePath()+"'!");
		}
		File f2=new File(dataParent, "ab");
		if(!f2.createNewFile())
		{
			fail("Couldn't create file '"+f2.getAbsolutePath()+"'!");
		}
		try
		{
			instance.contains("aaa");
			fail("AmbiguousIdException wasn't thrown as expected!");
		}
		catch(AmbiguousIdException ex)
		{
			assertEquals("aaa", ex.getId());
			assertArrayEquals(new String[]{"aaaa", "aaab"}, ex.getCandidates());
		}
		instance.put(TEST_DATA.getBytes(StandardCharsets.UTF_8));
		assertTrue(instance.contains(TEST_DATA_ID));
	}

	@Test
	public void putGet() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFolder(FOO));

		try(InputStream is = instance.get(instance.put(TEST_DATA.getBytes(StandardCharsets.UTF_8))))
		{
			assertEquals(TEST_DATA, IOUtils.toString(is, StandardCharsets.UTF_8));
		}
	}

	@Test
	public void putGetCaseSensitive() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFolder(FOO));
		String id = instance.put(TEST_DATA.getBytes(StandardCharsets.UTF_8));
		id = id.toUpperCase(Locale.US);
		assertNull(instance.get(id));
		assertFalse(instance.contains(id));
		instance.setCaseSensitive(false);
		assertTrue(instance.contains(id));

		try(InputStream is = instance.get(id))
		{
			assertEquals(TEST_DATA, IOUtils.toString(is, StandardCharsets.UTF_8));
		}
	}

	@Test
	public void validating() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setValidating(true);
		File baseDirectory = folder.newFolder(FOO);
		instance.setBaseDirectory(baseDirectory);
		String id = instance.put(TEST_DATA.getBytes(StandardCharsets.UTF_8));

		try(InputStream is = instance.get(id))
		{
			assertEquals(TEST_DATA, IOUtils.toString(is, StandardCharsets.UTF_8));
		}

		File dataParent = new File(baseDirectory, TEST_DATA_ID.substring(0,2));
		File f1=new File(dataParent, TEST_DATA_ID.substring(2));
		File f2=new File(dataParent, WRONG_DATA_ID.substring(2));
		if(f1.renameTo(f2))
		{
			if(logger.isDebugEnabled()) logger.debug("Renamed {} to {}.", f1.getAbsolutePath(), f2.getAbsolutePath()); // NOPMD
		}
		assertTrue(instance.contains(WRONG_DATA_ID));
		assertNull(instance.get(WRONG_DATA_ID));
		assertFalse(instance.contains(WRONG_DATA_ID));
	}

	@Test
	public void putGetIncomplete() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFolder(FOO));

		String id=instance.put(TEST_DATA.getBytes(StandardCharsets.UTF_8));
		assertEquals(TEST_DATA_ID, id);

		try(InputStream is = instance.get(TEST_DATA_ID.substring(0,3)))
		{
			assertEquals(TEST_DATA, IOUtils.toString(is, StandardCharsets.UTF_8));
		}
	}

	@Test
	public void getMissing() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFolder(FOO));

		assertNull(instance.get(TEST_DATA_ID));
	}

	@Test
	public void size() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFolder(FOO));

		instance.put(TEST_DATA.getBytes(StandardCharsets.UTF_8));

		assertEquals(7,instance.sizeOf(TEST_DATA_ID));
	}

	@Test
	public void sizeMissing() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFolder(FOO));

		assertEquals(-1,instance.sizeOf(TEST_DATA_ID));
	}

	@Test
	public void emptyIdSet()
		throws IOException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFolder(FOO));

		assertEquals(0, instance.idSet().size());
	}

	@Test
	public void putIdSet() throws IOException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFolder(FOO));

		String id=instance.put(TEST_DATA.getBytes(StandardCharsets.UTF_8));
		assertEquals(TEST_DATA_ID, id);

		String fooId=instance.put(FOO.getBytes(StandardCharsets.UTF_8));

		String barId=instance.put("bar".getBytes(StandardCharsets.UTF_8));

		Set<String> idSet = instance.idSet();
		assertEquals(3, idSet.size());
		assertTrue(idSet.contains(TEST_DATA_ID));
		assertTrue(idSet.contains(fooId));
		assertTrue(idSet.contains(barId));
	}

	@Test
	public void putDeleteContains() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		File baseDirectory = folder.newFolder(FOO);
		instance.setBaseDirectory(baseDirectory);

		String id=instance.put(TEST_DATA.getBytes(StandardCharsets.UTF_8));
		assertEquals(TEST_DATA_ID, id);
		File parent = new File(baseDirectory, TEST_DATA_ID.substring(0, 2));
		assertTrue(instance.contains(TEST_DATA_ID));
		assertTrue(parent.isDirectory());
		assertTrue(instance.delete(TEST_DATA_ID));
		assertFalse(instance.contains(TEST_DATA_ID));
		assertFalse(parent.isDirectory());
		assertFalse(instance.delete(TEST_DATA_ID));
	}

	@Test
	public void putDeleteIdSet() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFolder(FOO));

		String id=instance.put(TEST_DATA.getBytes(StandardCharsets.UTF_8));
		assertEquals(TEST_DATA_ID, id);

		String fooId=instance.put(FOO.getBytes(StandardCharsets.UTF_8));

		String barId=instance.put("bar".getBytes(StandardCharsets.UTF_8));

		instance.delete(fooId);

		Set<String> idSet = instance.idSet();
		assertEquals(2, idSet.size());
		assertTrue(idSet.contains(TEST_DATA_ID));
		assertFalse(idSet.contains(fooId));
		assertTrue(idSet.contains(barId));
	}

	@Test(expected = IllegalArgumentException.class)
	public void containsNull() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFolder(FOO));

		instance.contains(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void getNull() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFolder(FOO));

		instance.get(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void deleteNull() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFolder(FOO));

		instance.delete(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void sizeOfNull() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFolder(FOO));

		instance.sizeOf(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void putEmptyByteArray() throws IOException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFolder(FOO));

		instance.put(new byte[0]);
	}

	@Test(expected = IllegalArgumentException.class)
	public void putEmptyStream() throws IOException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFolder(FOO));

		instance.put(new ByteArrayInputStream(new byte[0]));
	}

	@Test(expected = IllegalArgumentException.class)
	public void putNullByteArray() throws IOException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFolder(FOO));

		instance.put((byte[])null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void putNullStream() throws IOException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFolder(FOO));

		instance.put((InputStream)null);
	}

	@Test(expected = IllegalStateException.class)
	public void misconfigurationPut() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();

		instance.put(TEST_DATA.getBytes(StandardCharsets.UTF_8));
	}

	@Test(expected = IllegalStateException.class)
	public void misconfigurationGet() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();

		instance.get(TEST_DATA_ID);
	}

	@Test(expected = IllegalStateException.class)
	public void misconfigurationSize() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();

		instance.sizeOf(TEST_DATA_ID);
	}

	@Test(expected = IllegalStateException.class)
	public void misconfigurationContains() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();

		instance.contains(TEST_DATA_ID);
	}

	@Test(expected = IllegalStateException.class)
	public void misconfigurationIdSet() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();

		instance.idSet();
	}

	@Test(expected = IllegalStateException.class)
	public void misconfigurationFile() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		instance.setBaseDirectory(folder.newFile(FOO));

		instance.put(TEST_DATA.getBytes(StandardCharsets.UTF_8));
	}

	@Test
	public void baseDirectory()
		throws IOException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		File baseDirectory=folder.newFolder(FOO);
		instance.setBaseDirectory(baseDirectory);
		assertEquals(baseDirectory, instance.getBaseDirectory());
	}

	@SuppressWarnings({"ResultOfMethodCallIgnored"})
	@Test
	public void missingBaseDirectory()
		throws IOException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		File baseDirectory=folder.newFolder(FOO);
		baseDirectory.delete();
		instance.setBaseDirectory(baseDirectory);
		assertEquals(baseDirectory, instance.getBaseDirectory());
	}

	@Test (expected = IllegalArgumentException.class)
	public void nullId() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		File baseDirectory=folder.newFolder(FOO);
		instance.setBaseDirectory(baseDirectory);
		instance.get(null);
	}

	@Test (expected = IllegalArgumentException.class)
	public void shortId() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		File baseDirectory=folder.newFolder(FOO);
		instance.setBaseDirectory(baseDirectory);
		instance.get("a");
	}

	@SuppressWarnings({"ResultOfMethodCallIgnored"})
	@Test
	public void junkFiles() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		File baseDirectory=folder.newFolder(FOO);
		instance.setBaseDirectory(baseDirectory);
		String id=instance.put(TEST_DATA.getBytes(StandardCharsets.UTF_8));
		assertEquals(TEST_DATA_ID, id);

		new File(baseDirectory, "bar1").createNewFile();
		new File(baseDirectory, "bar2").mkdirs();
		File parent = new File(baseDirectory, TEST_DATA_ID.substring(0, 2));
		new File(parent, "bar1").createNewFile();
		new File(parent, "bar2").mkdirs();
		Set<String> idSet = instance.idSet();
		assertEquals(1, idSet.size());
		assertTrue(idSet.contains(TEST_DATA_ID));
	}

	@Test
	public void keepDir() throws IOException, AmbiguousIdException
	{
		BlobRepositoryImpl instance=new BlobRepositoryImpl();
		File baseDirectory=folder.newFolder(FOO);
		instance.setBaseDirectory(baseDirectory);
		String id=instance.put(TEST_DATA.getBytes(StandardCharsets.UTF_8));
		assertEquals(TEST_DATA_ID, id);

		File parent = new File(baseDirectory, TEST_DATA_ID.substring(0, 2));
		File foo=new File(parent, "bar1");
		if(foo.createNewFile())
		{
			if(logger.isDebugEnabled()) logger.debug("Created file {}.", foo.getAbsolutePath()); // NOPMD
		}
		assertTrue(instance.delete(TEST_DATA_ID));
		assertTrue(parent.isDirectory());
		assertTrue(foo.isFile());
	}

	@SuppressWarnings({"ResultOfMethodCallIgnored"})
	@Test(expected = IllegalStateException.class)
    public void brokenBaseDirectory()
		throws IOException
    {
        BlobRepositoryImpl instance=new BlobRepositoryImpl();
        File readonlyBaseDirectory = folder.newFolder(FOO);
        readonlyBaseDirectory.setWritable(false, false);
        File baseDirectory=new File(readonlyBaseDirectory, "bar");
		// lets check if creating baseDirectory actually fails...
		assumeTrue(!baseDirectory.mkdirs());
		if(logger.isInfoEnabled()) logger.info("Actually executing brokenBaseDirectory test...");
        instance.setBaseDirectory(baseDirectory);
    }
}
