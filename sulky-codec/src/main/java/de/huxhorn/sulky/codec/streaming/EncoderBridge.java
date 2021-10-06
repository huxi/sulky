/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2021 Joern Huxhorn
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
 * Copyright 2007-2021 Joern Huxhorn
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

package de.huxhorn.sulky.codec.streaming;

import de.huxhorn.sulky.codec.Encoder;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EncoderBridge<E>
	implements Encoder<E>
{
	private final Logger logger = LoggerFactory.getLogger(EncoderBridge.class);

	private final StreamingEncoder<E> wrapped;

	public EncoderBridge(StreamingEncoder<E> wrapped)
	{
		this.wrapped = Objects.requireNonNull(wrapped, "wrapped must not be null!");
	}

	@Override
	@SuppressWarnings("PMD.ReturnEmptyCollectionRatherThanNull")
	public byte[] encode(E object)
	{
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		try
		{
			wrapped.encode(object, bos);
			return bos.toByteArray();
		}
		catch (IOException e)
		{
			if(logger.isWarnEnabled()) logger.warn("Exception while encoding {}!", object, e);
		}
		return null;
	}
}
