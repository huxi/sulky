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
package de.huxhorn.sulky.buffers;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.List;
import java.util.Iterator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlockingCircularBuffer<E>
	implements CircularBuffer<E>
{
	private final Logger logger = LoggerFactory.getLogger(BlockingCircularBuffer.class);

	private Lock lock;
	private OverwritingCircularBuffer<E> events;
	private static final int DEFAULT_CONGESTION_DELAY = 500;
	private int congestionDelay;

	public BlockingCircularBuffer(int bufferSize, int congestionDelay)
	{
		events=new OverwritingCircularBuffer<E>(bufferSize);
		lock=new ReentrantLock();
		this.congestionDelay=congestionDelay;
	}

	public BlockingCircularBuffer(int bufferSize)
	{
		this(bufferSize,DEFAULT_CONGESTION_DELAY);
	}

	public int getCongestionDelay()
	{
		return congestionDelay;
	}

	public void setCongestionDelay(int congestionDelay)
	{
		if(congestionDelay<0)
		{
			throw new IllegalArgumentException("congestionDelay ("+congestionDelay+") must not be negative!");
		}
		this.congestionDelay = congestionDelay;
	}

	public void add(E element)
	{
		lock.lock();
		try
		{
			while(events.isFull())
			{
				lock.unlock();
				try
				{
					if(logger.isWarnEnabled()) logger.warn("Congestion ({} events) detected, sleeping for {} millis.", events.getAvailableElements(), congestionDelay);
					if(congestionDelay>0)
					{
						Thread.sleep(congestionDelay);
					}
				}
				catch (InterruptedException e)
				{
					if(logger.isInfoEnabled()) logger.info("Interrupted...");
					return;
				}
				lock.lock();
			}
			events.add(element);
		}
		finally
		{
			lock.unlock();
		}
	}

	public void addAll(List<E> elements)
	{
		for (E element : elements)
		{
			add(element);
		}
	}

	public void addAll(E[] elements)
	{
		for (E element : elements)
		{
			add(element);
		}
	}

	public boolean isFull()
	{
		lock.lock();
		try
		{
			return events.isFull();
		}
		finally
		{
			lock.unlock();
		}
	}

	public E removeFirst()
	{
		lock.lock();
		try
		{
			return events.removeFirst();
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 *
	 * @return either <tt>null</tt> or a List containing all accumulated events.
	 */
	public List<E> removeAll()
	{
		lock.lock();
		try
		{
			if(!events.isEmpty())
			{
				return events.removeAll();
			}
			return null;
		}
		finally
		{
			lock.unlock();
		}
	}

	public void clear()
	{
		lock.lock();
		try
		{
			events.clear();
		}
		finally
		{
			lock.unlock();
		}
	}

	public boolean isEmpty()
	{
		lock.lock();
		try
		{
			return events.isEmpty();
		}
		finally
		{
			lock.unlock();
		}
	}

	public E getRelative(int index)
	{
		lock.lock();
		try
		{
			return events.getRelative(index);
		}
		finally
		{
			lock.unlock();
		}
	}

	public E setRelative(int index, E element)
	{
		lock.lock();
		try
		{
			return events.setRelative(index, element);
		}
		finally
		{
			lock.unlock();
		}
	}

	public long getOverflowCounter()
	{
		return 0;
	}

	public int getAvailableElements()
	{
		lock.lock();
		try
		{
			return events.getAvailableElements();
		}
		finally
		{
			lock.unlock();
		}
	}

	public int getBufferSize()
	{
		return events.getBufferSize();
	}

	public E get(long index)
	{
		lock.lock();
		try
		{
			return events.get(index);
		}
		finally
		{
			lock.unlock();
		}
	}

	public long getSize()
	{
		lock.lock();
		try
		{
			return events.getSize();
		}
		finally
		{
			lock.unlock();
		}
	}

	public void reset()
	{
		lock.lock();
		try
		{
			events.reset();
		}
		finally
		{
			lock.unlock();
		}
	}

	public Iterator<E> iterator()
	{
		lock.lock();
		try
		{
			return events.iterator();
		}
		finally
		{
			lock.unlock();
		}
	}
}
