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

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Locale;

/**
 * DOCUMENT: <code>LocalizableFactory</code>
 */
public final class LocalizableFactory
{
	/**
	 * No instances...
	 */
	private LocalizableFactory()
	{
	}

	/**
	 * Returns a Localizable for the given object no matter
	 * what <code>object</code> really is.
	 * If <code>object</code> implements the <code>Localizable</code>
	 * interface it's simply casted and returned. Otherwise a proxy is
	 * created. The proxy calls the objects <code>getLocale</code> and
	 * <code>setLocale</code> methods if they are available. Otherwise
	 * <code>setLocale</code> does nothing and <code>getLocale</code>
	 * always returns <code>null</code>.
	 *
	 * @param object the <code>object</code> that will be either casted or wrapped.
	 * @return either the original <code>object</code> or a proxy wrapping the given <code>object</code>.
	 * @throws NullPointerException if <code>object</code> is <code>null</code>.
	 */
	public static Localizable getLocalizable(Object object)
	{
		final Logger logger = LoggerFactory.getLogger(MapLoader.class);

		Class clazz = object.getClass();
		Localizable loc;
		if(object instanceof Localizable)
		{
			loc = (Localizable) object;
			if(logger.isDebugEnabled()) logger.debug("Casting to Localizable.");
		}
		else
		{
			// Important! Use the objects classloader so proxy's may be unloaded as well...
			loc = (Localizable)
				Proxy.newProxyInstance(clazz.getClassLoader(),
					new Class[]{Localizable.class}, new LocalizableInvocationHandler(object));
			if(logger.isDebugEnabled()) logger.debug("Created Localizable proxy.");
		}
		return loc;
	}

	private static class LocalizableInvocationHandler
		implements InvocationHandler
	{
		private final Logger logger = LoggerFactory.getLogger(LocalizableInvocationHandler.class);

		public static final String GETTER_NAME = "getLocale";
		public static final String SETTER_NAME = "setLocale";

		private final Object object;
		private final Method getter;
		private final Method setter;

		public LocalizableInvocationHandler(Object obj)
		{
			this.object = obj;
			Class clazz = obj.getClass();

			Method m = null;

			try
			{
				m = clazz.getMethod(GETTER_NAME);
			}
			catch(NoSuchMethodException ex)
			{
				if(logger.isInfoEnabled()) logger.info("getLocale-method not found...");
			}
			this.getter = m;

			m = null;
			try
			{
				m = clazz.getMethod(SETTER_NAME, new Class[]{Locale.class});
			}
			catch(NoSuchMethodException ex)
			{
				if(logger.isInfoEnabled()) logger.info("setLocale-method not found...");
			}
			this.setter = m;
		}

		public Object invoke(Object proxy, Method method, Object[] args)
			// see java.lang.reflect.InvocationHandler, yeah, right :p - Illegal Throws : Throwing 'Throwable' is not allowed.
			throws Throwable
		{
			try
			{
				if(logger.isDebugEnabled())
				{
					logger.debug("Calling method '" + method + "' on instance of class '" + object.getClass()
						.getName() + "' with proxy '" + proxy.getClass() + "'.");
				}
				if(getter != null && GETTER_NAME.equals(method.getName()))
				{
					Object result = getter.invoke(object, args);
					if(logger.isDebugEnabled()) logger.debug("Call-Result: " + result);
					return result;
				}
				else if(setter != null && SETTER_NAME.equals(method.getName()))
				{
					return setter.invoke(object, args);
				}
			}
			catch(Throwable t)
			{
				if(logger.isWarnEnabled()) logger.warn("Throwable while invoking proxy-method!", t);
				throw t;
			}
			return null;
		}
	}
}
