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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

public class OrTest
	extends ConditionTestBase
{
	@Test
	public void empty()
		throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		Or condition = new Or();
		assertNull(condition.getConditions());
		assertEquals(false, condition.isTrue(null));
		internalTestCondition(condition);

		List<Condition> conditions = new ArrayList<Condition>();
		condition.setConditions(conditions);
		assertNotNull(condition.getConditions());
		assertEquals(conditions, condition.getConditions());
		assertEquals(false, condition.isTrue(null));
		internalTestCondition(condition);
	}

	@Test
	public void testTrue()
		throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		Or condition = new Or();
		List<Condition> conditions = new ArrayList<Condition>();
		condition.setConditions(conditions);

		conditions.add(BooleanValues.TRUE);

		assertEquals(true, condition.isTrue(null));
		internalTestCondition(condition);

		conditions.add(BooleanValues.FALSE);
		conditions.add(BooleanValues.FALSE);
		assertEquals(true, condition.isTrue(null));
		internalTestCondition(condition);
	}

	@Test
	public void testFalse()
		throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		Or condition = new Or();
		List<Condition> conditions = new ArrayList<Condition>();
		condition.setConditions(conditions);

		conditions.add(BooleanValues.FALSE);

		assertEquals(false, condition.isTrue(null));
		internalTestCondition(condition);


		conditions.add(BooleanValues.FALSE);
		conditions.add(BooleanValues.FALSE);
		assertEquals(false, condition.isTrue(null));
		internalTestCondition(condition);
	}
}
