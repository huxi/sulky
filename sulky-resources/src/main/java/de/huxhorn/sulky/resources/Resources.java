/*
 * sulky-resources - inheritance-safe class resources.
 * Copyright (C) 2002-2010 Joern Huxhorn
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
 * Copyright 2002-2010 Joern Huxhorn
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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Stack;

/**
 * <p><code>Resources</code> provides "object-oriented" resource resolution
 * and is supposed to be a replacement for <code>getResource(String)</code>
 * and <code>getResourceAsStream(String)</code> of <code>java.lang.Class</code>.</p>
 * <p/>
 * DOCUMENT: moep
 * Describe ordinary resource-location
 * - Using Class.getResource() or Class.getResourceAsStream()
 * <p/>
 * Document reasons for static class
 * - because the correct location for methods of this class would be in java.lang.Class
 * <p/>
 * Overall use of this class
 * Example/how-to-use-properly
 * <p/>
 * <p><strong>Question Of The Week:</strong> What is the plural of suffix?<br />
 * <strong>Answer:</strong> There is no answer in any available dictionary. But since
 * the latin root of "suffix"
 * is the adjective "suffixus" (derived from "subfigere") it is believed that the
 * correct plural is "suffixes" and not "suffices" as in "index/indices".<br />
 * Therefore "suffixes" is used in this document.</p>
 *
 * @see ResourceSupport ResourceSupport is a helper class that provides shortcuts for given objects.
 * @see Class#getResource(java.lang.String)
 * @see Class#getResourceAsStream(java.lang.String)
 */
public final class Resources
{
	/**
	 * Suffix that is used for links. It's value is ".link".
	 */
	public static final String LINK_SUFFIX = ".link";

	private static final String[] EMPTY_STRING_ARRAY = {};

	/**
	 * DOCUMENT: document
	 *
	 * @param clazz            the <code>Class</code> that is used to locate the resource.
	 * @param resourceBaseName
	 * @param suffixes
	 * @param locale           the <code>Locale</code> that is used to locate the resource.
	 * @return an <code>URL</code> array (empty if no resource was found)
	 * @see #getResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see Class#getResource(java.lang.String)
	 * @see Class#getResourceAsStream(java.lang.String)
	 */
	public static URL[] getLocalResources(final Class clazz, final String resourceBaseName, final String[] suffixes, final Locale locale)
	{
		return getLocalResources(clazz, resourceBaseName, suffixes, locale, false);
	}

	/**
	 * This is a shortcut method for <code>getLocalResources(clazz, resourceBaseName, suffixes, null);</code>.
	 *
	 * @param clazz            the <code>Class</code> that is used to locate the resource.
	 * @param resourceBaseName
	 * @param suffixes
	 * @return an <code>URL</code> array (empty if no resource was found)
	 * @see #getLocalResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static URL[] getLocalResources(final Class clazz, final String resourceBaseName, final String[] suffixes)
	{
		return getLocalResources(clazz, resourceBaseName, suffixes, null, false);
	}

	/**
	 * This is a shortcut method for <code>getLocalResources(clazz, resourceName, {""}, locale);</code>.
	 *
	 * @param clazz        the <code>Class</code> that is used to locate the resource.
	 * @param resourceName
	 * @param locale       the <code>Locale</code> that is used to locate the resource.
	 * @return an <code>URL</code> array (empty if no resource was found)
	 * @see #getLocalResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static URL[] getLocalResources(final Class clazz, final String resourceName, final Locale locale)
	{
		return getLocalResources(clazz, resourceName, NO_SUFFIX, locale, false);
	}

	/**
	 * This is a shortcut method for <code>getLocalResources(clazz, resourceName, {""}, null);</code>.
	 *
	 * @param clazz        the <code>Class</code> that is used to locate the resource.
	 * @param resourceName
	 * @return an <code>URL</code> array (empty if no resource was found)
	 * @see #getLocalResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static URL[] getLocalResources(final Class clazz, final String resourceName)
	{
		return getLocalResources(clazz, resourceName, NO_SUFFIX, null, false);
	}

	/**
	 * DOCUMENT: document
	 *
	 * @param clazz            the <code>Class</code> that is used to locate the resource.
	 * @param resourceBaseName
	 * @param suffixes
	 * @param locale           the <code>Locale</code> that is used to locate the resource.
	 * @return an <code>URL</code> to the resource or <code>null</code> if no resource was found.
	 * @see #getLocalResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static URL getLocalResource(final Class clazz, final String resourceBaseName, final String[] suffixes, final Locale locale)
	{
		URL results[] = getLocalResources(clazz, resourceBaseName, suffixes, locale, true);
		if(results.length == 0)
		{
			return null;
		}
		return results[0];
	}

	/**
	 * This is a shortcut method for <code>getLocalResource(clazz, resourceBaseName, suffixes, null);</code>.
	 *
	 * @param clazz            the <code>Class</code> that is used to locate the resource.
	 * @param resourceBaseName
	 * @param suffixes
	 * @return an <code>URL</code> to the resource or <code>null</code> if no resource was found.
	 * @see #getLocalResource(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see #getLocalResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static URL getLocalResource(final Class clazz, final String resourceBaseName, final String[] suffixes)
	{
		return getLocalResource(clazz, resourceBaseName, suffixes, null);
	}

	/**
	 * This is a shortcut method for <code>getLocalResource(clazz, resourceName, {""}, locale);</code>.
	 *
	 * @param clazz        the <code>Class</code> that is used to locate the resource.
	 * @param resourceName
	 * @param locale       the <code>Locale</code> that is used to locate the resource.
	 * @return an <code>URL</code> to the resource or <code>null</code> if no resource was found.
	 * @see #getLocalResource(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see #getLocalResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static URL getLocalResource(final Class clazz, final String resourceName, final Locale locale)
	{
		return getLocalResource(clazz, resourceName, NO_SUFFIX, locale);
	}

	/**
	 * This is a shortcut method for <code>getLocalResource(clazz, resourceName, {""}, null);</code>.
	 *
	 * @param clazz        the <code>Class</code> that is used to locate the resource.
	 * @param resourceName
	 * @return an <code>URL</code> to the resource or <code>null</code> if no resource was found.
	 * @see #getLocalResource(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see #getLocalResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static URL getLocalResource(final Class clazz, final String resourceName)
	{
		return getLocalResource(clazz, resourceName, NO_SUFFIX, null);
	}

	/**
	 * DOCUMENT:
	 *
	 * @param clazz            the <code>Class</code> that is used to locate the resource.
	 * @param resourceBaseName
	 * @param suffixes
	 * @param locale           the <code>Locale</code> that is used to locate the resource.
	 * @return an <code>InputStream</code> to the resource or <code>null</code> if no resource was found or opening the <code>InputStream</code> of the resource <code>URL</code> threw an exception.
	 * @see #getLocalResource(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see #getLocalResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static InputStream getLocalResourceAsStream(final Class clazz, final String resourceBaseName, final String[] suffixes, final Locale locale)
	{
		return getResourceStream(clazz, resourceBaseName, suffixes, locale, true);
	}

	/**
	 * This is a shortcut method for <code>getLocalResourceAsStream(clazz, resourceBaseName, suffixes, null);</code>.
	 *
	 * @param clazz            the <code>Class</code> that is used to locate the resource.
	 * @param resourceBaseName
	 * @param suffixes
	 * @return an <code>InputStream</code> to the resource or <code>null</code> if no resource was found or opening the <code>InputStream</code> of the resource <code>URL</code> threw an exception.
	 * @see #getLocalResourceAsStream(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see #getLocalResource(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see #getLocalResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static InputStream getLocalResourceAsStream(final Class clazz, final String resourceBaseName, final String[] suffixes)
	{
		return getLocalResourceAsStream(clazz, resourceBaseName, suffixes, null);
	}

	/**
	 * This is a shortcut method for <code>getLocalResourceAsStream(clazz, resourceName, {""}, locale);</code>.
	 *
	 * @param clazz        the <code>Class</code> that is used to locate the resource.
	 * @param resourceName
	 * @param locale       the <code>Locale</code> that is used to locate the resource.
	 * @return an <code>InputStream</code> to the resource or <code>null</code> if no resource was found or opening the <code>InputStream</code> of the resource <code>URL</code> threw an exception.
	 * @see #getLocalResourceAsStream(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see #getLocalResource(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see #getLocalResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static InputStream getLocalResourceAsStream(final Class clazz, final String resourceName, final Locale locale)
	{
		return getLocalResourceAsStream(clazz, resourceName, NO_SUFFIX, locale);
	}

	/**
	 * This is a shortcut method for <code>getLocalResourceAsStream(clazz, resourceName, {""}, null);</code>.
	 *
	 * @param clazz        the <code>Class</code> that is used to locate the resource.
	 * @param resourceName
	 * @return an <code>InputStream</code> to the resource or <code>null</code> if no resource was found or opening the <code>InputStream</code> of the resource <code>URL</code> threw an exception.
	 * @see #getLocalResourceAsStream(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see #getLocalResource(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see #getLocalResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static InputStream getLocalResourceAsStream(final Class clazz, final String resourceName)
	{
		return getLocalResourceAsStream(clazz, resourceName, NO_SUFFIX, null);
	}


	/**
	 * DOCUMENT:
	 * <p>
	 * This method returns an array of all URL's for the given resourceBaseName,
	 * suffixes and locale.	It will only return valid URLs.
	 * If no valid URLs exist at all this method returns an empty array.
	 * </p>
	 * <p>
	 * Note that the dot of a suffix needs to be included in the suffixes entries!
	 * </p>
	 * <p>
	 * Example:<br />
	 * <code><pre>
	 * package foobar;
	 * <p/>
	 * public class Foo
	 * {
	 *      public static class Bar
	 *      {
	 *      }
	 * }
	 * <p/>
	 * // in some method...
	 * Class c=foobar.Foo.Bar.class;
	 * String resourceBaseName="resource";
	 * String[] suffixes=new String[]{".txt", ".html"};
	 * Locale.setDefault(new Locale("en_US"));
	 * Locale locale=new Locale("de_DE");
	 * </pre></code>
	 * </p>
	 * <p>
	 * Result of the call getResources(c, resourceBaseName, suffixes, locale) if ALL files really exist:
	 * </p>
	 * <ul>
	 * <li>/foobar/Foo/Bar/de_DE/resource.txt</li>
	 * <li>/foobar/Foo/Bar/de_DE/resource.html</li>
	 * <li>/foobar/Foo/Bar/de/resource.txt</li>
	 * <li>/foobar/Foo/Bar/de/resource.html</li>
	 * <li>/foobar/Foo/Bar/en_US/resource.txt</li>
	 * <li>/foobar/Foo/Bar/en_US/resource.html</li>
	 * <li>/foobar/Foo/Bar/en/resource.txt</li>
	 * <li>/foobar/Foo/Bar/en/resource.html</li>
	 * <li>/foobar/Foo/Bar/resource.txt</li>
	 * <li>/foobar/Foo/Bar/resource.html</li>
	 * <li>[same for every parent class of foobar.Foo.Bar]</li>
	 * <li>[same for declaring class foobar.Foo]</li>
	 * <li>[same for every parent class of foobar.Foo]</li>
	 * </ul>
	 * <p>
	 * The resources /foobar/resource.txt and /foobar/resource.html are not returned to prevent
	 * name clashes of resources from different classes in the same package!
	 * This is a fundamental difference compared to c.getResource(String)/getResourceAsStream(String)
	 * </p>
	 *
	 * @param clazz            the <code>Class</code> that is used to locate the resource.
	 * @param resourceBaseName
	 * @param suffixes
	 * @param locale           the <code>Locale</code> that is used to locate the resource.
	 * @return an <code>URL</code> array (empty if no resource was found)
	 * @see #getLocalResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see Class#getResource(java.lang.String)
	 * @see Class#getResourceAsStream(java.lang.String)
	 */
	public static URL[] getResources(final Class clazz, final String resourceBaseName, final String[] suffixes, final Locale locale)
	{
		return getResources(clazz, resourceBaseName, suffixes, locale, false);
	}

	/**
	 * This is a shortcut method for <code>getResources(clazz, resourceBaseName, suffixes, null);</code>.
	 *
	 * @param clazz            the <code>Class</code> that is used to locate the resource.
	 * @param resourceBaseName
	 * @param suffixes
	 * @return an <code>URL</code> array (empty if no resource was found)
	 * @see #getResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static URL[] getResources(final Class clazz, final String resourceBaseName, final String[] suffixes)
	{
		return getResources(clazz, resourceBaseName, suffixes, null, false);
	}

	/**
	 * This is a shortcut method for <code>getResources(clazz, resourceName, {""}, locale);</code>.
	 *
	 * @param clazz        the <code>Class</code> that is used to locate the resource.
	 * @param resourceName
	 * @param locale       the <code>Locale</code> that is used to locate the resource.
	 * @return an <code>URL</code> array (empty if no resource was found)
	 * @see #getResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static URL[] getResources(final Class clazz, final String resourceName, final Locale locale)
	{
		return getResources(clazz, resourceName, NO_SUFFIX, locale, false);
	}

	/**
	 * This is a shortcut method for <code>getResources(clazz, resourceName, {""}, null);</code>.
	 *
	 * @param clazz        the <code>Class</code> that is used to locate the resource.
	 * @param resourceName
	 * @return an <code>URL</code> array (empty if no resource was found)
	 * @see #getResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static URL[] getResources(final Class clazz, final String resourceName)
	{
		return getResources(clazz, resourceName, NO_SUFFIX, null, false);
	}

	/**
	 * DOCUMENT: document
	 *
	 * @param clazz            the <code>Class</code> that is used to locate the resource.
	 * @param resourceBaseName
	 * @param suffixes
	 * @param locale           the <code>Locale</code> that is used to locate the resource.
	 * @return an <code>URL</code> to the resource or <code>null</code> if no resource was found.
	 * @see #getResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static URL getResource(final Class clazz, final String resourceBaseName, final String[] suffixes, final Locale locale)
	{
		URL[] resourceUrls = getResources(clazz, resourceBaseName, suffixes, locale, true);
		if(resourceUrls.length == 0)
		{
			return null;
		}
		return resourceUrls[0];
	}

	/**
	 * This is a shortcut method for <code>getResource(clazz, resourceBaseName, suffixes, null);</code>.
	 *
	 * @param clazz            the <code>Class</code> that is used to locate the resource.
	 * @param resourceBaseName
	 * @param suffixes
	 * @return an <code>URL</code> to the resource or <code>null</code> if no resource was found.
	 * @see #getResource(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static URL getResource(final Class clazz, final String resourceBaseName, final String[] suffixes)
	{
		return getResource(clazz, resourceBaseName, suffixes, null);
	}

	/**
	 * This is a shortcut method for <code>getResource(clazz, resourceName, {""}, locale);</code>.
	 *
	 * @param clazz        the <code>Class</code> that is used to locate the resource.
	 * @param resourceName
	 * @param locale       the <code>Locale</code> that is used to locate the resource.
	 * @return an <code>URL</code> to the resource or <code>null</code> if no resource was found.
	 * @see #getResource(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static URL getResource(final Class clazz, final String resourceName, final Locale locale)
	{
		return getResource(clazz, resourceName, NO_SUFFIX, locale);
	}

	/**
	 * This is a shortcut method for <code>getResource(clazz, resourceName, {""}, null);</code>.
	 *
	 * @param clazz        the <code>Class</code> that is used to locate the resource.
	 * @param resourceName
	 * @return an <code>URL</code> to the resource or <code>null</code> if no resource was found.
	 * @see #getResource(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static URL getResource(final Class clazz, final String resourceName)
	{
		return getResource(clazz, resourceName, NO_SUFFIX, null);
	}


	/**
	 * DOCUMENT:
	 *
	 * @param clazz            the <code>Class</code> that is used to locate the resource.
	 * @param resourceBaseName
	 * @param suffixes
	 * @param locale           the <code>Locale</code> that is used to locate the resource.
	 * @return an <code>InputStream</code> to the resource or <code>null</code> if no resource was found or opening the <code>InputStream</code> of the resource <code>URL</code> threw an exception.
	 * @see #getResource(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see #getResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static InputStream getResourceAsStream(final Class clazz, final String resourceBaseName, final String[] suffixes, final Locale locale)
	{
		return getResourceStream(clazz, resourceBaseName, suffixes, locale, false);
	}

	/**
	 * This is a shortcut method for <code>getResourceAsStream(clazz, resourceBaseName, suffixes, null);</code>.
	 *
	 * @param clazz            the <code>Class</code> that is used to locate the resource.
	 * @param resourceBaseName
	 * @param suffixes
	 * @return an <code>InputStream</code> to the resource or <code>null</code> if no resource was found or opening the <code>InputStream</code> of the resource <code>URL</code> threw an exception.
	 * @see #getResourceAsStream(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see #getResource(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see #getResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static InputStream getResourceAsStream(final Class clazz, final String resourceBaseName, final String[] suffixes)
	{
		return getResourceAsStream(clazz, resourceBaseName, suffixes, null);
	}

	/**
	 * This is a shortcut method for <code>getResourceAsStream(clazz, resourceName, {""}, locale);</code>.
	 *
	 * @param clazz        the <code>Class</code> that is used to locate the resource.
	 * @param resourceName
	 * @param locale       the <code>Locale</code> that is used to locate the resource.
	 * @return an <code>InputStream</code> to the resource or <code>null</code> if no resource was found or opening the <code>InputStream</code> of the resource <code>URL</code> threw an exception.
	 * @see #getResourceAsStream(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see #getResource(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see #getResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static InputStream getResourceAsStream(final Class clazz, final String resourceName, final Locale locale)
	{
		return getResourceAsStream(clazz, resourceName, NO_SUFFIX, locale);
	}

	/**
	 * This is a shortcut method for <code>getResourceAsStream(clazz, resourceName, {""}, null);</code>.
	 *
	 * @param clazz        the <code>Class</code> that is used to locate the resource.
	 * @param resourceName
	 * @return an <code>InputStream</code> to the resource or <code>null</code> if no resource was found or opening the <code>InputStream</code> of the resource <code>URL</code> threw an exception.
	 * @see #getResourceAsStream(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see #getResource(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see #getResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	public static InputStream getResourceAsStream(final Class clazz, final String resourceName)
	{
		return getResourceAsStream(clazz, resourceName, NO_SUFFIX, null);
	}


	// misc helper-methods below

	/**
	 * Returns the short classname of the given <code>Class</code> without package e.g.
	 * Bar for class foo.Bar.
	 *
	 * @param clazz the <code>Class</code> for which the short classname should be resolved.
	 * @return the short classname of the given <code>Class</code>
	 */
	public static String getShortClassName(final Class clazz)
	{
		String fullName = clazz.getName();

		int idx = fullName.lastIndexOf('.');
		if(idx == -1)
		{
			return fullName;
		}
		idx++;
		return fullName.substring(idx);
	}

	/**
	 * Returns a <code>String</code> array that contains all <code>Locale</code>
	 * suffixes for both the given <code>locale</code> and the default <code>Locale</code> in this order.
	 * The resulting array will not contain duplicate entries.
	 * <p/>
	 * <h4>Examples:</h4>
	 * <p><code>Locale("de_DE")</code> and default <code>Locale("en_US")</code> returns <code>{"de_DE", "de", "en_US", "en"}</code><br />
	 * <code>Locale("de")</code> and default <code>Locale("en_US")</code> returns <code>{"de", "en_US", "en"}</code><br />
	 * <code>Locale("en_US")</code> and default <code>Locale("en_US")</code> returns <code>{"en_US", "en"}</code><br />
	 * <code>null</code> and default <code>null</code> returns <code>{}</code></p>
	 *
	 * @param locale the <code>Locale</code> for which a suffix array will be created (beside <code>Locale.getDefault()</code>).
	 * @return a <code>String</code> array containing all representations
	 *         of the given <code>locale</code> and the default <code>Locale</code> with the most specific first.
	 *         There won't be any duplicates if they overlap.
	 * @see Locale#getDefault()
	 * @see Locale#setDefault(java.util.Locale)
	 */
	public static String[] getLocaleSuffixArray(final Locale locale)
	{
		String localeSuf[] = getSingleLocaleSuffixArray(locale);
		String defaultSuf[] = getSingleLocaleSuffixArray(Locale.getDefault());

		List<String> resultList = new ArrayList<String>(localeSuf.length + defaultSuf.length);
		for(String currentSuffix : localeSuf)
		{
			if(!resultList.contains(currentSuffix))
			{
				resultList.add(currentSuffix);
			}
		}
		for(String currentSuffix : defaultSuf)
		{
			if(!resultList.contains(currentSuffix))
			{
				resultList.add(currentSuffix);
			}
		}
		String result[] = new String[resultList.size()];
		resultList.toArray(result);

		return result;
	}

	/**
	 * Returns a <code>String</code> array that contains at most
	 * [language + "_" + country + "_" + variant, language + "_" + country, language] for the given <code>locale</code>.
	 * Returns an empty array if <code>locale</code> is <code>null</code> or empty.
	 * <p/>
	 * <h4>Examples:</h4>
	 * <p><code>Locale("de")</code> returns <code>{"de"}</code><br />
	 * <code>Locale("de","DE")</code> returns <code>{"de_DE", "de"}</code><br />
	 * <code>Locale("de","DE","hessisch")</code> returns <code>{"de_DE_hessisch", "de_DE", "de"}</code><br />
	 * <code>Locale("","","foo")</code> returns <code>{"__foo"}</code><br />
	 * <code>Locale("foo","","bar")</code> returns <code>{"foo__bar", "foo"}</code><br />
	 * <code>Locale("","","")</code> and <code>null</code> return <code>{}</code></p>
	 *
	 * @param locale the <code>Locale</code> for which a suffix array will be created.
	 * @return a <code>String</code> array containing all representations of the given locale with the most specific first.
	 */
	public static String[] getSingleLocaleSuffixArray(final Locale locale)
	{
		if(locale == null)
		{
			return EMPTY_STRING_ARRAY;
		}

		final String language = locale.getLanguage();
		final int languageLength = language.length();
		final String country = locale.getCountry();
		final int countryLength = country.length();
		final String variant = locale.getVariant();
		final int variantLength = variant.length();

		List<String> resultList = new ArrayList<String>(3);

		if(languageLength + countryLength + variantLength != 0)
		{
			final StringBuilder temp = new StringBuilder();
			//temp.append('_');
			temp.append(language);
			if(languageLength > 0)
			{
				resultList.add(temp.toString());
			}

			if(countryLength + variantLength != 0)
			{
				temp.append('_');
				temp.append(country);
				if(countryLength > 0)
				{
					resultList.add(temp.toString());
				}

				if(variantLength != 0)
				{
					temp.append('_');
					temp.append(variant);
					resultList.add(temp.toString());
				}
			}
		}
		String result[] = new String[resultList.size()];
		Collections.reverse(resultList);
		resultList.toArray(result);

		return result;
	}


	/**
	 * This method returns the path to the class-file of the class without
	 * the .class-Extension, e.g. for class foo.Bar this method will return
	 * the String "/foo/Bar".
	 * Be aware that the internal class foo.Bar$Foobar is mapped to /foo/Bar/Foobar
	 * instead of /foo/Bar$Foobar!
	 *
	 * @param clazz the <code>Class</code> for which the path should be created.
	 * @return generally the path to the class-file of the class without the .class-extension. Inner classes are handled differently.
	 */
	public static String getPathToClass(final Class clazz)
	{
		String className = clazz.getName();

		StringBuilder result = new StringBuilder(className.length() + 1);

		result.append("/");
		className = className.replace('.', '/');
		result.append(className.replace('$', '/')); // use subdirs for internal classes instead

		return result.toString();
	}


	/**
	 * This method returns the path to the package of the given class, e.g. for class
	 * foo.Bar this method will return the String "/foo".
	 * Be aware, however, that no trailing separator is added to this path!
	 *
	 * @param clazz the <code>Class</code> for which the package path should be resolved.
	 * @return the path to the package of the class
	 */
	public static String getPathToPackage(final Class clazz)
	{
		Package p = clazz.getPackage();
		if(p == null)
		{
			return "/";
		}
		return "/" + p.getName().replace('.', '/');
	}

//    /**
//     * The suffixes that will be used for resource maps.
//     * Currently only java.util.Properties (".properties") are supported.
//     */
//    private static final String[] BUNDLE_SUFFIXES = {
//        PROPERTY_BUNDLE_SUFFIX,
//    };

	/**
	 * Used internally to replace suffixes that are null or zero length.
	 */
	private static final String[] NO_SUFFIX = new String[]{""};

	/**
	 * private constructor, no instances needed/possible.
	 */
	private Resources()
	{

	}


	/**
	 * Reads the contents of the given InputStream and returns a list that contains each
	 * line of the stream that's neither empty (after stripping whitespaces)
	 * nor a comment (starting with a #).
	 *
	 * @param is the stream to be read into a list of strings.
	 * @return a List containing all lines that are neither empty nor a comment
	 * @throws IOException if reading of the stream fails.
	 */
	private static List<String> readLinkInputStream(InputStream is)
		throws IOException
	{
		final Logger logger = LoggerFactory.getLogger(Resources.class);

		List<String> result = new ArrayList<String>();
		BufferedReader br = null;
		IOException exception = null;
		try
		{
			InputStreamReader isr = new InputStreamReader(is);
			br = new BufferedReader(isr);

			String sl;
			for(; ;)
			{
				sl = br.readLine();
				if(sl == null)
				{
					break;
				}
				sl = sl.trim();
				if(sl.length() == 0)
				{
					continue;
				}
				if(sl.startsWith("#"))
				{
					if(logger.isDebugEnabled()) logger.debug("Comment: " + sl.substring(1));
					continue;
				}
				result.add(sl);
			}
		}
		catch(IOException ex)
		{
			exception = ex;
		}
		finally
		{
			if(br != null)
			{
				try
				{
					br.close();
				}
				catch(IOException ex)
				{
					// ignore
				}
			}
		}
		if(exception != null)
		{
			// rethrow after correct close...
			throw exception;
		}
		return result;
	}

	private static URL resolveLink(final Class clazz, final String resourcePath)
	{
		Stack<String> stack = new Stack<String>();

		return recursiveResolve(stack, clazz, resourcePath);
	}

	private static URL recursiveResolve(Stack<String> stack, Class clazz, String resourcePath)
	{
		final Logger logger = LoggerFactory.getLogger(Resources.class);

		String resourceLinkPath = resourcePath + LINK_SUFFIX;
		URL result = null;

		InputStream is = clazz.getResourceAsStream(resourceLinkPath);
		if(is != null)
		{
			// we found a link!
			// check for cyclic link and add current resourceLinkPath
			// to the linkStack
			if(logger.isDebugEnabled())
			{
				logger.debug("Found a link '" + resourceLinkPath + "' for resource '" + resourcePath + "'.");
			}

			// this is necessary because Class.getResourceAsStream is case-insensitive
			String lowLinkPath = resourceLinkPath.toLowerCase();
			if(stack.contains(lowLinkPath))
			{
				// the exception is only used for logging (stack-trace) and won't be thrown...
				if(logger.isWarnEnabled())
				{
					//noinspection ThrowableInstanceNeverThrown
					logger.warn("Found a cyclic link!", new CyclicLinkException(stack, resourceLinkPath));
				}
				return null;
			}
			stack.push(lowLinkPath);

			List<String> linkContent;
			try
			{
				// read and parse the linkContent...
				linkContent = readLinkInputStream(is);

				Iterator iter = linkContent.iterator();
				while(result == null && iter.hasNext())
				{
					String currentLinkTarget = (String) iter.next();
					// empty lines are allready ignored in readLinkInputStream
					// Stack is cloned to support multiple lines...
					String basePath = PathTools.getParentPath(resourceLinkPath);
					String previousLinkTarget=currentLinkTarget;
					currentLinkTarget = PathTools.getAbsolutePath(basePath, previousLinkTarget);
					if(currentLinkTarget == null)
					{
						if(logger.isDebugEnabled())
						{
							logger
								.debug("getAbsolutePath(\"" + basePath + "\", \"" + previousLinkTarget + "\") returned null - no valid absolute path found.");
						}
					}
					else
					{
						if(logger.isDebugEnabled())
						{
							logger.debug("Checking for link-target '" + currentLinkTarget + "'.");
						}
						result = recursiveResolve((Stack<String>) stack.clone(), clazz, currentLinkTarget);
						if(result != null)
						{
							if(logger.isDebugEnabled()) logger.debug("Found link-target '" + currentLinkTarget + "'.");
						}
						else
						{
							if(logger.isDebugEnabled())
							{
								logger.debug("Found unsatisfied link '" + currentLinkTarget + '.');
							}
						}
					}
				}
			}
			catch(IOException ex)
			{
				if(logger.isWarnEnabled())
				{
					logger.warn("Exception while reading link-content of '" + resourceLinkPath + "'.", ex);
				}
			}
			if(result != null)
			{
				// if we found a result return it...
				return result;
			}
		}// end if link

		// ... otherwise simply return the original resource-stream if available.
		return clazz.getResource(resourcePath);
	}

	/**
	 * Used by the private getLocalResources and getResources methods to collect the resources.
	 *
	 * @param urlList
	 * @param clazz            the <code>Class</code> that is used to locate the resource.
	 * @param resourceBaseName
	 * @param suffixes
	 * @param locale           the <code>Locale</code> that is used to locate the resource.
	 * @param firstOnly        if <code>true</code>, this method will stop searching after a single result has been found, returning immediatly.
	 * @see #getLocalResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale, boolean)
	 * @see #getResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale, boolean)
	 */
	private static void collectResources(List<URL> urlList, final Class clazz, final String resourceBaseName, final String[] suffixes, final Locale locale, final boolean firstOnly)
	{
		final Logger logger = LoggerFactory.getLogger(Resources.class);
		Class currentClass = clazz;
		while(currentClass != java.lang.Object.class)
		{
			if(logger.isDebugEnabled()) logger.debug("currentClass = " + currentClass.getName());
			URL urls[] = getLocalResources(currentClass, resourceBaseName, suffixes, locale, firstOnly);
			for(int i = 0; i < urls.length; i++)
			{
				if(!urlList.contains(urls[i]))
				{
					urlList.add(urls[i]);
					if(logger.isDebugEnabled()) logger.debug("added url[" + i + "]: " + urls[i]);
					if(firstOnly)
					{
						return;
					}
				}
			}
			currentClass = currentClass.getSuperclass();
		}
	}

	/**
	 * private method that is used by the public getLocalResource/getLocalResources methods.
	 *
	 * @param clazz            the <code>Class</code> that is used to locate the resource.
	 * @param resourceBaseName
	 * @param suffixes
	 * @param locale           the <code>Locale</code> that is used to locate the resource.
	 * @param firstOnly        if <code>true</code>, this method will stop searching after a single result has been found, returning immediatly.
	 * @return an <code>URL</code> array (empty if no resource was found)
	 * @see #getLocalResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see #getLocalResource(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	private static URL[] getLocalResources(final Class clazz, final String resourceBaseName, final String[] suffixes, final Locale locale, final boolean firstOnly)
	{
		final Logger logger = LoggerFactory.getLogger(Resources.class);

		String basePath = getPathToClass(clazz);

		String[] suff;
		if(suffixes == null || suffixes.length == 0)
		{
			suff = NO_SUFFIX;
		}
		else
		{
			suff = suffixes;
		}

		List<URL> urls = new ArrayList<URL>();
		// handle locale parameter localePaths
		String localePaths[] = getLocaleSuffixArray(locale);
		for(String currentLocPath : localePaths)
		{
			for(String aSuff : suff)
			{
				String resourceName = resourceBaseName + aSuff;

				String currentBase = basePath;
				if(currentLocPath.length() != 0)
				{
					currentBase = currentBase + "/" + currentLocPath;
				}

				String currentPath = PathTools.getAbsolutePath(currentBase, resourceName);

				if(currentPath == null)
				{
					if(logger.isDebugEnabled())
					{
						logger
							.debug("getAbsolutePath(\"" + currentBase + "\", \"" + resourceName + "\") returned null - no valid absolute path found.");
					}
				}
				else
				{
					if(logger.isDebugEnabled())
					{
						logger.debug("Trying to obtain URL for resource '" + currentPath + "'.");
					}
					URL url = resolveLink(clazz, currentPath);
					if(url != null && !urls.contains(url))
					{
						if(firstOnly)
						{
							return new URL[]{url};
						}
						urls.add(url);
						if(logger.isDebugEnabled())
						{
							logger.debug("Obtained new URL \"" + url + "\" for resource '" + currentPath + "'.");
						}
					}
				}
			}
		}
		// check for resource in Class-folder without suffix
		for(String aSuff : suff)
		{
			String resourceName = resourceBaseName + aSuff;
			String absResourcePath = PathTools.getAbsolutePath(basePath, resourceName);
			if(absResourcePath == null)
			{
				if(logger.isDebugEnabled())
				{
					logger
						.debug("getAbsolutePath(\"" + basePath + "\", \"" + resourceName + "\") returned null - no valid absolute path found.");
				}
			}
			else
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("Trying to obtain URL for resource '" + absResourcePath + "'.");
				}
				URL url = resolveLink(clazz, absResourcePath);
				if(url != null && !urls.contains(url))
				{
					if(firstOnly)
					{
						return new URL[]{url};
					}
					urls.add(url);
					if(logger.isDebugEnabled())
					{
						logger.debug("Obtained new URL \"" + url + "\" for resource '" + absResourcePath + "'.");
					}
				}
			}
		}

		URL[] result = new URL[urls.size()];
		urls.toArray(result);
		return result;
	}

	/**
	 * private method that is used by the public getResource/getResources methods.
	 *
	 * @param clazz            the <code>Class</code> that is used to locate the resource.
	 * @param resourceBaseName
	 * @param suffixes
	 * @param locale           the <code>Locale</code> that is used to locate the resource.
	 * @param firstOnly        if <code>true</code>, this method will stop searching after a single result has been found, returning immediatly.
	 * @return an <code>URL</code> array (empty if no resource was found)
	 * @see #getResources(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 * @see #getResource(java.lang.Class, java.lang.String, java.lang.String[], java.util.Locale)
	 */
	private static URL[] getResources(final Class clazz, final String resourceBaseName, final String[] suffixes, final Locale locale, final boolean firstOnly)
	{
		final Logger logger = LoggerFactory.getLogger(Resources.class);

		List<URL> urlList = new ArrayList<URL>();
		// collect resources from given class...
		collectResources(urlList, clazz, resourceBaseName, suffixes, locale, firstOnly);
		if(!firstOnly || urlList.size() == 0)
		{
			// collect resources from declaring classes...
			Class currentClass = clazz.getDeclaringClass();
			while(currentClass != null)
			{
				collectResources(urlList, currentClass, resourceBaseName, suffixes, locale, firstOnly);
				if(firstOnly && urlList.size() != 0)
				{
					break;
				}
				currentClass = currentClass.getDeclaringClass();
			}
		}

		URL[] result = new URL[urlList.size()];
		urlList.toArray(result);

		if(logger.isInfoEnabled() && result.length == 0)
		{
			StringBuilder msg = new StringBuilder();
			msg.append("Couldn't obtain any URL's for resource '").append(resourceBaseName).append("'");
			if(suffixes != null && suffixes.length != 0)
			{
				if(suffixes.length == 1 && suffixes[0].length() == 0)
				{
					msg.append(" with no suffix. ");
				}
				else
				{
					msg.append(" with suffix(es) [");
					for(int i = 0; i < suffixes.length; i++)
					{
						if(i != 0)
						{
							msg.append(",");
						}
						msg.append("\"").append(suffixes[i]).append("\"");
					}
					msg.append("]. ");
				}
			}
			msg.append("Search started at '").append(clazz).append("'.");
			logger.info(msg.toString());
		}
		return result;
	}


	private static InputStream getResourceStream(final Class clazz, final String resourceBaseName, final String[] suffixes, final Locale locale, final boolean local)
	{
		InputStream result = null;
		URL resource;
		if(local)
		{
			resource = getLocalResource(clazz, resourceBaseName, suffixes, locale);
		}
		else
		{
			resource = getResource(clazz, resourceBaseName, suffixes, locale);
		}
		if(resource != null)
		{
			try
			{
				result = resource.openStream();
			}
			catch(IOException ex)
			{
				final Logger logger = LoggerFactory.getLogger(Resources.class);
				if(logger.isWarnEnabled())
				{
					logger.warn("IOException while opening URL-Connection for URL '" + resource + "'!", ex);
				}
			}
		}
		return result;
	}

}
