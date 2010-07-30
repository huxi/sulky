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

/*
 * Copyright 2007-2010 Joern Huxhorn
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

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class ConditionsTest
	extends ConditionTestBase
{
	@Test
	public void conditionsContains()
	{
		assertFalse(Conditions.contains(null, null));
		assertFalse(Conditions.contains(null, BooleanValues.TRUE));
		assertTrue(Conditions.contains(BooleanValues.TRUE, BooleanValues.TRUE));
		assertTrue(Conditions.contains(BooleanValues.FALSE, BooleanValues.FALSE));

		ConditionGroup conditionGroup = new And();
		assertTrue(Conditions.contains(conditionGroup, conditionGroup));
		List<Condition> conditions = new ArrayList<Condition>();
		conditionGroup.setConditions(conditions);
		assertTrue(Conditions.contains(conditionGroup, conditionGroup));
		conditions.add(BooleanValues.TRUE);
		assertTrue(Conditions.contains(conditionGroup, conditionGroup));
		assertTrue(Conditions.contains(conditionGroup, BooleanValues.TRUE));
		assertFalse(Conditions.contains(conditionGroup, BooleanValues.FALSE));
		conditions.add(BooleanValues.FALSE);
		assertTrue(Conditions.contains(conditionGroup, BooleanValues.FALSE));

		ConditionWrapper conditionWrapper = new Not();
		assertTrue(Conditions.contains(conditionWrapper, conditionWrapper));
		conditionWrapper.setCondition(BooleanValues.TRUE);
		assertTrue(Conditions.contains(conditionWrapper, conditionWrapper));
		assertTrue(Conditions.contains(conditionWrapper, BooleanValues.TRUE));
		assertFalse(Conditions.contains(conditionWrapper, BooleanValues.FALSE));
	}
}
