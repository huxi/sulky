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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Locale;

public class LocalizableFactoryTest
{
	private final Logger logger = LoggerFactory.getLogger(LocalizableFactoryTest.class);

	private Localizable internalGetLocalizable(Object originalObject, boolean proxy)
	{
		Localizable result = LocalizableFactory.getLocalizable(originalObject);
		if(logger.isDebugEnabled()) logger.debug("Localizable for original object {}: {}", originalObject, result);
		if(proxy)
		{
			Class c = result.getClass();
			String name = c.getName();
			assertTrue("Expected proxy but got " + name + " instead.", name.startsWith("$Proxy"));
		}
		else
		{
			assertEquals(originalObject, result);
		}
		return result;
	}

	@Test
	public void getLocalizable()
		throws Exception
	{
		Locale locale = new Locale("de_DE");
		Localizable loc;

		Locale l;
		Object o;
		// implements Localizable - no proxy
		o = new Foobar();
		loc = internalGetLocalizable(o, false);
		loc.setLocale(locale);
		l = loc.getLocale();
		assertEquals(locale, l);

		// getter only - proxy
		o = ((Foobar) o).new Internal();
		loc = internalGetLocalizable(o, true);
		loc.setLocale(null);
		l = loc.getLocale();
		assertEquals(locale, l);

		// getter and setter - proxy
		o = new Foobar.StaticInternal();
		loc = internalGetLocalizable(o, true);
		loc.setLocale(locale);
		l = loc.getLocale();
		assertEquals(locale, l);

		// neither getter nor setter - proxy
		o = new Foobar.StaticInternal.Foo();
		loc = internalGetLocalizable(o, true);
		loc.setLocale(locale);
		l = loc.getLocale();
		assertEquals(null, l);
	}

}
