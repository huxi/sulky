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
package de.huxhorn.sulky.buffers;

import org.apache.commons.io.output.CountingOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import java.util.Map;
import java.util.Collections;
import java.util.HashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

public class ExtendedSerializingFileBuffer<E>
	implements FileBuffer<E>
{
	private final Logger logger = LoggerFactory.getLogger(ExtendedSerializingFileBuffer.class);

	private ReadWriteLock readWriteLock;

	/**
	 * the file that contains the serialized objects.
	 */
	private File serializeFile;

	/**
	 * index file that contains the number of contained objects as well as the offsets of the objects in the
	 * serialized file.
	 */
	private File serializeIndexFile;
	private static final String INDEX_EXTENSION = ".index";
	private Integer magicValue;
	private Map<String, String> metaData;
	private static final int MAGIC_VALUE_SIZE = 4;
	private static final int META_LENGTH_SIZE = 4;
	private int initialDataOffset;

	public ExtendedSerializingFileBuffer(Integer magicValue, Map<String, String> metaData, File serializeFile)
	{
		this(magicValue, metaData, serializeFile, null);
	}

	public ExtendedSerializingFileBuffer(Integer magicValue, Map<String, String> metaData, File serializeFile, File serializeIndexFile)
	{
		this.readWriteLock = new ReentrantReadWriteLock(true);
		this.magicValue=magicValue;
		if(metaData!=null)
		{
			this.metaData=new HashMap<String, String>(metaData);
		}
		setSerializeFile(serializeFile);

		if(serializeIndexFile == null)
		{
			File parent = serializeFile.getParentFile();
			String indexName = serializeFile.getName();
			int dotIndex = indexName.lastIndexOf('.');
			if(dotIndex > 0)
			{
				// remove extension,
				indexName = indexName.substring(0, dotIndex);
			}
			indexName += INDEX_EXTENSION;
			serializeIndexFile = new File(parent, indexName);
		}

		setSerializeIndexFile(serializeIndexFile);
		if(serializeFile.exists())
		{
			Lock lock = readWriteLock.readLock();
			lock.lock();
			try
			{
				validateMagicValue();
				initializeMetaData();
			}
			finally
			{
				lock.unlock();
			}
		}
		else
		{
			Lock lock = readWriteLock.writeLock();
			lock.lock();
			try
			{
				writeMagicValue();
				writeMetaData();
			}
			finally
			{
				lock.unlock();
			}
		}
	}

	/**
	 * Locking is already performed for this method.
	 */
	protected void writeMetaData()
	{
		int offset=0;
		if(magicValue!=null)
		{
			offset=MAGIC_VALUE_SIZE;
		}

		RandomAccessFile raf=null;
		try
		{
			byte[] buffer = null;
			int length=0;
			if(metaData!=null)
			{

				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				GZIPOutputStream gos = new GZIPOutputStream(bos);
				CountingOutputStream cos = new CountingOutputStream(gos);
				ObjectOutputStream out = new ObjectOutputStream(cos);
				out.writeObject(metaData);
				out.flush();
				out.close();
				gos.finish();
				buffer = bos.toByteArray();
				length=buffer.length;
			}

			raf = new RandomAccessFile(serializeFile, "rw");
			raf.seek(offset);
			raf.writeInt(length);
			if(length>0)
			{
				raf.write(buffer);
			}
		}
		catch(Throwable e)
		{
			throw new IllegalArgumentException("Could not write meta data to "+serializeFile.getAbsolutePath()+"!", e);
		}
		finally
		{
			closeQuietly(raf);
		}
	}

	/**
	 * Locking is already performed for this method.
	 */
	protected void writeMagicValue()
	{
		if(magicValue!=null)
		{
			RandomAccessFile raf=null;
			try
			{
				raf = new RandomAccessFile(serializeFile, "rw");
				raf.seek(0);
				raf.writeInt(magicValue);
			}
			catch(Throwable e)
			{
				throw new IllegalArgumentException("Could not write magic value to "+serializeFile.getAbsolutePath()+"!", e);
			}
			finally
			{
				closeQuietly(raf);
			}
		}
	}

	/**
	 * Locking is already performed for this method.
	 */
	protected void initializeMetaData()
	{
		int offset=0;
		if(magicValue!=null)
		{
			offset=MAGIC_VALUE_SIZE;
		}
		RandomAccessFile raf = null;
		Throwable throwable=null;
		try
		{
			raf = new RandomAccessFile(serializeFile, "r");
			if(raf.length()>=offset+META_LENGTH_SIZE)
			{
				raf.seek(offset);
				int metaLength = raf.readInt();
				if(metaLength>0)
				{
					if(raf.length() < offset + META_LENGTH_SIZE + metaLength)
					{
						throw new IndexOutOfBoundsException("Invalid length (" + metaLength + ") at offset: " + offset + "!");
					}
					initialDataOffset=offset+META_LENGTH_SIZE+metaLength;
					byte[] buffer = new byte[metaLength];
					raf.readFully(buffer);
					ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
					GZIPInputStream gis = new GZIPInputStream(bis);
					ObjectInputStream ois = new ObjectInputStream(gis);
					//noinspection unchecked
					metaData = (Map<String, String>) ois.readObject();
				}
				else
				{
					metaData=null;
					initialDataOffset=offset+META_LENGTH_SIZE;
				}

			}
			else
			{
				throw new IllegalArgumentException("Could not read meta data from "+serializeFile.getAbsolutePath()+"!");
			}
		}
		catch(RuntimeException ex)
		{
			// rethrow
			throw ex;
		}
		catch(Throwable e)
		{
			throwable = e;
		}
		finally
		{
			closeQuietly(raf);
		}
		if(throwable!=null)
		{
			throw new IllegalArgumentException("Could not read meta data from "+serializeFile.getAbsolutePath()+"!", throwable);
		}

	}

	/**
	 * Locking is already performed for this method.
	 */
	protected void validateMagicValue()
	{
		if(magicValue!=null)
		{
			RandomAccessFile raf = null;
			Throwable throwable=null;
			try
			{
				raf = new RandomAccessFile(serializeFile, "r");
				if(raf.length()>=MAGIC_VALUE_SIZE)
				{
					raf.seek(0);
					int readMagicValue = raf.readInt();
					if(magicValue != readMagicValue)
					{
						throw new IllegalArgumentException("Read magicValue "+Integer.toHexString(readMagicValue)+" differs from expected magic value "+Integer.toHexString(magicValue)+"!");
					}
				}
				else
				{
					throw new IllegalArgumentException("Could not read magic value from "+serializeFile.getAbsolutePath()+"!");
				}
			}
			catch(IllegalArgumentException ex)
			{
				// rethrow
				throw ex;
			}
			catch(Throwable e)
			{
				throwable = e;
			}
			finally
			{
				closeQuietly(raf);
			}
			if(throwable!=null)
			{
				throw new IllegalArgumentException("Could not read magic value from "+serializeFile.getAbsolutePath()+"!", throwable);
			}
		}
		//To change body of created methods use File | Settings | File Templates.
	}

	public Map<String, String> getMetaData()
	{
		if(metaData==null)
		{
			return null;
		}
		return Collections.unmodifiableMap(metaData);
	}

	public Integer getMagicValue()
	{
		return magicValue;
	}

	public long getSize()
	{
		RandomAccessFile raf = null;
		Lock lock = readWriteLock.readLock();
		lock.lock();
		Throwable throwable;
		try
		{
			if(!serializeIndexFile.canRead())
			{
				return 0;
			}
			raf = new RandomAccessFile(serializeIndexFile, "r");
			return internalGetSize(raf);
		}
		catch(Throwable e)
		{
			throwable = e;
		}
		finally
		{
			closeQuietly(raf);
			lock.unlock();
		}
		// it's a really bad idea to log while locked *sigh*
		if(throwable != null)
		{
			if(logger.isDebugEnabled()) logger.debug("Couldn't retrieve size!", throwable);
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
			if(!serializeFile.canRead() && !serializeIndexFile.canRead())
			{
				return null;
			}
			randomSerializeIndexFile = new RandomAccessFile(serializeIndexFile, "r");
			randomSerializeFile = new RandomAccessFile(serializeFile, "r");
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
			closeQuietly(randomSerializeFile);
			closeQuietly(randomSerializeIndexFile);
			lock.unlock();
		}

		// it's a really bad idea to log while locked *sigh*
		if(throwable != null)
		{
			if(logger.isWarnEnabled())
			{
				if(throwable instanceof ClassNotFoundException
					|| throwable instanceof InvalidClassException)
				{
					logger.warn("Couldn't deserialize object at index " + index + "!\n" + throwable);
				}
				else if(throwable instanceof ClassCastException)
				{
					logger.warn("Couldn't cast deserialized object at index " + index + "!\n" + throwable);
				}
				else
				{
					logger.warn("Couldn't retrieve element at index " + index + "!", throwable);
				}
			}
		}
		else if(index < 0 || index >= elementsCount)
		{
			if(logger.isInfoEnabled())
			{
				logger.info("index (" + index + ") must be in the range [0..<" + elementsCount + "]. Returning null.");
			}
			return null;
		}

		return result;
	}

	public void add(E element)
	{
		RandomAccessFile randomSerializeIndexFile = null;
		RandomAccessFile randomSerializeFile = null;
		Lock lock = readWriteLock.writeLock();
		lock.lock();
		Throwable throwable = null;
		try
		{
			randomSerializeIndexFile = new RandomAccessFile(serializeIndexFile, "rw");
			randomSerializeFile = new RandomAccessFile(serializeFile, "rw");
			long elementsCount = internalGetSize(randomSerializeIndexFile);

			long offset = initialDataOffset;
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
			closeQuietly(randomSerializeFile);
			closeQuietly(randomSerializeIndexFile);
			lock.unlock();
		}
		if(throwable != null)
		{
			// it's a really bad idea to log while locked *sigh*
			if(logger.isWarnEnabled()) logger.warn("Couldn't write element!", throwable);
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
				Lock lock = readWriteLock.writeLock();
				lock.lock();
				Throwable throwable = null;
				try
				{
					randomSerializeIndexFile = new RandomAccessFile(serializeIndexFile, "rw");
					randomSerializeFile = new RandomAccessFile(serializeFile, "rw");

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
					//if(logger.isInfoEnabled()) logger.info("Elements after batch-write: {}", index+elementsCount);
				}
				catch(Throwable e)
				{
					throwable = e;
				}
				finally
				{
					closeQuietly(randomSerializeFile);
					closeQuietly(randomSerializeIndexFile);
					lock.unlock();
				}
				if(throwable != null)
				{
					// it's a really bad idea to log while locked *sigh*
					if(logger.isWarnEnabled()) logger.warn("Couldn't write element!", throwable);
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
		Lock lock = readWriteLock.writeLock();
		lock.lock();
		try
		{
			serializeIndexFile.delete();
			serializeFile.delete();
			writeMagicValue();
			writeMetaData();
		}
		finally
		{
			lock.unlock();
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
		return new BasicBufferIterator<E>(this);
	}

	static private void closeQuietly(RandomAccessFile raf)
	{
		final Logger logger = LoggerFactory.getLogger(ExtendedSerializingFileBuffer.class);

		if(raf != null)
		{
			try
			{
				raf.close();
			}
			catch(IOException e)
			{
				if(logger.isDebugEnabled()) logger.debug("Close on random access file threw exception!", e);
			}
		}
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
		//noinspection unchecked
		return (E) ois.readObject();
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
		CountingOutputStream cos = new CountingOutputStream(gos);
		ObjectOutputStream out = new ObjectOutputStream(cos);
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

	private void setSerializeFile(File serializeFile)
	{
		prepareFile(serializeFile);
//		if(logger.isDebugEnabled()) logger.debug("serializeFile="+serializeFile.getAbsolutePath());
		this.serializeFile = serializeFile;
	}

	private void setSerializeIndexFile(File serializeIndexFile)
	{
		prepareFile(serializeIndexFile);
		//if(logger.isDebugEnabled()) logger.debug("serializeIndexFile="+serializeIndexFile.getAbsolutePath());
		this.serializeIndexFile = serializeIndexFile;
	}

	private void prepareFile(File file)
	{
		File parent = file.getParentFile();
		if(parent != null)
		{
			parent.mkdirs();
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
}