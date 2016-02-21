package de.huxhorn.sulky.tasks;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * <p>A Task is created by a TaskManager and encapsulates both Callable and Future as well as additional information.</p>
 *
 * @param <T> the type of the result.
 * @see java.util.concurrent.Callable
 * @see java.util.concurrent.Future
 * @see de.huxhorn.sulky.tasks.TaskManager#startTask(java.util.concurrent.Callable, String, String, java.util.Map)
 */
public interface Task<T>
{
	/**
	 * Returns the ID of this task. Task IDs are unique in relation to the TaskManager that started the task.
	 *
	 * @return the ID of the task.
	 */
	long getId();

	/**
	 * Returns the TaskManager that started this task.
	 *
	 * @return the TaskManager that started this Task.
	 */
	TaskManager<T> getTaskManager();

	String getName();

	String getDescription();

	int getProgress();

	Map<String, String> getMetaData();

	Future<T> getFuture();

	Callable<T> getCallable();
}
