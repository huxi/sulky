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

import java.util.*;
import java.io.Serializable;

/**
 * http://c2.com/cgi/wiki?CircularBuffer
 */
public class OverwritingCircularBuffer<E>
		implements CircularBuffer<E>, RandomAccess, Cloneable, Serializable
{
	private int bufferSize;
	transient private int startIndex;
	transient private int endIndex;
	transient private long overflowCounter;
	transient private long size;
	transient private boolean full;
	transient private Object[] array;

	public OverwritingCircularBuffer(int bufferSize)
	{
		if (bufferSize < 1)
		{
			throw new IllegalArgumentException("bufferSize (" + bufferSize + ") must be positive!");
		}
		this.bufferSize = bufferSize;

		array = new Object[bufferSize];
		reset();
	}

	public void add(E element)
	{
		internalAdd(element);
	}

	private void internalAdd(Object element)
	{
		if (isFull())
		{
			removeFirst();
			overflowCounter++;
		}
		size++;
		array[endIndex] = element;
		endIndex++;
		if (endIndex == bufferSize)
		{
			endIndex = 0;
		}
		if (startIndex == endIndex)
		{
			full = true;
		}
	}

	public void addAll(List<E> elements)
	{
		for (E element : elements)
		{
			add(element);
		}
	}

	public void addAll(E[] elements)
	{
		for (E element : elements)
		{
			add(element);
		}
	}

	public E get(long index)
	{
		if (index < 0 || index >= size)
		{
			throw new IndexOutOfBoundsException("Invalid index " + index + "! Must be 0.." + (size - 1) + ".");
		}
		int realIndex=(int) (index-overflowCounter);
		if(realIndex<0)
		{
			return null;
		}
		return getRelative(realIndex);
	}

	public E getRelative(int index)
	{
		long size = getAvailableElements();
		if (index < 0 || index >= size)
		{
			throw new IndexOutOfBoundsException("Invalid index " + index + "! Must be 0.." + (size - 1) + ".");
		}
		int realIndex = (startIndex + index) % bufferSize;
		//noinspection unchecked
		return (E) array[realIndex];
	}

	public E setRelative(int index, E element)
	{
		long size = getAvailableElements();
		if (index < 0 || index >= size)
		{
			throw new IndexOutOfBoundsException("Invalid index " + index + "! Must be 0.." + (size - 1) + ".");
		}
		int realIndex = (startIndex + index) % bufferSize;
		//noinspection unchecked
		E result = (E) array[realIndex];
		array[realIndex] = element;
		return result;
	}

	public E removeFirst()
	{
		if (isEmpty())
		{
			return null;
		}
		//noinspection unchecked
		E result = (E) array[startIndex];
		array[startIndex] = null;
		int newStart = startIndex + 1;
		if (newStart == bufferSize)
		{
			newStart = 0;
		}
		startIndex = newStart;
		full = false;
		return result;
	}

	public List<E> removeAll()
	{
		long size = getAvailableElements();
		List<E> result = new ArrayList<E>((int)size);

		for (int i = 0; i < size; i++)
		{
			result.add(removeFirst());
		}

		return result;
	}

	public boolean isEmpty()
	{
		return (!full && startIndex == endIndex);
	}

	public boolean isFull()
	{
		return (full && startIndex == endIndex);
	}

	public void clear()
	{
		startIndex = endIndex = 0;
		full = false;

		// just because of garbage collection...
		for (int i = 0; i < array.length; i++)
		{
			array[i] = null;
		}
	}

	public void reset()
	{
		clear();
		overflowCounter = 0;
		size = 0;
	}


	public long getSize()
	{
		return size;
	}

	public int getAvailableElements()
	{
		if (startIndex == endIndex)
		{
			if (full)
			{
				return bufferSize;
			}
			return 0;
		}
		if (startIndex < endIndex)
		{
			// well-formed
			return endIndex - startIndex;
		}
		return bufferSize - startIndex + endIndex;
	}


	public int getBufferSize()
	{
		return bufferSize;
	}

	public long getOverflowCounter()
	{
		return overflowCounter;
	}

	public Iterator<E> iterator()
	{
		return new BufferIterator();
	}

	private class BufferIterator implements Iterator<E>
	{
		int current;

		public BufferIterator()
		{
			current = 0;
		}

		public boolean hasNext()
		{
			return current < getAvailableElements();
		}

		public E next()
		{
			if (!hasNext())
			{
				throw new NoSuchElementException("Iterator doesn't have more entries");
			}
			E result = getRelative(current);
			current++;
			return result;
		}

		public void remove()
		{
			throw new UnsupportedOperationException("Buffer does not support removal of arbitrary elements!");
		}
	}

	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null) return false;
		if (!(o instanceof CircularBuffer)) return false;

		final CircularBuffer that = (CircularBuffer) o;

		long size = getAvailableElements();
		if (size != that.getAvailableElements()) return false;
		for (int i = 0; i < size; i++)
		{
			Object thisValue = getRelative(i);
			Object thatValue = that.getRelative(i);
			if (thisValue == null)
			{
				if (thatValue != null)
				{
					return false;
				}
			}
			else
			{
				if (!thisValue.equals(thatValue))
				{
					return false;
				}
			}
		}

		return true;
	}

	public int hashCode()
	{
		int result = 17;
		for (E element : this)
		{
			if (element != null)
			{
				result = 17 * result + element.hashCode();
			}
		}
		return result;
	}

	public String toString()
	{
		StringBuilder result = new StringBuilder();
		result.append("[");
		boolean first = true;
		for (E current : this)
		{
			if (first)
			{
				first = false;
			}
			else
			{
				result.append(", ");
			}
			result.append(current);
		}
		result.append("]");
		return result.toString();
	}

	/**
	 * Returns a shallow copy of this <tt>OverwritingCircularBuffer</tt> instance.  (The
	 * elements themselves are not copied.)
	 *
	 * @return a clone of this <tt>OverwritingCircularBuffer</tt> instance
	 */
	public Object clone() throws CloneNotSupportedException
	{
/*
	private int getBufferSize;
	transient private int startIndex;
	transient private int endIndex;
	transient private long overflowCounter;
	transient private long getSize;
	transient private boolean full;
	transient private E[] array;
*/
		OverwritingCircularBuffer<E> v = (OverwritingCircularBuffer<E>) super.clone();
		v.array = array.clone();
		return v;
	}

	/**
	 * Save the state of the <tt>OverwritingCircularBuffer</tt> instance to a stream (that
	 * is, serialize it).
	 */
	private void writeObject(java.io.ObjectOutputStream s)
			throws java.io.IOException
	{
		// Write out getBufferSize
		s.defaultWriteObject();

		// Write out getAvailableElements
		long size=getAvailableElements();
		s.writeLong(size);

		// Write out all elements
		for (int i = 0; i < size; i++)
		{
			s.writeObject(getRelative(i));
		}
	}

	/**
	 * Reconstitute the <tt>ArrayList</tt> instance from a stream (that is,
	 * deserialize it).
	 */
	private void readObject(java.io.ObjectInputStream s)
			throws java.io.IOException, ClassNotFoundException
	{
		// Read in getBufferSize
		s.defaultReadObject();
		array=new Object[bufferSize];

		// Read actual getAvailableElements
		int elementCount = s.readInt();

		// Read in all elements
		for (int i = 0; i < elementCount; i++)
		{
			internalAdd(s.readObject());
		}
	}
}
