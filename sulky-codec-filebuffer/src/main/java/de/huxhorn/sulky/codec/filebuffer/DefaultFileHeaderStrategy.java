package de.huxhorn.sulky.codec.filebuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

public class DefaultFileHeaderStrategy
	implements FileHeaderStrategy
{
	private final Logger logger = LoggerFactory.getLogger(DefaultFileHeaderStrategy.class);

	public static final int CODEC_FILE_HEADER_MAGIC_VALUE = 0x0B501E7E;
	public static final int MAGIC_VALUE_SIZE = 8;
	public static final int META_LENGTH_SIZE = 4;

	private MetaDataCodec metaCodec;

	public DefaultFileHeaderStrategy()
	{
		this.metaCodec = new MetaDataCodec();
	}

	public Integer readMagicValue(File dataFile)
		throws IOException
	{
		RandomAccessFile raf = null;
		Integer result = null;
		try
		{
			raf = new RandomAccessFile(dataFile, "r");
			long fileLength = raf.length();
			if(fileLength >= MAGIC_VALUE_SIZE)
			{
				raf.seek(0);
				int codecMagic = raf.readInt();
				if(codecMagic == CODEC_FILE_HEADER_MAGIC_VALUE)
				{
					result = raf.readInt();
				}
				else
				{
					if(logger.isWarnEnabled()) logger.warn("Couldn't read magic value because codecMagic was 0x{} instead of 0x{}!"
							, Integer.toHexString(codecMagic), Integer.toHexString(CODEC_FILE_HEADER_MAGIC_VALUE));
				}
			}
			else
			{
				if(logger.isWarnEnabled()) logger.warn("Couldn't read magic value because file size is {}!", fileLength);
			}
		}
		finally
		{
			closeQuietly(raf);
		}
		return result;
	}

	public FileHeader writeFileHeader(File dataFile, int magicValue, Map<String, String> metaData, boolean sparse)
		throws IOException
	{
		RandomAccessFile raf = null;
		FileHeader result = null;
		if(dataFile.isFile() && dataFile.length() > 0)
		{
			throw new IllegalArgumentException("File '" + dataFile.getAbsolutePath() + "' already exists and has a size of " + dataFile.length() + ".");
		}
		try
		{
			raf = new RandomAccessFile(dataFile, "rw");
			raf.seek(0);
			raf.writeInt(CODEC_FILE_HEADER_MAGIC_VALUE);
			raf.writeInt(magicValue);
			byte[] buffer = null;
			int length = 0;
			MetaData resultMetaData = new MetaData(metaData, sparse);

			if((metaData != null && metaData.size() > 0) || sparse)
			{
				buffer = metaCodec.encode(resultMetaData);
				if(buffer != null)
				{
					length = buffer.length;
				}
			}

			raf.writeInt(length);
			if(length > 0)
			{
				raf.write(buffer);
			}
			raf.close();
			raf = null;
			result = new FileHeader(magicValue, resultMetaData, MAGIC_VALUE_SIZE + META_LENGTH_SIZE + length);

		}
		finally
		{
			closeQuietly(raf);
		}
		return result;
	}

	public FileHeader readFileHeader(File dataFile)
		throws IOException
	{
		RandomAccessFile raf = null;
		FileHeader result = null;
		try
		{
			raf = new RandomAccessFile(dataFile, "r");
			if(raf.length() >= MAGIC_VALUE_SIZE)
			{
				raf.seek(0);
				int codecMagic = raf.readInt();
				if(codecMagic == CODEC_FILE_HEADER_MAGIC_VALUE)
				{
					int magicValue = raf.readInt();
					int offset = MAGIC_VALUE_SIZE;
					raf.seek(offset);
					int metaLength = raf.readInt();
					if(metaLength > 0)
					{
						if(raf.length() < offset + META_LENGTH_SIZE + metaLength)
						{
							throw new IllegalArgumentException("Invalid length (" + metaLength + ") at offset: " + offset + "!");
						}
						raf.seek(offset + META_LENGTH_SIZE);
						byte[] buffer = new byte[metaLength];
						raf.readFully(buffer);

						result = new FileHeader(magicValue, metaCodec.decode(buffer), offset + META_LENGTH_SIZE + metaLength);
					}
					else
					{
						result = new FileHeader(magicValue, new MetaData(false), offset + META_LENGTH_SIZE);
					}
				}
			}
		}
		finally
		{
			closeQuietly(raf);
		}
		return result;
	}

	public int getMinimalSize()
	{
		return MAGIC_VALUE_SIZE + META_LENGTH_SIZE;
	}

	private static void closeQuietly(RandomAccessFile raf)
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

}
