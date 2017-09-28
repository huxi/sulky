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

package de.huxhorn.sulky.version;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Java version according to JEP 223: New Version-String Scheme
 *
 * http://openjdk.java.net/jeps/223
 * https://bugs.openjdk.java.net/browse/JDK-8061493
 *
 * http://mail.openjdk.java.net/pipermail/verona-dev/2015-July/000071.html
 */
public class Jep223JavaVersion
	extends JavaVersion
	implements Comparable<Jep223JavaVersion>, Serializable
{
	private static final long serialVersionUID = 1757015785399443084L;
	/*
	A version number is a non-empty sequence of non-negative integer numerals,
	without leading zeroes, separated by period characters (U+002E); i.e.,
	it matches the regular expression [1-9][0-9]*(\.(0|[1-9][0-9]*))*.

	The sequence may be of arbitrary length but the first three elements are
	assigned specific meanings, as follows:

	$MAJOR.$MINOR.$SECURITY

	- $MAJOR
	  The major version number, incremented for a major release that contains
	  significant new features as specified in a new edition of the Java SE
	  Platform Specification, e.g., JSR 337 for Java SE 8. Features may be
	  removed in a major release, given advance notice at least one major
	  release ahead of time, and incompatible changes may be made when
	  justified. The $MAJOR version number of JDK 8 was 8; the $MAJOR version
	  number of JDK 9 will be 9.

	- $MINOR
	  The minor version number, incremented for a minor update release that
	  may contain compatible bug fixes, revisions to standard APIs mandated
	  by a Maintenance Release of the relevant Platform Specification, and
	  implementation features outside the scope of that Specification such
	  as new JDK-specific APIs, additional service providers, new garbage
	  collectors, and ports to new hardware architectures. $MINOR is
	  reset to zero when $MAJOR is incremented.

	- $SECURITY
	  The security level, incremented for a security-update release that
	  contains critical fixes including those necessary to improve security.
	  $SECURITY is reset to zero only when $MAJOR is incremented. A higher
	  value of $SECURITY for a given $MAJOR value, therefore, always indicates
	  a more secure release, regardless of the value of $MINOR.

	The fourth and later elements of a version number are free for use by
	downstream consumers of the JDK code base. Such a consumer may, e.g.,
	use the fourth element to identify patch releases which contain a small
	number of critical non-security fixes in addition to the security fixes
	in the corresponding security release.

	The sequence of numerals in a version number is compared to another such
	sequence in numerical, pointwise fashion; e.g., 9.9.1 is less than 9.10.0.
	If one sequence is shorter than another then the missing elements of the
	shorter sequence are considered to be zero; e.g., 9.1.2 is equal to
	9.1.2.0 but less than 9.1.2.1.
	*/
	private static final String VNUM_PATTERN_STRING = "(([1-9][0-9]*)(\\.(0|[1-9][0-9]*))*)";

	/*
	$PRE, matching ([a-zA-Z0-9]+)
	A pre-release identifier. Typically ea, for an early-access release
	that's under active development and potentially unstable, or internal,
	for an internal developer build.

	When comparing two version strings, a string with a pre-release
	identifier is always less than one with an equal $VNUM but no such
	identifier. Pre-release identifiers are compared numerically when they
	consist only of digits, and lexicographically otherwise.
	*/
	private static final String PRE_PATTERN_STRING = "([a-zA-Z0-9]+)";

	/*
	$BUILD, matching ([1-9][0-9]*)
	The build number, incremented for each promoted build. $BUILD is reset
	to one when any portion of $VNUM is incremented.

	When comparing two version strings the value of $BUILD, if present, is
	always ignored.
	*/
	private static final String BUILD_PATTERN_STRING = "([1-9][0-9]*)";


	/*
	$OPT, matching ([-a-zA-Z0-9\.]+)
	Additional build information, if desired. In the case of an internal
	build this will often contain the date and time of the build.

	When comparing two version strings the value of $OPT, if present, is
	always ignored.
	*/
	private static final String OPT_PATTERN_STRING = "([-a-zA-Z0-9\\.]+)";

	/*
	A version string $VSTR consists of a version number $VNUM, as described
	above, optionally followed by pre-release and build information, in the
	format

	$VNUM(-$PRE)?(\+$BUILD)?(-$OPT)?
	*/
	private static final String VSTR_PATTERN_STRING = VNUM_PATTERN_STRING + "(-" + PRE_PATTERN_STRING + ")?(\\+" + BUILD_PATTERN_STRING + ")?(-" + OPT_PATTERN_STRING + ")?";

	private static final Pattern VSTR_PATTERN = Pattern.compile(VSTR_PATTERN_STRING);
	private static final Pattern PRE_PATTERN = Pattern.compile(PRE_PATTERN_STRING);
	//private static final Pattern BUILD_PATTERN = Pattern.compile(BUILD_PATTERN_STRING);
	private static final Pattern OPT_PATTERN = Pattern.compile(OPT_PATTERN_STRING);


	private static final int VNUM_GROUP_INDEX = 1;
	private static final int PRE_GROUP_INDEX = 6;
	private static final int BUILD_GROUP_INDEX = 8;
	private static final int OPT_GROUP_INDEX = 10;
	private static final int MAJOR_INDEX = 0;
	private static final int MINOR_INDEX = 1;
	private static final int SECURITY_INDEX = 2;

	private final Integer[] versionNumbers;
	private final String preReleaseIdentifier;
	private final int buildNumber;
	private final String additionalBuildInformation;

	public static Jep223JavaVersion parse(String versionString)
	{
		if(versionString == null)
		{
			throw new NullPointerException("versionString must not be null!");
		}
		Matcher matcher = VSTR_PATTERN.matcher(versionString);
		if(!matcher.matches())
		{
			throw new IllegalArgumentException("versionString '"+versionString+"' is invalid.");
		}


		String vnum = matcher.group(VNUM_GROUP_INDEX);
		String pre = matcher.group(PRE_GROUP_INDEX);
		String build = matcher.group(BUILD_GROUP_INDEX);
		String opt = matcher.group(OPT_GROUP_INDEX);

		String[] vNumberStrings = vnum.split("\\.");
		Integer[] vNumbers=new Integer[vNumberStrings.length];
		for(int i=0;i<vNumberStrings.length;i++)
		{
			vNumbers[i] = Integer.parseInt(vNumberStrings[i]);
		}

		int buildNumber = 0;
		if(build != null)
		{
			buildNumber = Integer.parseInt(build);
		}

		return new Jep223JavaVersion(vNumbers, pre, buildNumber, opt);
	}

	private static Integer[] convert(int[] input)
	{
		if(input == null)
		{
			return null;
		}
		Integer[] result=new Integer[input.length];
		for(int i=0;i<input.length;i++)
		{
			result[i] = input[i];
		}
		return result;
	}

	public Jep223JavaVersion(int[] versionNumbers, String preReleaseIdentifier, int buildNumber, String additionalBuildInformation)
	{
		this(convert(versionNumbers), preReleaseIdentifier, buildNumber, additionalBuildInformation);
	}

	public Jep223JavaVersion(Integer[] versionNumbers, String preReleaseIdentifier, int buildNumber, String additionalBuildInformation)
	{
		if(versionNumbers == null)
		{
			throw new NullPointerException("versionNumbers must not be null!");
		}

		if(versionNumbers.length == 0)
		{
			throw new IllegalArgumentException("versionNumbers.length must not be zero!");
		}

		for (Integer current : versionNumbers)
		{
			if(current == null)
			{
				throw new IllegalArgumentException("versionNumbers must not contain null values!");
			}
		}

		if(buildNumber < 0)
		{
			throw new IllegalArgumentException("buildNumber must not be negative!");
		}

		if(preReleaseIdentifier != null)
		{
			Matcher matcher = PRE_PATTERN.matcher(preReleaseIdentifier);
			if(!matcher.matches())
			{
				throw new IllegalArgumentException("preReleaseIdentifier '"+ preReleaseIdentifier +"' is illegal. It doesn't match the pattern '"+PRE_PATTERN_STRING+"'.");
			}
		}

		if(additionalBuildInformation != null)
		{
			Matcher matcher = OPT_PATTERN.matcher(additionalBuildInformation);
			if(!matcher.matches())
			{
				throw new IllegalArgumentException("additionalBuildInformation '"+ additionalBuildInformation +"' is illegal. It doesn't match the pattern '"+OPT_PATTERN_STRING+"'.");
			}
		}

		Integer[] versionNumberCopy = new Integer[versionNumbers.length];
		System.arraycopy(versionNumbers, 0, versionNumberCopy, 0, versionNumbers.length);
		this.versionNumbers = versionNumberCopy;
		this.preReleaseIdentifier = preReleaseIdentifier;
		this.buildNumber = buildNumber;
		this.additionalBuildInformation = additionalBuildInformation;
	}

	public List<Integer> getVersionNumbers()
	{
		return Arrays.asList(versionNumbers);
	}

	/**
	 * The major version number, incremented for a major release that contains
	 * significant new features as specified in a new edition of the Java SE
	 * Platform Specification, e.g., JSR 337 for Java SE 8. Features may be
	 * removed in a major release, given advance notice at least one major
	 * release ahead of time, and incompatible changes may be made when
	 * justified. The $MAJOR version number of JDK 8 was 8; the $MAJOR version
	 * number of JDK 9 will be 9.
	 *
	 * @return the major version number.
	 */
	public int getMajor()
	{
		return versionNumbers[MAJOR_INDEX];
	}

	/**
	 * The minor version number, incremented for a minor update release that
	 * may contain compatible bug fixes, revisions to standard APIs mandated
	 * by a Maintenance Release of the relevant Platform Specification, and
	 * implementation features outside the scope of that Specification such
	 * as new JDK-specific APIs, additional service providers, new garbage
	 * collectors, and ports to new hardware architectures. $MINOR is reset
	 * to zero when $MAJOR is incremented.
	 *
	 * @return the minor version number.
	 */
	public int getMinor()
	{
		if(versionNumbers.length > MINOR_INDEX)
		{
			return versionNumbers[MINOR_INDEX];
		}
		return 0;
	}

	@Override
	public int getPatch()
	{
		return getSecurity();
	}


	/**
	 * The security level, incremented for a security-update release that
	 * contains critical fixes including those necessary to improve security.
	 * $SECURITY is reset to zero only when $MAJOR is incremented. A higher
	 * value of $SECURITY for a given $MAJOR value, therefore, always
	 * indicates a more secure release, regardless of the value of $MINOR.
	 *
	 * @return the security level.
	 */
	public int getSecurity()
	{
		if(versionNumbers.length > SECURITY_INDEX)
		{
			return versionNumbers[SECURITY_INDEX];
		}
		return 0;
	}

	/**
	 * A pre-release identifier. Typically ea, for an early-access release
	 * that's under active development and potentially unstable, or internal,
	 * for an internal developer build.
	 *
	 * When comparing two version strings, a string with a pre-release
	 * identifier is always less than one with an equal $VNUM but no such
	 * identifier. Pre-release identifiers are compared numerically when
	 * they consist only of digits, and lexicographically otherwise.
	 */
	@Override
	public String getPreReleaseIdentifier()
	{
		return preReleaseIdentifier;
	}

	/**
	 * The build number, incremented for each promoted build. $BUILD is reset
	 * to one when any portion of $VNUM is incremented.
	 *
	 * When comparing two version strings the value of $BUILD, if present,
	 * is always ignored.
	 *
	 * @return the build number.
	 */
	public int getBuildNumber()
	{
		return buildNumber;
	}

	/**
	 * Additional build information, if desired. In the case of an internal
	 * build this will often contain the date and time of the build.
	 *
	 * When comparing two version strings the value of $OPT, if present, is
	 * always ignored.
	 *
	 * @return additional build information, if available.
	 */
	public String getAdditionalBuildInformation()
	{
		return additionalBuildInformation;
	}

	@SuppressWarnings("SimplifiableIfStatement")
	@Override
	public boolean equals(Object o)
	{
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;

		Jep223JavaVersion that = (Jep223JavaVersion) o;

		if (!Arrays.equals(versionNumbers, that.versionNumbers)) return false;
		if (preReleaseIdentifier != null ? !preReleaseIdentifier.equals(that.preReleaseIdentifier) : that.preReleaseIdentifier != null) return false;
		if (buildNumber != that.buildNumber) return false;
		return !(additionalBuildInformation != null ? !additionalBuildInformation.equals(that.additionalBuildInformation) : that.additionalBuildInformation != null);

	}

	@Override
	public int hashCode()
	{
		int result = Arrays.hashCode(versionNumbers);
		result = 31 * result + (preReleaseIdentifier != null ? preReleaseIdentifier.hashCode() : 0);
		result = 31 * result + buildNumber;
		result = 31 * result + (additionalBuildInformation != null ? additionalBuildInformation.hashCode() : 0);
		return result;
	}

	@Override
	public String toString()
	{
		return "Jep223JavaVersion{" +
				"versionNumbers=" + Arrays.toString(versionNumbers) +
				", preReleaseIdentifier='" + preReleaseIdentifier + '\'' +
				", buildNumber=" + buildNumber +
				", additionalBuildInformation='" + additionalBuildInformation + '\'' +
				'}';
	}

	/**
	 * @return a string representation of the object represented by a long version string.
	 */
	@Override
	public String toVersionString()
	{
		// '9.7.6.5.4.3.2.1-ea+49-additionalBuildInformation'
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (Integer current : versionNumbers)
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
		if(preReleaseIdentifier != null)
		{
			result.append('-').append(preReleaseIdentifier);
		}
		if(buildNumber > 0)
		{
			result.append('+').append(buildNumber);
		}
		if(additionalBuildInformation != null)
		{
			result.append('-').append(additionalBuildInformation);
		}

		return result.toString();
	}

	/**
	 * A short version string, often useful in less formal contexts, is simply
	 * $MAJOR.$MINOR.$SECURITY with trailing zero elements omitted; i.e.,
	 * $SECURITY is omitted if it has the value zero, and $MINOR is omitted
	 * if both $MINOR and $SECURITY have the value zero. A short version
	 * string may optionally end with -$PRE.
	 *
	 * @return the short version string.
	 */
	public String toShortVersionString()
	{
		StringBuilder result = new StringBuilder();
		result.append(getMajor());
		int minor = getMinor();
		int security = getSecurity();
		if(minor != 0 || security != 0)
		{
			result.append('.').append(minor);
			if(security != 0)
			{
				result.append('.').append(security);
			}
		}
		if(preReleaseIdentifier != null)
		{
			result.append('-').append(preReleaseIdentifier);
		}

		return result.toString();
	}

	@Override
	public Jep223JavaVersion withoutPreReleaseIdentifier()
	{
		if(preReleaseIdentifier == null)
		{
			return this;
		}
		return new Jep223JavaVersion(versionNumbers, null, buildNumber, additionalBuildInformation);
	}

	@Override
	public int compareTo(Jep223JavaVersion other)
	{
		if(other == null)
		{
			throw new NullPointerException("other must not be null!");
		}
		Integer[] thisVersionNumbers = versionNumbers;
		Integer[] otherVersionNumbers = other.versionNumbers;
		int maxLength = thisVersionNumbers.length;
		if(maxLength < otherVersionNumbers.length)
		{
			maxLength = otherVersionNumbers.length;
		}
		for(int i=0;i<maxLength;i++)
		{
			int thisCurrent = 0;
			int otherCurrent = 0;
			if(i<thisVersionNumbers.length)
			{
				thisCurrent = thisVersionNumbers[i];
			}
			if(i<otherVersionNumbers.length)
			{
				otherCurrent = otherVersionNumbers[i];
			}
			if(thisCurrent > otherCurrent)
			{
				return 1;
			}
			if(thisCurrent < otherCurrent)
			{
				return -1;
			}
		}

		// When comparing two version strings, a string with a pre-release
		// identifier is always less than one with an equal $VNUM but no such
		// identifier. Pre-release identifiers are compared numerically when
		// they consist only of digits, and lexicographically otherwise.
		//
		// In contrast to the specification as of 2015-08-06,
		// preReleaseIdentifier is always compared lexicographically for now.
		// see http://mail.openjdk.java.net/pipermail/verona-dev/2015-July/000071.html
		if(preReleaseIdentifier == null)
		{
			if(other.preReleaseIdentifier != null)
			{
				return 1;
			}
			return 0;
		}
		if(other.preReleaseIdentifier == null)
		{
			return -1;
		}

		// neither preReleaseIdentifier nor o.preReleaseIdentifier are null
		/*
		if(preReleaseIdentifier.equals(other.preReleaseIdentifier))
		{
			return 0;
		}

		int value;
		try
		{
			int thisPreAsInt = Integer.parseInt(preReleaseIdentifier);
			int otherPreAsInt = Integer.parseInt(other.preReleaseIdentifier);
			// compare numerically
			value = thisPreAsInt - otherPreAsInt;
		}
		catch(NumberFormatException ex)
		{
			// compare lexicographically
			value = preReleaseIdentifier.compareTo(other.preReleaseIdentifier);
		}
		if(value < 0)
		{
			return -1;
		}
		if(value > 0)
		{
			return 1;
		}

		return 0;
		*/
		int value=preReleaseIdentifier.compareTo(other.preReleaseIdentifier);
		if(value < 0)
		{
			return -1;
		}
		if(value > 0)
		{
			return 1;
		}

		return 0;
	}
}
