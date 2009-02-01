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
package de.huxhorn.sulky.generics.io;

import java.beans.Encoder;
import java.beans.Expression;
import java.beans.PersistenceDelegate;

/**
 * As described in http://weblogs.java.net/blog/malenkov/archive/2006/08/how_to_encode_e.html
 */
public class EnumPersistenceDelegate
	extends PersistenceDelegate
{
	protected boolean mutatesTo(Object oldInstance, Object newInstance)
	{
		return oldInstance == newInstance;
	}

	protected Expression instantiate(Object oldInstance, Encoder out)
	{
		Enum e = (Enum) oldInstance;
		return new Expression(e, e.getClass(), "valueOf", new Object[]{e.name()});
	}
}
