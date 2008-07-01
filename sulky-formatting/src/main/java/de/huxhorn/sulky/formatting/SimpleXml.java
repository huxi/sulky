/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2008 Joern Huxhorn
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
package de.huxhorn.sulky.formatting;

public class SimpleXml
{
	/**
	 * Replaces the characters '&amp;', '&lt;', '&gt;' and '&quot;' with their respective xml-entities. Does also replace a zero byte with space.
	 *
	 * @param input
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
	 * @param input
	 * @return
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
	 * This method ensures that the output String has only
	 * valid XML unicode characters as specified by the
	 * XML 1.0 standard. For reference, please see
	 * <a href="http://www.w3.org/TR/2000/REC-xml-20001006#NT-Char">the
	 * standard</a>.
	 * <p/>
	 * Based on code from http://cse-mjmcl.cse.bris.ac.uk/blog/2007/02/14/1171465494443.html
	 *
	 * This method takes into account that no change will be necessary most of the time so
	 * nothing will be allocated/changed until the first non-valid character is found.
	 *
	 * @param in The String whose non-valid characters we want to remove.
	 * @param replacementChar the character to replace invalid characters with.
	 * @return The in String, with non-valid characters replaced by replacementChar.
	 */
	public static String replaceNonValidXMLCharacters(String in, char replacementChar)
	{
		StringBuffer out = null;

		if (!((replacementChar == 0x9) ||
				(replacementChar == 0xA) ||
				(replacementChar == 0xD) ||
				((replacementChar >= 0x20) && (replacementChar <= 0xD7FF)) ||
				((replacementChar >= 0xE000) && (replacementChar <= 0xFFFD)) ||
				((replacementChar >= 0x10000) && (replacementChar <= 0x10FFFF))))
		{
			throw new IllegalArgumentException("Replacement character 0x"+Integer.toString(replacementChar,16)+" is invalid itself!");
		}

		for (int i = 0; i < in.length(); i++)
		{
			char current = in.charAt(i);
			if (!((current == 0x9) ||
					(current == 0xA) ||
					(current == 0xD) ||
					((current >= 0x20) && (current <= 0xD7FF)) ||
					((current >= 0xE000) && (current <= 0xFFFD)) ||
					((current >= 0x10000) && (current <= 0x10FFFF))))
			{
				if(out==null)
				{
					out=new StringBuffer(in);
				}
				out.setCharAt(i, replacementChar);
			}
		}
		if(out!=null)
		{
			return out.toString();
		}
		return in; // no change.
	}
}
