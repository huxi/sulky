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
package de.huxhorn.sulky.codec;

import org.apache.commons.io.IOUtils;

import java.beans.XMLDecoder;
import java.io.ByteArrayInputStream;
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
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		XMLDecoder decoder;
		try
		{
			if(compressing)
			{
				GZIPInputStream gis = new GZIPInputStream(bis);
				decoder = new XMLDecoder(gis);
			}
			else
			{
				decoder = new XMLDecoder(bis);
			}

			Object result = decoder.readObject();
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
			IOUtils.closeQuietly(bis);
		}
	}

	public String toString()
	{
		return "XmlDeserializer[compressing=" + compressing + "]";
	}
}
