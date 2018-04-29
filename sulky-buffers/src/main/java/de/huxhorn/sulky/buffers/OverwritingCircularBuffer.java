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

package de.huxhorn.sulky.buffers;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.RandomAccess;

/**
 * http://c2.com/cgi/wiki?CircularBuffer
 */
public final class OverwritingCircularBuffer<E>
	implements CircularBuffer<E>, RandomAccess, Cloneable, Serializable
{
	private static final long serialVersionUID = 3423268103026176567L;

	private final int bufferSize;
	private transient int startIndex;
	private transient int endIndex;
	private transient long overflowCounter;
	private transient long size;
	private transient boolean full;
	private transient Object[] array;

	public OverwritingCircularBuffer(int bufferSize)
	{
		if(bufferSize < 1)
		{
			throw new IllegalArgumentException("bufferSize (" + bufferSize + ") must be positive!");
		}
		this.bufferSize = bufferSize;

		array = new Object[bufferSize];
		reset();
	}

	@Override
	public void add(E element)
	{
		internalAdd(element);
	}

	private void internalAdd(Object element)
	{
		if(isFull())
		{
			removeFirst();
			overflowCounter++;
		}
		size++;
		array[endIndex] = element;
		endIndex++;
		if(endIndex == bufferSize)
		{
			endIndex = 0;
		}
		if(startIndex == endIndex)
		{
			full = true;
		}
	}

	@Override
	public void addAll(List<E> elements)
	{
		for(E element : elements)
		{
			add(element);
		}
	}

	@Override
	public void addAll(E[] elements)
	{
		for(E element : elements)
		{
			add(element);
		}
	}

	@Override
	public E get(long index)
	{
		if(index < 0 || index >= size)
		{
			throw new IndexOutOfBoundsException("Invalid index " + index + "! Must be 0.." + (size - 1) + ".");
		}
		int realIndex = (int) (index - overflowCounter);
		if(realIndex < 0)
		{
			return null;
		}
		return getRelative(realIndex);
	}

	@Override
	public E getRelative(int index)
	{
		long availableElements = getAvailableElements();
		if(index < 0 || index >= availableElements)
		{
			throw new IndexOutOfBoundsException("Invalid index " + index + "! Must be 0.." + (availableElements - 1) + ".");
		}
		int realIndex = (startIndex + index) % bufferSize;
		@SuppressWarnings({"unchecked"})
		E result = (E) array[realIndex];
		return result;
	}

	@Override
	public E setRelative(int index, E element)
	{
		long availableElements = getAvailableElements();
		if(index < 0 || index >= availableElements)
		{
			throw new IndexOutOfBoundsException("Invalid index " + index + "! Must be 0.." + (availableElements - 1) + ".");
		}
		int realIndex = (startIndex + index) % bufferSize;
		@SuppressWarnings({"unchecked"})
		E result = (E) array[realIndex];
		array[realIndex] = element;
		return result;
	}

	@Override
	public E removeFirst()
	{
		if(isEmpty())
		{
			return null;
		}
		@SuppressWarnings({"unchecked"})
		E result = (E) array[startIndex];
		array[startIndex] = null;
		int newStart = startIndex + 1;
		if(newStart == bufferSize)
		{
			newStart = 0;
		}
		startIndex = newStart;
		full = false;
		return result;
	}

	@Override
	public List<E> removeAll()
	{
		long availableElements = getAvailableElements();
		List<E> result = new ArrayList<>((int) availableElements);

		for(int i = 0; i < availableElements; i++)
		{
			result.add(removeFirst());
		}

		return result;
	}

	@Override
	public boolean isEmpty()
	{
		return (!full && startIndex == endIndex);
	}

	@Override
	public boolean isFull()
	{
		return (full && startIndex == endIndex);
	}

	@Override
	public void clear()
	{
		startIndex = 0;
		endIndex = 0;
		full = false;

		// just because of garbage collection...
		for(int i = 0; i < array.length; i++)
		{
			array[i] = null;
		}
	}

	@Override
	public void reset()
	{
		clear();
		overflowCounter = 0;
		size = 0;
	}


	@Override
	public long getSize()
	{
		return size;
	}

	@Override
	public int getAvailableElements()
	{
		if(startIndex == endIndex)
		{
			if(full)
			{
				return bufferSize;
			}
			return 0;
		}
		if(startIndex < endIndex)
		{
			// well-formed
			return endIndex - startIndex;
		}
		return bufferSize - startIndex + endIndex;
	}


	@Override
	public int getBufferSize()
	{
		return bufferSize;
	}

	@Override
	public long getOverflowCounter()
	{
		return overflowCounter;
	}

	@Override
	public Iterator<E> iterator()
	{
		return new BufferIterator();
	}

	private class BufferIterator
		implements Iterator<E>
	{
		int current;

		BufferIterator()
		{
			current = 0;
		}

		@Override
		public boolean hasNext()
		{
			return current < getAvailableElements();
		}

		@Override
		public E next()
		{
			if(!hasNext())
			{
				throw new NoSuchElementException("Iterator doesn't have more entries");
			}
			E result = getRelative(current);
			current++;
			return result;
		}

		@Override
		public void remove()
		{
			throw new UnsupportedOperationException("Buffer does not support removal of arbitrary elements!");
		}
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null) return false;
		if(!(o instanceof CircularBuffer)) return false;

		final CircularBuffer that = (CircularBuffer) o;

		long availableElements = getAvailableElements();
		if(availableElements != that.getAvailableElements()) return false;
		for(int i = 0; i < availableElements; i++)
		{
			Object thisValue = getRelative(i);
			Object thatValue = that.getRelative(i);
			if(thisValue == null)
			{
				if(thatValue != null)
				{
					return false;
				}
			}
			else
			{
				if(!thisValue.equals(thatValue))
				{
					return false;
				}
			}
		}

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = 17;
		for(E element : this)
		{
			if(element != null)
			{
				result = 17 * result + element.hashCode();
			}
		}
		return result;
	}

	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();
		result.append('[');
		boolean first = true;
		for(E current : this)
		{
			if(first)
			{
				first = false;
			}
			else
			{
				result.append(", ");
			}
			result.append(current);
		}
		result.append(']');
		return result.toString();
	}

	/**
	 * Returns a shallow copy of this <code>OverwritingCircularBuffer</code> instance.  (The
	 * elements themselves are not cloned.)
	 *
	 * @return a clone of this <code>OverwritingCircularBuffer</code> instance
	 */
	@Override
	public OverwritingCircularBuffer<E> clone()
		throws CloneNotSupportedException
	{
		@SuppressWarnings({"unchecked"})
		OverwritingCircularBuffer<E> v = (OverwritingCircularBuffer<E>) super.clone();
		v.array = array.clone();
		return v;
	}

	/*
	 * Save the state of the <code>OverwritingCircularBuffer</code> instance to a stream (that
	 * is, serialize it).
	 */
	private void writeObject(java.io.ObjectOutputStream s)
		throws java.io.IOException
	{
		// Write out getBufferSize
		s.defaultWriteObject();

		// Write out getAvailableElements
		long availableElements = getAvailableElements();
		s.writeLong(availableElements);

		// Write out all elements
		for(int i = 0; i < availableElements; i++)
		{
			s.writeObject(getRelative(i));
		}
	}

	/*
	 * Reconstitute the <code>ArrayList</code> instance from a stream (that is,
	 * deserialize it).
	 */
	private void readObject(java.io.ObjectInputStream s)
		throws java.io.IOException, ClassNotFoundException
	{
		// Read in getBufferSize
		s.defaultReadObject();
		array = new Object[bufferSize];

		// Read actual getAvailableElements
		int elementCount = s.readInt();

		// Read in all elements
		for(int i = 0; i < elementCount; i++)
		{
			internalAdd(s.readObject());
		}
	}
}
