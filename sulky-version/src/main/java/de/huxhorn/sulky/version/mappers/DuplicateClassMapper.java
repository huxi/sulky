/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2018 Joern Huxhorn
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
 * Copyright 2007-2018 Joern Huxhorn
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

package de.huxhorn.sulky.version.mappers;

import de.huxhorn.sulky.version.ClassStatisticMapper;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

@SuppressWarnings("PMD.AvoidThrowingNullPointerException") // target is Java 1.6
public class DuplicateClassMapper
	implements ClassStatisticMapper
{
	@SuppressWarnings("PMD.UseDiamondOperator") // target is Java 1.6
	private final Map<ClassInfo, Set<String>> classSourceMapping=new HashMap<ClassInfo, Set<String>>();

	@SuppressWarnings("PMD.UseDiamondOperator") // target is Java 1.6
	private final Set<ClassInfo> duplicates = new TreeSet<ClassInfo>();

	@Override
	@SuppressWarnings("PMD.UseDiamondOperator") // target is Java 1.6
	public void evaluate(String source, String packageName, String className, char majorVersion)
	{
		if(source == null)
		{
			throw new NullPointerException("'source' must not be null!");
		}
		if(packageName == null)
		{
			throw new NullPointerException("'packageName' must not be null!");
		}
		if(className == null)
		{
			throw new NullPointerException("'className' must not be null!");
		}

		ClassInfo info = new ClassInfo(packageName, className);
		Set<String> sources = classSourceMapping.get(info);
		if(sources == null)
		{
			sources = new TreeSet<String>();
			classSourceMapping.put(info, sources);
		}
		sources.add(source);
		if(sources.size()>1)
		{
			duplicates.add(info);
		}
	}

	@Override
	public void reset()
	{
		classSourceMapping.clear();
		duplicates.clear();

	}

	public Map<ClassInfo, Set<String>> getClassSourceMapping()
	{
		return classSourceMapping;
	}

	public Set<ClassInfo> getDuplicates()
	{
		return duplicates;
	}

	public static class ClassInfo
		implements Comparable<ClassInfo>
	{
		private final String packageName;
		private final String className;

		public ClassInfo(String packageName, String className)
		{
			this.packageName = packageName;
			this.className = className;
		}

		public String getPackageName()
		{
			return packageName;
		}

		public String getClassName()
		{
			return className;
		}

		@Override
		public boolean equals(Object o)
		{
			if (this == o) return true;
			if (o == null || getClass() != o.getClass()) return false;

			ClassInfo classInfo = (ClassInfo) o;

			return (className != null ? className.equals(classInfo.className) : classInfo.className == null)
					&& (packageName != null ? packageName.equals(classInfo.packageName) : classInfo.packageName == null);
		}

		@Override
		public int hashCode()
		{
			int result = packageName != null ? packageName.hashCode() : 0;
			result = 31 * result + (className != null ? className.hashCode() : 0);
			return result;
		}

		@Override
		public String toString()
		{
			StringBuilder result=new StringBuilder();
			if(packageName != null && !"".equals(packageName))
			{
				result.append(packageName).append('.');
			}
			result.append(className);
			return result.toString();
		}

		@SuppressWarnings("StringEquality")
		@Override
		public int compareTo(ClassInfo o)
		{
			if(o == null)
			{
				throw new NullPointerException();
			}
			if(packageName != o.packageName)
			{
				if(packageName == null)
				{
					return -1;
				}
				if(o.packageName == null)
				{
					return 1;
				}
				int result = packageName.compareTo(o.packageName);
				if(result != 0)
				{
					return result;
				}
			}

			if(className != o.className)
			{
				if(className == null)
				{
					return -1;
				}
				if(o.className == null)
				{
					return 1;
				}
				return className.compareTo(o.className);
			}
			return 0;
		}
	}
}
