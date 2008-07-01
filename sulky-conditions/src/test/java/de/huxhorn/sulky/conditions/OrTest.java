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

import java.util.ArrayList;
import java.util.List;
import java.io.IOException;

public class OrTest
	extends ConditionTestBase
{
	public void testEmpty() throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		Or condition =new Or();
		assertNull(condition.getConditions());
		assertEquals(false, condition.isTrue(null));
		internalTestCondition(condition);

		List<Condition> conditions=new ArrayList<Condition>();
		condition.setConditions(conditions);
		assertNotNull(condition.getConditions());
		assertEquals(conditions, condition.getConditions());
		assertEquals(false, condition.isTrue(null));
		internalTestCondition(condition);
	}

	public void testTrue() throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		Or condition =new Or();
		List<Condition> conditions=new ArrayList<Condition>();
		condition.setConditions(conditions);

		conditions.add(BooleanValues.TRUE);

		assertEquals(true, condition.isTrue(null));
		internalTestCondition(condition);

		conditions.add(BooleanValues.FALSE);
		conditions.add(BooleanValues.FALSE);
		assertEquals(true, condition.isTrue(null));
		internalTestCondition(condition);
	}

	public void testFalse() throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		Or condition =new Or();
		List<Condition> conditions=new ArrayList<Condition>();
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
