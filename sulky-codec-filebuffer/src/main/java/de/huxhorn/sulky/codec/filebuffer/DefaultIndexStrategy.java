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
