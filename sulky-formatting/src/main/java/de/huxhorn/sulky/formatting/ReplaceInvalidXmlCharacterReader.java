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

package de.huxhorn.sulky.formatting;

import java.io.FilterReader;
import java.io.IOException;
import java.io.Reader;

public class ReplaceInvalidXmlCharacterReader
	extends FilterReader
{
	/**
	 * The default replacement character. 0xFFFD
	 */
	public static final char DEFAULT_REPLACEMENT_CHARACTER = (char) 0xFFFD;

	private final char replacementChar;

	/**
	 * Creates a new filtered reader.
	 *
	 * @param in a Reader object providing the underlying stream.
	 * @throws NullPointerException if <code>in</code> is <code>null</code>
	 */
	public ReplaceInvalidXmlCharacterReader(Reader in)
	{
		this(in, DEFAULT_REPLACEMENT_CHARACTER);
	}

	/**
	 * Creates a new filtered reader.
	 *
	 * @param in a Reader object providing the underlying stream.
	 * @param replacementChar the character to replace invalid characters with.
	 * @throws NullPointerException if <code>in</code> is <code>null</code>
	 * @throws IllegalArgumentException if <code>replacementChar</code> is an invalid character itself.
	 */
	public ReplaceInvalidXmlCharacterReader(Reader in, char replacementChar)
	{
		super(in);
		if(!SimpleXml.isValidXMLCharacter(replacementChar))
		{
			throw new IllegalArgumentException("Replacement character 0x"
					+ Integer.toString(replacementChar, 16).toUpperCase() + " is invalid itself!");
		}
		this.replacementChar = replacementChar;
	}

	public char getReplacementChar()
	{
		return replacementChar;
	}

	/**
	 * Reads a single character.
	 *
	 * @exception IOException  If an I/O error occurs
	 */
	public int read()
			throws IOException
	{
		int result = in.read();
		if(SimpleXml.isValidXMLCharacter(result))
		{
			return result;
		}
		return replacementChar;
	}

	/**
	 * Reads characters into a portion of an array.
	 *
	 * @exception  IOException  If an I/O error occurs
	 */
	public int read(char cbuf[], int off, int len)
			throws IOException
	{
		int result = in.read(cbuf, off, len);
		if(result > 0)
		{
			for (int i = off; i < off + result; i++)
			{
				if (SimpleXml.isValidXMLCharacter(cbuf[i]))
				{
					continue;
				}
				cbuf[i] = replacementChar;
			}
		}
		return result;
	}

}
