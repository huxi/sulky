/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2017 Joern Huxhorn
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
 * Copyright 2007-2017 Joern Huxhorn
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

package de.huxhorn.sulky.plist.impl;

import de.huxhorn.sulky.plist.PropertyList;
import de.huxhorn.sulky.stax.DateTimeFormatter;
import de.huxhorn.sulky.stax.GenericStreamWriter;
import de.huxhorn.sulky.stax.StaxUtilities;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropertyListWriter
	implements GenericStreamWriter<PropertyList>, PropertyListConstants
{
	private final Logger logger = LoggerFactory.getLogger(PropertyListWriter.class);

	private final DateTimeFormatter staxDateTimeFormatter = new DateTimeFormatter();

	public void write(XMLStreamWriter writer, PropertyList object, boolean isRoot) throws XMLStreamException
	{
		if(isRoot)
		{
			writer.writeStartDocument(StandardCharsets.UTF_8.toString(), "1.0");
			writer.writeCharacters("\n");
			writer.writeDTD("<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">");
			writer.writeCharacters("\n");
		}
		StaxUtilities.writeStartElement(writer, null, null, PLIST_NODE);
		StaxUtilities.writeAttribute(writer, false, null, null, PLIST_VERSION_ATTRIBUTE, PLIST_VERSION);

		if(object != null)
		{
			writeValue(writer, object.getRoot());
		}

		writer.writeEndElement();
		if(isRoot)
		{
			writer.writeEndDocument();
		}
	}

	private void writeValue(XMLStreamWriter writer, Object value)
		throws XMLStreamException
	{
		if(value == null)
		{
			return;
		}
		if(value instanceof String)
		{
			StaxUtilities.writeSimpleTextNode(writer, null, null, STRING_NODE, (String) value);
			return;
		}
		if(value instanceof Boolean)
		{
			Boolean bool= (Boolean) value;
			if(bool)
			{
				StaxUtilities.writeEmptyElement(writer, null, null, TRUE_NODE);
			}
			else
			{
				StaxUtilities.writeEmptyElement(writer, null, null, FALSE_NODE);
			}
			return;
		}

		if(value instanceof Character)
		{
			StaxUtilities.writeSimpleTextNode(writer, null, null, STRING_NODE, value.toString());
			return;
		}

		if(value instanceof Byte || value instanceof Short || value instanceof Integer || value instanceof Long)
		{
			Number number = (Number) value;
			StaxUtilities.writeSimpleTextNode(writer, null, null, INTEGER_NODE, Long.toString(number.longValue()));
			return;
		}

		if(value instanceof Float || value instanceof Double)
		{
			Number number = (Number) value;
			StaxUtilities.writeSimpleTextNode(writer, null, null, REAL_NODE, Double.toString(number.doubleValue()));
			return;
		}

		if(value instanceof byte[])
		{
			byte[] data = (byte[]) value;
			writeData(writer, data);
			return;
		}

		if(value instanceof Date)
		{
			Date date = (Date) value;
			writeDate(writer, date);
			return;
		}

		if(value instanceof Map)
		{
			Map map = (Map) value;
			writeDict(writer, map);
			return;
		}

		if(value instanceof Collection)
		{
			Collection collection = (Collection) value;
			writeArray(writer, collection);
			return;
		}

		Class valueClass = value.getClass();
		if(valueClass.isArray())
		{
			List<?> list;
			// we handled byte[] already
			if(value instanceof short[])
			{
				short[] array = (short[]) value;
				ArrayList<Short> arrayList = new ArrayList<>(array.length);
				for(short v:array)
				{
					arrayList.add(v);
				}
				list=arrayList;
			}
			else if(valueClass == int[].class)
			{
				int[] array = (int[]) value;
				ArrayList<Integer> arrayList = new ArrayList<>(array.length);
				for(int v:array)
				{
					arrayList.add(v);
				}
				list=arrayList;
			}
			else if(valueClass == long[].class)
			{
				long[] array = (long[]) value;
				ArrayList<Long> arrayList = new ArrayList<>(array.length);
				for(long v:array)
				{
					arrayList.add(v);
				}
				list=arrayList;
			}
			else if(valueClass == float[].class)
			{
				float[] array = (float[]) value;
				ArrayList<Float> arrayList = new ArrayList<>(array.length);
				for(float v:array)
				{
					arrayList.add(v);
				}
				list=arrayList;
			}
			else if(valueClass == double[].class)
			{
				double[] array = (double[]) value;
				ArrayList<Double> arrayList = new ArrayList<>(array.length);
				for(double v:array)
				{
					arrayList.add(v);
				}
				list=arrayList;
			}
			else if(valueClass == boolean[].class)
			{
				boolean[] array = (boolean[]) value;
				ArrayList<Boolean> arrayList = new ArrayList<>(array.length);
				for(boolean v:array)
				{
					arrayList.add(v);
				}
				list=arrayList;
			}
			else if(valueClass == char[].class)
			{
				char[] array = (char[]) value;
				String s=new String(array);
				StaxUtilities.writeSimpleTextNode(writer, null, null, STRING_NODE, s);
				return;
			}
			else
			{
				list=Arrays.asList((Object[])value);
				// special handling of container Object[]
			}
			writeArray(writer, list);
			return;
		}

		if(logger.isDebugEnabled()) logger.debug("No suitable conversion found for {}. Will save it as string.", valueClass.getName());
		StaxUtilities.writeSimpleTextNode(writer, null, null, STRING_NODE, value.toString());
	}

	private void writeArray(XMLStreamWriter writer, Collection collection)
		throws XMLStreamException
	{
		StaxUtilities.writeStartElement(writer, null, null, ARRAY_NODE);

		for(Object o:collection)
		{
			writeValue(writer, o);
		}

		writer.writeEndElement();
	}

	private void writeDict(XMLStreamWriter writer, Map<?,?> map)
		throws XMLStreamException
	{
		StaxUtilities.writeStartElement(writer, null, null, DICT_NODE);

		for(Map.Entry current:map.entrySet())
		{
			Object key=current.getKey();
			if(key != null)
			{
				StaxUtilities.writeSimpleTextNode(writer, null, null, KEY_NODE, key.toString());

				writeValue(writer, current.getValue());
			}
		}

		writer.writeEndElement();
	}

	private void writeDate(XMLStreamWriter writer, Date date)
		throws XMLStreamException
	{
		String formatted = staxDateTimeFormatter.format(date, false);
		StaxUtilities.writeSimpleTextNode(writer, null, null, DATE_NODE, formatted);
	}

	private void writeData(XMLStreamWriter writer, byte[] data)
		throws XMLStreamException
	{
		StaxUtilities.writeSimpleTextNode(writer, null, null, DATA_NODE, Base64.encodeBase64URLSafeString(data));
	}
}
