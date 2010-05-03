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

package de.huxhorn.sulky.swing;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import javax.swing.*;

public class Tables
{
	private static final Method CONVERT_ROW_INDEX_TO_MODEL_METHOD;

	private static final Method SET_AUTO_CREATE_ROW_SORTER_METHOD;

	static
	{
		Method method = null;
		try
		{
			method = JTable.class.getMethod("setAutoCreateRowSorter", boolean.class);
		}
		catch(Throwable e)
		{
			// ignore
		}
		SET_AUTO_CREATE_ROW_SORTER_METHOD = method;

		method = null;
		try
		{
			method = JTable.class.getMethod("convertRowIndexToModel", int.class);
		}
		catch(Throwable e)
		{
			// ignore
		}
		CONVERT_ROW_INDEX_TO_MODEL_METHOD = method;
	}

	public static void setAutoCreateRowSorter(JTable table, boolean auto)
	{
		if(SET_AUTO_CREATE_ROW_SORTER_METHOD != null)
		{
			try
			{
				SET_AUTO_CREATE_ROW_SORTER_METHOD.invoke(table, auto);
			}
			catch(IllegalAccessException e)
			{
				// ignore
			}
			catch(InvocationTargetException e)
			{
				// ignore
			}
		}
	}

	public static int convertRowIndexToModel(JTable table, int row)
	{
		int result = row;
		if(CONVERT_ROW_INDEX_TO_MODEL_METHOD != null)
		{
			try
			{
				result = (Integer) CONVERT_ROW_INDEX_TO_MODEL_METHOD.invoke(table, row);
			}
			catch(Throwable e)
			{
				// ignore
			}
		}
		return result;
	}

}
