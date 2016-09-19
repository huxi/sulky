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

package de.huxhorn.sulky.io;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Don't put a BufferedOutputStream into this class!
 * Its close() method (inherited from FilterOutputStream) calls flush() before actually closing
 * the underlying stream. While this is fine in most cases, it can cause a livelock in this case and prevents
 * the timeout of TimeoutOutputStream from working reliably.
 */
public class TimeoutOutputStream
	extends OutputStream
{
	private final OutputStream stream;
	private int timeout;
	private AtomicLong operationStartTime;
	private AtomicBoolean closed;
	private Thread watchdogThread;
	private AtomicBoolean watchdogThreadRunning;


	public TimeoutOutputStream(OutputStream stream, int timeout)
	{
		watchdogThreadRunning = new AtomicBoolean(false);
		if(stream == null)
		{
			throw new IllegalArgumentException("stream must not be null!");
		}
		if(timeout <= 0)
		{
			throw new IllegalArgumentException("timeout must be a positive value!");
		}
		this.stream = stream;
		this.timeout = timeout;
		operationStartTime = new AtomicLong(-1);
		closed = new AtomicBoolean(false);

		Runnable timeoutRunnable = new TimeoutRunnable();
		watchdogThread = new Thread(timeoutRunnable, "TimeoutOutputStream Watchdog-Thread");
		watchdogThread.start();
		try
		{
			while(!watchdogThreadRunning.get())
			{
				Thread.sleep(10); // give the watchdog thread a chance to start...
			}
		}
		catch(InterruptedException e)
		{
			IOUtilities.interruptIfNecessary(e);
		}
	}

	public void write(byte b[])
		throws IOException
	{
		try
		{
			operationStartTime.set(System.currentTimeMillis());
			stream.write(b);
			operationStartTime.set(-1);
		}
		catch(IOException | RuntimeException ex)
		{
			internalClose();
			throw ex;
		}
	}

	public void write(byte b[], int off, int len)
		throws IOException
	{
		try
		{
			operationStartTime.set(System.currentTimeMillis());
			stream.write(b, off, len);
			operationStartTime.set(-1);
		}
		catch(IOException | RuntimeException ex)
		{
			internalClose();
			throw ex;
		}
	}

	public void write(int b)
		throws IOException
	{
		try
		{
			operationStartTime.set(System.currentTimeMillis());
			stream.write(b);
			operationStartTime.set(-1);
		}
		catch(IOException | RuntimeException ex)
		{
			internalClose();
			throw ex;
		}
	}

	public void flush()
		throws IOException
	{
		try
		{
			operationStartTime.set(System.currentTimeMillis());
			stream.flush();
			operationStartTime.set(-1);
		}
		catch(IOException | RuntimeException ex)
		{
			internalClose();
			throw ex;
		}
	}

	public void close()
		throws IOException
	{
		internalClose();
	}

	public boolean isClosed()
	{
		return closed.get();
	}

	private void internalClose()
		throws IOException
	{
		if(!closed.get())
		{
			closed.set(true);
			try
			{
				stream.close();
			}
			finally
			{
				if(watchdogThread != null)
				{
					watchdogThread.interrupt();
					watchdogThread = null;
				}
			}
		}
	}

	boolean isWatchdogThreadRunning()
	{
		return watchdogThreadRunning.get();
	}

	private class TimeoutRunnable
		implements Runnable
	{
		public void run()
		{
			watchdogThreadRunning.set(true);
			try
			{
				for(; ;)
				{
					if(closed.get())
					{
						break;
					}
					long start = operationStartTime.get();
					if(start >= 0)
					{
						long since = System.currentTimeMillis() - start;

						if(since > timeout)
						{
							internalClose();
							break;
						}
						else
						{
							Thread.sleep(timeout - since);
						}
					}
					else
					{
						Thread.sleep(timeout);
					}
				}
			}
			catch(IOException | InterruptedException | RuntimeException e)
			{
				IOUtilities.interruptIfNecessary(e);
			}
			watchdogThreadRunning.set(false);
		}
	}

}
