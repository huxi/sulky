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
package de.huxhorn.sulky.codec.streaming;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class StreamingSerializableCodec<E extends Serializable>
	implements StreamingCodec<E>
{
	public E decode(InputStream from) throws IOException
	{
		ObjectInputStream ois = new ObjectInputStream(from);
		Object result = null;
		try
		{
			result = ois.readObject();
			//noinspection unchecked
			return (E) result;
		}
		catch (ClassNotFoundException e)
		{
			throw new IOException("Coudln't decode!",e);
		}
	}

	public void encode(E obj, OutputStream into) throws IOException
	{
		ObjectOutputStream oos = null;
		oos = new ObjectOutputStream(into);
		oos.writeObject(obj);
		oos.flush();
	}
}
