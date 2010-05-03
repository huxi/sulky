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
package de.huxhorn.sulky.buffers;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class BasicBufferIterator<E>
	implements Iterator<E>
{
	private long current;
	private Buffer<E> buffer;
	private long size;

	public BasicBufferIterator(Buffer<E> buffer)
	{
		this.buffer = buffer;
		this.size = buffer.getSize();
		this.current = 0;
	}

	public boolean hasNext()
	{
		return current < size;
	}

	public E next()
	{
		if(!hasNext())
		{
			throw new NoSuchElementException("Iterator doesn't have more entries");
		}
		E result = buffer.get(current);
		current++;
		return result;
	}

	public void remove()
	{
		throw new UnsupportedOperationException("Buffer does not support removal of arbitrary elements!");
	}
}
