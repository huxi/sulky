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

package de.huxhorn.sulky.formatting;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class HumanReadable
{
	private HumanReadable()
	{}
	
	static class Unit
	{
		private String name;
		private long factor;
		private String symbol;

		public Unit(long factor, String name, String symbol)
		{
			this.factor = factor;
			this.name = name;
			this.symbol = symbol;
		}

		public String getSymbol()
		{
			return symbol;
		}

		public void setSymbol(String symbol)
		{
			this.symbol = symbol;
		}

		public long getFactor()
		{
			return factor;
		}

		public void setFactor(long factor)
		{
			this.factor = factor;
		}

		public String getName()
		{
			return name;
		}

		public void setName(String name)
		{
			this.name = name;
		}

		public String toString()
		{
			StringBuilder result = new StringBuilder();
			result.append("Unit[");
			result.append("name=").append(name);
			result.append(", symbol=").append(symbol);
			result.append(", factor=").append(factor);
			result.append("]");
			return result.toString();
		}
	}

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
		final Logger logger = LoggerFactory.getLogger(HumanReadable.class);

		if(logger.isDebugEnabled())
		{
			StringBuilder msg = new StringBuilder("Binary units:\n");
			for(Unit unit : BINARY_UNITS)
			{
				msg.append("\t").append(unit).append("\n");
			}

			msg.append("\nDecimal units:\n");
			for(Unit unit : DECIMAL_UNITS)
			{
				msg.append("\t").append(unit).append("\n");
			}
			logger.debug(msg.toString());
		}
	}

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
		final Logger logger = LoggerFactory.getLogger(HumanReadable.class);

		Unit correctUnit = null;
		long fraction = 0;
		for(Unit unit : units)
		{
			fraction = size / unit.getFactor();
			if(fraction > 0)
			{
				if(logger.isDebugEnabled()) logger.debug("Correct unit: " + unit);
				correctUnit = unit;
				break;
			}
		}
		if(correctUnit == null)
		{
			return String.valueOf(size) + " ";
		}
		StringBuilder result = new StringBuilder();
		result.append(fraction);
		long remainder = size % correctUnit.getFactor();
		//if(remainder!=0)
		{
			result.append(".");
			remainder = remainder * 100;
			remainder = remainder / correctUnit.getFactor();
			if(remainder < 10)
			{
				result.append("0");
			}
			result.append(remainder);
		}
		result.append(" ");
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

}
