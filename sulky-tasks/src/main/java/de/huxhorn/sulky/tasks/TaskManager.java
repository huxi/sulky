/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2009 Joern Huxhorn
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
package de.huxhorn.sulky.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.*;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class TaskManager<V>
{
	// TODO: Use java.util.concurrent.locks.ReentrantReadWriteLock instead of synchronized.
	/**
	 * The states a TaskManager can be in. A TaskManager can't be restarted.
	 */
	public enum State
	{
		INITIALIZED,
		RUNNING,
		STOPPED
	}

	private final Logger logger = LoggerFactory.getLogger(TaskManager.class);

	private boolean usingEventQueue;
	private final ExecutorService executorService;

	/**
	 * synchronized(tasks)
	 */
	private final Map<Integer, Task<V>> tasks;

	/**
	 * synchronized(tasks)
	 */
	private final Map<Integer, Task<V>> callableTasks;

	/**
	 * synchronized(tasks)
	 */
	private final List<ProgressChange<V>> internalProgressChanges;

	/**
	 * synchronized(tasks)
	 */
	private int nextTaskId;

	private final List<TaskListener<V>> taskListeners;

	private final PropertyChangeListener progressChangeListener;
	private Thread resultPollerThread;
	private State state;

	/**
	 * Creates a new task manager with a cached thread pool.
	 * <p/>
	 * By default, it is not using the event dispatch thread to fire task events.
	 */
	public TaskManager()
	{
		this(Executors.newCachedThreadPool(), false);
	}


	/**
	 * Creates a new task manager with the given executor service.
	 * <p/>
	 * By default, it is not using the event dispatch thread to fire task events.
	 *
	 * @param executorService the executor service to be used by this task manager.
	 */
	public TaskManager(ExecutorService executorService)
	{
		this(executorService, false);
	}

	/**
	 * Creates a new task manager with the given executor service.
	 *
	 * @param executorService the executor service to be used by this task manager.
	 * @param usingEventQueue whether or not the event dispatch thread should be used to fire task events.
	 */
	public TaskManager(ExecutorService executorService, boolean usingEventQueue)
	{
		this.nextTaskId = 1;
		this.usingEventQueue = usingEventQueue;
		this.tasks = new HashMap<Integer, Task<V>>();
		this.callableTasks = new HashMap<Integer, Task<V>>();
		this.progressChangeListener = new ProgressChangeListener();
		this.internalProgressChanges = new ArrayList<ProgressChange<V>>();
		this.taskListeners = new LinkedList<TaskListener<V>>();
		this.executorService = executorService;
		this.state = State.INITIALIZED;
	}

	/**
	 * Starts up this task manager.
	 *
	 * @throws IllegalStateException if the task manager was not INITIALIZED.
	 */
	public void startUp()
	{
		if(state == State.INITIALIZED)
		{
			resultPollerThread = new Thread(new TaskResultPoller(), "TaskResultPoller");
			resultPollerThread.setDaemon(true);
			resultPollerThread.start();
			state = State.RUNNING;
		}
		else
		{
			throw new IllegalStateException("You tried to start a task manager but it's state was " + state + " instead of INITIALIZED!");
		}
	}

	/**
	 * Shuts down this task manager including the used executor service.
	 * This call is simply ignored if the task manager was not running.
	 */
	public void shutDown()
	{
		if(state == State.RUNNING)
		{
			executorService.shutdownNow();
			resultPollerThread.interrupt();
			state = State.STOPPED;
		}
	}

	/**
	 * @return the state this task manager is in.
	 */
	public State getState()
	{
		return state;
	}

	/**
	 * Starts a task.
	 *
	 * @param callable the callable that will be used to create a task.
	 * @param name     name of the task, need not be unique.
	 * @return the started Task.
	 * @throws IllegalStateException    if the task manager is not running.
	 * @throws IllegalArgumentException if the name is null or the Callable was already started.
	 * @see de.huxhorn.sulky.tasks.Task
	 * @see de.huxhorn.sulky.tasks.ProgressingCallable
	 */
	public Task<V> startTask(Callable<V> callable, String name)
	{
		return startTask(callable, name, null, null);
	}

	/**
	 * Starts a task.
	 *
	 * @param callable    the callable that will be used to create a task.
	 * @param name        name of the task, need not be unique.
	 * @param description optional human-readable descriiption of what this task is about.
	 * @return the started Task.
	 * @throws IllegalStateException    if the task manager is not running.
	 * @throws IllegalArgumentException if the name is null or the Callable was already started.
	 * @see de.huxhorn.sulky.tasks.Task
	 * @see de.huxhorn.sulky.tasks.ProgressingCallable
	 */
	public Task<V> startTask(Callable<V> callable, String name, String description)
	{
		return startTask(callable, name, description, null);
	}

	/**
	 * Starts a task.
	 *
	 * @param callable    the callable that will be used to create a task.
	 * @param name        name of the task, need not be unique.
	 * @param description optional human-readable descriiption of what this task is about.
	 * @param metaData    optional meta data to be associated with this task.
	 * @return the started Task.
	 * @throws IllegalStateException    if the task manager is not running.
	 * @throws IllegalArgumentException if the name is null or the Callable was already started.
	 * @see de.huxhorn.sulky.tasks.Task
	 * @see de.huxhorn.sulky.tasks.ProgressingCallable
	 */
	public Task<V> startTask(Callable<V> callable, String name, String description, Map<String, String> metaData)
	{
		if(state != State.RUNNING)
		{
			throw new IllegalStateException("You tried to start a task but the task managers state was " + state + " instead of RUNNING!");
		}
		if(name == null)
		{
			throw new IllegalArgumentException("name must not be null!");
		}
		if(callable instanceof ProgressingCallable)
		{
			ProgressingCallable pcallable = (ProgressingCallable) callable;
			pcallable.addPropertyChangeListener(progressChangeListener);
			if(logger.isDebugEnabled()) logger.debug("Added progress change listener to callable.");
		}

		int callableIdentity = System.identityHashCode(callable);

		Future<V> future = executorService.submit(callable);
		Task<V> task;
		synchronized(tasks)
		{
			if(callableTasks.containsKey(callableIdentity))
			{
				throw new IllegalArgumentException("Callable is already scheduled!");
			}
			int newId = nextTaskId++;
			task = new TaskImpl<V>(newId, this, future, callable, name, description, metaData);
			tasks.put(newId, task);
			callableTasks.put(callableIdentity, task);
		}
		return task;
	}

	public Task<V> getTaskById(int taskId)
	{
		synchronized(tasks)
		{
			return tasks.get(taskId);
		}
	}

	public Task<V> getTaskByCallable(Callable<V> callable)
	{
		int callableIdentity = System.identityHashCode(callable);
		synchronized(tasks)
		{
			return callableTasks.get(callableIdentity);
		}
	}

	public Map<Integer, Task<V>> getTasks()
	{
		Map<Integer, Task<V>> result;

		synchronized(tasks)
		{
			result = new HashMap<Integer, Task<V>>(tasks);
		}
		return result;
	}

	public boolean isUsingEventQueue()
	{
		return usingEventQueue;
	}

	public void setUsingEventQueue(boolean usingEventQueue)
	{
		this.usingEventQueue = usingEventQueue;
	}

	private static class ProgressChange<V>
	{
		private final int progress;
		private final Task<V> task;

		public ProgressChange(Task<V> task, int progress)
		{
			this.progress = progress;
			this.task = task;
		}

		public int getProgress()
		{
			return progress;
		}

		public Task<V> getTask()
		{
			return task;
		}
	}

	private class TaskResultPoller
		implements Runnable
	{
		private final Logger logger = LoggerFactory.getLogger(TaskResultPoller.class);

		private static final long POLL_INTERVAL = 200;

		public void run()
		{
			for(; ;)
			{
				try
				{
					List<ProgressChange<V>> progressChanges = null;
					List<Task<V>> doneTasks = null;
					synchronized(tasks)
					{
						if(internalProgressChanges.size() > 0)
						{
							progressChanges = new ArrayList<ProgressChange<V>>(internalProgressChanges);
							internalProgressChanges.clear();
						}
						for(Map.Entry<Integer, Task<V>> entry : tasks.entrySet())
						{
							Task<V> task = entry.getValue();
							if(task.getFuture().isDone())
							{
								if(doneTasks == null)
								{
									doneTasks = new ArrayList<Task<V>>();
								}
								doneTasks.add(task);
							}
						}
						if(doneTasks != null)
						{
							for(Task task : doneTasks)
							{
								int callableIdentity = System.identityHashCode(task.getCallable());
								tasks.remove(task.getId());
								callableTasks.remove(callableIdentity);
							}
						}
					} // synchronized (tasks)


					if(doneTasks != null || progressChanges != null)
					{
						ResultListenerFireRunnable runnable = new ResultListenerFireRunnable(doneTasks, progressChanges);
						if(usingEventQueue)
						{
							EventQueue.invokeLater(runnable);
						}
						else
						{
							runnable.run();
						}
					}
					Thread.sleep(POLL_INTERVAL);
				}
				catch(InterruptedException e)
				{
					if(logger.isInfoEnabled()) logger.info("Interrupted...", e);
					break;
				}
			}
		}
	}

	public void addTaskListener(TaskListener<V> listener)
	{
		synchronized(taskListeners)
		{
			taskListeners.add(listener);
		}
	}

	public void removeTaskListener(TaskListener<V> listener)
	{
		synchronized(taskListeners)
		{
			taskListeners.remove(listener);
		}
	}

	private class ResultListenerFireRunnable
		implements Runnable
	{
		private final Logger logger = LoggerFactory.getLogger(TaskManager.class);

		private List<Task<V>> done;
		private List<TaskListener<V>> clonedListeners;
		private List<ProgressChange<V>> progressChanges;

		public ResultListenerFireRunnable(List<Task<V>> done, List<ProgressChange<V>> progressChanges)
		{
			this.done = done;
			this.progressChanges = progressChanges;
		}

		public void run()
		{
			synchronized(taskListeners)
			{
				this.clonedListeners = new ArrayList<TaskListener<V>>(taskListeners);
			}

			// fire changes of progress before any other events
			if(progressChanges != null)
			{
				for(ProgressChange<V> current : progressChanges)
				{
					fireProgressEvent(current.getTask(), current.getProgress());
				}
			}
			if(done != null)
			{
				for(Task<V> task : done)
				{
					Callable<V> callable = task.getCallable();
					if(callable instanceof ProgressingCallable)
					{
						// remove propertyChangeListener...
						ProgressingCallable pc = (ProgressingCallable) callable;
						pc.removePropertyChangeListener(progressChangeListener);
						if(logger.isDebugEnabled()) logger.debug("Removed progress change listener from callable.");
					}
					Future<V> future = task.getFuture();
					if(future.isCancelled())
					{
						fireCanceledEvent(task);
					}
					else
					{
						// at this point we are sure that the future finished either
						// sucessfully or with an error.
						try
						{
							fireFinishedEvent(task, future.get());
						}
						catch(InterruptedException e)
						{
							if(logger.isInfoEnabled()) logger.info("Interrupted...", e);
						}
						catch(ExecutionException e)
						{
							fireExceptionEvent(task, e);
						}
					}
				}

			}
		}

		private void fireProgressEvent(Task<V> task, int progress)
		{
			for(TaskListener<V> listener : clonedListeners)
			{
				try
				{
					listener.progressUpdated(task, progress);
				}
				catch(Throwable t)
				{
					if(logger.isErrorEnabled())
					{
						logger
							.error("TaskListener " + listener + " threw an exception while progressUpdated was called!", t);
					}
				}
			}
		}

		private void fireExceptionEvent(Task<V> task, ExecutionException exception)
		{
			for(TaskListener<V> listener : clonedListeners)
			{
				try
				{
					listener.executionFailed(task, exception);
				}
				catch(Throwable t)
				{
					if(logger.isErrorEnabled())
					{
						logger
							.error("TaskListener " + listener + " threw an exception while executionFailed was called!", t);
					}
				}
			}
		}

		private void fireFinishedEvent(Task<V> task, V result)
		{
			for(TaskListener<V> listener : clonedListeners)
			{
				try
				{
					listener.executionFinished(task, result);
				}
				catch(Throwable t)
				{
					if(logger.isErrorEnabled())
					{
						logger
							.error("TaskListener " + listener + " threw an exception while executionFinished was called!", t);
					}
				}
			}
		}

		private void fireCanceledEvent(Task<V> task)
		{
			for(TaskListener<V> listener : clonedListeners)
			{
				try
				{
					listener.executionCanceled(task);
				}
				catch(Throwable t)
				{
					if(logger.isErrorEnabled())
					{
						logger
							.error("TaskListener " + listener + " threw an exception while executionCanceled was called!", t);
					}
				}
			}
		}
	}

	private class ProgressChangeListener
		implements PropertyChangeListener
	{

		public void propertyChange(PropertyChangeEvent evt)
		{
			Object source = evt.getSource();
			Object newValue = evt.getNewValue();
			if(source instanceof ProgressingCallable
				&& newValue instanceof Integer
				&& ProgressingCallable.PROGRESS_PROPERTY_NAME.equals(evt.getPropertyName()))
			{
				int progress = (Integer) newValue;
				int callableIdentity = System.identityHashCode(source);

				synchronized(tasks)
				{
					Task<V> task = callableTasks.get(callableIdentity);
					if(task != null)
					{
						internalProgressChanges.add(new ProgressChange<V>(task, progress));
					}
				}
			}
		}
	}

	private final static class TaskImpl<V>
		implements Task<V>
	{
		private final int id;
		private final TaskManager<V> taskManager;
		private final Future<V> future;
		private final Callable<V> callable;
		private final String name;
		private final String description;
		private final Map<String, String> metaData;

		public TaskImpl(int id, TaskManager<V> taskManager, Future<V> future, Callable<V> callable, String name, String description, Map<String, String> metaData)
		{
			this.id = id;
			this.taskManager = taskManager;
			this.callable = callable;
			this.future = future;
			this.name = name;
			this.description = description;
			if(metaData != null)
			{
				this.metaData = new HashMap<String, String>(metaData);
			}
			else
			{
				this.metaData = null;
			}
		}

		public int getId()
		{
			return id;
		}

		public String getName()
		{
			return name;
		}

		public String getDescription()
		{
			return description;
		}

		public Map<String, String> getMetaData()
		{
			if(metaData == null)
			{
				return null;
			}
			return Collections.unmodifiableMap(metaData);
		}

		public Future<V> getFuture()
		{
			return future;
		}

		public Callable<V> getCallable()
		{
			return callable;
		}

		public TaskManager<V> getTaskManager()
		{
			return taskManager;
		}

		@Override
		public boolean equals(Object o)
		{
			if(this == o) return true;
			if(!(o instanceof TaskImpl)) return false;

			TaskImpl<?> task = (TaskImpl<?>) o;

			if(id != task.id) return false;
			if(taskManager != task.getTaskManager()) return false;

			return true;
		}

		@Override
		public int hashCode()
		{
			return id;
		}
	}
}
