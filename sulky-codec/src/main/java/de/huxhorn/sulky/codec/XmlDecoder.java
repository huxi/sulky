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

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;

public class XmlDecoder<E>
	implements Decoder<E>
{
	private boolean compressing;

	public XmlDecoder()
	{
		this(false);
	}

	public XmlDecoder(boolean compressing)
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

	public E decode(byte[] bytes)
	{
		try(XMLDecoder decoder=createXMLDecoder(bytes))
		{
			Object result = decoder.readObject();
			@SuppressWarnings({"unchecked"})
			E e = (E) result;
			return e;
		}
		catch(Throwable e)
		{
			// silently ignore any problems
			return null;
		}
	}

	private XMLDecoder createXMLDecoder(byte[] bytes)
			throws IOException
	{
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		if(compressing)
		{
			GZIPInputStream gis = new GZIPInputStream(bis);
			return new XMLDecoder(gis);
		}
		return new XMLDecoder(bis);
	}

	public String toString()
	{
		return "XmlDecoder[compressing=" + compressing + "]";
	}
}
