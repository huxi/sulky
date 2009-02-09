package de.huxhorn.sulky.tasks;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface Task<V>
{
	/**
	 * Returns the ID of this task. Task IDs are unique in relation to the TaskManager that started the task.
	 *
	 * @return the ID of the task.
	 */
	int getId();

	/**
	 * Returns the TaskManager that started this task.
	 *
	 * @return the TaskManager that started this Task.
	 */
	TaskManager<V> getTaskManager();

	String getName();

	String getDescription();

	Map<String, String> getMetaData();

	Future<V> getFuture();

	Callable<V> getCallable();
}
