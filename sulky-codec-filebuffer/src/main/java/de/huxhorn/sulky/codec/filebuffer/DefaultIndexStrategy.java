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

/*
 * Copyright 2007-2010 Joern Huxhorn
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

import java.io.IOException;
import java.io.RandomAccessFile;

public class DefaultIndexStrategy
	implements IndexStrategy
{
	public static final long DATA_OFFSET_SIZE = 8;

	public void setOffset(RandomAccessFile indexFile, long index, long offset)
		throws IOException
	{
		long offsetOffset = DATA_OFFSET_SIZE * index;
		long fileLength = indexFile.length();
		if(fileLength < offsetOffset)
		{
			// extend file, filling with -1
			long lastIndex = fileLength / DATA_OFFSET_SIZE;
			indexFile.seek(lastIndex * DATA_OFFSET_SIZE); // this copes with malformed files
			for(long i = lastIndex; i < index; i++)
			{
				indexFile.writeLong(-1L);
			}
		}
		indexFile.seek(offsetOffset);
		indexFile.writeLong(offset);
	}

	public long getOffset(RandomAccessFile indexFile, long index)
		throws IOException
	{
		if(index < 0)
		{
			return -1;
		}
		long offsetOffset = DATA_OFFSET_SIZE * index;
		if(indexFile.length() < offsetOffset + DATA_OFFSET_SIZE)
		{
			return -1;
		}
		indexFile.seek(offsetOffset);
		return indexFile.readLong();
	}

	public long getSize(RandomAccessFile indexFile)
		throws IOException
	{
		//if(logger.isDebugEnabled()) logger.debug("size={}", result);
		return indexFile.length() / DATA_OFFSET_SIZE;
	}

}
