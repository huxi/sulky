/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2014 Joern Huxhorn
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
 * Copyright 2007-2014 Joern Huxhorn
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

package de.huxhorn.sulky.generics.io;

import de.huxhorn.sulky.io.IOUtilities;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.util.zip.GZIPOutputStream;


/**
 * @param <E> The type to be serialized.
 * @deprecated Use sulky-codec instead.
 */
public class SerializableSerializer<E extends Serializable>
	implements Serializer<E>
{
	private boolean compressing;

	public SerializableSerializer()
	{
		this(false);
	}

	public SerializableSerializer(boolean compressing)
	{
		this.compressing = compressing;
	}

	public boolean isCompressing()
	{
		return compressing;
	}

	public void setCompressing(boolean compressing)
	{
		this.compressing = compressing;
	}

	public byte[] serialize(E object)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		try
		{
			if(compressing)
			{
				GZIPOutputStream gos = new GZIPOutputStream(bos);
				oos = new ObjectOutputStream(gos);
			}
			else
			{
				oos = new ObjectOutputStream(bos);
			}
			oos.writeObject(object);
			oos.flush();
			oos.close();
			return bos.toByteArray();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			IOUtilities.closeQuietly(oos);
			IOUtilities.closeQuietly(bos);
		}
	}

	public String toString()
	{
		return "SerializableSerializer[compressing=" + compressing + "]";
	}
}
