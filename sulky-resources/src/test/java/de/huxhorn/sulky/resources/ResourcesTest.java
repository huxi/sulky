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
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static de.huxhorn.sulky.resources.ResourceTestHelper.assertResultEndsWith;
import static de.huxhorn.sulky.resources.ResourceTestHelper.logResult;
import static de.huxhorn.sulky.resources.ResourceTestHelper.logResults;
import static de.huxhorn.sulky.resources.ResourceTestHelper.appendSuffixes;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@SuppressWarnings("PMD.CloseResource")
public class ResourcesTest
{
	private final Logger logger = LoggerFactory.getLogger(ResourcesTest.class);

	private static final Locale US_LOCALE = Locale.of("en", "US");
	private static final Locale GERMANY_LOCALE = Locale.of("de", "DE");
	private static final Locale HESSIAN_LOCALE = Locale.of("de", "DE", "hessisch");
	private static final Locale EMPTY_LOCALE = Locale.of("", "", "");
	private static Locale prevDefault;

	private void internalGetLocalResources(Class clazz, String resourceBaseName, String[] suffixes, Locale locale, String[] resultEndsWith)
	{

		// checking method with four arguments
		StringBuilder methCallBuff = new StringBuilder(200);
		methCallBuff.append("getLocalResources(").append(clazz.getName()).append(", \"").append(resourceBaseName)
			.append("\", ");
		appendSuffixes(methCallBuff, suffixes);
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if(logger.isInfoEnabled()) logger.info("Testing {}.", methodCall);

		URL[] result;
		result = Resources.getLocalResources(clazz, resourceBaseName, suffixes, locale);
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
			methCallBuff.append("getLocalResources(").append(clazz.getName()).append(", \"").append(resourceBaseName)
				.append("\", ");
			appendSuffixes(methCallBuff, suffixes);
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getLocalResources(clazz, resourceBaseName, suffixes);
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
				methCallBuff.append("getLocalResources(").append(clazz.getName()).append(", \"")
					.append(resourceBaseName).append("\");");
				methodCall = methCallBuff.toString();
				if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
				result = Resources.getLocalResources(clazz, resourceBaseName);
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
			methCallBuff.append("getLocalResources(").append(clazz.getName()).append(", \"").append(resourceBaseName)
				.append("\", ");
			methCallBuff.append(locale).append(')');
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getLocalResources(clazz, resourceBaseName, locale);
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

	private void internalGetLocalResource(Class clazz, String resourceBaseName, String[] suffixes, Locale locale, String resultEndsWith)
	{
		// checking method with four arguments
		StringBuilder methCallBuff = new StringBuilder(200);
		methCallBuff.append("getLocalResource(").append(clazz.getName()).append(", \"").append(resourceBaseName)
			.append("\", ");
		appendSuffixes(methCallBuff, suffixes);
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if(logger.isInfoEnabled()) logger.info("Testing {}.", methodCall);

		URL result;
		result = Resources.getLocalResource(clazz, resourceBaseName, suffixes, locale);
		logResult(logger, methodCall, result);

		assertResultEndsWith(methodCall, result, resultEndsWith);

		if(locale == null)
		{
			// checking shortcut method (Class,String,String[]) without locale
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getLocalResource(").append(clazz.getName()).append(", \"").append(resourceBaseName)
				.append("\", ");
			appendSuffixes(methCallBuff, suffixes);
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getLocalResource(clazz, resourceBaseName, suffixes);
			logResult(logger, methodCall, result);

			assertResultEndsWith(methodCall, result, resultEndsWith);

			if(suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
			{
				// checking shortcut method (Class,String) without suffixes and locale
				// results must be the same...
				methCallBuff = new StringBuilder();
				methCallBuff.append("getLocalResource(").append(clazz.getName()).append(", \"").append(resourceBaseName)
					.append("\");");
				methodCall = methCallBuff.toString();
				if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
				result = Resources.getLocalResource(clazz, resourceBaseName);
				logResult(logger, methodCall, result);

				assertResultEndsWith(methodCall, result, resultEndsWith);
			} // if no suffixes
		} // if locale == null
		else if(suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
		{
			// checking shortcut method (Class,String,Locale) without suffixes
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getLocalResource(").append(clazz.getName()).append(", \"").append(resourceBaseName)
				.append("\", ");
			methCallBuff.append(locale).append(')');
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getLocalResource(clazz, resourceBaseName, locale);
			logResult(logger, methodCall, result);

			assertResultEndsWith(methodCall, result, resultEndsWith);
		} // if no suffixes but locale
	}

	private void internalGetLocalResourceAsStream(Class clazz, String resourceBaseName, String[] suffixes, Locale locale, boolean found)
	{
		// checking method with four arguments
		StringBuilder methCallBuff = new StringBuilder(200);
		methCallBuff.append("getLocalResourceAsStream(").append(clazz.getName()).append(", \"").append(resourceBaseName)
			.append("\", ");
		appendSuffixes(methCallBuff, suffixes);
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if(logger.isInfoEnabled()) logger.info("Testing {}.", methodCall);

		InputStream result;
		result = Resources.getLocalResourceAsStream(clazz, resourceBaseName, suffixes, locale);
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
			methCallBuff.append("getLocalResourceAsStream(").append(clazz.getName()).append(", \"")
				.append(resourceBaseName).append("\", ");
			appendSuffixes(methCallBuff, suffixes);
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getLocalResourceAsStream(clazz, resourceBaseName, suffixes);
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
				methCallBuff.append("getLocalResourceAsStream(").append(clazz.getName()).append(", \"")
					.append(resourceBaseName).append("\");");
				methodCall = methCallBuff.toString();
				if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
				result = Resources.getLocalResourceAsStream(clazz, resourceBaseName);
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
			methCallBuff.append("getLocalResourceAsStream(").append(clazz.getName()).append(", \"")
				.append(resourceBaseName).append("\", ");
			methCallBuff.append(locale).append(')');
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getLocalResourceAsStream(clazz, resourceBaseName, locale);
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

	private void internalGetResources(Class clazz, String resourceBaseName, String[] suffixes, Locale locale, String[] resultEndsWith)
	{
		// checking method with four arguments
		StringBuilder methCallBuff = new StringBuilder(200);
		methCallBuff.append("getResources(").append(clazz.getName()).append(", \"").append(resourceBaseName)
			.append("\", ");
		appendSuffixes(methCallBuff, suffixes);
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if(logger.isInfoEnabled()) logger.info("Testing {}.", methodCall);

		URL[] result;
		result = Resources.getResources(clazz, resourceBaseName, suffixes, locale);
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
			methCallBuff.append("getResources(").append(clazz.getName()).append(", \"").append(resourceBaseName)
				.append("\", ");
			appendSuffixes(methCallBuff, suffixes);
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getResources(clazz, resourceBaseName, suffixes);
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
				methCallBuff.append("getResources(").append(clazz.getName()).append(", \"").append(resourceBaseName)
					.append("\");");
				methodCall = methCallBuff.toString();
				if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
				result = Resources.getResources(clazz, resourceBaseName);
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
			methCallBuff.append("getResources(").append(clazz.getName()).append(", \"").append(resourceBaseName)
				.append("\", ");
			methCallBuff.append(locale).append(')');
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getResources(clazz, resourceBaseName, locale);
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

	private void internalGetResource(Class clazz, String resourceBaseName, String[] suffixes, Locale locale, String resultEndsWith)
	{
		// checking method with four arguments
		StringBuilder methCallBuff = new StringBuilder(200);
		methCallBuff.append("getResource(").append(clazz.getName()).append(", \"").append(resourceBaseName)
			.append("\", ");
		appendSuffixes(methCallBuff, suffixes);
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if(logger.isInfoEnabled()) logger.info("Testing {}.", methodCall);

		URL result;
		result = Resources.getResource(clazz, resourceBaseName, suffixes, locale);
		logResult(logger, methodCall, result);

		assertResultEndsWith(methodCall, result, resultEndsWith);

		if(locale == null)
		{
			// checking shortcut method (Class,String,String[]) without locale
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getResource(").append(clazz.getName()).append(", \"").append(resourceBaseName)
				.append("\", ");
			appendSuffixes(methCallBuff, suffixes);
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getResource(clazz, resourceBaseName, suffixes);
			logResult(logger, methodCall, result);

			assertResultEndsWith(methodCall, result, resultEndsWith);

			if(suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
			{
				// checking shortcut method (Class,String) without suffixes and locale
				// results must be the same...
				methCallBuff = new StringBuilder();
				methCallBuff.append("getResource(").append(clazz.getName()).append(", \"").append(resourceBaseName)
					.append("\");");
				methodCall = methCallBuff.toString();
				if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
				result = Resources.getResource(clazz, resourceBaseName);
				logResult(logger, methodCall, result);

				assertResultEndsWith(methodCall, result, resultEndsWith);
			} // if no suffixes
		} // if locale == null
		else if(suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
		{
			// checking shortcut method (Class,String,Locale) without suffixes
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getResource(").append(clazz.getName()).append(", \"").append(resourceBaseName)
				.append("\", ");
			methCallBuff.append(locale).append(')');
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getResource(clazz, resourceBaseName, locale);
			logResult(logger, methodCall, result);

			assertResultEndsWith(methodCall, result, resultEndsWith);
		} // if no suffixes but locale
	}

	private void internalGetResourceAsStream(Class clazz, String resourceBaseName, String[] suffixes, Locale locale, boolean found)
	{
		// checking method with four arguments
		StringBuilder methCallBuff = new StringBuilder(200);
		methCallBuff.append("getResourceAsStream(").append(clazz.getName()).append(", \"").append(resourceBaseName)
			.append("\", ");
		appendSuffixes(methCallBuff, suffixes);
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if(logger.isInfoEnabled()) logger.info("Testing {}.", methodCall);

		InputStream result;
		result = Resources.getResourceAsStream(clazz, resourceBaseName, suffixes, locale);
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
			methCallBuff.append("getResourceAsStream(").append(clazz.getName()).append(", \"").append(resourceBaseName)
				.append("\", ");
			appendSuffixes(methCallBuff, suffixes);
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getResourceAsStream(clazz, resourceBaseName, suffixes);
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
				methCallBuff.append("getResourceAsStream(").append(clazz.getName()).append(", \"")
					.append(resourceBaseName).append("\");");
				methodCall = methCallBuff.toString();
				if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
				result = Resources.getResourceAsStream(clazz, resourceBaseName);
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
			methCallBuff.append("getResourceAsStream(").append(clazz.getName()).append(", \"").append(resourceBaseName)
				.append("\", ");
			methCallBuff.append(locale).append(')');
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getResourceAsStream(clazz, resourceBaseName, locale);
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

	private void internalGetLocaleSuffixArray(Locale locale, String[] expectedResults)
	{
		String methodCall = "getLocaleSuffixArray(" + locale + ")";
		String[] result = Resources.getLocaleSuffixArray(locale);
		assertEquals(methodCall + " - Number of Suffixes", expectedResults.length, result.length);
		for(int i = 0; i < expectedResults.length; i++)
		{
			assertEquals(methodCall + " - Wrong suffix at index #" + i, expectedResults[i], result[i]);
		}
	}

	private void internalGetSingleLocaleSuffixArray(Locale locale, String[] expectedResults)
	{
		String methodCall = "getSingleLocaleSuffixArray(" + locale + ")";
		String[] result = Resources.getSingleLocaleSuffixArray(locale);
		assertEquals(methodCall + " - Number of Suffixes", expectedResults.length, result.length);
		for(int i = 0; i < expectedResults.length; i++)
		{
			assertEquals(methodCall + " - Wrong suffix at index #" + i, expectedResults[i], result[i]);
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
	public void getLocalResources()
		throws Exception
	{
		Class c;

		c = Foobar.class;
		internalGetLocalResources(c, "defaultFallback.txt", null, GERMANY_LOCALE, new String[]{}); // not anymore
		internalGetLocalResources(c, "defaultFallback", new String[]{".txt", ".html"}, GERMANY_LOCALE, new String[]{}); // not anymore
		internalGetLocalResources(c, "................../underflow.txt", null, GERMANY_LOCALE, new String[]{});

		internalGetLocalResources(c, "test.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/test.txt",
		});

		// checking the different shortcuts...
		internalGetLocalResources(c, "overloaded.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/overloaded.txt",
		});
		internalGetLocalResources(c, "overloaded.txt", null, null, new String[]{
			"Foobar/overloaded.txt",
		});
		internalGetLocalResources(c, "overloaded.txt", new String[]{}, null, new String[]{
			"Foobar/overloaded.txt",
		});
		internalGetLocalResources(c, "overloaded.txt", new String[]{""}, null, new String[]{
			"Foobar/overloaded.txt",
		});
		internalGetLocalResources(c, "overloaded", new String[]{".txt"}, null, new String[]{
			"Foobar/overloaded.txt",
		});

		// checking for links
		internalGetLocalResources(c, "link.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/test.txt",
		});
		internalGetLocalResources(c, "cyclic1", null, GERMANY_LOCALE, new String[]{});
		internalGetLocalResources(c, "cyclicFallback1", null, GERMANY_LOCALE, new String[]{
			"Foobar/cyclicFallback1",
		});

		// checking internal class handling
		c = Foobar.Internal.class;
		internalGetLocalResources(c, "test.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/Internal/test.txt",
		});

		c = Foobar.StaticInternal.class;
		internalGetLocalResources(c, "test.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/StaticInternal/de/test.txt",
			"Foobar/StaticInternal/test.txt",
		});
		c = Foobar.StaticInternal.Foo.class;
		internalGetLocalResources(c, "test.txt", null, GERMANY_LOCALE, new String[]{});
	}

	@Test
	public void getLocalResource()
		throws Exception
	{
		Class c;

		c = Foobar.class;
		internalGetLocalResource(c, "defaultFallback.txt", null, GERMANY_LOCALE, null); // not anymore
		internalGetLocalResource(c, "defaultFallback", new String[]{".txt", ".html"}, GERMANY_LOCALE, null); // not anymore
		internalGetLocalResource(c, "test.txt", null, GERMANY_LOCALE, "Foobar/test.txt");

		// checking the different shortcuts...
		internalGetLocalResource(c, "overloaded.txt", null, GERMANY_LOCALE, "Foobar/overloaded.txt");
		internalGetLocalResource(c, "overloaded.txt", null, null, "Foobar/overloaded.txt");
		internalGetLocalResource(c, "overloaded.txt", new String[]{}, null, "Foobar/overloaded.txt");
		internalGetLocalResource(c, "overloaded.txt", new String[]{""}, null, "Foobar/overloaded.txt");
		internalGetLocalResource(c, "overloaded", new String[]{".txt"}, null, "Foobar/overloaded.txt");

		// checking for links
		internalGetLocalResource(c, "link.txt", null, GERMANY_LOCALE, "Foobar/test.txt");
		internalGetLocalResource(c, "cyclic1", null, GERMANY_LOCALE, null);
		internalGetLocalResource(c, "cyclicFallback1", null, GERMANY_LOCALE, "Foobar/cyclicFallback1");

		// checking internal class handling
		c = Foobar.Internal.class;
		internalGetLocalResource(c, "test.txt", null, GERMANY_LOCALE, "Foobar/Internal/test.txt");

		c = Foobar.StaticInternal.class;
		internalGetLocalResource(c, "test.txt", null, GERMANY_LOCALE, "Foobar/StaticInternal/de/test.txt");

		c = Foobar.StaticInternal.Foo.class;
		internalGetLocalResource(c, "test.txt", null, GERMANY_LOCALE, null);
	}

	@Test
	public void getLocalResourceAsStream()
		throws Exception
	{
		Class c;

		c = Foobar.class;
		internalGetLocalResourceAsStream(c, "defaultFallback.txt", null, GERMANY_LOCALE, false); // not anymore
		internalGetLocalResourceAsStream(c, "defaultFallback", new String[]{".txt", ".html"}, GERMANY_LOCALE, false); // not anymore
		internalGetLocalResourceAsStream(c, "test.txt", null, GERMANY_LOCALE, true);

		// checking the different shortcuts...
		internalGetLocalResourceAsStream(c, "overloaded.txt", null, GERMANY_LOCALE, true);
		internalGetLocalResourceAsStream(c, "overloaded.txt", null, null, true);
		internalGetLocalResourceAsStream(c, "overloaded.txt", new String[]{}, null, true);
		internalGetLocalResourceAsStream(c, "overloaded.txt", new String[]{""}, null, true);
		internalGetLocalResourceAsStream(c, "overloaded", new String[]{".txt"}, null, true);

		// checking for links
		internalGetLocalResourceAsStream(c, "link.txt", null, GERMANY_LOCALE, true);
		internalGetLocalResourceAsStream(c, "cyclic1", null, GERMANY_LOCALE, false);
		internalGetLocalResourceAsStream(c, "cyclicFallback1", null, GERMANY_LOCALE, true);

		// checking internal class handling
		c = Foobar.Internal.class;
		internalGetLocalResourceAsStream(c, "test.txt", null, GERMANY_LOCALE, true);

		c = Foobar.StaticInternal.class;
		internalGetLocalResourceAsStream(c, "test.txt", null, GERMANY_LOCALE, true);

		c = Foobar.StaticInternal.Foo.class;
		internalGetLocalResourceAsStream(c, "test.txt", null, GERMANY_LOCALE, false);
	}

	@Test
	public void getResources()
		throws Exception
	{
		Class c;

		c = Foobar.class;
		internalGetResources(c, "defaultFallback.txt", null, GERMANY_LOCALE, new String[]{}); // not anymore
		internalGetResources(c, "defaultFallback", new String[]{".txt", ".html"}, GERMANY_LOCALE, new String[]{}); // not anymore
		internalGetResources(c, "................../underflow.txt", null, GERMANY_LOCALE, new String[]{});
		internalGetResources(c, "test.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/test.txt",
		});

		// checking the different shortcuts...
		internalGetResources(c, "overloaded.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/overloaded.txt",
			"BaseClass/overloaded.txt",
		});
		internalGetResources(c, "overloaded.txt", null, null, new String[]{
			"Foobar/overloaded.txt",
			"BaseClass/overloaded.txt",
		});
		internalGetResources(c, "overloaded.txt", new String[]{}, null, new String[]{
			"Foobar/overloaded.txt",
			"BaseClass/overloaded.txt",
		});
		internalGetResources(c, "overloaded.txt", new String[]{""}, null, new String[]{
			"Foobar/overloaded.txt",
			"BaseClass/overloaded.txt",
		});
		internalGetResources(c, "overloaded", new String[]{".txt"}, null, new String[]{
			"Foobar/overloaded.txt",
			"BaseClass/overloaded.txt",
		});

		// checking for links
		internalGetResources(c, "link.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/test.txt",
		});
		internalGetResources(c, "cyclic1", null, GERMANY_LOCALE, new String[]{});
		internalGetResources(c, "cyclicFallback1", null, GERMANY_LOCALE, new String[]{
			"Foobar/cyclicFallback1",
		});

		// checking internal class handling
		c = Foobar.Internal.class;
		internalGetResources(c, "test.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/Internal/test.txt",
			"Foobar/test.txt",
		});

		c = Foobar.StaticInternal.class;
		internalGetResources(c, "test.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/StaticInternal/de/test.txt",
			"Foobar/StaticInternal/test.txt",
			"Foobar/test.txt",
		});
		c = Foobar.StaticInternal.Foo.class;
		internalGetResources(c, "test.txt", null, GERMANY_LOCALE, new String[]{
			"Foobar/StaticInternal/de/test.txt",
			"Foobar/StaticInternal/test.txt",
			"Foobar/test.txt",
		});
	}

	@Test
	public void getResource()
		throws Exception
	{
		Class c;

		c = Foobar.class;
		internalGetResource(c, "defaultFallback.txt", null, GERMANY_LOCALE, null); // not anymore
		internalGetResource(c, "defaultFallback", new String[]{".txt", ".html"}, GERMANY_LOCALE, null); // not anymore
		internalGetResource(c, "test.txt", null, GERMANY_LOCALE, "Foobar/test.txt");

		// checking the different shortcuts...
		internalGetResource(c, "overloaded.txt", null, GERMANY_LOCALE, "Foobar/overloaded.txt");
		internalGetResource(c, "overloaded.txt", null, null, "Foobar/overloaded.txt");
		internalGetResource(c, "overloaded.txt", new String[]{}, null, "Foobar/overloaded.txt");
		internalGetResource(c, "overloaded.txt", new String[]{""}, null, "Foobar/overloaded.txt");
		internalGetResource(c, "overloaded", new String[]{".txt"}, null, "Foobar/overloaded.txt");

		// checking for links
		internalGetResource(c, "link.txt", null, GERMANY_LOCALE, "Foobar/test.txt");
		internalGetResource(c, "cyclic1", null, GERMANY_LOCALE, null);
		internalGetResource(c, "cyclicFallback1", null, GERMANY_LOCALE, "Foobar/cyclicFallback1");

		// checking internal class handling
		c = Foobar.Internal.class;
		internalGetResource(c, "test.txt", null, GERMANY_LOCALE, "Foobar/Internal/test.txt");

		c = Foobar.StaticInternal.class;
		internalGetResource(c, "test.txt", null, GERMANY_LOCALE, "Foobar/StaticInternal/de/test.txt");

		c = Foobar.StaticInternal.Foo.class;
		internalGetResource(c, "test.txt", null, GERMANY_LOCALE, "Foobar/StaticInternal/de/test.txt");
	}

	@Test
	public void getResourceAsStream()
		throws Exception
	{
		Class c;

		c = Foobar.class;
		internalGetResourceAsStream(c, "defaultFallback.txt", null, GERMANY_LOCALE, false); // not anymore
		internalGetResourceAsStream(c, "defaultFallback", new String[]{".txt", ".html"}, GERMANY_LOCALE, false); // not anymore
		internalGetResourceAsStream(c, "test.txt", null, GERMANY_LOCALE, true);

		// checking the different shortcuts...
		internalGetResourceAsStream(c, "overloaded.txt", null, GERMANY_LOCALE, true);
		internalGetResourceAsStream(c, "overloaded.txt", null, null, true);
		internalGetResourceAsStream(c, "overloaded.txt", new String[]{}, null, true);
		internalGetResourceAsStream(c, "overloaded.txt", new String[]{""}, null, true);
		internalGetResourceAsStream(c, "overloaded", new String[]{".txt"}, null, true);

		// checking for links
		internalGetResourceAsStream(c, "link.txt", null, GERMANY_LOCALE, true);
		internalGetResourceAsStream(c, "cyclic1", null, GERMANY_LOCALE, false);
		internalGetResourceAsStream(c, "cyclicFallback1", null, GERMANY_LOCALE, true);

		// checking internal class handling
		c = Foobar.Internal.class;
		internalGetResourceAsStream(c, "test.txt", null, GERMANY_LOCALE, true);

		c = Foobar.StaticInternal.class;
		internalGetResourceAsStream(c, "test.txt", null, GERMANY_LOCALE, true);

		c = Foobar.StaticInternal.Foo.class;
		internalGetResourceAsStream(c, "test.txt", null, GERMANY_LOCALE, true);
	}

	@Test
	public void getShortClassName()
		throws Exception
	{
		String str;

		str = Resources.getShortClassName(Foobar.class);
		assertEquals("Foobar", str);

		str = Resources.getShortClassName(Foobar.Internal.class);
		assertEquals("Foobar$Internal", str);
	}

	@Test
	public void getLocaleSuffixArray()
		throws Exception
	{
		internalGetLocaleSuffixArray(GERMANY_LOCALE, new String[]
			{
				"de_DE",
				"de",
				"en_US",
				"en",
			});

		internalGetLocaleSuffixArray(HESSIAN_LOCALE, new String[]
			{
				"de_DE_hessisch",
				"de_DE",
				"de",
				"en_US",
				"en",
			});

		internalGetLocaleSuffixArray(Locale.of("", "", "foo"), new String[]
			{
				"__foo",
				"en_US",
				"en",
			});
		internalGetLocaleSuffixArray(Locale.of("foo", "", "bar"), new String[]
			{
				"foo__bar",
				"foo",
				"en_US",
				"en",
			});

		internalGetLocaleSuffixArray(EMPTY_LOCALE, new String[]
			{
				"en_US",
				"en",
			});

		internalGetLocaleSuffixArray(null, new String[]
			{
				"en_US",
				"en",
			});

		internalGetLocaleSuffixArray(US_LOCALE, new String[]
			{
				"en_US",
				"en",
			});

		Locale.setDefault(EMPTY_LOCALE);
		internalGetLocaleSuffixArray(EMPTY_LOCALE, new String[]{});
	}

	@Test
	public void getSingleLocaleSuffixArray()
		throws Exception
	{
		internalGetSingleLocaleSuffixArray(GERMANY_LOCALE, new String[]
			{
				"de_DE",
				"de",
			});

		internalGetSingleLocaleSuffixArray(HESSIAN_LOCALE, new String[]
			{
				"de_DE_hessisch",
				"de_DE",
				"de",
			});

		internalGetSingleLocaleSuffixArray(Locale.of("", "", "foo"), new String[]
			{
				"__foo",
			});
		internalGetSingleLocaleSuffixArray(Locale.of("foo", "", "bar"), new String[]
			{
				"foo__bar",
				"foo",
			});

		internalGetSingleLocaleSuffixArray(EMPTY_LOCALE, new String[]{});

		internalGetSingleLocaleSuffixArray(null, new String[]{});
	}

	@Test
	public void getPathToClass()
		throws Exception
	{
		String str;
		str = Resources.getPathToClass(Foobar.class);
		assertEquals("/de/huxhorn/sulky/resources/junit/Foobar", str);
		str = Resources.getPathToClass(Foobar.Internal.class);
		assertEquals("/de/huxhorn/sulky/resources/junit/Foobar/Internal", str);
		str = Resources.getPathToClass(Foobar.StaticInternal.Foo.class);
		assertEquals("/de/huxhorn/sulky/resources/junit/Foobar/StaticInternal/Foo", str);
	}

	@Test
	public void getPathToPackage()
		throws Exception
	{
		String str;

		str = Resources.getPathToPackage(Foobar.class);
		assertEquals("/de/huxhorn/sulky/resources/junit", str);
		str = Resources.getPathToPackage(Foobar.StaticInternal.class);
		assertEquals("/de/huxhorn/sulky/resources/junit", str);
	}
}
