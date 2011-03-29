/*
 * sulky-resources - inheritance-safe class resources.
 * Copyright (C) 2002-2011 Joern Huxhorn
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
 * Copyright 2002-2011 Joern Huxhorn
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

package de.huxhorn.sulky.resources.junit;

import de.huxhorn.sulky.resources.Localizable;

import java.util.Locale;

class BaseClass
{

}

/**
 * <code>Foobar</code> is used to test the ResourceTools functionality.
 * It extends the package-private class <code>BaseClass</code> and has the
 * following associated resource structure in /resources/de/huxhorn/resources/junit/:
 * <ul>
 * <li>Foobar/
 * <ul>
 * <li>de/
 * <ul>
 * <li>locale.txt</li>
 * <li>locale.html</li>
 * </ul>
 * </li>
 * <li>de_DE/
 * <ul>
 * <li>locale.txt</li>
 * <li>locale.html</li>
 * <li>resources.properties</li>
 * </ul>
 * </li>
 * <li>de_DE_hessisch/
 * <ul>
 * <li>locale.txt</li>
 * <li>resources.properties</li>
 * </ul>
 * </li>
 * <li>en/
 * <ul>
 * <li>locale.txt</li>
 * </ul>
 * </li>
 * <li>en_US/
 * <ul>
 * <li>locale.txt</li>
 * </ul>
 * </li>
 * <li>
 * <ul>
 * <li>Internal/
 * <ul>
 * <li>test.txt</li>
 * </ul>
 * </li>
 * <li>StaticInternal/
 * <ul>
 * <li>de/
 * <ul>
 * <li>test.txt</li>
 * </ul>
 * </li>
 * <li>test.txt</li>
 * </ul>
 * </li>
 * </ul>
 * </li>
 * <li>cyclic1.link</li>
 * <li>cyclic2.link</li>
 * <li>cyclic3.link</li>
 * <li>cyclicFallback1</li>
 * <li>cyclicFallback1.link</li>
 * <li>cyclicFallback2.link</li>
 * <li>cyclicFallback3.link</li>
 * <li>link.txt.link</li>
 * <li>overloaded.txt</li>
 * <li>resources.properties</li>
 * <li>test.txt</li>
 * </ul>
 * </li>
 * <li>BaseClass/
 * <ul>
 * <li>baseFallback.txt</li>
 * <li>overloaded.txt</li>
 * <li>resources.properties</li>
 * </ul>
 * </li>
 * <li>defaultFallback.txt</li>
 * </ul>
 * <p/>
 * Foobar implements the interface Localizable directly (without a Proxy in ResourceSupport).
 */
public class Foobar
	extends BaseClass
	implements Localizable
{
	private Locale locale = null;

	public void setLocale(Locale locale)
	{
		this.locale = locale;
	}

	public Locale getLocale()
	{
		return locale;
	}


	/**
	 * Static internal class that has both Localizable methods and that has one declaring classes.
	 */
	public static class StaticInternal
	{
		private Locale locale;

		/**
		 * Static internal class that has no Localizable methods and that has two declaring classes.
		 */
		public static class Foo
		{

		}

		public void foo()
		{
		}

		public void setLocale(Locale locale)
		{
			this.locale = locale;
		}

		public Locale getLocale()
		{
			return locale;
		}
	}

	/**
	 * Internal class that has only the Localizable getLocale methods and that has one declaring classes.
	 */
	public class Internal
	{
		public Locale getLocale()
		{
			// method to justify the non-staticness of this internal class
			// FindBugs SIC detector
			return Foobar.this.getLocale();
		}
	}
}
