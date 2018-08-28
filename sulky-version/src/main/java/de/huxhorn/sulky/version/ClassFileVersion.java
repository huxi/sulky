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
	JAVA_1_6((char) 0x32, "6"),
	JAVA_1_7((char) 0x33, "7"),
	JAVA_1_8((char) 0x34, "8"),
	JAVA_9((char) 0x35, "9"),
	JAVA_10((char) 0x36, "10"),
	JAVA_11((char) 0x37, "11"),
	JAVA_12((char) 0x38, "12"),
	JAVA_13((char) 0x39, "13"),
	JAVA_14((char) 0x40, "14"),
	JAVA_15((char) 0x41, "15"),
	JAVA_16((char) 0x42, "16"),
	JAVA_17((char) 0x43, "17"),
	JAVA_18((char) 0x44, "18"),
	JAVA_19((char) 0x45, "19"),
	JAVA_20((char) 0x46, "20"),
	JAVA_21((char) 0x47, "21"),
	JAVA_22((char) 0x48, "22"),
	JAVA_23((char) 0x49, "23");

	private char majorVersionCharacter;
	private String sourceName;

	private static final Map<Character, ClassFileVersion> MAJOR_VERSION_MAPPING = new HashMap<Character, ClassFileVersion>();
	private static final Map<String, ClassFileVersion> SOURCE_NAME_MAPPING = new HashMap<String, ClassFileVersion>();

	static
	{
		ClassFileVersion[] values = ClassFileVersion.values();
		for (ClassFileVersion current : values)
		{
			MAJOR_VERSION_MAPPING.put(current.getMajorVersionCharacter(), current);
			SOURCE_NAME_MAPPING.put(current.getSourceName(), current);
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
		return MAJOR_VERSION_MAPPING.get(majorVersionNumber);
	}

	public static ClassFileVersion getBySourceName(String sourceName)
	{
		return SOURCE_NAME_MAPPING.get(sourceName);
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
