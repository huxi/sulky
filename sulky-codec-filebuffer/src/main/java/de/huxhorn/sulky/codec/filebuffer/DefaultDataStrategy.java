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
package de.huxhorn.sulky.codec.filebuffer;

import de.huxhorn.sulky.codec.Codec;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.List;

public class DefaultDataStrategy<E>
	implements DataStrategy<E>
{
	/**
	 * The size of the data size, i.e. an int.
	 */
	public static final long DATA_LENGTH_SIZE = 4;

	public void add(E element,
	                RandomAccessFile indexFile,
	                RandomAccessFile dataFile,
	                Codec<E> codec,
	                IndexStrategy indexStrategy)
		throws IOException
	{
		long elementsCount = indexStrategy.getSize(indexFile);

		long offset = dataFile.length();

		internalWriteElement(dataFile, offset, element, codec);

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
					offset = offset + internalWriteElement(dataFile, offset, element, codec) + DATA_LENGTH_SIZE;
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
		throw new UnsupportedOperationException("DefaultDataStrategy does not support set!");
	}

	public boolean isSetSupported()
	{
		return false;
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


	private int internalWriteElement(RandomAccessFile dataFile, long offset, E element, Codec<E> codec)
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

		if(dataFile.length() < offset + DATA_LENGTH_SIZE)
		{
			throw new IndexOutOfBoundsException("Invalid offset: " + offset + "! Couldn't read length of data!");
		}
		dataFile.seek(offset);
		int bufferSize = dataFile.readInt();
		if(dataFile.length() < offset + DATA_LENGTH_SIZE + bufferSize)
		{
			throw new IndexOutOfBoundsException("Invalid length (" + bufferSize + ") at offset: " + offset + "!");
		}
		byte[] buffer = new byte[bufferSize];
		dataFile.readFully(buffer);
		return codec.decode(buffer);
	}
}
