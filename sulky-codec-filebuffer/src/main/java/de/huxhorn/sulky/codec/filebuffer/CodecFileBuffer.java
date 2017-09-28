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

import de.huxhorn.sulky.buffers.BasicBufferIterator;
import de.huxhorn.sulky.buffers.Dispose;
import de.huxhorn.sulky.buffers.DisposeOperation;
import de.huxhorn.sulky.buffers.ElementProcessor;
import de.huxhorn.sulky.buffers.FileBuffer;
import de.huxhorn.sulky.buffers.Reset;
import de.huxhorn.sulky.buffers.SetOperation;
import de.huxhorn.sulky.codec.Codec;
import de.huxhorn.sulky.io.IOUtilities;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * In contrast to SerializingFileBuffer, this implementation supports the following:
 *
 * <ul>
 * <li>An optional magic value to identify the type of a buffer file.
 * If present (and it should be), it is contained in the first four bytes of the data-file
 * and can be evaluated by external classes, e.g. FileFilters.
 * An application would use one (or more) specific magic value to identify it's own files.
 * </li>
 * <li>Configurable Codec so the way the elements are actually written and read can be changed as needed.
 * </li>
 * <li>
 * Optional meta data that can be used to provide additional information about the content of the buffer.
 * It might be used to identify the correct Codec required by the buffer
 * </li>
 * <li>Optional ElementProcessors that are executed after elements are added to the buffer.</li>
 * </ul>
 *
 * TODO: more documentation :p
 *
 * @param <E> the type of objects that are stored in this buffer.
 */
public class CodecFileBuffer<E>
	implements FileBuffer<E>, SetOperation<E>, DisposeOperation
{
	private final Logger logger = LoggerFactory.getLogger(CodecFileBuffer.class);

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
	private Map<String, String> preferredMetaData;

	private Codec<E> codec;
	private List<ElementProcessor<E>> elementProcessors;
	private FileHeaderStrategy fileHeaderStrategy;
	private int magicValue;
	private FileHeader fileHeader;
	private boolean preferredSparse;
	private DataStrategy<E> dataStrategy;
	private IndexStrategy indexStrategy;

	/**
	 * TODO: add description :p
	 *
	 * @param magicValue        the magic value of the buffer.
	 * @param sparse            whether or not this buffer is sparse, i.e. not continuous.
	 * @param preferredMetaData the meta data of the buffer. Might be null.
	 * @param codec             the codec used by this buffer. Might be null.
	 * @param dataFile          the data file.
	 * @param indexFile         the index file of the buffer.
	 */
	public CodecFileBuffer(int magicValue, boolean sparse, Map<String, String> preferredMetaData, Codec<E> codec, File dataFile, File indexFile)
	{
		this(magicValue, sparse, preferredMetaData, codec, dataFile, indexFile, new DefaultFileHeaderStrategy());
	}

	public CodecFileBuffer(int magicValue, boolean preferredSparse, Map<String, String> preferredMetaData, Codec<E> codec, File dataFile, File indexFile, FileHeaderStrategy fileHeaderStrategy)
	{
		this.indexStrategy = new DefaultIndexStrategy();
		this.magicValue = magicValue;
		this.fileHeaderStrategy = fileHeaderStrategy;
		this.readWriteLock = new ReentrantReadWriteLock(true);
		this.preferredSparse = preferredSparse;
		if(preferredMetaData != null)
		{
			preferredMetaData = new HashMap<>(preferredMetaData);
		}
		if(preferredMetaData != null)
		{
			this.preferredMetaData = new HashMap<>(preferredMetaData);
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
		try
		{
			this.fileHeader = null;
			FileHeader header = fileHeaderStrategy.readFileHeader(dataFile);
			if(header == null)
			{
				throw new IllegalArgumentException("Could not read file header from file '" + dataFile.getAbsolutePath() + "'. File isn't compatible.");
			}
			if(header.getMagicValue() != magicValue)
			{
				throw new IllegalArgumentException("Wrong magic value. Expected 0x" + Integer.toHexString(magicValue) + " but was " + Integer.toHexString(header.getMagicValue()) + "!");
			}
			if(dataFile.length() > header.getDataOffset() && !indexFile.exists())
			{
				throw new IllegalArgumentException("dataFile contains data but indexFile " + indexFile.getAbsolutePath() + " is not valid!");
			}
			setFileHeader(header);
		}
		catch(IOException ex)
		{
			IOUtilities.interruptIfNecessary(ex);
			throw new IllegalArgumentException("Could not read magic value from file '" + dataFile.getAbsolutePath() + "'!", ex);
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
				elementProcessors = new ArrayList<>(elementProcessors);
			}
		}
		this.elementProcessors = elementProcessors;
	}

	private boolean initFilesIfNecessary()
	{
		if(!dataFile.exists() || dataFile.length() < fileHeaderStrategy.getMinimalSize())
		{
			Throwable t=null;
			boolean dataDeleted=false;
			boolean indexDeleted=false;
			Lock lock = readWriteLock.writeLock();
			lock.lock();
			try
			{
				dataDeleted=dataFile.delete();
				setFileHeader(fileHeaderStrategy.writeFileHeader(dataFile, magicValue, preferredMetaData, preferredSparse));
				indexDeleted=indexFile.delete();
			}
			catch(IOException e)
			{
				t=e;
			}
			finally
			{
				lock.unlock();
			}
			if(!indexDeleted)
			{
				if(logger.isDebugEnabled()) logger.debug("Couldn't delete index file {}.", indexFile.getAbsolutePath()); // NOPMD
			}
			if(!dataDeleted)
			{
				if(logger.isDebugEnabled()) logger.debug("Couldn't delete data file {}.", dataFile.getAbsolutePath()); // NOPMD
			}
			if(t!=null)
			{
				if(logger.isWarnEnabled()) logger.warn("Exception while initializing files!", t);
				IOUtilities.interruptIfNecessary(t);
				return false;
			}
			return true;
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

	public File getIndexFile()
	{
		return indexFile;
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
			return indexStrategy.getSize(raf);
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
			IOUtilities.interruptIfNecessary(throwable);
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
		Lock lock = readWriteLock.readLock();
		lock.lock();
		Throwable throwable = null;
		try
		{
			if(!dataFile.canRead() || !indexFile.canRead())
			{
				return null;
			}
			randomSerializeIndexFile = new RandomAccessFile(indexFile, "r");
			randomSerializeFile = new RandomAccessFile(dataFile, "r");

			return dataStrategy.get(index, randomSerializeIndexFile, randomSerializeFile, codec, indexStrategy);
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
		return null;
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

			dataStrategy.add(element, randomIndexFile, randomDataFile, codec, indexStrategy);
			// call processors if available
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
			IOUtilities.closeQuietly(randomDataFile);
			IOUtilities.closeQuietly(randomIndexFile);
			lock.unlock();
		}
		if(throwable != null)
		{
			// it's a really bad idea to log while locked *sigh*
			if(logger.isWarnEnabled()) logger.warn("Couldn't write element!", throwable);
			IOUtilities.interruptIfNecessary(throwable);
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

					dataStrategy.addAll(elements, randomIndexFile, randomDataFile, codec, indexStrategy);

					// call processors if available
					if(elementProcessors != null)
					{
						for(ElementProcessor<E> current : elementProcessors)
						{
							current.processElements(elements);
						}
					}
				}
				catch(Throwable e)
				{
					throwable = e;
				}
				finally
				{
					IOUtilities.closeQuietly(randomDataFile);
					IOUtilities.closeQuietly(randomIndexFile);
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
		Throwable t=null;
		boolean indexDeleted=false;
		boolean dataDeleted=false;
		Lock lock = readWriteLock.writeLock();
		lock.lock();
		try
		{
			indexDeleted=indexFile.delete();
			dataDeleted=dataFile.delete();
			fileHeaderStrategy.writeFileHeader(dataFile, magicValue, preferredMetaData, preferredSparse);
			if(elementProcessors != null)
			{
				for(ElementProcessor<E> current : elementProcessors)
				{
					Reset.reset(current);
				}
			}
		}
		catch(IOException e)
		{
			t=e;
		}
		finally
		{
			lock.unlock();
		}
		if(!indexDeleted)
		{
			if(logger.isDebugEnabled()) logger.debug("Couldn't delete index file {}.", indexFile.getAbsolutePath()); // NOPMD
		}
		if(!dataDeleted)
		{
			if(logger.isDebugEnabled()) logger.debug("Couldn't delete data file {}.", dataFile.getAbsolutePath()); // NOPMD
		}
		if(t != null)
		{
			if(logger.isWarnEnabled()) logger.warn("Exception while resetting file!", t);
			IOUtilities.interruptIfNecessary(t);
		}
	}

	/**
	 * @return will always return false, i.e. it does not check for disk space!
	 */
	public boolean isFull()
	{
		return false;
	}

	public Iterator<E> iterator()
	{
		return new BasicBufferIterator<>(this);
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
				if(logger.isDebugEnabled()) logger.debug("Created directory {}.", parent.getAbsolutePath()); // NOPMD
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

	public void dispose()
	{

		if(elementProcessors != null)
		{
			for(ElementProcessor current : elementProcessors)
			{
				Dispose.dispose(current);
			}
		}
		// TODO: implement dispose()
	}

	public boolean isDisposed()
	{
		return false;  // TODO: implement isDisposed()
	}

	private void setFileHeader(FileHeader fileHeader)
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

	public boolean set(long index, E element)
	{
		initFilesIfNecessary();
		RandomAccessFile randomIndexFile = null;
		RandomAccessFile randomDataFile = null;
		Lock lock = readWriteLock.writeLock();
		lock.lock();
		Throwable throwable = null;
		boolean result = false;
		try
		{
			randomIndexFile = new RandomAccessFile(indexFile, "rw");
			randomDataFile = new RandomAccessFile(dataFile, "rw");

			result = dataStrategy.set(index, element, randomIndexFile, randomDataFile, codec, indexStrategy);
			// call processors if available
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
			IOUtilities.closeQuietly(randomDataFile);
			IOUtilities.closeQuietly(randomIndexFile);
			lock.unlock();
		}
		if(throwable != null)
		{
			// it's a really bad idea to log while locked *sigh*
			if(logger.isWarnEnabled()) logger.warn("Couldn't write element!", throwable);
			IOUtilities.interruptIfNecessary(throwable);
		}
		return result;
	}

	public boolean isSetSupported()
	{
		return dataStrategy != null && dataStrategy.isSetSupported();
	}
}
