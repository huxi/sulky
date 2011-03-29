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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * DOCUMENT: <code>MapLoader</code>
 */
public abstract class MapLoader
{

	private final List<String> suffixes;
	private static final MapLoader INSTANCE = new PropertiesMapLoader();

	public static MapLoader getInstance()
	{
		return INSTANCE;
	}

	protected MapLoader(String[] suffixes)
	{
		if(suffixes.length == 0)
		{
			throw new IllegalArgumentException("MapLoader must have a suffix.");
		}
		Logger logger = LoggerFactory.getLogger(MapLoader.class);

		List<String> suff = new ArrayList<String>(suffixes.length);
		for(int i = 0; i < suffixes.length; i++)
		{
			String cur = suffixes[i];
			if(cur == null)
			{
				if(logger.isWarnEnabled()) logger.warn("suffixes[" + i + "] is null!");
			}
			else
			{
				cur = cur.trim().toLowerCase();
				if(cur.length() != 0)
				{
					if(suff.contains(cur))
					{
						if(logger.isWarnEnabled())
						{
							logger.warn("Duplicate suffix entry at suffixes[" + i + "]: " + cur);
						}
					}
					else
					{
						suff.add(cur);
					}
				}
			}
		}
		this.suffixes = Collections.unmodifiableList(suff);
	}

	/**
	 * @return an unmodifiable List of the supported suffixes, which are always lower case.
	 */
	public final List<String> getSupportedSuffixes()
	{
		return suffixes;
	}

	public boolean isSupported(URL url)
	{
		String fileName = url.getPath().toLowerCase();
		for(String suffix : suffixes)
		{
			if(fileName.endsWith(suffix))
			{
				return true;
			}
		}
		return false;
	}

	/**
	 * Loads the <code>Map</code> from the given <code>URL</code>.
	 * If this worked it will be merged into <code>result</code>.
	 * If <code>overwrite</code> is <code>true</code>, non-<code>null</code>
	 * entries are overwritten if also contained in the loaded <code>Map</code>
	 * and not <code>null</code>. Otherwise, already existing entries in
	 * <code>result</code> will not be replaced.
	 *
	 * @param url       the source URL of the <code>Map</code> to be loaded.
	 * @param result    the result map that will be merged with the loaded map. Will be created if <code>null</code>.
	 * @param overwrite if <code>true</code>, non-<code>null</code> values in result will be replaced with non-<code>null</code> values of the loaded map.
	 * @return the merged map
	 * @throws IOException if loading the map fails.
	 */
	public Map<String, Object> mergeMaps(URL url, Map<String, Object> result, boolean overwrite)
		throws IOException
	{
		if(result == null)
		{
			result = new HashMap<String, Object>();
		}

		Map<String, Object> map = loadMap(url);
		if(map != null)
		{
			for(Map.Entry<String, Object> current : map.entrySet())
			{
				String key = current.getKey();
				Object value = current.getValue();
				if(value != null)
				{
					Object previous = result.get(key);
					if(overwrite || previous == null)
					{
						result.put(key, value);
					}
				}
			}
		}
		return result;
	}

	public abstract Map<String, Object> loadMap(URL url)
		throws IOException;
}

class PropertiesMapLoader
	extends MapLoader
{
	public static final String PROPERTY_BUNDLE_SUFFIX = ".properties";

	public PropertiesMapLoader()
	{
		super(new String[]{PROPERTY_BUNDLE_SUFFIX});
	}

	public Map<String, Object> loadMap(URL url)
		throws IOException
	{
		if(!isSupported(url))
		{
			return null;
		}
		Map<String, Object> result = new HashMap<String, Object>();
		InputStream is = url.openStream();

		Properties bundle = new Properties();
		bundle.load(is);

		Enumeration e = bundle.keys();
		while(e.hasMoreElements())
		{
			String key = (String) e.nextElement();
			Object value = bundle.get(key);
			if(value != null)
			{
				result.put(key, value);
			}
		}

		return result;
	}
}