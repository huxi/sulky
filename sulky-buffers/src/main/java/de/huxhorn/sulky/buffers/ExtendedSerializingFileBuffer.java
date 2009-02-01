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

import de.huxhorn.sulky.generics.io.Deserializer;
import de.huxhorn.sulky.generics.io.Serializer;

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
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * In contrast to SerializingFileBuffer, this implementation supports the following:
 * <p/>
 * <ul>
 * <li>An optional magic value to identify the type of a buffer file.<br/>
 * If present (and it should be), it is contained in the first four bytes of the data-file and can be evaluated by external classes, e.g. FileFilters.
 * An application would use one (or more) specific magic value to identify it's own files.
 * </li>
 * <li>Configurable Serializer and Deserializer so the way the elements are actually written and read can be changed as needed.
 * </li>
 * <li>
 * Optional meta data that can be used to provide additional informations about the content of the buffer.
 * It might be used to identify the correct pair of Serializer and Deserrializer required by the buffer
 * </li>
 * <li>Optional ElementProcessors that are executed after elements are added to the buffer.</li>
 * </ul>
 * <p/>
 * TODO: more docu :p
 *
 * @param <E> the type of objects that are stored in this buffer.
 */
public class ExtendedSerializingFileBuffer<E>
	implements FileBuffer<E>
{
	private final Logger logger = LoggerFactory.getLogger(ExtendedSerializingFileBuffer.class);

	private ReadWriteLock readWriteLock;

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
	private Integer magicValue;
	private Map<String, String> metaData;
	private static final int MAGIC_VALUE_SIZE = 4;
	private static final int META_LENGTH_SIZE = 4;
	private long initialDataOffset;

	private Serializer<E> serializer;
	private Deserializer<E> deserializer;
	private List<ElementProcessor<E>> elementProcessors;

	/**
	 * Shortcut for ExtendedSerializingFileBuffer(magicValue, metaData, null, null, serializeFile, null).
	 *
	 * @param magicValue the magic value of the buffer, can be null but shouldn't.
	 * @param metaData   the meta data of the buffer. Might be null.
	 * @param dataFile   the data file.
	 * @see ExtendedSerializingFileBuffer#ExtendedSerializingFileBuffer(Integer, java.util.Map, de.huxhorn.sulky.generics.io.Serializer, de.huxhorn.sulky.generics.io.Deserializer, java.io.File, java.io.File) for description.
	 */
	public ExtendedSerializingFileBuffer(Integer magicValue, Map<String, String> metaData, File dataFile)
	{
		this(magicValue, metaData, null, null, dataFile, null);
	}

	/**
	 * Shortcut for ExtendedSerializingFileBuffer(magicValue, metaData, null, null, serializeFile, null).
	 *
	 * @param magicValue   the magic value of the buffer, can be null but shouldn't.
	 * @param metaData     the meta data of the buffer. Might be null.
	 * @param serializer   the serializer used by this buffer. Might be null.
	 * @param deserializer the serializer used by this buffer.  Might be null.
	 * @param dataFile     the data file.
	 * @see ExtendedSerializingFileBuffer#ExtendedSerializingFileBuffer(Integer, java.util.Map, de.huxhorn.sulky.generics.io.Serializer, de.huxhorn.sulky.generics.io.Deserializer, java.io.File, java.io.File) for description.
	 */
	public ExtendedSerializingFileBuffer(Integer magicValue, Map<String, String> metaData, Serializer<E> serializer, Deserializer<E> deserializer, File dataFile)
	{
		this(magicValue, metaData, serializer, deserializer, dataFile, null);
	}

	/**
	 * TODO: add description :p
	 *
	 * @param magicValue   the magic value of the buffer, can be null but shouldn't.
	 * @param metaData     the meta data of the buffer. Might be null.
	 * @param serializer   the serializer used by this buffer. Might be null.
	 * @param deserializer the serializer used by this buffer.  Might be null.
	 * @param dataFile     the data file.
	 * @param indexFile    the index file of the buffer.
	 * @see ExtendedSerializingFileBuffer#ExtendedSerializingFileBuffer(Integer, java.util.Map, de.huxhorn.sulky.generics.io.Serializer, de.huxhorn.sulky.generics.io.Deserializer, java.io.File, java.io.File) for description.
	 */
	public ExtendedSerializingFileBuffer(Integer magicValue, Map<String, String> metaData, Serializer<E> serializer, Deserializer<E> deserializer, File dataFile, File indexFile)
	{
		this.readWriteLock = new ReentrantReadWriteLock(true);
		this.magicValue = magicValue;
		if(metaData != null)
		{
			this.metaData = new HashMap<String, String>(metaData);
		}
		this.serializer = serializer;
		this.deserializer = deserializer;

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
		if(dataFile.exists())
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
		if(dataFile.length() > initialDataOffset)
		{

		}
	}

	/**
	 * Locking is already performed for this method.
	 */
	protected void validateMagicValue()
	{
		if(magicValue != null)
		{
			RandomAccessFile raf = null;
			Throwable throwable = null;
			try
			{
				raf = new RandomAccessFile(dataFile, "r");
				if(raf.length() >= MAGIC_VALUE_SIZE)
				{
					raf.seek(0);
					int readMagicValue = raf.readInt();
					if(magicValue != readMagicValue)
					{
						throw new IllegalArgumentException("Read magic value 0x" + Integer
							.toHexString(readMagicValue) + " differs from expected magic value 0x" + Integer
							.toHexString(magicValue) + "!");
					}
				}
				else
				{
					throw new IllegalArgumentException("Could not read magic value from " + dataFile
						.getAbsolutePath() + "!");
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
			if(throwable != null)
			{
				throw new IllegalArgumentException("Could not read magic value from " + dataFile
					.getAbsolutePath() + "!", throwable);
			}
		}
	}

	/**
	 * Locking is already performed for this method.
	 */
	protected void writeMagicValue()
	{
		if(magicValue != null)
		{
			RandomAccessFile raf = null;
			try
			{
				raf = new RandomAccessFile(dataFile, "rw");
				raf.seek(0);
				raf.writeInt(magicValue);
			}
			catch(Throwable e)
			{
				throw new IllegalArgumentException("Could not write magic value to " + dataFile
					.getAbsolutePath() + "!", e);
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
	protected void writeMetaData()
	{
		int offset = 0;
		if(magicValue != null)
		{
			offset = MAGIC_VALUE_SIZE;
		}

		RandomAccessFile raf = null;
		try
		{
			byte[] buffer = null;
			int length = 0;
			if(metaData != null)
			{
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				GZIPOutputStream gos = new GZIPOutputStream(bos);
				ObjectOutputStream out = new ObjectOutputStream(gos);
				out.writeObject(metaData);
				out.flush();
				out.close();
				gos.finish();
				buffer = bos.toByteArray();
				length = buffer.length;
			}

			raf = new RandomAccessFile(dataFile, "rw");
			raf.seek(offset);
			raf.writeInt(length);
			if(length > 0)
			{
				raf.seek(offset + META_LENGTH_SIZE);
				raf.write(buffer);
			}
			setInitialDataOffset(offset + META_LENGTH_SIZE + length);
		}
		catch(Throwable e)
		{
			throw new IllegalArgumentException("Could not write meta data to " + dataFile
				.getAbsolutePath() + "!", e);
		}
		finally
		{
			closeQuietly(raf);
		}
	}

	/**
	 * Locking is already performed for this method.
	 */
	protected void initializeMetaData()
	{
		int offset = 0;
		if(magicValue != null)
		{
			offset = MAGIC_VALUE_SIZE;
		}
		RandomAccessFile raf = null;
		Throwable throwable = null;
		try
		{
			raf = new RandomAccessFile(dataFile, "r");
			if(raf.length() >= offset + META_LENGTH_SIZE)
			{
				raf.seek(offset);
				int metaLength = raf.readInt();
				if(metaLength > 0)
				{
					if(raf.length() < offset + META_LENGTH_SIZE + metaLength)
					{
						throw new IndexOutOfBoundsException("Invalid length (" + metaLength + ") at offset: " + offset + "!");
					}
					setInitialDataOffset(offset + META_LENGTH_SIZE + metaLength);
					raf.seek(offset + META_LENGTH_SIZE);
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
					metaData = null;
					setInitialDataOffset(offset + META_LENGTH_SIZE);
				}
			}
			else
			{
				throw new IllegalArgumentException("Could not read meta data from " + dataFile
					.getAbsolutePath() + "!");
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
		if(throwable != null)
		{
			throw new IllegalArgumentException("Could not read meta data from " + dataFile
				.getAbsolutePath() + "!", throwable);
		}
	}


	protected void setInitialDataOffset(long initialDataOffset)
	{
		this.initialDataOffset = initialDataOffset;
	}

	protected long getInitialDataOffset()
	{
		return initialDataOffset;
	}

	public Serializer<E> getSerializer()
	{
		return serializer;
	}

	public void setSerializer(Serializer<E> serializer)
	{
		this.serializer = serializer;
	}

	public Deserializer<E> getDeserializer()
	{
		return deserializer;
	}

	public void setDeserializer(Deserializer<E> deserializer)
	{
		this.deserializer = deserializer;
	}

	public List<ElementProcessor<E>> getElementProcessors()
	{
		return elementProcessors;
	}

	public void setElementProcessors(List<ElementProcessor<E>> elementProcessors)
	{
		this.elementProcessors = elementProcessors;
	}

	public Map<String, String> getMetaData()
	{
		if(metaData == null)
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

	/**
	 * @param index must be in the range <tt>[0..(getSize()-1)]</tt>.
	 * @return the element at the given index.
	 * @throws IllegalStateException     if no Deserializer has been set.
	 * @throws IndexOutOfBoundsException if there is no element at the given index.
	 */
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

	/**
	 * Adds the element to the end of the buffer.
	 *
	 * @param element to add.
	 * @throws IllegalStateException if no Serializer has been set.
	 */
	public void add(E element)
	{
		RandomAccessFile randomSerializeIndexFile = null;
		RandomAccessFile randomSerializeFile = null;
		Lock lock = readWriteLock.writeLock();
		lock.lock();
		Throwable throwable = null;
		try
		{
			randomSerializeIndexFile = new RandomAccessFile(indexFile, "rw");
			randomSerializeFile = new RandomAccessFile(dataFile, "rw");
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

			// call proecssors if available
			List<ElementProcessor<E>> localProcessors = elementProcessors;
			if(localProcessors != null)
			{
				for(ElementProcessor<E> current : elementProcessors)
				{
					current.processElement(element);
				}
			}
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

	/**
	 * Adds all elements to the end of the buffer.
	 *
	 * @param elements to add.
	 * @throws IllegalStateException if no Serializer has been set.
	 */
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
					randomSerializeIndexFile = new RandomAccessFile(indexFile, "rw");
					randomSerializeFile = new RandomAccessFile(dataFile, "rw");

					long elementsCount = internalGetSize(randomSerializeIndexFile);

					long offset = initialDataOffset;
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
					// call proecssors if available
					List<ElementProcessor<E>> localProcessors = elementProcessors;
					if(localProcessors != null)
					{
						for(ElementProcessor<E> current : elementProcessors)
						{
							current.processElements(elements);
						}
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
			indexFile.delete();
			dataFile.delete();
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
		if(deserializer == null)
		{
			throw new IllegalStateException("Deserializer has not been initialized!");
		}

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
		return deserializer.deserialize(buffer);
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
		if(serializer == null)
		{
			throw new IllegalStateException("Serializer has not been initialized!");
		}
		byte[] buffer = serializer.serialize(element);

		int bufferSize = buffer.length;

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
//		if(logger.isDebugEnabled()) logger.debug("dataFile="+dataFile.getAbsolutePath());
		this.dataFile = dataFile;
	}

	private void setIndexFile(File indexFile)
	{
		prepareFile(indexFile);
		//if(logger.isDebugEnabled()) logger.debug("indexFile="+indexFile.getAbsolutePath());
		this.indexFile = indexFile;
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

	public String toString()
	{
		StringBuilder result = new StringBuilder();
		result.append("ExtendedSerializingFileBuffer[");

		result.append("magicValue=");
		if(magicValue == null)
		{
			result.append("null");
		}
		else
		{
			result.append("0x").append(Integer.toHexString(magicValue));
		}
		result.append(", ");

		result.append("metaData=").append(metaData);
		result.append(", ");

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
		result.append(", ");

		result.append("serializer=").append(serializer);
		result.append(", ");

		result.append("deserializer=").append(deserializer);

		result.append("]");
		return result.toString();
	}
}