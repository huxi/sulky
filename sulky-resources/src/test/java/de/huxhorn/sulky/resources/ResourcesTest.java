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

package de.huxhorn.sulky.resources;

import de.huxhorn.sulky.resources.junit.Foobar;

import org.junit.AfterClass;
import static org.junit.Assert.*;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.Locale;

public class ResourcesTest
{
	private final Logger logger = LoggerFactory.getLogger(ResourcesTest.class);

	private static final Locale usLocale = new Locale("en", "US");
	final Locale germanLocale = new Locale("de", "DE");
	final Locale hessianLocale = new Locale("de", "DE", "hessisch");
	final Locale empty = new Locale("", "", "");
	private static Locale prevDefault;

	private void internalGetLocalResources(Class clazz, String resourceBaseName, String[] suffixes, Locale locale, String[] resultEndsWith)
	{

		// checking method with four arguments
		StringBuilder methCallBuff = new StringBuilder();
		methCallBuff.append("getLocalResources(").append(clazz.getName()).append(", \"").append(resourceBaseName)
			.append("\", ");
		if(suffixes == null)
		{
			methCallBuff.append(suffixes);
		}
		else
		{
			methCallBuff.append("[");
			for(int i = 0; i < suffixes.length; i++)
			{
				if(i != 0)
				{
					methCallBuff.append(", ");
				}
				methCallBuff.append("\"").append(suffixes[i]).append("\"");
			}
			methCallBuff.append("]");
		}
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if(logger.isInfoEnabled()) logger.info("Testing {}.", methodCall);

		URL[] result;
		result = Resources.getLocalResources(clazz, resourceBaseName, suffixes, locale);
		if(logger.isInfoEnabled())
		{
			StringBuilder debug = new StringBuilder();
			debug.append("Results returned by method call ");
			debug.append(methodCall);
			debug.append(":\n");
			for(int i = 0; i < result.length; i++)
			{
				debug.append("#").append(i).append(": ").append(result[i]).append("\n");
			}
			logger.info(debug.toString());
		}
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
			if(suffixes == null)
			{
				methCallBuff.append(suffixes);
			}
			else
			{
				methCallBuff.append("[");
				for(int i = 0; i < suffixes.length; i++)
				{
					if(i != 0)
					{
						methCallBuff.append(", ");
					}
					methCallBuff.append("\"").append(suffixes[i]).append("\"");
				}
				methCallBuff.append("]");
			}
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getLocalResources(clazz, resourceBaseName, suffixes);
			if(logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Results returned by method call ");
				debug.append(methodCall);
				debug.append(":\n");
				for(int i = 0; i < result.length; i++)
				{
					debug.append("#").append(i).append(": ").append(result[i]).append("\n");
				}
				logger.info(debug.toString());
			}
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
				if(logger.isInfoEnabled())
				{
					StringBuilder debug = new StringBuilder();
					debug.append("Results returned by method call ");
					debug.append(methodCall);
					debug.append(":\n");
					for(int i = 0; i < result.length; i++)
					{
						debug.append("#").append(i).append(": ").append(result[i]).append("\n");
					}
					logger.info(debug.toString());
				}
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
			methCallBuff.append(locale).append(")");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getLocalResources(clazz, resourceBaseName, locale);
			if(logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Results returned by method call ");
				debug.append(methodCall);
				debug.append(":\n");
				for(int i = 0; i < result.length; i++)
				{
					debug.append("#").append(i).append(": ").append(result[i]).append("\n");
				}
				logger.info(debug.toString());
			}
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
		StringBuilder methCallBuff = new StringBuilder();
		methCallBuff.append("getLocalResource(").append(clazz.getName()).append(", \"").append(resourceBaseName)
			.append("\", ");
		if(suffixes == null)
		{
			methCallBuff.append(suffixes);
		}
		else
		{
			methCallBuff.append("[");
			for(int i = 0; i < suffixes.length; i++)
			{
				if(i != 0)
				{
					methCallBuff.append(", ");
				}
				methCallBuff.append("\"").append(suffixes[i]).append("\"");
			}
			methCallBuff.append("]");
		}
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if(logger.isInfoEnabled()) logger.info("Testing {}.", methodCall);

		URL result;
		result = Resources.getLocalResource(clazz, resourceBaseName, suffixes, locale);
		if(logger.isInfoEnabled())
		{
			StringBuilder debug = new StringBuilder();
			debug.append("Result returned by method call ");
			debug.append(methodCall);
			debug.append(": ").append(result);
			logger.info(debug.toString());
		}

		if(result == null)
		{
			if(resultEndsWith != null)
			{
				fail(methodCall + " - result was null but should end in \"" + resultEndsWith + "\"!");
			}
		}
		else
		{
			String cur = result.toString();
			String msg = methodCall + " - resultUrl:\"" + cur + "\" does not end with \"" + resultEndsWith + "\"!";
			assertTrue(msg, cur.endsWith(resultEndsWith));
		}

		if(locale == null)
		{
			// checking shortcut method (Class,String,String[]) without locale
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getLocalResource(").append(clazz.getName()).append(", \"").append(resourceBaseName)
				.append("\", ");
			if(suffixes == null)
			{
				methCallBuff.append(suffixes);
			}
			else
			{
				methCallBuff.append("[");
				for(int i = 0; i < suffixes.length; i++)
				{
					if(i != 0)
					{
						methCallBuff.append(", ");
					}
					methCallBuff.append("\"").append(suffixes[i]).append("\"");
				}
				methCallBuff.append("]");
			}
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getLocalResource(clazz, resourceBaseName, suffixes);
			if(logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Result returned by method call ");
				debug.append(methodCall);
				debug.append(": ").append(result);
				logger.info(debug.toString());
			}

			if(result == null)
			{
				if(resultEndsWith != null)
				{
					fail(methodCall + " - result was null but should end in \"" + resultEndsWith + "\"!");
				}
			}
			else
			{
				String cur = result.toString();
				String msg = methodCall + " - resultUrl:\"" + cur + "\" does not end with \"" + resultEndsWith + "\"!";
				assertTrue(msg, cur.endsWith(resultEndsWith));
			}

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
				if(logger.isInfoEnabled())
				{
					StringBuilder debug = new StringBuilder();
					debug.append("Result returned by method call ");
					debug.append(methodCall);
					debug.append(": ").append(result);
					logger.info(debug.toString());
				}

				if(result == null)
				{
					if(resultEndsWith != null)
					{
						fail(methodCall + " - result was null but should end in \"" + resultEndsWith + "\"!");
					}
				}
				else
				{
					String cur = result.toString();
					String msg = methodCall + " - resultUrl:\"" + cur + "\" does not end with \"" + resultEndsWith + "\"!";
					assertTrue(msg, cur.endsWith(resultEndsWith));
				}
			} // if no suffixes
		} // if locale == null
		else if(suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
		{
			// checking shortcut method (Class,String,Locale) without suffixes
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getLocalResource(").append(clazz.getName()).append(", \"").append(resourceBaseName)
				.append("\", ");
			methCallBuff.append(locale).append(")");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getLocalResource(clazz, resourceBaseName, locale);
			if(logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Result returned by method call ");
				debug.append(methodCall);
				debug.append(": ").append(result);
				logger.info(debug.toString());
			}

			if(result == null)
			{
				if(resultEndsWith != null)
				{
					fail(methodCall + " - result was null but should end in \"" + resultEndsWith + "\"!");
				}
			}
			else
			{
				String cur = result.toString();
				String msg = methodCall + " - resultUrl:\"" + cur + "\" does not end with \"" + resultEndsWith + "\"!";
				assertTrue(msg, cur.endsWith(resultEndsWith));
			}
		} // if no suffixes but locale
	}

	private void internalGetLocalResourceAsStream(Class clazz, String resourceBaseName, String[] suffixes, Locale locale, boolean found)
	{
		// checking method with four arguments
		StringBuilder methCallBuff = new StringBuilder();
		methCallBuff.append("getLocalResourceAsStream(").append(clazz.getName()).append(", \"").append(resourceBaseName)
			.append("\", ");
		if(suffixes == null)
		{
			methCallBuff.append(suffixes);
		}
		else
		{
			methCallBuff.append("[");
			for(int i = 0; i < suffixes.length; i++)
			{
				if(i != 0)
				{
					methCallBuff.append(", ");
				}
				methCallBuff.append("\"").append(suffixes[i]).append("\"");
			}
			methCallBuff.append("]");
		}
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if(logger.isInfoEnabled()) logger.info("Testing {}.", methodCall);

		InputStream result;
		result = Resources.getLocalResourceAsStream(clazz, resourceBaseName, suffixes, locale);
		if(logger.isInfoEnabled())
		{
			StringBuilder debug = new StringBuilder();
			debug.append("Result returned by method call ");
			debug.append(methodCall);
			debug.append(": ").append(result);
			logger.info(debug.toString());
		}

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
			if(suffixes == null)
			{
				methCallBuff.append(suffixes);
			}
			else
			{
				methCallBuff.append("[");
				for(int i = 0; i < suffixes.length; i++)
				{
					if(i != 0)
					{
						methCallBuff.append(", ");
					}
					methCallBuff.append("\"").append(suffixes[i]).append("\"");
				}
				methCallBuff.append("]");
			}
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getLocalResourceAsStream(clazz, resourceBaseName, suffixes);
			if(logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Result returned by method call ");
				debug.append(methodCall);
				debug.append(": ").append(result);
				logger.info(debug.toString());
			}

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
				if(logger.isInfoEnabled())
				{
					StringBuilder debug = new StringBuilder();
					debug.append("Result returned by method call ");
					debug.append(methodCall);
					debug.append(": ").append(result);
					logger.info(debug.toString());
				}

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
			methCallBuff = new StringBuilder();
			methCallBuff.append("getLocalResourceAsStream(").append(clazz.getName()).append(", \"")
				.append(resourceBaseName).append("\", ");
			methCallBuff.append(locale).append(")");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getLocalResourceAsStream(clazz, resourceBaseName, locale);
			if(logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Result returned by method call ");
				debug.append(methodCall);
				debug.append(": ").append(result);
				logger.info(debug.toString());
			}

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
		StringBuilder methCallBuff = new StringBuilder();
		methCallBuff.append("getResources(").append(clazz.getName()).append(", \"").append(resourceBaseName)
			.append("\", ");
		if(suffixes == null)
		{
			methCallBuff.append(suffixes);
		}
		else
		{
			methCallBuff.append("[");
			for(int i = 0; i < suffixes.length; i++)
			{
				if(i != 0)
				{
					methCallBuff.append(", ");
				}
				methCallBuff.append("\"").append(suffixes[i]).append("\"");
			}
			methCallBuff.append("]");
		}
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if(logger.isInfoEnabled()) logger.info("Testing {}.", methodCall);

		URL[] result;
		result = Resources.getResources(clazz, resourceBaseName, suffixes, locale);
		if(logger.isInfoEnabled())
		{
			StringBuilder debug = new StringBuilder();
			debug.append("Results returned by method call ");
			debug.append(methodCall);
			debug.append(":\n");
			for(int i = 0; i < result.length; i++)
			{
				debug.append("#").append(i).append(": ").append(result[i]).append("\n");
			}
			logger.info(debug.toString());
		}
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
			if(suffixes == null)
			{
				methCallBuff.append(suffixes);
			}
			else
			{
				methCallBuff.append("[");
				for(int i = 0; i < suffixes.length; i++)
				{
					if(i != 0)
					{
						methCallBuff.append(", ");
					}
					methCallBuff.append("\"").append(suffixes[i]).append("\"");
				}
				methCallBuff.append("]");
			}
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getResources(clazz, resourceBaseName, suffixes);
			if(logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Results returned by method call ");
				debug.append(methodCall);
				debug.append(":\n");
				for(int i = 0; i < result.length; i++)
				{
					debug.append("#").append(i).append(": ").append(result[i]).append("\n");
				}
				logger.info(debug.toString());
			}
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
				if(logger.isInfoEnabled())
				{
					StringBuilder debug = new StringBuilder();
					debug.append("Results returned by method call ");
					debug.append(methodCall);
					debug.append(":\n");
					for(int i = 0; i < result.length; i++)
					{
						debug.append("#").append(i).append(": ").append(result[i]).append("\n");
					}
					logger.info(debug.toString());
				}
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
			methCallBuff.append("getResources(").append(clazz.getName()).append(", \"").append(resourceBaseName)
				.append("\", ");
			methCallBuff.append(locale).append(")");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getResources(clazz, resourceBaseName, locale);
			if(logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Results returned by method call ");
				debug.append(methodCall);
				debug.append(":\n");
				for(int i = 0; i < result.length; i++)
				{
					debug.append("#").append(i).append(": ").append(result[i]).append("\n");
				}
				logger.info(debug.toString());
			}
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
		StringBuilder methCallBuff = new StringBuilder();
		methCallBuff.append("getResource(").append(clazz.getName()).append(", \"").append(resourceBaseName)
			.append("\", ");
		if(suffixes == null)
		{
			methCallBuff.append(suffixes);
		}
		else
		{
			methCallBuff.append("[");
			for(int i = 0; i < suffixes.length; i++)
			{
				if(i != 0)
				{
					methCallBuff.append(", ");
				}
				methCallBuff.append("\"").append(suffixes[i]).append("\"");
			}
			methCallBuff.append("]");
		}
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if(logger.isInfoEnabled()) logger.info("Testing {}.", methodCall);

		URL result;
		result = Resources.getResource(clazz, resourceBaseName, suffixes, locale);
		if(logger.isInfoEnabled())
		{
			StringBuilder debug = new StringBuilder();
			debug.append("Result returned by method call ");
			debug.append(methodCall);
			debug.append(": ").append(result);
			logger.info(debug.toString());
		}

		if(result == null)
		{
			if(resultEndsWith != null)
			{
				fail(methodCall + " - result was null but should end in \"" + resultEndsWith + "\"!");
			}
		}
		else
		{
			String cur = result.toString();
			String msg = methodCall + " - resultUrl:\"" + cur + "\" does not end with \"" + resultEndsWith + "\"!";
			assertTrue(msg, cur.endsWith(resultEndsWith));
		}

		if(locale == null)
		{
			// checking shortcut method (Class,String,String[]) without locale
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getResource(").append(clazz.getName()).append(", \"").append(resourceBaseName)
				.append("\", ");
			if(suffixes == null)
			{
				methCallBuff.append(suffixes);
			}
			else
			{
				methCallBuff.append("[");
				for(int i = 0; i < suffixes.length; i++)
				{
					if(i != 0)
					{
						methCallBuff.append(", ");
					}
					methCallBuff.append("\"").append(suffixes[i]).append("\"");
				}
				methCallBuff.append("]");
			}
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getResource(clazz, resourceBaseName, suffixes);
			if(logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Result returned by method call ");
				debug.append(methodCall);
				debug.append(": ").append(result);
				logger.info(debug.toString());
			}

			if(result == null)
			{
				if(resultEndsWith != null)
				{
					fail(methodCall + " - result was null but should end in \"" + resultEndsWith + "\"!");
				}
			}
			else
			{
				String cur = result.toString();
				String msg = methodCall + " - resultUrl:\"" + cur + "\" does not end with \"" + resultEndsWith + "\"!";
				assertTrue(msg, cur.endsWith(resultEndsWith));
			}

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
				if(logger.isInfoEnabled())
				{
					StringBuilder debug = new StringBuilder();
					debug.append("Result returned by method call ");
					debug.append(methodCall);
					debug.append(": ").append(result);
					logger.info(debug.toString());
				}

				if(result == null)
				{
					if(resultEndsWith != null)
					{
						fail(methodCall + " - result was null but should end in \"" + resultEndsWith + "\"!");
					}
				}
				else
				{
					String cur = result.toString();
					String msg = methodCall + " - resultUrl:\"" + cur + "\" does not end with \"" + resultEndsWith + "\"!";
					assertTrue(msg, cur.endsWith(resultEndsWith));
				}
			} // if no suffixes
		} // if locale == null
		else if(suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
		{
			// checking shortcut method (Class,String,Locale) without suffixes
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getResource(").append(clazz.getName()).append(", \"").append(resourceBaseName)
				.append("\", ");
			methCallBuff.append(locale).append(")");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getResource(clazz, resourceBaseName, locale);
			if(logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Result returned by method call ");
				debug.append(methodCall);
				debug.append(": ").append(result);
				logger.info(debug.toString());
			}

			if(result == null)
			{
				if(resultEndsWith != null)
				{
					fail(methodCall + " - result was null but should end in \"" + resultEndsWith + "\"!");
				}
			}
			else
			{
				String cur = result.toString();
				String msg = methodCall + " - resultUrl:\"" + cur + "\" does not end with \"" + resultEndsWith + "\"!";
				assertTrue(msg, cur.endsWith(resultEndsWith));
			}
		} // if no suffixes but locale
	}

	private void internalGetResourceAsStream(Class clazz, String resourceBaseName, String[] suffixes, Locale locale, boolean found)
	{
		// checking method with four arguments
		StringBuilder methCallBuff = new StringBuilder();
		methCallBuff.append("getResourceAsStream(").append(clazz.getName()).append(", \"").append(resourceBaseName)
			.append("\", ");
		if(suffixes == null)
		{
			methCallBuff.append(suffixes);
		}
		else
		{
			methCallBuff.append("[");
			for(int i = 0; i < suffixes.length; i++)
			{
				if(i != 0)
				{
					methCallBuff.append(", ");
				}
				methCallBuff.append("\"").append(suffixes[i]).append("\"");
			}
			methCallBuff.append("]");
		}
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if(logger.isInfoEnabled()) logger.info("Testing {}.", methodCall);

		InputStream result;
		result = Resources.getResourceAsStream(clazz, resourceBaseName, suffixes, locale);
		if(logger.isInfoEnabled())
		{
			StringBuilder debug = new StringBuilder();
			debug.append("Result returned by method call ");
			debug.append(methodCall);
			debug.append(": ").append(result);
			logger.info(debug.toString());
		}

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
			if(suffixes == null)
			{
				methCallBuff.append(suffixes);
			}
			else
			{
				methCallBuff.append("[");
				for(int i = 0; i < suffixes.length; i++)
				{
					if(i != 0)
					{
						methCallBuff.append(", ");
					}
					methCallBuff.append("\"").append(suffixes[i]).append("\"");
				}
				methCallBuff.append("]");
			}
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getResourceAsStream(clazz, resourceBaseName, suffixes);
			if(logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Result returned by method call ");
				debug.append(methodCall);
				debug.append(": ").append(result);
				logger.info(debug.toString());
			}

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
				if(logger.isInfoEnabled())
				{
					StringBuilder debug = new StringBuilder();
					debug.append("Result returned by method call ");
					debug.append(methodCall);
					debug.append(": ").append(result);
					logger.info(debug.toString());
				}

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
			methCallBuff = new StringBuilder();
			methCallBuff.append("getResourceAsStream(").append(clazz.getName()).append(", \"").append(resourceBaseName)
				.append("\", ");
			methCallBuff.append(locale).append(")");
			methodCall = methCallBuff.toString();
			if(logger.isInfoEnabled()) logger.info("Testing shortcut {}.", methodCall);
			result = Resources.getResourceAsStream(clazz, resourceBaseName, locale);
			if(logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Result returned by method call ");
				debug.append(methodCall);
				debug.append(": ").append(result);
				logger.info(debug.toString());
			}

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

	private void internalGetLocaleSuffixArray(Locale locale, String expectedResults[])
	{
		String methodCall = "getLocaleSuffixArray(" + locale + ")";
		String result[] = Resources.getLocaleSuffixArray(locale);
		assertEquals(methodCall + " - Number of Suffixes", expectedResults.length, result.length);
		for(int i = 0; i < expectedResults.length; i++)
		{
			assertEquals(methodCall + " - Wrong suffix at index #" + i, expectedResults[i], result[i]);
		}
	}

	private void internalGetSingleLocaleSuffixArray(Locale locale, String[] expectedResults)
	{
		String methodCall = "getSingleLocaleSuffixArray(" + locale + ")";
		String result[] = Resources.getSingleLocaleSuffixArray(locale);
		assertEquals(methodCall + " - Number of Suffixes", expectedResults.length, result.length);
		for(int i = 0; i < expectedResults.length; i++)
		{
			assertEquals(methodCall + " - Wrong suffix at index #" + i, expectedResults[i], result[i]);
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
	public void getLocalResources()
		throws Exception
	{
		Class c;

		c = de.huxhorn.sulky.resources.junit.Foobar.class;
		internalGetLocalResources(c, "defaultFallback.txt", null, germanLocale, new String[]{}); // not anymore
		internalGetLocalResources(c, "defaultFallback", new String[]{".txt", ".html"}, germanLocale, new String[]{}); // not anymore
		internalGetLocalResources(c, "................../underflow.txt", null, germanLocale, new String[]{});

		internalGetLocalResources(c, "test.txt", null, germanLocale, new String[]{
			"Foobar/test.txt"
		});

		// checking the different shortcuts...
		internalGetLocalResources(c, "overloaded.txt", null, germanLocale, new String[]{
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
		internalGetLocalResources(c, "link.txt", null, germanLocale, new String[]{
			"Foobar/test.txt"
		});
		internalGetLocalResources(c, "cyclic1", null, germanLocale, new String[]{});
		internalGetLocalResources(c, "cyclicFallback1", null, germanLocale, new String[]{
			"Foobar/cyclicFallback1"
		});

		// checking internal class handling
		c = de.huxhorn.sulky.resources.junit.Foobar.Internal.class;
		internalGetLocalResources(c, "test.txt", null, germanLocale, new String[]{
			"Foobar/Internal/test.txt",
		});

		c = de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.class;
		internalGetLocalResources(c, "test.txt", null, germanLocale, new String[]{
			"Foobar/StaticInternal/de/test.txt",
			"Foobar/StaticInternal/test.txt",
		});
		c = de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.Foo.class;
		internalGetLocalResources(c, "test.txt", null, germanLocale, new String[]{});
	}

	@Test
	public void getLocalResource()
		throws Exception
	{
		Class c;

		c = de.huxhorn.sulky.resources.junit.Foobar.class;
		internalGetLocalResource(c, "defaultFallback.txt", null, germanLocale, null); // not anymore
		internalGetLocalResource(c, "defaultFallback", new String[]{".txt", ".html"}, germanLocale, null); // not anymore
		internalGetLocalResource(c, "test.txt", null, germanLocale, "Foobar/test.txt");

		// checking the different shortcuts...
		internalGetLocalResource(c, "overloaded.txt", null, germanLocale, "Foobar/overloaded.txt");
		internalGetLocalResource(c, "overloaded.txt", null, null, "Foobar/overloaded.txt");
		internalGetLocalResource(c, "overloaded.txt", new String[]{}, null, "Foobar/overloaded.txt");
		internalGetLocalResource(c, "overloaded.txt", new String[]{""}, null, "Foobar/overloaded.txt");
		internalGetLocalResource(c, "overloaded", new String[]{".txt"}, null, "Foobar/overloaded.txt");

		// checking for links
		internalGetLocalResource(c, "link.txt", null, germanLocale, "Foobar/test.txt");
		internalGetLocalResource(c, "cyclic1", null, germanLocale, null);
		internalGetLocalResource(c, "cyclicFallback1", null, germanLocale, "Foobar/cyclicFallback1");

		// checking internal class handling
		c = de.huxhorn.sulky.resources.junit.Foobar.Internal.class;
		internalGetLocalResource(c, "test.txt", null, germanLocale, "Foobar/Internal/test.txt");

		c = de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.class;
		internalGetLocalResource(c, "test.txt", null, germanLocale, "Foobar/StaticInternal/de/test.txt");

		c = de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.Foo.class;
		internalGetLocalResource(c, "test.txt", null, germanLocale, null);
	}

	@Test
	public void getLocalResourceAsStream()
		throws Exception
	{
		Class c;

		c = de.huxhorn.sulky.resources.junit.Foobar.class;
		internalGetLocalResourceAsStream(c, "defaultFallback.txt", null, germanLocale, false); // not anymore
		internalGetLocalResourceAsStream(c, "defaultFallback", new String[]{".txt", ".html"}, germanLocale, false); // not anymore
		internalGetLocalResourceAsStream(c, "test.txt", null, germanLocale, true);

		// checking the different shortcuts...
		internalGetLocalResourceAsStream(c, "overloaded.txt", null, germanLocale, true);
		internalGetLocalResourceAsStream(c, "overloaded.txt", null, null, true);
		internalGetLocalResourceAsStream(c, "overloaded.txt", new String[]{}, null, true);
		internalGetLocalResourceAsStream(c, "overloaded.txt", new String[]{""}, null, true);
		internalGetLocalResourceAsStream(c, "overloaded", new String[]{".txt"}, null, true);

		// checking for links
		internalGetLocalResourceAsStream(c, "link.txt", null, germanLocale, true);
		internalGetLocalResourceAsStream(c, "cyclic1", null, germanLocale, false);
		internalGetLocalResourceAsStream(c, "cyclicFallback1", null, germanLocale, true);

		// checking internal class handling
		c = de.huxhorn.sulky.resources.junit.Foobar.Internal.class;
		internalGetLocalResourceAsStream(c, "test.txt", null, germanLocale, true);

		c = de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.class;
		internalGetLocalResourceAsStream(c, "test.txt", null, germanLocale, true);

		c = de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.Foo.class;
		internalGetLocalResourceAsStream(c, "test.txt", null, germanLocale, false);
	}

	@Test
	public void getResources()
		throws Exception
	{
		Class c;

		c = de.huxhorn.sulky.resources.junit.Foobar.class;
		internalGetResources(c, "defaultFallback.txt", null, germanLocale, new String[]{}); // not anymore
		internalGetResources(c, "defaultFallback", new String[]{".txt", ".html"}, germanLocale, new String[]{}); // not anymore
		internalGetResources(c, "................../underflow.txt", null, germanLocale, new String[]{});
		internalGetResources(c, "test.txt", null, germanLocale, new String[]{
			"Foobar/test.txt"
		});

		// checking the different shortcuts...
		internalGetResources(c, "overloaded.txt", null, germanLocale, new String[]{
			"Foobar/overloaded.txt",
			"BaseClass/overloaded.txt"
		});
		internalGetResources(c, "overloaded.txt", null, null, new String[]{
			"Foobar/overloaded.txt",
			"BaseClass/overloaded.txt"
		});
		internalGetResources(c, "overloaded.txt", new String[]{}, null, new String[]{
			"Foobar/overloaded.txt",
			"BaseClass/overloaded.txt"
		});
		internalGetResources(c, "overloaded.txt", new String[]{""}, null, new String[]{
			"Foobar/overloaded.txt",
			"BaseClass/overloaded.txt"
		});
		internalGetResources(c, "overloaded", new String[]{".txt"}, null, new String[]{
			"Foobar/overloaded.txt",
			"BaseClass/overloaded.txt"
		});

		// checking for links
		internalGetResources(c, "link.txt", null, germanLocale, new String[]{
			"Foobar/test.txt"
		});
		internalGetResources(c, "cyclic1", null, germanLocale, new String[]{});
		internalGetResources(c, "cyclicFallback1", null, germanLocale, new String[]{
			"Foobar/cyclicFallback1"
		});

		// checking internal class handling
		c = de.huxhorn.sulky.resources.junit.Foobar.Internal.class;
		internalGetResources(c, "test.txt", null, germanLocale, new String[]{
			"Foobar/Internal/test.txt",
			"Foobar/test.txt"
		});

		c = de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.class;
		internalGetResources(c, "test.txt", null, germanLocale, new String[]{
			"Foobar/StaticInternal/de/test.txt",
			"Foobar/StaticInternal/test.txt",
			"Foobar/test.txt"
		});
		c = de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.Foo.class;
		internalGetResources(c, "test.txt", null, germanLocale, new String[]{
			"Foobar/StaticInternal/de/test.txt",
			"Foobar/StaticInternal/test.txt",
			"Foobar/test.txt"
		});
	}

	@Test
	public void getResource()
		throws Exception
	{
		Class c;

		c = de.huxhorn.sulky.resources.junit.Foobar.class;
		internalGetResource(c, "defaultFallback.txt", null, germanLocale, null); // not anymore
		internalGetResource(c, "defaultFallback", new String[]{".txt", ".html"}, germanLocale, null); // not anymore
		internalGetResource(c, "test.txt", null, germanLocale, "Foobar/test.txt");

		// checking the different shortcuts...
		internalGetResource(c, "overloaded.txt", null, germanLocale, "Foobar/overloaded.txt");
		internalGetResource(c, "overloaded.txt", null, null, "Foobar/overloaded.txt");
		internalGetResource(c, "overloaded.txt", new String[]{}, null, "Foobar/overloaded.txt");
		internalGetResource(c, "overloaded.txt", new String[]{""}, null, "Foobar/overloaded.txt");
		internalGetResource(c, "overloaded", new String[]{".txt"}, null, "Foobar/overloaded.txt");

		// checking for links
		internalGetResource(c, "link.txt", null, germanLocale, "Foobar/test.txt");
		internalGetResource(c, "cyclic1", null, germanLocale, null);
		internalGetResource(c, "cyclicFallback1", null, germanLocale, "Foobar/cyclicFallback1");

		// checking internal class handling
		c = de.huxhorn.sulky.resources.junit.Foobar.Internal.class;
		internalGetResource(c, "test.txt", null, germanLocale, "Foobar/Internal/test.txt");

		c = de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.class;
		internalGetResource(c, "test.txt", null, germanLocale, "Foobar/StaticInternal/de/test.txt");

		c = de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.Foo.class;
		internalGetResource(c, "test.txt", null, germanLocale, "Foobar/StaticInternal/de/test.txt");
	}

	@Test
	public void getResourceAsStream()
		throws Exception
	{
		Class c;

		c = de.huxhorn.sulky.resources.junit.Foobar.class;
		internalGetResourceAsStream(c, "defaultFallback.txt", null, germanLocale, false); // not anymore
		internalGetResourceAsStream(c, "defaultFallback", new String[]{".txt", ".html"}, germanLocale, false); // not anymore
		internalGetResourceAsStream(c, "test.txt", null, germanLocale, true);

		// checking the different shortcuts...
		internalGetResourceAsStream(c, "overloaded.txt", null, germanLocale, true);
		internalGetResourceAsStream(c, "overloaded.txt", null, null, true);
		internalGetResourceAsStream(c, "overloaded.txt", new String[]{}, null, true);
		internalGetResourceAsStream(c, "overloaded.txt", new String[]{""}, null, true);
		internalGetResourceAsStream(c, "overloaded", new String[]{".txt"}, null, true);

		// checking for links
		internalGetResourceAsStream(c, "link.txt", null, germanLocale, true);
		internalGetResourceAsStream(c, "cyclic1", null, germanLocale, false);
		internalGetResourceAsStream(c, "cyclicFallback1", null, germanLocale, true);

		// checking internal class handling
		c = de.huxhorn.sulky.resources.junit.Foobar.Internal.class;
		internalGetResourceAsStream(c, "test.txt", null, germanLocale, true);

		c = de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.class;
		internalGetResourceAsStream(c, "test.txt", null, germanLocale, true);

		c = de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.Foo.class;
		internalGetResourceAsStream(c, "test.txt", null, germanLocale, true);
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
		internalGetLocaleSuffixArray(germanLocale, new String[]
			{
				"de_DE",
				"de",
				"en_US",
				"en",
			});

		internalGetLocaleSuffixArray(hessianLocale, new String[]
			{
				"de_DE_hessisch",
				"de_DE",
				"de",
				"en_US",
				"en",
			});

		internalGetLocaleSuffixArray(new Locale("", "", "foo"), new String[]
			{
				"__foo",
				"en_US",
				"en",
			});
		internalGetLocaleSuffixArray(new Locale("foo", "", "bar"), new String[]
			{
				"foo__bar",
				"foo",
				"en_US",
				"en",
			});

		internalGetLocaleSuffixArray(empty, new String[]
			{
				"en_US",
				"en",
			});

		internalGetLocaleSuffixArray(null, new String[]
			{
				"en_US",
				"en",
			});

		internalGetLocaleSuffixArray(usLocale, new String[]
			{
				"en_US",
				"en",
			});

		Locale.setDefault(empty);
		internalGetLocaleSuffixArray(empty, new String[]{});
	}

	@Test
	public void getSingleLocaleSuffixArray()
		throws Exception
	{
		internalGetSingleLocaleSuffixArray(germanLocale, new String[]
			{
				"de_DE",
				"de",
			});

		internalGetSingleLocaleSuffixArray(hessianLocale, new String[]
			{
				"de_DE_hessisch",
				"de_DE",
				"de",
			});

		internalGetSingleLocaleSuffixArray(new Locale("", "", "foo"), new String[]
			{
				"__foo",
			});
		internalGetSingleLocaleSuffixArray(new Locale("foo", "", "bar"), new String[]
			{
				"foo__bar",
				"foo",
			});

		internalGetSingleLocaleSuffixArray(empty, new String[]{});

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
