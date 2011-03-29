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

/**
 * This class is a minimal wrapper for date stored and retrieved from Apple Property List (plist) files.
 *
 * Due to the nature of plists, the interface is very minimal and simply supports the retrieval of the plist root.
 *
 * This root can be any of the following:
 * <ul>
 * <li>array</li>
 * <li>dict (essentially a Map&lt;String, ?&gt;)</li>
 * <li>data (byte[])</li>
 * <li>date</li>
 * <li>real</li>
 * <li>integer</li>
 * <li>string</li>
 * <li>true</li>
 * <li>false</li>
 * </ul>
 *
 * array and dict are both collections that can, in turn, contain the same elements again.
 *
 * Please take a look at the documentation of PropertyListEncoder and PropertyListDecoder for
 * further information how the different Java data types are handled.
 *
 * Further information can be obtained at the following links:
 * <ul>
 * <li><a href="http://www.apple.com/DTDs/PropertyList-1.0.dtd">The PropertyList DTD hosted at Apple.</a></li>
 * <li><a href="http://developer.apple.com/library/ios/#documentation/General/Reference/InfoPlistKeyReference/Introduction/Introduction.html">Information Property List Key Reference</a></li>
 * <li><a href=" http://developer.apple.com/library/mac/#documentation/Java/Reference/Java_InfoplistRef/Articles/JavaDictionaryInfo.plistKeys.html">Java Dictionary Info.plist Keys</a></li>
 * </ul>
 *
 * @see PropertyListEncoder
 * @see PropertyListDecoder
 */
public class PropertyList
{
	private Object root;

	public Object getRoot()
	{
		return root;
	}

	public void setRoot(Object root)
	{
		this.root = root;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		PropertyList that = (PropertyList) o;

		if(root != null ? !root.equals(that.root) : that.root != null) return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		return root != null ? root.hashCode() : 0;
	}

	@Override
	public String toString()
	{
		return "PropertyList{root=" + root + "}";
	}
}
