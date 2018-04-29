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

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultFileHeaderStrategy
	implements FileHeaderStrategy
{
	private final Logger logger = LoggerFactory.getLogger(DefaultFileHeaderStrategy.class);

	public static final int CODEC_FILE_HEADER_MAGIC_VALUE = 0x0B501E7E;
	public static final int MAGIC_VALUE_SIZE = 8;
	public static final int META_LENGTH_SIZE = 4;

	private final MetaDataCodec metaCodec;

	public DefaultFileHeaderStrategy()
	{
		this.metaCodec = new MetaDataCodec();
	}

	@Override
	public Integer readMagicValue(File dataFile)
		throws IOException
	{
		try(RandomAccessFile raf = new RandomAccessFile(dataFile, "r"))
		{
			long fileLength = raf.length();
			if(fileLength >= MAGIC_VALUE_SIZE)
			{
				raf.seek(0);
				int codecMagic = raf.readInt();
				if(codecMagic == CODEC_FILE_HEADER_MAGIC_VALUE)
				{
					return raf.readInt();
				}
				if(logger.isWarnEnabled()) logger.warn("Couldn't read magic value because codecMagic was 0x{} instead of 0x{}!",
						Integer.toHexString(codecMagic),
						Integer.toHexString(CODEC_FILE_HEADER_MAGIC_VALUE));
			}
			else
			{
				if(logger.isWarnEnabled()) logger.warn("Couldn't read magic value because file size is {}!", fileLength);
			}
		}
		return null;
	}

	@Override
	public FileHeader writeFileHeader(File dataFile, int magicValue, Map<String, String> metaData, boolean sparse)
		throws IOException
	{
		if(dataFile.isFile() && dataFile.length() > 0)
		{
			throw new IllegalArgumentException("File '" + dataFile.getAbsolutePath() + "' already exists and has a size of " + dataFile.length() + ".");
		}

		try(RandomAccessFile raf = new RandomAccessFile(dataFile, "rw"))
		{
			raf.seek(0);
			raf.writeInt(CODEC_FILE_HEADER_MAGIC_VALUE);
			raf.writeInt(magicValue);
			byte[] buffer = null;
			int length = 0;
			MetaData resultMetaData = new MetaData(metaData, sparse);

			if((metaData != null && !metaData.isEmpty()) || sparse)
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
			return new FileHeader(magicValue, resultMetaData, MAGIC_VALUE_SIZE + META_LENGTH_SIZE + length);
		}
	}

	@Override
	public FileHeader readFileHeader(File dataFile)
		throws IOException
	{
		try(RandomAccessFile raf = new RandomAccessFile(dataFile, "r"))
		{
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

						return new FileHeader(magicValue, metaCodec.decode(buffer), offset + META_LENGTH_SIZE + metaLength);
					}
					else
					{
						return new FileHeader(magicValue, new MetaData(false), offset + META_LENGTH_SIZE);
					}
				}
			}
		}
		return null;
	}

	@Override
	public int getMinimalSize()
	{
		return MAGIC_VALUE_SIZE + META_LENGTH_SIZE;
	}

}
