/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2009 Joern Huxhorn
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
package de.huxhorn.sulky.stax;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

// XXX Use Joda instead
public class DateTimeFormatter
{
	private static final String SIMPLE_DATE_FORMAT_DATETIME_PATTERN = "yyyy-MM-dd'T'HH:mm:ss.SSSZ";
	private static final String TIMEZONE_DATE_FORMAT_PATTERN = ".*([+-]\\d{2})(\\d{2})$";
	private static final int TIMEZONE_DATE_FORMAT_LENGTH = 5;
	private static final String TIMEZONE_XML_FORMAT_PATTERN = ".*([+-]\\d{2}):(\\d{2})$";
	private static final int TIMEZONE_XML_FORMAT_LENGTH = 6;

	private SimpleDateFormat dateFormat;
	private Pattern xmlTimezonePattern;
	private Pattern javaTimezonePattern;

	public DateTimeFormatter()
	{
		dateFormat = new SimpleDateFormat(SIMPLE_DATE_FORMAT_DATETIME_PATTERN);
		xmlTimezonePattern = Pattern.compile(TIMEZONE_XML_FORMAT_PATTERN);
		javaTimezonePattern = Pattern.compile(TIMEZONE_DATE_FORMAT_PATTERN);

	}

	public Date parse(String dateTime)
		throws ParseException
	{
		Matcher matcher = xmlTimezonePattern.matcher(dateTime);
		if(matcher.matches())
		{
			String hh = matcher.group(1);
			String mm = matcher.group(2);
			dateTime = dateTime.substring(0, dateTime.length() - TIMEZONE_XML_FORMAT_LENGTH) + hh + mm;
		}
		return dateFormat.parse(dateTime);
	}

	public String format(Date date)
	{
		String result = dateFormat.format(date);
		Matcher matcher = javaTimezonePattern.matcher(result);
		if(matcher.matches())
		{
			String hh = matcher.group(1);
			String mm = matcher.group(2);
			result = result.substring(0, result.length() - TIMEZONE_DATE_FORMAT_LENGTH) + hh + ":" + mm;
		}
		return result;
	}

}
