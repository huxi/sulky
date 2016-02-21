/*
 * sulky-resources - inheritance-safe class resources.
 * Copyright (C) 2002-2011 Joern Huxhorn
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
 * Copyright 2002-2011 Joern Huxhorn
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

import de.huxhorn.sulky.resources.Resources;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * DOCUMENT: <code>PackagelessTest</code>
 * A seperate class to test Resources.getShortClassName() without package.
 */
public class PackagelessTest
{
	@Test
	public void getShortClassName()
		throws Exception
	{
		String str;

		str = Resources.getShortClassName(PackagelessTest.class);
		assertEquals("PackagelessTest", str);
	}

	@Test
	public void getPathToPackage()
		throws Exception
	{
		String str;

		str = Resources.getPathToPackage(PackagelessTest.class);
		assertEquals("/", str);
	}
}
