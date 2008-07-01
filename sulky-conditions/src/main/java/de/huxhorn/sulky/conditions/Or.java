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

import java.util.List;
import java.util.ArrayList;

/**
 * Returns true if any of it's sub-conditions return true.
 */
public class Or
	implements ConditionGroup
{
	private static final long serialVersionUID = 4475481127736826653L;

	private List<Condition> conditions;

	public Or()
	{
		this(null);
	}

	public Or(List<Condition> conditions)
	{
		this.conditions = conditions;
	}

	public List<Condition> getConditions()
	{
		return conditions;
	}

	public void setConditions(List<Condition> conditions)
	{
		this.conditions = conditions;
	}

	/**
	 * Returns true if any of the contained conditions is true.
	 *
	 * This implies that it returns false if the contained conditions are either null or empty.
	 * @param element
	 * @return true if any of the contained conditions is true.
	 */
	public boolean isTrue(Object element)
	{
		if(conditions!=null)
		{
			for(Condition condition:conditions)
			{
				if(condition.isTrue(element))
				{
					return true;
				}
			}
		}
		return false;
	}

	public Or clone() throws CloneNotSupportedException
	{
		Or result=(Or)super.clone();
		if(result.conditions!= null)
		{
			List<Condition> clonedConditions=new ArrayList<Condition>(conditions.size());
			for(Condition condition:result.conditions)
			{
				clonedConditions.add(condition.clone());
			}
			result.conditions=clonedConditions;
		}
		return result;
	}


	public String toString()
	{
		StringBuffer result=new StringBuffer();
		if(conditions==null || conditions.size()==0)
		{
			result.append("false");
		}
		else
		{
			result.append("(");
			boolean first=true;
			for(Condition condition:conditions)
			{
				if(first)
				{
					first=false;
				}
				else
				{
					result.append(" || ");
				}
				result.append(condition);
			}
			result.append(")");
		}
		return result.toString();
	}

	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		final Or or = (Or) o;

		return !(conditions != null ? !conditions.equals(or.conditions) : or.conditions != null);
	}

	public int hashCode()
	{
		return (conditions != null ? conditions.hashCode() : 0);
	}
}
