/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2017 Joern Huxhorn
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
 * Copyright 2007-2017 Joern Huxhorn
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

package de.huxhorn.sulky.conditions;

/**
 * Returns true if it's sub-condition returns false.
 */
public class Not
	implements ConditionWrapper, Cloneable
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
	 *
	 * This implies that it returns false if the contained condition is null.
	 *
	 * @param element the object to be evaluated.
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
			result.append("!(").append(condition).append(')');
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
