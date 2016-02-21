/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2015 Joern Huxhorn
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
 * Copyright 2007-2015 Joern Huxhorn
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
import de.huxhorn.sulky.stax.GenericStreamReader;
import de.huxhorn.sulky.stax.StaxUtilities;
import org.apache.commons.codec.binary.Base64;

import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PropertyListReader
	implements GenericStreamReader<PropertyList>, PropertyListConstants
{
	private DateTimeFormatter staxDateTimeFormatter = new DateTimeFormatter();

	public PropertyList read(XMLStreamReader reader)
		throws XMLStreamException
	{
		int type = reader.getEventType();

		if(XMLStreamConstants.START_DOCUMENT == type)
		{
			do
			{
				reader.next();
				type = reader.getEventType();
			}
			while(XMLStreamConstants.START_ELEMENT != type);
		}
		PropertyList result=new PropertyList();
		if(XMLStreamConstants.START_ELEMENT == type && PLIST_NODE.equals(reader.getLocalName()))
		{
			reader.nextTag();
			type = reader.getEventType();
			if(!(XMLStreamConstants.END_ELEMENT == type && PLIST_NODE.equals(reader.getLocalName())))
			{
				result.setRoot(readValue(reader));
			}
			reader.require(XMLStreamConstants.END_ELEMENT, null, PLIST_NODE);
		}
		return result;
	}

	private Object readValue(XMLStreamReader reader)
		throws XMLStreamException
	{
		int type = reader.getEventType();
		if(XMLStreamConstants.START_ELEMENT == type && TRUE_NODE.equals(reader.getLocalName()))
		{
			reader.nextTag();
			reader.require(XMLStreamConstants.END_ELEMENT, null, TRUE_NODE);
			reader.nextTag();
			return Boolean.TRUE;
		}
		if(XMLStreamConstants.START_ELEMENT == type && FALSE_NODE.equals(reader.getLocalName()))
		{
			reader.nextTag();
			reader.require(XMLStreamConstants.END_ELEMENT, null, FALSE_NODE);
			reader.nextTag();
			return Boolean.FALSE;
		}
		if(XMLStreamConstants.START_ELEMENT == type && REAL_NODE.equals(reader.getLocalName()))
		{
			String text = StaxUtilities.readSimpleTextNodeIfAvailable(reader, null, REAL_NODE);

			return Double.parseDouble(text);
		}
		if(XMLStreamConstants.START_ELEMENT == type && INTEGER_NODE.equals(reader.getLocalName()))
		{
			String text = StaxUtilities.readSimpleTextNodeIfAvailable(reader, null, INTEGER_NODE);

			return Long.parseLong(text);
		}
		if(XMLStreamConstants.START_ELEMENT == type && STRING_NODE.equals(reader.getLocalName()))
		{
			return StaxUtilities.readSimpleTextNodeIfAvailable(reader, null, STRING_NODE);
		}
		if(XMLStreamConstants.START_ELEMENT == type && DATA_NODE.equals(reader.getLocalName()))
		{
			return readData(reader);
		}
		if(XMLStreamConstants.START_ELEMENT == type && DATE_NODE.equals(reader.getLocalName()))
		{
			return readDate(reader);
		}
		if(XMLStreamConstants.START_ELEMENT == type && ARRAY_NODE.equals(reader.getLocalName()))
		{
			return readArray(reader);
		}
		if(XMLStreamConstants.START_ELEMENT == type && DICT_NODE.equals(reader.getLocalName()))
		{
			return readDict(reader);
		}
		throw new RuntimeException("Unexpected XML-Node: "+reader.getLocalName());
	}

	private Object readDate(XMLStreamReader reader)
		throws XMLStreamException
	{
		reader.require(XMLStreamConstants.START_ELEMENT, null, DATE_NODE);
		String text = StaxUtilities.readSimpleTextNodeIfAvailable(reader, null, DATE_NODE);
		try
		{
			return staxDateTimeFormatter.parse(text);
		}
		catch(ParseException e)
		{
			throw new XMLStreamException("Invalid date: '"+text+"'", e);
		}
	}

	private byte[] readData(XMLStreamReader reader)
		throws XMLStreamException
	{
		reader.require(XMLStreamConstants.START_ELEMENT, null, DATA_NODE);
		String text = StaxUtilities.readSimpleTextNodeIfAvailable(reader, null, DATA_NODE);
		return Base64.decodeBase64(text);
	}

	private Map<String, ?> readDict(XMLStreamReader reader)
		throws XMLStreamException
	{
		reader.require(XMLStreamConstants.START_ELEMENT, null, DICT_NODE);
		reader.nextTag();
		Map<String, Object> map = new HashMap<>();
		for(; ;)
		{
			int type = reader.getEventType();
			if(XMLStreamConstants.END_ELEMENT == type && DICT_NODE.equals(reader.getLocalName()))
			{
				reader.nextTag();
				break;
			}
			String key=StaxUtilities.readSimpleTextNodeIfAvailable(reader, null, KEY_NODE);
			if(key != null)
			{
				map.put(key, readValue(reader));
			}
		}
		return map;
	}

	private List<?> readArray(XMLStreamReader reader)
		throws XMLStreamException
	{
		reader.require(XMLStreamConstants.START_ELEMENT, null, ARRAY_NODE);
		reader.nextTag();
		List<Object> array = new ArrayList<>();
		for(; ;)
		{
			int type = reader.getEventType();
			if(XMLStreamConstants.END_ELEMENT == type && ARRAY_NODE.equals(reader.getLocalName()))
			{
				reader.nextTag();
				break;
			}
			array.add(readValue(reader));
		}
		return array;
	}
}
