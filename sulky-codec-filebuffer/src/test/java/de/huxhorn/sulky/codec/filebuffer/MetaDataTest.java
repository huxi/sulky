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
package de.huxhorn.sulky.codec.filebuffer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Test;

public class MetaDataTest
{
	@Test
	public void emptyNonSparse()
	{
		boolean sparse = false;
		MetaData instance = new MetaData(sparse);
		MetaData other = new MetaData(sparse);
		assertEquals(other, instance);
		assertEquals(0, instance.getData().size());

		other = new MetaData(!sparse);
		assertFalse(other.equals(instance));
	}

	@Test
	public void emptySparse()
	{
		boolean sparse = true;
		MetaData instance = new MetaData(sparse);
		MetaData other = new MetaData(sparse);
		assertEquals(other, instance);
		assertEquals(0, instance.getData().size());

		other = new MetaData(!sparse);
		assertFalse(other.equals(instance));
	}
}
