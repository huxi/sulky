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

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;


public class PathToolsTest
{
	private final Logger logger = LoggerFactory.getLogger(PathToolsTest.class);

	private void internalResolvePath(String basePath, String path, String expectedResult)
	{
		String result = PathTools.resolvePath(basePath, path);
		String methodCall = "resolvePath(\"" + basePath + "\", \"" + path + "\")";
		assertEquals(methodCall, expectedResult, result);
	}

	private void internalEvaluatePath(String path, String expectedResult)
	{
		String result = PathTools.evaluatePath(path);
		assertEquals("evaluatePath(\"" + path + "\")", expectedResult, result);
	}

	private void internalGetAbsolutePath(String basePath, String path, String expectedResult)
	{
		String result = PathTools.getAbsolutePath(basePath, path);
		assertEquals("getAbsolutePath(\"" + basePath + "\", \"" + path + "\")", expectedResult, result);
	}

	private void internalGetParentPath(String path, String expectedResult)
	{
		String result = PathTools.getParentPath(path);
		assertEquals("getParentPath(\"" + path + "\")", expectedResult, result);
	}

	private void internalGetCompatiblePath(String path, String expectedResult)
	{
		String result = PathTools.getCompatiblePath(path);
		assertEquals("getCompatiblePath(\"" + path + "\")", expectedResult, result);
	}

	private void internalGetPathStack(String path, boolean reduce, String expectedPathElements[])
	{
		List<String> stack = PathTools.getPathStack(path, reduce);
		String methodCall = "getPathStack(\"" + path + "\", " + reduce + ");";
		assertEquals(methodCall + " - Number of Stack-Elements", expectedPathElements.length, stack.size());
		for(int i = 0; i < expectedPathElements.length; i++)
		{
			assertEquals(methodCall + " - Mismatch at Index #" + i, expectedPathElements[i], stack.get(i));
		}
	}

	private void internalGetPathStackString(String path, boolean reduce, String expectedPathString)
	{
		String pathString = PathTools.getPathStackString(PathTools.getPathStack(path, reduce));
		String methodCall = "getPathStackString(getPathStack(\"" + path + "\", " + reduce + "));";
		assertEquals(methodCall + " - ", expectedPathString, pathString);
	}

	@Test
	public void resolvePath()
		throws Exception
	{
		internalResolvePath("", "", "");
		internalResolvePath("", "foo", "foo");
		internalResolvePath("foo", "", "foo");
		internalResolvePath("foo", "/", "/");
		internalResolvePath("foo", "bar", "foo/bar");
		internalResolvePath("/foo", "bar", "/foo/bar");
		internalResolvePath(".../foo", "../bar", ".../foo/../bar");

		// Examples...
		internalResolvePath("foo", "/bar", "/bar");
		internalResolvePath("/foo", "bar", "/foo/bar");
		internalResolvePath("/foo", "../bar", "/foo/../bar");
	}

	@Test
	public void evaluatePath()
		throws Exception
	{
		internalEvaluatePath("", "");
		internalEvaluatePath("/", "/");
		internalEvaluatePath("/foo/bar", "/foo/bar");
		internalEvaluatePath("foo/bar", "foo/bar");
		internalEvaluatePath("/foobar/../foo/bar", "/foo/bar");
		internalEvaluatePath("/foo/bar/../foobar/..", "/foo");
		internalEvaluatePath("/foo/bar/...../foobar", ".../foobar");

		// Examples
		internalEvaluatePath("/foo/bar/../foobar", "/foo/foobar");
		internalEvaluatePath("/foo/bar/..../foobar", "../foobar");
	}

	@Test
	public void getAbsolutePath()
		throws Exception
	{
		internalGetAbsolutePath("", "", null);
		internalGetAbsolutePath("/", "", "/");
		internalGetAbsolutePath("/", "foo/bar", "/foo/bar");
		internalGetAbsolutePath("/foobar", "foo/bar", "/foobar/foo/bar");
		internalGetAbsolutePath("/foobar/", "foo/bar", "/foobar/foo/bar");
		internalGetAbsolutePath("/foobar", "/foo/bar", "/foo/bar");
		internalGetAbsolutePath("/foobar", "../foo/bar", "/foo/bar");

		// Examples
		internalGetAbsolutePath("/foo/bar", "../foobar", "/foo/foobar");
		internalGetAbsolutePath("/foo/bar", "..../foobar", null);
		internalGetAbsolutePath("bar", "foobar", null);
	}

	@Test
	public void getParentPath()
		throws Exception
	{
		internalGetParentPath("", "..");
		internalGetParentPath(".", "..");
		internalGetParentPath("/", "..");
		internalGetParentPath("/foo/bar", "/foo");
		internalGetParentPath("foo/bar", "foo");
		internalGetParentPath("/foobar/../foo/bar", "/foo");
		internalGetParentPath("/foo/bar/../foobar", "/foo");
		internalGetParentPath("/foo/bar/..x/foobar", "/foo/bar/..x");
		internalGetParentPath("/foo/bar/../foobar/..", "/");
		internalGetParentPath("/foo/bar/..../foobar", "..");
	}

	@Test
	public void getCompatiblePath()
		throws Exception
	{
		internalGetCompatiblePath(".../foo", "../../foo");
		internalGetCompatiblePath("./foo", "foo");
		internalGetCompatiblePath("/foo/bar/foobar/...", "/foo");
		internalGetCompatiblePath("/foo", "/foo");
	}

	@Test
	public void isDotPattern()
		throws Exception
	{
		// TEST: de.huxhorn.sulky.resources.PathTools.isDotPattern(String): testIsDotPattern
		if(logger.isWarnEnabled())
		{
			logger
				.warn("Empty test method for de.huxhorn.sulky.resources.PathTools.isDotPattern(String): testIsDotPattern");
		}
	}

	@Test
	public void getPathStack()
		throws Exception
	{
		internalGetPathStack("", true, new String[]{});
		internalGetPathStack("/", true, new String[]{"/",});
		internalGetPathStack("/foo", true, new String[]{"/", "foo",});
		internalGetPathStack("foo", true, new String[]{"foo",});
		internalGetPathStack("/foo/bar/../foo", true, new String[]{"/", "foo", "foo",});
		internalGetPathStack("/foo/bar/...", true, new String[]{"/",});
		internalGetPathStack("/foo/bar/....", true, new String[]{"..",});
		internalGetPathStack("/foo/bar/..../foo", true, new String[]{"..", "foo",});
	}

	@Test
	public void getPathStackString()
		throws Exception
	{
		internalGetPathStackString("", true, "");
		internalGetPathStackString("/", true, "/");
		internalGetPathStackString("/foo", true, "/foo");
		internalGetPathStackString("foo", true, "foo");
		internalGetPathStackString("/foo/bar/../foo", true, "/foo/foo");
		internalGetPathStackString("/foo/bar/...", true, "/");
		internalGetPathStackString("/foo/bar/....", true, "..");
		internalGetPathStackString("/foo/bar/..../foo", true, "../foo");
		internalGetPathStackString("/foo/bar/..../foo", false, "/foo/bar/..../foo");
	}
}
