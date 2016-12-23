/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2011 Joern Huxhorn
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
 * Copyright 2007-2011 Joern Huxhorn
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

import de.huxhorn.sulky.codec.Codec;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public class SparseDataStrategy<E>
	implements DataStrategy<E>
{
	/**
	 * The size of the data size, i.e. an int.
	 */
	public static final long DATA_LENGTH_SIZE = 4;

	/**
	 * The size of index, i.e. a long.
	 */
	public static final long INDEX_SIZE = 8;

	private boolean supportingOverwrite;

	public SparseDataStrategy()
	{
		this(true);
	}

	public SparseDataStrategy(boolean supportingOverwrite)
	{
		this.supportingOverwrite = supportingOverwrite;
	}

	public boolean isSupportingOverwrite()
	{
		return supportingOverwrite;
	}

	public void setSupportingOverwrite(boolean supportingOverwrite)
	{
		this.supportingOverwrite = supportingOverwrite;
	}

	public void add(E element,
	                RandomAccessFile indexFile,
	                RandomAccessFile dataFile,
	                Codec<E> codec,
	                IndexStrategy indexStrategy)
		throws IOException
	{
		long elementsCount = indexStrategy.getSize(indexFile);

		long offset = dataFile.length();

		internalWriteElement(dataFile, offset, elementsCount, element, codec);

		indexStrategy.setOffset(indexFile, elementsCount, offset);
	}

	public void addAll(List<E> elements,
	                   RandomAccessFile indexFile,
	                   RandomAccessFile dataFile,
	                   Codec<E> codec,
	                   IndexStrategy indexStrategy)
		throws IOException
	{
		if(elements != null)
		{
			int newElementCount = elements.size();
			if(newElementCount > 0)
			{
				long elementsCount = indexStrategy.getSize(indexFile);

				long offset = dataFile.length();

				long[] offsets = new long[newElementCount];
				int index = 0;
				for(E element : elements)
				{
					offsets[index] = offset;
					offset = offset + internalWriteElement(dataFile, offset, elementsCount + index, element, codec) + DATA_LENGTH_SIZE + INDEX_SIZE;
					index++;
				}

				index = 0;
				for(long curOffset : offsets)
				{
					indexStrategy.setOffset(indexFile, elementsCount + index, curOffset);
					index++;
				}
			}

		}
	}

	public boolean set(long index, E element, RandomAccessFile indexFile, RandomAccessFile dataFile, Codec<E> codec, IndexStrategy indexStrategy)
		throws IOException, UnsupportedOperationException
	{
		long offset = indexStrategy.getOffset(indexFile, index);
		if(!supportingOverwrite && offset >= 0)
		{
			return false;
		}
		if(element != null)
		{
			offset = dataFile.length();
			internalWriteElement(dataFile, offset, index, element, codec);

			indexStrategy.setOffset(indexFile, index, offset);
			return true;
		}
		else
		{
			// set offset to -1 to signal a null value.
			indexStrategy.setOffset(indexFile, index, -1);
			return true;
		}
	}

	public boolean isSetSupported()
	{
		return true;
	}

	public E get(long index,
	             RandomAccessFile indexFile,
	             RandomAccessFile dataFile,
	             Codec<E> codec,
	             IndexStrategy indexStrategy)
		throws IOException, ClassNotFoundException
	{
		long elementsCount = indexStrategy.getSize(indexFile);
		if(index >= 0 && index < elementsCount)
		{
			long offset = indexStrategy.getOffset(indexFile, index);
			if(offset < 0)
			{
				return null;
			}

			return internalReadElement(dataFile, offset, codec);
		}
		return null;
	}


	private int internalWriteElement(RandomAccessFile dataFile, long offset, long index, E element, Codec<E> codec)
		throws IOException
	{
		if(codec == null)
		{
			throw new IllegalStateException("Codec has not been initialized!");
		}
		byte[] buffer = codec.encode(element);

		int bufferSize = buffer.length;

		dataFile.seek(offset);
		dataFile.writeInt(bufferSize);
		dataFile.writeLong(index);
		dataFile.write(buffer);
		return bufferSize;
	}

	private E internalReadElement(RandomAccessFile dataFile, long offset, Codec<E> codec)
		throws IOException, ClassNotFoundException, ClassCastException
	{
		if(codec == null)
		{
			throw new IllegalStateException("Codec has not been initialized!");
		}

		if(dataFile.length() < offset + DATA_LENGTH_SIZE + INDEX_SIZE)
		{
			throw new IndexOutOfBoundsException("Invalid offset: " + offset + "! Couldn't read length of data!");
		}
		dataFile.seek(offset);
		int bufferSize = dataFile.readInt();
		long startOfData = offset + DATA_LENGTH_SIZE + INDEX_SIZE;
		if(dataFile.length() < startOfData + bufferSize)
		{
			throw new IndexOutOfBoundsException("Invalid length (" + bufferSize + ") at offset: " + offset + "!");
		}
		// ignore stored index in case of read
		dataFile.seek(startOfData);
		byte[] buffer = new byte[bufferSize];
		dataFile.readFully(buffer);
		return codec.decode(buffer);
	}
}
