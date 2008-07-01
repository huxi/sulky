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

/**
 * This class can be used as an alias for other, potentially complex, conditions.
 * Returns true if the contained condition returns true.
 *
 */
public class NamedCondition
	implements ConditionWrapper
{
	private static final long serialVersionUID = 8403611136475749962L;

	private String name;
	private Condition condition;

	public String getName()
	{
		return name;
	}

	public void setName(String name)
	{
		this.name = name;
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
	 * Returns true if the contained condition returns true.
	 *
	 * This implies that it returns false if the contained condition is null.
	 *
	 * @param element
	 * @return true if the contained condition returns true.
	 */
	public boolean isTrue(Object element)
	{
		if(condition!=null)
		{
			return condition.isTrue(element);
		}
		return false;
	}

	public NamedCondition clone() throws CloneNotSupportedException
	{
		NamedCondition result=(NamedCondition)super.clone();
		if(result.condition!= null)
		{
			result.condition=result.condition.clone();
		}
		return result;
	}

	public String toString()
	{
		return "["+name+"]";
	}

	// TODO: decide if equals/hashCode should probably only take the name into account.
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final NamedCondition that = (NamedCondition) o;

		if (condition != null ? !condition.equals(that.condition) : that.condition != null) return false;
		return !(name != null ? !name.equals(that.name) : that.name != null);
	}

	public int hashCode()
	{
		int result;
		result = (name != null ? name.hashCode() : 0);
		result = 29 * result + (condition != null ? condition.hashCode() : 0);
		return result;
	}
}
