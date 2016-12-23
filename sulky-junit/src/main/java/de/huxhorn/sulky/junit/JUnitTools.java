/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2016 Joern Huxhorn
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
 * Copyright 2007-2016 Joern Huxhorn
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

package de.huxhorn.sulky.junit;

import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.Objects;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

public final class JUnitTools
{
	static {
		new JUnitTools(); // stfu, coverage.
	}

	private JUnitTools()
	{}

	public static void equal(Object original, Object other, boolean same)
	{
		if(original == null)
		{
			assertNull(other);
			return;
		}
		String messagePart = "" + original + " and " + other;
		if (same)
		{
			assertSame(original, other);
		}
		else
		{
			assertNotSame(messagePart + " are the same but shouldn't.", original, other);
			assertEquals(messagePart + " are not equal.", original, other);
			assertEquals("Hashes of " + messagePart + " differ!", original.hashCode(), other.hashCode());
		}
	}

	/**
	 * Serializes the original and returns the deserialized instance.
	 * Serialization is using ObjectOutputStream/ObjectInputStream.
	 *
	 * @param <T> The type to be serialized.
	 * @param original the original Serializable,
	 * @return the deserialized instance.
	 * @throws java.io.IOException    In case of error during (de)serialization.
	 * @throws ClassNotFoundException In case of error during (de)serialization.
	 */
	public static <T extends Serializable> T serialize(T original)
		throws IOException, ClassNotFoundException
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(original);
		oos.close();
		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(is);
		@SuppressWarnings({"unchecked"})
		T t =(T) ois.readObject();
		return t;
	}

	/**
	 * Serializes the original and returns the deserialized instance.
	 * Serialization is using XMLEncoder/XMLDecoder.
	 *
	 * @param <T> The type to be serialized.
	 * @param original the original Serializable,
	 * @param unused   was only needed in Java &lt;1.6.
	 * @return the deserialized instance.
	 * @throws java.io.IOException    In case of error during (de)serialization.
	 * @throws ClassNotFoundException In case of error during (de)serialization.
	 * @see java.beans.PersistenceDelegate
	 */
	public static <T extends Serializable> T serializeXml(T original, Class... unused)
		throws IOException, ClassNotFoundException
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		XMLEncoder e = new XMLEncoder(os);
		e.writeObject(original);
		e.close();

		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		XMLDecoder d = new XMLDecoder(is);
		@SuppressWarnings({"unchecked"})
		T result = (T) d.readObject();
		d.close();
		return result;
	}

	public static <T extends Serializable> T testSerialization(T original)
		throws IOException, ClassNotFoundException
	{
		return testSerialization(original, false);
	}

	public static <T extends Serializable> T testSerialization(T original, boolean same)
		throws IOException, ClassNotFoundException
	{
		T result = serialize(original);

		equal(original, result, same);
		return result;
	}

	public static <T extends Serializable> T testXmlSerialization(T original, Class... unused)
		throws IOException, ClassNotFoundException
	{
		return testXmlSerialization(original, false, unused);
	}

	public static <T extends Serializable> T testXmlSerialization(T original, boolean same, Class... unused)
		throws IOException, ClassNotFoundException
	{
		T result = serializeXml(original, unused);

		equal(original, result, same);
		return result;
	}

	public static <T extends Cloneable> T reflectionClone(T original)
		throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{
		Class<? extends Cloneable> clazz = original.getClass();
		Method method = clazz.getMethod("clone");

		//assertTrue("clone() method isn't accessible!", method.isAccessible());

		@SuppressWarnings({"unchecked"})
		T t = (T) method.invoke(original);
		return t;
	}

	public static <T extends Cloneable> T testClone(T original)
		throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{
		return testClone(original, false);
	}

	public static <T extends Cloneable> T testClone(T original, boolean same)
		throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{
		T result = reflectionClone(original);

		equal(original, result, same);

		return result;
	}

	public static void copyResourceToFile(String resource, File target)
			throws IOException
	{
		copyResourceToFile(resource, target, -1);
	}

	public static void copyResourceToFile(String resource, File target, long lastModified)
			throws IOException
	{
		Objects.requireNonNull(resource, "resource must not be null!");
		Objects.requireNonNull(target, "target must not be null!");
		File outputFile = target.getAbsoluteFile();
		File parentFile = outputFile.getParentFile();
		//noinspection ResultOfMethodCallIgnored
		parentFile.mkdirs();
		Path outputPath = outputFile.toPath();
		try(InputStream in = JUnitTools.class.getResourceAsStream(resource))
		{
			if(in == null)
			{
				throw new IllegalArgumentException("Could not find resource '"+resource+"' in classpath!");
			}
			Files.copy(in, outputPath, REPLACE_EXISTING);
		}
		if(lastModified >= 0)
		{
			Files.setLastModifiedTime(outputPath, FileTime.fromMillis(lastModified));
		}
	}

}
