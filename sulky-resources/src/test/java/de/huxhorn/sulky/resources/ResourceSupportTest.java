/*
 * sulky-resources - inheritance-safe class resources.
 * Copyright (C) 2002-2019 Joern Huxhorn
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
 * Copyright 2002-2019 Joern Huxhorn
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
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Map;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.huxhorn.sulky.resources.ResourceTestHelper.appendSuffixes;
import static de.huxhorn.sulky.resources.ResourceTestHelper.assertResultEndsWith;
import static de.huxhorn.sulky.resources.ResourceTestHelper.logResult;
import static de.huxhorn.sulky.resources.ResourceTestHelper.logResults;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("PMD.CloseResource")
public class ResourceSupportTest
{
	private final Logger logger = LoggerFactory.getLogger(ResourceSupportTest.class);

	private ResourceSupport resourcesupport = null;

	private static final Locale US_LOCALE = Locale.of("en", "US");
	private static final Locale GERMANY_LOCALE = Locale.of("de", "DE");
	private static final Locale HESSIAN_LOCALE = Locale.of("de", "DE", "hessisch");
	private static Locale prevDefault;


	private void internalGetLocalResources(String resourceBaseName, String[] suffixes, Locale locale, String[] resultEndsWith)
	{
		// checking method with four arguments
		StringBuilder methCallBuff = new StringBuilder(100);
		methCallBuff.append("getLocalResources(\"").append(resourceBaseName).append("\", ");
		if(suffixes == null)
		{
			methCallBuff.append("null");
		}
		else
		{
			methCallBuff.append('[');
			for(int i = 0; i < suffixes.length; i++)
			{
				if(i != 0)
				{
					methCallBuff.append(", ");
				}
				methCallBuff.append('"').append(suffixes[i]).append('"');
			}
			methCallBuff.append(']');
		}
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if(logger.isInfoEnabled()) logger.info("Testing {}.", methodCall);

		URL[] result;
		result = resourcesupport.getLocalResources(resourceBaseName, suffixes, locale);
		logResults(logger, methodCall, result);
		assertEquals(methodCall + " - Number of results", resultEndsWith.length, result.length);

		for(int i = 0; i < result.length; i++)
		{
			String cur = result[i].toString();
			String msg = methodCall + " - resultUrls[" + i + "]:\"" + cur + "\" does not end with \"" + resultEndsWith[i] + "\"!";
			assertTrue(msg, cur.endsWith(resultEndsWith[i]));
		}

		if(locale == null)
		{
			// checking shortcut method (Class,String,String[]) without locale
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getLocalResources(\"").append(resourceBaseName).append("\", ");
			appendSuffixes(methCallBuff, suffixes);
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = resourcesupport.getLocalResources(resourceBaseName, suffixes);
			logResults(logger, methodCall, result);
			assertEquals(methodCall + " - Number of results", resultEndsWith.length, result.length);

			for(int i = 0; i < result.length; i++)
			{
				String cur = result[i].toString();
				String msg = methodCall + " - resultUrls[" + i + "]:\"" + cur + "\" does not end with \"" + resultEndsWith[i] + "\"!";
				assertTrue(msg, cur.endsWith(resultEndsWith[i]));
			}
			if(suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
			{
				// checking shortcut method (Class,String) without suffixes and locale
				// results must be the same...
				methCallBuff = new StringBuilder();
				methCallBuff.append("getLocalResources(\"").append(resourceBaseName).append("\");");
				methodCall = methCallBuff.toString();
				if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
				result = resourcesupport.getLocalResources(resourceBaseName);
				logResults(logger, methodCall, result);
				assertEquals(methodCall + " - Number of results", resultEndsWith.length, result.length);

				for(int i = 0; i < result.length; i++)
				{
					String cur = result[i].toString();
					String msg = methodCall + " - resultUrls[" + i + "]:\"" + cur + "\" does not end with \"" + resultEndsWith[i] + "\"!";
					assertTrue(msg, cur.endsWith(resultEndsWith[i]));
				}
			} // if no suffixes
		} // if locale == null
		else if(suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
		{
			// checking shortcut method (Class,String,Locale) without suffixes
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getLocalResources(\"").append(resourceBaseName).append("\", ");
			methCallBuff.append(locale).append(')');
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = resourcesupport.getLocalResources(resourceBaseName, locale);
			logResults(logger, methodCall, result);
			assertEquals(methodCall + " - Number of results", resultEndsWith.length, result.length);

			for(int i = 0; i < result.length; i++)
			{
				String cur = result[i].toString();
				String msg = methodCall + " - resultUrls[" + i + "]:\"" + cur + "\" does not end with \"" + resultEndsWith[i] + "\"!";
				assertTrue(msg, cur.endsWith(resultEndsWith[i]));
			}
		} // if no suffixes but locale
	}


	private void internalGetLocalResource(String resourceBaseName, String[] suffixes, Locale locale, String resultEndsWith)
	{
		// checking method with four arguments
		StringBuilder methCallBuff = new StringBuilder(200);
		methCallBuff.append("getLocalResource(\"").append(resourceBaseName).append("\", ");
		appendSuffixes(methCallBuff, suffixes);
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if(logger.isInfoEnabled()) logger.info("Testing {}.", methodCall);

		URL result;
		result = resourcesupport.getLocalResource(resourceBaseName, suffixes, locale);
		logResult(logger, methodCall, result);

		assertResultEndsWith(methodCall, result, resultEndsWith);

		if(locale == null)
		{
			// checking shortcut method (Class,String,String[]) without locale
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getLocalResource(\"").append(resourceBaseName).append("\", ");
			appendSuffixes(methCallBuff, suffixes);
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = resourcesupport.getLocalResource(resourceBaseName, suffixes);
			logResult(logger, methodCall, result);

			assertResultEndsWith(methodCall, result, resultEndsWith);

			if(suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
			{
				// checking shortcut method (Class,String) without suffixes and locale
				// results must be the same...
				methCallBuff = new StringBuilder();
				methCallBuff.append("getLocalResource(\"").append(resourceBaseName).append("\");");
				methodCall = methCallBuff.toString();
				if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
				result = resourcesupport.getLocalResource(resourceBaseName);
				logResult(logger, methodCall, result);

				assertResultEndsWith(methodCall, result, resultEndsWith);
			} // if no suffixes
		} // if locale == null
		else if(suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
		{
			// checking shortcut method (Class,String,Locale) without suffixes
			// results must be the same...
			methCallBuff = new StringBuilder(200);
			methCallBuff.append("getLocalResource(\"").append(resourceBaseName).append("\", ");
			methCallBuff.append(locale).append(')');
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = resourcesupport.getLocalResource(resourceBaseName, locale);
			logResult(logger, methodCall, result);

			assertResultEndsWith(methodCall, result, resultEndsWith);
		} // if no suffixes but locale
	}

	private void internalGetLocalResourceAsStream(String resourceBaseName, String[] suffixes, Locale locale, boolean found)
	{
		// checking method with four arguments
		StringBuilder methCallBuff = new StringBuilder(200);
		methCallBuff.append("getLocalResourceAsStream(\"").append(resourceBaseName).append("\", ");
		appendSuffixes(methCallBuff, suffixes);
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if(logger.isInfoEnabled()) logger.info("Testing {}.", methodCall);

		InputStream result;
		result = resourcesupport.getLocalResourceAsStream(resourceBaseName, suffixes, locale);
		logResult(logger, methodCall, result);

		if(result == null && found)
		{
			fail(methodCall + " - Expected result but was null!");
		}
		else if(result != null && !found)
		{
			fail(methodCall + " - Found a result but didn't expect one!");
		}

		if(locale == null)
		{
			// checking shortcut method (Class,String,String[]) without locale
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getLocalResourceAsStream(\"").append(resourceBaseName).append("\", ");
			appendSuffixes(methCallBuff, suffixes);
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = resourcesupport.getLocalResourceAsStream(resourceBaseName, suffixes);
			logResult(logger, methodCall, result);

			if(result == null && found)
			{
				fail(methodCall + " - Expected result but was null!");
			}
			else if(result != null && !found)
			{
				fail(methodCall + " - Found a result but didn't expect one!");
			}

			if(suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
			{
				// checking shortcut method (Class,String) without suffixes and locale
				// results must be the same...
				methCallBuff = new StringBuilder();
				methCallBuff.append("getLocalResourceAsStream(\"").append(resourceBaseName).append("\");");
				methodCall = methCallBuff.toString();
				if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
				result = resourcesupport.getLocalResourceAsStream(resourceBaseName);
				logResult(logger, methodCall, result);

				if(result == null && found)
				{
					fail(methodCall + " - Expected result but was null!");
				}
				else if(result != null && !found)
				{
					fail(methodCall + " - Found a result but didn't expect one!");
				}
			} // if no suffixes
		} // if locale == null
		else if(suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
		{
			// checking shortcut method (Class,String,Locale) without suffixes
			// results must be the same...
			methCallBuff = new StringBuilder(200);
			methCallBuff.append("getLocalResourceAsStream(\"").append(resourceBaseName).append("\", ");
			methCallBuff.append(locale).append(')');
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = resourcesupport.getLocalResourceAsStream(resourceBaseName, locale);
			logResult(logger, methodCall, result);

			if(result == null && found)
			{
				fail(methodCall + " - Expected result but was null!");
			}
			else if(result != null && !found)
			{
				fail(methodCall + " - Found a result but didn't expect one!");
			}
		} // if no suffixes but locale
	}

	private void internalGetResources(String resourceBaseName, String[] suffixes, Locale locale, String[] resultEndsWith)
	{
		// checking method with four arguments
		StringBuilder methCallBuff = new StringBuilder(200);
		methCallBuff.append("getResources(\"").append(resourceBaseName).append("\", ");
		appendSuffixes(methCallBuff, suffixes);
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if(logger.isInfoEnabled()) logger.info("Testing {}.", methodCall);

		URL[] result;
		result = resourcesupport.getResources(resourceBaseName, suffixes, locale);
		logResults(logger, methodCall, result);
		assertEquals(methodCall + " - Number of results", resultEndsWith.length, result.length);

		for(int i = 0; i < result.length; i++)
		{
			String cur = result[i].toString();
			String msg = methodCall + " - resultUrls[" + i + "]:\"" + cur + "\" does not end with \"" + resultEndsWith[i] + "\"!";
			assertTrue(msg, cur.endsWith(resultEndsWith[i]));
		}

		if(locale == null)
		{
			// checking shortcut method (Class,String,String[]) without locale
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getResources(\"").append(resourceBaseName).append("\", ");
			appendSuffixes(methCallBuff, suffixes);
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = resourcesupport.getResources(resourceBaseName, suffixes);
			logResults(logger, methodCall, result);
			assertEquals(methodCall + " - Number of results", resultEndsWith.length, result.length);

			for(int i = 0; i < result.length; i++)
			{
				String cur = result[i].toString();
				String msg = methodCall + " - resultUrls[" + i + "]:\"" + cur + "\" does not end with \"" + resultEndsWith[i] + "\"!";
				assertTrue(msg, cur.endsWith(resultEndsWith[i]));
			}
			if(suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
			{
				// checking shortcut method (Class,String) without suffixes and locale
				// results must be the same...
				methCallBuff = new StringBuilder();
				methCallBuff.append("getResources(\"").append(resourceBaseName).append("\");");
				methodCall = methCallBuff.toString();
				if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
				result = resourcesupport.getResources(resourceBaseName);
				logResults(logger, methodCall, result);
				assertEquals(methodCall + " - Number of results", resultEndsWith.length, result.length);

				for(int i = 0; i < result.length; i++)
				{
					String cur = result[i].toString();
					String msg = methodCall + " - resultUrls[" + i + "]:\"" + cur + "\" does not end with \"" + resultEndsWith[i] + "\"!";
					assertTrue(msg, cur.endsWith(resultEndsWith[i]));
				}
			} // if no suffixes
		} // if locale == null
		else if(suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
		{
			// checking shortcut method (Class,String,Locale) without suffixes
			// results must be the same...
			methCallBuff = new StringBuilder(200);
			methCallBuff.append("getResources(\"").append(resourceBaseName).append("\", ");
			methCallBuff.append(locale).append(')');
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = resourcesupport.getResources(resourceBaseName, locale);
			logResults(logger, methodCall, result);
			assertEquals(methodCall + " - Number of results", resultEndsWith.length, result.length);

			for(int i = 0; i < result.length; i++)
			{
				String cur = result[i].toString();
				String msg = methodCall + " - resultUrls[" + i + "]:\"" + cur + "\" does not end with \"" + resultEndsWith[i] + "\"!";
				assertTrue(msg, cur.endsWith(resultEndsWith[i]));
			}
		} // if no suffixes but locale
	}

	private void internalGetResource(String resourceBaseName, String[] suffixes, Locale locale, String resultEndsWith)
	{
		// checking method with four arguments
		StringBuilder methCallBuff = new StringBuilder(200);
		methCallBuff.append("getResource(\"").append(resourceBaseName).append("\", ");
		appendSuffixes(methCallBuff, suffixes);
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if(logger.isInfoEnabled()) logger.info("Testing {}.", methodCall);

		URL result;
		result = resourcesupport.getResource(resourceBaseName, suffixes, locale);
		logResult(logger, methodCall, result);

		assertResultEndsWith(methodCall, result, resultEndsWith);

		if(locale == null)
		{
			// checking shortcut method (Class,String,String[]) without locale
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getResource(\"").append(resourceBaseName).append("\", ");
			appendSuffixes(methCallBuff, suffixes);
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = resourcesupport.getResource(resourceBaseName, suffixes);
			logResult(logger, methodCall, result);

			assertResultEndsWith(methodCall, result, resultEndsWith);

			if(suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
			{
				// checking shortcut method (Class,String) without suffixes and locale
				// results must be the same...
				methCallBuff = new StringBuilder();
				methCallBuff.append("getResource(\"").append(resourceBaseName).append("\");");
				methodCall = methCallBuff.toString();
				if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
				result = resourcesupport.getResource(resourceBaseName);
				logResult(logger, methodCall, result);

				assertResultEndsWith(methodCall, result, resultEndsWith);
			} // if no suffixes
		} // if locale == null
		else if(suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
		{
			// checking shortcut method (Class,String,Locale) without suffixes
			// results must be the same...
			methCallBuff = new StringBuilder(200);
			methCallBuff.append("getResource(\"").append(resourceBaseName).append("\", ");
			methCallBuff.append(locale).append(')');
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = resourcesupport.getResource(resourceBaseName, locale);
			logResult(logger, methodCall, result);

			assertResultEndsWith(methodCall, result, resultEndsWith);
		} // if no suffixes but locale
	}

	private void internalGetResourceAsStream(String resourceBaseName, String[] suffixes, Locale locale, boolean found)
	{
		// checking method with four arguments
		StringBuilder methCallBuff = new StringBuilder(200);
		methCallBuff.append("getResourceAsStream(\"").append(resourceBaseName).append("\", ");
		appendSuffixes(methCallBuff, suffixes);
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if(logger.isInfoEnabled()) logger.info("Testing {}.", methodCall);

		InputStream result;
		result = resourcesupport.getResourceAsStream(resourceBaseName, suffixes, locale);
		logResult(logger, methodCall, result);

		if(result == null && found)
		{
			fail(methodCall + " - Expected result but was null!");
		}
		else if(result != null && !found)
		{
			fail(methodCall + " - Found a result but didn't expect one!");
		}

		if(locale == null)
		{
			// checking shortcut method (Class,String,String[]) without locale
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getResourceAsStream(\"").append(resourceBaseName).append("\", ");
			appendSuffixes(methCallBuff, suffixes);
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = resourcesupport.getResourceAsStream(resourceBaseName, suffixes);
			logResult(logger, methodCall, result);

			if(result == null && found)
			{
				fail(methodCall + " - Expected result but was null!");
			}
			else if(result != null && !found)
			{
				fail(methodCall + " - Found a result but didn't expect one!");
			}

			if(suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
			{
				// checking shortcut method (Class,String) without suffixes and locale
				// results must be the same...
				methCallBuff = new StringBuilder();
				methCallBuff.append("getResourceAsStream(\"").append(resourceBaseName).append("\");");
				methodCall = methCallBuff.toString();
				if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
				result = resourcesupport.getResourceAsStream(resourceBaseName);
				logResult(logger, methodCall, result);

				if(result == null && found)
				{
					fail(methodCall + " - Expected result but was null!");
				}
				else if(result != null && !found)
				{
					fail(methodCall + " - Found a result but didn't expect one!");
				}
			} // if no suffixes
		} // if locale == null
		else if(suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
		{
			// checking shortcut method (Class,String,Locale) without suffixes
			// results must be the same...
			methCallBuff = new StringBuilder(200);
			methCallBuff.append("getResourceAsStream(\"").append(resourceBaseName).append("\", ");
			methCallBuff.append(locale).append(')');
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = resourcesupport.getResourceAsStream(resourceBaseName, locale);
			logResult(logger, methodCall, result);

			if(result == null && found)
			{
				fail(methodCall + " - Expected result but was null!");
			}
			else if(result != null && !found)
			{
				fail(methodCall + " - Found a result but didn't expect one!");
			}
		} // if no suffixes but locale
	}

	private void internalGetResourceMap(@SuppressWarnings("SameParameterValue") String resourceBaseName, Locale locale, String[][] expectedResults)
	{
		Map<String, Object> result;
		result = resourcesupport.getResourceMap(resourceBaseName, locale);
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
			result = resourcesupport.getResourceMap(resourceBaseName);
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

	private void internalGetLocalResourceMap(@SuppressWarnings("SameParameterValue") String resourceBaseName, Locale locale, String[][] expectedResults)
	{
		Map result;
		result = resourcesupport.getLocalResourceMap(resourceBaseName, locale);
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
			result = resourcesupport.getLocalResourceMap(resourceBaseName);
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


	private void internalResolveLocale(Locale param, Locale expected)
	{
		Locale result = resourcesupport.resolveLocale(param);
		assertEquals(expected, result);
	}

	private void internalGetResourceClass(Class expectedClass)
	{
		Class c = resourcesupport.getResourceClass();
		assertEquals(expectedClass, c);
	}

	private void internalGetResourceObject(Object expectedObject)
	{
		Object o = resourcesupport.getResourceObject();
		assertEquals(expectedObject, o);
	}

	private void internalGetLocalizable(Object expectedObject, boolean proxy)
	{
		Object o = resourcesupport.getLocalizable();
		if(proxy)
		{
			assertNotNull("Expected proxy but result was null!", o);

			Class c = o.getClass();
			String name = c.getName();
			assertTrue("Expected proxy but got " + name + " instead.", name.contains("$Proxy"));
		}
		else
		{
			assertEquals(expectedObject, o);
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

	@Before
	public void initInstance()
	{
		resourcesupport = new ResourceSupport(new Foobar());
	}

	@Test(expected = IllegalArgumentException.class)
	public void constructor()
	{
		resourcesupport = new ResourceSupport(null);
	}

	@Test
	public void resolveLocale()
		throws Exception
	{
		Localizable l = resourcesupport.getLocalizable();
		internalResolveLocale(GERMANY_LOCALE, GERMANY_LOCALE);
		internalResolveLocale(null, null);
		l.setLocale(HESSIAN_LOCALE);
		internalResolveLocale(GERMANY_LOCALE, HESSIAN_LOCALE);
		internalResolveLocale(null, HESSIAN_LOCALE);
	}

	@Test
	public void getResourceClass()
		throws Exception
	{
		internalGetResourceClass(Foobar.class);

		resourcesupport = new ResourceSupport(Foobar.StaticInternal.class);
		internalGetResourceClass(Foobar.StaticInternal.class);

		Foobar tc = new Foobar();
		Foobar.Internal internal = tc.new Internal();
		resourcesupport = new ResourceSupport(internal);
		internalGetResourceClass(Foobar.Internal.class);
	}

	@Test
	public void getResourceObject()
		throws Exception
	{
		resourcesupport = new ResourceSupport(Foobar.class);
		internalGetResourceObject(null);

		Foobar tc = new Foobar();
		resourcesupport = new ResourceSupport(tc);
		internalGetResourceObject(tc);

		Foobar.Internal internal = tc.new Internal();
		resourcesupport = new ResourceSupport(internal);
		internalGetResourceObject(internal);
	}

	@Test
	public void getLocalizable()
		throws Exception
	{
		resourcesupport = new ResourceSupport(Foobar.class);
		internalGetLocalizable(null, false);

		Object o = new Foobar();
		resourcesupport = new ResourceSupport(o);
		internalGetLocalizable(o, false);

		o = ((Foobar) o).new Internal();
		resourcesupport = new ResourceSupport(o);
		internalGetLocalizable(o, true);

		Locale l;
		Localizable loc;

		// implements Localizable
		o = new Foobar();
		resourcesupport = new ResourceSupport(o);
		loc = resourcesupport.getLocalizable();
		loc.setLocale(HESSIAN_LOCALE);
		l = loc.getLocale();
		assertEquals(HESSIAN_LOCALE, l);

		// getter only
		o = ((Foobar) o).new Internal();
		resourcesupport = new ResourceSupport(o);
		loc = resourcesupport.getLocalizable();
		loc.setLocale(null);
		l = loc.getLocale();
		assertEquals(HESSIAN_LOCALE, l);

		// getter and setter
		o = new Foobar.StaticInternal();
		resourcesupport = new ResourceSupport(o);
		loc = resourcesupport.getLocalizable();
		loc.setLocale(HESSIAN_LOCALE);
		l = loc.getLocale();
		assertEquals(HESSIAN_LOCALE, l);

		// neither getter nor setter
		o = new Foobar.StaticInternal.Foo();
		resourcesupport = new ResourceSupport(o);
		loc = resourcesupport.getLocalizable();
		loc.setLocale(HESSIAN_LOCALE);
		l = loc.getLocale();
		assertEquals(null, l);
	}

	@Test
	public void getLocalResources()
		throws Exception
	{
		// de.huxhorn.sulky.resources.junit.Foobar.class;
		internalGetLocalResources("defaultFallback.txt", null, GERMANY_LOCALE, new String[]{}); // not anymore
		internalGetLocalResources("defaultFallback", new String[]{".txt", ".html"}, GERMANY_LOCALE, new String[]{}); // not anymore
		internalGetLocalResources("test.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/test.txt",
		});

		// checking the different shortcuts...
		internalGetLocalResources("overloaded.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/overloaded.txt",
		});
		internalGetLocalResources("overloaded.txt", null, null, new String[]{
			"Foobar/overloaded.txt",
		});
		internalGetLocalResources("overloaded.txt", new String[]{}, null, new String[]{
			"Foobar/overloaded.txt",
		});
		internalGetLocalResources("overloaded.txt", new String[]{""}, null, new String[]{
			"Foobar/overloaded.txt",
		});
		internalGetLocalResources("overloaded", new String[]{".txt"}, null, new String[]{
			"Foobar/overloaded.txt",
		});

		// checking for links
		internalGetLocalResources("link.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/test.txt",
		});
		internalGetLocalResources("cyclic1", null, GERMANY_LOCALE, new String[]{});
		internalGetLocalResources("cyclicFallback1", null, GERMANY_LOCALE, new String[]{
			"Foobar/cyclicFallback1",
		});

		// checking internal class handling
		resourcesupport = new ResourceSupport(Foobar.Internal.class);
		internalGetLocalResources("test.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/Internal/test.txt",
		});

		resourcesupport = new ResourceSupport(Foobar.StaticInternal.class);
		internalGetLocalResources("test.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/StaticInternal/de/test.txt",
			"Foobar/StaticInternal/test.txt",
		});
		resourcesupport = new ResourceSupport(Foobar.StaticInternal.Foo.class);
		internalGetLocalResources("test.txt", null, GERMANY_LOCALE, new String[]{});
	}

	@Test
	public void getLocalResource()
		throws Exception
	{
		// de.huxhorn.sulky.resources.junit.Foobar.class;
		internalGetLocalResource("defaultFallback.txt", null, GERMANY_LOCALE, null); // not anymore
		internalGetLocalResource("defaultFallback", new String[]{".txt", ".html"}, GERMANY_LOCALE, null); // not anymore
		internalGetLocalResource("test.txt", null, GERMANY_LOCALE, "Foobar/test.txt");

		// checking the different shortcuts...
		internalGetLocalResource("overloaded.txt", null, GERMANY_LOCALE, "Foobar/overloaded.txt");
		internalGetLocalResource("overloaded.txt", null, null, "Foobar/overloaded.txt");
		internalGetLocalResource("overloaded.txt", new String[]{}, null, "Foobar/overloaded.txt");
		internalGetLocalResource("overloaded.txt", new String[]{""}, null, "Foobar/overloaded.txt");
		internalGetLocalResource("overloaded", new String[]{".txt"}, null, "Foobar/overloaded.txt");

		// checking for links
		internalGetLocalResource("link.txt", null, GERMANY_LOCALE, "Foobar/test.txt");
		internalGetLocalResource("cyclic1", null, GERMANY_LOCALE, null);
		internalGetLocalResource("cyclicFallback1", null, GERMANY_LOCALE, "Foobar/cyclicFallback1");

		// checking internal class handling
		resourcesupport = new ResourceSupport(Foobar.Internal.class);
		internalGetLocalResource("test.txt", null, GERMANY_LOCALE, "Foobar/Internal/test.txt");

		resourcesupport = new ResourceSupport(Foobar.StaticInternal.class);
		internalGetLocalResource("test.txt", null, GERMANY_LOCALE, "Foobar/StaticInternal/de/test.txt");

		resourcesupport = new ResourceSupport(Foobar.StaticInternal.Foo.class);
		internalGetLocalResource("test.txt", null, GERMANY_LOCALE, null);
	}

	@Test
	public void getLocalResourceAsStream()
		throws Exception
	{
		// de.huxhorn.sulky.resources.junit.Foobar.class;
		internalGetLocalResourceAsStream("defaultFallback.txt", null, GERMANY_LOCALE, false); // not anymore
		internalGetLocalResourceAsStream("defaultFallback", new String[]{".txt", ".html"}, GERMANY_LOCALE, false); // not anymore
		internalGetLocalResourceAsStream("test.txt", null, GERMANY_LOCALE, true);

		// checking the different shortcuts...
		internalGetLocalResourceAsStream("overloaded.txt", null, GERMANY_LOCALE, true);
		internalGetLocalResourceAsStream("overloaded.txt", null, null, true);
		internalGetLocalResourceAsStream("overloaded.txt", new String[]{}, null, true);
		internalGetLocalResourceAsStream("overloaded.txt", new String[]{""}, null, true);
		internalGetLocalResourceAsStream("overloaded", new String[]{".txt"}, null, true);

		// checking for links
		internalGetLocalResourceAsStream("link.txt", null, GERMANY_LOCALE, true);
		internalGetLocalResourceAsStream("cyclic1", null, GERMANY_LOCALE, false);
		internalGetLocalResourceAsStream("cyclicFallback1", null, GERMANY_LOCALE, true);

		// checking internal class handling
		resourcesupport = new ResourceSupport(Foobar.Internal.class);
		internalGetLocalResourceAsStream("test.txt", null, GERMANY_LOCALE, true);

		resourcesupport = new ResourceSupport(Foobar.StaticInternal.class);
		internalGetLocalResourceAsStream("test.txt", null, GERMANY_LOCALE, true);

		resourcesupport = new ResourceSupport(Foobar.StaticInternal.Foo.class);
		internalGetLocalResourceAsStream("test.txt", null, GERMANY_LOCALE, false);
	}

	@Test
	public void getResources()
		throws Exception
	{
		// de.huxhorn.sulky.resources.junit.Foobar.class;
		internalGetResources("defaultFallback.txt", null, GERMANY_LOCALE, new String[]{}); // not anymore
		internalGetResources("defaultFallback", new String[]{".txt", ".html"}, GERMANY_LOCALE, new String[]{}); // not anymore
		internalGetResources("test.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/test.txt",
		});

		// checking the different shortcuts...
		internalGetResources("overloaded.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/overloaded.txt",
			"BaseClass/overloaded.txt",
		});
		internalGetResources("overloaded.txt", null, null, new String[]{
			"Foobar/overloaded.txt",
			"BaseClass/overloaded.txt",
		});
		internalGetResources("overloaded.txt", new String[]{}, null, new String[]{
			"Foobar/overloaded.txt",
			"BaseClass/overloaded.txt",
		});
		internalGetResources("overloaded.txt", new String[]{""}, null, new String[]{
			"Foobar/overloaded.txt",
			"BaseClass/overloaded.txt",
		});
		internalGetResources("overloaded", new String[]{".txt"}, null, new String[]{
			"Foobar/overloaded.txt",
			"BaseClass/overloaded.txt",
		});

		// checking for links
		internalGetResources("link.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/test.txt",
		});
		internalGetResources("cyclic1", null, GERMANY_LOCALE, new String[]{});
		internalGetResources("cyclicFallback1", null, GERMANY_LOCALE, new String[]{
			"Foobar/cyclicFallback1",
		});

		// checking internal class handling
		resourcesupport = new ResourceSupport(Foobar.Internal.class);
		internalGetResources("test.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/Internal/test.txt",
			"Foobar/test.txt",
		});

		resourcesupport = new ResourceSupport(Foobar.StaticInternal.class);
		internalGetResources("test.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/StaticInternal/de/test.txt",
			"Foobar/StaticInternal/test.txt",
			"Foobar/test.txt",
		});
		resourcesupport = new ResourceSupport(Foobar.StaticInternal.Foo.class);
		internalGetResources("test.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/StaticInternal/de/test.txt",
			"Foobar/StaticInternal/test.txt",
			"Foobar/test.txt",
		});

		// TODO: #######################################################################
		resourcesupport = new ResourceSupport(new Foobar());
		internalGetResources("locale.txt", null, null, new String[]{
			"Foobar/en_US/locale.txt",
			"Foobar/en/locale.txt",
			"Foobar/locale.txt",
		});

		internalGetResources("locale.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/de_DE/locale.txt",
			"Foobar/de/locale.txt",
			"Foobar/en_US/locale.txt",
			"Foobar/en/locale.txt",
			"Foobar/locale.txt",
		});
		Localizable l = resourcesupport.getLocalizable();
		l.setLocale(HESSIAN_LOCALE);
		internalGetResources("locale.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/de_DE_hessisch/locale.txt",
			"Foobar/de_DE/locale.txt",
			"Foobar/de/locale.txt",
			"Foobar/en_US/locale.txt",
			"Foobar/en/locale.txt",
			"Foobar/locale.txt",
		});
		internalGetResources("locale", new String[]{".html", ".txt"}, GERMANY_LOCALE, new String[]{
			"Foobar/de_DE_hessisch/locale.txt",
			"Foobar/de_DE/locale.html",
			"Foobar/de_DE/locale.txt",
			"Foobar/de/locale.html",
			"Foobar/de/locale.txt",
			"Foobar/en_US/locale.txt",
			"Foobar/en/locale.txt",
			"Foobar/locale.txt",
		});
	}

	@Test
	public void getResource()
		throws Exception
	{
		// de.huxhorn.sulky.resources.junit.Foobar.class;
		internalGetResource("defaultFallback.txt", null, GERMANY_LOCALE, null); // not anymore
		internalGetResource("defaultFallback", new String[]{".txt", ".html"}, GERMANY_LOCALE, null); // not anymore
		internalGetResource("test.txt", null, GERMANY_LOCALE, "Foobar/test.txt");

		// checking the different shortcuts...
		internalGetResource("overloaded.txt", null, GERMANY_LOCALE, "Foobar/overloaded.txt");
		internalGetResource("overloaded.txt", null, null, "Foobar/overloaded.txt");
		internalGetResource("overloaded.txt", new String[]{}, null, "Foobar/overloaded.txt");
		internalGetResource("overloaded.txt", new String[]{""}, null, "Foobar/overloaded.txt");
		internalGetResource("overloaded", new String[]{".txt"}, null, "Foobar/overloaded.txt");

		// checking for links
		internalGetResource("link.txt", null, GERMANY_LOCALE, "Foobar/test.txt");
		internalGetResource("cyclic1", null, GERMANY_LOCALE, null);
		internalGetResource("cyclicFallback1", null, GERMANY_LOCALE, "Foobar/cyclicFallback1");

		// checking internal class handling
		resourcesupport = new ResourceSupport(Foobar.Internal.class);
		internalGetResource("test.txt", null, GERMANY_LOCALE, "Foobar/Internal/test.txt");

		resourcesupport = new ResourceSupport(Foobar.StaticInternal.class);
		internalGetResource("test.txt", null, GERMANY_LOCALE, "Foobar/StaticInternal/de/test.txt");

		resourcesupport = new ResourceSupport(Foobar.StaticInternal.Foo.class);
		internalGetResource("test.txt", null, GERMANY_LOCALE, "Foobar/StaticInternal/de/test.txt");
	}

	@Test
	public void getResourceAsStream()
		throws Exception
	{
		// de.huxhorn.sulky.resources.junit.Foobar.class;
		internalGetResourceAsStream("defaultFallback.txt", null, GERMANY_LOCALE, false); // not anymore
		internalGetResourceAsStream("defaultFallback", new String[]{".txt", ".html"}, GERMANY_LOCALE, false); // not anymore
		internalGetResourceAsStream("test.txt", null, GERMANY_LOCALE, true);

		// checking the different shortcuts...
		internalGetResourceAsStream("overloaded.txt", null, GERMANY_LOCALE, true);
		internalGetResourceAsStream("overloaded.txt", null, null, true);
		internalGetResourceAsStream("overloaded.txt", new String[]{}, null, true);
		internalGetResourceAsStream("overloaded.txt", new String[]{""}, null, true);
		internalGetResourceAsStream("overloaded", new String[]{".txt"}, null, true);

		// checking for links
		internalGetResourceAsStream("link.txt", null, GERMANY_LOCALE, true);
		internalGetResourceAsStream("cyclic1", null, GERMANY_LOCALE, false);
		internalGetResourceAsStream("cyclicFallback1", null, GERMANY_LOCALE, true);

		// checking internal class handling
		resourcesupport = new ResourceSupport(Foobar.Internal.class);
		internalGetResourceAsStream("test.txt", null, GERMANY_LOCALE, true);

		resourcesupport = new ResourceSupport(Foobar.StaticInternal.class);
		internalGetResourceAsStream("test.txt", null, GERMANY_LOCALE, true);

		resourcesupport = new ResourceSupport(Foobar.StaticInternal.Foo.class);
		internalGetResourceAsStream("test.txt", null, GERMANY_LOCALE, true);
	}

	@Test
	public void getLocalResourceMap()
		throws Exception
	{
		// Foobar.class;
		internalGetLocalResourceMap("resources", HESSIAN_LOCALE, new String[][]{
			{"attention.txt", "Uffbasse!"},
			{"ok.txt", "OK"},
			{"cancel.txt", "Abbruch"},
		});
		internalGetLocalResourceMap("resources", GERMANY_LOCALE, new String[][]{
			{"attention.txt", "Achtung!"},
			{"ok.txt", "OK"},
			{"cancel.txt", "Abbruch"},
		});
		internalGetLocalResourceMap("resources", null, new String[][]{
			{"attention.txt", "Attention!"},
			{"ok.txt", "OK"},
			{"cancel.txt", "Cancel"},
		});

		resourcesupport = new ResourceSupport(Foobar.class.getSuperclass());
		internalGetLocalResourceMap("resources", null, new String[][]{
			{"base.txt", "BaseClass"},
		});
	}

	@Test
	public void getResourceMap()
		throws Exception
	{
		// Foobar.class;
		internalGetResourceMap("resources", HESSIAN_LOCALE, new String[][]{
			{"attention.txt", "Uffbasse!"},
			{"ok.txt", "OK"},
			{"cancel.txt", "Abbruch"},
			{"base.txt", "BaseClass"},
		});
		internalGetResourceMap("resources", GERMANY_LOCALE, new String[][]{
			{"attention.txt", "Achtung!"},
			{"ok.txt", "OK"},
			{"cancel.txt", "Abbruch"},
			{"base.txt", "BaseClass"},
		});
		internalGetResourceMap("resources", null, new String[][]{
			{"attention.txt", "Attention!"},
			{"ok.txt", "OK"},
			{"cancel.txt", "Cancel"},
			{"base.txt", "BaseClass"},
		});

		resourcesupport = new ResourceSupport(Foobar.class.getSuperclass());
		internalGetResourceMap("resources", null, new String[][]{
			{"base.txt", "BaseClass"},
		});
	}
}
