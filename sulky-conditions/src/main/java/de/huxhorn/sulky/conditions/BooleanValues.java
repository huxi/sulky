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
package de.huxhorn.sulky.conditions;

public class BooleanValues
	implements Condition
{
	private static final long serialVersionUID = 1780367398890411212L;

	/**
	 * true singleton
	 */
	public static final BooleanValues TRUE=new BooleanValues(true);

	/**
	 * false singleton
	 */
	public static final BooleanValues FALSE=new BooleanValues(false);

	public static BooleanValues getInstance(boolean value)
	{
		if(value)
		{
			return TRUE;
		}
		return FALSE;
	}

	private final boolean value;
	private transient final String string;

	BooleanValues(boolean b)
	{
		this.value=b;
		this.string=""+b;
	}

	/**
	 * Returns either true or false, depending on the instance.
	 *
	 * @param element
	 * @return either true or false, depending on the instance.
	 */
	public boolean isTrue(Object element)
	{
		return value;
	}

	/** @noinspection CloneDoesntCallSuperClone*/
	@Override
	public BooleanValues clone() throws CloneNotSupportedException
	{
		return this;
	}

	private Object readResolve()
	{
		return getInstance(value);
	}

	@Override
	public String toString()
	{
		return string;
	}
}
