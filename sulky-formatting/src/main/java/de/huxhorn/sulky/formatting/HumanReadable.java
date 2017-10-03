/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2017 Joern Huxhorn
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
 * Copyright 2007-2017 Joern Huxhorn
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

package de.huxhorn.sulky.formatting;

public final class HumanReadable
{
	private static final Unit[] BINARY_UNITS =
			{
					new Unit(1L << 60, "exbi", "Ei"),
					new Unit(1L << 50, "pebi", "Pi"),
					new Unit(1L << 40, "tebi", "Ti"),
					new Unit(1L << 30, "gibi", "Gi"),
					new Unit(1L << 20, "mebi", "Mi"),
					new Unit(1L << 10, "kibi", "Ki"),
			};

	private static final Unit[] DECIMAL_UNITS =
			{
					new Unit(1000L * 1000L * 1000L * 1000L * 1000L * 1000L, "exa", "E"),
					new Unit(1000L * 1000L * 1000L * 1000L * 1000L, "peta", "P"),
					new Unit(1000L * 1000L * 1000L * 1000L, "tera", "T"),
					new Unit(1000L * 1000L * 1000L, "giga", "G"),
					new Unit(1000L * 1000L, "mega", "M"),
					new Unit(1000L, "kilo", "k"),
			};

	static
	{
		// for the sake of coverage
		new HumanReadable();
	}

	private HumanReadable()
	{}

	public static String getHumanReadableSize(long size, boolean useBinaryUnits, boolean useSymbol)
	{
		if(useBinaryUnits)
		{
			return internalGetHumanReadableSize(size, BINARY_UNITS, useSymbol);
		}
		return internalGetHumanReadableSize(size, DECIMAL_UNITS, useSymbol);
	}

	private static String internalGetHumanReadableSize(long size, Unit[] units, boolean useSymbol)
	{
		boolean negative = false;
		if(size < 0)
		{
			negative = true;
			if(size == Long.MIN_VALUE)
			{
				// rounding won't care
				size = Long.MAX_VALUE;
			}
			else
			{
				size = size * -1;
			}
		}
		Unit correctUnit = null;
		long fraction = 0;
		for (Unit unit : units)
		{
			fraction = size / unit.getFactor();
			if (fraction > 0)
			{
				correctUnit = unit;
				break;
			}
		}
		StringBuilder result = new StringBuilder();
		if(negative)
		{
			result.append('-');
		}
		if(correctUnit == null)
		{
			return result.append(size).append(' ').toString();
		}
		long remainder = size % correctUnit.getFactor();
		remainder = Math.round(((((double)remainder) * 100L) / correctUnit.getFactor()));
		if(remainder > 99)
		{
			fraction++;
			remainder = 0;
		}
		result.append(fraction).append('.');
		if(remainder < 10)
		{
			result.append('0');
		}
		result.append(remainder).append(' ');
		if(useSymbol)
		{
			result.append(correctUnit.getSymbol());
		}
		else
		{
			result.append(correctUnit.getName());
		}
		return result.toString();
	}

	@SuppressWarnings("PMD.ShortClassName")
	private static class Unit
	{
		private final long factor;
		private final String name;
		private final String symbol;

		Unit(long factor, String name, String symbol)
		{
			this.factor = factor;
			this.name = name;
			this.symbol = symbol;
		}

		String getSymbol()
		{
			return symbol;
		}

		long getFactor()
		{
			return factor;
		}

		String getName()
		{
			return name;
		}
	}
}
