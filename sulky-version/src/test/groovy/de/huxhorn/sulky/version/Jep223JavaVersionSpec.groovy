/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2019 Joern Huxhorn
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
 * Copyright 2007-2019 Joern Huxhorn
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

package de.huxhorn.sulky.version

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

@Subject(Jep223JavaVersion)
class Jep223JavaVersionSpec extends Specification {

/*
                      Existing                Proposed
Release Type    long           short    long           short
------------    --------------------    --------------------
Early Access    1.9.0-ea-b19    9-ea    9.0.0-ea+19    9-ea
Major           1.9.0-b100      9       9.0.0+100      9
Security #1     1.9.0_5-b20     9u5     9.0.1+20       9.0.1
Security #2     1.9.0_11-b12    9u11    9.0.2+12       9.0.2
Minor #1        1.9.0_20-b62    9u20    9.1.2+62       9.1.2
Security #3     1.9.0_25-b15    9u25    9.1.3+15       9.1.3
Security #4     1.9.0_31-b08    9u31    9.1.4+8        9.1.4
Minor #2        1.9.0_40-b45    9u40    9.2.4+45       9.2.4

For reference, this table shows version strings in the new format as they would have been used, hypothetically, for some JDK 7 update and security releases:

                          Actual               Hypothetical
Release Type        long           short    long          short
------------        --------------------    -------------------
Security 2013/04    1.7.0_21-b11    7u21    7.4.10+11    7.4.10
Security 2013/06    1.7.0_25-b15    7u25    7.4.11+15    7.4.11
Minor    2013/09    1.7.0_40-b43    7u40    7.5.11+43    7.5.11
Security 2013/10    1.7.0_45-b18    7u45    7.5.12+18    7.5.12
Security 2014/01    1.7.0_51-b13    7u51    7.5.13+13    7.5.13
Security 2014/04    1.7.0_55-b13    7u55    7.5.14+13    7.5.14
Minor    2014/05    1.7.0_60-b19    7u60    7.6.14+19    7.6.14
Security 2014/07    1.7.0_65-b20    7u65    7.6.15+20    7.6.15
*/

	@Unroll('Jep223JavaVersion.matches(#versionString) matches. (#description)')
	isPatternMatching(String description, String versionString, Jep223JavaVersion expectedVersion) {
		when:
		Jep223JavaVersion parsed = Jep223JavaVersion.parse(versionString)

		then:
		parsed == expectedVersion

		where:
		description           | versionString               | expectedVersion
		'Early Access'        | '9.0.0-ea+19'               | new Jep223JavaVersion([9, 0, 0] as int[], 'ea', 19, null)
		'Major'               | '9.0.0+100'                 | new Jep223JavaVersion([9, 0, 0] as int[], null, 100, null)
		'Security #1'         | '9.0.1+20'                  | new Jep223JavaVersion([9, 0, 1] as int[], null, 20, null)
		'Security #2'         | '9.0.2+12'                  | new Jep223JavaVersion([9, 0, 2] as int[], null, 12, null)
		'Minor #1'            | '9.1.2+62'                  | new Jep223JavaVersion([9, 1, 2] as int[], null, 62, null)
		'Security #3'         | '9.1.3+15'                  | new Jep223JavaVersion([9, 1, 3] as int[], null, 15, null)
		'Security #4'         | '9.1.4+8'                   | new Jep223JavaVersion([9, 1, 4] as int[], null, 8, null)
		'Minor #2'            | '9.2.4+45'                  | new Jep223JavaVersion([9, 2, 4] as int[], null, 45, null)

		'Early Access'        | '9-ea'                      | new Jep223JavaVersion([9] as int[], 'ea', 0, null)
		'Major'               | '9'                         | new Jep223JavaVersion([9] as int[], null, 0, null)
		'Security #1'         | '9.0.1'                     | new Jep223JavaVersion([9, 0, 1] as int[], null, 0, null)
		'Security #2'         | '9.0.2'                     | new Jep223JavaVersion([9, 0, 2] as int[], null, 0, null)
		'Minor #1'            | '9.1.2'                     | new Jep223JavaVersion([9, 1, 2] as int[], null, 0, null)
		'Security #3'         | '9.1.3'                     | new Jep223JavaVersion([9, 1, 3] as int[], null, 0, null)
		'Security #4'         | '9.1.4'                     | new Jep223JavaVersion([9, 1, 4] as int[], null, 0, null)
		'Minor #2'            | '9.2.4'                     | new Jep223JavaVersion([9, 2, 4] as int[], null, 0, null)

		'Security 2013/04'    | '7.4.10+11'                 | new Jep223JavaVersion([7, 4, 10] as int[], null, 11, null)
		'Security 2013/06'    | '7.4.11+15'                 | new Jep223JavaVersion([7, 4, 11] as int[], null, 15, null)
		'Minor    2013/09'    | '7.5.11+43'                 | new Jep223JavaVersion([7, 5, 11] as int[], null, 43, null)
		'Security 2013/10'    | '7.5.12+18'                 | new Jep223JavaVersion([7, 5, 12] as int[], null, 18, null)
		'Security 2014/01'    | '7.5.13+13'                 | new Jep223JavaVersion([7, 5, 13] as int[], null, 13, null)
		'Security 2014/04'    | '7.6.14+13'                 | new Jep223JavaVersion([7, 6, 14] as int[], null, 13, null)
		'Minor    2014/05'    | '7.6.14+19'                 | new Jep223JavaVersion([7, 6, 14] as int[], null, 19, null)
		'Security 2014/07'    | '7.6.15+20'                 | new Jep223JavaVersion([7, 6, 15] as int[], null, 20, null)

		'Security 2013/04'    | '7.4.10'                    | new Jep223JavaVersion([7, 4, 10] as int[], null, 0, null)
		'Security 2013/06'    | '7.4.11'                    | new Jep223JavaVersion([7, 4, 11] as int[], null, 0, null)
		'Minor    2013/09'    | '7.5.11'                    | new Jep223JavaVersion([7, 5, 11] as int[], null, 0, null)
		'Security 2013/10'    | '7.5.12'                    | new Jep223JavaVersion([7, 5, 12] as int[], null, 0, null)
		'Security 2014/01'    | '7.5.13'                    | new Jep223JavaVersion([7, 5, 13] as int[], null, 0, null)
		'Security 2014/04'    | '7.5.14'                    | new Jep223JavaVersion([7, 5, 14] as int[], null, 0, null)
		'Minor    2014/05'    | '7.6.14'                    | new Jep223JavaVersion([7, 6, 14] as int[], null, 0, null)
		'Security 2014/07'    | '7.6.15'                    | new Jep223JavaVersion([7, 6, 15] as int[], null, 0, null)

		'Whatever'            | '9.7.6.5.4.3'               | new Jep223JavaVersion([9, 7, 6, 5, 4, 3] as int[], null, 0, null)
		'withOpt'             | '9.7.6.5.4-ea-opt'          | new Jep223JavaVersion([9, 7, 6, 5, 4] as int[], 'ea', 0, 'opt')
		'full'                | '9.7.6.5.4.3.2.1-ea+49-opt' | new Jep223JavaVersion([9, 7, 6, 5, 4, 3, 2, 1] as int[], 'ea', 49, 'opt')

		'jep-322'             | '11.0.2+13-LTS'             | new Jep223JavaVersion([11, 0, 2] as int[], null, 13, 'LTS')
		'jep-322 (emergency)' | '11.0.2.17+13-LTS'          | new Jep223JavaVersion([11, 0, 2, 17] as int[], null, 13, 'LTS')
	}


	@Unroll('Jep223JavaVersion.toVersionString() is #expectedVersionString. (#description)')
	longString(String description, String expectedVersionString, Jep223JavaVersion version) {
		when:
		def versionString = version.toVersionString()

		then:
		versionString == expectedVersionString

		where:
		description           | expectedVersionString                         | version
		'Early Access'        | '9.0.0-ea+19'                                 | new Jep223JavaVersion([9, 0, 0] as int[], 'ea', 19, null)
		'Major'               | '9.0.0+100'                                   | new Jep223JavaVersion([9, 0, 0] as int[], null, 100, null)
		'Security #1'         | '9.0.1+20'                                    | new Jep223JavaVersion([9, 0, 1] as int[], null, 20, null)
		'Security #2'         | '9.0.2+12'                                    | new Jep223JavaVersion([9, 0, 2] as int[], null, 12, null)
		'Minor #1'            | '9.1.2+62'                                    | new Jep223JavaVersion([9, 1, 2] as int[], null, 62, null)
		'Security #3'         | '9.1.3+15'                                    | new Jep223JavaVersion([9, 1, 3] as int[], null, 15, null)
		'Security #4'         | '9.1.4+8'                                     | new Jep223JavaVersion([9, 1, 4] as int[], null, 8, null)
		'Minor #2'            | '9.2.4+45'                                    | new Jep223JavaVersion([9, 2, 4] as int[], null, 45, null)

		'Early Access'        | '9-ea'                                        | new Jep223JavaVersion([9] as int[], 'ea', 0, null)
		'Major'               | '9'                                           | new Jep223JavaVersion([9] as int[], null, 0, null)
		'Security #1'         | '9.0.1'                                       | new Jep223JavaVersion([9, 0, 1] as int[], null, 0, null)
		'Security #2'         | '9.0.2'                                       | new Jep223JavaVersion([9, 0, 2] as int[], null, 0, null)
		'Minor #1'            | '9.1.2'                                       | new Jep223JavaVersion([9, 1, 2] as int[], null, 0, null)
		'Security #3'         | '9.1.3'                                       | new Jep223JavaVersion([9, 1, 3] as int[], null, 0, null)
		'Security #4'         | '9.1.4'                                       | new Jep223JavaVersion([9, 1, 4] as int[], null, 0, null)
		'Minor #2'            | '9.2.4'                                       | new Jep223JavaVersion([9, 2, 4] as int[], null, 0, null)

		'Security 2013/04'    | '7.4.10+11'                                   | new Jep223JavaVersion([7, 4, 10] as int[], null, 11, null)
		'Security 2013/06'    | '7.4.11+15'                                   | new Jep223JavaVersion([7, 4, 11] as int[], null, 15, null)
		'Minor    2013/09'    | '7.5.11+43'                                   | new Jep223JavaVersion([7, 5, 11] as int[], null, 43, null)
		'Security 2013/10'    | '7.5.12+18'                                   | new Jep223JavaVersion([7, 5, 12] as int[], null, 18, null)
		'Security 2014/01'    | '7.5.13+13'                                   | new Jep223JavaVersion([7, 5, 13] as int[], null, 13, null)
		'Security 2014/04'    | '7.6.14+13'                                   | new Jep223JavaVersion([7, 6, 14] as int[], null, 13, null)
		'Minor    2014/05'    | '7.6.14+19'                                   | new Jep223JavaVersion([7, 6, 14] as int[], null, 19, null)
		'Security 2014/07'    | '7.6.15+20'                                   | new Jep223JavaVersion([7, 6, 15] as int[], null, 20, null)

		'Security 2013/04'    | '7.4.10'                                      | new Jep223JavaVersion([7, 4, 10] as int[], null, 0, null)
		'Security 2013/06'    | '7.4.11'                                      | new Jep223JavaVersion([7, 4, 11] as int[], null, 0, null)
		'Minor    2013/09'    | '7.5.11'                                      | new Jep223JavaVersion([7, 5, 11] as int[], null, 0, null)
		'Security 2013/10'    | '7.5.12'                                      | new Jep223JavaVersion([7, 5, 12] as int[], null, 0, null)
		'Security 2014/01'    | '7.5.13'                                      | new Jep223JavaVersion([7, 5, 13] as int[], null, 0, null)
		'Security 2014/04'    | '7.5.14'                                      | new Jep223JavaVersion([7, 5, 14] as int[], null, 0, null)
		'Minor    2014/05'    | '7.6.14'                                      | new Jep223JavaVersion([7, 6, 14] as int[], null, 0, null)
		'Security 2014/07'    | '7.6.15'                                      | new Jep223JavaVersion([7, 6, 15] as int[], null, 0, null)

		'Whatever'            | '9.7.6.5.4.3'                                 | new Jep223JavaVersion([9, 7, 6, 5, 4, 3] as int[], null, 0, null)
		'withOpt'             | '9.7.6.5.4-ea-opt'                            | new Jep223JavaVersion([9, 7, 6, 5, 4] as int[], 'ea', 0, 'opt')
		'full'                | '9.7.6.5.4.3.2.1-ea+49-opt'                   | new Jep223JavaVersion([9, 7, 6, 5, 4, 3, 2, 1] as int[], 'ea', 49, 'opt')
		'zeroSecurity'        | '9.1.0'                                       | new Jep223JavaVersion([9, 1, 0] as int[], null, 0, null)
		'verona'              | '9-internal+17-2015-07-14-120103.iris.verona' | new Jep223JavaVersion([9] as int[], 'internal', 17, '2015-07-14-120103.iris.verona')

		'jep-322'             | '11.0.2+13-LTS'                               | new Jep223JavaVersion([11, 0, 2] as int[], null, 13, 'LTS')
		'jep-322 (emergency)' | '11.0.2.17+13-LTS'                            | new Jep223JavaVersion([11, 0, 2, 17] as int[], null, 13, 'LTS')
	}

	@Unroll('Getters return correct values for #versionString. (#description)')
	getters(String description, String versionString, List<Integer> expectedVersionNumbers, int expectedMajor, int expectedMinor, int expectedSecurity, int expectedEmergencyPatch, String expectedPre, int expectedBuild, String expectedOpt) {
		when:
		Jep223JavaVersion version = Jep223JavaVersion.parse(versionString)

		then:
		expectedVersionNumbers == version.versionNumbers

		expectedMajor == version.major
		expectedMajor == version.feature

		expectedMinor == version.minor
		expectedMinor == version.interim

		expectedSecurity == version.security
		expectedSecurity == version.patch
		expectedSecurity == version.update

		expectedEmergencyPatch == version.emergencyPatch

		expectedPre == version.preReleaseIdentifier

		expectedBuild == version.buildNumber

		expectedOpt == version.additionalBuildInformation


		where:
		description           | versionString                                 | expectedVersionNumbers   | expectedMajor | expectedMinor | expectedSecurity | expectedEmergencyPatch | expectedPre | expectedBuild | expectedOpt
		'Early Access'        | '9.0.0-ea+19'                                 | [9, 0, 0]                | 9             | 0             | 0                | 0                      | 'ea'        | 19            | null
		'Major'               | '9.0.0+100'                                   | [9, 0, 0]                | 9             | 0             | 0                | 0                      | null        | 100           | null
		'Security #1'         | '9.0.1+20'                                    | [9, 0, 1]                | 9             | 0             | 1                | 0                      | null        | 20            | null
		'Security #2'         | '9.0.2+12'                                    | [9, 0, 2]                | 9             | 0             | 2                | 0                      | null        | 12            | null
		'Minor #1'            | '9.1.2+62'                                    | [9, 1, 2]                | 9             | 1             | 2                | 0                      | null        | 62            | null
		'Security #3'         | '9.1.3+15'                                    | [9, 1, 3]                | 9             | 1             | 3                | 0                      | null        | 15            | null
		'Security #4'         | '9.1.4+8'                                     | [9, 1, 4]                | 9             | 1             | 4                | 0                      | null        | 8             | null
		'Minor #2'            | '9.2.4+45'                                    | [9, 2, 4]                | 9             | 2             | 4                | 0                      | null        | 45            | null

		'Early Access'        | '9-ea'                                        | [9]                      | 9             | 0             | 0                | 0                      | 'ea'        | 0             | null
		'Major'               | '9'                                           | [9]                      | 9             | 0             | 0                | 0                      | null        | 0             | null
		'Security #1'         | '9.0.1'                                       | [9, 0, 1]                | 9             | 0             | 1                | 0                      | null        | 0             | null
		'Security #2'         | '9.0.2'                                       | [9, 0, 2]                | 9             | 0             | 2                | 0                      | null        | 0             | null
		'Minor #1'            | '9.1.2'                                       | [9, 1, 2]                | 9             | 1             | 2                | 0                      | null        | 0             | null
		'Security #3'         | '9.1.3'                                       | [9, 1, 3]                | 9             | 1             | 3                | 0                      | null        | 0             | null
		'Security #4'         | '9.1.4'                                       | [9, 1, 4]                | 9             | 1             | 4                | 0                      | null        | 0             | null
		'Minor #2'            | '9.2.4'                                       | [9, 2, 4]                | 9             | 2             | 4                | 0                      | null        | 0             | null

		'Security 2013/04'    | '7.4.10+11'                                   | [7, 4, 10]               | 7             | 4             | 10               | 0                      | null        | 11            | null
		'Security 2013/06'    | '7.4.11+15'                                   | [7, 4, 11]               | 7             | 4             | 11               | 0                      | null        | 15            | null
		'Minor    2013/09'    | '7.5.11+43'                                   | [7, 5, 11]               | 7             | 5             | 11               | 0                      | null        | 43            | null
		'Security 2013/10'    | '7.5.12+18'                                   | [7, 5, 12]               | 7             | 5             | 12               | 0                      | null        | 18            | null
		'Security 2014/01'    | '7.5.13+13'                                   | [7, 5, 13]               | 7             | 5             | 13               | 0                      | null        | 13            | null
		'Security 2014/04'    | '7.6.14+13'                                   | [7, 6, 14]               | 7             | 6             | 14               | 0                      | null        | 13            | null
		'Minor    2014/05'    | '7.6.14+19'                                   | [7, 6, 14]               | 7             | 6             | 14               | 0                      | null        | 19            | null
		'Security 2014/07'    | '7.6.15+20'                                   | [7, 6, 15]               | 7             | 6             | 15               | 0                      | null        | 20            | null

		'Security 2013/04'    | '7.4.10'                                      | [7, 4, 10]               | 7             | 4             | 10               | 0                      | null        | 0             | null
		'Security 2013/06'    | '7.4.11'                                      | [7, 4, 11]               | 7             | 4             | 11               | 0                      | null        | 0             | null
		'Minor    2013/09'    | '7.5.11'                                      | [7, 5, 11]               | 7             | 5             | 11               | 0                      | null        | 0             | null
		'Security 2013/10'    | '7.5.12'                                      | [7, 5, 12]               | 7             | 5             | 12               | 0                      | null        | 0             | null
		'Security 2014/01'    | '7.5.13'                                      | [7, 5, 13]               | 7             | 5             | 13               | 0                      | null        | 0             | null
		'Security 2014/04'    | '7.5.14'                                      | [7, 5, 14]               | 7             | 5             | 14               | 0                      | null        | 0             | null
		'Minor    2014/05'    | '7.6.14'                                      | [7, 6, 14]               | 7             | 6             | 14               | 0                      | null        | 0             | null
		'Security 2014/07'    | '7.6.15'                                      | [7, 6, 15]               | 7             | 6             | 15               | 0                      | null        | 0             | null

		'Whatever'            | '9.7.6.5.4.3'                                 | [9, 7, 6, 5, 4, 3]       | 9             | 7             | 6                | 5                      | null        | 0             | null
		'withOpt'             | '9.7.6.5.4-ea-opt'                            | [9, 7, 6, 5, 4]          | 9             | 7             | 6                | 5                      | 'ea'        | 0             | 'opt'
		'full'                | '9.7.6.5.4.3.2.1-ea+49-opt'                   | [9, 7, 6, 5, 4, 3, 2, 1] | 9             | 7             | 6                | 5                      | 'ea'        | 49            | 'opt'
		'zeroSecurity'        | '9.1.0'                                       | [9, 1, 0]                | 9             | 1             | 0                | 0                      | null        | 0             | null
		'verona'              | '9-internal+17-2015-07-14-120103.iris.verona' | [9]                      | 9             | 0             | 0                | 0                      | 'internal'  | 17            | '2015-07-14-120103.iris.verona'

		'jep-322'             | '11.0.2+13-LTS'                               | [11, 0, 2]               | 11            | 0             | 2                | 0                      | null        | 13            | 'LTS'
		'jep-322 (emergency)' | '11.0.2.17+13-LTS'                            | [11, 0, 2, 17]           | 11            | 0             | 2                | 17                     | null        | 13            | 'LTS'
	}

	@Unroll('Jep223JavaVersion.toShortVersionString() is #expectedVersionString. (#description)')
	shortString(String description, String expectedVersionString, Jep223JavaVersion version) {
		when:
		def versionString = version.toShortVersionString()

		then:
		versionString == expectedVersionString

		where:
		description           | expectedVersionString | version
		'Early Access'        | '9-ea'                | new Jep223JavaVersion([9, 0, 0] as int[], 'ea', 19, null)
		'Major'               | '9'                   | new Jep223JavaVersion([9, 0, 0] as int[], null, 100, null)
		'Security #1'         | '9.0.1'               | new Jep223JavaVersion([9, 0, 1] as int[], null, 20, null)
		'Security #2'         | '9.0.2'               | new Jep223JavaVersion([9, 0, 2] as int[], null, 12, null)
		'Minor #1'            | '9.1.2'               | new Jep223JavaVersion([9, 1, 2] as int[], null, 62, null)
		'Security #3'         | '9.1.3'               | new Jep223JavaVersion([9, 1, 3] as int[], null, 15, null)
		'Security #4'         | '9.1.4'               | new Jep223JavaVersion([9, 1, 4] as int[], null, 8, null)
		'Minor #2'            | '9.2.4'               | new Jep223JavaVersion([9, 2, 4] as int[], null, 45, null)

		'Early Access'        | '9-ea'                | new Jep223JavaVersion([9] as int[], 'ea', 0, null)
		'Major'               | '9'                   | new Jep223JavaVersion([9] as int[], null, 0, null)
		'Security #1'         | '9.0.1'               | new Jep223JavaVersion([9, 0, 1] as int[], null, 0, null)
		'Security #2'         | '9.0.2'               | new Jep223JavaVersion([9, 0, 2] as int[], null, 0, null)
		'Minor #1'            | '9.1.2'               | new Jep223JavaVersion([9, 1, 2] as int[], null, 0, null)
		'Security #3'         | '9.1.3'               | new Jep223JavaVersion([9, 1, 3] as int[], null, 0, null)
		'Security #4'         | '9.1.4'               | new Jep223JavaVersion([9, 1, 4] as int[], null, 0, null)
		'Minor #2'            | '9.2.4'               | new Jep223JavaVersion([9, 2, 4] as int[], null, 0, null)

		'Security 2013/04'    | '7.4.10'              | new Jep223JavaVersion([7, 4, 10] as int[], null, 11, null)
		'Security 2013/06'    | '7.4.11'              | new Jep223JavaVersion([7, 4, 11] as int[], null, 15, null)
		'Minor    2013/09'    | '7.5.11'              | new Jep223JavaVersion([7, 5, 11] as int[], null, 43, null)
		'Security 2013/10'    | '7.5.12'              | new Jep223JavaVersion([7, 5, 12] as int[], null, 18, null)
		'Security 2014/01'    | '7.5.13'              | new Jep223JavaVersion([7, 5, 13] as int[], null, 13, null)
		'Security 2014/04'    | '7.6.14'              | new Jep223JavaVersion([7, 6, 14] as int[], null, 13, null)
		'Minor    2014/05'    | '7.6.14'              | new Jep223JavaVersion([7, 6, 14] as int[], null, 19, null)
		'Security 2014/07'    | '7.6.15'              | new Jep223JavaVersion([7, 6, 15] as int[], null, 20, null)

		'Security 2013/04'    | '7.4.10'              | new Jep223JavaVersion([7, 4, 10] as int[], null, 0, null)
		'Security 2013/06'    | '7.4.11'              | new Jep223JavaVersion([7, 4, 11] as int[], null, 0, null)
		'Minor    2013/09'    | '7.5.11'              | new Jep223JavaVersion([7, 5, 11] as int[], null, 0, null)
		'Security 2013/10'    | '7.5.12'              | new Jep223JavaVersion([7, 5, 12] as int[], null, 0, null)
		'Security 2014/01'    | '7.5.13'              | new Jep223JavaVersion([7, 5, 13] as int[], null, 0, null)
		'Security 2014/04'    | '7.5.14'              | new Jep223JavaVersion([7, 5, 14] as int[], null, 0, null)
		'Minor    2014/05'    | '7.6.14'              | new Jep223JavaVersion([7, 6, 14] as int[], null, 0, null)
		'Security 2014/07'    | '7.6.15'              | new Jep223JavaVersion([7, 6, 15] as int[], null, 0, null)

		'Whatever'            | '9.7.6.5'             | new Jep223JavaVersion([9, 7, 6, 5, 4, 3] as int[], null, 0, null)
		'withOpt'             | '9.7.6.5-ea'          | new Jep223JavaVersion([9, 7, 6, 5, 4] as int[], 'ea', 0, 'opt')
		'full'                | '9.7.6.5-ea'          | new Jep223JavaVersion([9, 7, 6, 5, 4, 3, 2, 1] as int[], 'ea', 49, 'opt')
		'zeroSecurity'        | '9.1'                 | new Jep223JavaVersion([9, 1, 0] as int[], null, 0, null)
		'verona'              | '9-internal'          | new Jep223JavaVersion([9] as int[], 'internal', 17, '2015-07-14-120103.iris.verona')

		'jep-322'             | '11.0.2'              | new Jep223JavaVersion([11, 0, 2] as int[], null, 13, 'LTS')
		'jep-322 (emergency)' | '11.0.2.17'           | new Jep223JavaVersion([11, 0, 2, 17] as int[], null, 13, 'LTS')
	}

	def 'serializable'() {
		expect:
		JUnitTools.testSerialization(object)

		where:
		object << [
				new Jep223JavaVersion([9] as int[], null, 0, null),
				new Jep223JavaVersion([9, 7, 6, 5, 4, 3, 2, 1] as int[], 'ea', 49, 'opt')
		]
	}

//    def 'sorting fail'() {
//        when:
//        List<Jep223JavaVersion> list = new ArrayList<>()
//        // 4 > 2A, 2A > 12, and 12 > 4
//        list.add(new Jep223JavaVersion([9, 0, 0] as int[], '12', 0, null))
//        list.add(new Jep223JavaVersion([9, 0, 0] as int[], '4', 0, null))
//        list.add(new Jep223JavaVersion([9, 0, 0] as int[], '2A', 0, null))
//
//        /*
//        Collections.sort([9.0.0-4, 9.0.0-2A, 9.0.0-12]) => [9.0.0-12, 9.0.0-2A, 9.0.0-4]
//        Collections.sort([9.0.0-4, 9.0.0-12, 9.0.0-2A]) => [9.0.0-4, 9.0.0-12, 9.0.0-2A]
//        Collections.sort([9.0.0-12, 9.0.0-2A, 9.0.0-4]) => [9.0.0-12, 9.0.0-2A, 9.0.0-4]
//        Collections.sort([9.0.0-12, 9.0.0-4, 9.0.0-2A]) => [9.0.0-2A, 9.0.0-4, 9.0.0-12]
//        */
//
//        println list
//        Collections.sort(list)
//        println list
//        Collections.sort(list)
//        println list
//        then:
//        true
//    }

	def 'parse(null) throws an exception'() {
		when:
		Jep223JavaVersion.parse(null)

		then:
		NullPointerException ex = thrown()
		ex.message == 'versionString must not be null!'
	}

	def 'new Jep223JavaVersion((int[])null) throws an exception'() {
		when:
		new Jep223JavaVersion((int[]) null, null, 0, null)

		then:
		NullPointerException ex = thrown()
		ex.message == 'versionNumbers must not be null!'
	}

	def 'new Jep223JavaVersion((Integer[])null) throws an exception'() {
		when:
		new Jep223JavaVersion((Integer[]) null, null, 0, null)

		then:
		NullPointerException ex = thrown()
		ex.message == 'versionNumbers must not be null!'
	}

	def 'new Jep223JavaVersion([] as int[]) throws an exception'() {
		when:
		new Jep223JavaVersion([] as int[], null, 0, null)

		then:
		IllegalArgumentException ex = thrown()
		ex.message == 'versionNumbers.length must not be zero!'
	}

	def 'new Jep223JavaVersion([] as Integer[]) throws an exception'() {
		when:
		new Jep223JavaVersion([] as Integer[], null, 0, null)

		then:
		IllegalArgumentException ex = thrown()
		ex.message == 'versionNumbers.length must not be zero!'
	}

	def 'new Jep223JavaVersion([1, null, 2]) throws an exception'() {
		when:
		new Jep223JavaVersion([1, null, 2] as Integer[], null, 0, null)

		then:
		IllegalArgumentException ex = thrown()
		ex.message == 'versionNumbers must not contain null values!'
	}

	def 'new Jep223JavaVersion(build<0) throws an exception'() {
		when:
		new Jep223JavaVersion([1, 2] as Integer[], null, -1, null)

		then:
		IllegalArgumentException ex = thrown()
		ex.message == 'buildNumber must not be negative!'
	}

	def 'new Jep223JavaVersion(illegal prerelease) throws an exception'() {
		when:
		new Jep223JavaVersion([1, 2] as Integer[], '1#2', 0, null)

		then:
		IllegalArgumentException ex = thrown()
		ex.message == 'preReleaseIdentifier \'1#2\' is illegal. It doesn\'t match the pattern \'([a-zA-Z0-9]+)\'.'
	}

	def 'new Jep223JavaVersion(illegal opt) throws an exception'() {
		when:
		new Jep223JavaVersion([1, 2] as Integer[], null, 0, '1#2')

		then:
		IllegalArgumentException ex = thrown()
		ex.message == 'additionalBuildInformation \'1#2\' is illegal. It doesn\'t match the pattern \'([-a-zA-Z0-9.]+)\'.'
	}

	def 'equals(null) returns false'() {
		when:
		Jep223JavaVersion object = new Jep223JavaVersion([8, 1] as Integer[], null, 0, null)

		then:
		//noinspection ChangeToOperator
		!object.equals(null)
	}

	@SuppressWarnings("GrEqualsBetweenInconvertibleTypes")
	'equals(someOtherClass) returns false'() {
		when:
		Jep223JavaVersion object = new Jep223JavaVersion([8, 1] as Integer[], null, 0, null)

		then:
		//noinspection ChangeToOperator
		!object.equals(1)
	}

	def 'equals(same) returns true'() {
		when:
		Jep223JavaVersion object = new Jep223JavaVersion([8, 1] as Integer[], null, 0, null)

		then:
		//noinspection ChangeToOperator
		object.equals(object)
	}

	def 'compareTo(null) throws exception'() {
		when:
		Jep223JavaVersion object = new Jep223JavaVersion([8, 1] as Integer[], null, 0, null)
		//noinspection ChangeToOperator
		object.compareTo(null)

		then:
		NullPointerException ex = thrown()
		ex.getMessage() == 'other must not be null!'
	}

	def 'ensure version numbers array immutability'() {
		when:
		Integer[] versions = [1, 2, 3]
		Jep223JavaVersion object = new Jep223JavaVersion(versions, null, 0, null)
		versions[1] = 17

		then:
		object.toVersionString() == '1.2.3'

	}

	def 'ensure version numbers list immutability'() {
		when:
		Integer[] versions = [1, 2, 3]
		Jep223JavaVersion object = new Jep223JavaVersion(versions, null, 0, null)
		object.getVersionNumbers().remove(0)

		then:
		thrown(UnsupportedOperationException)
	}

	def 'validate version numbers list content'() {
		when:
		Integer[] versions = [1, 2, 3, 4, 5]
		Jep223JavaVersion object = new Jep223JavaVersion(versions, null, 0, null)
		List<Integer> versionNumbersList = object.getVersionNumbers()

		then:
		versionNumbersList[0] == 1
		versionNumbersList[1] == 2
		versionNumbersList[2] == 3
		versionNumbersList[3] == 4
		versionNumbersList[4] == 5
		versionNumbersList.size() == 5
	}

	@Unroll('#versionAString #compareString #versionBString.')
	'comparing instances works using compareTo and equals'() {
		when:
		Jep223JavaVersion versionA = Jep223JavaVersion.parse(versionAString)
		Jep223JavaVersion versionB = Jep223JavaVersion.parse(versionBString)
		//noinspection ChangeToOperator
		int compareToAB = versionA.compareTo(versionB)
		//noinspection ChangeToOperator
		int compareToBA = versionB.compareTo(versionA)

		then:
		compareToAB == expectedCompareResult
		compareToAB == -1 * compareToBA

		//noinspection ChangeToOperator
		expectedEqualsResult == versionA.equals(versionB)
		//noinspection ChangeToOperator
		expectedEqualsResult == versionB.equals(versionA)

		if (expectedEqualsResult) {
			assert versionA.hashCode() == versionB.hashCode()
		}

		where:
		versionAString     | versionBString     | expectedCompareResult | expectedEqualsResult
		'9'                | '9.0.0'            | 0                     | false
		'9.0'              | '9.0.0'            | 0                     | false
		'9.0.0'            | '9.0.0'            | 0                     | true
		'9'                | '10.0.0'           | -1                    | false
		'9.0'              | '10.0.0'           | -1                    | false
		'9.0.0'            | '10.0.0'           | -1                    | false
		'9'                | '9.1.0'            | -1                    | false
		'9.0'              | '9.1.0'            | -1                    | false
		'9.0.0'            | '9.1.0'            | -1                    | false
		'9'                | '9.0.1'            | -1                    | false
		'9.0'              | '9.0.1'            | -1                    | false
		'9.0.0'            | '9.0.1'            | -1                    | false
		'9.0.0'            | '9-ea'             | 1                     | false
		'9'                | '9-ea'             | 1                     | false
		'9-ea'             | '9-eb'             | -1                    | false
		'9.1.2-ea+49-optA' | '9.1.2-ea+49-optA' | 0                     | true
		'9.1.2-ea+49-optA' | '9.1.2-ea+70-optB' | 0                     | false
		'9.1.2-ea+49-optA' | '9.1.2-ea+49'      | 0                     | false
		'11.0.2+13-LTS'    | '11.0.2+13-LTS'    | 0                     | true
		'11.0.2+13-LTS'    | '11.0.2.0+13-LTS'  | 0                     | false
		'11.0.2.17+13-LTS' | '11.0.2.17+13-LTS' | 0                     | true
		'11.0.2+13-LTS'    | '11.0.2.17+13-LTS' | -1                    | false
		'11.0.2.17+13-LTS' | '11.0.2+13-LTS'    | 1                     | false

		compareString = compareDescription(expectedCompareResult)
	}

	@Unroll
	'withoutPreReleaseIdentifier() works as expected for #input'(Jep223JavaVersion input, Jep223JavaVersion expectedResult) {
		when:
		Jep223JavaVersion result = input.withoutPreReleaseIdentifier()

		then:
		result == expectedResult
		if (input.preReleaseIdentifier == null) {
			assert result.is(input)
		}

		where:
		input                                                              | expectedResult
		new Jep223JavaVersion([0, 0, 0, 0] as Integer[], 'foo', 17, 'bar') | new Jep223JavaVersion([0, 0, 0, 0] as Integer[], null, 17, 'bar')
		new Jep223JavaVersion([1, 2, 3, 4] as Integer[], 'foo', 17, 'bar') | new Jep223JavaVersion([1, 2, 3, 4] as Integer[], null, 17, 'bar')
		new Jep223JavaVersion([1, 2, 3, 4] as Integer[], null, 17, 'bar')  | new Jep223JavaVersion([1, 2, 3, 4] as Integer[], null, 17, 'bar')
	}

	private static String compareDescription(int value) {
		if (value < 0) {
			return 'is less than'
		}

		if (value > 0) {
			return 'is greater than'
		}

		return 'is equal to'
	}
}
