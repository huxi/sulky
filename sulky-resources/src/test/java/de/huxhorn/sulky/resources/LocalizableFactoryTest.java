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
import java.util.Locale;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

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
			assertTrue("Expected proxy but got " + name + " instead.", name.contains("$Proxy"));
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
		Locale locale = Locale.of("de_DE");
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
