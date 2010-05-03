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

package de.huxhorn.sulky.tasks;

import java.util.concurrent.ExecutionException;

/**
 * Registered TaskListeners are called by a TaskManager whenever the state of one of its Tasks changes.
 * <p/>
 * The calls are guaranteed to be executed on the event dispatch thread if usingEventQueue of the TaskManager is true.
 * Otherwise, they are executed on an internal thread of the TaskManager.
 *
 * @param <T> the type of the result.
 * @see de.huxhorn.sulky.tasks.TaskManager#setUsingEventQueue(boolean)
 * @see de.huxhorn.sulky.tasks.TaskManager#addTaskListener(TaskListener)
 * @see de.huxhorn.sulky.tasks.TaskManager#removeTaskListener(TaskListener)
 */
public interface TaskListener<T>
{
	/**
	 * This method is called after a new Task has been created.
	 *
	 * @param task the new Task.
	 */
	void taskCreated(Task<T> task);

	/**
	 * This method is called if the execution of the Task fails with an exception.
	 *
	 * @param task      the Task that failed.
	 * @param exception contains the actual Exception that caused the failure.
	 */
	void executionFailed(Task<T> task, ExecutionException exception);

	/**
	 * This method is called after a tasks finishes.
	 *
	 * @param task   the Task that finished.
	 * @param result the result the Task computed.
	 */
	void executionFinished(Task<T> task, T result);

	/**
	 * This method is called if a Task was canceled.
	 *
	 * @param task the Task that was canceled.
	 */
	void executionCanceled(Task<T> task);

	/**
	 * This method is called if the progress of the Task changed.
	 * It will only be called if the contained Callable is actually a proper ProgressingCallable.
	 *
	 * @param task     the Task that has a new progress value
	 * @param progress the new progress value of the Task.
	 */
	void progressUpdated(Task<T> task, int progress);
}
