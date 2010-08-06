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

package de.huxhorn.sulky.buffers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.ref.Reference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.SoftReference;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SoftReferenceCachingBuffer<E>
	implements Buffer<E>, ResetOperation, DisposeOperation, FlushOperation
{
	private final Logger logger = LoggerFactory.getLogger(SoftReferenceCachingBuffer.class);

	private static final ReferenceQueue REFERENCE_QUEUE = new ReferenceQueue();

	static
	{
		Thread cleanupThread = new Thread(new ReferenceQueueRunnable(), "ReferenceQueue-Cleanup");
		cleanupThread.setDaemon(true);
		cleanupThread.start();
		final Logger logger = LoggerFactory.getLogger(SoftReferenceCachingBuffer.class);

		if(logger.isInfoEnabled()) logger.info("Started thread {}.", cleanupThread);
	}

	private Buffer<E> buffer;

	private Map<Long, MySoftReference<E>> cache;
	private boolean disposed;

	public SoftReferenceCachingBuffer(Buffer<E> buffer)
	{
		this.disposed = false;
		this.buffer = buffer;
		this.cache = new ConcurrentHashMap<Long, MySoftReference<E>>();
	}

	Buffer<E> getWrappedBuffer()
	{
		return buffer;
	}

	public E get(long index)
	{
		if(disposed)
		{
			return null;
		}
		SoftReference<E> softy = cache.get(index);
		E result;
		if(softy != null)
		{
			result = softy.get();
			if(result == null)
			{
				cache.remove(index);
			}
			else
			{
				// found in cache...
				if(logger.isDebugEnabled()) logger.debug("Retrieved {} from cache.", index);
				return result;
			}
		}

		result = buffer.get(index);
		if(result != null)
		{
			cache.put(index, new MySoftReference<E>(cache, index, result));
			if(logger.isDebugEnabled()) logger.debug("Added {} to cache.", index);
		}
		return result;
	}

	public long getSize()
	{
		return buffer.getSize();
	}

	public Iterator<E> iterator()
	{
		return buffer.iterator();
	}

	public void reset()
	{
		Reset.reset(buffer);
		cache.clear();
	}

	public void dispose()
	{
		disposed = true;
		cache.clear();
		Dispose.dispose(buffer);
	}

	public boolean isDisposed()
	{
		return disposed;
	}

	public void flush()
	{
		Flush.flush(buffer);
		cache.clear();
	}

	private static class MySoftReference<E>
		extends SoftReference<E>
	{
		private long index;
		private Map<Long, MySoftReference<E>> cache;

		@SuppressWarnings({"unchecked"})
		public MySoftReference(Map<Long, MySoftReference<E>> cache, long index, E referent)
		{
			// the following cast is safe since we are not using the content in the reference queue......
			super(referent, REFERENCE_QUEUE);
			this.index = index;
			this.cache = cache;
		}

		public long getIndex()
		{
			return index;
		}

		public void removeFromCache()
		{
			cache.remove(index);
			final Logger logger = LoggerFactory.getLogger(SoftReferenceCachingBuffer.class);
			if(logger.isDebugEnabled()) logger.debug("Removed {} from cache.", index);
		}
	}

	private static class ReferenceQueueRunnable
		implements Runnable
	{

		public void run()
		{

			for(; ;)
			{
				try
				{
					Reference ref = REFERENCE_QUEUE.remove();
					if(ref instanceof MySoftReference)
					{
						MySoftReference reference = (MySoftReference) ref;
						reference.removeFromCache();
					}
					else
					{
						final Logger logger = LoggerFactory.getLogger(SoftReferenceCachingBuffer.class);
						if(logger.isWarnEnabled()) logger.warn("Unexpected reference!! {}", ref);
					}
				}
				catch(InterruptedException e)
				{
					final Logger logger = LoggerFactory.getLogger(SoftReferenceCachingBuffer.class);
					if(logger.isInfoEnabled()) logger.info("Interrupted ReferenceQueueRunnable...");
					break;
				}
			}
		}
	}
}
