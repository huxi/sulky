/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2011 Joern Huxhorn
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
 * Copyright 2007-2011 Joern Huxhorn
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

package de.huxhorn.sulky.plist.impl;

public interface PropertyListConstants
{
	String PLIST_NODE = "plist";
	String PLIST_VERSION_ATTRIBUTE = "version";
	String PLIST_VERSION = "1.0";

	// Collections
	String ARRAY_NODE = "array";
	String DICT_NODE = "dict";
	String KEY_NODE = "key";

	// Primitive types
	String STRING_NODE = "string";
	/**
	 * Contents interpreted as Base-64 encoded
	 */
	String DATA_NODE = "data";

	/**
	 * Contents should conform to a subset of ISO 8601
	 * (in particular, YYYY '-' MM '-' DD 'T' HH ':' MM ':' SS 'Z'.
	 * Smaller units may be omitted with a loss of precision)
	 */
	String DATE_NODE = "date";

	// Numerical primitives
	/**
	 * Boolean constant true
	 */
	String TRUE_NODE = "true";

	/**
	 * Boolean constant false
	 */
	String FALSE_NODE = "false";

	/**
	 * Contents should represent a floating point number matching
	 * ("+" | "-")? d+ ("."d*)? ("E" ("+" | "-") d+)? where d is a digit 0-9.
	 */
	String REAL_NODE = "real";

	/**
	 * Contents should represent a (possibly signed) integer number in base 10
	 */
	String INTEGER_NODE = "integer";

	String DATE_FORMAT = "yyyy-MM-dd'T'HH:mm:ss'Z'";
}
