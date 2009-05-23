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
package de.huxhorn.sulky.codec.filebuffer;

import de.huxhorn.sulky.codec.Encoder;
import de.huxhorn.sulky.codec.filebuffer.generated.MetaDataProto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

public class MetaDataEncoder
	implements Encoder<MetaData>
{
	private boolean compressing;

	public MetaDataEncoder(boolean compressing)
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

	public byte[] encode(MetaData data)
	{
		MetaDataProto.MetaData converted = convert(data);
		if(converted == null)
		{
			return null;
		}
		if(!compressing)
		{
			return converted.toByteArray();
		}
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		GZIPOutputStream gos;
		try
		{
			gos = new GZIPOutputStream(out);
			converted.writeTo(gos);
			gos.flush();
			gos.close();
			return out.toByteArray();
		}
		catch(IOException e)
		{
			// ignore
		}
		return null;
	}

	public static MetaDataProto.MetaData convert(MetaData metaData)
	{
		if(metaData == null)
		{
			return null;
		}

		boolean sparse = metaData.isSparse();
		Map<String, String> data = metaData.getData();

		MetaDataProto.MetaData.Builder builder = MetaDataProto.MetaData.newBuilder();
		if(sparse)
		{
			builder.setSparse(sparse);
		}
		for(Map.Entry<String, String> current : data.entrySet())
		{
			String key = current.getKey();
			String value = current.getValue();
			if(key != null)
			{
				builder.addEntry(MetaDataProto.MapEntry.newBuilder().setKey(key).setValue(value).build());
			}
		}

		return builder.build();
	}
}
