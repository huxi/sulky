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

package de.huxhorn.sulky.groovy;

import de.huxhorn.sulky.junit.LoggingTestBase;
import groovy.lang.Script;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

public class GroovyInstanceTest
	extends LoggingTestBase
{
	@Rule
	public TemporaryFolder folder = new TemporaryFolder();
	private File fooFile;

	public GroovyInstanceTest(Boolean logging)
	{
		super(logging);
	}

	@Before
	public void setUp()
		throws IOException
	{
		fooFile = folder.newFile("Foo.groovy");
		copyIntoFile("/Foo.groovy", fooFile);
	}

	private void copyIntoFile(String resource, File output)
		throws IOException
	{
		FileOutputStream out = null;
		InputStream in = null;
		try
		{
			out = FileUtils.openOutputStream(output);
			in = GroovyInstanceTest.class.getResourceAsStream(resource);
			IOUtils.copy(in, out);
		}
		finally
		{
			IOUtils.closeQuietly(out);
			IOUtils.closeQuietly(in);
		}
	}

	@SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
	@Test
	public void normal()
	{
		GroovyInstance instance = new GroovyInstance();
		instance.setGroovyFileName(fooFile.getAbsolutePath());
		Class instanceClass = instance.getInstanceClass();
		assertNotNull(instanceClass);
		assertEquals("Foo", instanceClass.getName());
		Object object = instance.getInstance();
		assertNotNull(object);
		assertTrue(object instanceof Script);
		Script script=(Script)object;
		String result = (String)script.run();
		assertEquals("Foo", result);

		assertNull(instance.getErrorCause());
		assertNull(instance.getErrorMessage());


		Object newObject = instance.getNewInstance();
		assertNotNull(object);
		assertTrue(newObject instanceof Script);
		Script newScript=(Script)newObject;
		String newResult = (String)newScript.run();
		assertEquals("Foo", newResult);
		assertNotSame(newScript, script);
		assertNotSame(instance.getNewInstance(), newScript);
		assertSame(instance.getInstance(), script);

		assertSame(script, instance.getInstanceAs(Script.class));
		assertNotSame(script, instance.getNewInstanceAs(Script.class));
		newScript = instance.getNewInstanceAs(Script.class);
		newResult = (String)newScript.run();
		assertEquals("Foo", newResult);

		assertNull(instance.getInstanceAs(Comparable.class));
		assertNull(instance.getNewInstanceAs(Comparable.class));
	}

	@Test
	public void refresh()
		throws IOException, InterruptedException
	{
		GroovyInstance instance = new GroovyInstance();
		instance.setGroovyFileName(fooFile.getAbsolutePath());
		instance.setRefreshInterval(200);
		Class instanceClass = instance.getInstanceClass();
		assertNotNull(instanceClass);
		assertEquals("Foo", instanceClass.getName());
		Object object = instance.getInstance();
		assertNotNull(object);
		assertTrue(object instanceof Script);
		Script script=(Script)object;
		String result = (String)script.run();
		assertEquals("Foo", result);
		Thread.sleep(1000); // workaround for filesystem lastModified accuracy
		copyIntoFile("/Bar.groovy", fooFile);

		object = instance.getInstance();
		assertTrue(object instanceof Script);
		script = (Script)object;
		result = (String)script.run();
		assertEquals("Bar", result);
	}

	@SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
	@Test
	public void broken()
		throws IOException, InterruptedException
	{
		GroovyInstance instance = new GroovyInstance();
		instance.setGroovyFileName(fooFile.getAbsolutePath());
		instance.setRefreshInterval(200);
		Class instanceClass = instance.getInstanceClass();
		assertNotNull(instanceClass);
		assertEquals("Foo", instanceClass.getName());
		Object object = instance.getInstance();
		assertNotNull(object);
		assertTrue(object instanceof Script);
		Script script=(Script)object;
		String result = (String)script.run();
		assertEquals("Foo", result);
		Thread.sleep(1000); // workaround for filesystem lastModified accuracy
		copyIntoFile("/Broken.b0rken", fooFile);
		Thread.sleep(1000); // workaround for filesystem lastModified accuracy


		assertNull(""+instance.getInstanceClass(), instance.getInstanceClass());
		assertNull(""+instance.getInstance(), instance.getInstance());
		// error should be logged only once...
		Thread.sleep(500);
		assertNull(""+instance.getInstanceClass(), instance.getInstanceClass());
		assertNull(""+instance.getInstance(), instance.getInstance());
		Thread.sleep(500);
		assertNull(""+instance.getInstanceClass(), instance.getInstanceClass());
		assertNull(""+instance.getInstance(), instance.getInstance());
		Thread.sleep(500);
		assertNotNull(instance.getErrorCause());
		assertNotNull(instance.getErrorMessage());

		copyIntoFile("/Bar.groovy", fooFile);

		object = instance.getInstance();
		assertTrue(object instanceof Script);
		script = (Script)object;
		result = (String)script.run();
		assertEquals("Bar", result);

		assertNull(instance.getErrorCause());
		assertNull(instance.getErrorMessage());
	}

	@SuppressWarnings({"ThrowableResultOfMethodCallIgnored"})
	@Test
	public void nullFile()
	{
		GroovyInstance instance = new GroovyInstance();
		instance.setGroovyFileName(null);
		Class instanceClass = instance.getInstanceClass();
		assertNull(instanceClass);
		Object object = instance.getInstance();
		assertNull(object);
		assertNull(instance.getErrorCause());
		assertEquals("groovyFileName must not be null!", instance.getErrorMessage());
	}
}
