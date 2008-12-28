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

import java.util.Stack;
import java.util.StringTokenizer;

/**
 * DOCUMENT: <code>PathTools</code>
 * ..-like path elements
 * In addition to the traditional "." and ".." path elements this class also supports
 * a shorthand version for path structures like "../.." which can be written as "...".
 */
public class PathTools
{
	/**
	 * This method will resolve the given <code>path</code> in relation
	 * with <code>basePath</code> if necessary.
	 * If <code>path</code> is a relative path it is appended to <code>basePath</code>
	 * and returned. Otherwise it is returned unchanged.
	 * An empty string is considered a relative path.
	 * Possibly contained dots are NOT evaluated/reduced!
	 * <p/>
	 * Examples:
	 * resolvePath("foo","/bar") returns "/bar"
	 * resolvePath("/foo","bar") returns "/foo/bar"
	 * resolvePath("/foo","../bar") returns "/foo/../bar"
	 *
	 * @param basePath the base-path needed in case of an relative path.
	 * @param path	 the path that will be resolved against <code>basePath</code> if it
	 *                 is relative.
	 * @return the new path that was resolved according to <code>basePath</code>
	 *         if necessary.
	 * @throws NullPointerException if either basePath or path are null.
	 * @see #evaluatePath(java.lang.String) evaluatePath can be used to evaluate/reduce dots
	 *      contained in resolved paths.
	 */
	public static String resolvePath(String basePath, String path)
	{
		final Logger logger = LoggerFactory.getLogger(PathTools.class);

		if (basePath == null)
		{
			NullPointerException ex = new NullPointerException("basePath must not be null!");
			if (logger.isDebugEnabled())
			{
				logger.debug("Parameter 'basePath' of method 'resolvePath' must not be null!", ex);
			}
			throw ex;
		}
		if (path == null)
		{
			NullPointerException ex = new NullPointerException("path must not be null!");
			if (logger.isDebugEnabled()) logger.debug("Parameter 'path' of method 'resolvePath' must not be null!", ex);
			throw ex;
		}
		if (path.startsWith("/"))
		{
			// path is absolute
			return path;
		}
		if (basePath.length() == 0)
		{
			return path;
		}
		if (path.length() == 0)
		{
			return basePath;
		}
		if (!basePath.endsWith("/"))
		{
			basePath = basePath + "/";
		}
		return basePath + path;
	}


	/**
	 * This method returns the given path after evaluating contained
	 * ..-style path elements, e.g. "/foo/bar/../foobar" will be evaluated
	 * to "/foo/foobar". You can use more dots to get further up the hierarchy,
	 * e.g. "/foo/bar/..../foobar" will be evaluated to "../foobar".
	 *
	 * @param path the path to be evaluated
	 * @return a path that contains at most one single ..-style path element as the first element.
	 * @throws NullPointerException if path is null.
	 * @see #resolvePath(java.lang.String, java.lang.String) resolvePath can be used to obtain a single path from a basePath and another path.
	 */
	public static String evaluatePath(String path)
	{
		Stack pathStack = getPathStack(path, true);

		return getPathStackString(pathStack);
	}

	/**
	 * Returns either the absolute path for the given basePath and path or null if
	 * the resulting path would not be absolute.
	 * <p/>
	 * Examples:
	 * getAbsolutePath("/foo/bar", "../foobar") returns "/foo/foobar"
	 * getAbsolutePath("/foo/bar", "..../foobar") returns null (path-underflow)
	 * getAbsolutePath("bar", "foobar") returns null (basePath not absolute)
	 *
	 * @param basePath the base path that is used to evaluate path.
	 * @param path the relative path.
	 * @return the absolute path or null if no such path was found, e.g. because
	 *         basePath wasn't absolute or because a path-underflow happened.
	 * @throws NullPointerException if either basePath or path is null.
	 */
	public static String getAbsolutePath(String basePath, String path)
	{
		String resolvedPath = resolvePath(basePath, path);

		String result = evaluatePath(resolvedPath);
		if (!result.startsWith("/"))
		{
			final Logger logger = LoggerFactory.getLogger(PathTools.class);
			// path is not absolute - returning null...
			if (logger.isDebugEnabled())
			{
				logger.debug("The evaluated path is not absolute: \"" + result + "\". Returning null for getAbsolutePath(\"" + basePath + "\", \"" + path + "\").");
			}
			return null;
		}

		return result;
	}

	/**
	 * Returns the parent of the given path. ..-style path elements will be evaluated
	 * in the process.
	 * It's essentially a shortcut for evaluatePath(resolvePath(path, "..")).
	 *
	 * @param path the path for which the parent should be resolved.
	 * @return the parent of the given path
	 */
	public static String getParentPath(String path)
	{
		return evaluatePath(resolvePath(path, ".."));
	}

	public static String getCompatiblePath(String path)
	{
		Stack<String> pathStack = getPathStack(path, true);
		if (pathStack.empty())
		{
			return "";
		}
		String first = pathStack.get(0);
		if (isDotPattern(first))
//        Matcher m=DOT_PATTERN.matcher(first);
//        if(m.matches())
		{
			pathStack.remove(0);
			int len = first.length();
			for (int i = 1; i < len; i++)
			{
				pathStack.add(0, "..");
			}
		}
		return getPathStackString(pathStack);
	}

	/**
	 * Returns <code>true</code> if the given String contains only dots.
	 *
	 * @param s the string that is checked for dots.
	 * @return <code>true</code> if the given String contains only dots
	 */
	public static boolean isDotPattern(String s)
	{
		for (int i = 0; i < s.length(); i++)
		{
			char c = s.charAt(i);
			if (c != '.')
			{
				return false;
			}
		}
		return true;
	}

	/**
	 * Returns a stack containing all the path elements of the given path.
	 * if evaluateDots is true the resulting stack will have at most one ..-like
	 * path element as the first element.
	 * If the resulting path is an absolute path the first element of the stack will be "/".
	 *
	 * @param path the input path
	 * @param evaluateDots if <code>true</code>, dots will be evaluated.
	 * @return a Stack containing all the path elements of path.
	 */
	public static Stack<String> getPathStack(String path, boolean evaluateDots)
	{
		Stack<String> pathStack = new Stack<String>();
		boolean wasAbsolute = false;
		if (path.startsWith("/"))
		{
			// remember absolute path...
			wasAbsolute = true;
		}
		int underflowCounter = 0;
		StringTokenizer tok = new StringTokenizer(path, "/", false);
		while (tok.hasMoreTokens())
		{
			String pathElement = tok.nextToken();
			if (pathElement.length() != 0)
			{
				// ignore wrong //-entries
				if (evaluateDots)
				{
//                    Matcher m=DOT_PATTERN.matcher(pathElement);
//                    if( m.matches())
					if (isDotPattern(pathElement))
					{
						// we found a dot-element
						for (int i = 1; i < pathElement.length(); i++)
						{
							if (pathStack.empty())
							{
								underflowCounter++;
							}
							else
							{
								pathStack.pop();
							}
						}
					}
					else
					{
						pathStack.push(pathElement);
					}
				}
				else
				{
					pathStack.push(pathElement);
				}
			}
		}
		if (underflowCounter > 0)
		{
			StringBuilder dots = new StringBuilder(".");
			for (int i = 0; i < underflowCounter; i++)
			{
				dots.append(".");
			}
			pathStack.add(0, dots.toString());
		}
		else if (wasAbsolute)
		{
			pathStack.add(0, "/");
		}

		return pathStack;
	}

	/**
	 * The pathStack itself is emptied in the process.
	 *
	 * @param pathStack the path stack.
	 * @return the string representation of the given path stack.
	 */
	public static String getPathStackString(Stack pathStack)
	{
		StringBuilder result = new StringBuilder();
		boolean rootFound = false;
		if (!pathStack.empty())
		{
			rootFound = true;
			String currentEntry = (String) pathStack.remove(0);
			if (!currentEntry.equals("/"))
			{

				rootFound = false;
				result.append(currentEntry);
			}

			while (!pathStack.empty())
			{
				result.append("/");
				result.append(pathStack.remove(0));
			}
		}
		if (rootFound && result.length() == 0)
		{
			result.append("/");
		}
		return result.toString();
	}
}
