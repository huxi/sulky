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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.Map;

/**
 * DOCUMENT: <code>ResourceSupport</code>
 * There are two distinct usages of this class:
 * <ul>
 * <li>In a static context.<br />
 * <code>private static final ResourceSupport RESOURCES=new ResourceSupport(Foo.class);</code>
 * </li>
 * <li>In a non-static context.<br />
 * <code>private final ResourceSupport resources=new ResourceSupport(this);</code>
 * </li>
 * </ul>
 * The usage of this class is identical in both cases.
 * The non-static version can be used for classes that implement the
 * <code>Localizable</code> interface (either directly or indirectly).
 *
 * @see Localizable
 */
public class ResourceSupport
{

	private final Class clazz;
	private final Object object;
	private final Localizable localizable;

	public ResourceSupport(Object object)
	{
		Logger logger = LoggerFactory.getLogger(ResourceSupport.class);
		if(object == null)
		{
			NullPointerException ex = new NullPointerException("obj must not be null!");
			if(logger.isDebugEnabled())
			{
				logger.debug("Parameter 'obj' of method 'ResourceSupport' must not be null!", ex);
			}
			throw ex;
		}
		Class c = object.getClass();
		if(c == java.lang.Class.class)
		{
			this.clazz = (Class) object;
			this.object = null;
			this.localizable = null;
			if(logger.isDebugEnabled()) logger.debug("Parameter of ResourceSupport constructor was class " + clazz);
		}
		else
		{
			this.clazz = c;
			this.object = object;
			localizable = LocalizableFactory.getLocalizable(object);
			if(logger.isDebugEnabled())
			{
				logger.debug("Parameter of ResourceSupport constructor was an instance of class " + clazz);
			}
		}
	}

	/**
	 * Returns the argument <code>locale</code> if this <code>ResourceSupport</code>
	 * was created in a static context.
	 * Otherwise a possibly existing getLocale method is called on the contained object.
	 * If the result is not <code>null</code> it is returned. Otherwise
	 * <code>locale</code> is returned.
	 *
	 * @param locale the locale that will be used if an instance locale can not be obtained.
	 * @return the resolved <code>Locale</code>
	 */
	public Locale resolveLocale(final Locale locale)
	{
		Locale result = null;
		if(localizable != null)
		{
			result = localizable.getLocale();
		}

		if(result == null)
		{
			result = locale;
		}

		return result;
	}

	public Class getResourceClass()
	{
		return clazz;
	}

	public Object getResourceObject()
	{
		return object;
	}

	/**
	 * Returns the Localizable (either explicit or implicit) for the contained object.
	 * If this ResourceSupport was created in a static context this method will always return null.
	 *
	 * @return the contained object as a <code>Localizable</code> or <code>null</code>
	 *         if this <code>ResourceSupport</code> was created in a static context.
	 * @see Localizable
	 */
	public Localizable getLocalizable()
	{
		return localizable;
	}


	public URL[] getLocalResources(final String resourceBaseName, final String[] suffixes, final Locale locale)
	{
		if(object != null)
		{
			return Resources.getLocalResources(clazz, resourceBaseName, suffixes, resolveLocale(locale));
		}
		return Resources.getLocalResources(clazz, resourceBaseName, suffixes, locale);
	}

	public URL[] getLocalResources(final String resourceBaseName, final String[] suffixes)
	{
		return getLocalResources(resourceBaseName, suffixes, null);
	}

	public URL[] getLocalResources(final String resourceName, final Locale locale)
	{
		return getLocalResources(resourceName, null, locale);
	}

	public URL[] getLocalResources(final String resourceName)
	{
		return getLocalResources(resourceName, null, null);
	}


	public URL getLocalResource(final String resourceBaseName, final String[] suffixes, final Locale locale)
	{
		if(object != null)
		{
			return Resources.getLocalResource(clazz, resourceBaseName, suffixes, resolveLocale(locale));
		}
		return Resources.getLocalResource(clazz, resourceBaseName, suffixes, locale);
	}

	public URL getLocalResource(final String resourceBaseName, final String[] suffixes)
	{
		return getLocalResource(resourceBaseName, suffixes, null);
	}

	public URL getLocalResource(final String resourceName, final Locale locale)
	{
		return getLocalResource(resourceName, null, locale);
	}

	public URL getLocalResource(final String resourceName)
	{
		return getLocalResource(resourceName, null, null);
	}


	public InputStream getLocalResourceAsStream(final String resourceBaseName, final String[] suffixes, final Locale locale)
	{
		if(object != null)
		{
			return Resources.getLocalResourceAsStream(clazz, resourceBaseName, suffixes, resolveLocale(locale));
		}
		return Resources.getLocalResourceAsStream(clazz, resourceBaseName, suffixes, locale);
	}

	public InputStream getLocalResourceAsStream(final String resourceBaseName, final String[] suffixes)
	{
		return getLocalResourceAsStream(resourceBaseName, suffixes, null);
	}

	public InputStream getLocalResourceAsStream(final String resourceBaseName, final Locale locale)
	{
		return getLocalResourceAsStream(resourceBaseName, null, locale);
	}

	public InputStream getLocalResourceAsStream(final String resourceName)
	{
		return getLocalResourceAsStream(resourceName, null, null);
	}


	public URL[] getResources(final String resourceBaseName, final String[] suffixes, final Locale locale)
	{
		if(object != null)
		{
			return Resources.getResources(clazz, resourceBaseName, suffixes, resolveLocale(locale));
		}
		return Resources.getResources(clazz, resourceBaseName, suffixes, locale);
	}

	public URL[] getResources(final String resourceBaseName, final String[] suffixes)
	{
		return getResources(resourceBaseName, suffixes, null);
	}

	public URL[] getResources(final String resourceName, final Locale locale)
	{
		return getResources(resourceName, null, locale);
	}

	public URL[] getResources(final String resourceName)
	{
		return getResources(resourceName, null, null);
	}


	public URL getResource(final String resourceBaseName, final String[] suffixes, final Locale locale)
	{
		if(object != null)
		{
			return Resources.getResource(clazz, resourceBaseName, suffixes, resolveLocale(locale));
		}
		return Resources.getResource(clazz, resourceBaseName, suffixes, locale);
	}

	public URL getResource(final String resourceBaseName, final String[] suffixes)
	{
		return getResource(resourceBaseName, suffixes, null);
	}

	public URL getResource(final String resourceName, final Locale locale)
	{
		return getResource(resourceName, null, locale);
	}

	public URL getResource(final String resourceName)
	{
		return getResource(resourceName, null, null);
	}


	public InputStream getResourceAsStream(final String resourceBaseName, final String[] suffixes, final Locale locale)
	{
		if(object != null)
		{
			return Resources.getResourceAsStream(clazz, resourceBaseName, suffixes, resolveLocale(locale));
		}
		return Resources.getResourceAsStream(clazz, resourceBaseName, suffixes, locale);
	}

	public InputStream getResourceAsStream(final String resourceBaseName, final String[] suffixes)
	{
		return getResourceAsStream(resourceBaseName, suffixes, null);
	}

	public InputStream getResourceAsStream(final String resourceName, final Locale locale)
	{
		return getResourceAsStream(resourceName, null, locale);
	}

	public InputStream getResourceAsStream(final String resourceName)
	{
		return getResourceAsStream(resourceName, null, null);
	}


	public Map<String, Object> getLocalResourceMap(final String bundleBaseName, final Locale locale)
	{
		if(object != null)
		{
			return ResourceMaps.getLocalResourceMap(clazz, bundleBaseName, resolveLocale(locale));
		}
		return ResourceMaps.getLocalResourceMap(clazz, bundleBaseName, locale);
	}

	public Map<String, Object> getLocalResourceMap(final String bundleBaseName)
	{
		return getLocalResourceMap(bundleBaseName, null);
	}


	public Map<String, Object> getResourceMap(final String bundleBaseName, final Locale locale)
	{
		if(object != null)
		{
			return ResourceMaps.getResourceMap(clazz, bundleBaseName, resolveLocale(locale));
		}
		return ResourceMaps.getResourceMap(clazz, bundleBaseName, locale);
	}

	public Map<String, Object> getResourceMap(final String bundleBaseName)
	{
		return getResourceMap(bundleBaseName, null);
	}
}
