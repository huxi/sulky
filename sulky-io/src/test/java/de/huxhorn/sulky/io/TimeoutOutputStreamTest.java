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

import org.junit.Test;
import static org.easymock.classextension.EasyMock.createStrictMock;
import static org.easymock.classextension.EasyMock.replay;
import static org.easymock.classextension.EasyMock.verify;
import static org.easymock.classextension.EasyMock.eq;
import static org.easymock.classextension.EasyMock.expectLastCall;
import static org.easymock.classextension.EasyMock.makeThreadSafe;
import org.easymock.IAnswer;

import java.io.OutputStream;
import java.io.IOException;

import static junit.framework.Assert.fail;
import static junit.framework.Assert.assertTrue;

public class TimeoutOutputStreamTest
{
	
	@Test
	public void normalUse() throws IOException
	{
		byte[] bytes=new byte[]{1,2,3,4,5,6,7,8,9};
		OutputStream mockStream = createStrictMock(OutputStream.class);
		mockStream.write(eq(bytes));
		mockStream.flush();
		mockStream.write(eq(17));
		mockStream.write(eq(bytes), eq(0), eq(5));
		mockStream.close();

		replay(mockStream);

		TimeoutOutputStream instance=new TimeoutOutputStream(mockStream, 1000);
		assertTrue("Stream is already closed!", !instance.isClosed());
		assertTrue("Watchdog Thread is not running!", instance.isWatchdogThreadRunning());
		instance.write(bytes);
		instance.flush();
		instance.write(17);
		instance.write(bytes, 0, 5);

		assertTrue("Stream is already closed!", !instance.isClosed());
		assertTrue("Watchdog Thread is not running!", instance.isWatchdogThreadRunning());

		instance.close();

		verify(mockStream);

		assertTrue("Stream is not closed!", instance.isClosed());
		assertTrue("Watchdog Thread is still running!", !instance.isWatchdogThreadRunning());
	}

	@Test
	public void exceptionInWriteByte() throws IOException
	{
		OutputStream mockStream = createStrictMock(OutputStream.class);
		mockStream.write(17);
		//noinspection ThrowableInstanceNeverThrown
		expectLastCall().andThrow(new IOException("Some exception"));

		mockStream.close();

		replay(mockStream);

		TimeoutOutputStream instance=new TimeoutOutputStream(mockStream, 1000);
		assertTrue("Stream is already closed!", !instance.isClosed());
		assertTrue("Watchdog Thread is not running!", instance.isWatchdogThreadRunning());
		try
		{
			instance.write(17);
			fail("Exception should have been thrown!");
		}
		catch(IOException ex)
		{
			// expected
		}

		verify(mockStream);
		assertTrue("Stream is not closed!", instance.isClosed());
		assertTrue("Watchdog Thread is still running!", !instance.isWatchdogThreadRunning());
	}

	@Test
	public void exceptionInWriteByteArray() throws IOException
	{
		byte[] bytes=new byte[]{1,2,3,4,5,6,7,8,9};
		OutputStream mockStream = createStrictMock(OutputStream.class);
		mockStream.write(eq(bytes));
		//noinspection ThrowableInstanceNeverThrown
		expectLastCall().andThrow(new IOException("Some exception"));

		mockStream.close();

		replay(mockStream);

		TimeoutOutputStream instance=new TimeoutOutputStream(mockStream, 1000);
		assertTrue("Stream is already closed!", !instance.isClosed());
		assertTrue("Watchdog Thread is not running!", instance.isWatchdogThreadRunning());
		try
		{
			instance.write(bytes);
			fail("Exception should have been thrown!");
		}
		catch(IOException ex)
		{
			// expected
		}

		verify(mockStream);
		assertTrue("Stream is not closed!", instance.isClosed());
		assertTrue("Watchdog Thread is still running!", !instance.isWatchdogThreadRunning());
	}

	@Test
	public void exceptionInWriteByteArrayOffset() throws IOException
	{
		byte[] bytes=new byte[]{1,2,3,4,5,6,7,8,9};
		OutputStream mockStream = createStrictMock(OutputStream.class);
		mockStream.write(eq(bytes), eq(0), eq(5));
		//noinspection ThrowableInstanceNeverThrown
		expectLastCall().andThrow(new IOException("Some exception"));

		mockStream.close();

		replay(mockStream);

		TimeoutOutputStream instance=new TimeoutOutputStream(mockStream, 1000);
		assertTrue("Stream is already closed!", !instance.isClosed());
		assertTrue("Watchdog Thread is not running!", instance.isWatchdogThreadRunning());
		try
		{
			instance.write(bytes, 0, 5);
			fail("Exception should have been thrown!");
		}
		catch(IOException ex)
		{
			// expected
		}

		verify(mockStream);
		assertTrue("Stream is not closed!", instance.isClosed());
		assertTrue("Watchdog Thread is still running!", !instance.isWatchdogThreadRunning());
	}

	@Test
	public void exceptionInFlush() throws IOException
	{
		OutputStream mockStream = createStrictMock(OutputStream.class);
		mockStream.flush();
		//noinspection ThrowableInstanceNeverThrown
		expectLastCall().andThrow(new IOException("Some exception"));

		mockStream.close();

		replay(mockStream);

		TimeoutOutputStream instance=new TimeoutOutputStream(mockStream, 1000);
		assertTrue("Stream is already closed!", !instance.isClosed());
		assertTrue("Watchdog Thread is not running!", instance.isWatchdogThreadRunning());
		try
		{
			instance.flush();
			fail("Exception should have been thrown!");
		}
		catch(IOException ex)
		{
			// expected
		}

		verify(mockStream);
		assertTrue("Stream is not closed!", instance.isClosed());
		assertTrue("Watchdog Thread is still running!", !instance.isWatchdogThreadRunning());
	}

	@Test
	public void exceptionInClose() throws IOException
	{
		OutputStream mockStream = createStrictMock(OutputStream.class);
		mockStream.close();
		//noinspection ThrowableInstanceNeverThrown
		expectLastCall().andThrow(new IOException("Some exception"));

		replay(mockStream);

		TimeoutOutputStream instance=new TimeoutOutputStream(mockStream, 1000);
		assertTrue("Stream is already closed!", !instance.isClosed());
		assertTrue("Watchdog Thread is not running!", instance.isWatchdogThreadRunning());
		try
		{
			instance.close();
			fail("Exception should have been thrown!");
		}
		catch(IOException ex)
		{
			// expected
		}

		verify(mockStream);
		assertTrue("Stream is not closed!", instance.isClosed());
		assertTrue("Watchdog Thread is still running!", !instance.isWatchdogThreadRunning());
	}



	@Test
	public void runtimeExceptionInWriteByte() throws IOException
	{
		OutputStream mockStream = createStrictMock(OutputStream.class);
		mockStream.write(17);
		//noinspection ThrowableInstanceNeverThrown
		expectLastCall().andThrow(new RuntimeException("Some exception"));

		mockStream.close();

		replay(mockStream);

		TimeoutOutputStream instance=new TimeoutOutputStream(mockStream, 1000);
		assertTrue("Stream is already closed!", !instance.isClosed());
		assertTrue("Watchdog Thread is not running!", instance.isWatchdogThreadRunning());
		try
		{
			instance.write(17);
			fail("Exception should have been thrown!");
		}
		catch(RuntimeException ex)
		{
			// expected
		}

		verify(mockStream);
		assertTrue("Stream is not closed!", instance.isClosed());
		assertTrue("Watchdog Thread is still running!", !instance.isWatchdogThreadRunning());
	}

	@Test
	public void runtimeExceptionInWriteByteArray() throws IOException
	{
		byte[] bytes=new byte[]{1,2,3,4,5,6,7,8,9};
		OutputStream mockStream = createStrictMock(OutputStream.class);
		mockStream.write(eq(bytes));
		//noinspection ThrowableInstanceNeverThrown
		expectLastCall().andThrow(new RuntimeException("Some exception"));

		mockStream.close();

		replay(mockStream);

		TimeoutOutputStream instance=new TimeoutOutputStream(mockStream, 1000);
		assertTrue("Stream is already closed!", !instance.isClosed());
		assertTrue("Watchdog Thread is not running!", instance.isWatchdogThreadRunning());
		try
		{
			instance.write(bytes);
			fail("Exception should have been thrown!");
		}
		catch(RuntimeException ex)
		{
			// expected
		}

		verify(mockStream);
		assertTrue("Stream is not closed!", instance.isClosed());
		assertTrue("Watchdog Thread is still running!", !instance.isWatchdogThreadRunning());
	}

	@Test
	public void runtimeExceptionInWriteByteArrayOffset() throws IOException
	{
		byte[] bytes=new byte[]{1,2,3,4,5,6,7,8,9};
		OutputStream mockStream = createStrictMock(OutputStream.class);
		mockStream.write(eq(bytes), eq(0), eq(5));
		//noinspection ThrowableInstanceNeverThrown
		expectLastCall().andThrow(new RuntimeException("Some exception"));

		mockStream.close();

		replay(mockStream);

		TimeoutOutputStream instance=new TimeoutOutputStream(mockStream, 1000);
		assertTrue("Stream is already closed!", !instance.isClosed());
		assertTrue("Watchdog Thread is not running!", instance.isWatchdogThreadRunning());
		try
		{
			instance.write(bytes, 0, 5);
			fail("Exception should have been thrown!");
		}
		catch(RuntimeException ex)
		{
			// expected
		}

		verify(mockStream);
		assertTrue("Stream is not closed!", instance.isClosed());
		assertTrue("Watchdog Thread is still running!", !instance.isWatchdogThreadRunning());
	}

	@Test
	public void runtimeExceptionInFlush() throws IOException
	{
		OutputStream mockStream = createStrictMock(OutputStream.class);
		mockStream.flush();
		//noinspection ThrowableInstanceNeverThrown
		expectLastCall().andThrow(new RuntimeException("Some exception"));

		mockStream.close();

		replay(mockStream);

		TimeoutOutputStream instance=new TimeoutOutputStream(mockStream, 1000);
		assertTrue("Stream is already closed!", !instance.isClosed());
		assertTrue("Watchdog Thread is not running!", instance.isWatchdogThreadRunning());
		try
		{
			instance.flush();
			fail("Exception should have been thrown!");
		}
		catch(RuntimeException ex)
		{
			// expected
		}

		verify(mockStream);
		assertTrue("Stream is not closed!", instance.isClosed());
		assertTrue("Watchdog Thread is still running!", !instance.isWatchdogThreadRunning());
	}

	@Test
	public void runtimeExceptionInClose() throws IOException
	{
		OutputStream mockStream = createStrictMock(OutputStream.class);
		mockStream.close();
		//noinspection ThrowableInstanceNeverThrown
		expectLastCall().andThrow(new RuntimeException("Some exception"));

		replay(mockStream);

		TimeoutOutputStream instance=new TimeoutOutputStream(mockStream, 1000);
		assertTrue("Stream is already closed!", !instance.isClosed());
		assertTrue("Watchdog Thread is not running!", instance.isWatchdogThreadRunning());
		try
		{
			instance.close();
			fail("Exception should have been thrown!");
		}
		catch(RuntimeException ex)
		{
			// expected
		}

		verify(mockStream);
		assertTrue("Stream is not closed!", instance.isClosed());
		assertTrue("Watchdog Thread is still running!", !instance.isWatchdogThreadRunning());
	}





	@Test
	public void timeoutInWriteByte() throws IOException
	{
		OutputStream mockStream = createStrictMock(OutputStream.class);
		makeThreadSafe(mockStream,true);
		mockStream.write(eq(17));
		expectLastCall().andAnswer(new IAnswer<Object>()
		{
			public Object answer() throws Throwable
			{
				Thread.sleep(300);
				return null;
			}
		});
		mockStream.close();

		replay(mockStream);

		TimeoutOutputStream instance=new TimeoutOutputStream(mockStream, 100);
		assertTrue("Stream is already closed!", !instance.isClosed());
		assertTrue("Watchdog Thread is not running!", instance.isWatchdogThreadRunning());

		instance.write(17); // would throw exception in case of a real output stream.

		verify(mockStream);
		assertTrue("Stream is not closed!", instance.isClosed());
		assertTrue("Watchdog Thread is still running!", !instance.isWatchdogThreadRunning());
	}

	@Test
	public void timeoutInWriteByteArray() throws IOException
	{
		byte[] bytes=new byte[]{1,2,3,4,5,6,7,8,9};
		OutputStream mockStream = createStrictMock(OutputStream.class);
		makeThreadSafe(mockStream,true);
		mockStream.write(eq(bytes));
		expectLastCall().andAnswer(new IAnswer<Object>()
		{
			public Object answer() throws Throwable
			{
				Thread.sleep(300);
				return null;
			}
		});
		mockStream.close();

		replay(mockStream);

		TimeoutOutputStream instance=new TimeoutOutputStream(mockStream, 100);
		assertTrue("Stream is already closed!", !instance.isClosed());
		assertTrue("Watchdog Thread is not running!", instance.isWatchdogThreadRunning());

		instance.write(bytes); // would throw exception in case of a real output stream.

		verify(mockStream);
		assertTrue("Stream is not closed!", instance.isClosed());
		assertTrue("Watchdog Thread is still running!", !instance.isWatchdogThreadRunning());
	}

	@Test
	public void timeoutInWriteByteArrayOffset() throws IOException
	{
		byte[] bytes=new byte[]{1,2,3,4,5,6,7,8,9};
		OutputStream mockStream = createStrictMock(OutputStream.class);
		makeThreadSafe(mockStream,true);
		mockStream.write(eq(bytes), eq(0), eq(5));
		expectLastCall().andAnswer(new IAnswer<Object>()
		{
			public Object answer() throws Throwable
			{
				Thread.sleep(300);
				return null;
			}
		});
		mockStream.close();

		replay(mockStream);

		TimeoutOutputStream instance=new TimeoutOutputStream(mockStream, 100);
		assertTrue("Stream is already closed!", !instance.isClosed());
		assertTrue("Watchdog Thread is not running!", instance.isWatchdogThreadRunning());

		instance.write(bytes, 0, 5); // would throw exception in case of a real output stream.

		verify(mockStream);
		assertTrue("Stream is not closed!", instance.isClosed());
		assertTrue("Watchdog Thread is still running!", !instance.isWatchdogThreadRunning());
	}

	@Test
	public void timeoutInFlush() throws IOException
	{
		OutputStream mockStream = createStrictMock(OutputStream.class);
		makeThreadSafe(mockStream,true);
		mockStream.flush();
		expectLastCall().andAnswer(new IAnswer<Object>()
		{
			public Object answer() throws Throwable
			{
				Thread.sleep(300);
				return null;
			}
		});
		mockStream.close();

		replay(mockStream);

		TimeoutOutputStream instance=new TimeoutOutputStream(mockStream, 100);
		assertTrue("Stream is already closed!", !instance.isClosed());
		assertTrue("Watchdog Thread is not running!", instance.isWatchdogThreadRunning());

		instance.flush(); // would throw exception in case of a real output stream.

		verify(mockStream);
		assertTrue("Stream is not closed!", instance.isClosed());
		assertTrue("Watchdog Thread is still running!", !instance.isWatchdogThreadRunning());
	}

	@Test
	public void timeoutInClose() throws IOException
	{
		OutputStream mockStream = createStrictMock(OutputStream.class);
		makeThreadSafe(mockStream,true);
		mockStream.close();
		expectLastCall().andAnswer(new IAnswer<Object>()
		{
			public Object answer() throws Throwable
			{
				Thread.sleep(300);
				return null;
			}
		});

		replay(mockStream);

		TimeoutOutputStream instance=new TimeoutOutputStream(mockStream, 100);
		assertTrue("Stream is already closed!", !instance.isClosed());
		assertTrue("Watchdog Thread is not running!", instance.isWatchdogThreadRunning());

		instance.close(); // would throw exception in case of a real output stream.

		verify(mockStream);
		assertTrue("Stream is not closed!", instance.isClosed());
		assertTrue("Watchdog Thread is still running!", !instance.isWatchdogThreadRunning());
	}
}
