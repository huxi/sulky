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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.junit.Assert.assertNotNull;
import org.junit.Test;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Before;

import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

public class ResourceSupportTest
{
	private final Logger logger = LoggerFactory.getLogger(ResourceSupportTest.class);

	ResourceSupport resourcesupport = null;

	private static final Locale usLocale = new Locale("en", "US");
	final Locale germanLocale = new Locale("de", "DE");
	final Locale hessianLocale = new Locale("de", "DE", "hessisch");
	private static Locale prevDefault;


	private void internalGetLocalResources(String resourceBaseName, String[] suffixes, Locale locale, String[] resultEndsWith)
	{
		// checking method with four arguments
		StringBuilder methCallBuff = new StringBuilder();
		methCallBuff.append("getLocalResources(\"").append(resourceBaseName).append("\", ");
		if (suffixes == null)
		{
			methCallBuff.append(suffixes);
		}
		else
		{
			methCallBuff.append("[");
			for (int i = 0; i < suffixes.length; i++)
			{
				if (i != 0)
				{
					methCallBuff.append(", ");
				}
				methCallBuff.append("\"").append(suffixes[i]).append("\"");
			}
			methCallBuff.append("]");
		}
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if (logger.isInfoEnabled()) logger.info("Testing " + methodCall + ".");

		URL[] result;
		result = resourcesupport.getLocalResources(resourceBaseName, suffixes, locale);
		if (logger.isInfoEnabled())
		{
			StringBuilder debug = new StringBuilder();
			debug.append("Results returned by method call ");
			debug.append(methodCall);
			debug.append(":\n");
			for (int i = 0; i < result.length; i++)
			{
				debug.append("#").append(i).append(": ").append(result[i]).append("\n");
			}
			logger.info(debug.toString());
		}
		assertEquals(methodCall + " - Number of results", resultEndsWith.length, result.length);

		for (int i = 0; i < result.length; i++)
		{
			String cur = result[i].toString();
			String msg = methodCall + " - resultUrls[" + i + "]:\"" + cur + "\" does not end with \"" + resultEndsWith[i] + "\"!";
			assertTrue(msg, cur.endsWith(resultEndsWith[i]));
		}

		if (locale == null)
		{
			// checking shortcut method (Class,String,String[]) without locale
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getLocalResources(\"").append(resourceBaseName).append("\", ");
			if (suffixes == null)
			{
				methCallBuff.append(suffixes);
			}
			else
			{
				methCallBuff.append("[");
				for (int i = 0; i < suffixes.length; i++)
				{
					if (i != 0)
					{
						methCallBuff.append(", ");
					}
					methCallBuff.append("\"").append(suffixes[i]).append("\"");
				}
				methCallBuff.append("]");
			}
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if (logger.isInfoEnabled()) logger.info("Testing shortcut " + methodCall + ".");
			result = resourcesupport.getLocalResources(resourceBaseName, suffixes);
			if (logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Results returned by method call ");
				debug.append(methodCall);
				debug.append(":\n");
				for (int i = 0; i < result.length; i++)
				{
					debug.append("#").append(i).append(": ").append(result[i]).append("\n");
				}
				logger.info(debug.toString());
			}
			assertEquals(methodCall + " - Number of results", resultEndsWith.length, result.length);

			for (int i = 0; i < result.length; i++)
			{
				String cur = result[i].toString();
				String msg = methodCall + " - resultUrls[" + i + "]:\"" + cur + "\" does not end with \"" + resultEndsWith[i] + "\"!";
				assertTrue(msg, cur.endsWith(resultEndsWith[i]));
			}
			if (suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
			{
				// checking shortcut method (Class,String) without suffixes and locale
				// results must be the same...
				methCallBuff = new StringBuilder();
				methCallBuff.append("getLocalResources(\"").append(resourceBaseName).append("\");");
				methodCall = methCallBuff.toString();
				if (logger.isInfoEnabled()) logger.info("Testing shortcut " + methodCall + ".");
				result = resourcesupport.getLocalResources(resourceBaseName);
				if (logger.isInfoEnabled())
				{
					StringBuilder debug = new StringBuilder();
					debug.append("Results returned by method call ");
					debug.append(methodCall);
					debug.append(":\n");
					for (int i = 0; i < result.length; i++)
					{
						debug.append("#").append(i).append(": ").append(result[i]).append("\n");
					}
					logger.info(debug.toString());
				}
				assertEquals(methodCall + " - Number of results", resultEndsWith.length, result.length);

				for (int i = 0; i < result.length; i++)
				{
					String cur = result[i].toString();
					String msg = methodCall + " - resultUrls[" + i + "]:\"" + cur + "\" does not end with \"" + resultEndsWith[i] + "\"!";
					assertTrue(msg, cur.endsWith(resultEndsWith[i]));
				}
			} // if no suffixes
		} // if locale == null
		else if (suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
		{
			// checking shortcut method (Class,String,Locale) without suffixes
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getLocalResources(\"").append(resourceBaseName).append("\", ");
			methCallBuff.append(locale).append(")");
			methodCall = methCallBuff.toString();
			if (logger.isInfoEnabled()) logger.info("Testing shortcut " + methodCall + ".");
			result = resourcesupport.getLocalResources(resourceBaseName, locale);
			if (logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Results returned by method call ");
				debug.append(methodCall);
				debug.append(":\n");
				for (int i = 0; i < result.length; i++)
				{
					debug.append("#").append(i).append(": ").append(result[i]).append("\n");
				}
				logger.info(debug.toString());
			}
			assertEquals(methodCall + " - Number of results", resultEndsWith.length, result.length);

			for (int i = 0; i < result.length; i++)
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
		StringBuilder methCallBuff = new StringBuilder();
		methCallBuff.append("getLocalResource(\"").append(resourceBaseName).append("\", ");
		if (suffixes == null)
		{
			methCallBuff.append(suffixes);
		}
		else
		{
			methCallBuff.append("[");
			for (int i = 0; i < suffixes.length; i++)
			{
				if (i != 0)
				{
					methCallBuff.append(", ");
				}
				methCallBuff.append("\"").append(suffixes[i]).append("\"");
			}
			methCallBuff.append("]");
		}
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if (logger.isInfoEnabled()) logger.info("Testing " + methodCall + ".");

		URL result;
		result = resourcesupport.getLocalResource(resourceBaseName, suffixes, locale);
		if (logger.isInfoEnabled())
		{
			StringBuilder debug = new StringBuilder();
			debug.append("Result returned by method call ");
			debug.append(methodCall);
			debug.append(": ").append(result);
			logger.info(debug.toString());
		}

		if (result == null)
		{
			if (resultEndsWith != null)
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

		if (locale == null)
		{
			// checking shortcut method (Class,String,String[]) without locale
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getLocalResource(\"").append(resourceBaseName).append("\", ");
			if (suffixes == null)
			{
				methCallBuff.append(suffixes);
			}
			else
			{
				methCallBuff.append("[");
				for (int i = 0; i < suffixes.length; i++)
				{
					if (i != 0)
					{
						methCallBuff.append(", ");
					}
					methCallBuff.append("\"").append(suffixes[i]).append("\"");
				}
				methCallBuff.append("]");
			}
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if (logger.isInfoEnabled()) logger.info("Testing shortcut " + methodCall + ".");
			result = resourcesupport.getLocalResource(resourceBaseName, suffixes);
			if (logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Result returned by method call ");
				debug.append(methodCall);
				debug.append(": ").append(result);
				logger.info(debug.toString());
			}

			if (result == null)
			{
				if (resultEndsWith != null)
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

			if (suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
			{
				// checking shortcut method (Class,String) without suffixes and locale
				// results must be the same...
				methCallBuff = new StringBuilder();
				methCallBuff.append("getLocalResource(\"").append(resourceBaseName).append("\");");
				methodCall = methCallBuff.toString();
				if (logger.isInfoEnabled()) logger.info("Testing shortcut " + methodCall + ".");
				result = resourcesupport.getLocalResource(resourceBaseName);
				if (logger.isInfoEnabled())
				{
					StringBuilder debug = new StringBuilder();
					debug.append("Result returned by method call ");
					debug.append(methodCall);
					debug.append(": ").append(result);
					logger.info(debug.toString());
				}

				if (result == null)
				{
					if (resultEndsWith != null)
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
		else if (suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
		{
			// checking shortcut method (Class,String,Locale) without suffixes
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getLocalResource(\"").append(resourceBaseName).append("\", ");
			methCallBuff.append(locale).append(")");
			methodCall = methCallBuff.toString();
			if (logger.isInfoEnabled()) logger.info("Testing shortcut " + methodCall + ".");
			result = resourcesupport.getLocalResource(resourceBaseName, locale);
			if (logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Result returned by method call ");
				debug.append(methodCall);
				debug.append(": ").append(result);
				logger.info(debug.toString());
			}

			if (result == null)
			{
				if (resultEndsWith != null)
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

	private void internalGetLocalResourceAsStream(String resourceBaseName, String[] suffixes, Locale locale, boolean found)
	{
		// checking method with four arguments
		StringBuilder methCallBuff = new StringBuilder();
		methCallBuff.append("getLocalResourceAsStream(\"").append(resourceBaseName).append("\", ");
		if (suffixes == null)
		{
			methCallBuff.append(suffixes);
		}
		else
		{
			methCallBuff.append("[");
			for (int i = 0; i < suffixes.length; i++)
			{
				if (i != 0)
				{
					methCallBuff.append(", ");
				}
				methCallBuff.append("\"").append(suffixes[i]).append("\"");
			}
			methCallBuff.append("]");
		}
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if (logger.isInfoEnabled()) logger.info("Testing " + methodCall + ".");

		InputStream result;
		result = resourcesupport.getLocalResourceAsStream(resourceBaseName, suffixes, locale);
		if (logger.isInfoEnabled())
		{
			StringBuilder debug = new StringBuilder();
			debug.append("Result returned by method call ");
			debug.append(methodCall);
			debug.append(": ").append(result);
			logger.info(debug.toString());
		}

		if (result == null && found)
		{
			fail(methodCall + " - Expected result but was null!");
		}
		else if (result != null && !found)
		{
			fail(methodCall + " - Found a result but didn't expect one!");
		}

		if (locale == null)
		{
			// checking shortcut method (Class,String,String[]) without locale
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getLocalResourceAsStream(\"").append(resourceBaseName).append("\", ");
			if (suffixes == null)
			{
				methCallBuff.append(suffixes);
			}
			else
			{
				methCallBuff.append("[");
				for (int i = 0; i < suffixes.length; i++)
				{
					if (i != 0)
					{
						methCallBuff.append(", ");
					}
					methCallBuff.append("\"").append(suffixes[i]).append("\"");
				}
				methCallBuff.append("]");
			}
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if (logger.isInfoEnabled()) logger.info("Testing shortcut " + methodCall + ".");
			result = resourcesupport.getLocalResourceAsStream(resourceBaseName, suffixes);
			if (logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Result returned by method call ");
				debug.append(methodCall);
				debug.append(": ").append(result);
				logger.info(debug.toString());
			}

			if (result == null && found)
			{
				fail(methodCall + " - Expected result but was null!");
			}
			else if (result != null && !found)
			{
				fail(methodCall + " - Found a result but didn't expect one!");
			}

			if (suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
			{
				// checking shortcut method (Class,String) without suffixes and locale
				// results must be the same...
				methCallBuff = new StringBuilder();
				methCallBuff.append("getLocalResourceAsStream(\"").append(resourceBaseName).append("\");");
				methodCall = methCallBuff.toString();
				if (logger.isInfoEnabled()) logger.info("Testing shortcut " + methodCall + ".");
				result = resourcesupport.getLocalResourceAsStream(resourceBaseName);
				if (logger.isInfoEnabled())
				{
					StringBuilder debug = new StringBuilder();
					debug.append("Result returned by method call ");
					debug.append(methodCall);
					debug.append(": ").append(result);
					logger.info(debug.toString());
				}

				if (result == null && found)
				{
					fail(methodCall + " - Expected result but was null!");
				}
				else if (result != null && !found)
				{
					fail(methodCall + " - Found a result but didn't expect one!");
				}
			} // if no suffixes
		} // if locale == null
		else if (suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
		{
			// checking shortcut method (Class,String,Locale) without suffixes
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getLocalResourceAsStream(\"").append(resourceBaseName).append("\", ");
			methCallBuff.append(locale).append(")");
			methodCall = methCallBuff.toString();
			if (logger.isInfoEnabled()) logger.info("Testing shortcut " + methodCall + ".");
			result = resourcesupport.getLocalResourceAsStream(resourceBaseName, locale);
			if (logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Result returned by method call ");
				debug.append(methodCall);
				debug.append(": ").append(result);
				logger.info(debug.toString());
			}

			if (result == null && found)
			{
				fail(methodCall + " - Expected result but was null!");
			}
			else if (result != null && !found)
			{
				fail(methodCall + " - Found a result but didn't expect one!");
			}
		} // if no suffixes but locale
	}

	private void internalGetResources(String resourceBaseName, String[] suffixes, Locale locale, String[] resultEndsWith)
	{
		// checking method with four arguments
		StringBuilder methCallBuff = new StringBuilder();
		methCallBuff.append("getResources(\"").append(resourceBaseName).append("\", ");
		if (suffixes == null)
		{
			methCallBuff.append(suffixes);
		}
		else
		{
			methCallBuff.append("[");
			for (int i = 0; i < suffixes.length; i++)
			{
				if (i != 0)
				{
					methCallBuff.append(", ");
				}
				methCallBuff.append("\"").append(suffixes[i]).append("\"");
			}
			methCallBuff.append("]");
		}
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if (logger.isInfoEnabled()) logger.info("Testing " + methodCall + ".");

		URL[] result;
		result = resourcesupport.getResources(resourceBaseName, suffixes, locale);
		if (logger.isInfoEnabled())
		{
			StringBuilder debug = new StringBuilder();
			debug.append("Results returned by method call ");
			debug.append(methodCall);
			debug.append(":\n");
			for (int i = 0; i < result.length; i++)
			{
				debug.append("#").append(i).append(": ").append(result[i]).append("\n");
			}
			logger.info(debug.toString());
		}
		assertEquals(methodCall + " - Number of results", resultEndsWith.length, result.length);

		for (int i = 0; i < result.length; i++)
		{
			String cur = result[i].toString();
			String msg = methodCall + " - resultUrls[" + i + "]:\"" + cur + "\" does not end with \"" + resultEndsWith[i] + "\"!";
			assertTrue(msg, cur.endsWith(resultEndsWith[i]));
		}

		if (locale == null)
		{
			// checking shortcut method (Class,String,String[]) without locale
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getResources(\"").append(resourceBaseName).append("\", ");
			if (suffixes == null)
			{
				methCallBuff.append(suffixes);
			}
			else
			{
				methCallBuff.append("[");
				for (int i = 0; i < suffixes.length; i++)
				{
					if (i != 0)
					{
						methCallBuff.append(", ");
					}
					methCallBuff.append("\"").append(suffixes[i]).append("\"");
				}
				methCallBuff.append("]");
			}
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if (logger.isInfoEnabled()) logger.info("Testing shortcut " + methodCall + ".");
			result = resourcesupport.getResources(resourceBaseName, suffixes);
			if (logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Results returned by method call ");
				debug.append(methodCall);
				debug.append(":\n");
				for (int i = 0; i < result.length; i++)
				{
					debug.append("#").append(i).append(": ").append(result[i]).append("\n");
				}
				logger.info(debug.toString());
			}
			assertEquals(methodCall + " - Number of results", resultEndsWith.length, result.length);

			for (int i = 0; i < result.length; i++)
			{
				String cur = result[i].toString();
				String msg = methodCall + " - resultUrls[" + i + "]:\"" + cur + "\" does not end with \"" + resultEndsWith[i] + "\"!";
				assertTrue(msg, cur.endsWith(resultEndsWith[i]));
			}
			if (suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
			{
				// checking shortcut method (Class,String) without suffixes and locale
				// results must be the same...
				methCallBuff = new StringBuilder();
				methCallBuff.append("getResources(\"").append(resourceBaseName).append("\");");
				methodCall = methCallBuff.toString();
				if (logger.isInfoEnabled()) logger.info("Testing shortcut " + methodCall + ".");
				result = resourcesupport.getResources(resourceBaseName);
				if (logger.isInfoEnabled())
				{
					StringBuilder debug = new StringBuilder();
					debug.append("Results returned by method call ");
					debug.append(methodCall);
					debug.append(":\n");
					for (int i = 0; i < result.length; i++)
					{
						debug.append("#").append(i).append(": ").append(result[i]).append("\n");
					}
					logger.info(debug.toString());
				}
				assertEquals(methodCall + " - Number of results", resultEndsWith.length, result.length);

				for (int i = 0; i < result.length; i++)
				{
					String cur = result[i].toString();
					String msg = methodCall + " - resultUrls[" + i + "]:\"" + cur + "\" does not end with \"" + resultEndsWith[i] + "\"!";
					assertTrue(msg, cur.endsWith(resultEndsWith[i]));
				}
			} // if no suffixes
		} // if locale == null
		else if (suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
		{
			// checking shortcut method (Class,String,Locale) without suffixes
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getResources(\"").append(resourceBaseName).append("\", ");
			methCallBuff.append(locale).append(")");
			methodCall = methCallBuff.toString();
			if (logger.isInfoEnabled()) logger.info("Testing shortcut " + methodCall + ".");
			result = resourcesupport.getResources(resourceBaseName, locale);
			if (logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Results returned by method call ");
				debug.append(methodCall);
				debug.append(":\n");
				for (int i = 0; i < result.length; i++)
				{
					debug.append("#").append(i).append(": ").append(result[i]).append("\n");
				}
				logger.info(debug.toString());
			}
			assertEquals(methodCall + " - Number of results", resultEndsWith.length, result.length);

			for (int i = 0; i < result.length; i++)
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
		StringBuilder methCallBuff = new StringBuilder();
		methCallBuff.append("getResource(\"").append(resourceBaseName).append("\", ");
		if (suffixes == null)
		{
			methCallBuff.append(suffixes);
		}
		else
		{
			methCallBuff.append("[");
			for (int i = 0; i < suffixes.length; i++)
			{
				if (i != 0)
				{
					methCallBuff.append(", ");
				}
				methCallBuff.append("\"").append(suffixes[i]).append("\"");
			}
			methCallBuff.append("]");
		}
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if (logger.isInfoEnabled()) logger.info("Testing " + methodCall + ".");

		URL result;
		result = resourcesupport.getResource(resourceBaseName, suffixes, locale);
		if (logger.isInfoEnabled())
		{
			StringBuilder debug = new StringBuilder();
			debug.append("Result returned by method call ");
			debug.append(methodCall);
			debug.append(": ").append(result);
			logger.info(debug.toString());
		}

		if (result == null)
		{
			if (resultEndsWith != null)
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

		if (locale == null)
		{
			// checking shortcut method (Class,String,String[]) without locale
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getResource(\"").append(resourceBaseName).append("\", ");
			if (suffixes == null)
			{
				methCallBuff.append(suffixes);
			}
			else
			{
				methCallBuff.append("[");
				for (int i = 0; i < suffixes.length; i++)
				{
					if (i != 0)
					{
						methCallBuff.append(", ");
					}
					methCallBuff.append("\"").append(suffixes[i]).append("\"");
				}
				methCallBuff.append("]");
			}
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if (logger.isInfoEnabled()) logger.info("Testing shortcut " + methodCall + ".");
			result = resourcesupport.getResource(resourceBaseName, suffixes);
			if (logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Result returned by method call ");
				debug.append(methodCall);
				debug.append(": ").append(result);
				logger.info(debug.toString());
			}

			if (result == null)
			{
				if (resultEndsWith != null)
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

			if (suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
			{
				// checking shortcut method (Class,String) without suffixes and locale
				// results must be the same...
				methCallBuff = new StringBuilder();
				methCallBuff.append("getResource(\"").append(resourceBaseName).append("\");");
				methodCall = methCallBuff.toString();
				if (logger.isInfoEnabled()) logger.info("Testing shortcut " + methodCall + ".");
				result = resourcesupport.getResource(resourceBaseName);
				if (logger.isInfoEnabled())
				{
					StringBuilder debug = new StringBuilder();
					debug.append("Result returned by method call ");
					debug.append(methodCall);
					debug.append(": ").append(result);
					logger.info(debug.toString());
				}

				if (result == null)
				{
					if (resultEndsWith != null)
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
		else if (suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
		{
			// checking shortcut method (Class,String,Locale) without suffixes
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getResource(\"").append(resourceBaseName).append("\", ");
			methCallBuff.append(locale).append(")");
			methodCall = methCallBuff.toString();
			if (logger.isInfoEnabled()) logger.info("Testing shortcut " + methodCall + ".");
			result = resourcesupport.getResource(resourceBaseName, locale);
			if (logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Result returned by method call ");
				debug.append(methodCall);
				debug.append(": ").append(result);
				logger.info(debug.toString());
			}

			if (result == null)
			{
				if (resultEndsWith != null)
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

	private void internalGetResourceAsStream(String resourceBaseName, String[] suffixes, Locale locale, boolean found)
	{
		// checking method with four arguments
		StringBuilder methCallBuff = new StringBuilder();
		methCallBuff.append("getResourceAsStream(\"").append(resourceBaseName).append("\", ");
		if (suffixes == null)
		{
			methCallBuff.append(suffixes);
		}
		else
		{
			methCallBuff.append("[");
			for (int i = 0; i < suffixes.length; i++)
			{
				if (i != 0)
				{
					methCallBuff.append(", ");
				}
				methCallBuff.append("\"").append(suffixes[i]).append("\"");
			}
			methCallBuff.append("]");
		}
		methCallBuff.append(", ").append(locale).append(");");
		String methodCall = methCallBuff.toString();
		if (logger.isInfoEnabled()) logger.info("Testing " + methodCall + ".");

		InputStream result;
		result = resourcesupport.getResourceAsStream(resourceBaseName, suffixes, locale);
		if (logger.isInfoEnabled())
		{
			StringBuilder debug = new StringBuilder();
			debug.append("Result returned by method call ");
			debug.append(methodCall);
			debug.append(": ").append(result);
			logger.info(debug.toString());
		}

		if (result == null && found)
		{
			fail(methodCall + " - Expected result but was null!");
		}
		else if (result != null && !found)
		{
			fail(methodCall + " - Found a result but didn't expect one!");
		}

		if (locale == null)
		{
			// checking shortcut method (Class,String,String[]) without locale
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getResourceAsStream(\"").append(resourceBaseName).append("\", ");
			if (suffixes == null)
			{
				methCallBuff.append(suffixes);
			}
			else
			{
				methCallBuff.append("[");
				for (int i = 0; i < suffixes.length; i++)
				{
					if (i != 0)
					{
						methCallBuff.append(", ");
					}
					methCallBuff.append("\"").append(suffixes[i]).append("\"");
				}
				methCallBuff.append("]");
			}
			methCallBuff.append(");");
			methodCall = methCallBuff.toString();
			if (logger.isInfoEnabled()) logger.info("Testing shortcut " + methodCall + ".");
			result = resourcesupport.getResourceAsStream(resourceBaseName, suffixes);
			if (logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Result returned by method call ");
				debug.append(methodCall);
				debug.append(": ").append(result);
				logger.info(debug.toString());
			}

			if (result == null && found)
			{
				fail(methodCall + " - Expected result but was null!");
			}
			else if (result != null && !found)
			{
				fail(methodCall + " - Found a result but didn't expect one!");
			}

			if (suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
			{
				// checking shortcut method (Class,String) without suffixes and locale
				// results must be the same...
				methCallBuff = new StringBuilder();
				methCallBuff.append("getResourceAsStream(\"").append(resourceBaseName).append("\");");
				methodCall = methCallBuff.toString();
				if (logger.isInfoEnabled()) logger.info("Testing shortcut " + methodCall + ".");
				result = resourcesupport.getResourceAsStream(resourceBaseName);
				if (logger.isInfoEnabled())
				{
					StringBuilder debug = new StringBuilder();
					debug.append("Result returned by method call ");
					debug.append(methodCall);
					debug.append(": ").append(result);
					logger.info(debug.toString());
				}

				if (result == null && found)
				{
					fail(methodCall + " - Expected result but was null!");
				}
				else if (result != null && !found)
				{
					fail(methodCall + " - Found a result but didn't expect one!");
				}
			} // if no suffixes
		} // if locale == null
		else if (suffixes == null || suffixes.length == 0 || (suffixes.length == 1 && suffixes[0].length() == 0))
		{
			// checking shortcut method (Class,String,Locale) without suffixes
			// results must be the same...
			methCallBuff = new StringBuilder();
			methCallBuff.append("getResourceAsStream(\"").append(resourceBaseName).append("\", ");
			methCallBuff.append(locale).append(")");
			methodCall = methCallBuff.toString();
			if (logger.isInfoEnabled()) logger.info("Testing shortcut " + methodCall + ".");
			result = resourcesupport.getResourceAsStream(resourceBaseName, locale);
			if (logger.isInfoEnabled())
			{
				StringBuilder debug = new StringBuilder();
				debug.append("Result returned by method call ");
				debug.append(methodCall);
				debug.append(": ").append(result);
				logger.info(debug.toString());
			}

			if (result == null && found)
			{
				fail(methodCall + " - Expected result but was null!");
			}
			else if (result != null && !found)
			{
				fail(methodCall + " - Found a result but didn't expect one!");
			}
		} // if no suffixes but locale
	}

	private void internalGetResourceMap(String resourceBaseName, Locale locale, String[][] expectedResults)
	{
		Map<String, Object> result;
		result = resourcesupport.getResourceMap(resourceBaseName, locale);
		assertEquals("Number of elements", expectedResults.length, result.size());
		for (String[] kv : expectedResults)
		{
			// kv[0]=key, kv[1]=value
			String key = kv[0];
			String cur = (String) result.get(key);
			assertEquals("Property \"" + key + "\"", kv[1], cur);
		}
		if (locale == null)
		{
			result = resourcesupport.getResourceMap(resourceBaseName);
			assertEquals("Number of elements", expectedResults.length, result.size());
			for (String[] kv : expectedResults)
			{
				// kv[0]=key, kv[1]=value
				String key = kv[0];
				String cur = (String) result.get(key);
				assertEquals("Property \"" + key + "\"", kv[1], cur);
			}
		}
	}

	private void internalGetLocalResourceMap(String resourceBaseName, Locale locale, String[][] expectedResults)
	{
		Map result;
		result = resourcesupport.getLocalResourceMap(resourceBaseName, locale);
		assertEquals("Number of elements", expectedResults.length, result.size());
		for (String[] kv : expectedResults)
		{
			// kv[0]=key, kv[1]=value
			String key = kv[0];
			String cur = (String) result.get(key);
			assertEquals("Property \"" + key + "\"", kv[1], cur);
		}
		if (locale == null)
		{
			result = resourcesupport.getLocalResourceMap(resourceBaseName);
			assertEquals("Number of elements", expectedResults.length, result.size());
			for (String[] kv : expectedResults)
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
		if (proxy)
		{
			assertNotNull("Expected proxy but result was null!", o);

			Class c = o.getClass();
			String name = c.getName();
			assertTrue("Expected proxy but got " + name + " instead.", name.startsWith("$Proxy"));
		}
		else
		{
			assertEquals(expectedObject, o);
		}

	}

	@BeforeClass
	public static void setUp() throws Exception
	{
		prevDefault = Locale.getDefault();
		Locale.setDefault(usLocale);
	}

	@AfterClass
	public static void tearDown() throws Exception
	{
		Locale.setDefault(prevDefault);
	}

	@Before
	public void initInstance()
	{
		resourcesupport = new ResourceSupport(new Foobar());
	}

	@Test
	public void constructor()
	{
		try
		{
			resourcesupport = new ResourceSupport(null);
			fail("Should throw NullPointerException!");
		}
		catch (NullPointerException ex)
		{
			if (logger.isDebugEnabled()) logger.debug("Expected exception", ex);
		}
	}

	@Test
	public void resolveLocale() throws Exception
	{
		Localizable l = resourcesupport.getLocalizable();
		internalResolveLocale(germanLocale, germanLocale);
		internalResolveLocale(null, null);
		l.setLocale(hessianLocale);
		internalResolveLocale(germanLocale, hessianLocale);
		internalResolveLocale(null, hessianLocale);
	}

	@Test
	public void getResourceClass() throws Exception
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
	public void getResourceObject() throws Exception
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
	public void getLocalizable() throws Exception
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
		loc.setLocale(hessianLocale);
		l = loc.getLocale();
		assertEquals(hessianLocale, l);

		// getter only
		o = ((Foobar) o).new Internal();
		resourcesupport = new ResourceSupport(o);
		loc = resourcesupport.getLocalizable();
		loc.setLocale(null);
		l = loc.getLocale();
		assertEquals(hessianLocale, l);

		// getter and setter
		o = new Foobar.StaticInternal();
		resourcesupport = new ResourceSupport(o);
		loc = resourcesupport.getLocalizable();
		loc.setLocale(hessianLocale);
		l = loc.getLocale();
		assertEquals(hessianLocale, l);

		// neither getter nor setter
		o = new Foobar.StaticInternal.Foo();
		resourcesupport = new ResourceSupport(o);
		loc = resourcesupport.getLocalizable();
		loc.setLocale(hessianLocale);
		l = loc.getLocale();
		assertEquals(null, l);
	}

	@Test
	public void getLocalResources() throws Exception
	{
		// de.huxhorn.sulky.resources.junit.Foobar.class;
		internalGetLocalResources("defaultFallback.txt", null, germanLocale, new String[]{}); // not anymore
		internalGetLocalResources("defaultFallback", new String[]{".txt", ".html"}, germanLocale, new String[]{}); // not anymore
		internalGetLocalResources("test.txt", null, germanLocale, new String[]{
				"Foobar/test.txt"
		});

		// checking the different shortcuts...
		internalGetLocalResources("overloaded.txt", null, germanLocale, new String[]{
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
		internalGetLocalResources("link.txt", null, germanLocale, new String[]{
				"Foobar/test.txt"
		});
		internalGetLocalResources("cyclic1", null, germanLocale, new String[]{});
		internalGetLocalResources("cyclicFallback1", null, germanLocale, new String[]{
				"Foobar/cyclicFallback1"
		});

		// checking internal class handling
		resourcesupport = new ResourceSupport(de.huxhorn.sulky.resources.junit.Foobar.Internal.class);
		internalGetLocalResources("test.txt", null, germanLocale, new String[]{
				"Foobar/Internal/test.txt",
		});

		resourcesupport = new ResourceSupport(de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.class);
		internalGetLocalResources("test.txt", null, germanLocale, new String[]{
				"Foobar/StaticInternal/de/test.txt",
				"Foobar/StaticInternal/test.txt",
		});
		resourcesupport = new ResourceSupport(de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.Foo.class);
		internalGetLocalResources("test.txt", null, germanLocale, new String[]{});
	}

	@Test
	public void getLocalResource() throws Exception
	{
		// de.huxhorn.sulky.resources.junit.Foobar.class;
		internalGetLocalResource("defaultFallback.txt", null, germanLocale, null); // not anymore
		internalGetLocalResource("defaultFallback", new String[]{".txt", ".html"}, germanLocale, null); // not anymore
		internalGetLocalResource("test.txt", null, germanLocale, "Foobar/test.txt");

		// checking the different shortcuts...
		internalGetLocalResource("overloaded.txt", null, germanLocale, "Foobar/overloaded.txt");
		internalGetLocalResource("overloaded.txt", null, null, "Foobar/overloaded.txt");
		internalGetLocalResource("overloaded.txt", new String[]{}, null, "Foobar/overloaded.txt");
		internalGetLocalResource("overloaded.txt", new String[]{""}, null, "Foobar/overloaded.txt");
		internalGetLocalResource("overloaded", new String[]{".txt"}, null, "Foobar/overloaded.txt");

		// checking for links
		internalGetLocalResource("link.txt", null, germanLocale, "Foobar/test.txt");
		internalGetLocalResource("cyclic1", null, germanLocale, null);
		internalGetLocalResource("cyclicFallback1", null, germanLocale, "Foobar/cyclicFallback1");

		// checking internal class handling
		resourcesupport = new ResourceSupport(de.huxhorn.sulky.resources.junit.Foobar.Internal.class);
		internalGetLocalResource("test.txt", null, germanLocale, "Foobar/Internal/test.txt");

		resourcesupport = new ResourceSupport(de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.class);
		internalGetLocalResource("test.txt", null, germanLocale, "Foobar/StaticInternal/de/test.txt");

		resourcesupport = new ResourceSupport(de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.Foo.class);
		internalGetLocalResource("test.txt", null, germanLocale, null);
	}

	@Test
	public void getLocalResourceAsStream() throws Exception
	{
		// de.huxhorn.sulky.resources.junit.Foobar.class;
		internalGetLocalResourceAsStream("defaultFallback.txt", null, germanLocale, false); // not anymore
		internalGetLocalResourceAsStream("defaultFallback", new String[]{".txt", ".html"}, germanLocale, false); // not anymore
		internalGetLocalResourceAsStream("test.txt", null, germanLocale, true);

		// checking the different shortcuts...
		internalGetLocalResourceAsStream("overloaded.txt", null, germanLocale, true);
		internalGetLocalResourceAsStream("overloaded.txt", null, null, true);
		internalGetLocalResourceAsStream("overloaded.txt", new String[]{}, null, true);
		internalGetLocalResourceAsStream("overloaded.txt", new String[]{""}, null, true);
		internalGetLocalResourceAsStream("overloaded", new String[]{".txt"}, null, true);

		// checking for links
		internalGetLocalResourceAsStream("link.txt", null, germanLocale, true);
		internalGetLocalResourceAsStream("cyclic1", null, germanLocale, false);
		internalGetLocalResourceAsStream("cyclicFallback1", null, germanLocale, true);

		// checking internal class handling
		resourcesupport = new ResourceSupport(de.huxhorn.sulky.resources.junit.Foobar.Internal.class);
		internalGetLocalResourceAsStream("test.txt", null, germanLocale, true);

		resourcesupport = new ResourceSupport(de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.class);
		internalGetLocalResourceAsStream("test.txt", null, germanLocale, true);

		resourcesupport = new ResourceSupport(de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.Foo.class);
		internalGetLocalResourceAsStream("test.txt", null, germanLocale, false);
	}

	@Test
	public void getResources() throws Exception
	{
		// de.huxhorn.sulky.resources.junit.Foobar.class;
		internalGetResources("defaultFallback.txt", null, germanLocale, new String[]{}); // not anymore
		internalGetResources("defaultFallback", new String[]{".txt", ".html"}, germanLocale, new String[]{}); // not anymore
		internalGetResources("test.txt", null, germanLocale, new String[]{
				"Foobar/test.txt"
		});

		// checking the different shortcuts...
		internalGetResources("overloaded.txt", null, germanLocale, new String[]{
				"Foobar/overloaded.txt",
				"BaseClass/overloaded.txt"
		});
		internalGetResources("overloaded.txt", null, null, new String[]{
				"Foobar/overloaded.txt",
				"BaseClass/overloaded.txt"
		});
		internalGetResources("overloaded.txt", new String[]{}, null, new String[]{
				"Foobar/overloaded.txt",
				"BaseClass/overloaded.txt"
		});
		internalGetResources("overloaded.txt", new String[]{""}, null, new String[]{
				"Foobar/overloaded.txt",
				"BaseClass/overloaded.txt"
		});
		internalGetResources("overloaded", new String[]{".txt"}, null, new String[]{
				"Foobar/overloaded.txt",
				"BaseClass/overloaded.txt"
		});

		// checking for links
		internalGetResources("link.txt", null, germanLocale, new String[]{
				"Foobar/test.txt"
		});
		internalGetResources("cyclic1", null, germanLocale, new String[]{});
		internalGetResources("cyclicFallback1", null, germanLocale, new String[]{
				"Foobar/cyclicFallback1"
		});

		// checking internal class handling
		resourcesupport = new ResourceSupport(de.huxhorn.sulky.resources.junit.Foobar.Internal.class);
		internalGetResources("test.txt", null, germanLocale, new String[]{
				"Foobar/Internal/test.txt",
				"Foobar/test.txt"
		});

		resourcesupport = new ResourceSupport(de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.class);
		internalGetResources("test.txt", null, germanLocale, new String[]{
				"Foobar/StaticInternal/de/test.txt",
				"Foobar/StaticInternal/test.txt",
				"Foobar/test.txt"
		});
		resourcesupport = new ResourceSupport(de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.Foo.class);
		internalGetResources("test.txt", null, germanLocale, new String[]{
				"Foobar/StaticInternal/de/test.txt",
				"Foobar/StaticInternal/test.txt",
				"Foobar/test.txt"
		});

		// TODO: #######################################################################
		resourcesupport = new ResourceSupport(new Foobar());
		internalGetResources("locale.txt", null, null, new String[]{
				"Foobar/en_US/locale.txt",
				"Foobar/en/locale.txt",
				"Foobar/locale.txt",
		});

		internalGetResources("locale.txt", null, germanLocale, new String[]{
				"Foobar/de_DE/locale.txt",
				"Foobar/de/locale.txt",
				"Foobar/en_US/locale.txt",
				"Foobar/en/locale.txt",
				"Foobar/locale.txt",
		});
		Localizable l = resourcesupport.getLocalizable();
		l.setLocale(hessianLocale);
		internalGetResources("locale.txt", null, germanLocale, new String[]{
				"Foobar/de_DE_hessisch/locale.txt",
				"Foobar/de_DE/locale.txt",
				"Foobar/de/locale.txt",
				"Foobar/en_US/locale.txt",
				"Foobar/en/locale.txt",
				"Foobar/locale.txt",
		});
		internalGetResources("locale", new String[]{".html", ".txt"}, germanLocale, new String[]{
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
	public void getResource() throws Exception
	{
		// de.huxhorn.sulky.resources.junit.Foobar.class;
		internalGetResource("defaultFallback.txt", null, germanLocale, null); // not anymore
		internalGetResource("defaultFallback", new String[]{".txt", ".html"}, germanLocale, null); // not anymore
		internalGetResource("test.txt", null, germanLocale, "Foobar/test.txt");

		// checking the different shortcuts...
		internalGetResource("overloaded.txt", null, germanLocale, "Foobar/overloaded.txt");
		internalGetResource("overloaded.txt", null, null, "Foobar/overloaded.txt");
		internalGetResource("overloaded.txt", new String[]{}, null, "Foobar/overloaded.txt");
		internalGetResource("overloaded.txt", new String[]{""}, null, "Foobar/overloaded.txt");
		internalGetResource("overloaded", new String[]{".txt"}, null, "Foobar/overloaded.txt");

		// checking for links
		internalGetResource("link.txt", null, germanLocale, "Foobar/test.txt");
		internalGetResource("cyclic1", null, germanLocale, null);
		internalGetResource("cyclicFallback1", null, germanLocale, "Foobar/cyclicFallback1");

		// checking internal class handling
		resourcesupport = new ResourceSupport(de.huxhorn.sulky.resources.junit.Foobar.Internal.class);
		internalGetResource("test.txt", null, germanLocale, "Foobar/Internal/test.txt");

		resourcesupport = new ResourceSupport(de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.class);
		internalGetResource("test.txt", null, germanLocale, "Foobar/StaticInternal/de/test.txt");

		resourcesupport = new ResourceSupport(de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.Foo.class);
		internalGetResource("test.txt", null, germanLocale, "Foobar/StaticInternal/de/test.txt");
	}

	@Test
	public void getResourceAsStream() throws Exception
	{
		// de.huxhorn.sulky.resources.junit.Foobar.class;
		internalGetResourceAsStream("defaultFallback.txt", null, germanLocale, false); // not anymore
		internalGetResourceAsStream("defaultFallback", new String[]{".txt", ".html"}, germanLocale, false); // not anymore
		internalGetResourceAsStream("test.txt", null, germanLocale, true);

		// checking the different shortcuts...
		internalGetResourceAsStream("overloaded.txt", null, germanLocale, true);
		internalGetResourceAsStream("overloaded.txt", null, null, true);
		internalGetResourceAsStream("overloaded.txt", new String[]{}, null, true);
		internalGetResourceAsStream("overloaded.txt", new String[]{""}, null, true);
		internalGetResourceAsStream("overloaded", new String[]{".txt"}, null, true);

		// checking for links
		internalGetResourceAsStream("link.txt", null, germanLocale, true);
		internalGetResourceAsStream("cyclic1", null, germanLocale, false);
		internalGetResourceAsStream("cyclicFallback1", null, germanLocale, true);

		// checking internal class handling
		resourcesupport = new ResourceSupport(de.huxhorn.sulky.resources.junit.Foobar.Internal.class);
		internalGetResourceAsStream("test.txt", null, germanLocale, true);

		resourcesupport = new ResourceSupport(de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.class);
		internalGetResourceAsStream("test.txt", null, germanLocale, true);

		resourcesupport = new ResourceSupport(de.huxhorn.sulky.resources.junit.Foobar.StaticInternal.Foo.class);
		internalGetResourceAsStream("test.txt", null, germanLocale, true);
	}

	@Test
	public void getLocalResourceMap() throws Exception
	{
		// Foobar.class;
		internalGetLocalResourceMap("resources", hessianLocale, new String[][]{
				{"attention.txt", "Uffbasse!"},
				{"ok.txt", "OK"},
				{"cancel.txt", "Abbruch"},
		});
		internalGetLocalResourceMap("resources", germanLocale, new String[][]{
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
	public void getResourceMap() throws Exception
	{
		// Foobar.class;
		internalGetResourceMap("resources", hessianLocale, new String[][]{
				{"attention.txt", "Uffbasse!"},
				{"ok.txt", "OK"},
				{"cancel.txt", "Abbruch"},
				{"base.txt", "BaseClass"},
		});
		internalGetResourceMap("resources", germanLocale, new String[][]{
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
