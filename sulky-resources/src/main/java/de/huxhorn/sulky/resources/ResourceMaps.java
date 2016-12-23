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

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides functionality for inheritance-safe resolution of resource maps.
 *
 * They are a replacement for ResourceBundle and provide similar functionality.
 */
public final class ResourceMaps
{
	private static final MapLoader MAP_LOADER = MapLoader.getInstance();
	private static final String[] MAP_SUFFIXES;

	static
	{
		List<String> suff = MAP_LOADER.getSupportedSuffixes();
		String[] suffs = new String[suff.size()];
		suffs = suff.toArray(suffs);
		MAP_SUFFIXES = suffs;
	}

	private ResourceMaps()
	{}

	/**
	 * Returns a map containing the key-value-pairs of all bundles
	 * retrieved by calling getResources(clazz, resourceMapBaseName, BUNDLE_SUFFIXES, locale).
	 * Bundles with a low index in the retrieved array will override settings in bundles with a
	 * higher index.
	 *
	 * @param clazz               the <code>Class</code> that is used to locate the resource.
	 * @param resourceMapBaseName the basename of the bundle without extension.
	 * @param locale              the <code>Locale</code> that is used to locate the resource.
	 * @return a map containing the key-value-pairs of the bundles found.
	 * @see de.huxhorn.sulky.resources.Resources#getResources(Class, String, String[], java.util.Locale)
	 */
	public static Map<String, Object> getResourceMap(final Class clazz, final String resourceMapBaseName, final Locale locale)
	{
		return getResourceMap(clazz, resourceMapBaseName, locale, false);
	}

	public static Map<String, Object> getResourceMap(final Class clazz, final String resourceMapBaseName)
	{
		return getResourceMap(clazz, resourceMapBaseName, null, false);
	}

	public static Map<String, Object> getLocalResourceMap(final Class clazz, final String resourceMapBaseName, final Locale locale)
	{
		return getResourceMap(clazz, resourceMapBaseName, locale, true);
	}

	public static Map<String, Object> getLocalResourceMap(final Class clazz, final String resourceMapBaseName)
	{
		return getResourceMap(clazz, resourceMapBaseName, null, true);
	}

	/**
	 * private method that is used by the public getResourceMap/getLocalResourceMap methods.
	 *
	 * @param clazz               the <code>Class</code> that is used to locate the resource.
	 * @param resourceMapBaseName the base-name of the resource map.
	 * @param locale              the <code>Locale</code> that is used to locate the resource.
	 * @param local               if <code>true</code>, this method will perform a local search for the bundle (getLocalResources), otherwise the inheritance hierarchy is searched (getResources).
	 * @return a <code>Map</code> containing the cumulated resources.
	 * @see #getLocalResourceMap(java.lang.Class, java.lang.String, java.util.Locale)
	 * @see #getResourceMap(java.lang.Class, java.lang.String, java.util.Locale)
	 */
	private static Map<String, Object> getResourceMap(final Class clazz, final String resourceMapBaseName, final Locale locale, final boolean local)
	{
		final Logger logger = LoggerFactory.getLogger(Resources.class);

		Map<String, Object> result = new HashMap<>();

		URL[] resourceUrls;
		if(local)
		{
			resourceUrls = Resources.getLocalResources(clazz, resourceMapBaseName, MAP_SUFFIXES, locale);
		}
		else
		{
			resourceUrls = Resources.getResources(clazz, resourceMapBaseName, MAP_SUFFIXES, locale);
		}

		if(logger.isDebugEnabled())
		{
			StringBuilder debug = new StringBuilder();
			for(int i = 0; i < resourceUrls.length; i++)
			{
				debug.append("URL[");
				debug.append(i);
				debug.append("]: ");
				debug.append(resourceUrls[i]);
				debug.append("\n");
			}
			logger.debug(debug.toString());
		}
		for(URL resourceUrl : resourceUrls)
		{
			try
			{
				MAP_LOADER.mergeMaps(resourceUrl, result, false);
			}
			catch(IOException ex)
			{
				if(logger.isWarnEnabled()) logger.warn("IOException while loading resource map \"{}\"!", resourceUrl, ex);
			}
		}

		if(logger.isDebugEnabled())
		{
			StringBuilder buffer = new StringBuilder();

			for(Map.Entry<String, Object> current : result.entrySet())
			{
				Object key = current.getKey();
				Object value = current.getValue();
				buffer.append("Key: ");
				buffer.append(key);
				buffer.append("    Value: ");
				buffer.append(value);
				buffer.append("\n");

			}
			logger.debug("ResourceMap for \"" + resourceMapBaseName + "\":\n" + buffer.toString());
		}
		return result;
	}

}
