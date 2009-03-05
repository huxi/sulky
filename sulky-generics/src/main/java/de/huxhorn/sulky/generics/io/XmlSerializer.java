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

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.zip.GZIPOutputStream;

/**
 * This class does only support XML serialization for the simple case where the class to serialize
 * adheres to the Java Beans guidelines.
 *
 * It must be reimplemented if PersistenceDelegates are required.
 *
 * @param <E>
 * @deprecated Use sulky-codec instead.
 */
public class XmlSerializer<E>
	implements Serializer<E>
{
	private static final Class[] NO_ENUMS = {};
	private boolean compressing;
	private final Class[] enums;
	private EnumPersistenceDelegate enumDelegate;

	public XmlSerializer()
	{
		this(false);
	}

	public XmlSerializer(boolean compressing)
	{
		this(compressing, NO_ENUMS);
	}

	/**
	 * Special c'tor to support enums in JDK < 1.6.
	 * You have to list all enum types.
	 * @param compressing if the data is supposed to be gzipped.
	 * @param enums a list of all enum classes that need to be handles.
	 */
	public XmlSerializer(boolean compressing, Class... enums)
	{
		setCompressing(compressing);
		this.enums = enums;
		if(this.enums != null && this.enums.length > 0)
		{
			enumDelegate = new EnumPersistenceDelegate();
		}
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
		XMLEncoder encoder;
		try
		{
			if(compressing)
			{
				GZIPOutputStream gos = new GZIPOutputStream(bos);
				encoder = new XMLEncoder(gos);
			}
			else
			{
				encoder = new XMLEncoder(bos);
			}
			if(enums != null)
			{
				for(Class c : enums)
				{
					encoder.setPersistenceDelegate(c, enumDelegate);
				}
			}

			encoder.writeObject(object);
			encoder.close();
			return bos.toByteArray();
		}
		catch(IOException e)
		{
			e.printStackTrace();
			return null;
		}
		finally
		{
			IOUtils.closeQuietly(bos);
		}
	}


	public String toString()
	{
		return "XmlSerializer[compressing=" + compressing + ", enums=" + Arrays.toString(enums) + "]";
	}
}
