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

package de.huxhorn.sulky.codec;

import java.beans.XMLEncoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.zip.GZIPOutputStream;

/**
 * This class does only support XML serialization for the simple case where the class to serialize
 * adheres to the Java Beans guidelines.
 *
 * It must be reimplemented if PersistenceDelegates are required.
 *
 * @param <E> the type to be encoded
 */
public class XmlEncoder<E>
	implements Encoder<E>
{
	private boolean compressing;

	public XmlEncoder()
	{
		this(false);
	}

	public XmlEncoder(boolean compressing)
	{
		this.compressing = compressing;
	}

	/**
	 * Special c'tor to support enums in JDK &lt; 1.6.
	 * You have to list all enum types.
	 *
	 * @param compressing if the data is supposed to be gzipped.
	 * @param ignored     a list of all enum classes that need to be handles.
	 * @deprecated use XmlEncoder(boolean) instead.
	 */
	@SuppressWarnings("PMD.UnusedFormalParameter")
	public XmlEncoder(boolean compressing, Class... ignored)
	{
		this(compressing);
	}

	public boolean isCompressing()
	{
		return compressing;
	}

	public void setCompressing(boolean compressing)
	{
		this.compressing = compressing;
	}

	public byte[] encode(E object)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();

		try(XMLEncoder encoder=createXmlEncoder(bos))
		{
			encoder.writeObject(object);
			encoder.close();
			return bos.toByteArray();
		}
		catch(IOException e)
		{
			e.printStackTrace(); // NOPMD
			return null;
		}
	}

	private XMLEncoder createXmlEncoder(ByteArrayOutputStream bos)
			throws IOException
	{
		if(compressing)
		{
			GZIPOutputStream gos = new GZIPOutputStream(bos);
			return new XMLEncoder(gos);
		}
		return new XMLEncoder(bos);
	}

	public String toString()
	{
		return "XmlEncoder[compressing=" + compressing + "]";
	}
}
