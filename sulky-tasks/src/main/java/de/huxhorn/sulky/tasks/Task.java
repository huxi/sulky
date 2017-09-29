/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2017 Joern Huxhorn
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
 * Copyright 2007-2017 Joern Huxhorn
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
@SuppressWarnings("PMD.ShortClassName")
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
