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
package de.huxhorn.sulky.generics.io;

/**
 *
 * @param <E>
 * @deprecated Use sulky-codec instead.
 */
public class DelegatingCodecBase<E>
	implements Codec<E>
{
	private Serializer<E> serializer;
	private Deserializer<E> deserializer;

	protected DelegatingCodecBase()
	{
	    this(null, null);
	}

	protected DelegatingCodecBase(Serializer<E> serializer, Deserializer<E> deserializer)
	{
		this.serializer = serializer;
		this.deserializer = deserializer;
	}

	protected Serializer<E> getSerializer()
	{
		return serializer;
	}

	protected void setSerializer(Serializer<E> serializer)
	{
		this.serializer = serializer;
	}

	protected Deserializer<E> getDeserializer()
	{
		return deserializer;
	}

	protected void setDeserializer(Deserializer<E> deserializer)
	{
		this.deserializer = deserializer;
	}

	public byte[] serialize(E object)
	{
		return serializer.serialize(object);
	}

	public E deserialize(byte[] bytes)
	{
		return deserializer.deserialize(bytes);
	}
}
