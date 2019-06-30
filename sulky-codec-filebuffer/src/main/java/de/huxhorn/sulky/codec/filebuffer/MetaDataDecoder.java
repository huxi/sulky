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

package de.huxhorn.sulky.codec.filebuffer;

import com.google.protobuf.InvalidProtocolBufferException;
import de.huxhorn.sulky.codec.Decoder;
import de.huxhorn.sulky.codec.filebuffer.generated.MetaDataProto;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class MetaDataDecoder
	implements Decoder<MetaData>
{
	private boolean compressing;

	public MetaDataDecoder(boolean compressing)
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

	@Override
	public MetaData decode(byte[] bytes)
	{
		if(bytes == null)
		{
			return null;
		}
		MetaDataProto.MetaData parsedData = null;
		if(!compressing)
		{
			try
			{
				parsedData = MetaDataProto.MetaData.parseFrom(bytes);
			}
			catch(InvalidProtocolBufferException e)
			{
				// ignore
			}
		}
		else
		{

			try(ByteArrayInputStream in = new ByteArrayInputStream(bytes);
				GZIPInputStream gis = new GZIPInputStream(in))
			{
				parsedData = MetaDataProto.MetaData.parseFrom(gis);
			}
			catch(IOException e)
			{
				// ignore
			}
		}
		return convert(parsedData);
	}

	public static MetaData convert(MetaDataProto.MetaData data)
	{
		if(data == null)
		{
			return null;
		}
		int entryCount = data.getEntryCount();
		Map<String, String> dataMap = new HashMap<>();
		for(int i = 0; i < entryCount; i++)
		{
			MetaDataProto.MapEntry entry = data.getEntry(i);
			String key = null;
			String value = null;
			if(entry.hasKey())
			{
				key = entry.getKey();
			}
			if(entry.hasValue())
			{
				value = entry.getValue();
			}
			if(key != null)
			{
				dataMap.put(key, value);
			}
		}
		boolean sparse = false;
		if(data.hasSparse())
		{
			sparse = data.getSparse();
		}
		return new MetaData(dataMap, sparse);
	}
}
