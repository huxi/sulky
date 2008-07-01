/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2008 Joern Huxhorn
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
package de.huxhorn.sulky.io;

import java.io.OutputStream;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicBoolean;

public class TimeoutOutputStream extends OutputStream
{
	private OutputStream stream;
	private int timeout;
	private AtomicLong operationStartTime;
	private AtomicBoolean closed;


	public TimeoutOutputStream(OutputStream stream, int timeout)
	{
		if(stream==null)
		{
			throw new NullPointerException("stream must not be null!");
		}
		if(timeout<=0)
		{
			throw new IllegalArgumentException("timeout must be a positive value!");
		}
		this.stream = stream;
		this.timeout = timeout;
		operationStartTime=new AtomicLong(-1);
		closed=new AtomicBoolean(false);

		Runnable timeoutRunnable=new TimeoutRunnable();
		Thread t=new Thread(timeoutRunnable, "TimeoutOutputStream Watchdog-Thread");
		t.start();
	}

	public void write(byte b[]) throws IOException
	{
		operationStartTime.set(System.currentTimeMillis());
		stream.write(b);
		operationStartTime.set(-1);
	}

	public void write(byte b[], int off, int len) throws IOException
	{
		operationStartTime.set(System.currentTimeMillis());
		stream.write(b, off, len);
		operationStartTime.set(-1);
	}

	public void write(int b) throws IOException
	{
		operationStartTime.set(System.currentTimeMillis());
		stream.write(b);
		operationStartTime.set(-1);
	}

	public void flush() throws IOException
	{
		operationStartTime.set(System.currentTimeMillis());
		stream.flush();
		operationStartTime.set(-1);
	}

	public void close() throws IOException
	{
		internalClose();
	}

	private void internalClose() throws IOException
	{
		try
		{
			stream.close();
		}
		finally
		{
			closed.set(true);
		}
	}

	private class TimeoutRunnable
		implements Runnable
	{
		public void run()
		{
			try
			{
				for (; ;)
				{
					if(closed.get())
					{
						//if(logger.isInfoEnabled()) logger.info("Stream was closed. Exiting run()-loop.");
						break;
					}
					long start = operationStartTime.get();
					if (start >= 0)
					{
						long since = System.currentTimeMillis() - start;

						if (since > timeout)
						{
							//if(logger.isInfoEnabled()) logger.info("Timeout detected! Exiting run()-loop.");
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
			catch (IOException e)
			{
				//if(logger.isWarnEnabled()) logger.warn("Exception while closing stream.", e);
			}
			catch (InterruptedException e)
			{
				//if(logger.isInfoEnabled()) logger.info("Interrupted....", e);
			}
		}
	}

}
