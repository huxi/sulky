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
		super(new SerializableSerializer<E>(compressing), new SerializableDeserializer<E>(compressing));
	}

	public void setCompressing(boolean compressing)
	{
		{
			Serializer<E> s = getSerializer();
			if(s instanceof SerializableSerializer)
			{
				SerializableSerializer ss= (SerializableSerializer) s;
				ss.setCompressing(compressing);
			}
		}
		{
			Deserializer<E> d = getDeserializer();
			if(d instanceof SerializableDeserializer)
			{
				SerializableDeserializer sd= (SerializableDeserializer) d;
				sd.setCompressing(compressing);
			}
		}
	}
}
