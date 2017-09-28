/*
 * sulky-resources - inheritance-safe class resources.
 * Copyright (C) 2002-2017 Joern Huxhorn
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
 * Copyright 2002-2017 Joern Huxhorn
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

import java.net.URL;
import org.slf4j.Logger;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

class ResourceTestHelper
{
	static void logResult(Logger logger, String methodCall, Object result)
	{
		if(logger.isInfoEnabled()) logger.info("Result returned by method call {}: {}", methodCall, result);
	}

	static void logResults(Logger logger, String methodCall, URL[] result)
	{
		if (logger.isInfoEnabled())
		{
			StringBuilder results = new StringBuilder();
			for (int i = 0; i < result.length; i++)
			{
				results.append('#').append(i).append(": ").append(result[i]).append('\n');
			}
			logger.info("Results returned by method call {}:\n{}", methodCall, results);
		}
	}

	static void appendSuffixes(StringBuilder builder, String[] suffixes)
	{
		if (suffixes != null)
		{
			builder.append('[');
			for(int i = 0; i < suffixes.length; i++)
			{
				if(i != 0)
				{
					builder.append(", ");
				}
				builder.append('"').append(suffixes[i]).append('"');
			}
			builder.append(']');
		}
		else
		{
			builder.append("null");
		}
	}

	static void assertResultEndsWith(String methodCall, URL result, String resultEndsWith)
	{
		if(result == null)
		{
			if(resultEndsWith != null)
			{
				fail(methodCall + " - result was null but should end in \"" + resultEndsWith + "\"!");
			}
		}
		else
		{
			String cur = result.toString();
			String msg = methodCall + " - resultUrl:\"" + cur + "\" does not end with \"" + resultEndsWith + "\"!";
			assertTrue(msg, cur.endsWith(resultEndsWith));
		}
	}
}
