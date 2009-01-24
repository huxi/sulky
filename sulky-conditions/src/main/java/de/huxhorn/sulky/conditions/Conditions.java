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

import java.util.List;

public class Conditions
{
	/**
	 * Returns true if condition contains otherCondition.
	 * <p/>
	 * Conditions "contain" themselves so this method returns true if condition equals otherCondition.
	 *
	 * @param condition
	 * @param otherCondition
	 * @return true if condition contains otherCondition.
	 */
	public static boolean contains(Condition condition, Condition otherCondition)
	{
		if(condition == null)
		{
			return false;
		}
		if(condition.equals(otherCondition))
		{
			return true;
		}
		if(condition instanceof ConditionWrapper)
		{
			ConditionWrapper conditionWrapper = (ConditionWrapper) condition;
			Condition wrappedCondition = conditionWrapper.getCondition();
			if(wrappedCondition == null)
			{
				return false;
			}
			return contains(wrappedCondition, otherCondition);
		}
		else if(condition instanceof ConditionGroup)
		{
			ConditionGroup conditionGroup = (ConditionGroup) condition;
			List<Condition> conditions = conditionGroup.getConditions();
			if(conditions == null)
			{
				return false;
			}
			for(Condition c : conditions)
			{
				if(contains(c, otherCondition))
				{
					return true;
				}
			}
		}
		return false;
	}
}
