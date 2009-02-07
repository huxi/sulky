package de.huxhorn.sulky.tasks;

import java.util.concurrent.Future;
import java.util.concurrent.Callable;

public interface Task<V>
{
	int getId();

	TaskManager<V> getTaskManager();

	String getName();
	
	String getDescription();

	Future<V> getFuture();

	Callable<V> getCallable();
}
