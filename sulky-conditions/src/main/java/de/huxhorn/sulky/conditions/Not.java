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
package de.huxhorn.sulky.conditions;

/**
 * Returns true if it's sub-condition returns false.
 */
public class Not
	implements ConditionWrapper
{
	private static final long serialVersionUID = -8217260808132179891L;

	private Condition condition;

	public Not()
	{
		this(null);
	}

	public Not(Condition condition)
	{
		this.condition = condition;
	}

	public Condition getCondition()
	{
		return condition;
	}

	public void setCondition(Condition condition)
	{
		this.condition = condition;
	}

	/**
	 * Returns true if the contained condition returns false.
	 * <p/>
	 * This implies that it returns false if the contained condition is null.
	 *
	 * @param element
	 * @return true if the contained condition returns false.
	 */
	public boolean isTrue(Object element)
	{
		if(condition != null)
		{
			return !condition.isTrue(element);
		}
		return false;
	}

	public Not clone()
		throws CloneNotSupportedException
	{
		Not result = (Not) super.clone();
		if(result.condition != null)
		{
			result.condition = result.condition.clone();
		}
		return result;
	}

	public String toString()
	{
		StringBuilder result = new StringBuilder();
		if(condition == null)
		{
			result.append("false");
		}
		else
		{
			result.append("!(");
			result.append(condition);
			result.append(")");
		}
		return result.toString();
	}

	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		final Not not = (Not) o;

		return !(condition != null ? !condition.equals(not.condition) : not.condition != null);
	}

	public int hashCode()
	{
		return (condition != null ? condition.hashCode() : 0);
	}
}
