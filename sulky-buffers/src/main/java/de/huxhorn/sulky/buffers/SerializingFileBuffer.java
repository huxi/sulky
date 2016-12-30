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

package de.huxhorn.sulky.buffers;

import de.huxhorn.sulky.io.IOUtilities;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SerializingFileBuffer<E>
	implements FileBuffer<E>
{
	private final Logger logger = LoggerFactory.getLogger(SerializingFileBuffer.class);

	private final ReadWriteLock readWriteLock;

	/**
	 * the file that contains the serialized objects.
	 */
	private File dataFile;

	/**
	 * index file that contains the number of contained objects as well as the offsets of the objects in the
	 * serialized file.
	 */
	private File indexFile;

	private static final String INDEX_EXTENSION = ".index";

	public SerializingFileBuffer(File dataFile)
	{
		this(dataFile, null);
	}

	public SerializingFileBuffer(File dataFile, File indexFile)
	{
		this.readWriteLock = new ReentrantReadWriteLock(true);
		setDataFile(dataFile);

		if(indexFile == null)
		{
			File parent = dataFile.getParentFile();
			String indexName = dataFile.getName();
			int dotIndex = indexName.lastIndexOf('.');
			if(dotIndex > 0)
			{
				// remove extension,
				indexName = indexName.substring(0, dotIndex);
			}
			indexName += INDEX_EXTENSION;
			indexFile = new File(parent, indexName);
		}

		setIndexFile(indexFile);
	}

	public long getSize()
	{
		RandomAccessFile raf = null;
		Throwable throwable;
		Lock lock = readWriteLock.readLock();
		lock.lock(); // FindBugs "Multithreaded correctness - Method does not release lock on all exception paths" is a false positive
		try
		{
			if(!indexFile.canRead())
			{
				return 0;
			}
			raf = new RandomAccessFile(indexFile, "r");
			return internalGetSize(raf);
		}
		catch(Throwable e)
		{
			throwable = e;
		}
		finally
		{
			IOUtilities.closeQuietly(raf);
			lock.unlock();
		}
		// it's a really bad idea to log while locked *sigh*
		if(throwable != null)
		{
			if(logger.isDebugEnabled()) logger.debug("Couldn't retrieve size!", throwable);
			IOUtilities.interruptIfNecessary(throwable);
		}
		return 0;
	}

	public E get(long index)
	{
		RandomAccessFile randomSerializeIndexFile = null;
		RandomAccessFile randomSerializeFile = null;
		E result = null;
		Lock lock = readWriteLock.readLock();
		lock.lock();
		Throwable throwable = null;
		long elementsCount = 0;
		try
		{
			if(!dataFile.canRead() || !indexFile.canRead())
			{
				return null;
			}
			randomSerializeIndexFile = new RandomAccessFile(indexFile, "r");
			randomSerializeFile = new RandomAccessFile(dataFile, "r");
			elementsCount = internalGetSize(randomSerializeIndexFile);
			if(index >= 0 && index < elementsCount)
			{
				long offset = internalOffsetOfElement(randomSerializeIndexFile, index);
				result = internalReadElement(randomSerializeFile, offset);

				return result;
			}
		}
		catch(Throwable e)
		{
			throwable = e;
		}
		finally
		{
			IOUtilities.closeQuietly(randomSerializeFile);
			IOUtilities.closeQuietly(randomSerializeIndexFile);
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
			IOUtilities.interruptIfNecessary(throwable);
		}
		else if(index < 0 || index >= elementsCount)
		{
			if(logger.isInfoEnabled()) logger.info("index ({}) must be in the range [0..<{}]. Returning null.", index, elementsCount);
			return null;
		}

		return result;
	}

	public void add(E element)
	{
		RandomAccessFile randomSerializeIndexFile = null;
		RandomAccessFile randomSerializeFile = null;
		Throwable throwable = null;
		Lock lock = readWriteLock.writeLock();
		lock.lock(); // FindBugs "Multithreaded correctness - Method does not release lock on all exception paths" is a false positive
		try
		{
			randomSerializeIndexFile = new RandomAccessFile(indexFile, "rw");
			randomSerializeFile = new RandomAccessFile(dataFile, "rw");
			long elementsCount = internalGetSize(randomSerializeIndexFile);

			long offset = 0;
			if(elementsCount > 0)
			{
				long prevElement = elementsCount - 1;
				offset = internalOffsetOfElement(randomSerializeIndexFile, prevElement);
				offset = offset + internalReadElementSize(randomSerializeFile, offset) + 4;
			}
			internalWriteElement(randomSerializeFile, offset, element);
			internalWriteOffset(randomSerializeIndexFile, elementsCount, offset);
		}
		catch(IOException e)
		{
			throwable = e;
		}
		finally
		{
			IOUtilities.closeQuietly(randomSerializeFile);
			IOUtilities.closeQuietly(randomSerializeIndexFile);
			lock.unlock();
		}
		if(throwable != null)
		{
			// it's a really bad idea to log while locked *sigh*
			if(logger.isWarnEnabled()) logger.warn("Couldn't write element!", throwable);
			IOUtilities.interruptIfNecessary(throwable);
		}

	}

	public void addAll(List<E> elements)
	{
		if(elements != null)
		{
			int newElementCount = elements.size();
			if(newElementCount > 0)
			{
				RandomAccessFile randomSerializeIndexFile = null;
				RandomAccessFile randomSerializeFile = null;
				Throwable throwable = null;
				Lock lock = readWriteLock.writeLock();
				lock.lock(); // FindBugs "Multithreaded correctness - Method does not release lock on all exception paths" is a false positive
				try
				{
					randomSerializeIndexFile = new RandomAccessFile(indexFile, "rw");
					randomSerializeFile = new RandomAccessFile(dataFile, "rw");

					long elementsCount = internalGetSize(randomSerializeIndexFile);

					long offset = 0;
					if(elementsCount > 0)
					{
						long prevElement = elementsCount - 1;
						offset = internalOffsetOfElement(randomSerializeIndexFile, prevElement);
						offset = offset + internalReadElementSize(randomSerializeFile, offset) + 4;
					}
					long[] offsets = new long[elements.size()];
					int index = 0;
					for(E element : elements)
					{
						offsets[index] = offset;
						offset = offset + internalWriteElement(randomSerializeFile, offset, element) + 4;
						index++;
					}

					index = 0;
					for(long curOffset : offsets)
					{
						internalWriteOffset(randomSerializeIndexFile, elementsCount + index, curOffset);
						index++;
					}
				}
				catch(Throwable e)
				{
					throwable = e;
				}
				finally
				{
					IOUtilities.closeQuietly(randomSerializeFile);
					IOUtilities.closeQuietly(randomSerializeIndexFile);
					lock.unlock();
				}
				if(throwable != null)
				{
					// it's a really bad idea to log while locked *sigh*
					if(logger.isWarnEnabled()) logger.warn("Couldn't write element!", throwable);
					IOUtilities.interruptIfNecessary(throwable);
				}
			}

		}
	}

	public void addAll(E[] elements)
	{
		addAll(Arrays.asList(elements));
	}


	public void reset()
	{
		boolean dataDeleted;
		boolean indexDeleted;
		Lock lock = readWriteLock.writeLock();
		lock.lock();
		try
		{
			indexDeleted=indexFile.delete();
			dataDeleted=dataFile.delete();
		}
		finally
		{
			lock.unlock();
		}
		if(!indexDeleted)
		{
			if(logger.isDebugEnabled()) logger.debug("Couldn't delete index file {}.", indexFile.getAbsolutePath());
		}
		if(!dataDeleted)
		{
			if(logger.isDebugEnabled()) logger.debug("Couldn't delete data file {}.", dataFile.getAbsolutePath());
		}
	}

	/**
	 * @return will always return false, i.e. it does not check for diskspace!
	 */
	public boolean isFull()
	{
		return false;
	}

	public Iterator<E> iterator()
	{
		return new BasicBufferIterator<>(this);
	}

	public File getDataFile()
	{
		return dataFile;
	}

	private long internalOffsetOfElement(RandomAccessFile randomSerializeIndexFile, long index)
		throws IOException
	{
		long offsetOffset = 8 * index;
		if(randomSerializeIndexFile.length() < offsetOffset + 8)
		{
			throw new IndexOutOfBoundsException("Invalid index: " + index + "!");
		}
		randomSerializeIndexFile.seek(offsetOffset);
		//if(logger.isDebugEnabled()) logger.debug("Offset of element {}: {}", index, result);
		return randomSerializeIndexFile.readLong();
	}

	private long internalGetSize(RandomAccessFile randomSerializeIndexFile)
		throws IOException
	{
		//if(logger.isDebugEnabled()) logger.debug("size={}", result);
		return randomSerializeIndexFile.length() / 8;
	}

	private E internalReadElement(RandomAccessFile randomSerializeFile, long offset)
		throws IOException, ClassNotFoundException, ClassCastException
	{
		if(randomSerializeFile.length() < offset + 4)
		{
			throw new IndexOutOfBoundsException("Invalid offset: " + offset + "! Couldn't read length of data!");
		}
		randomSerializeFile.seek(offset);
		int bufferSize = randomSerializeFile.readInt();
		if(randomSerializeFile.length() < offset + 4 + bufferSize)
		{
			throw new IndexOutOfBoundsException("Invalid length (" + bufferSize + ") at offset: " + offset + "!");
		}
		byte[] buffer = new byte[bufferSize];
		randomSerializeFile.readFully(buffer);
		ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
		GZIPInputStream gis = new GZIPInputStream(bis);
		ObjectInputStream ois = new ObjectInputStream(gis);
		//if(logger.isDebugEnabled()) logger.debug("Read element from offset {}.", offset);
		@SuppressWarnings({"unchecked"})
		E e= (E) ois.readObject();
		return e;
	}

	private void internalWriteOffset(RandomAccessFile randomSerializeIndexFile, long index, long offset)
		throws IOException
	{
		long offsetOffset = 8 * index;
		if(randomSerializeIndexFile.length() < offsetOffset)
		{
			throw new IOException("Invalid offsetOffset " + offsetOffset + "!");
		}
		randomSerializeIndexFile.seek(offsetOffset);
		randomSerializeIndexFile.writeLong(offset);
	}

	private int internalWriteElement(RandomAccessFile randomSerializeFile, long offset, E element)
		throws IOException
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		GZIPOutputStream gos = new GZIPOutputStream(bos);
		ObjectOutputStream out = new ObjectOutputStream(gos);
		out.writeObject(element);
		out.flush();
		out.close();
		gos.finish();
		byte[] buffer = bos.toByteArray();
		//int uncompressed=cos.getCount();

		int bufferSize = buffer.length;
		/*
		if(logger.isDebugEnabled())
		{
			int packedPercent=(int)(((double)bufferSize/(double)uncompressed)*100f);
			logger.debug("Uncompressed size: {}", uncompressed);
			logger.debug("Compressed size  : {} ({}%)", bufferSize, packedPercent);
		}
		*/
		randomSerializeFile.seek(offset);
		randomSerializeFile.writeInt(bufferSize);
		randomSerializeFile.write(buffer);
		return bufferSize;
	}

	private long internalReadElementSize(RandomAccessFile randomSerializeFile, long offset)
		throws IOException
	{
		randomSerializeFile.seek(offset);
		return randomSerializeFile.readInt();
	}

	private void setDataFile(File dataFile)
	{
		prepareFile(dataFile);
		this.dataFile = dataFile;
	}

	private void setIndexFile(File indexFile)
	{
		prepareFile(indexFile);
		this.indexFile = indexFile;
	}

	private void prepareFile(File file)
	{
		File parent = file.getParentFile();
		if(parent != null)
		{
			if(parent.mkdirs())
			{
				if(logger.isDebugEnabled()) logger.debug("Created directory {}.", parent.getAbsolutePath());
			}
			if(!parent.isDirectory())
			{
				throw new IllegalArgumentException(parent.getAbsolutePath() + " is not a directory!");
			}
			if(file.isFile() && !file.canWrite())
			{
				throw new IllegalArgumentException(file.getAbsolutePath() + " is not writable!");
			}
		}
	}

	public String toString()
	{
		StringBuilder result = new StringBuilder();
		result.append("SerializingFileBuffer[");

		result.append("dataFile=");
		if(dataFile == null)
		{
			result.append("null");
		}
		else
		{
			result.append("\"").append(dataFile.getAbsolutePath()).append("\"");
		}
		result.append(", ");

		result.append("indexFile=");
		if(indexFile == null)
		{
			result.append("null");
		}
		else
		{
			result.append("\"").append(indexFile.getAbsolutePath()).append("\"");
		}

		result.append("]");
		return result.toString();
	}
}
