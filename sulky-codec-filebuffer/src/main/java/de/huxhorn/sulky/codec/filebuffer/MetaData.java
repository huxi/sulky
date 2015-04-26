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

package de.huxhorn.sulky.codec.filebuffer;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Immutable class representing the meta-data of a CodecFileBuffer.
 */
public final class MetaData
	implements Serializable, Cloneable
{
	private static final long serialVersionUID = 8435511960228370781L;

	private final Map<String, String> data;
	private final boolean sparse;

	public MetaData(Map<String, String> data)
	{
		this(data, false);
	}

	public MetaData(boolean sparse)
	{
		this(null, sparse);
	}

	public MetaData(Map<String, String> data, boolean sparse)
	{
		if(data == null)
		{
			data = Collections.emptyMap();
		}
		else
		{
			data = new HashMap<>(data);
		}
		this.data = data;
		this.sparse = sparse;
	}

	public boolean isSparse()
	{
		return sparse;
	}

	@Override
	public boolean equals(Object o)
	{
		if(this == o) return true;
		if(o == null || getClass() != o.getClass()) return false;

		MetaData metaData = (MetaData) o;

		if(this.sparse != metaData.sparse) return false;
		if(!data.equals(metaData.data)) return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		return data.hashCode();
	}

	/**
	 * @return an unmodifiable map of meta data.
	 */
	public Map<String, String> getData()
	{
		return Collections.unmodifiableMap(data);
	}

	@Override
	public String toString()
	{
		return "MetaData[sparse=" + sparse + ", data=" + data + "]";
	}

	public MetaData clone()
		throws CloneNotSupportedException
	{
		return (MetaData) super.clone();
		// not necessary to clone data because it's private and immutable.
	}
}
