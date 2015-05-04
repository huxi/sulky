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

package de.huxhorn.sulky.stax;

import java.text.ParseException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DateTimeFormatter
{
	private static final String TIMEZONE_DATE_FORMAT_PATTERN = ".*([+-]\\d{2})(\\d{2})$";
	private static final int TIMEZONE_DATE_FORMAT_LENGTH = 5;

	private static final java.time.format.DateTimeFormatter ISO_DATE_TIME_PARSER =
			new DateTimeFormatterBuilder()
					.parseCaseInsensitive()
					.append(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
					.appendLiteral('T')
					.appendValue(ChronoField.HOUR_OF_DAY, 2)
					.appendLiteral(':')
					.appendValue(ChronoField.MINUTE_OF_HOUR, 2)
					.appendLiteral(':')
					.appendValue(ChronoField.SECOND_OF_MINUTE, 2)
					.optionalStart()
					.appendFraction(ChronoField.MILLI_OF_SECOND, 3, 3, true)
					.optionalEnd()
					.appendOffset("+HH:MM", "Z")
					.toFormatter()
					.withZone(ZoneOffset.UTC);

	private static final java.time.format.DateTimeFormatter ISO_DATE_TIME_FORMATTER_WITH_MILLIS =
			new DateTimeFormatterBuilder()
					.parseCaseInsensitive()
					.append(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
					.appendLiteral('T')
					.appendValue(ChronoField.HOUR_OF_DAY, 2)
					.appendLiteral(':')
					.appendValue(ChronoField.MINUTE_OF_HOUR, 2)
					.appendLiteral(':')
					.appendValue(ChronoField.SECOND_OF_MINUTE, 2)
					.appendFraction(ChronoField.MILLI_OF_SECOND, 3, 3, true)
					.appendOffset("+HH:MM", "+00:00")
					.toFormatter()
					.withZone(ZoneOffset.UTC);

	private static final java.time.format.DateTimeFormatter ISO_DATE_TIME_FORMATTER_WITHOUT_MILLIS =
			new DateTimeFormatterBuilder()
					.parseCaseInsensitive()
					.append(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE)
					.appendLiteral('T')
					.appendValue(ChronoField.HOUR_OF_DAY, 2)
					.appendLiteral(':')
					.appendValue(ChronoField.MINUTE_OF_HOUR, 2)
					.appendLiteral(':')
					.appendValue(ChronoField.SECOND_OF_MINUTE, 2)
					.appendOffset("+HH:MM", "+00:00")
					.toFormatter()
					.withZone(ZoneOffset.UTC);

	private Pattern javaTimezonePattern;

	public DateTimeFormatter()
	{
		javaTimezonePattern = Pattern.compile(TIMEZONE_DATE_FORMAT_PATTERN);

	}

	/**
	 * This method parses a given string containing a dateTime in ISO8601 notation into a date.
	 *
	 * It can handle an optional millisecond fraction as well as timezone with either explicit '+/-HH:MM' or 'Z' UTC designator.
	 *
	 * @param dateTime a string containing a dateTime in ISO8601 notation.
	 * @return the parsed date
	 * @throws ParseException If the dateTime string is invalid.
	 */
	public Date parse(String dateTime)
		throws ParseException
	{
		Matcher matcher = javaTimezonePattern.matcher(dateTime);
		if(matcher.matches())
		{
			String hh = matcher.group(1);
			String mm = matcher.group(2);
			dateTime = dateTime.substring(0, dateTime.length() - TIMEZONE_DATE_FORMAT_LENGTH) + hh + ":" + mm;
		}
		TemporalAccessor temporal = ISO_DATE_TIME_PARSER.parse(dateTime);
		long seconds = temporal.getLong(ChronoField.INSTANT_SECONDS) + temporal.getLong(ChronoField.OFFSET_SECONDS);
		long millis = seconds * 1000 + temporal.getLong(ChronoField.MILLI_OF_SECOND);

		return new Date(millis);
	}

	/**
	 * Returns a simplified ISO8601 datetime string in UTC.
	 *
	 * It will always contain a three-number millisecond field regardless if it is "needed"
	 * (i.e. MILLI_OF_SECOND != 0) or not. The timezone of the date is always UTC but isn't using
	 * the UTC designator 'Z'. Instead, it's using an explicit '+00:00'.
	 * That way a date formatted by this method will always have the same number of characters while creating output
	 * that less intelligent date-parsing frameworks (incapable of the 'Z' notation) are still able to process.
	 *
	 * @param date the date to be formatted.
	 * @return a simplified ISO8601 datetime string in UTC.
	 */
	public String format(Date date)
	{
		return this.format(date, true);
	}

	/**
	 * Returns a simplified ISO8601 datetime string in UTC.
	 *
	 * It will always contain a three-number millisecond field regardless if it is "needed"
	 * (i.e. MILLI_OF_SECOND != 0) or not if withMillis is true. The timezone of the date is always UTC but isn't using
	 * the UTC designator 'Z'. Instead, it's using an explicit '+00:00'.
	 * That way a date formatted by this method will always have the same number of characters while creating output
	 * that less intelligent date-parsing frameworks (incapable of the 'Z' notation) are still able to process.
	 *
	 * @param date the date to be formatted.
	 * @param withMillis whether or not milliseconds should be printed.
	 * @return a simplified ISO8601 datetime string in UTC.
	 */
	public String format(Date date, boolean withMillis)
	{
		Instant instant = Instant.ofEpochMilli(date.getTime());
		ZonedDateTime zoned = ZonedDateTime.ofInstant(instant, ZoneOffset.UTC);
		if(withMillis)
		{
			return ISO_DATE_TIME_FORMATTER_WITH_MILLIS.format(zoned);
		}
		return ISO_DATE_TIME_FORMATTER_WITHOUT_MILLIS.format(zoned);
	}

}
