package de.huxhorn.sulky.codec.filebuffer;

import de.huxhorn.sulky.buffers.BasicBufferIterator;
import de.huxhorn.sulky.buffers.Buffer;
import de.huxhorn.sulky.codec.Codec;
import de.huxhorn.sulky.io.IOUtilities;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.RandomAccessFile;
import java.util.Iterator;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

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
	 * @param index must be in the range <tt>[0..(getSize()-1)]</tt>.
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
			IOUtilities.interruptIfNecessary(throwable);
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
			IOUtilities.interruptIfNecessary(throwable);
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
			IOUtilities.closeQuietly(randomAccessIndexFile);
			randomAccessIndexFile = null;
			IOUtilities.closeQuietly(randomAccessDataFile);
			randomAccessDataFile = null;
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
