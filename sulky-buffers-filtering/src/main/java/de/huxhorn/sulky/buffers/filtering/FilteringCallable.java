/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2018 Joern Huxhorn
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
 * Copyright 2007-2018 Joern Huxhorn
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

package de.huxhorn.sulky.buffers.filtering;

import de.huxhorn.sulky.buffers.Buffer;
import de.huxhorn.sulky.conditions.Condition;
import de.huxhorn.sulky.tasks.AbstractProgressingCallable;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FilteringCallable<E>
	extends AbstractProgressingCallable<Long>
{
	private final Logger logger = LoggerFactory.getLogger(FilteringCallable.class);

	private final FilteringBuffer<E> filteringBuffer;
	private final int filterDelay;
	private long lastFilteredElement = -1;

	public FilteringCallable(FilteringBuffer<E> filteringBuffer, int filterDelay)
	{
		this.filteringBuffer = Objects.requireNonNull(filteringBuffer, "filteringBuffer must not be null!");
		this.filterDelay = filterDelay;
	}

	@Override
	public Long call()
		throws Exception
	{
		for(;;)
		{
			Buffer<E> sourceBuffer = filteringBuffer.getSourceBuffer();
			Condition condition = filteringBuffer.getCondition();
			boolean disposed = filteringBuffer.isDisposed();
			if(disposed)
			{
				break;
			}
			long currentSize = sourceBuffer.getSize();
			long filterStartIndex = lastFilteredElement + 1;
			if(filterStartIndex > currentSize)
			{
				filterStartIndex = 0;
				lastFilteredElement = -1;
				filteringBuffer.clearFilteredIndices();
			}

			setNumberOfSteps(currentSize);
			setCurrentStep(filterStartIndex);

			if(currentSize != filterStartIndex)
			{
				for(long i = filterStartIndex; i < currentSize; i++)
				{
					disposed = filteringBuffer.isDisposed();
					if(disposed)
					{
						break;
					}
					E current = sourceBuffer.get(i);
					if(current != null && condition.isTrue(current))
					{
						filteringBuffer.addFilteredIndex(i);
						if(logger.isDebugEnabled()) logger.debug("Added index: {}", i);
					}
					setCurrentStep(i);
					lastFilteredElement = i;
				}
			}
			try
			{
				Thread.sleep(filterDelay);
			}
			catch(InterruptedException e)
			{
				if(logger.isDebugEnabled()) logger.debug("Interrupted...", e);
				return lastFilteredElement;
			}
		}
		if(logger.isDebugEnabled()) logger.debug("Callable finished.");
		return lastFilteredElement;
	}
}
