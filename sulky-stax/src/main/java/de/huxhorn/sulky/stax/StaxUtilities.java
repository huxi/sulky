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

import org.slf4j.Logger;

import java.util.Iterator;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

public final class StaxUtilities
{
	private static final char TAB = 0x09;
	private static final char LINE_FEED = 0x0A;
	private static final char CARRIAGE_RETURN = 0x0D;
	private static final char SPACE = 0x20;

	public static final String CDATA_END = "]]>";

	public static final String XML_SCHEMA_INSTANCE_NAMESPACE_URI = "http://www.w3.org/2001/XMLSchema-instance";
	public static final String XML_SCHEMA_INSTANCE_PREFIX = "xsi";
	public static final String XML_SCHEMA_INSTANCE_SCHEMA_LOCATION_ATTRIBUTE = "schemaLocation";
	public static final String NO_PREFIX = "";
	private static final String NEWLINE = "\n";
	private static final String INDENT = "\t";

	private StaxUtilities()
	{}
	
	/**
	 * Shortcut for readSimpleTextNodeIfAvailable(reader, namespaceURI, nodeName, WhiteSpaceHandling.PRESERVE_NORMALIZE_NEWLINE).
	 *
	 * @param reader
	 * @param namespaceURI
	 * @param nodeName
	 * @return the read text.
	 * @throws XMLStreamException
	 */
	public static String readSimpleTextNodeIfAvailable(XMLStreamReader reader, String namespaceURI, String nodeName)
		throws XMLStreamException
	{
		return readSimpleTextNodeIfAvailable(reader, namespaceURI, nodeName, WhiteSpaceHandling.PRESERVE_NORMALIZE_NEWLINE);
	}

	public static String readSimpleTextNodeIfAvailable(XMLStreamReader reader, String namespaceURI, String nodeName, WhiteSpaceHandling whiteSpace)
		throws XMLStreamException
	{
		int type = reader.getEventType();
		String result = null;
		if(XMLStreamConstants.START_ELEMENT == type && nodeName.equals(reader.getLocalName()))
		{
			if((namespaceURI == null && reader.getNamespaceURI() == null)
				|| (namespaceURI != null && namespaceURI.equals(reader.getNamespaceURI())))
			{
				result = readText(reader, whiteSpace);
				reader.nextTag();
			}
		}
		return result;
	}

	/**
	 * Shortcut for readText(reader, WhiteSpaceHandling.PRESERVE_NORMALIZE_NEWLINE).
	 *
	 * @param reader
	 * @return the read text.
	 * @throws XMLStreamException
	 */
	public static String readText(XMLStreamReader reader)
		throws XMLStreamException
	{
		return readText(reader, WhiteSpaceHandling.PRESERVE_NORMALIZE_NEWLINE);
	}

	public static String readText(XMLStreamReader reader, WhiteSpaceHandling whiteSpace)
		throws XMLStreamException
	{
		String result = reader.getElementText();
		result = processWhiteSpace(result, whiteSpace);
		//reader.nextTag();
		return result;
	}

	public static String processWhiteSpace(String string, WhiteSpaceHandling whiteSpace)
	{
		if(string == null)
		{
			return null;
		}

		switch(whiteSpace)
		{
			case PRESERVE_NORMALIZE_NEWLINE:
				return normalizeNewlines(string);
			case REPLACE:
				return replaceWhiteSpace(string);
			case COLLAPSE:
				return collapseWhiteSpace(string);
		}
		return string;
	}

	/**
	 * After the processing implied by replace, contiguous sequences of #x20's are collapsed to a single #x20,
	 * and leading and trailing #x20's are removed.
	 *
	 * @param string
	 * @return the string with collapsed whitespace, null if string is null.
	 * @see #replaceWhiteSpace(String)
	 */
	public static String collapseWhiteSpace(String string)
	{
		if(string == null)
		{
			return null;
		}
		String replaced = replaceWhiteSpace(string);
		char[] chars = replaced.toCharArray();
		StringBuilder result = new StringBuilder(replaced.length());
		boolean needSpace = false;
		for(char c : chars)
		{
			if(c == SPACE)
			{
				if(result.length() != 0)
				{
					// we are not at the start anymore
					needSpace = true;
				}
			}
			else
			{
				if(needSpace)
				{
					needSpace = false;
					result.append(SPACE);
				}
				result.append(c);
			}
		}
		return result.toString();
	}

	/**
	 * All occurrences of #x9 (tab), #xA (line feed) and #xD (carriage return) are replaced with #x20 (space)
	 *
	 * @param string
	 * @return the string with replaced whitespace, null if string is null.
	 */
	public static String replaceWhiteSpace(String string)
	{
		if(string == null)
		{
			return null;
		}
		StringBuilder result = new StringBuilder(string);
		for(int i = 0; i < result.length(); i++)
		{
			char c = result.charAt(i);
			if(c == TAB || c == CARRIAGE_RETURN || c == LINE_FEED)
			{
				result.setCharAt(i, SPACE);
			}
		}
		return result.toString();
	}

	/**
	 * Shortcut for readAttributeValue(reader, namespaceURI, name, WhiteSpaceHandling.COLLAPSE);
	 */
	public static String readAttributeValue(XMLStreamReader reader, String namespaceURI, String name)
	{
		return readAttributeValue(reader, namespaceURI, name, WhiteSpaceHandling.COLLAPSE);
	}

	public static String readAttributeValue(XMLStreamReader reader, String namespaceURI, String name, WhiteSpaceHandling whiteSpace)
	{
		String attributeValue = reader.getAttributeValue(namespaceURI, name);
		if(attributeValue == null)
		{
			// this actually seems to be okay... attributes are unique
			attributeValue = reader.getAttributeValue(null, name);
		}
		return processWhiteSpace(attributeValue, whiteSpace);
	}

	public static void writeNamespace(XMLStreamWriter writer, String prefix, String namespaceURI)
		throws XMLStreamException
	{
		if(prefix == null || NO_PREFIX.equals(prefix))
		{
			writer.writeDefaultNamespace(namespaceURI);
		}
		else
		{
			writer.writeNamespace(prefix, namespaceURI);
		}
	}

	public static NamespaceInfo setNamespace(XMLStreamWriter writer, String prefix, String namespaceURI, String defaultPrefix)
		throws XMLStreamException
	{
		String p = writer.getPrefix(namespaceURI);
		if(p == null)
		{
			// not defined yet
			NamespaceContext nsc = writer.getNamespaceContext();
			if(prefix == null || NO_PREFIX.equals(prefix))
			{
				String defaultNamespaceURI = nsc.getNamespaceURI(NO_PREFIX);
				if(defaultNamespaceURI == null || XMLConstants.NULL_NS_URI.equals(defaultNamespaceURI))
				{
					// no defaultNS yet.
					writer.setDefaultNamespace(namespaceURI);
					return new NamespaceInfo(null, true);
				}
				return resolveNamespacePrefix(writer, nsc, defaultPrefix, namespaceURI);
			}
			else
			{
				return resolveNamespacePrefix(writer, nsc, prefix, namespaceURI);
			}
		}
		else
		{
			if(NO_PREFIX.equals(p))
			{
				p = null;
			}
			return new NamespaceInfo(p, false);
		}
	}

	private static NamespaceInfo resolveNamespacePrefix(XMLStreamWriter writer, NamespaceContext nsc, String prefix, String namespaceURI)
		throws XMLStreamException
	{
		int counter = 1;
		String prefixCandidate = prefix;
		for(; ;)
		{
			String ns = nsc.getNamespaceURI(prefixCandidate);
			if(!XMLConstants.NULL_NS_URI.equals(ns))
			{
				writer.setPrefix(prefixCandidate, namespaceURI);
				return new NamespaceInfo(prefixCandidate, true);
			}
			counter++;
			prefixCandidate = prefix + counter;
		}
	}

	public static class NamespaceInfo
	{
		private String prefix;
		private boolean created;

		public NamespaceInfo(String prefix, boolean created)
		{
			this.prefix = prefix;
			this.created = created;
		}

		public String getPrefix()
		{
			return prefix;
		}

		public boolean isCreated()
		{
			return created;
		}
	}

	/**
	 * If namespaceURI is null, this method calls writer.writeStartElement without namespaceURI argument.
	 * Otherwise the one including it is being used.
	 *
	 * @param writer
	 * @param namespaceURI
	 * @param nodeName
	 * @throws XMLStreamException
	 */
	public static void writeStartElement(XMLStreamWriter writer, String prefix, String namespaceURI, String nodeName)
		throws XMLStreamException
	{
		if(namespaceURI != null)
		{
			if(prefix != null && !NO_PREFIX.equals(prefix))
			{
				writer.writeStartElement(prefix, nodeName, namespaceURI);
			}
			else
			{
				writer.writeStartElement(namespaceURI, nodeName);
			}
		}
		else
		{
			writer.writeStartElement(nodeName);
		}
	}

	public static void writeEmptyElement(XMLStreamWriter writer, String prefix, String namespaceURI, String nodeName)
		throws XMLStreamException
	{
		if(namespaceURI != null)
		{
			if(prefix != null && !NO_PREFIX.equals(prefix))
			{
				writer.writeEmptyElement(prefix, nodeName, namespaceURI);
			}
			else
			{
				writer.writeEmptyElement(namespaceURI, nodeName);
			}
		}
		else
		{
			writer.writeEmptyElement(nodeName);
		}
	}

	/**
	 * Shortcut for writeAttribute(writer, prefix, namespaceURI, name, value, WhiteSpaceHandling.COLLAPSE).
	 *
	 * @param writer
	 * @param prefix
	 * @param namespaceURI
	 * @param name
	 * @param value
	 * @throws XMLStreamException
	 */
	public static void writeAttribute(XMLStreamWriter writer, boolean qualified, String prefix, String namespaceURI, String name, String value)
		throws XMLStreamException
	{
		writeAttribute(writer, qualified, prefix, namespaceURI, name, value, WhiteSpaceHandling.COLLAPSE);
	}

	public static void writeAttribute(XMLStreamWriter writer, boolean qualified, String prefix, String namespaceURI, String name, String value, WhiteSpaceHandling whiteSpace)
		throws XMLStreamException
	{
		value = processWhiteSpace(value, whiteSpace);
		if(qualified && namespaceURI != null)
		{
			if(prefix == null || NO_PREFIX.equals(prefix))
			{
				writer.writeAttribute(namespaceURI, name, value);
			}
			else
			{
				writer.writeAttribute(prefix, namespaceURI, name, value);
			}
		}
		else
		{
			writer.writeAttribute(name, value);
		}
	}

	/**
	 * Shortcut for writeAttributeIfNotNull(writer, prefix, namespaceURI, name, value, WhiteSpaceHandling.COLLAPSE).
	 *
	 * @param writer
	 * @param prefix
	 * @param namespaceURI
	 * @param name
	 * @param value
	 * @throws XMLStreamException
	 */
	public static void writeAttributeIfNotNull(XMLStreamWriter writer, boolean qualified, String prefix, String namespaceURI, String name, String value)
		throws XMLStreamException
	{
		writeAttributeIfNotNull(writer, qualified, prefix, namespaceURI, name, value, WhiteSpaceHandling.COLLAPSE);
	}

	public static void writeAttributeIfNotNull(XMLStreamWriter writer, boolean qualified, String prefix, String namespaceURI, String name, String value, WhiteSpaceHandling whiteSpace)
		throws XMLStreamException
	{
		if(value != null)
		{
			writeAttribute(writer, qualified, prefix, namespaceURI, name, value, whiteSpace);
		}
	}

	/**
	 * Normalizes the newlines of the string.
	 * <p/>
	 * Replaces "\r\n", "\n\r" and "\r" with a single "\n".
	 *
	 * @param input
	 * @return a string with cleaned up newlines, i.e. only \n, no \r.
	 */
	public static String normalizeNewlines(String input)
	{
		String result = input;
		if(input != null)
		{
			char[] chars = input.toCharArray();
			final int length = chars.length;

			int startWritePos = 0;

			StringBuilder resultBuffer = null;

			char previousChar = 0;
			for(int index = 0; index < length; index++)
			{
				char ch = chars[index];
				switch(ch)
				{
					case '\r':
						if(resultBuffer == null)
						{
							resultBuffer = new StringBuilder();
						}
						resultBuffer.append(chars, startWritePos, index - startWritePos);
						if(previousChar != '\n')
						{
							resultBuffer.append(NEWLINE);
							previousChar = ch;
						}
						else
						{
							// reset so only pairs are ignored
							previousChar = 0;
						}
						startWritePos = index + 1;
						break;

					case '\n':
						if(resultBuffer == null)
						{
							resultBuffer = new StringBuilder();
						}
						resultBuffer.append(chars, startWritePos, index - startWritePos);
						if(previousChar != '\r')
						{
							resultBuffer.append(NEWLINE);
							previousChar = ch;
						}
						else
						{
							// reset so only pairs are ignored
							previousChar = 0;
						}
						startWritePos = index + 1;

						break;

					default:
						previousChar = 0;
						break;
				}
			}

			// Write any pending data
			if(resultBuffer != null)
			{
				resultBuffer.append(chars, startWritePos, length - startWritePos);
				result = resultBuffer.toString();
			}
		}
		return result;
	}


	/**
	 * Writes a &lt;nodeName&gt; node containing the given text.
	 * <p/>
	 * If tryUsingCData is true and the text does not contain the CData end token,
	 * the text will be written using writeCData. Otherwise writeCharacters is used.
	 *
	 * @param writer
	 * @param prefix        the prefix of the node. May be null.
	 * @param namespaceURI  the namespaceURI of the node. May be null.
	 * @param nodeName      the nodeName of the node.
	 * @param text          the text that is written into to node. Must not be null.
	 * @param tryUsingCData is trying to is CData instead of encoded characters if possible.
	 * @throws XMLStreamException
	 * @see #writeText(javax.xml.stream.XMLStreamWriter, String, WhiteSpaceHandling, boolean)
	 */
	public static void writeSimpleTextNode(XMLStreamWriter writer, String prefix, String namespaceURI, String nodeName, String text, WhiteSpaceHandling whiteSpace, boolean tryUsingCData)
		throws XMLStreamException
	{
		writeStartElement(writer, prefix, namespaceURI, nodeName);
		writeText(writer, text, whiteSpace, tryUsingCData);
		writer.writeEndElement();
	}

	/**
	 * Shortcut for writeSimpleTextNode(writer, prefix, namespaceURI, nodeName, text, false).
	 * <p/>
	 * It won't try to use CDATA.
	 *
	 * @param writer
	 * @param prefix       the prefix of the node. May be null.
	 * @param namespaceURI the namespaceURI of the node. May be null.
	 * @param nodeName     the nodeName of the node.
	 * @param text         the text that is written into to node. Must not be null.
	 * @throws XMLStreamException
	 * @see #writeSimpleTextNode(javax.xml.stream.XMLStreamWriter, String, String, String, String, WhiteSpaceHandling, boolean)
	 */
	public static void writeSimpleTextNode(XMLStreamWriter writer, String prefix, String namespaceURI, String nodeName, String text, WhiteSpaceHandling whiteSpace)
		throws XMLStreamException
	{
		writeSimpleTextNode(writer, prefix, namespaceURI, nodeName, text, whiteSpace, false);
	}

	/**
	 * Shortcut for writeSimpleTextNode(writer, prefix, namespaceURI, nodeName, text, false).
	 * <p/>
	 * It won't try to use CDATA and uses WhiteSpaceHandling.PRESERVE_NORMALIZE_NEWLINE.
	 *
	 * @param writer
	 * @param prefix       the prefix of the node. May be null.
	 * @param namespaceURI the namespaceURI of the node. May be null.
	 * @param nodeName     the nodeName of the node.
	 * @param text         the text that is written into to node. Must not be null.
	 * @throws XMLStreamException
	 * @see #writeSimpleTextNode(javax.xml.stream.XMLStreamWriter, String, String, String, String, WhiteSpaceHandling, boolean)
	 */
	public static void writeSimpleTextNode(XMLStreamWriter writer, String prefix, String namespaceURI, String nodeName, String text)
		throws XMLStreamException
	{
		writeSimpleTextNode(writer, prefix, namespaceURI, nodeName, text, WhiteSpaceHandling.PRESERVE_NORMALIZE_NEWLINE, false);
	}

	/**
	 * Writes a &lt;nodeName&gt; node containing the given text if text is not null.
	 * <p/>
	 * If tryUsingCData is true and the text does not contain the CData end token,
	 * the text will be written using writeCData. Otherwise writeCharacters is used.
	 *
	 * @param writer
	 * @param prefix        the prefix of the node. May be null.
	 * @param namespaceURI  the namespaceURI of the node. May be null.
	 * @param nodeName      the nodeName of the node.
	 * @param text          the text that is written into to node. May be null.
	 * @param tryUsingCData is trying to is CData instead of encoded characters if possible.
	 * @throws XMLStreamException
	 * @see #writeSimpleTextNode(javax.xml.stream.XMLStreamWriter, String, String, String, String, WhiteSpaceHandling, boolean)
	 */
	public static void writeSimpleTextNodeIfNotNull(XMLStreamWriter writer, String prefix, String namespaceURI, String nodeName, String text, WhiteSpaceHandling whiteSpace, boolean tryUsingCData)
		throws XMLStreamException
	{
		if(text != null)
		{
			writeSimpleTextNode(writer, prefix, namespaceURI, nodeName, text, whiteSpace, tryUsingCData);
		}
	}

	/**
	 * Writes a &lt;nodeName&gt; node containing the given text if text is not null.
	 * <p/>
	 * It won't try to use CDATA.
	 *
	 * @param writer
	 * @param prefix       the prefix of the node. May be null.
	 * @param namespaceURI the namespaceURI of the node. May be null.
	 * @param nodeName     the nodeName of the node.
	 * @param text         the text that is written into to node. May be null.
	 * @throws XMLStreamException
	 * @see #writeSimpleTextNode(javax.xml.stream.XMLStreamWriter, String, String, String, String, WhiteSpaceHandling, boolean)
	 */
	public static void writeSimpleTextNodeIfNotNull(XMLStreamWriter writer, String prefix, String namespaceURI, String nodeName, String text, WhiteSpaceHandling whiteSpace)
		throws XMLStreamException
	{
		if(text != null)
		{
			writeSimpleTextNode(writer, prefix, namespaceURI, nodeName, text, whiteSpace, false);
		}
	}

	/**
	 * Writes a &lt;nodeName&gt; node containing the given text if text is not null.
	 * <p/>
	 * It won't try to use CDATA and uses WhiteSpaceHandling.PRESERVE_NORMALIZE_NEWLINE.
	 *
	 * @param writer
	 * @param prefix       the prefix of the node. May be null.
	 * @param namespaceURI the namespaceURI of the node. May be null.
	 * @param nodeName     the nodeName of the node.
	 * @param text         the text that is written into to node. May be null.
	 * @throws XMLStreamException
	 * @see #writeSimpleTextNode(javax.xml.stream.XMLStreamWriter, String, String, String, String, WhiteSpaceHandling, boolean)
	 */
	public static void writeSimpleTextNodeIfNotNull(XMLStreamWriter writer, String prefix, String namespaceURI, String nodeName, String text)
		throws XMLStreamException
	{
		if(text != null)
		{
			writeSimpleTextNode(writer, prefix, namespaceURI, nodeName, text, WhiteSpaceHandling.PRESERVE_NORMALIZE_NEWLINE, false);
		}
	}

	/**
	 * Writes either characters or CDATA.
	 * <p/>
	 * CDATA is only used if tryUsingCData is true and text does not contain the CDATA end token.
	 *
	 * @param writer
	 * @param text          the text to be written. Must not be null.
	 * @param tryUsingCData
	 * @throws XMLStreamException
	 */
	public static void writeText(XMLStreamWriter writer, String text, WhiteSpaceHandling whiteSpace, boolean tryUsingCData)
		throws XMLStreamException
	{
		processWhiteSpace(text, whiteSpace);
		if(tryUsingCData && !text.contains(CDATA_END))
		{
			writer.writeCData(text);
		}
		else
		{
			writer.writeCharacters(text);
		}
	}

	/**
	 * Shortcut for writeText(writer, text, WhiteSpaceHandling.PRESERVE_NORMALIZE_NEWLINE, false).
	 *
	 * @param writer
	 * @param text
	 * @throws XMLStreamException
	 */
	public static void writeText(XMLStreamWriter writer, String text)
		throws XMLStreamException
	{
		writeText(writer, text, WhiteSpaceHandling.PRESERVE_NORMALIZE_NEWLINE, false);
	}

	public static String getEventTypeString(int eventType)
	{
		switch(eventType)
		{
			case XMLStreamConstants.ATTRIBUTE:
				return "ATTRIBUTE";
			case XMLStreamConstants.CDATA:
				return "CDATA";
			case XMLStreamConstants.CHARACTERS:
				return "CHARACTERS";
			case XMLStreamConstants.COMMENT:
				return "COMMENT";
			case XMLStreamConstants.DTD:
				return "DTD";
			case XMLStreamConstants.END_DOCUMENT:
				return "END_DOCUMENT";
			case XMLStreamConstants.END_ELEMENT:
				return "END_ELEMENT";
			case XMLStreamConstants.ENTITY_DECLARATION:
				return "ENTITY_DECLARATION";
			case XMLStreamConstants.ENTITY_REFERENCE:
				return "ENTITY_REFERENCE";
			case XMLStreamConstants.NAMESPACE:
				return "NAMESPACE";
			case XMLStreamConstants.NOTATION_DECLARATION:
				return "NOTATION_DECLARATION";
			case XMLStreamConstants.PROCESSING_INSTRUCTION:
				return "PROCESSING_INSTRUCTION";
			case XMLStreamConstants.SPACE:
				return "SPACE";
			case XMLStreamConstants.START_DOCUMENT:
				return "START_DOCUMENT";
			case XMLStreamConstants.START_ELEMENT:
				return "START_ELEMENT";
			default:
				return "<unknown type!>";
		}
	}

	public static String writerStatus(String msg, XMLStreamWriter writer, String namespaceURI)
	{
		StringBuilder msgBuf = new StringBuilder();
		NamespaceContext nsc = writer.getNamespaceContext();
		Iterator iter = nsc.getPrefixes(namespaceURI);
		if(msg != null)
		{
			msgBuf.append(msg).append(" - ");
		}
		msgBuf.append("Prefixes defined for namespace ").append(namespaceURI).append(":");
		if(iter.hasNext())
		{
			boolean isFirst = true;
			while(iter.hasNext())
			{
				if(!isFirst)
				{
					msgBuf.append(", ");
				}
				else
				{
					isFirst = false;
				}
				msgBuf.append("'").append(iter.next()).append("'");
			}
		}
		else
		{
			msgBuf.append("Undefined.");
		}

		return msgBuf.toString();
	}

	public static void logWriter(Logger logger, String msg, XMLStreamWriter writer, String namespaceURI)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug(writerStatus(msg, writer, namespaceURI));
		}
	}

	public static String readerStatus(String msg, XMLStreamReader reader)
	{
		int type = reader.getEventType();
		StringBuilder msgBuf = new StringBuilder(msg);
		msgBuf.append(NEWLINE);
		msgBuf.append(INDENT).append("eventType=").append(getEventTypeString(type)).append(NEWLINE);
		if(type == XMLStreamConstants.START_ELEMENT || type == XMLStreamConstants.END_ELEMENT)
		{
			msgBuf.append(INDENT).append("localName=").append(reader.getLocalName()).append(NEWLINE);
			msgBuf.append(INDENT).append("namespaceURI=").append(reader.getNamespaceURI()).append(NEWLINE);
		}
		if(type == XMLStreamConstants.START_ELEMENT)
		{
			int attCount = reader.getAttributeCount();
			msgBuf.append(INDENT).append("attributeCount=").append(attCount).append(NEWLINE);
			for(int i = 0; i < attCount; i++)
			{
				msgBuf.append(INDENT).append(INDENT).append("#####\n");
				msgBuf.append(INDENT).append(INDENT).append("attributeNamespace=").append(reader.getAttributeNamespace(i))
					.append(NEWLINE);
				msgBuf.append(INDENT).append(INDENT).append("attributeLocalName=").append(reader.getAttributeLocalName(i))
					.append(NEWLINE);
				msgBuf.append(INDENT).append(INDENT).append("attributeValue=").append(reader.getAttributeValue(i)).append(NEWLINE);
				msgBuf.append(INDENT).append(INDENT).append("attributePrefix=").append(reader.getAttributePrefix(i)).append(NEWLINE);
			}

		}
		msgBuf.append(INDENT).append("readerClass: ").append(reader.getClass().getName());
		return msgBuf.toString();
	}

	public static void logReader(Logger logger, String msg, XMLStreamReader reader)
	{
		if(logger.isDebugEnabled())
		{
			logger.debug(readerStatus(msg, reader));
		}

	}

}
