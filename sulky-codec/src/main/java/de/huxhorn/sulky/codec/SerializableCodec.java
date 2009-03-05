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

import java.io.Serializable;

public class SerializableCodec<E extends Serializable>
	extends DelegatingCodecBase<E>
{
	public SerializableCodec()
	{
		this(false);
	}

	public SerializableCodec(boolean compressing)
	{
		super(new SerializableEncoder<E>(compressing), new SerializableDecoder<E>(compressing));
	}

	public void setCompressing(boolean compressing)
	{
		{
			Encoder<E> s = getEncoder();
			if(s instanceof SerializableEncoder)
			{
				SerializableEncoder ss= (SerializableEncoder) s;
				ss.setCompressing(compressing);
			}
		}
		{
			Decoder<E> d = getDecoder();
			if(d instanceof SerializableDecoder)
			{
				SerializableDecoder sd= (SerializableDecoder) d;
				sd.setCompressing(compressing);
			}
		}
	}
}
