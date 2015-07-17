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

package de.huxhorn.sulky.version

import de.huxhorn.sulky.junit.JUnitTools
import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

@Subject(SemanticVersion)
class SemanticVersionSpec
    extends Specification
{

    @Unroll("'#versionString' is a valid semantic version.")
    def validVersions(String versionString, SemanticVersion expectedValue) {
        when:
        SemanticVersion version = SemanticVersion.parse(versionString)
        String generatedVersionString = version.toString()
        String regeneratedVersionString = version.toString() // check transient variable

        then:
        // using explicit equals() since == uses compareTo if Comparable is implemented.
        version.equals(expectedValue)
        version.hashCode() == expectedValue.hashCode()
        generatedVersionString == versionString
        generatedVersionString == expectedValue.toString()
        generatedVersionString == regeneratedVersionString

        where:
        versionString                | expectedValue
        '0.1.2'                      | new SemanticVersion(0, 1, 2)
        '1.0.0'                      | new SemanticVersion(1, 0, 0)
        '1.0.0'                      | new SemanticVersion(1, 0, 0, null, null)
        '1.0.0+20130313144700'       | new SemanticVersion(1, 0, 0, null, ['20130313144700'] as String[])
        '1.0.0-0.3.7'                | new SemanticVersion(1, 0, 0, ['0', '3', '7'] as String[])
        '1.0.0-alpha'                | new SemanticVersion(1, 0, 0, ['alpha'] as String[])
        '1.0.0-alpha+001'            | new SemanticVersion(1, 0, 0, ['alpha'] as String[], ['001'] as String[])
        '1.0.0-alpha.1'              | new SemanticVersion(1, 0, 0, ['alpha', '1'] as String[])
        '1.0.0-alpha.beta'           | new SemanticVersion(1, 0, 0, ['alpha', 'beta'] as String[])
        '1.0.0-beta'                 | new SemanticVersion(1, 0, 0, ['beta'] as String[])
        '1.0.0-beta+exp.sha.5114f85' | new SemanticVersion(1, 0, 0, ['beta'] as String[], ['exp', 'sha', '5114f85'] as String[])
        '1.0.0-beta.2'               | new SemanticVersion(1, 0, 0, ['beta', '2'] as String[])
        '1.0.0-beta.11'              | new SemanticVersion(1, 0, 0, ['beta', '11'] as String[])
        '1.0.0-rc.1'                 | new SemanticVersion(1, 0, 0, ['rc', '1'] as String[])
        '1.0.0-x.7.z.92'             | new SemanticVersion(1, 0, 0, ['x', '7', 'z', '92'] as String[])
        '1.9.0'                      | new SemanticVersion(1, 9, 0)
        '1.10.0'                     | new SemanticVersion(1, 10, 0)
        '1.11.0'                     | new SemanticVersion(1, 11, 0)
        '2.0.0'                      | new SemanticVersion(2, 0, 0)
        '2.1.0'                      | new SemanticVersion(2, 1, 0)
        '2.1.1'                      | new SemanticVersion(2, 1, 1)

    }

    @Unroll('#versionAString #compareString #versionBString.')
    def 'comparing instances works using compareTo'() {
        when:
        SemanticVersion versionA = SemanticVersion.parse(versionAString)
        SemanticVersion versionB = SemanticVersion.parse(versionBString)
        int compareToAB = versionA.compareTo(versionB)
        int compareToBA = versionB.compareTo(versionA)

        then:
        compareToAB == expectedResult
        compareToAB ==  -1 * compareToBA


        // 1.9.0 -> 1.10.0 -> 1.11.0
        // 0.x.y
        // 1.0.0-alpha, 1.0.0-alpha.1, 1.0.0-0.3.7, 1.0.0-x.7.z.92
        // 1.0.0-alpha+001, 1.0.0+20130313144700, 1.0.0-beta+exp.sha.5114f85
        // 1.0.0 < 2.0.0 < 2.1.0 < 2.1.1
        // 1.0.0-alpha < 1.0.0
        // 1.0.0-alpha < 1.0.0-alpha.1 < 1.0.0-alpha.beta < 1.0.0-beta < 1.0.0-beta.2 < 1.0.0-beta.11 < 1.0.0-rc.1 < 1.0.0

        where:
        versionAString     | versionBString     | expectedResult
        '0.9.0'            | '1.0.0'            | -1
        '1.9.0'            | '1.10.0'           | -1
        '1.9.0'            | '1.11.0'           | -1
        '1.10.0'           | '1.11.0'           | -1
        '1.11.0'           | '1.11.1'           | -1
        '1.0.0'            | '2.0.0'            | -1
        '1.0.0'            | '2.1.0'            | -1
        '1.0.0'            | '2.1.1'            | -1
        '2.0.0'            | '2.1.0'            | -1
        '2.0.0'            | '2.1.1'            | -1
        '2.1.0'            | '2.1.1'            | -1
        '1.0.0-alpha'      | '1.0.0'            | -1
        '1.0.0-alpha'      | '1.0.0-alpha.1'    | -1
        '1.0.0-alpha'      | '1.0.0-alpha.beta' | -1
        '1.0.0-alpha'      | '1.0.0-beta'       | -1
        '1.0.0-alpha'      | '1.0.0-beta.2'     | -1
        '1.0.0-alpha'      | '1.0.0-beta.11'    | -1
        '1.0.0-alpha'      | '1.0.0-rc.1'       | -1
        '1.0.0-alpha.1'    | '1.0.0-alpha.beta' | -1
        '1.0.0-alpha.1'    | '1.0.0-beta'       | -1
        '1.0.0-alpha.1'    | '1.0.0-beta.2'     | -1
        '1.0.0-alpha.1'    | '1.0.0-beta.11'    | -1
        '1.0.0-alpha.1'    | '1.0.0-rc.1'       | -1
        '1.0.0-alpha.beta' | '1.0.0-beta'       | -1
        '1.0.0-alpha.beta' | '1.0.0-beta.2'     | -1
        '1.0.0-alpha.beta' | '1.0.0-beta.11'    | -1
        '1.0.0-alpha.beta' | '1.0.0-rc.1'       | -1
        '1.0.0-beta'       | '1.0.0-beta.2'     | -1
        '1.0.0-beta'       | '1.0.0-beta.11'    | -1
        '1.0.0-beta'       | '1.0.0-rc.1'       | -1
        '1.0.0-beta.2'     | '1.0.0-beta.11'    | -1
        '1.0.0-beta.2'     | '1.0.0-rc.1'       | -1
        '1.0.0-beta.11'    | '1.0.0-rc.1'       | -1
        '1.9.0'            | '1.9.0'            | 0
        '1.0.0-alpha'      | '1.0.0-alpha+001'  | 0
        '1.0.0-alpha.1'    | '1.0.0-alpha.1'    | 0

        compareString = compareString(expectedResult)
    }

    @Unroll('#versionAString #compareString #versionBString.')
    def 'comparing instances works using equals and hashCode'() {
        when:
        SemanticVersion versionA = SemanticVersion.parse(versionAString)
        SemanticVersion versionB = SemanticVersion.parse(versionBString)

        then:
        if(expectedResult) {
            assert versionA.equals(versionB)
            assert versionA.hashCode() == versionB.hashCode()
        }
        else {
            assert !versionA.equals(versionB)
        }

        where:
        versionAString     | versionBString      | expectedResult
        '0.9.0'            | '1.0.0'             | false
        '1.9.0'            | '1.10.0'            | false
        '1.9.0'            | '1.11.0'            | false
        '1.10.0'           | '1.11.0'            | false
        '1.11.0'           | '1.11.1'            | false
        '1.0.0'            | '2.0.0'             | false
        '1.0.0'            | '2.1.0'             | false
        '1.0.0'            | '2.1.1'             | false
        '2.0.0'            | '2.1.0'             | false
        '2.0.0'            | '2.1.1'             | false
        '2.1.0'            | '2.1.1'             | false
        '1.0.0-alpha'      | '1.0.0'             | false
        '1.0.0-alpha'      | '1.0.0-alpha.1'     | false
        '1.0.0-alpha'      | '1.0.0-alpha.beta'  | false
        '1.0.0-alpha'      | '1.0.0-beta'        | false
        '1.0.0-alpha'      | '1.0.0-beta.2'      | false
        '1.0.0-alpha'      | '1.0.0-beta.11'     | false
        '1.0.0-alpha'      | '1.0.0-rc.1'        | false
        '1.0.0-alpha.1'    | '1.0.0-alpha.beta'  | false
        '1.0.0-alpha.1'    | '1.0.0-beta'        | false
        '1.0.0-alpha.1'    | '1.0.0-beta.2'      | false
        '1.0.0-alpha.1'    | '1.0.0-beta.11'     | false
        '1.0.0-alpha.1'    | '1.0.0-rc.1'        | false
        '1.0.0-alpha.beta' | '1.0.0-beta'        | false
        '1.0.0-alpha.beta' | '1.0.0-beta.2'      | false
        '1.0.0-alpha.beta' | '1.0.0-beta.11'     | false
        '1.0.0-alpha.beta' | '1.0.0-rc.1'        | false
        '1.0.0-beta'       | '1.0.0-beta.2'      | false
        '1.0.0-beta'       | '1.0.0-beta.11'     | false
        '1.0.0-beta'       | '1.0.0-rc.1'        | false
        '1.0.0-beta.2'     | '1.0.0-beta.11'     | false
        '1.0.0-beta.2'     | '1.0.0-rc.1'        | false
        '1.0.0-beta.11'    | '1.0.0-rc.1'        | false
        '1.9.0'            | '1.9.0'             | true
        '1.0.0-alpha'      | '1.0.0-alpha+001'   | false
        '1.0.0-alpha.1'    | '1.0.0-alpha.1'     | true
        '1.0.0-alpha+001'  | '2.0.0-alpha+001'   | false
        '1.0.0-alpha+001'  | '1.1.0-alpha+001'   | false
        '1.0.0-alpha+001'  | '1.0.1-alpha+001'   | false
        '1.0.0-alpha+001'  | '1.0.1-alpha.1+001' | false
        '1.0.0-alpha+001'  | '1.0.1-alpha1+001'  | false
        '1.0.0-alpha+001'  | '1.0.1-alpha+001.1' | false
        '1.0.0-alpha+001'  | '1.0.1-alpha+002'   | false

        compareString = expectedResult? 'equals' : 'does not equal'
    }

    def "null preRelease value"() {
        when:
        new SemanticVersion(1,0,0, ['foo', null, 'bar'] as String[])

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'preRelease must not contain null!'
    }

    @Unroll("'#illegalValue' is an illegal preRelease identifier.")
    def "illegal preRelease value"() {
        when:
        new SemanticVersion(1,0,0, ['foo', illegalValue, 'bar'] as String[])

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'preRelease identifier \''+illegalValue+'\' is invalid!'

        where:
        illegalValue << [
                '',
                '$foo'
        ]
    }

    def "null buildMetadata value"() {
        when:
        new SemanticVersion(1,0,0, null, ['foo', null, 'bar'] as String[])

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'buildMetadata must not contain null!'
    }

    @Unroll("'#illegalValue' is an illegal preRelease identifier.")
    def "illegal buildMetadata value"() {
        when:
        new SemanticVersion(1,0,0, null, ['foo', illegalValue, 'bar'] as String[])

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'buildMetadata identifier \''+illegalValue+'\' is invalid!'

        where:
        illegalValue << [
                '',
                '$foo'
        ]
    }

    def "ensure getPreRelease() immutability"() {
        when:
        new SemanticVersion(1,0,0, ['foo', 'bar'] as String[]).preRelease.remove(0)

        then:
        thrown(UnsupportedOperationException)
    }

    def "ensure getBuildMetadata() immutability"() {
        when:
        new SemanticVersion(1,0,0, null, ['foo', 'bar'] as String[]).buildMetadata.remove(0)

        then:
        thrown(UnsupportedOperationException)
    }

    def "ensure preRelease array immutability"() {
        when:
        String[] identifiers = ['foo', 'bar']
        SemanticVersion version = new SemanticVersion(1,0,0, identifiers)
        identifiers[0] = 'fooBar'

        then:
        version.preRelease[0] == 'foo'
    }

    def "ensure buildMetadata array immutability"() {
        when:
        String[] identifiers = ['foo', 'bar']
        SemanticVersion version = new SemanticVersion(1,0,0, null, identifiers)
        identifiers[0] = 'fooBar'

        then:
        version.buildMetadata[0] == 'foo'
    }
    def "equals(null)"() {
        expect:
        !new SemanticVersion(1,0,0).equals(null)
    }

    def "equals(Object)"() {
        expect:
        !new SemanticVersion(1,0,0).equals(new Object())
    }

    def "equals(this)"() {
        when:
        SemanticVersion version = new SemanticVersion(1,0,0)

        then:
        version.equals(version)
    }

    def "parse(null) throws exception."() {
        when:
        SemanticVersion.parse(null)

        then:
        NullPointerException ex = thrown()
        ex.message == 'versionString must not be null!'
    }

    def "compareTo(null) throws exception."() {
        when:
        new SemanticVersion(1,0,0).compareTo(null)

        then:
        NullPointerException ex = thrown()
        ex.message == 'other must not be null!'
    }

    @Unroll("parse('#versionString') throws exception.")
    def "parsing invalid strings throws proper exception."() {
        when:
        SemanticVersion.parse(versionString)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == "'$versionString' is not a valid semantic version!".toString()

        where:
        versionString << [
                '1.0'
        ]
    }

    def "invalid major number"() {
        when:
        new SemanticVersion(-1,0,0)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'major must not be negative!'
    }

    def "invalid minor number"() {
        when:
        new SemanticVersion(0,-1,0)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'minor must not be negative!'
    }

    def "invalid patch number"() {
        when:
        new SemanticVersion(0,0,-1)

        then:
        IllegalArgumentException ex = thrown()
        ex.message == 'patch must not be negative!'
    }

    def "check getters"() {
        when:
        SemanticVersion version = new SemanticVersion(major, minor, patch, preRelease, buildMetadata)

        then:
        version.major == major
        version.minor == minor
        version.patch == patch
        version.preRelease == preRelease as List<String>
        version.buildMetadata == buildMetadata as List<String>

        where:
        major | minor | patch | preRelease            | buildMetadata
        1     | 2     | 3     | [] as String[]        | [] as String[]
        1     | 2     | 3     | ['alpha'] as String[] | ['foo'] as String[]
    }

    private static String compareString(int value)
    {
        if(value < 0) {
            return 'is less than'
        }

        if(value > 0) {
            return 'is greater than'
        }

        return 'is equal to'
    }

    def 'serializable'() {
        expect:
        JUnitTools.testSerialization(object)

        where:
        object << [
                new SemanticVersion(1, 2, 3),
                new SemanticVersion(1, 2, 3, ['foo', 'bar'] as String[]),
                new SemanticVersion(1, 2, 3, ['foo', 'bar'] as String[], ['17', 'foobar', '1'] as String[])
        ]
    }
}
