/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2014 Joern Huxhorn
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
 * Copyright 2007-2014 Joern Huxhorn
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

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * This class handles parsing and comparison of
 * <a href="http://www.oracle.com/technetwork/java/javase/versioning-naming-139433.html">Java version numbers</a>.
 *
 * The static JVM attribute contains the version retrieved from the "java.version" system property.
 * If parsing that property fails (because security prevents access or the content is invalid) then
 * "java.specification.version" is used as a fallback. If parsing that property also fails (for similar reasons)
 * then the JVM attribute is initialized with MIN_VALUE, i.e. new JavaVersion(0,0,0,0,"!").
 */
public class JavaVersion
	implements Comparable<JavaVersion>
{
	private static final Pattern VERSION_PATTERN = Pattern.compile("(\\d+)\\.(\\d+)(\\.(\\d+)([_](\\d+))?)?(-(.+))?");

	private static final int HUGE_GROUP_INDEX  = 1;
	private static final int MAJOR_GROUP_INDEX = 2;
	private static final int MINOR_GROUP_INDEX = 4;
	private static final int PATCH_GROUP_INDEX = 6;
	private static final int IDENTIFIER_GROUP_INDEX = 8;

	/*
	 e.g. 1.8.0_25
	 */
	private static final String JAVA_VERSION_PROPERTY_NAME = "java.version";

	/*
	 e.g. 1.8
	 */
	private static final String JAVA_SPECIFICATION_VERSION_PROPERTY_NAME = "java.specification.version";

	/**
	 * Smallest possible version is JavaVersion(0,0,0,0,"!").
	 */
	public static final JavaVersion MIN_VALUE = new JavaVersion(0,0,0,0,"!");

	/**
	 * The best possible approximation to the JVM JavaVersion.
	 *
	 * This can
	 */
	public static final JavaVersion JVM;

	static
	{
		JVM=getSystemJavaVersion();
	}

	static JavaVersion getSystemJavaVersion() {
		JavaVersion version=null;
		try
		{
			String versionString = System.getProperty(JAVA_VERSION_PROPERTY_NAME);
			if(versionString != null)
			{
				version = parse(versionString);
			}
		}
		catch(SecurityException ex)
		{
			// ignore
		}
		catch(IllegalArgumentException ex)
		{
			// didn't parse. Probably some strangeness like 1.8.0_25.1
		}

		if(version == null)
		{
			// either SecurityException or missing/broken standard property
			// fall back to specification version
			try
			{
				String versionString = System.getProperty(JAVA_SPECIFICATION_VERSION_PROPERTY_NAME);
				if(versionString != null)
				{
					version = parse(versionString);
				}
			}
			catch(SecurityException ex)
			{
				// ignore
			}
			catch(IllegalArgumentException ex)
			{
				// didn't parse.
			}
		}
		if(version != null)
		{
			return version;
		}
		return MIN_VALUE;
	}

	/**
	 * Parses a Java version and returns the corresponding JavaVersion instance.
	 *
	 * @param versionString the String to be parsed
	 * @return the JavaVersion corresponding to the given versionString
	 * @throws java.lang.IllegalArgumentException if versionString is null or invalid.
	 */
	public static JavaVersion parse(String versionString)
	{
		if(versionString == null)
		{
			throw new IllegalArgumentException("versionString must not be null!");
		}
		Matcher matcher = VERSION_PATTERN.matcher(versionString);
		if(!matcher.matches())
		{
			throw new IllegalArgumentException("versionString '"+versionString+"' is invalid.");
		}

		/*
		for (int i=0; i<=matcher.groupCount(); i++)
		{
			System.out.println("Index #"+i+": "+matcher.group(i));
		}
		*/

		int huge = Integer.parseInt(matcher.group(HUGE_GROUP_INDEX));
		int major = Integer.parseInt(matcher.group(MAJOR_GROUP_INDEX));
		int minor = 0;
		int patch = 0;

		String minorString = matcher.group(MINOR_GROUP_INDEX);
		if(minorString != null)
		{
			minor = Integer.parseInt(minorString);
		}
		String patchString = matcher.group(PATCH_GROUP_INDEX);
		if(patchString != null)
		{
			patch = Integer.parseInt(patchString);
		}

		String identifier = matcher.group(IDENTIFIER_GROUP_INDEX);
		return new JavaVersion(huge, major, minor, patch, identifier);
	}

	/**
	 * Returns true, if the JVM version is bigger or equals to the given versionString.
	 *
	 * This is a convenience method that is simply a shortcut for
	 * (JVM.compareTo(parse(versionString)) &gt;= 0).
	 *
	 * @param versionString the version to compare with the JVM version.
	 * @return true, if the JVM version is bigger or equals to the given versionString.
	 * @throws java.lang.IllegalArgumentException if versionString is null or invalid.
	 */
	public static boolean isAtLeast(String versionString)
	{
		return JVM.compareTo(parse(versionString)) >= 0;
	}

	private final int huge;
	private final int major;
	private final int minor;
	private final int patch;
	private final String identifier;

	/**
	 * Creates a JavaVersion.
	 *
	 * @param huge the "huge" part of the version.
	 * @param major the "major" part of the version.
	 * @throws IllegalArgumentException if huge or major are negative.
	 */
	public JavaVersion(int huge, int major)
	{
		this(huge, major, 0, 0, null);
	}

	/**
	 * Creates a JavaVersion.
	 *
	 * @param huge the "huge" part of the version.
	 * @param major the "major" part of the version.
	 * @param minor the "minor" part of the version.
	 * @throws IllegalArgumentException if huge, major or minor are negative.
	 */
	public JavaVersion(int huge, int major, int minor)
	{
		this(huge, major, minor, 0, null);
	}

	/**
	 * Creates a JavaVersion.
	 *
	 * @param huge the "huge" part of the version.
	 * @param major the "major" part of the version.
	 * @param minor the "minor" part of the version.
	 * @param patch the "patch" part of the version.
	 * @throws IllegalArgumentException if huge, major, minor or patch are negative.
	 */
	public JavaVersion(int huge, int major, int minor, int patch)
	{
		this(huge, major, minor, patch, null);
	}

	/**
	 * Creates a JavaVersion.
	 *
	 * @param huge the "huge" part of the version.
	 * @param major the "major" part of the version.
	 * @param minor the "minor" part of the version.
	 * @param patch the "patch" part of the version.
	 * @param identifier the "identifier" part of the version.
	 * @throws IllegalArgumentException if huge, major, minor or patch are negative or if identifier is invalid.
	 */
	public JavaVersion(int huge, int major, int minor, int patch, String identifier)
	{
		if(huge < 0)
		{
			throw new IllegalArgumentException("huge must not be negative!");
		}
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
		if(identifier != null)
		{
			identifier = identifier.trim();
			if(identifier.length() == 0)
			{
				throw new IllegalArgumentException("identifier must not be empty string!");
			}
			if(identifier.indexOf('*') != -1)
			{
				throw new IllegalArgumentException("identifier must not contain the '*' character!");
			}
			if(identifier.indexOf('+') != -1)
			{
				throw new IllegalArgumentException("identifier must not contain the '+' character!");
			}
		}

		this.huge = huge;
		this.major = major;
		this.minor = minor;
		this.patch = patch;
		this.identifier = identifier;
	}

	/**
	 * Returns the "huge" part of this version, e.g. 1 in case of 1.8.0_25.
	 *
	 * @return the "huge" part of this version.
	 */
	public int getHuge()
	{
		return huge;
	}

	/**
	 * Returns the "major" part of this version, e.g. 8 in case of 1.8.0_25.
	 *
	 * @return the "major" part of this version.
	 */
	public int getMajor()
	{
		return major;
	}

	/**
	 * Returns the "minor" part of this version, e.g. 0 in case of 1.8.0_25.
	 *
	 * @return the "minor" part of this version.
	 */
	public int getMinor()
	{
		return minor;
	}

	/**
	 * Returns the "patch" (or update) part of this version, e.g. 25 in case of 1.8.0_25.
	 *
	 * @return the "patch" (or update) part of this version.
	 */
	public int getPatch()
	{
		return patch;
	}

	/**
	 * Returns the "identifier" part of this version, e.g. "ea" in case of 1.8.0_25-ea.
	 *
	 * @return the "identifier" part of this version.
	 */
	public String getIdentifier()
	{
		return identifier;
	}

	/**
	 * Returns the version string of this version, e.g. "1.8.0_25-ea" in case of JavaVersion(1,8,0,25,"ea").
	 *
	 * @return the version string of this version.
	 */
	public String toVersionString()
	{
		StringBuilder result = new StringBuilder();
		result.append(huge).append('.').append(major).append('.').append(minor);
		if(patch != 0)
		{
			result.append('_');
			if(patch < 10)
			{
				result.append('0');
			}
			result.append(patch);
		}
		if(identifier != null)
		{
			result.append('-');
			result.append(identifier);
		}
		return result.toString();
	}

	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		JavaVersion that = (JavaVersion) o;

		if (huge != that.huge) return false;
		if (major != that.major) return false;
		if (minor != that.minor) return false;
		if (patch != that.patch) return false;
		if (identifier != null ? !identifier.equals(that.identifier) : that.identifier != null) return false;

		return true;
	}

	@Override
	public int hashCode()
	{
		int result = huge;
		result = 31 * result + major;
		result = 31 * result + minor;
		result = 31 * result + patch;
		result = 31 * result + (identifier != null ? identifier.hashCode() : 0);
		return result;
	}

	@Override
	public String toString()
	{
		StringBuilder result = new StringBuilder();

		result.append("JavaVersion{huge=").append(huge).append(", major=").append(major);
		result.append(", minor=").append(minor).append(", patch=").append(patch);

		result.append(", identifier=");
		if(identifier == null)
		{
			result.append("null");
		}
		else
		{
			result.append('"').append(identifier).append('"');
		}

		result.append('}');

		return result.toString();
	}

	@SuppressWarnings("NullableProblems")
	@Override
	public int compareTo(JavaVersion other)
	{
		if(other == null)
		{
			throw new NullPointerException("other must not be null!");
		}
		if(huge < other.huge)
		{
			return -1;
		}
		if(huge > other.huge)
		{
			return 1;
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

		if(identifier == null)
		{
			// this is a release
			if(other.identifier != null)
			{
				// other is rc/ea, i.e. non-GA/non-FCS => this is greater.
				return 1;
			}
			return 0;
		}
		// this is rc/ea, i.e. non-GA/non-FCS.
		if(other.identifier == null)
		{
			// other is a release => other is greater.
			return -1;
		}

		// both this and other are rc/ea, i.e. non-GA/non-FCS.
		// the code below is only an approximation.
		return identifier.compareTo(other.identifier);
	}
}
