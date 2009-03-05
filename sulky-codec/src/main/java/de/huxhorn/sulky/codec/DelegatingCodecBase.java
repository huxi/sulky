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
