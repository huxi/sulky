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

public interface CircularBuffer<E>
	extends Buffer<E>, AppendOperation<E>, RemoveOperation<E>, ResetOperation
{
	/**
	 * Returns the element at the given <tt>index</tt>. <tt>index</tt> must be in the
	 * range <tt>[0..(getAvailableElements()-1)]</tt>.
	 * @param index
	 */
	E getRelative(int index);

	/**
	 * Sets the element at the given <tt>index</tt>. <tt>index</tt> must be in the
	 * range <tt>[0..(getAvailableElements()-1)]</tt>.
	 * @param index
	 */
	E setRelative(int index, E element);

	long getOverflowCounter();

	int getAvailableElements();

	int getBufferSize();
}
