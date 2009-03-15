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

import de.huxhorn.sulky.codec.Codec;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;

public class CodecFileHeader
{
	public static final int MAGIC_VALUE_SIZE = 4;
	public static final int META_LENGTH_SIZE = 4;

	private int magicValue;
	private Map<String, String> preferredMetaData;
	private MetaData metaData;
	private File dataFile;
	private Codec<MetaData> metaCodec;
	private long initialDataOffset;

	public CodecFileHeader(File dataFile, int magicValue, Map<String, String> preferredMetaData)
	{
		this.dataFile=dataFile;
		this.magicValue=magicValue;
		this.preferredMetaData=preferredMetaData;
		this.metaCodec=new MetaDataCodec();
		this.initialDataOffset=-1;
		this.metaData=null;
	}

	public int getMagicValue()
	{
		return magicValue;
	}

	public MetaData getMetaData()
	{
		return metaData;
	}

	/**
	 * Locking is already performed for this method.
	 *
	 * @throws IllegalArgumentException if metaData could not be read.
	 */
	protected void initializeMetaData()
	{
		int offset = MAGIC_VALUE_SIZE;

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
						throw new IllegalArgumentException("Invalid length (" + metaLength + ") at offset: " + offset + "!");
					}
					setInitialDataOffset(offset + META_LENGTH_SIZE + metaLength);
					raf.seek(offset + META_LENGTH_SIZE);
					byte[] buffer = new byte[metaLength];
					raf.readFully(buffer);

					metaData = metaCodec.decode(buffer);
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

	public long getInitialDataOffset()
	{
		return initialDataOffset;
	}

	/**
	 * Locking is already performed for this method.
	 *
	 * @throws IllegalArgumentException if magicValue could not be read or differs from the specified value of this buffer.
	 */
	protected void validateMagicValue()
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

	/**
	 * Locking is already performed for this method.
	 *
	 * @throws IllegalArgumentException if magicValue could not be written.
	 */
	protected void writeMagicValue()
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

	/**
	 * Locking is already performed for this method.
	 *
	 * @throws IllegalArgumentException if metaData could not be written.
	 */
	protected void writeMetaData()
	{
		int offset = MAGIC_VALUE_SIZE;

		RandomAccessFile raf = null;
		try
		{
			byte[] buffer = null;
			int length = 0;
			MetaData actualMetaData = null;
			if(preferredMetaData != null && preferredMetaData.size()>0)
			{
				actualMetaData=new MetaData(preferredMetaData);
				buffer = metaCodec.encode(actualMetaData);
				if(buffer != null)
				{
					length = buffer.length;
				}
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
			metaData = actualMetaData;
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

	public String toString()
	{
		StringBuilder result=new StringBuilder();
		result.append("CodecFileHeader[");
		result.append("magicValue=");
		result.append("0x");
		String hexValue = Integer.toHexString(magicValue);
		int zeroes = 8 - hexValue.length();
		for(int i = 0; i < zeroes; i++)
		{
			result.append("0");
		}
		result.append(hexValue);
		result.append(", ");
		result.append("metaData=").append(metaData);
		result.append("]");

		return result.toString();

	}
}
