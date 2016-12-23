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

package de.huxhorn.sulky.stax;

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IndentingXMLStreamWriter
	implements XMLStreamWriter
{

	private static final String SYSTEM_LINE_SEPARATOR = System.getProperty("line.separator");
	private XMLStreamWriter writer;
	private int indentLevel;
	private boolean wroteText;
	private final String lineSeparator;
	private final String indentString;

	public IndentingXMLStreamWriter(XMLStreamWriter writer)
	{
		this(writer, SYSTEM_LINE_SEPARATOR, "\t");
	}

	public IndentingXMLStreamWriter(XMLStreamWriter writer, String lineSeparator)
	{
		this(writer, lineSeparator, "\t");
	}

	public IndentingXMLStreamWriter(XMLStreamWriter writer, String lineSeparator, String indentString)
	{
		if(writer == null)
		{
			// so we have consistent behaviour in case of debug or not.
			throw new IllegalArgumentException("writer must not be null!");
		}
		if(lineSeparator == null)
		{
			throw new IllegalArgumentException("lineSeparator must not be null!");
		}
		if(indentString == null)
		{
			throw new IllegalArgumentException("indentString must not be null!");
		}

		this.writer = writer;
		this.lineSeparator = lineSeparator;
		this.indentString = indentString;
		final Logger logger = LoggerFactory.getLogger(IndentingXMLStreamWriter.class);
		if(logger.isDebugEnabled()) logger.debug("writer-class: {}", writer.getClass());
		this.indentLevel = 0;
		this.wroteText = false;
	}

	public void writeStartElement(String localName)
		throws XMLStreamException
	{
		writeIndent();
		writer.writeStartElement(localName);
		increaseIndentLevel();
		wroteText = false;
	}

	public void writeStartElement(String namespaceURI, String localName)
		throws XMLStreamException
	{
		writeIndent();
		writer.writeStartElement(namespaceURI, localName);
		increaseIndentLevel();
		wroteText = false;
	}

	public void writeStartElement(String prefix, String localName, String namespaceURI)
		throws XMLStreamException
	{
		writeIndent();
		writer.writeStartElement(prefix, localName, namespaceURI);
		increaseIndentLevel();
		wroteText = false;
	}

	public void writeEmptyElement(String namespaceURI, String localName)
		throws XMLStreamException
	{
		writeIndent();
		writer.writeEmptyElement(namespaceURI, localName);
		wroteText = false;
	}

	public void writeEmptyElement(String prefix, String localName, String namespaceURI)
		throws XMLStreamException
	{
		writeIndent();
		writer.writeEmptyElement(prefix, localName, namespaceURI);
		wroteText = false;
	}

	public void writeEmptyElement(String localName)
		throws XMLStreamException
	{
		writeIndent();
		writer.writeEmptyElement(localName);
		wroteText = false;
	}

	private void writeIndent()
		throws XMLStreamException
	{

		StringBuilder indentStr = new StringBuilder(lineSeparator);
		for(int i = 0; i < indentLevel; i++)
		{
			indentStr.append(indentString);
		}
		writer.writeCharacters(indentStr.toString());
	}

	private void increaseIndentLevel()
	{
		indentLevel++;
	}

	private void decreaseIndentLevel()
	{
		indentLevel--;
		if(indentLevel < 0)
		{
			indentLevel = 0;
		}
	}

	public void writeEndElement()
		throws XMLStreamException
	{
		decreaseIndentLevel();
		if(!wroteText)
		{
			writeIndent();
			wroteText = false;
		}
		writer.writeEndElement();
		wroteText = false;
	}

	public void writeEndDocument()
		throws XMLStreamException
	{
		writer.writeEndDocument();
	}

	public void close()
		throws XMLStreamException
	{
		writer.close();
	}

	public void flush()
		throws XMLStreamException
	{
		writer.flush();
	}

	public void writeAttribute(String localName, String value)
		throws XMLStreamException
	{
		writer.writeAttribute(localName, value);
	}

	public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
		throws XMLStreamException
	{
		writer.writeAttribute(prefix, namespaceURI, localName, value);
	}

	public void writeAttribute(String namespaceURI, String localName, String value)
		throws XMLStreamException
	{
		writer.writeAttribute(namespaceURI, localName, value);
	}

	public void writeNamespace(String prefix, String namespaceURI)
		throws XMLStreamException
	{
		writer.writeNamespace(prefix, namespaceURI);
	}

	public void writeDefaultNamespace(String namespaceURI)
		throws XMLStreamException
	{
		writer.writeDefaultNamespace(namespaceURI);
	}

	public void writeComment(String data)
		throws XMLStreamException
	{
		data = StaxUtilities.normalizeNewlines(data);
		if(data != null)
		{
			data = data.replace("\n", lineSeparator);
		}
		writer.writeComment(data);
	}

	public void writeProcessingInstruction(String target)
		throws XMLStreamException
	{
		writer.writeProcessingInstruction(target);
	}

	public void writeProcessingInstruction(String target, String data)
		throws XMLStreamException
	{
		writer.writeProcessingInstruction(target, data);
	}

	public void writeCData(String data)
		throws XMLStreamException
	{
		data = StaxUtilities.normalizeNewlines(data);
		if(data != null)
		{
			data = data.replace("\n", lineSeparator);
		}
		writer.writeCData(data);
		wroteText = true;
	}

	public void writeDTD(String dtd)
		throws XMLStreamException
	{
		writer.writeDTD(dtd);
	}

	public void writeEntityRef(String name)
		throws XMLStreamException
	{
		writer.writeEntityRef(name);
	}

	public void writeStartDocument()
		throws XMLStreamException
	{
		writer.writeStartDocument();
		wroteText = false;
	}

	public void writeStartDocument(String version)
		throws XMLStreamException
	{
		writer.writeStartDocument(version);
		wroteText = false;
	}

	public void writeStartDocument(String encoding, String version)
		throws XMLStreamException
	{
		writer.writeStartDocument(encoding, version);
		wroteText = false;
	}

	public void writeCharacters(String text)
		throws XMLStreamException
	{
		text = StaxUtilities.normalizeNewlines(text);
		if(text != null)
		{
			text = text.replace("\n", lineSeparator);
		}
		writer.writeCharacters(text);
		wroteText = true;
	}

	public void writeCharacters(char[] text, int start, int len)
		throws XMLStreamException
	{
		writer.writeCharacters(text, start, len);
		wroteText = true;
	}

	public String getPrefix(String uri)
		throws XMLStreamException
	{
		return writer.getPrefix(uri);
	}

	public void setPrefix(String prefix, String uri)
		throws XMLStreamException
	{
		writer.setPrefix(prefix, uri);
	}

	public void setDefaultNamespace(String uri)
		throws XMLStreamException
	{
		writer.setDefaultNamespace(uri);
	}

	public void setNamespaceContext(NamespaceContext context)
		throws XMLStreamException
	{
		writer.setNamespaceContext(context);
	}

	public NamespaceContext getNamespaceContext()
	{
		return writer.getNamespaceContext();
	}

	public Object getProperty(String name)
		throws IllegalArgumentException
	{
		return writer.getProperty(name);
	}
}
