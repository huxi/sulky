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

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Version implementation according to <a href="http://semver.org/">Semantic Versioning 2.0.0</a>.
 *
 *
 */
@SuppressWarnings("PMD.AvoidThrowingNullPointerException") // target is Java 1.6
public class SemanticVersion
	implements Serializable, Comparable<SemanticVersion>
{
	private static final long serialVersionUID = -2336778957623151857L;

	private static final String VERSION_NUMBER_PATTERN_STRING = "((0|[1-9][0-9]*)\\.(0|[1-9][0-9]*)\\.(0|[1-9][0-9]*))";

	private static final String IDENTIFIER_ELEMENT_PATTERN_STRING = "([-a-zA-Z0-9]+)";

	private static final String IDENTIFIER_PATTERN_STRING = "(" + IDENTIFIER_ELEMENT_PATTERN_STRING + "(\\." + IDENTIFIER_ELEMENT_PATTERN_STRING + ")*)";


	private static final String SEMANTIC_VERSION_PATTERN_STRING =
			VERSION_NUMBER_PATTERN_STRING
			+ "(-" + IDENTIFIER_PATTERN_STRING + ")?"
			+ "(\\+" + IDENTIFIER_PATTERN_STRING + ")?";

	private static final Pattern SEMANTIC_VERSION_PATTERN = Pattern.compile(SEMANTIC_VERSION_PATTERN_STRING);
	private static final Pattern IDENTIFIER_ELEMENT_PATTERN = Pattern.compile(IDENTIFIER_ELEMENT_PATTERN_STRING);

	private static final int MAJOR_GROUP_INDEX = 2;
	private static final int MINOR_GROUP_INDEX = 3;
	private static final int PATCH_GROUP_INDEX = 4;

	private static final int PRE_RELEASE_GROUP_INDEX = 6;
	private static final int BUILD_METADATA_GROUP_INDEX = 11;
	private static final String[] EMPTY_STRING_ARRAY = new String[0];

	private final long major;
	private final long minor;
	private final long patch;
	private final String[] preRelease;
	private final String[] buildMetadata;
	private transient String versionString;

	public static SemanticVersion parse(String versionString)
	{
		if(versionString == null)
		{
			throw new NullPointerException("versionString must not be null!");
		}

		Matcher matcher = SEMANTIC_VERSION_PATTERN.matcher(versionString);
		if(!matcher.matches())
		{
			throw new IllegalArgumentException("'"+versionString+"' is not a valid semantic version!");
		}

		long major = Long.parseLong(matcher.group(MAJOR_GROUP_INDEX));
		long minor = Long.parseLong(matcher.group(MINOR_GROUP_INDEX));
		long patch = Long.parseLong(matcher.group(PATCH_GROUP_INDEX));

		String preReleaseGroup = matcher.group(PRE_RELEASE_GROUP_INDEX);
		String[] preRelease = EMPTY_STRING_ARRAY;
		if(preReleaseGroup != null)
		{
			preRelease = preReleaseGroup.split("\\.");
		}

		String buildMetadataGroup = matcher.group(BUILD_METADATA_GROUP_INDEX);
		String[] buildMetadata = EMPTY_STRING_ARRAY;
		if(buildMetadataGroup != null)
		{
			buildMetadata = buildMetadataGroup.split("\\.");
		}

		return new SemanticVersion(major, minor, patch, preRelease, buildMetadata);
	}

	public SemanticVersion(long major, long minor, long patch)
	{
		this(major, minor, patch, EMPTY_STRING_ARRAY, EMPTY_STRING_ARRAY);
	}

	public SemanticVersion(long major, long minor, long patch, String[] preRelease)
	{
		this(major, minor, patch, preRelease, EMPTY_STRING_ARRAY);
	}

	public SemanticVersion(long major, long minor, long patch, final String[] preRelease, final String[] buildMetadata)
	{
		if(major < 0)
		{
			throw new IllegalArgumentException("major must not be negative!");
		}
		if(minor < 0)
		{
			throw new IllegalArgumentException("minor must not be negative!");
		}
		if(patch < 0)
		{
			throw new IllegalArgumentException("patch must not be negative!");
		}

		String[] preReleaseCopy = preProcess(preRelease);
		for (String current : preReleaseCopy)
		{
			if(current == null)
			{
				throw new IllegalArgumentException("preRelease must not contain null!");
			}
			if(!IDENTIFIER_ELEMENT_PATTERN.matcher(current).matches())
			{
				throw new IllegalArgumentException("preRelease identifier '"+current+"' is invalid!");
			}
		}

		String[] buildMetadataCopy = preProcess(buildMetadata);
		for (String current : buildMetadataCopy)
		{
			if(current == null)
			{
				throw new IllegalArgumentException("buildMetadata must not contain null!");
			}
			if(!IDENTIFIER_ELEMENT_PATTERN.matcher(current).matches())
			{
				throw new IllegalArgumentException("buildMetadata identifier '"+current+"' is invalid!");
			}
		}

		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.preRelease = preReleaseCopy;
		this.buildMetadata = buildMetadataCopy;
	}

	private static String[] preProcess(String[] input) {
		if(input == null)
		{
			return EMPTY_STRING_ARRAY;
		}
		if(input.length == 0)
		{
			return EMPTY_STRING_ARRAY;
		}
		String[] newArray = new String[input.length];
		System.arraycopy(input, 0, newArray, 0, input.length);
		return newArray;
	}

	public long getMajor()
	{
		return major;
	}

	public long getMinor()
	{
		return minor;
	}

	public long getPatch()
	{
		return patch;
	}

	public List<String> getPreRelease()
	{
		return Arrays.asList(preRelease);
	}

	public List<String> getBuildMetadata()
	{
		return Arrays.asList(buildMetadata);
	}

	private String generateString()
	{
		StringBuilder result = new StringBuilder();
		result.append(major).append('.').append(minor).append('.').append(patch);
		if(preRelease.length > 0)
		{
			result.append('-');
			boolean first = true;
			for (String current : preRelease)
			{
				if(first)
				{
					first = false;
				}
				else
				{
					result.append('.');
				}
				result.append(current);
			}
		}
		if(buildMetadata.length > 0)
		{
			result.append('+');
			boolean first = true;
			for (String current : buildMetadata)
			{
				if(first)
				{
					first = false;
				}
				else
				{
					result.append('.');
				}
				result.append(current);
			}
		}
		return result.toString();
	}

	@Override
	public String toString()
	{
		if(versionString == null)
		{
			versionString = generateString();
		}
		return versionString;
	}

	public int compareTo(SemanticVersion other)
	{
		if(other == null)
		{
			throw new NullPointerException("other must not be null!");
		}
		if(major < other.major)
		{
			return -1;
		}
		if(major > other.major)
		{
			return 1;
		}

		if(minor < other.minor)
		{
			return -1;
		}
		if(minor > other.minor)
		{
			return 1;
		}

		if(patch < other.patch)
		{
			return -1;
		}
		if(patch > other.patch)
		{
			return 1;
		}
		if(preRelease.length == 0)
		{
			if(other.preRelease.length == 0)
			{
				return 0;
			}
			return 1;
		}
		if(other.preRelease.length == 0)
		{
			return -1;
		}

		int maxLength = Math.max(preRelease.length, other.preRelease.length);

		for(int i=0; i<maxLength; i++)
		{
			String a;
			if(i < preRelease.length)
			{
				a = preRelease[i];
			}
			else
			{
				return -1;
			}

			String b;
			if(i < other.preRelease.length)
			{
				b = other.preRelease[i];
			}
			else
			{
				return 1;
			}

			boolean aIsNumber = true;
			boolean bIsNumber = true;

			long aAsNumber = 0;
			long bAsNumber = 0;

			try
			{
				aAsNumber = Long.parseLong(a);
			}
			catch(NumberFormatException ex)
			{
				aIsNumber = false;
			}
			try
			{
				bAsNumber = Long.parseLong(b);
			}
			catch(NumberFormatException ex)
			{
				bIsNumber = false;
			}
			if(aIsNumber)
			{
				if(bIsNumber)
				{
					if(aAsNumber == bAsNumber)
					{
						continue;
					}
					if(aAsNumber < bAsNumber)
					{
						return -1;
					}
					return 1;
				}
				return -1;
			}
			if(bIsNumber)
			{
				return 1;
			}
			// neither a nor b are numbers
			int compared = a.compareTo(b);
			if(compared > 0)
			{
				return 1;
			}
			if(compared < 0)
			{
				return -1;
			}
		}

		return 0;
	}

	@SuppressWarnings("SimplifiableIfStatement")
	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		SemanticVersion that = (SemanticVersion) o;

		if (major != that.major) return false;
		if (minor != that.minor) return false;
		if (patch != that.patch) return false;
		return Arrays.equals(preRelease, that.preRelease) && Arrays.equals(buildMetadata, that.buildMetadata);
	}

	@Override
	public int hashCode()
	{
		int result = (int) (major ^ (major >>> 32));
		result = 31 * result + (int) (minor ^ (minor >>> 32));
		result = 31 * result + (int) (patch ^ (patch >>> 32));
		result = 31 * result + Arrays.hashCode(preRelease);
		result = 31 * result + Arrays.hashCode(buildMetadata);
		return result;
	}
}
