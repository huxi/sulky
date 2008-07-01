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

import java.io.IOException;

public class NamedConditionTest
	extends ConditionTestBase
{
	public void testEmpty() throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		NamedCondition condition =new NamedCondition();
		assertNull(condition.getCondition());
		assertEquals(false, condition.isTrue(null));
		internalTestCondition(condition);
	}

	public void testTrue() throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		NamedCondition condition =new NamedCondition();
		condition.setCondition(BooleanValues.TRUE);
		assertEquals(true, condition.isTrue(null));
		internalTestCondition(condition);
	}

	public void testFalse() throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		NamedCondition condition =new NamedCondition();
		condition.setCondition(BooleanValues.FALSE);
		assertEquals(false, condition.isTrue(null));
		internalTestCondition(condition);
	}
}
