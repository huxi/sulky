/*
 * sulky-resources - inheritance-safe class resources.
 * Copyright (C) 2002-2008 Joern Huxhorn
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
package de.huxhorn.sulky.resources;

import java.util.Locale;

/**
 * This interface is used for Proxy-creation in sulky-resources and should have been
 * part of J2SDK anyway.
 */
public interface Localizable
{
	void setLocale(Locale l);

	Locale getLocale();
}
