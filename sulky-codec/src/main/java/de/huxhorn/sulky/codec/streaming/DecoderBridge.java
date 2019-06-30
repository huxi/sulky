/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2019 Joern Huxhorn
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
 * Copyright 2007-2019 Joern Huxhorn
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

import de.huxhorn.sulky.codec.Decoder;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DecoderBridge<E>
	implements Decoder<E>
{
	private final Logger logger = LoggerFactory.getLogger(DecoderBridge.class);

	private final StreamingDecoder<E> wrapped;

	public DecoderBridge(StreamingDecoder<E> wrapped)
	{
		this.wrapped = Objects.requireNonNull(wrapped, "wrapped must not be null!");
	}

	@Override
	public E decode(byte[] bytes)
	{
		try(ByteArrayInputStream bis=new ByteArrayInputStream(bytes))
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
