/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2018 Joern Huxhorn
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
 * Copyright 2007-2018 Joern Huxhorn
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

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class OrTest
	extends ConditionTestBase
{
	@Test
	public void empty()
		throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		Or condition = new Or();
		assertNull(condition.getConditions());
		assertFalse(condition.isTrue(null));
		internalTestCondition(condition);

		List<Condition> conditions = new ArrayList<>();
		condition.setConditions(conditions);
		assertNotNull(condition.getConditions());
		assertEquals(conditions, condition.getConditions());
		assertFalse(condition.isTrue(null));
		internalTestCondition(condition);
	}

	@Test
	public void testTrue()
		throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		Or condition = new Or();
		List<Condition> conditions = new ArrayList<>();
		condition.setConditions(conditions);

		conditions.add(BooleanValues.TRUE);

		assertTrue(condition.isTrue(null));
		internalTestCondition(condition);

		conditions.add(BooleanValues.FALSE);
		conditions.add(BooleanValues.FALSE);
		assertTrue(condition.isTrue(null));
		internalTestCondition(condition);
	}

	@Test
	public void testFalse()
		throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		Or condition = new Or();
		List<Condition> conditions = new ArrayList<>();
		condition.setConditions(conditions);

		conditions.add(BooleanValues.FALSE);

		assertFalse(condition.isTrue(null));
		internalTestCondition(condition);


		conditions.add(BooleanValues.FALSE);
		conditions.add(BooleanValues.FALSE);
		assertFalse(condition.isTrue(null));
		internalTestCondition(condition);
	}

	@Test
	public void testHash()
	{
		Or condition1 = new Or();
		Or condition2 = new Or();
		assertEquals(condition1.hashCode(), condition2.hashCode());

		List<Condition> conditions1=new ArrayList<>();
		conditions1.add(BooleanValues.FALSE);
		List<Condition> conditions2=new ArrayList<>();
		conditions2.add(BooleanValues.FALSE);

		condition1.setConditions(conditions1);
		condition2.setConditions(conditions2);
		assertEquals(condition1.hashCode(), condition2.hashCode());

		conditions2.add(BooleanValues.FALSE);
		assertNotEquals(condition1.hashCode(), condition2.hashCode());
	}

	@Test
	public void testEquals()
	{
		Or condition1 = new Or();
		Or condition2 = new Or();
		assertEquals(condition1, condition2);

		List<Condition> conditions1=new ArrayList<>();
		List<Condition> conditions2=new ArrayList<>();

		condition1.setConditions(conditions1);
		assertNotEquals(condition1, condition2);
		assertNotEquals(condition2, condition1);

		condition2.setConditions(conditions2);
		assertEquals(condition1, condition2);

		conditions1.add(BooleanValues.FALSE);
		conditions2.add(BooleanValues.FALSE);
		assertEquals(condition1, condition2);

		conditions2.add(BooleanValues.FALSE);
		assertNotEquals(hashCode(), condition2);
	}

	@Test
	@SuppressWarnings({"PMD.EqualsNull", "PMD.UseAssertEqualsInsteadOfAssertTrue"})
	public void testBasicEquals()
	{
		Condition condition = new Or();
		//noinspection ObjectEqualsNull
		assertFalse(condition.equals(null));
		assertFalse(condition.equals(new Object()));
		//noinspection EqualsWithItself
		assertTrue(condition.equals(condition));
	}

	@Test
	public void testString()
	{
		Or condition = new Or();
		assertEquals("false", condition.toString());

		List<Condition> conditions=new ArrayList<>();
		condition.setConditions(conditions);
		assertEquals("false", condition.toString());

		conditions.add(BooleanValues.FALSE);
		condition.setConditions(conditions);
		assertEquals("(false)", condition.toString());

		conditions.add(BooleanValues.FALSE);
		assertEquals("(false || false)", condition.toString());
	}
}
