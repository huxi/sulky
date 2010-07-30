/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2010 Joern Huxhorn
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
 * Copyright 2007-2010 Joern Huxhorn
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
package de.huxhorn.sulky.io;

import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.io.Reader;
import java.io.Writer;

/**
 * As a matter of fact, catching IOException seems to be a major issue on Solaris
 * since it might actually be a java.io.InterruptedIOException that extends java.io.IOException
 * but must be handled like an InterruptedException, i.e. it should interrupt the current
 * thread.
 *
 * Catching java.lang.Exception or java.lang.Throwable has the same problem
 * since those could swallow either java.lang.InterruptedException
 * or java.io.InterruptedIOException, too.
 *
 * Use the method {@link #interruptIfNecessary(Throwable)} in those cases to make sure that
 * the current thread is interrupted properly. This method calls {@link java.lang.Thread#interrupt()}
 * on the current thread if either exception is contained in the cause hierarchy.
 *
 * The methods {@link #closeQuietly(InputStream)}, {@link #closeQuietly(OutputStream)},
 * {@link #closeQuietly(Reader)} and {@link #closeQuietly(Writer)} are replacements for the corresponding
 * methods in org.apache.commons.io.IOUtils. In addition, this class also includes a similar
 * {@link #closeQuietly(RandomAccessFile)} method for use with {@link RandomAccessFile}.
 *
 * This class is a direct result of http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=4385444
 * as well as the corresponding log4j bug report at https://issues.apache.org/bugzilla/show_bug.cgi?id=44157
 *
 * It was brought to my attention by the following post to the logback-user-mailinglist:
 * http://www.qos.ch/pipermail/logback-user/2010-July/001644.html
 * 
 * Thanks to Andreas Dejung for teaching me yet another piece of Java.
 *
 * @see InterruptedException
 * @see InterruptedIOException
 * @see Thread#interrupt() 
 */
public final class IOUtilities
{
	private IOUtilities()
	{}


	/**
	 * This method calls Thread.currentThread().interrupt() if any exception
	 * in the hierarchy (including all getCause()) is either InterruptedIOException
	 * or InterruptedException.
	 *
	 * This method should be called in every catch(IOException), catch(Exception) or
	 * catch(Throwable) block.
	 *
	 * @param t the Throwable to be checked for interruption. Does nothing if null.
	 */
	public static void interruptIfNecessary(Throwable t)
	{
		if(t == null)
		{
			return;
		}
		Throwable current=t;
		do
		{
			if(current instanceof InterruptedIOException || current instanceof InterruptedException)
			{
				Thread.currentThread().interrupt();
				break;
			}
			current=current.getCause();
		}
		while(current !=null);
	}

	/**
	 * Unconditionally close an <code>InputStream</code>.
	 * <p>
	 * Equivalent to {@link InputStream#close()}, except any exceptions will be ignored.
	 * {@link InterruptedIOException} is handled correctly by {@link #interruptIfNecessary(Throwable)}.
	 * This is typically used in finally blocks.
	 *
	 * @param x  the InputStream to close, may be null or already closed
	 */
	public static void closeQuietly(InputStream x)
	{
		if(x == null)
		{
			return;
		}
		try
		{
			x.close();
		}
		catch(IOException e)
		{
			interruptIfNecessary(e);
		}
	}

	/**
	 * Unconditionally close an <code>Reader</code>.
	 * <p>
	 * Equivalent to {@link Reader#close()}, except any exceptions will be ignored.
	 * {@link InterruptedIOException} is handled correctly by {@link #interruptIfNecessary(Throwable)}.
	 * This is typically used in finally blocks.
	 *
	 * @param x  the Reader to close, may be null or already closed
	 */
	public static void closeQuietly(Reader x)
	{
		if(x == null)
		{
			return;
		}
		try
		{
			x.close();
		}
		catch(IOException e)
		{
			interruptIfNecessary(e);
		}
	}

	/**
	 * Unconditionally close an <code>OutputStream</code>.
	 * <p>
	 * Equivalent to {@link OutputStream#close()}, except any exceptions will be ignored.
	 * {@link InterruptedIOException} is handled correctly by {@link #interruptIfNecessary(Throwable)}.
	 * This is typically used in finally blocks.
	 *
	 * @param x  the OutputStream to close, may be null or already closed
	 */
	public static void closeQuietly(OutputStream x)
	{
		if(x == null)
		{
			return;
		}
		try
		{
			x.close();
		}
		catch(IOException e)
		{
			interruptIfNecessary(e);
		}
	}

	/**
	 * Unconditionally close a <code>Writer</code>.
	 * <p>
	 * Equivalent to {@link Writer#close()}, except any exceptions will be ignored.
	 * {@link InterruptedIOException} is handled correctly by {@link #interruptIfNecessary(Throwable)}.
	 * This is typically used in finally blocks.
	 *
	 * @param x  the Writer to close, may be null or already closed
	 */
	public static void closeQuietly(Writer x)
	{
		if(x == null)
		{
			return;
		}
		try
		{
			x.close();
		}
		catch(IOException e)
		{
			interruptIfNecessary(e);
		}
	}

	/**
	 * Unconditionally close a <code>RandomAccessFile</code>.
	 * <p>
	 * Equivalent to {@link RandomAccessFile#close()}, except any exceptions will be ignored.
	 * {@link InterruptedIOException} is handled correctly by {@link #interruptIfNecessary(Throwable)}.
	 * This is typically used in finally blocks.
	 *
	 * @param x  the RandomAccessFile to close, may be null or already closed
	 */
	public static void closeQuietly(RandomAccessFile x)
	{
		if(x == null)
		{
			return;
		}
		try
		{
			x.close();
		}
		catch(IOException e)
		{
			interruptIfNecessary(e);
		}
	}
}
