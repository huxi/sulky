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
package de.huxhorn.sulky.buffers;

public interface SetOperation<E>
{
	/**
	 * Set the element at the given index to object.
	 *
	 * @param index  the index of the given object
	 * @param object the object to be set at index
	 * @return true if the object could be set at the given index, false if that was not possible (for example because that spot was already used and the buffer is a write-once buffer)
	 * @throws IllegalStateException if isSetSupported returns false.
	 */
	boolean set(long index, E object);

	/**
	 * @return true if set is currently supported by this instance, false otherwise.
	 */
	boolean isSetSupported();
}
