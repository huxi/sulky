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
import java.util.Objects;


public final class SafeString
{
	public enum StringWrapping
	{
		/**
		 * Strings are not wrapped at all.
		 */
		NONE,

		/**
		 * Only Strings contained in Collection, Map or array are wrapped.
		 */
		CONTAINED,

		/**
		 * All Strings are wrapped.
		 */
		ALL
	}

	public enum StringStyle
	{
		/**
		 * String is rendered as "String".
		 */
		JAVA('"'),

		/**
		 * String is rendered as 'String'.
		 */
		GROOVY('\'');

		private char quoteChar;

		StringStyle(char quoteChar)
		{
			this.quoteChar=quoteChar;
		}

		public char getQuoteChar()
		{
			return quoteChar;
		}
	}

	public enum MapStyle
	{
		/**
		 * Map is rendered as {key=value, key2=value2}.
		 */
		JAVA('{', '}', '='),

		/**
		 * Map is rendered as [key:value, key2:value2].
		 */
		GROOVY('[', ']', ':');

		private char prefix;
		private char suffix;
		private char keyValueSeparator;

		MapStyle(char prefix, char suffix, char keyValueSeparator)
		{
			this.prefix = prefix;
			this.suffix = suffix;
			this.keyValueSeparator = keyValueSeparator;
		}

		public char getPrefix()
		{
			return prefix;
		}

		public char getSuffix()
		{
			return suffix;
		}

		public char getKeyValueSeparator()
		{
			return keyValueSeparator;
		}
	}

	public static final String  ERROR_PREFIX            = "[!!!";
	public static final String  ERROR_SEPARATOR         = "=>";
	public static final char    ERROR_MSG_SEPARATOR     = ':';
	public static final String  ERROR_SUFFIX            = "!!!]";

	public static final String  RECURSION_PREFIX        = "[...";
	public static final String  RECURSION_SUFFIX        = "...]";

	private static final char   CONTAINER_PREFIX        = '[';
	private static final String CONTAINER_SEPARATOR     = ", ";
	private static final char   CONTAINER_SUFFIX        = ']';

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

	private static final String BYTE_PREFIX = "0x";
	private static final String[] BYTE_STRINGS;

	static
	{
		// for the sake of coverage
		new SafeString();

		final char[] HEX_CHARS = new char[] {
				'0', '1', '2', '3', '4', '5', '6', '7',
				'8', '9', 'A', 'B', 'C', 'D', 'E', 'F'
		};

		BYTE_STRINGS = new String[256];
		for(int i=0;i<256;i++)
		{
			@SuppressWarnings("StringBufferReplaceableByString")
			StringBuilder sb=new StringBuilder(2);

			sb.append(HEX_CHARS[i >>> 4]);
			sb.append(HEX_CHARS[i & 0xf]);

			BYTE_STRINGS[i]=sb.toString();
		}
	}

	private SafeString()
	{}

	public static String toString(Object o)
	{
		return toString(o, StringWrapping.NONE, StringStyle.JAVA, MapStyle.JAVA);
	}

	public static String toString(Object o, StringWrapping stringWrapping, StringStyle stringStyle, MapStyle mapStyle)
	{
		Objects.requireNonNull(stringWrapping, "stringWrapping must not be null!");
		Objects.requireNonNull(stringStyle, "stringStyle must not be null!");
		Objects.requireNonNull(mapStyle, "mapStyle must not be null!");

		if(o == null)
		{
			return NULL_VALUE;
		}

		if(o instanceof String)
		{
			String string = (String) o;
			if(stringWrapping != StringWrapping.ALL)
			{
				return string;
			}
			char quoteChar=stringStyle.getQuoteChar();
			return ""+quoteChar+string+quoteChar;
		}

		StringBuilder stringBuilder = new StringBuilder();
		append(o, stringBuilder, stringWrapping, stringStyle, mapStyle);
		return stringBuilder.toString();
	}

	public static void append(Object o, StringBuilder stringBuilder)
	{
		append(o, stringBuilder, StringWrapping.NONE, StringStyle.JAVA, MapStyle.JAVA);
	}

	public static void append(Object o, StringBuilder stringBuilder, StringWrapping stringWrapping, StringStyle stringStyle, MapStyle mapStyle)
	{
		Objects.requireNonNull(stringBuilder, "stringBuilder must not be null!");
		Objects.requireNonNull(stringWrapping, "stringWrapping must not be null!");
		Objects.requireNonNull(stringStyle, "stringStyle must not be null!");
		Objects.requireNonNull(mapStyle, "mapStyle must not be null!");

		if(o == null)
		{
			stringBuilder.append(NULL_VALUE);
			return;
		}

		if(o instanceof String)
		{
			String string = (String) o;
			if(stringWrapping != StringWrapping.ALL)
			{
				stringBuilder.append(string);
				return;
			}
			char quoteChar=stringStyle.getQuoteChar();
			stringBuilder.append(quoteChar).append(string).append(quoteChar);
			return;
		}

		IdentityHashMap<Object, Object> dejaVu = new IdentityHashMap<>(); // that's actually a neat name ;)
		if(stringWrapping == StringWrapping.NONE)
		{
			stringStyle = null;
		}
		recursiveAppend(o, stringBuilder, stringStyle, mapStyle, dejaVu);
	}

	/**
	 * This method returns the same as if {@code Object.toString()} would not have been
	 * overridden in obj.
	 *
	 * <p>Note that this isn't 100% secure as collisions can always happen with hash codes.
	 *
	 * <p>Copied from {@code Object.hashCode()}:
	 *
	 * <blockquote>
	 * As much as is reasonably practical, the hashCode method defined by
	 * class {@code Object} does return distinct integers for distinct
	 * objects. (This is typically implemented by converting the internal
	 * address of the object into an integer, but this implementation
	 * technique is not required by the
	 * Java&trade; programming language.)
	 * </blockquote>
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

	/**
	 * This method performs a deep toString of the given Object.
	 *
	 * <p>Primitive arrays are converted using their respective Arrays.toString methods while
	 * special handling is implemented for "container types", i.e. Object[], Map and Collection because those could
	 * contain themselves.
	 *
	 * <p>dejaVu is used in case of those container types to prevent an endless recursion.
	 *
	 * <p>It should be noted that neither AbstractMap.toString() nor AbstractCollection.toString() implement such a behavior.
	 * They only check if the container is directly contained in itself, but not if a contained container contains the
	 * original one. Because of that, Arrays.toString(Object[]) isn't safe either.
	 *
	 * <p>Confusing? Just read the last paragraph again and check the respective toString() implementation.
	 *
	 * <p>This means, in effect, that logging would produce a usable output even if an ordinary System.out.println(o)
	 * would produce a relatively hard-to-debug StackOverflowError.
	 *
	 * @param o              the Object to convert into a String
	 * @param stringBuilder  the StringBuilder that o will be appended to
	 * @param stringStyle    the String quoting style.
	 * @param mapStyle       the Map printing style.
	 * @param dejaVu         used to detect recursions.
	 */
	private static void recursiveAppend(Object o, StringBuilder stringBuilder, StringStyle stringStyle, MapStyle mapStyle, IdentityHashMap<Object, Object> dejaVu)
	{
		// o will never be null or String at this point since those cases are already handled by shortcuts.
		Class oClass = o.getClass();
		if(oClass.isArray())
		{
			if(oClass == byte[].class)
			{
				stringBuilder.append(CONTAINER_PREFIX);

				byte[] array = (byte[]) o;
				boolean first = true;
				for(byte current : array)
				{
					if (first)
					{
						first = false;
					}
					else
					{
						stringBuilder.append(CONTAINER_SEPARATOR);
					}
					appendByte(current, stringBuilder);
				}

				stringBuilder.append(CONTAINER_SUFFIX);
				return;
			}
			if(oClass == Byte[].class)
			{
				//stringBuilder.append(Arrays.toString((byte[]) o));
				stringBuilder.append(CONTAINER_PREFIX);

				Byte[] array = (Byte[]) o;
				boolean first = true;
				for(Byte current : array)
				{
					if (first)
					{
						first = false;
					}
					else
					{
						stringBuilder.append(CONTAINER_SEPARATOR);
					}
					appendByte(current, stringBuilder);
				}

				stringBuilder.append(CONTAINER_SUFFIX);
				return;
			}
			if(oClass == short[].class)
			{
				stringBuilder.append(Arrays.toString((short[]) o));
				return;
			}
			if(oClass == int[].class)
			{
				stringBuilder.append(Arrays.toString((int[]) o));
				return;
			}
			if(oClass == long[].class)
			{
				stringBuilder.append(Arrays.toString((long[]) o));
				return;
			}
			if(oClass == float[].class)
			{
				stringBuilder.append(Arrays.toString((float[]) o));
				return;
			}
			if(oClass == double[].class)
			{
				stringBuilder.append(Arrays.toString((double[]) o));
				return;
			}
			if(oClass == boolean[].class)
			{
				stringBuilder.append(Arrays.toString((boolean[]) o));
				return;
			}
			if(oClass == char[].class)
			{
				stringBuilder.append(Arrays.toString((char[]) o));
				return;
			}

			// special handling of container Object[]
			if(dejaVu.containsKey(o))
			{
				stringBuilder.append(RECURSION_PREFIX).append(identityToString(o)).append(RECURSION_SUFFIX);
				return;
			}
			dejaVu.put(o, null);

			Object[] oArray = (Object[]) o;
			stringBuilder.append(CONTAINER_PREFIX);
			boolean first = true;
			for(Object current : oArray)
			{
				if(first)
				{
					first = false;
				}
				else
				{
					stringBuilder.append(CONTAINER_SEPARATOR);
				}

				if(!handleSimpleContainerValue(current, stringBuilder, stringStyle))
				{
					recursiveAppend(current, stringBuilder, stringStyle, mapStyle, new IdentityHashMap<>(dejaVu));
				}
			}
			stringBuilder.append(CONTAINER_SUFFIX);

			return;
		}

		if(o instanceof Byte)
		{
			appendByte((Byte)o, stringBuilder);
			return;
		}
		if(o instanceof Map)
		{
			// special handling of container Map
			if(dejaVu.containsKey(o))
			{
				stringBuilder.append(RECURSION_PREFIX).append(identityToString(o)).append(RECURSION_SUFFIX);
				return;
			}
			dejaVu.put(o, null);

			Map<?, ?> oMap = (Map<?, ?>) o;
			stringBuilder.append(mapStyle.getPrefix());
			boolean first = true;
			for(Map.Entry<?, ?> current : oMap.entrySet())
			{
				if(first)
				{
					first = false;
				}
				else
				{
					stringBuilder.append(CONTAINER_SEPARATOR);
				}

				Object key = current.getKey();
				if(!handleSimpleContainerValue(key, stringBuilder, stringStyle))
				{
					recursiveAppend(key, stringBuilder, stringStyle, mapStyle, new IdentityHashMap<>(dejaVu));
				}

				stringBuilder.append(mapStyle.getKeyValueSeparator());

				Object value = current.getValue();
				if(!handleSimpleContainerValue(value, stringBuilder, stringStyle))
				{
					recursiveAppend(value, stringBuilder, stringStyle, mapStyle, new IdentityHashMap<>(dejaVu));
				}
			}
			stringBuilder.append(mapStyle.getSuffix());
			return;
		}

		if(o instanceof Collection)
		{
			// special handling of container Collection
			if(dejaVu.containsKey(o))
			{
				stringBuilder.append(RECURSION_PREFIX).append(identityToString(o)).append(RECURSION_SUFFIX);
				return;
			}
			dejaVu.put(o, null);

			Collection<?> oCol = (Collection<?>) o;
			stringBuilder.append(CONTAINER_PREFIX);
			boolean first = true;
			for(Object current : oCol)
			{
				if(first)
				{
					first = false;
				}
				else
				{
					stringBuilder.append(CONTAINER_SEPARATOR);
				}

				if(!handleSimpleContainerValue(current, stringBuilder, stringStyle))
				{
					recursiveAppend(current, stringBuilder, stringStyle, mapStyle, new IdentityHashMap<>(dejaVu));
				}
			}
			stringBuilder.append(CONTAINER_SUFFIX);
			return;
		}

		if(o instanceof Date)
		{
			ISO_DATE_TIME_FORMATTER.formatTo(Instant.ofEpochMilli(((Date)o).getTime()), stringBuilder);
			return;
		}

		if(o instanceof TemporalAccessor)
		{
			try
			{
				ISO_DATE_TIME_FORMATTER.formatTo((TemporalAccessor) o, stringBuilder);
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
			stringBuilder.append(o.toString());
		}
		catch(Throwable t)
		{
			stringBuilder.append(ERROR_PREFIX);
			stringBuilder.append(identityToString(o));
			stringBuilder.append(ERROR_SEPARATOR);
			String msg = t.getMessage();
			String className = t.getClass().getName();
			stringBuilder.append(className);
			if(msg != null && !className.equals(msg))
			{
				stringBuilder.append(ERROR_MSG_SEPARATOR);
				stringBuilder.append(msg);
			}
			stringBuilder.append(ERROR_SUFFIX);
		}
	}

	private static boolean handleSimpleContainerValue(Object current, StringBuilder str, StringStyle stringStyle)
	{
		if(current == null)
		{
			str.append(NULL_VALUE);
			return true;
		}
		if(current instanceof String)
		{
			String string = (String) current;
			if(stringStyle == null)
			{
				str.append(string);
			}
			else
			{
				char quoteChar=stringStyle.getQuoteChar();
				str.append(quoteChar).append(string).append(quoteChar);
			}
			return true;
		}

		// not calling recursiveAppend here to preserve stack space.
		return false;
	}

	private static void appendByte(int byteIndex, StringBuilder stringBuilder)
	{
		stringBuilder.append(BYTE_PREFIX);
		stringBuilder.append(BYTE_STRINGS[0x000000FF & byteIndex]);
	}
}
