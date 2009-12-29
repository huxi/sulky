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
package de.huxhorn.sulky.codec.streaming;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import static org.junit.Assert.assertEquals;

public class StreamingSerializableCodecTest
{
	@Test
	public void test() throws IOException
	{
		String obj = "Foo";
		StreamingSerializableCodec<String> instance = new StreamingSerializableCodec<String>();
		ByteArrayOutputStream bos=new ByteArrayOutputStream();
		instance.encode(obj, bos);
		ByteArrayInputStream bis=new ByteArrayInputStream(bos.toByteArray());
		String decoded = instance.decode(bis);
		assertEquals(obj, decoded);
	}
}
