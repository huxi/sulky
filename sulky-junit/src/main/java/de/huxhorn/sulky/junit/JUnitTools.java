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

package de.huxhorn.sulky.junit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertSame;

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

public final class JUnitTools
{
	private JUnitTools()
	{}
	
	/**
	 * Serializes the original and returns the deserialized instance.
	 * Serialization is using ObjectOutputStream/ObjectInputStream.
	 *
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
		//noinspection unchecked
		return (T) ois.readObject();
	}

	/**
	 * Serializes the original and returns the deserialized instance.
	 * Serialization is using XMLEncoder/XMLDecoder.
	 * <p/>
	 * See http://weblogs.java.net/blog/malenkov/archive/2006/08/how_to_encode_e.html
	 * for a description of an enum problem.
	 *
	 * @param original the original Serializable,
	 * @param enums    a list of enums that can be contained in original. Only needed in J2SE 1.5.
	 * @return the deserialized instance.
	 * @throws java.io.IOException    In case of error during (de)serialization.
	 * @throws ClassNotFoundException In case of error during (de)serialization.
	 * @see java.beans.PersistenceDelegate
	 */
	public static <T extends Serializable> T serializeXml(T original, Class... enums)
		throws IOException, ClassNotFoundException
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		XMLEncoder e = new XMLEncoder(os);
		if(enums != null)
		{
			PersistenceDelegate delegate = new EnumPersistenceDelegate();
			for(Class c : enums)
			{
				e.setPersistenceDelegate(c, delegate);
			}
		}
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

		if(same)
		{
			assertSame(original, result);
		}
		else
		{
			assertEquals("Hashcodes of " + original + " and " + result + " differ!", original.hashCode(), result.hashCode());
			assertEquals(original, result);
		}
		return result;
	}

	public static <T extends Serializable> T testXmlSerialization(T original, Class... enums)
		throws IOException, ClassNotFoundException
	{
		return testXmlSerialization(original, false, enums);
	}

	public static <T extends Serializable> T testXmlSerialization(T original, boolean same, Class... enums)
		throws IOException, ClassNotFoundException
	{
		T result = serializeXml(original, enums);

		if(same)
		{
			assertSame(original, result);
		}
		else
		{
			assertEquals("Hashcodes of " + original + " and " + result + " differ!", original.hashCode(), result.hashCode());
			assertEquals(original, result);
		}
		return result;
	}

	public static <T extends Cloneable> T reflectionClone(T original)
		throws IllegalAccessException, InvocationTargetException, NoSuchMethodException
	{
		Class<? extends Cloneable> clazz = original.getClass();
		Method method = clazz.getMethod("clone");

		//assertTrue("clone() method isn't accessible!", method.isAccessible());

		//noinspection unchecked
		return (T) method.invoke(original);
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

		if(same)
		{
			assertSame(original, result);
		}
		else
		{
			assertEquals("Hashcodes of " + original + " and " + result + " differ!", original.hashCode(), result.hashCode());
			assertEquals(original, result);
		}

		return result;
	}

	/**
	 * As described in http://weblogs.java.net/blog/malenkov/archive/2006/08/how_to_encode_e.html
	 */
	static class EnumPersistenceDelegate
		extends PersistenceDelegate
	{
		protected boolean mutatesTo(Object oldInstance, Object newInstance)
		{
			return oldInstance == newInstance;
		}

		protected Expression instantiate(Object oldInstance, Encoder out)
		{
			Enum e = (Enum) oldInstance;
			return new Expression(e, e.getClass(), "valueOf", new Object[]{e.name()});
		}
	}

}
