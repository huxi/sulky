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

package de.huxhorn.sulky.tasks;

import de.huxhorn.sulky.io.IOUtilities;
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
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>A TaskManager is used to create Tasks for given Callables.</p>
 *
 * @param <T> the type of the result.
 */
//@ThreadSafe
public class TaskManager<T>
{
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

	private final ReentrantReadWriteLock tasksLock;
	private final ReentrantReadWriteLock taskListenersLock;
	private boolean usingEventQueue;
	private final ExecutorService executorService;

	//@GuardedBy("tasksLock")
	private final List<Task<T>> internalCreatedTasks;

	//@GuardedBy("tasksLock")
	private final Map<Long, Task<T>> tasks;

	//@GuardedBy("tasksLock")
	private final Map<Integer, Task<T>> callableTasks;

	//@GuardedBy("tasksLock")
	private final List<ProgressChange<T>> internalProgressChanges;

	//@GuardedBy("tasksLock")
	private long nextTaskId;

	//@GuardedBy("taskListenersLock")
	private final List<TaskListener<T>> taskListeners;

	private final PropertyChangeListener progressChangeListener;
	private Thread resultPollerThread;
	private State state;

	/**
	 * Creates a new task manager with a cached thread pool.
	 *
	 * By default, it is not using the event dispatch thread to fire task events.
	 */
	public TaskManager()
	{
		this(Executors.newCachedThreadPool(), false);
	}


	/**
	 * Creates a new task manager with the given executor service.
	 *
	 * By default, it is not using the event dispatch thread to fire task events.
	 *
	 * @param executorService the executor service to be used by this task manager. Must not be null.
	 * @throws IllegalArgumentException if executorService is null.
	 */
	public TaskManager(ExecutorService executorService)
	{
		this(executorService, false);
	}

	/**
	 * Creates a new task manager with the given executor service and the given us.
	 *
	 * @param executorService the executor service to be used by this task manager. Must not be null.
	 * @param usingEventQueue whether or not the event dispatch thread should be used to fire task events.
	 * @throws IllegalArgumentException if executorService is null.
	 */
	public TaskManager(ExecutorService executorService, boolean usingEventQueue)
	{
		if(executorService == null)
		{
			throw new IllegalArgumentException("executorService must not be null!");
		}
		this.tasksLock = new ReentrantReadWriteLock(true);
		this.taskListenersLock = new ReentrantReadWriteLock(true);
		this.nextTaskId = 1;
		this.usingEventQueue = usingEventQueue;
		this.internalCreatedTasks = new ArrayList<>();
		this.tasks = new HashMap<>();
		this.callableTasks = new HashMap<>();
		this.progressChangeListener = new ProgressChangeListener();
		this.internalProgressChanges = new ArrayList<>();
		this.taskListeners = new LinkedList<>();
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
	 * No TaskListener calls will be executed after this method was executed.
	 */
	public void shutDown()
	{
		if(state == State.RUNNING)
		{
			state = State.STOPPED;
			resultPollerThread.interrupt();
			executorService.shutdownNow();
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
	public Task<T> startTask(Callable<T> callable, String name)
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
	public Task<T> startTask(Callable<T> callable, String name, String description)
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
	public Task<T> startTask(Callable<T> callable, String name, String description, Map<String, String> metaData)
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

		Future<T> future = executorService.submit(callable);
		Task<T> task;
		ReentrantReadWriteLock.WriteLock lock = tasksLock.writeLock();
		lock.lock();
		try
		{
			if(callableTasks.containsKey(callableIdentity))
			{
				throw new IllegalArgumentException("Callable is already scheduled!");
			}
			long newId = nextTaskId++;
			task = new TaskImpl<>(newId, this, future, callable, name, description, metaData);
			internalCreatedTasks.add(task);
			tasks.put(newId, task);
			callableTasks.put(callableIdentity, task);
		}
		finally
		{
			lock.unlock();
		}
		return task;
	}

	public int getNumberOfTasks()
	{
		ReentrantReadWriteLock.ReadLock lock = tasksLock.readLock();
		lock.lock();
		try
		{
			return tasks.size();
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Returns the Task associated with the Task ID.
	 *
	 * @param taskId the Task ID for which the Task should be resolved.
	 * @return the Task associated with the Task ID.
	 */
	public Task<T> getTaskById(long taskId)
	{
		ReentrantReadWriteLock.ReadLock lock = tasksLock.readLock();
		lock.lock();
		try
		{
			return tasks.get(taskId);
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Returns the Task associated with the given Callable.
	 *
	 * @param callable the Callable for which the Task should be resolved.
	 * @return the Task associated with the given Callable.
	 */
	public Task<T> getTaskByCallable(Callable<T> callable)
	{
		int callableIdentity = System.identityHashCode(callable);
		ReentrantReadWriteLock.ReadLock lock = tasksLock.readLock();
		lock.lock();
		try
		{
			return callableTasks.get(callableIdentity);
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Returns a Map containing all Tasks with their Task ID as key.
	 *
	 * @return a Map containing all Tasks with their Task ID as key.
	 */
	public Map<Long, Task<T>> getTasks()
	{
		Map<Long, Task<T>> result;

		ReentrantReadWriteLock.ReadLock lock = tasksLock.readLock();
		lock.lock();
		try
		{
			result = new HashMap<>(tasks);
		}
		finally
		{
			lock.unlock();
		}
		return result;
	}

	/**
	 * Returns true if this TaskManager calls TaskListeners on the event dispatcher thread, false otherwise.
	 *
	 * @return true if this TaskManager calls TaskListeners on the event dispatcher thread, false otherwise.
	 */
	public boolean isUsingEventQueue()
	{
		return usingEventQueue;
	}

	/**
	 * Set whether or not this TaskManager calls TaskListeners on the event dispatcher thread.
	 *
	 * @param usingEventQueue whether or not this TaskManager calls TaskListeners on the event dispatcher thread.
	 */
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
					List<ProgressChange<T>> progressChanges = null;
					List<Task<T>> doneTasks = null;
					List<Task<T>> createdTasks = null;

					ReentrantReadWriteLock.WriteLock lock = tasksLock.writeLock();
					lock.lock();
					try
					{
						if(internalCreatedTasks.size() > 0)
						{
							createdTasks = new ArrayList<>(internalCreatedTasks);
							internalCreatedTasks.clear();
						}
						if(internalProgressChanges.size() > 0)
						{
							progressChanges = new ArrayList<>(internalProgressChanges);
							internalProgressChanges.clear();
						}
						for(Map.Entry<Long, Task<T>> entry : tasks.entrySet())
						{
							Task<T> task = entry.getValue();
							if(task.getFuture().isDone())
							{
								if(doneTasks == null)
								{
									doneTasks = new ArrayList<>();
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
					}
					finally
					{
						lock.unlock();
					}

					if(createdTasks != null || doneTasks != null || progressChanges != null)
					{
						ResultListenerFireRunnable runnable = new ResultListenerFireRunnable(createdTasks, doneTasks, progressChanges);
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
					if(logger.isDebugEnabled()) logger.debug("Interrupted...", e);
					IOUtilities.interruptIfNecessary(e);
					break;
				}
			}
		}
	}

	/**
	 * Adds the given TaskListener.
	 *
	 * @param listener the TaskListener to add.
	 */
	public void addTaskListener(TaskListener<T> listener)
	{
		ReentrantReadWriteLock.WriteLock lock = taskListenersLock.writeLock();
		lock.lock();
		try
		{
			taskListeners.add(listener);
		}
		finally
		{
			lock.unlock();
		}
	}

	/**
	 * Removes the given TaskListener.
	 *
	 * @param listener the TaskListener to remove.
	 */
	public void removeTaskListener(TaskListener<T> listener)
	{
		ReentrantReadWriteLock.WriteLock lock = taskListenersLock.writeLock();
		lock.lock();
		try
		{
			taskListeners.remove(listener);
		}
		finally
		{
			lock.unlock();
		}
	}

	private class ResultListenerFireRunnable
		implements Runnable
	{
		private final Logger logger = LoggerFactory.getLogger(TaskManager.class);

		private List<Task<T>> createdTasks;
		private List<Task<T>> done;
		private List<TaskListener<T>> clonedListeners;
		private List<ProgressChange<T>> progressChanges;

		public ResultListenerFireRunnable(List<Task<T>> createdTasks, List<Task<T>> done, List<ProgressChange<T>> progressChanges)
		{
			this.createdTasks = createdTasks;
			this.done = done;
			this.progressChanges = progressChanges;
		}

		public void run()
		{
			ReentrantReadWriteLock.ReadLock lock = taskListenersLock.readLock();
			lock.lock();
			try
			{
				this.clonedListeners = new ArrayList<>(taskListeners);
			}
			finally
			{
				lock.unlock();
			}

			// fire creation of tasks before any other events
			if(createdTasks != null)
			{
				for(Task<T> current : createdTasks)
				{
					fireCreatedEvent(current);
				}
			}
			// then fire changes of progress before finish, cancel or fail
			if(progressChanges != null)
			{
				for(ProgressChange<T> current : progressChanges)
				{
					fireProgressEvent(current.getTask(), current.getProgress());
				}
			}
			if(done != null)
			{
				for(Task<T> task : done)
				{
					Callable<T> callable = task.getCallable();
					if(callable instanceof ProgressingCallable)
					{
						// remove propertyChangeListener...
						ProgressingCallable pc = (ProgressingCallable) callable;
						pc.removePropertyChangeListener(progressChangeListener);
						if(logger.isDebugEnabled()) logger.debug("Removed progress change listener from callable.");
					}
					Future<T> future = task.getFuture();
					if(future.isCancelled())
					{
						fireCanceledEvent(task);
					}
					else
					{
						// at this point we are sure that the future finished either
						// successfully or with an error.
						try
						{
							fireFinishedEvent(task, future.get());
						}
						catch(InterruptedException e)
						{
							if(logger.isInfoEnabled()) logger.info("Interrupted...", e);
							IOUtilities.interruptIfNecessary(e);
						}
						catch(ExecutionException e)
						{
							fireExceptionEvent(task, e);
						}
					}
				}

			}
		}

		private void fireCreatedEvent(Task<T> task)
		{
			for(TaskListener<T> listener : clonedListeners)
			{
				try
				{
					listener.taskCreated(task);
				}
				catch(Throwable t)
				{
					if(logger.isErrorEnabled())
					{
						logger
							.error("TaskListener " + listener + " threw an exception while progressUpdated was called!", t);
					}
					IOUtilities.interruptIfNecessary(t);
				}
			}
		}

		private void fireProgressEvent(Task<T> task, int progress)
		{
			for(TaskListener<T> listener : clonedListeners)
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
					IOUtilities.interruptIfNecessary(t);
				}
			}
		}

		private void fireExceptionEvent(Task<T> task, ExecutionException exception)
		{
			for(TaskListener<T> listener : clonedListeners)
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
					IOUtilities.interruptIfNecessary(t);
				}
			}
		}

		private void fireFinishedEvent(Task<T> task, T result)
		{
			for(TaskListener<T> listener : clonedListeners)
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
					IOUtilities.interruptIfNecessary(t);
				}
			}
		}

		private void fireCanceledEvent(Task<T> task)
		{
			for(TaskListener<T> listener : clonedListeners)
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
					IOUtilities.interruptIfNecessary(t);
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

				ReentrantReadWriteLock.WriteLock lock = tasksLock.writeLock();
				// using write lock because internalProgressChanges is changed.
				lock.lock();
				try
				{
					Task<T> task = callableTasks.get(callableIdentity);
					if(task != null)
					{
						internalProgressChanges.add(new ProgressChange<>(task, progress));
					}
				}
				finally
				{
					lock.unlock();
				}
			}
		}
	}

	//@Immutable
	private static final class TaskImpl<V>
		implements Task<V>
	{
		private final long id;
		private final TaskManager<V> taskManager;
		private final Future<V> future;
		private final Callable<V> callable;
		private final String name;
		private final String description;
		private final Map<String, String> metaData;

		public TaskImpl(long id, TaskManager<V> taskManager, Future<V> future, Callable<V> callable, String name, String description, Map<String, String> metaData)
		{
			this.id = id;
			this.taskManager = taskManager;
			this.callable = callable;
			this.future = future;
			this.name = name;
			this.description = description;
			if(metaData != null)
			{
				this.metaData = new HashMap<>(metaData);
			}
			else
			{
				this.metaData = null;
			}
		}

		public long getId()
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

		public int getProgress()
		{
			if(callable instanceof ProgressingCallable)
			{
				ProgressingCallable pc = (ProgressingCallable) callable;
				return pc.getProgress();
			}
			return -1;
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
			return (int) id;
		}

		@Override
		public String toString()
		{
			StringBuilder result = new StringBuilder();
			result.append("Task[id=").append(id)
				.append(", name=\"").append(name).append("\"");
			result.append(", progress=");
			int progress = getProgress();
			if(progress < 0)
			{
				result.append("unknown");
			}
			else
			{
				result.append(progress);
			}
			result.append("]");

			return result.toString();
		}
	}
}
