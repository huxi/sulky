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

import junit.framework.TestCase;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public class ConditionTestBase
	extends TestCase
{
	public void internalTestCondition(Condition condition)
		throws CloneNotSupportedException, IOException, ClassNotFoundException
	{
		internalTestCloneEquals(condition);
		internalTestSerialization(condition);
	}

	public void internalTestSerialization(Condition condition)
		throws IOException, ClassNotFoundException
	{
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		ObjectOutputStream oos = new ObjectOutputStream(os);
		oos.writeObject(condition);
		oos.close();
		ByteArrayInputStream is = new ByteArrayInputStream(os.toByteArray());
		ObjectInputStream ois = new ObjectInputStream(is);
		Condition deserialized = (Condition) ois.readObject();
		internalTestEquals(condition, deserialized);
	}

	public void internalTestCloneEquals(Condition condition)
		throws CloneNotSupportedException
	{
		Condition clone = condition.clone();
		internalTestEquals(condition, clone);
	}

	public void internalTestEquals(Condition original, Condition other)
	{
		assertEquals(original, other);
		if(other instanceof ConditionGroup)
		{
			ConditionGroup originalGroup = (ConditionGroup) original;
			ConditionGroup clonedGroup = (ConditionGroup) other;
			assertEquals(originalGroup.getConditions(), clonedGroup.getConditions());
		}
		else if(other instanceof ConditionWrapper)
		{
			ConditionWrapper originalWrapper = (ConditionWrapper) original;
			ConditionWrapper clonedWrapper = (ConditionWrapper) other;
			assertEquals(originalWrapper.getCondition(), clonedWrapper.getCondition());
		}
	}
}
