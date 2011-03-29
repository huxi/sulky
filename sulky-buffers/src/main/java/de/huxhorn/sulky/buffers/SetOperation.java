/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2011 Joern Huxhorn
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
 * Copyright 2007-2011 Joern Huxhorn
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

public interface SetOperation<E>
{
	/**
	 * Set the element at the given index to object.
	 *
	 * @param index  the index of the given object
	 * @param object the object to be set at index
	 * @return true if the object could be set at the given index, false if that was not possible (for example because that spot was already used and the buffer is a write-once buffer)
	 * @throws UnsupportedOperationException if isSetSupported returns false.
	 */
	boolean set(long index, E object);

	/**
	 * @return true if set is currently supported by this instance, false otherwise.
	 */
	boolean isSetSupported();
}
