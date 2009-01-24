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
package de.huxhorn.sulky.swing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;

/**
 * The registered PropertyChangeLsiteners are called from the calculating thread
 * not from the EventDispatchThread. This is perfectly ok since SwingWorkManager
 * can (and will) transform those changes into ResultListener calls that are guaranteed
 * to be executed by the EventDispatchThread.
 * <p/>
 * Extending classes should normally only call setNumberOfSteps and setCurrentStep whenever
 * necessary.
 * <p/>
 * The constructors with initialSleepSteps and laterSleepSteps arguments are recommended for longer
 * operations. setCurrentStep will sleep for 1ms every time initialSleepSteps number of steps have been
 * processed. If laterSleepSteps is defined this value is used if more than 5*initialSleepSteps steps have
 * been prcessed.
 * It can be usefull to use a smaller initialSleepSteps to support faster cancelation at the start of an
 * operation, e.g. in case of an accidental start by the user, while using a larger laterSleepSteps amount
 * after an initial warm-up-period for performance reason (switching threads is expensive).
 * <p/>
 * The sleep itself is necessary to allow cancelation from the executor. The InterruptedException should not
 * be caught by the caller. If it is caught, e.g. to perform some cleanup,  care should be taken to leave
 * the call-method at the earliest possible time.
 */
public abstract class AbstractProgressingCallable<T>
	implements ProgressingCallable<T>
{
	private final Logger logger = LoggerFactory.getLogger(AbstractProgressingCallable.class);

	private PropertyChangeSupport changeSupport;
	private int progress;
	private long numberOfSteps;
	private long currentStep;
	private int initialSleepSteps;
	private int laterSleepSteps;
	private long lastSleepStep;

	public AbstractProgressingCallable()
	{
		this(0, 0);
	}

	public AbstractProgressingCallable(int sleepSteps)
	{
		this(sleepSteps, 0);
	}

	public AbstractProgressingCallable(int initialSleepSteps, int laterSleepSteps)
	{
		this.changeSupport = new PropertyChangeSupport(this);
		this.progress = 0;
		this.numberOfSteps = 1;
		this.initialSleepSteps = initialSleepSteps;
		this.laterSleepSteps = laterSleepSteps;
		this.lastSleepStep = 0;
	}

	protected void setNumberOfSteps(long numberOfSteps)
	{
		if(numberOfSteps < 1)
		{
			throw new IllegalArgumentException("numberOfSteps (" + numberOfSteps + ") must be positive!");
		}
		this.numberOfSteps = numberOfSteps;
	}

	protected void setCurrentStep(long currentStep)
		throws InterruptedException
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

	private void calculateProgress()
	{
		int newProgress = (int) (((double) currentStep / (double) numberOfSteps) * 100);
		if(logger.isDebugEnabled()) logger.debug("New progress: {}", newProgress);
		setProgress(newProgress);
	}

	protected void setProgress(int progress)
	{
		if(this.progress != progress)
		{
			Object oldValue = this.progress;
			this.progress = progress;
			Object newValue = this.progress;
			changeSupport.firePropertyChange(PROGRESS_PROPERTY_NAME, oldValue, newValue);
		}

	}

	public int getProgress()
	{
		return progress;
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
