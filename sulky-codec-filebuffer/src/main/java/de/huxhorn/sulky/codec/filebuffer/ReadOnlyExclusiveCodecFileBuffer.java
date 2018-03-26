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

package de.huxhorn.sulky.codec.filebuffer;

import de.huxhorn.sulky.buffers.BasicBufferIterator;
import de.huxhorn.sulky.buffers.Buffer;
import de.huxhorn.sulky.codec.Codec;
import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReadOnlyExclusiveCodecFileBuffer<E>
	implements Buffer<E>
{
	private final Logger logger = LoggerFactory.getLogger(ReadOnlyExclusiveCodecFileBuffer.class);
	private final Lock lock=new ReentrantLock();

	private Codec<E> codec;
	private DataStrategy<E> dataStrategy;
	private IndexStrategy indexStrategy;
	private FileHeader fileHeader;
	private RandomAccessFile randomAccessIndexFile;
	private RandomAccessFile randomAccessDataFile;

	public ReadOnlyExclusiveCodecFileBuffer(File dataFile, File indexFile)
			throws IOException
	{
		this(dataFile, indexFile, new DefaultFileHeaderStrategy());
	}

	public ReadOnlyExclusiveCodecFileBuffer(File dataFile, File indexFile, FileHeaderStrategy fileHeaderStrategy)
			throws IOException
	{
		this.indexStrategy = new DefaultIndexStrategy();
		if(!dataFile.canRead())
		{
			throw new IllegalArgumentException("'"+dataFile.getAbsolutePath()+"' is not readable.");
		}
		if(!indexFile.canRead())
		{
			throw new IllegalArgumentException("'"+indexFile.getAbsolutePath()+"' is not readable.");
		}
		lock.lock();
		try
		{
			FileHeader header = fileHeaderStrategy.readFileHeader(dataFile);

			if(header == null)
			{
				throw new IllegalArgumentException("Could not read file header from file '" + dataFile.getAbsolutePath() + "'. File isn't compatible.");
			}
			setFileHeader(header);

			randomAccessIndexFile = new RandomAccessFile(indexFile, "r");
			randomAccessDataFile = new RandomAccessFile(dataFile, "r");
		}
		finally
		{
			lock.unlock();
		}
	}

	public Codec<E> getCodec()
	{
		lock.lock();
		try
		{
			return codec;
		}
		finally
		{
			lock.unlock();
		}
	}

	public void setCodec(Codec<E> codec)
	{
		lock.lock();
		try
		{
			this.codec = codec;
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * If no element is found, null is returned.
	 *
	 * @param index must be in the range <code>[0..(getSize()-1)]</code>.
	 * @return the element at the given index.
	 * @throws IllegalStateException if no Decoder has been set.
	 */
	public E get(long index)
	{
		Throwable throwable = null;
		lock.lock();
		try
		{
			if(codec == null)
			{
				throw new IllegalStateException("codec must not be null!");
			}
			if(randomAccessIndexFile != null && randomAccessDataFile != null)
			{
				try
				{
					return dataStrategy.get(index, randomAccessIndexFile, randomAccessDataFile, codec, indexStrategy);
				}
				catch(Throwable e)
				{
					throwable = e;
				}
			}
		}
		finally
		{
			lock.unlock();
		}
		// it's a really bad idea to log while locked *sigh*
		if(throwable != null)
		{
			if(throwable instanceof ClassNotFoundException
				|| throwable instanceof InvalidClassException)
			{
				if(logger.isWarnEnabled()) logger.warn("Couldn't deserialize object at index {}!\n{}", index, throwable);
			}
			else if(throwable instanceof ClassCastException)
			{
				if(logger.isWarnEnabled()) logger.warn("Couldn't cast deserialized object at index {}!\n{}", index, throwable);
			}
			else
			{
				if(logger.isWarnEnabled()) logger.warn("Couldn't retrieve element at index {}!", index, throwable);
			}
		}
		else
		{
			if(logger.isWarnEnabled()) logger.warn("Buffer has already been closed.");
		}
		return null;
	}

	@Override
	public long getSize()
	{
		Throwable throwable = null;
		lock.lock();
		try
		{
			if(randomAccessIndexFile != null)
			{
				try
				{
					return indexStrategy.getSize(randomAccessIndexFile);
				}
				catch(Throwable e)
				{
					throwable = e;
				}
			}
		}
		finally
		{
			lock.unlock();
		}

		// it's a really bad idea to log while locked *sigh*
		if(throwable != null)
		{
			if(logger.isDebugEnabled()) logger.debug("Couldn't retrieve size!", throwable);
		}
		else
		{
			if(logger.isWarnEnabled()) logger.warn("Buffer has already been closed.");
		}
		return 0;
	}

	@Override
	public Iterator<E> iterator()
	{
		return new BasicBufferIterator<>(this);
	}

	public void close()
	{
		lock.lock();
		try
		{
			if(randomAccessIndexFile != null) {
				try
				{
					randomAccessIndexFile.close();
				}
				catch (IOException e)
				{
					// ignore
				}
				finally
				{
					randomAccessIndexFile = null;
				}
			}
			if(randomAccessDataFile != null) {
				try
				{
					randomAccessDataFile.close();
				}
				catch (IOException e)
				{
					// ignore
				}
				finally
				{
					randomAccessDataFile = null;
				}
			}
		}
		finally
		{
			lock.unlock();
		}
	}

	public FileHeader getFileHeader()
	{
		lock.lock();
		try
		{
			return fileHeader;
		}
		finally
		{
			lock.unlock();
		}
	}

	private void setFileHeader(FileHeader fileHeader)
	{
		lock.lock();
		try
		{
			MetaData metaData = fileHeader.getMetaData();
			if(metaData.isSparse())
			{
				dataStrategy = new SparseDataStrategy<>();
			}
			else
			{
				dataStrategy = new DefaultDataStrategy<>();
			}
			this.fileHeader = fileHeader;
		}
		finally
		{
			lock.unlock();
		}
	}
}
