/*
 * sulky-resources - inheritance-safe class resources.
 * Copyright (C) 2002-2008 Joern Huxhorn
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
package de.huxhorn.sulky.resources;

import java.util.Stack;

/**
 * DOCUMENT: <code>CyclicLinkException</code>
 */
public class CyclicLinkException
	extends Exception
{
	private static final long serialVersionUID = 5197383554254357523L;

	private Stack<String> linkStack;
	private String cycleCause;

	public CyclicLinkException(Stack<String> linkStack, String cycleCause)
	{
		this.linkStack = linkStack;
		this.cycleCause = cycleCause;
	}

	public String getCycleCause()
	{
		return cycleCause;
	}

	public String getLinkStackString()
	{
		StringBuilder result = new StringBuilder();
		for(int i = 0; i < linkStack.size(); i++)
		{
			if(i != 0)
			{
				result.append(" => ");
			}
			result.append("\"");
			result.append(linkStack.elementAt(i));
			result.append("\"");
		}

		return result.toString();
	}

	public String toString()
	{
		StringBuilder result = new StringBuilder();
		result.append("CyclicLinkException: ");
		result.append("Link-Stack: {");
		result.append(getLinkStackString());
		result.append(" => \"");
		result.append(cycleCause);
		result.append("\"}");

		return result.toString();
	}
}
