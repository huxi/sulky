/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2015 Joern Huxhorn
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
 * Copyright 2007-2015 Joern Huxhorn
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

package de.huxhorn.sulky.formatting;

import java.time.DateTimeException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;

import java.time.temporal.TemporalAccessor;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.Map;

public final class SafeString
{
	public static final String  ERROR_PREFIX            = "[!!!";
	public static final String  ERROR_SEPARATOR         = "=>";
	public static final char    ERROR_MSG_SEPARATOR     = ':';
	public static final String  ERROR_SUFFIX            = "!!!]";

	public static final String  RECURSION_PREFIX        = "[...";
	public static final String  RECURSION_SUFFIX        = "...]";

	private static final char   CONTAINER_PREFIX        = '[';
	private static final String CONTAINER_SEPARATOR     = ", ";
	private static final char   CONTAINER_SUFFIX        = ']';

	private static final char   MAP_PREFIX              = '{';
	private static final char   MAP_KEY_VALUE_SEPARATOR = '=';
	private static final char   MAP_SUFFIX              = '}';

	private static final char   IDENTITY_SEPARATOR      = '@';

	private static final String NULL_VALUE              = "null";

	private static final DateTimeFormatter ISO_DATE_TIME_FORMATTER =
			new DateTimeFormatterBuilder()
					.parseCaseInsensitive()
					.append(DateTimeFormatter.ISO_LOCAL_DATE)
					.appendLiteral('T')
					.appendValue(ChronoField.HOUR_OF_DAY, 2)
					.appendLiteral(':')
					.appendValue(ChronoField.MINUTE_OF_HOUR, 2)
					.optionalStart()
					.appendLiteral(':')
					.appendValue(ChronoField.SECOND_OF_MINUTE, 2)
					.optionalStart()
					.appendFraction(ChronoField.MILLI_OF_SECOND, 3, 3, true)
					.appendZoneId()
					.toFormatter()
					.withZone(ZoneOffset.UTC);

	static
	{
		// for the sake of coverage
		new SafeString();
	}

	private SafeString()
	{}

	public static String toString(Object o)
	{
		if(o == null)
		{
			return NULL_VALUE;
		}
		StringBuilder builder = new StringBuilder();
		append(o, builder);
		return builder.toString();
	}

	public static void append(Object obj, StringBuilder into)
	{
		IdentityHashMap<Object, Object> dejaVu = new IdentityHashMap<>(); // that's actually a neat name ;)
		recursiveAppend(obj, into, dejaVu);
	}

	/**
	 * This method performs a deep toString of the given Object.
	 * Primitive arrays are converted using their respective Arrays.toString methods while
	 * special handling is implemented for "container types", i.e. Object[], Map and Collection because those could
	 * contain themselves.
	 * <p/>
	 * dejaVu is used in case of those container types to prevent an endless recursion.
	 * <p/>
	 * It should be noted that neither AbstractMap.toString() nor AbstractCollection.toString() implement such a behavior.
	 * They only check if the container is directly contained in itself, but not if a contained container contains the
	 * original one. Because of that, Arrays.toString(Object[]) isn't safe either.
	 * Confusing? Just read the last paragraph again and check the respective toString() implementation.
	 * <p/>
	 * This means, in effect, that logging would produce a usable output even if an ordinary System.out.println(o)
	 * would produce a relatively hard-to-debug StackOverflowError.
	 *
	 * @param o      the Object to convert into a String
	 * @param str    the StringBuilder that o will be appended to
	 * @param dejaVu used to detect recursions.
	 */
	private static void recursiveAppend(Object o, StringBuilder str, IdentityHashMap<Object, Object> dejaVu)
	{
		if(o == null)
		{
			str.append(NULL_VALUE);
			return;
		}
		if(o instanceof String)
		{
			str.append(o);
			return;
		}

		Class oClass = o.getClass();
		if(oClass.isArray())
		{
			if(oClass == byte[].class)
			{
				str.append(Arrays.toString((byte[]) o));
				return;
			}
			if(oClass == short[].class)
			{
				str.append(Arrays.toString((short[]) o));
				return;
			}
			if(oClass == int[].class)
			{
				str.append(Arrays.toString((int[]) o));
				return;
			}
			if(oClass == long[].class)
			{
				str.append(Arrays.toString((long[]) o));
				return;
			}
			if(oClass == float[].class)
			{
				str.append(Arrays.toString((float[]) o));
				return;
			}
			if(oClass == double[].class)
			{
				str.append(Arrays.toString((double[]) o));
				return;
			}
			if(oClass == boolean[].class)
			{
				str.append(Arrays.toString((boolean[]) o));
				return;
			}
			if(oClass == char[].class)
			{
				str.append(Arrays.toString((char[]) o));
				return;
			}

			// special handling of container Object[]
			if(dejaVu.containsKey(o))
			{
				str.append(RECURSION_PREFIX).append(identityToString(o)).append(RECURSION_SUFFIX);
				return;
			}
			dejaVu.put(o, null);

			Object[] oArray = (Object[]) o;
			str.append(CONTAINER_PREFIX);
			boolean first = true;
			for(Object current : oArray)
			{
				if(first)
				{
					first = false;
				}
				else
				{
					str.append(CONTAINER_SEPARATOR);
				}
				recursiveAppend(current, str, new IdentityHashMap<>(dejaVu));
			}
			str.append(CONTAINER_SUFFIX);

			return;
		}

		if(o instanceof Map)
		{
			// special handling of container Map
			if(dejaVu.containsKey(o))
			{
				str.append(RECURSION_PREFIX).append(identityToString(o)).append(RECURSION_SUFFIX);
				return;
			}
			dejaVu.put(o, null);

			Map<?, ?> oMap = (Map<?, ?>) o;
			str.append(MAP_PREFIX);
			boolean first = true;
			for(Map.Entry<?, ?> current : oMap.entrySet())
			{
				if(first)
				{
					first = false;
				}
				else
				{
					str.append(CONTAINER_SEPARATOR);
				}
				Object key = current.getKey();
				Object value = current.getValue();
				recursiveAppend(key, str, new IdentityHashMap<>(dejaVu));
				str.append(MAP_KEY_VALUE_SEPARATOR);
				recursiveAppend(value, str, new IdentityHashMap<>(dejaVu));
			}
			str.append(MAP_SUFFIX);
			return;
		}

		if(o instanceof Collection)
		{
			// special handling of container Collection
			if(dejaVu.containsKey(o))
			{
				str.append(RECURSION_PREFIX).append(identityToString(o)).append(RECURSION_SUFFIX);
				return;
			}
			dejaVu.put(o, null);

			Collection<?> oCol = (Collection<?>) o;
			str.append(CONTAINER_PREFIX);
			boolean first = true;
			for(Object current : oCol)
			{
				if(first)
				{
					first = false;
				}
				else
				{
					str.append(CONTAINER_SEPARATOR);
				}
				recursiveAppend(current, str, new IdentityHashMap<>(dejaVu));
			}
			str.append(CONTAINER_SUFFIX);
			return;
		}

		if(o instanceof Date)
		{
			ISO_DATE_TIME_FORMATTER.formatTo(Instant.ofEpochMilli(((Date)o).getTime()), str);
			return;
		}

		if(o instanceof TemporalAccessor)
		{
			try
			{
				ISO_DATE_TIME_FORMATTER.formatTo((TemporalAccessor) o, str);
				return;
			}
			catch(DateTimeException ignore)
			{
				// this is not a bug. fall through to simple Object handling.
			}
		}

		// it's just some other Object, we can only use toString().
		try
		{
			str.append(o.toString());
		}
		catch(Throwable t)
		{
			str.append(ERROR_PREFIX);
			str.append(identityToString(o));
			str.append(ERROR_SEPARATOR);
			String msg = t.getMessage();
			String className = t.getClass().getName();
			str.append(className);
			if(msg != null && !className.equals(msg))
			{
				str.append(ERROR_MSG_SEPARATOR);
				str.append(msg);
			}
			str.append(ERROR_SUFFIX);
		}
	}

	/**
	 * This method returns the same as if Object.toString() would not have been
	 * overridden in obj.
	 *
	 * Note that this isn't 100% secure as collisions can always happen with hash codes.
	 *
	 * Copied from Object.hashCode():
	 * As much as is reasonably practical, the hashCode method defined by
	 * class <tt>Object</tt> does return distinct integers for distinct
	 * objects. (This is typically implemented by converting the internal
	 * address of the object into an integer, but this implementation
	 * technique is not required by the
	 * Java&#x2122; programming language.)
	 *
	 * @param obj the Object that is to be converted into an identity string.
	 * @return the identity string as also defined in Object.toString()
	 */
	public static String identityToString(Object obj)
	{
		if(obj == null)
		{
			return null;
		}
		return obj.getClass().getName() + IDENTITY_SEPARATOR + Integer.toHexString(System.identityHashCode(obj));
	}

}
