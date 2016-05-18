/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2016 Joern Huxhorn
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
 * Copyright 2007-2016 Joern Huxhorn
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

package de.huxhorn.sulky.plist;

import de.huxhorn.sulky.codec.Decoder;
import de.huxhorn.sulky.codec.streaming.StreamingDecoder;
import de.huxhorn.sulky.plist.impl.PropertyListReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * This class creates a PropertyList from either an InputStream or byte[].
 *
 * <dl>
 * <dt>array</dt>
 * <dd>Results in a List&lt;?&gt; containing any of the values below.</dd>
 * <dt>dict</dt>
 * <dd>Results in a Map&lt;String, ?&gt; containing any of the values below.</dd>
 * <dt>data</dt>
 * <dd>Results in a byte[].</dd>
 * <dt>date</dt>
 * <dd>Results in a java.util.Date.</dd>
 * <dt>real</dt>
 * <dd>Results in a java.lang.Double.</dd>
 * <dt>integer</dt>
 * <dd>Results in a java.lang.Long.</dd>
 * <dt>string</dt>
 * <dd>Results in a java.lang.String.</dd>
 * <dt>true</dt>
 * <dd>Results in java.lang.Boolean.TRUE.</dd>
 * <dt>false</dt>
 * <dd>Results in java.lang.Boolean.FALSE.</dd>
 * </dl>
 */
public class PropertyListDecoder
	implements StreamingDecoder<PropertyList>, Decoder<PropertyList>
{
	private final Logger logger = LoggerFactory.getLogger(PropertyListDecoder.class);

	public PropertyList decode(InputStream from)
		throws IOException
	{
		PropertyListReader propertyListReader = new PropertyListReader();
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		inputFactory.setProperty(XMLInputFactory.IS_SUPPORTING_EXTERNAL_ENTITIES, false);
		inputFactory.setProperty(XMLInputFactory.SUPPORT_DTD, false);
		inputFactory.setProperty(XMLInputFactory.IS_VALIDATING, false);

		try
		{
			XMLStreamReader reader = inputFactory.createXMLStreamReader(new InputStreamReader(from, StandardCharsets.UTF_8));
			PropertyList result = propertyListReader.read(reader);
			reader.close();
			return result;
		}
		catch(XMLStreamException e)
		{
			throw new IOException("Exception while reading XML!", e);
		}
	}

	public PropertyList decode(byte[] bytes)
	{
		try
		{
			return decode(new ByteArrayInputStream(bytes));
		}
		catch(IOException e)
		{
			if(logger.isWarnEnabled()) logger.warn("Exception while decoding property list!", e);
			return null;
		}
	}
}
