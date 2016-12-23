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
import static org.junit.Assert.assertTrue;

public class BooleanValuesTest
	extends ConditionTestBase
{
	@Test
	public void testTrue()
		throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		BooleanValues condition = BooleanValues.TRUE;
		assertEquals(true, condition.isTrue(null));
		internalTestCondition(condition);
	}

	@Test
	public void testFalse()
		throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		BooleanValues condition = BooleanValues.FALSE;
		assertEquals(false, condition.isTrue(null));
		internalTestCondition(condition);
	}

	@Test
	public void testHashCode()
			throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		assertNotEquals(BooleanValues.FALSE.hashCode(), BooleanValues.TRUE.hashCode());
	}

	@Test
	public void testEquals()
			throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		assertNotEquals(BooleanValues.FALSE, BooleanValues.TRUE);
	}

	@Test
	public void testBasicEquals()
	{
		Condition condition = BooleanValues.TRUE;
		//noinspection ObjectEqualsNull
		assertFalse(condition.equals(null));
		assertFalse(condition.equals(new Object()));
		assertTrue(condition.equals(condition));
	}

	@Test
	public void testString()
			throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		assertEquals("false", BooleanValues.FALSE.toString());
		assertEquals("true", BooleanValues.TRUE.toString());
	}
}
