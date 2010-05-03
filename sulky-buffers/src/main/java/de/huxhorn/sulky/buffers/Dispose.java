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

public class Dispose
{
	/**
	 * Executes dispose() on the given object if it implements DisposeOperation. Does nothing otherwise.
	 *
	 * @param obj an object that might implement DisposeOperation
	 * @return true if dispose() has been called.
	 */
	public static boolean dispose(Object obj)
	{
		if(obj instanceof DisposeOperation)
		{
			DisposeOperation dispose = (DisposeOperation) obj;
			dispose.dispose();
			return true;
		}
		return false;
	}

	/**
	 * @param obj an object that might implement DisposeOperation
	 * @return false if obj does not implement DisposeOperation, obj.isDisposed() otherwise.
	 */
	public static boolean isDisposed(Object obj)
	{
		if(obj instanceof DisposeOperation)
		{
			DisposeOperation dispose = (DisposeOperation) obj;
			return dispose.isDisposed();
		}
		return false;
	}

}
