/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2017 Joern Huxhorn
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
 * Copyright 2007-2017 Joern Huxhorn
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

public class FileHeader
{
	private final int magicValue;
	private final MetaData metaData;
	private final long dataOffset;

	public FileHeader(int magicValue, MetaData metaData, long dataOffset)
	{
		this.magicValue=magicValue;
		this.metaData=metaData;
		this.dataOffset=dataOffset;
	}

	public long getDataOffset()
	{
		return dataOffset;
	}

	public int getMagicValue()
	{
		return magicValue;
	}

	public MetaData getMetaData()
	{
		return metaData;
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		FileHeader that = (FileHeader) o;

		return dataOffset == that.dataOffset
				&& magicValue == that.magicValue
				&& (metaData != null ? metaData.equals(that.metaData) : that.metaData == null);
	}

	@Override
	public int hashCode()
	{
		int result = magicValue;
		result = 31 * result + (metaData != null ? metaData.hashCode() : 0);
		result = 31 * result + (int) (dataOffset ^ (dataOffset >>> 32));
		return result;
	}

	@Override
	public String toString()
	{
		StringBuilder result=new StringBuilder(200);
		result.append("FileHeader[magicValue=0x");

		String hexValue = Integer.toHexString(magicValue);
		int zeroes = 8 - hexValue.length();
		for(int i = 0; i < zeroes; i++)
		{
			result.append('0');
		}
		result.append(hexValue)

				.append(", metaData=").append(metaData)
				.append(", dataOffset=").append(dataOffset)
				.append(']');

		return result.toString();
	}
}
