/*
 * sulky-resources - inheritance-safe class resources.
 * Copyright (C) 2002-2017 Joern Huxhorn
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
 * Copyright 2002-2017 Joern Huxhorn
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

package de.huxhorn.sulky.resources;

import java.util.List;
import java.util.Objects;

/**
 * DOCUMENT: <code>CyclicLinkException</code>
 */
public class CyclicLinkException
	extends Exception
{
	private static final long serialVersionUID = 5197383554254357523L;

	private final List<String> linkStack;
	private final String cycleCause;

	public CyclicLinkException(List<String> linkStack, String cycleCause)
	{
		this.linkStack = Objects.requireNonNull(linkStack, "linkStack must not be null!");
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
			result.append('"').append(linkStack.get(i)).append('"');
		}

		return result.toString();
	}

	@Override
	public String toString()
	{
		return "CyclicLinkException: " +
				"Link-Stack: {" +
				getLinkStackString() +
				" => \"" +
				cycleCause +
				"\"}";
	}
}
