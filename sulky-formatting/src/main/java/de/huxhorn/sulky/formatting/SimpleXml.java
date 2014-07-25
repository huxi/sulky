/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2014 Joern Huxhorn
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
 * Copyright 2007-2014 Joern Huxhorn
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

public final class SimpleXml
{
	// below constants are the valid ranges of XML characters
	// according to http://www.w3.org/TR/REC-xml#charsets
    private static final int XML_CHAR_RANGE_A_START = 0x000020;
    private static final int XML_CHAR_RANGE_A_END   = 0x00D7FF;
    private static final int XML_CHAR_RANGE_B_START = 0x00E000;
    private static final int XML_CHAR_RANGE_B_END   = 0x00FFFD;
    private static final int XML_CHAR_RANGE_C_START = 0x010000;
    private static final int XML_CHAR_RANGE_C_END   = 0x10FFFF;

	private SimpleXml()
	{}

    /**
     * Tests a given character whether or not it is a valid XML character.
	 *
	 * For reference, please see
	 * <a href="http://www.w3.org/TR/REC-xml#charsets">the
	 * specification</a>.
	 *
     * @param character The character to test
     * @return whether or not the supplied character is a valid XML character
     */
    public static boolean isValidXMLCharacter(char character)
    {
	    return isValidXMLCharacter(0xFFFF & character);
    }

    public static boolean isValidXMLCharacter(int codePoint)
    {
        return codePoint == '\t' || codePoint == '\r' || codePoint == '\n' ||
			(codePoint >= XML_CHAR_RANGE_A_START && codePoint <= XML_CHAR_RANGE_A_END) ||
			(codePoint >= XML_CHAR_RANGE_B_START && codePoint <= XML_CHAR_RANGE_B_END) ||
			(codePoint >= XML_CHAR_RANGE_C_START && codePoint <= XML_CHAR_RANGE_C_END);
    }

	/**
	 * Replaces the characters '&amp;', '&lt;', '&gt;' and '&quot;' with their respective xml-entities. Does also replace a zero byte with space.
	 *
	 * @param input the input that will be xml-escaped
	 * @return the xml-escaped input.
	 */
	public static String escape(String input)
	{
		String result = input;

		result = result.replace((char) 0, ' ');
		result = result.replace("&", "&amp;");
		result = result.replace("<", "&lt;");
		result = result.replace(">", "&gt;");
		result = result.replace("\"", "&quot;");
		//result=result.replace("'", "&apos;");
		// apos is not escaped because swing html does not know about &apos;...

		return result;
	}

	/**
	 * Reverses escape with the exception of the zero-byte escape.
	 *
	 * @param input the input that will be xml-unescaped
	 * @return the unescaped string.
	 */
	public static String unescape(String input)
	{
		String result = input;

		//result=result.replace("&apos;", "'");
		result = result.replace("&quot;", "\"");
		result = result.replace("&gt;", ">");
		result = result.replace("&lt;", "<");
		result = result.replace("&amp;", "&");

		return result;
	}

	/**
	 * <p>
	 * This method ensures that the output String has only
	 * valid XML unicode characters as specified by the
	 * XML 1.0 standard.
	 * For reference, please see
	 * <a href="http://www.w3.org/TR/REC-xml#charsets">the
	 * specification</a>.
	 * </p>
	 * <p>
	 * Based on code from http://cse-mjmcl.cse.bris.ac.uk/blog/2007/02/14/1171465494443.html
	 * </p>
	 *
	 * <p>
	 * This method takes into account that no change will be necessary most of the time so
	 * nothing will be allocated/changed until the first non-valid character is found.
	 * </p>
	 *
	 * @param in              The String whose non-valid characters we want to remove.
	 * @param replacementChar the character to replace invalid characters with.
	 * @return The in String, with non-valid characters replaced by replacementChar.
	 */
	public static String replaceNonValidXMLCharacters(String in, char replacementChar)
	{
		StringBuilder out = null;

		if(!isValidXMLCharacter(replacementChar))
		{
			throw new IllegalArgumentException("Replacement character 0x"
				+ Integer.toString(replacementChar, 16).toUpperCase() + " is invalid itself!");
		}

		for(int i = 0; i < in.length(); i++)
		{
			int current = in.codePointAt(i);

			if(isValidXMLCharacter(current))
			{
                continue;
			}
            if(out == null)
            {
                out = new StringBuilder(in);
            }
            out.setCharAt(i, replacementChar);
		}
		if(out != null)
		{
			return out.toString();
		}
		return in; // no change.
	}
}
