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

package de.huxhorn.sulky.plist;

import de.huxhorn.sulky.codec.Encoder;
import de.huxhorn.sulky.codec.streaming.StreamingEncoder;
import de.huxhorn.sulky.plist.impl.PropertyListWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This class writes a PropertyList into either an OutputStream or byte[].
 *
 * <ul>
 * <li>java.lang.String, java.lang.Character, char[] and anything not listed explicitly below is stored as a 'string'.</li>
 * <li>java.lang.Boolean is stored as either 'true' or 'false'.</li>
 * <li>java.lang.Byte, java.lang.Short, java.lang.Integer and java.lang.Long are stored as 'integer'.</li>
 * <li>java.lang.Float and java.lang.Double are stored as 'real'.</li>
 * <li>byte[] is stored as 'data'.</li>
 * <li>java.util.Date is stored as 'date'.</li>
 * <li>java.util.Map is stored as a 'dict'. The keys are converted to java.lang.String if necessary, the values are stored according to the rules above.</li>
 * <li>Any other java.util.Collection and java.lang.Object[] is stored as an 'array' with values stored according to the rules above.</li>
 * <li>short[], int[], long[], float[], double[] and boolean[] are stored as an 'array' containing elements according to their respective type as defined above.</li>
 * </ul>
 */
public class PropertyListEncoder
	implements StreamingEncoder<PropertyList>, Encoder<PropertyList>
{
	private final Logger logger = LoggerFactory.getLogger(PropertyListEncoder.class);

	public void encode(PropertyList obj, OutputStream into)
		throws IOException
	{
		PropertyListWriter propertyListWriter =new PropertyListWriter();
		XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

		try
		{
			XMLStreamWriter writer = outputFactory.createXMLStreamWriter(into, "UTF-8");
			propertyListWriter.write(writer, obj, true);
			writer.close();
		}
		catch(XMLStreamException e)
		{
			throw new IOException("Exception while writing XML!", e);
		}
	}

	public byte[] encode(PropertyList object)
	{
		ByteArrayOutputStream into=new ByteArrayOutputStream();
		try
		{
			encode(object, into);
		}
		catch(IOException e)
		{
			if(logger.isWarnEnabled()) logger.warn("Exception while encoding property list!", e);
			return null;
		}
		return into.toByteArray();
	}
}
