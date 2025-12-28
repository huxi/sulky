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

import de.huxhorn.sulky.resources.junit.Foobar;
import java.util.Locale;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class ResourceMapsTest
{
	private static final Locale US_LOCALE = Locale.of("en", "US");
	private static final Locale GERMANY_LOCALE = Locale.of("de", "DE");
	private static final  Locale HESSIAN_LOCALE = Locale.of("de", "DE", "hessisch");
	private static Locale prevDefault;

	private void internalGetResourceMap(Class clazz, String resourceBaseName, Locale locale, String[][] expectedResults)
	{
		Map<String, Object> result;

		result = ResourceMaps.getResourceMap(clazz, resourceBaseName, locale);
		assertEquals("Number of elements", expectedResults.length, result.size());
		for(String[] kv : expectedResults)
		{
			// kv[0]=key, kv[1]=value
			String key = kv[0];
			String cur = (String) result.get(key);
			assertEquals("Property \"" + key + "\"", kv[1], cur);
		}
		if(locale == null)
		{
			result = ResourceMaps.getResourceMap(clazz, resourceBaseName);
			assertEquals("Number of elements", expectedResults.length, result.size());
			for(String[] kv : expectedResults)
			{
				// kv[0]=key, kv[1]=value
				String key = kv[0];
				String cur = (String) result.get(key);
				assertEquals("Property \"" + key + "\"", kv[1], cur);
			}
		}
	}

	private void internalGetLocalResourceMap(Class clazz, String resourceBaseName, Locale locale, String[][] expectedResults)
	{
		Map result;
		result = ResourceMaps.getLocalResourceMap(clazz, resourceBaseName, locale);
		assertEquals("Number of elements", expectedResults.length, result.size());
		for(String[] kv : expectedResults)
		{
			// kv[0]=key, kv[1]=value
			String key = kv[0];
			String cur = (String) result.get(key);
			assertEquals("Property \"" + key + "\"", kv[1], cur);
		}
		if(locale == null)
		{
			result = ResourceMaps.getLocalResourceMap(clazz, resourceBaseName);
			assertEquals("Number of elements", expectedResults.length, result.size());
			for(String[] kv : expectedResults)
			{
				// kv[0]=key, kv[1]=value
				String key = kv[0];
				String cur = (String) result.get(key);
				assertEquals("Property \"" + key + "\"", kv[1], cur);
			}
		}
	}

	@BeforeClass
	public static void beforeClass()
		throws Exception
	{
		prevDefault = Locale.getDefault();
		Locale.setDefault(US_LOCALE);
	}

	@AfterClass
	public static void afterClass()
		throws Exception
	{
		Locale.setDefault(prevDefault);
	}

	@Test
	public void getResourceMap()
		throws Exception
	{
		Class c;

		c = Foobar.class;
		internalGetResourceMap(c, "resources", HESSIAN_LOCALE, new String[][]{
			{"attention.txt", "Uffbasse!"},
			{"ok.txt", "OK"},
			{"cancel.txt", "Abbruch"},
			{"base.txt", "BaseClass"},
		});
		internalGetResourceMap(c, "resources", GERMANY_LOCALE, new String[][]{
			{"attention.txt", "Achtung!"},
			{"ok.txt", "OK"},
			{"cancel.txt", "Abbruch"},
			{"base.txt", "BaseClass"},
		});
		internalGetResourceMap(c, "resources", null, new String[][]{
			{"attention.txt", "Attention!"},
			{"ok.txt", "OK"},
			{"cancel.txt", "Cancel"},
			{"base.txt", "BaseClass"},
		});

		c = c.getSuperclass();
		internalGetResourceMap(c, "resources", null, new String[][]{
			{"base.txt", "BaseClass"},
		});
	}

	@Test
	public void getLocalResourceMap()
		throws Exception
	{
		Class c;

		c = Foobar.class;
		internalGetLocalResourceMap(c, "resources", HESSIAN_LOCALE, new String[][]{
			{"attention.txt", "Uffbasse!"},
			{"ok.txt", "OK"},
			{"cancel.txt", "Abbruch"},
		});
		internalGetLocalResourceMap(c, "resources", GERMANY_LOCALE, new String[][]{
			{"attention.txt", "Achtung!"},
			{"ok.txt", "OK"},
			{"cancel.txt", "Abbruch"},
		});
		internalGetLocalResourceMap(c, "resources", null, new String[][]{
			{"attention.txt", "Attention!"},
			{"ok.txt", "OK"},
			{"cancel.txt", "Cancel"},
		});

		c = c.getSuperclass();
		internalGetLocalResourceMap(c, "resources", null, new String[][]{
			{"base.txt", "BaseClass"},
		});
	}
}
