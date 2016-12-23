/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2014 Joern Huxhorn
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
 * Copyright 2007-2014 Joern Huxhorn
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
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

public class NotTest
	extends ConditionTestBase
{
	@Test
	public void empty()
		throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		Not condition = new Not();
		assertNull(condition.getCondition());
		assertEquals(false, condition.isTrue(null));
		internalTestCondition(condition);
	}

	@Test
	public void testTrue()
		throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		Not condition = new Not();
		condition.setCondition(BooleanValues.FALSE);
		assertEquals(true, condition.isTrue(null));
		internalTestCondition(condition);
	}

	@Test
	public void testFalse()
		throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		Not condition = new Not();
		condition.setCondition(BooleanValues.TRUE);
		assertEquals(false, condition.isTrue(null));
		internalTestCondition(condition);
	}

	@Test
	public void testHash()
	{
		Not condition1 = new Not();
		Not condition2 = new Not();
		assertEquals(condition1.hashCode(), condition2.hashCode());

		condition1.setCondition(BooleanValues.TRUE);
		condition2.setCondition(BooleanValues.TRUE);
		assertEquals(condition1.hashCode(), condition2.hashCode());

		condition1.setCondition(BooleanValues.FALSE);
		condition2.setCondition(BooleanValues.FALSE);
		assertEquals(condition1.hashCode(), condition2.hashCode());

		condition1.setCondition(BooleanValues.TRUE);
		assertNotEquals(condition1.hashCode(), condition2.hashCode());
	}

	@Test
	public void testEquals()
	{
		Not condition1 = new Not();
		Not condition2 = new Not();
		assertEquals(condition1, condition2);

		condition1.setCondition(BooleanValues.TRUE);
		assertNotEquals(condition1, condition2);
		assertNotEquals(condition2, condition1);

		condition2.setCondition(BooleanValues.TRUE);
		assertEquals(condition1, condition2);

		condition1.setCondition(BooleanValues.FALSE);
		condition2.setCondition(BooleanValues.FALSE);
		assertEquals(condition1, condition2);

		condition1.setCondition(BooleanValues.TRUE);
		assertNotEquals(condition1, condition2);
	}

	@Test
	public void testBasicEquals()
	{
		Condition condition = new Not();
		//noinspection ObjectEqualsNull
		assertFalse(condition.equals(null));
		assertFalse(condition.equals(new Object()));
		assertTrue(condition.equals(condition));
	}

	@Test
	public void testString()
	{
		Not condition = new Not();
		assertEquals("false", condition.toString());

		condition.setCondition(BooleanValues.TRUE);
		assertEquals("!(true)", condition.toString());

		condition.setCondition(BooleanValues.FALSE);
		assertEquals("!(false)", condition.toString());
	}
}
