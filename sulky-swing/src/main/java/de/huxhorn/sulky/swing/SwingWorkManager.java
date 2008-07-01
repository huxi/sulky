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
package de.huxhorn.sulky.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.swing.SwingUtilities;
import java.util.concurrent.*;
import java.util.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

public class SwingWorkManager<V>
{
	private final Logger logger = LoggerFactory.getLogger(SwingWorkManager.class);

	private ExecutorService executor;
	private final List<Future<V>> futures;
	private final List<ResultListener> resultListeners;
	private final List<PropertyChangeEvent> internalProgressChanges;
	private final Map<Future<V>, Callable<V>> futureCallableMapping;

	private PropertyChangeListener progressChangeListener;

	public SwingWorkManager()
	{
		futures = new LinkedList<Future<V>>();
		futureCallableMapping=new HashMap<Future<V>, Callable<V>>();
		progressChangeListener =new ProgressChangeListener();
		internalProgressChanges =new ArrayList<PropertyChangeEvent>();
		resultListeners = new LinkedList<ResultListener>();
		executor = Executors.newCachedThreadPool();
		Thread t=new Thread(new ResultPoller(), "ResultPoller Runnable");
		t.setDaemon(true);
		t.start();
	}

	public Future<V> add(Callable<V> callable)
	{
		if(callable instanceof ProgressingCallable)
		{
			ProgressingCallable pcallable=(ProgressingCallable) callable;
			pcallable.addPropertyChangeListener(progressChangeListener);
			if(logger.isDebugEnabled()) logger.debug("Added progress change listener to callable.");
		}
		Future<V> future = executor.submit(callable);
		synchronized(futureCallableMapping)
		{
			futureCallableMapping.put(future, callable);
		}
		watch(future);
		return future;
	}

	private void watch(Future<V> future)
	{
		synchronized (futures)
		{
			futures.add(future);
		}
	}

	class ResultPoller
			implements Runnable
	{
		private final Logger logger = LoggerFactory.getLogger(ResultPoller.class);

		private static final long POLL_INTERVAL = 200;

		public void run()
		{
			for (; ;)
			{
				try
				{
					List<Future<V>> done = null;
					synchronized (futures)
					{
						for(Future<V> future: futures)
						{
							if (future.isDone())
							{
								if(done==null)
								{
									done=new ArrayList<Future<V>>();
								}
								done.add(future);
							}
						}
						if(done!=null)
						{
							futures.removeAll(done);
						}
					}
					List<PropertyChangeEvent> progressChanges = null;
					synchronized(internalProgressChanges)
					{
						if(internalProgressChanges.size()>0)
						{
							progressChanges=new ArrayList<PropertyChangeEvent>(internalProgressChanges);
							internalProgressChanges.clear();
						}
					}
					if(done!=null || progressChanges!=null)
					{
						SwingUtilities.invokeLater(new ResultListenerFireRunnable(done, progressChanges));
					}
					Thread.sleep(POLL_INTERVAL);
				}
				catch (InterruptedException e)
				{
					if (logger.isInfoEnabled()) logger.info("Interrupted...", e);
					break;
				}
			}
		}
	}

	public void addResultListener(ResultListener listener)
	{
		synchronized (resultListeners)
		{
			resultListeners.add(listener);
		}
	}

	public void removeResultListener(ResultListener listener)
	{
		synchronized (resultListeners)
		{
			resultListeners.remove(listener);
		}
	}

	private class ResultListenerFireRunnable
			implements Runnable
	{
		private final Logger logger = LoggerFactory.getLogger(SwingWorkManager.class);

		private List<Future<V>> done;
		private List<ResultListener> clonedListeners;
		private List<PropertyChangeEvent> progressChanges;

		public ResultListenerFireRunnable(List<Future<V>> done, List<PropertyChangeEvent> progressChanges)
		{
			this.done = done;
			this.progressChanges = progressChanges;
		}

		public void run()
		{
			synchronized(resultListeners)
			{
				this.clonedListeners = new ArrayList<ResultListener>(resultListeners);
			}

			// fire changes of progress before any other events
			if(progressChanges!=null)
			{
				for(PropertyChangeEvent current:progressChanges)
				{
					Object source=current.getSource();
					Object newValue=current.getNewValue();
					if(source instanceof ProgressingCallable
							&& newValue instanceof Integer
							&& ProgressingCallable.PROGRESS_PROPERTY_NAME.equals(current.getPropertyName()) )
					{

						ProgressingCallable<V> progressingCallable=(ProgressingCallable<V>) source;
						int progress=(Integer)newValue;
						fireProgressEvent(progressingCallable, progress);
					}
					else
					{
						if(logger.isWarnEnabled())
							logger.warn("Somethings wrong with the propChangeEvent! source={}, newValue={}", source, newValue);
					}
				}
			}
			if(done!=null)
			{
				// remove done from mapping and remove propertyChangeListener...
				Map<Future<V>, Callable<V>> doneMapping=new HashMap<Future<V>, Callable<V>>();
				synchronized(futureCallableMapping)
				{
					for (Future<V> future : done)
					{
						Callable<V> callable=futureCallableMapping.remove(future);
						if(callable==null)
						{
							if(logger.isErrorEnabled()) logger.error("callable not found!");
						}
						else if(callable instanceof ProgressingCallable)
						{
							ProgressingCallable pc=(ProgressingCallable) callable;
							pc.removePropertyChangeListener(progressChangeListener);
							if(logger.isDebugEnabled()) logger.debug("Removed progress change listener from callable.");
						}
						doneMapping.put(future, callable);
					}
				}

				// outside synchronized!
				// fire remaining events...
				for(Map.Entry<Future<V>, Callable<V>> current:doneMapping.entrySet())
				{
					Future<V> future=current.getKey();
					Callable<V> callable=current.getValue();
					if (future.isCancelled())
					{
						fireCanceledEvent(callable);
					}
					else
					{
						// at this point we are sure that the future finished either
						// sucessfully or with an error.
						try
						{
							fireFinishedEvent(callable, future.get());
						}
						catch (InterruptedException e)
						{
							if (logger.isInfoEnabled()) logger.info("Interrupted...", e);
						}
						catch (ExecutionException e)
						{
							fireExceptionEvent(callable, e);
						}
					}
				}
			}
		}

		private void fireProgressEvent(ProgressingCallable<V> callable, int progress)
		{
			for (ResultListener<V> listener : clonedListeners)
			{
				listener.progressUpdated(callable, progress);
			}
		}

		private void fireExceptionEvent(Callable<V> callable, ExecutionException exception)
		{
			for (ResultListener<V> listener : clonedListeners)
			{
				listener.executionFailed(callable, exception);
			}
		}

		private void fireFinishedEvent(Callable<V> callable, V result)
		{
			for (ResultListener<V> listener : clonedListeners)
			{
				listener.executionFinished(callable, result);
			}
		}

		private void fireCanceledEvent(Callable<V> callable)
		{
			for (ResultListener<V> listener : clonedListeners)
			{
				listener.executionCanceled(callable);
			}
		}
	}

	private class ProgressChangeListener
		implements PropertyChangeListener
	{

		public void propertyChange(PropertyChangeEvent evt)
		{
			if(ProgressingCallable.PROGRESS_PROPERTY_NAME.equals(evt.getPropertyName()))
			{
				synchronized(internalProgressChanges)
				{
					internalProgressChanges.add(evt);
				}
			}
		}
	}
}
