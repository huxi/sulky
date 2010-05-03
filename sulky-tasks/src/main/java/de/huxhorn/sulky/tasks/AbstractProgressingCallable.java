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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * <p>This class is an abstract implementation of the ProgressingCallable interface.</p>
 * <p/>
 * <p>Extending classes only call setNumberOfSteps and setCurrentStep whenever
 * necessary. The progress is automatically calculated.</p>
 * <p/>
 * <p>The registered PropertyChangeListeners are called from the calculating thread
 * not from the event dispatch thread. This is perfectly ok since TaskManager
 * transforms those changes into ResultListener calls that are guaranteed
 * to be executed on the event dispatch thread if usingEventQueue is set to true.</p>
 * <p/>
 * <p>The constructors with initialSleepSteps and laterSleepSteps arguments are recommended for longer
 * operations. setCurrentStep will sleep for 1ms every time initialSleepSteps number of steps have been
 * processed. If laterSleepSteps is defined this value is used if more than 5*initialSleepSteps steps have
 * been processed.</p>
 * <p/>
 * <p>It can be usefull to use a smaller value for initialSleepSteps to support faster cancelation at the start of an
 * operation, e.g. in case of an accidental start by the user, while using a larger value for laterSleepSteps
 * after an initial warm-up-period for performance reason (switching threads is expensive).</p>
 * <p/>
 * <p>The sleep itself is necessary to allow cancelation from the executor. The InterruptedException should not
 * be caught by the caller. If it is caught, e.g. to perform some cleanup, care should be taken to leave
 * the call-method at the earliest possible time.</p>
 *
 * @param <T> the type of the result.
 * @see java.util.concurrent.Callable
 * @see de.huxhorn.sulky.tasks.ProgressingCallable
 */
public abstract class AbstractProgressingCallable<T>
	implements ProgressingCallable<T>
{
	private final Logger logger = LoggerFactory.getLogger(AbstractProgressingCallable.class);

	private PropertyChangeSupport changeSupport;
	private int progress;
	private long numberOfSteps;
	private long currentStep;
	private long initialSleepSteps;
	private long laterSleepSteps;
	private long lastSleepStep;
	private final ReentrantReadWriteLock rwLock;


	public AbstractProgressingCallable()
	{
		this(0, 0);
	}

	public AbstractProgressingCallable(long sleepSteps)
	{
		this(sleepSteps, 0);
	}

	public AbstractProgressingCallable(long initialSleepSteps, long laterSleepSteps)
	{
		rwLock = new ReentrantReadWriteLock(true);

		this.changeSupport = new PropertyChangeSupport(this);
		this.progress = -1;
		this.numberOfSteps = 0;
		this.initialSleepSteps = initialSleepSteps;
		this.laterSleepSteps = laterSleepSteps;
		this.lastSleepStep = 0;
	}

	/**
	 * Sets the number of steps required to complete the task. A number <= 0 means that the number of steps are not
	 * (yet) known and will result in a progress of -1.
	 *
	 * @param numberOfSteps the number of steps to complete the task.
	 */
	protected void setNumberOfSteps(long numberOfSteps)
	{
		if(this.numberOfSteps != numberOfSteps)
		{
			this.numberOfSteps = numberOfSteps;
			calculateProgress();
		}
	}

	protected void setCurrentStep(long currentStep)
		throws InterruptedException
	{
		if(this.currentStep != currentStep)
		{
			this.currentStep = currentStep;
			calculateProgress();
			if(currentStep != 0 && initialSleepSteps > 0)
			{
				if(laterSleepSteps > 0 && currentStep > initialSleepSteps * 5)
				{

					if(lastSleepStep + laterSleepSteps < currentStep)
					{
						lastSleepStep = currentStep;
						Thread.sleep(1);
					}
				}
				else if(lastSleepStep + initialSleepSteps < currentStep)
				{
					lastSleepStep = currentStep;
					Thread.sleep(1);
				}
			}
		}
	}

	private void calculateProgress()
	{
		int newProgress = -1;
		if(numberOfSteps > 0)
		{
			newProgress = (int) (((double) currentStep / (double) numberOfSteps) * 100);
		}
		setProgress(newProgress);
	}

	/**
	 * Fires the PropertyChangeEvent if required - as defined in the interface.
	 *
	 * @param progress the new progress.
	 */
	private void setProgress(int progress)
	{
		ReentrantReadWriteLock.WriteLock lock = rwLock.writeLock();
		lock.lock();
		try
		{
			if(this.progress != progress)
			{
				if(logger.isDebugEnabled()) logger.debug("New progress: {}", progress);
				Object oldValue = this.progress;
				this.progress = progress;
				Object newValue = this.progress;
				changeSupport.firePropertyChange(PROGRESS_PROPERTY_NAME, oldValue, newValue);
			}
		}
		finally
		{
			lock.unlock();
		}

	}

	/**
	 * Returns values between 0 and 100, or a negative value indicating an unknown progress.
	 *
	 * @return the progress of the operation.
	 */
	public int getProgress()
	{
		ReentrantReadWriteLock.ReadLock lock = rwLock.readLock();
		lock.lock();
		try
		{
			return progress;
		}
		finally
		{
			lock.unlock();
		}
	}

	public void addPropertyChangeListener(PropertyChangeListener listener)
	{
		changeSupport.addPropertyChangeListener(listener);

	}

	public void removePropertyChangeListener(PropertyChangeListener listener)
	{
		changeSupport.removePropertyChangeListener(listener);
	}
}
