/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2019 Joern Huxhorn
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
 * Copyright 2007-2019 Joern Huxhorn
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

package de.huxhorn.sulky.version;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

/**
 * Copy of JUnitTools to prevent complaining about Java version 8 vs. 6
 */
public final class JUnitTools {
	static {
		new JUnitTools(); // stfu, coverage.
	}

	private JUnitTools()
	{}

	static <T extends Serializable> T testSerialization(T original)
		throws IOException, ClassNotFoundException
	{
		return testSerialization(original, false);
	}

	private static <T extends Serializable> T testSerialization(T original, boolean same)
		throws IOException, ClassNotFoundException
	{
		T result = serialize(original);

		equal(original, result, same);
		return result;
	}

	private static <T extends Serializable> T serialize(T original)
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

	private static void equal(Object original, Object other, boolean same)
	{
		if(original == null)
		{
			assertNull(other);
			return;
		}

		String messagePart = original.toString() + " and " + other;
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
}
