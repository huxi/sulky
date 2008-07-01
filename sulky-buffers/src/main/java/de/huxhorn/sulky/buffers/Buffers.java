/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2008 Joern Huxhorn
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
package de.huxhorn.sulky.buffers;

import de.huxhorn.sulky.conditions.Condition;

import java.util.Collection;

public class Buffers
{
	private Buffers() {}

	public static <E> void filter(Buffer<E> buffer, Condition condition, AppendOperation<E> toAppendTo)
	{
		for(E element:buffer)
		{
			if(condition.isTrue(element))
			{
				toAppendTo.add(element);
			}
		}
	}

	public static <E> void filter(Buffer<E> buffer, FilterJob<E> filterJob)
	{
		filter(buffer, filterJob.getCondition(), filterJob.getAppendOperation());
	}

	public static <E> void filter(Buffer<E> buffer, Collection<FilterJob<E>> filterJobs)
	{
		for(E element:buffer)
		{
			for(FilterJob<E> job:filterJobs)
			{
				if(job.getCondition().isTrue(element))
				{
					job.getAppendOperation().add(element);
				}
			}
		}
	}

	/**
	 * Executes buffer.dispose() if Buffer implements DisposeOperation.
	 *
	 * @param buffer
	 */
	public static void dispose(Buffer<?> buffer)
	{
		if(buffer instanceof DisposeOperation)
		{
			DisposeOperation op=(DisposeOperation) buffer;
			op.dispose();
		}
	}

	/**
	 * Executes buffer.reset() if Buffer implements ResetOperation.
	 *
	 * @param buffer
	 */
	public static void reset(Buffer<?> buffer)
	{
		if(buffer instanceof ResetOperation)
		{
			ResetOperation op=(ResetOperation) buffer;
			op.reset();
		}
	}
}
