/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2010 Joern Huxhorn
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
package de.huxhorn.sulky.codec.streaming;

import de.huxhorn.sulky.codec.Decoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.IOException;

public class DecoderBridge<E>
	implements Decoder<E>
{
	private final Logger logger = LoggerFactory.getLogger(DecoderBridge.class);

	private StreamingDecoder<E> wrapped;

	public DecoderBridge(StreamingDecoder<E> wrapped)
	{
		this.wrapped = wrapped;
	}

	public E decode(byte[] bytes)
	{
		ByteArrayInputStream bis=new ByteArrayInputStream(bytes);
		try
		{
			return wrapped.decode(bis);
		}
		catch (IOException e)
		{
			if(logger.isWarnEnabled()) logger.warn("Couldn't decode!", e);
		}
		return null;
	}
}
