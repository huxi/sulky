/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2015 Joern Huxhorn
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
 * Copyright 2007-2015 Joern Huxhorn
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

package de.huxhorn.sulky.version;

import java.util.HashMap;
import java.util.Map;

public enum ClassFileVersion
{
	JAVA_1_1((char) 0x2D, "1.1"),
	JAVA_1_2((char) 0x2E, "1.2"),
	JAVA_1_3((char) 0x2F, "1.3"),
	JAVA_1_4((char) 0x30, "1.4"),
	JAVA_1_5((char) 0x31, "1.5"),
	JAVA_1_6((char) 0x32, "1.6"),
	JAVA_1_7((char) 0x33, "1.7"),
	JAVA_1_8((char) 0x34, "1.8");
	// JAVA_1_9((char) 0x35, "1.9"); ??

	private char majorVersionCharacter;
	private String sourceName;

	private static final Map<Character, ClassFileVersion> charMapping = new HashMap<Character, ClassFileVersion>();
	private static final Map<String, ClassFileVersion> sourceNameMapping = new HashMap<String, ClassFileVersion>();

	static
	{
		ClassFileVersion values[] = ClassFileVersion.values();
		for (ClassFileVersion current : values)
		{
			charMapping.put(current.getMajorVersionCharacter(), current);
			sourceNameMapping.put(current.getSourceName(), current);
		}
	}

	ClassFileVersion(char major, String sourceName)
	{
		this.majorVersionCharacter = major;
		this.sourceName = sourceName;
	}

	public char getMajorVersionCharacter()
	{
		return majorVersionCharacter;
	}

	public String getSourceName()
	{
		return sourceName;
	}

	public static ClassFileVersion getByMajorVersionChar(char majorVersionNumber)
	{
		return charMapping.get(majorVersionNumber);
	}

	public static ClassFileVersion getBySourceName(String sourceName)
	{
		return sourceNameMapping.get(sourceName);
	}

	@Override
	public String toString()
	{
		return "ClassFileVersion{" +
				"sourceName='" + sourceName + '\'' +
				", majorVersionCharacter=0x" + Integer.toHexString(majorVersionCharacter) +
				'}';
	}
}
