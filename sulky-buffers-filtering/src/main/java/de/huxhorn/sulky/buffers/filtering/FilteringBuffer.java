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

package de.huxhorn.sulky.buffers.filtering;

import de.huxhorn.sulky.buffers.BasicBufferIterator;
import de.huxhorn.sulky.buffers.Buffer;
import de.huxhorn.sulky.buffers.DisposeOperation;
import de.huxhorn.sulky.buffers.Reset;
import de.huxhorn.sulky.buffers.ResetOperation;
import de.huxhorn.sulky.conditions.Condition;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class FilteringBuffer<E>
	implements Buffer<E>, DisposeOperation, ResetOperation
{
	private final Logger logger = LoggerFactory.getLogger(FilteringBuffer.class);

	public static <E> Buffer<E> resolveSourceBuffer(Buffer<E> buffer)
	{
		for(; ;)
		{
			if(!(buffer instanceof FilteringBuffer))
			{
				return buffer;
			}
			buffer = ((FilteringBuffer<E>) buffer).getSourceBuffer();
		}
	}

	private Buffer<E> sourceBuffer;
	private Condition condition;
	private final ReentrantReadWriteLock indicesLock;
	private final List<Long> filteredIndices;
	private boolean disposed;

	public FilteringBuffer(Buffer<E> sourceBuffer, Condition condition)
	{
		this.indicesLock = new ReentrantReadWriteLock(true);
		this.sourceBuffer = sourceBuffer;
		this.condition = condition;
		this.filteredIndices = new ArrayList<Long>();
		this.disposed = false;
	}

	public E get(long index)
	{
		long realIndex = getSourceIndex(index);
		if(realIndex >= 0)
		{
			return sourceBuffer.get(realIndex);
		}
		return null;
	}

	public long getSourceIndex(long index)
	{
		long realIndex = -1;

		ReentrantReadWriteLock.ReadLock lock = indicesLock.readLock();
		lock.lock();
		try
		{
			if(index >= 0 && index < filteredIndices.size())
			{
				realIndex = filteredIndices.get((int) index);
			}
		}
		finally
		{
			lock.unlock();
		}
		return realIndex;
	}

	public long getSize()
	{
		ReentrantReadWriteLock.ReadLock lock = indicesLock.readLock();
		lock.lock();
		try
		{
			return filteredIndices.size();
		}
		finally
		{
			lock.unlock();
		}
	}

	public void addFilteredIndex(long index)
	{
		long size = sourceBuffer.getSize();
		if(index < 0 || index >= sourceBuffer.getSize())
		{
			if(logger.isInfoEnabled()) logger.info("Invalid filtered index {} (size={})!", index, size);
		}
		ReentrantReadWriteLock.WriteLock lock = indicesLock.writeLock();
		lock.lock();
		try
		{
			filteredIndices.add(index);
		}
		finally
		{
			lock.unlock();
		}
	}

	public void clearFilteredIndices()
	{
		ReentrantReadWriteLock.WriteLock lock = indicesLock.writeLock();
		lock.lock();
		try
		{
			filteredIndices.clear();
		}
		finally
		{
			lock.unlock();
		}
	}

	public Iterator<E> iterator()
	{
		return new BasicBufferIterator<E>(this);
	}

	public Buffer<E> getSourceBuffer()
	{
		return sourceBuffer;
	}

	public Condition getCondition()
	{
		return condition;
	}

	public void dispose()
	{
		this.disposed = true;
	}

	public boolean isDisposed()
	{
		return disposed;
	}

	public void reset()
	{
		boolean reset = Reset.reset(sourceBuffer);
		if(reset)
		{
			clearFilteredIndices();
		}
	}
}
