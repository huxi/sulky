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

import de.huxhorn.sulky.resources.junit.Foobar;

import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;
import java.util.Map;

public class ResourceMapsTest
{
	private final Logger logger = LoggerFactory.getLogger(ResourceMapsTest.class);

	private static final Locale usLocale = new Locale("en", "US");
	final Locale germanLocale = new Locale("de", "DE");
	final Locale hessianLocale = new Locale("de", "DE", "hessisch");
	final Locale empty = new Locale("", "", "");
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
	public static void setUp()
		throws Exception
	{
		prevDefault = Locale.getDefault();
		Locale.setDefault(usLocale);
	}

	@AfterClass
	public static void tearDown()
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
		internalGetResourceMap(c, "resources", hessianLocale, new String[][]{
			{"attention.txt", "Uffbasse!"},
			{"ok.txt", "OK"},
			{"cancel.txt", "Abbruch"},
			{"base.txt", "BaseClass"},
		});
		internalGetResourceMap(c, "resources", germanLocale, new String[][]{
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
		internalGetLocalResourceMap(c, "resources", hessianLocale, new String[][]{
			{"attention.txt", "Uffbasse!"},
			{"ok.txt", "OK"},
			{"cancel.txt", "Abbruch"},
		});
		internalGetLocalResourceMap(c, "resources", germanLocale, new String[][]{
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