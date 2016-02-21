/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2011 Joern Huxhorn
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
 * Copyright 2007-2011 Joern Huxhorn
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

package de.huxhorn.sulky.codec;

public class DelegatingCodecBase<E>
	implements Codec<E>
{
	private Encoder<E> encoder;
	private Decoder<E> decoder;

	protected DelegatingCodecBase()
	{
		this(null, null);
	}

	protected DelegatingCodecBase(Encoder<E> encoder, Decoder<E> decoder)
	{
		this.encoder = encoder;
		this.decoder = decoder;
	}

	protected Encoder<E> getEncoder()
	{
		return encoder;
	}

	protected void setEncoder(Encoder<E> encoder)
	{
		this.encoder = encoder;
	}

	protected Decoder<E> getDecoder()
	{
		return decoder;
	}

	protected void setDecoder(Decoder<E> decoder)
	{
		this.decoder = decoder;
	}

	public byte[] encode(E object)
	{
		if(encoder == null)
		{
			throw new IllegalStateException("encoder must not be null!");
		}
		return encoder.encode(object);
	}

	public E decode(byte[] bytes)
	{
		if(decoder == null)
		{
			throw new IllegalStateException("decoder must not be null!");
		}
		return decoder.decode(bytes);
	}
}
