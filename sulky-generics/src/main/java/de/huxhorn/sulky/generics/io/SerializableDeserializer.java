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
package de.huxhorn.sulky.generics.io;

import org.apache.commons.io.IOUtils;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.io.Serializable;
import java.util.zip.GZIPInputStream;

public class SerializableDeserializer<E extends Serializable>
	implements Deserializer<E>
{
	private boolean compressing;

	public SerializableDeserializer()
	{
		this(false);
	}

	public SerializableDeserializer(boolean compressing)
	{
		setCompressing(compressing);
	}

	public boolean isCompressing()
	{
		return compressing;
	}

	public void setCompressing(boolean compressing)
	{
		this.compressing = compressing;
	}

	public E deserialize(byte[] bytes)
	{
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInputStream ois = null;
		try
		{
			if(compressing)
			{
				GZIPInputStream gis = new GZIPInputStream(bis);
				ois = new ObjectInputStream(gis);
			}
			else
			{
				ois = new ObjectInputStream(bis);
			}
			Object result = ois.readObject();
			//noinspection unchecked
			return (E) result;
		}
		catch(Throwable e)
		{
			// silently ignore any problems 
			return null;
		}
		finally
		{
			IOUtils.closeQuietly(ois);
			IOUtils.closeQuietly(bis);
		}
	}

	public String toString()
	{
		return "SerializableDeserializer[compressing=" + compressing + "]";
	}

}
