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
package de.huxhorn.sulky.tasks;

import java.beans.PropertyChangeListener;
import java.util.concurrent.Callable;

/**
 * This is the interface definition of a Callable that can provide information about it's progress.
 * User-code should never need to implement this interface.
 * A proper default implementation is available with the AbstractProgressingCallable.
 *
 * @param <T> the type of the result.
 * @see java.util.concurrent.Callable
 * @see de.huxhorn.sulky.tasks.AbstractProgressingCallable
 */
public interface ProgressingCallable<T>
	extends Callable<T>
{
	String PROGRESS_PROPERTY_NAME = "progress";

	/**
	 * Should return values between 0 and 100, or a negative value indicating an unknown progress.
	 * <p/>
	 * Implementations must fire a PropertyChangeEvent using PROGRESS_PROPERTY_NAME as the property name.
	 * <p/>
	 * This event does not have to be fired on the event dispatch thread because TaskManager will take care about that
	 * if required/requested.
	 *
	 * @return values between 0 and 100, or a negative value to indicate an unknown progress.
	 */
	int getProgress();

	/**
	 * User-code should not use this method but use add/removeTaskListener instead.
	 * <p/>
	 * Adds the given PropertyChangeListener.
	 *
	 * @param listener the PropertyChangeListener
	 * @see de.huxhorn.sulky.tasks.TaskManager#addTaskListener(TaskListener)
	 * @see de.huxhorn.sulky.tasks.TaskManager#removeTaskListener(TaskListener)
	 */
	void addPropertyChangeListener(PropertyChangeListener listener);

	/**
	 * User-code should not use this method but use add/removeTaskListener instead.
	 * <p/>
	 * Removes the given PropertyChangeListener.
	 *
	 * @param listener the PropertyChangeListener
	 * @see de.huxhorn.sulky.tasks.TaskManager#addTaskListener(TaskListener)
	 * @see de.huxhorn.sulky.tasks.TaskManager#removeTaskListener(TaskListener)
	 */
	void removePropertyChangeListener(PropertyChangeListener listener);
}
