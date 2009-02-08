package de.huxhorn.sulky.tasks;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

public interface Task<V>
{
	int getId();

	TaskManager<V> getTaskManager();

	String getName();

	String getDescription();

	Map<String, String> getMetaData();

	Future<V> getFuture();

	Callable<V> getCallable();
}
