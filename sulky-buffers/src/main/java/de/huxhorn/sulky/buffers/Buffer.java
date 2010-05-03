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

public interface Buffer<E>
	extends Iterable<E>
{
	/**
	 * Returns the value at the given <tt>index</tt>.
	 * <tt>index</tt> must be in the range <tt>[0..(getSize()-1)]</tt>.
	 * Returns <tt>null</tt> if a value couldn't be resolved, e.g. in case of a volatile buffer.
	 *
	 * @param index must be in the range <tt>[0..(getSize()-1)]</tt>.
	 * @return the value at the given <tt>index</tt>.
	 */
	E get(long index);

	/**
	 * @return the size of the buffer.
	 */
	long getSize();
}
