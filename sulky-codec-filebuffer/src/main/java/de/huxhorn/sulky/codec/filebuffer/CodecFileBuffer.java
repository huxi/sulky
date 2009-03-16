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

import de.huxhorn.sulky.buffers.BasicBufferIterator;
import de.huxhorn.sulky.buffers.ElementProcessor;
import de.huxhorn.sulky.buffers.FileBuffer;
import de.huxhorn.sulky.buffers.ResetOperation;
import de.huxhorn.sulky.codec.Codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * In contrast to SerializingFileBuffer, this implementation supports the following:
 * <p/>
 * <ul>
 * <li>An optional magic value to identify the type of a buffer file.<br/>
 * If present (and it should be), it is contained in the first four bytes of the data-file and can be evaluated by external classes, e.g. FileFilters.
 * An application would use one (or more) specific magic value to identify it's own files.
 * </li>
 * <li>Configurable Codec so the way the elements are actually written and read can be changed as needed.
 * </li>
 * <li>
 * Optional meta data that can be used to provide additional informations about the content of the buffer.
 * It might be used to identify the correct Codec required by the buffer
 * </li>
 * <li>Optional ElementProcessors that are executed after elements are added to the buffer.</li>
 * </ul>
 * <p/>
 * TODO: more docu :p
 *
 * @param <E> the type of objects that are stored in this buffer.
 */
public class CodecFileBuffer<E>
	implements FileBuffer<E>
{
	private static final long DATA_OFFSET_SIZE = 8;
	private static final long DATA_LENGTH_SIZE = 4;

	private final Logger logger = LoggerFactory.getLogger(CodecFileBuffer.class);

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
	private Map<String, String> preferredMetaData;

	private Codec<E> codec;
	private List<ElementProcessor<E>> elementProcessors;
	//private CodecFileHeader codecFileHeader;
	private FileHeaderStrategy fileHeaderStrategy;
	private int magicValue;
	private FileHeader fileHeader;

	/**
	 * Shortcut for CodecFileBuffer(magicValue, metaData, null, null, serializeFile, null).
	 *
	 * @param magicValue        the magic value of the buffer.
	 * @param preferredMetaData the meta data of the buffer. Might be null.
	 * @param dataFile          the data file.
	 * @see CodecFileBuffer#CodecFileBuffer(int, java.util.Map, de.huxhorn.sulky.codec.Codec, java.io.File, java.io.File) for description.
	 */
	public CodecFileBuffer(int magicValue, Map<String, String> preferredMetaData, File dataFile)
	{
		this(magicValue, preferredMetaData, null, dataFile, null);
	}

	/**
	 * Shortcut for CodecFileBuffer(magicValue, metaData, null, null, serializeFile, null).
	 *
	 * @param magicValue        the magic value of the buffer.
	 * @param preferredMetaData the meta data of the buffer. Might be null.
	 * @param codec             the codec used by this buffer. Might be null.
	 * @param dataFile          the data file.
	 * @see CodecFileBuffer#CodecFileBuffer(int, java.util.Map, de.huxhorn.sulky.codec.Codec, java.io.File, java.io.File) for description.
	 */
	public CodecFileBuffer(int magicValue, Map<String, String> preferredMetaData, Codec<E> codec, File dataFile)
	{
		this(magicValue, preferredMetaData, codec, dataFile, null);
	}

	/**
	 * TODO: add description :p
	 *
	 * @param magicValue        the magic value of the buffer.
	 * @param preferredMetaData the meta data of the buffer. Might be null.
	 * @param codec             the codec used by this buffer. Might be null.
	 * @param dataFile          the data file.
	 * @param indexFile         the index file of the buffer.
	 * @throws NullPointerException if magicValue is null.
	 */
	public CodecFileBuffer(int magicValue, Map<String, String> preferredMetaData, Codec<E> codec, File dataFile, File indexFile)
	{
		this(magicValue, preferredMetaData, codec, dataFile, indexFile, new DefaultFileHeaderStrategy());
	}

	public CodecFileBuffer(int magicValue, Map<String, String> preferredMetaData, Codec<E> codec, File dataFile, File indexFile, FileHeaderStrategy fileHeaderStrategy)
	{
		this.magicValue = magicValue;
		this.fileHeaderStrategy = fileHeaderStrategy;
		this.readWriteLock = new ReentrantReadWriteLock(true);

		if(preferredMetaData != null)
		{
			preferredMetaData = new HashMap<String, String>(preferredMetaData);
		}
		if(preferredMetaData != null)
		{
			this.preferredMetaData = new HashMap<String, String>(preferredMetaData);
		}
		this.codec = codec;

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

		if(!initFilesIfNecessary())
		{
			validateHeader();
		}
	}

	private void validateHeader()
	{
		Lock lock = readWriteLock.readLock();
		lock.lock();
		this.fileHeader = null;
		try
		{
			FileHeader fileHeader = fileHeaderStrategy.readFileHeader(dataFile);
			if(fileHeader == null)
			{
				throw new IllegalArgumentException("Could not read file header from file '" + dataFile
					.getAbsolutePath() + "'. File isn't compatible.");
			}
			if(fileHeader.getMagicValue() != magicValue)
			{
				throw new IllegalArgumentException("Wrong magic value. Expected 0x" + Integer
					.toHexString(magicValue) + " but was " + Integer.toHexString(fileHeader.getMagicValue()) + "!");
			}
			if(dataFile.length() > fileHeader.getDataOffset())
			{
				if(!indexFile.exists() || indexFile.length() < DATA_OFFSET_SIZE)
				{
					throw new IllegalArgumentException("dataFile contains data but indexFile " + indexFile
						.getAbsolutePath() + " is not valid!");
				}
			}
			this.fileHeader = fileHeader;
		}
		catch(IOException ex)
		{
			throw new IllegalArgumentException("Could not read magic value from file '" + dataFile
				.getAbsolutePath() + "'!", ex);
		}
		finally
		{
			lock.unlock();
		}
	}


	public Codec<E> getCodec()
	{
		return codec;
	}

	public void setCodec(Codec<E> codec)
	{
		this.codec = codec;
	}

	public List<ElementProcessor<E>> getElementProcessors()
	{
		if(elementProcessors == null)
		{
			return null;
		}
		return Collections.unmodifiableList(elementProcessors);
	}

	public void setElementProcessors(List<ElementProcessor<E>> elementProcessors)
	{
		if(elementProcessors != null)
		{
			if(elementProcessors.size() == 0)
			{
				// performance enhancement
				elementProcessors = null;
			}
			else
			{
				elementProcessors = new ArrayList<ElementProcessor<E>>(elementProcessors);
			}
		}
		this.elementProcessors = elementProcessors;
	}

	private boolean initFilesIfNecessary()
	{
		if(!dataFile.exists() || dataFile.length() < fileHeaderStrategy.getMinimalSize())
		{
			Lock lock = readWriteLock.writeLock();
			lock.lock();
			try
			{
				dataFile.delete();
				fileHeader = fileHeaderStrategy.writeFileHeader(dataFile, magicValue, preferredMetaData);
				indexFile.delete();
				return true;
			}
			catch(IOException e)
			{
				if(logger.isWarnEnabled()) logger.warn("Exception while initializing file!", e);
			}
			finally
			{
				lock.unlock();
			}
		}
		return false;
	}

	public FileHeader getFileHeader()
	{
		return fileHeader;
	}

	/**
	 * @return the preferred meta data of the buffer, as defined by c'tor.
	 */
	public Map<String, String> getPreferredMetaData()
	{
		if(preferredMetaData == null)
		{
			return null;
		}
		return Collections.unmodifiableMap(preferredMetaData);
	}

	public File getDataFile()
	{
		return dataFile;
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
	 * If no element is found, null is returned.
	 *
	 * @param index must be in the range <tt>[0..(getSize()-1)]</tt>.
	 * @return the element at the given index.
	 * @throws IllegalStateException if no Decoder has been set.
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
				if(offset < 0)
				{
					return null;
				}
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
	 * @throws IllegalStateException if no Encoder has been set.
	 */
	public void add(E element)
	{
		initFilesIfNecessary();
		RandomAccessFile randomIndexFile = null;
		RandomAccessFile randomDataFile = null;
		Lock lock = readWriteLock.writeLock();
		lock.lock();
		Throwable throwable = null;
		try
		{
			randomIndexFile = new RandomAccessFile(indexFile, "rw");
			randomDataFile = new RandomAccessFile(dataFile, "rw");
			long elementsCount = internalGetSize(randomIndexFile);

			long offset = fileHeader.getDataOffset();
			if(elementsCount > 0)
			{
				long prevElement = elementsCount - 1;
				long readOffset = internalOffsetOfElement(randomIndexFile, prevElement);
				if(readOffset > 0)
				{
					int elementSize = internalReadElementSize(randomDataFile, readOffset);
					if(elementSize > 0)
					{
						offset = readOffset + elementSize + DATA_LENGTH_SIZE;
					}
					else
					{
						randomIndexFile.setLength(0);
						elementsCount = 0;
					}
				}
			}
			internalWriteElement(randomDataFile, offset, element);

			internalWriteOffset(randomIndexFile, elementsCount, offset);

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
			closeQuietly(randomDataFile);
			closeQuietly(randomIndexFile);
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
	 * @throws IllegalStateException if no Encoder has been set.
	 */
	public void addAll(List<E> elements)
	{
		if(elements != null)
		{
			initFilesIfNecessary();
			int newElementCount = elements.size();
			if(newElementCount > 0)
			{
				RandomAccessFile randomIndexFile = null;
				RandomAccessFile randomDataFile = null;
				Lock lock = readWriteLock.writeLock();
				lock.lock();
				Throwable throwable = null;
				try
				{
					randomIndexFile = new RandomAccessFile(indexFile, "rw");
					randomDataFile = new RandomAccessFile(dataFile, "rw");

					long elementsCount = internalGetSize(randomIndexFile);

					long offset = fileHeader.getDataOffset();
					if(elementsCount > 0)
					{
						long prevElement = elementsCount - 1;
						long readOffset = internalOffsetOfElement(randomIndexFile, prevElement);
						if(readOffset > 0)
						{
							int elementSize = internalReadElementSize(randomDataFile, readOffset);
							if(elementSize > 0)
							{
								offset = readOffset + elementSize + DATA_LENGTH_SIZE;
							}
							else
							{
								randomIndexFile.setLength(0);
								elementsCount = 0;
							}
						}
					}
					long[] offsets = new long[elements.size()];
					int index = 0;
					for(E element : elements)
					{
						offsets[index] = offset;
						offset = offset + internalWriteElement(randomDataFile, offset, element) + DATA_LENGTH_SIZE;
						index++;
					}

					index = 0;
					for(long curOffset : offsets)
					{
						internalWriteOffset(randomIndexFile, elementsCount + index, curOffset);
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
					closeQuietly(randomDataFile);
					closeQuietly(randomIndexFile);
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
			fileHeaderStrategy.writeFileHeader(dataFile, magicValue, preferredMetaData);
			if(elementProcessors != null)
			{
				for(ElementProcessor<E> current : elementProcessors)
				{
					if(current instanceof ResetOperation)
					{
						ResetOperation reset = (ResetOperation) current;
						reset.reset();
					}
				}
			}
		}
		catch(IOException e)
		{
			if(logger.isWarnEnabled()) logger.warn("Exception while resetting file!", e);
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
		final Logger logger = LoggerFactory.getLogger(CodecFileBuffer.class);

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

	private long internalOffsetOfElement(RandomAccessFile randomIndexFile, long index)
		throws IOException
	{
		long offsetOffset = DATA_OFFSET_SIZE * index;
		if(randomIndexFile.length() < offsetOffset + DATA_OFFSET_SIZE)
		{
			return -1;
		}
		randomIndexFile.seek(offsetOffset);
		return randomIndexFile.readLong();
	}

	private long internalGetSize(RandomAccessFile randomIndexFile)
		throws IOException
	{
		//if(logger.isDebugEnabled()) logger.debug("size={}", result);
		return randomIndexFile.length() / DATA_OFFSET_SIZE;
	}

	private E internalReadElement(RandomAccessFile randomSerializeFile, long offset)
		throws IOException, ClassNotFoundException, ClassCastException
	{
		if(codec == null)
		{
			throw new IllegalStateException("Codec has not been initialized!");
		}

		if(randomSerializeFile.length() < offset + DATA_LENGTH_SIZE)
		{
			throw new IndexOutOfBoundsException("Invalid offset: " + offset + "! Couldn't read length of data!");
		}
		randomSerializeFile.seek(offset);
		int bufferSize = randomSerializeFile.readInt();
		if(randomSerializeFile.length() < offset + DATA_LENGTH_SIZE + bufferSize)
		{
			throw new IndexOutOfBoundsException("Invalid length (" + bufferSize + ") at offset: " + offset + "!");
		}
		byte[] buffer = new byte[bufferSize];
		randomSerializeFile.readFully(buffer);
		return codec.decode(buffer);
	}

	private void internalWriteOffset(RandomAccessFile randomIndexFile, long index, long offset)
		throws IOException
	{
		long offsetOffset = DATA_OFFSET_SIZE * index;
		if(randomIndexFile.length() < offsetOffset)
		{
			throw new IOException("Invalid offsetOffset " + offsetOffset + "!");
		}
		randomIndexFile.seek(offsetOffset);
		randomIndexFile.writeLong(offset);
	}

	private int internalWriteElement(RandomAccessFile randomSerializeFile, long offset, E element)
		throws IOException
	{
		if(codec == null)
		{
			throw new IllegalStateException("Codec has not been initialized!");
		}
		byte[] buffer = codec.encode(element);

		int bufferSize = buffer.length;

		randomSerializeFile.seek(offset);
		randomSerializeFile.writeInt(bufferSize);
		randomSerializeFile.write(buffer);
		return bufferSize;
	}

	private int internalReadElementSize(RandomAccessFile randomDataFile, long offset)
		throws IOException
	{
		long size = randomDataFile.length();
		if(size >= offset + DATA_LENGTH_SIZE)
		{
			randomDataFile.seek(offset);
			return randomDataFile.readInt();
		}
		return -1;
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
		result.append("CodecFileBuffer[");

		result.append("fileHeader=");
		result.append(fileHeader);
		result.append(", ");

		result.append("preferredMetaData=").append(preferredMetaData);
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

		result.append("codec=").append(codec);

		result.append("]");
		return result.toString();
	}
}
