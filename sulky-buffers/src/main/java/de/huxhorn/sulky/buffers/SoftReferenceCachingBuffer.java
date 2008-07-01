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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.lang.ref.SoftReference;
import java.lang.ref.ReferenceQueue;
import java.lang.ref.Reference;

public class SoftReferenceCachingBuffer<E>
	implements Buffer<E>, ResetOperation, DisposeOperation
{
	private final Logger logger = LoggerFactory.getLogger(SoftReferenceCachingBuffer.class);

	private static final ReferenceQueue refQueue=new ReferenceQueue();
	static
	{
		Thread cleanupThread=new Thread(new ReferenceQueueRunnable(), "ReferenceQueue-Cleanup");
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
		this.disposed=false;
		this.buffer = buffer;
		this.cache=new ConcurrentHashMap<Long, MySoftReference<E>>();
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
		SoftReference<E> softy=cache.get(index);
		E result;
		if(softy!=null)
		{
			result=softy.get();
			if(result==null)
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

		result=buffer.get(index);
		if(result!=null)
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
		if(buffer instanceof ResetOperation)
		{
			ResetOperation op=(ResetOperation) buffer;
			op.reset();
		}
		cache.clear();
	}

	public void dispose()
	{
		disposed=true;
		cache.clear();
		if(buffer instanceof DisposeOperation)
		{
			DisposeOperation disposeable = (DisposeOperation) buffer;
			disposeable.dispose();
		}
	}

	public boolean isDisposed()
	{
		return disposed;
	}

	private static class MySoftReference<E>
		extends SoftReference<E>
	{
		private long index;
		private Map<Long, MySoftReference<E>> cache;

		public MySoftReference(Map<Long, MySoftReference<E>> cache, long index, E referent)
		{
			// the following cast is safe since we are not using the content in the reference queue......
			//noinspection unchecked
			super(referent, refQueue);
			this.index = index;
			this.cache=cache;
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

			for(;;)
			{
				try
				{
					Reference ref = refQueue.remove();
					if(ref instanceof MySoftReference)
					{
						MySoftReference reference=(MySoftReference)ref;
						reference.removeFromCache();
					}
					else
					{
						final Logger logger = LoggerFactory.getLogger(SoftReferenceCachingBuffer.class);
						if(logger.isWarnEnabled()) logger.warn("Unexpected reference!! {}",ref);
					}
				}
				catch (InterruptedException e)
				{
					final Logger logger = LoggerFactory.getLogger(SoftReferenceCachingBuffer.class);
					if(logger.isInfoEnabled()) logger.info("Interrupted ReferenceQueueRunnable...");
					break;
				}
			}
		}
	}
}
