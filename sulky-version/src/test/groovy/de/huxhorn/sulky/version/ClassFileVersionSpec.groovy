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

package de.huxhorn.sulky.version

import spock.lang.Specification
import spock.lang.Subject
import spock.lang.Unroll

@Subject(ClassFileVersion)
class ClassFileVersionSpec extends Specification {

    @Unroll
    def 'getByMajorVersionChar(#majorVersionAsHex) returns #classFileVersion'(int classFileMajorVersion, String sourceName, ClassFileVersion classFileVersion, String majorVersionAsHex) {
        when:
        ClassFileVersion version = ClassFileVersion.getByMajorVersionChar(classFileMajorVersion as char)

        then:
        version == classFileVersion
        version.sourceName == sourceName
        version.majorVersionCharacter == classFileMajorVersion as char

        where:
		classFileMajorVersion | sourceName | classFileVersion
		0x2D                  | '1.1'      | ClassFileVersion.JAVA_1_1
		0x2E                  | '1.2'      | ClassFileVersion.JAVA_1_2
		0x2F                  | '1.3'      | ClassFileVersion.JAVA_1_3
		0x30                  | '1.4'      | ClassFileVersion.JAVA_1_4
		0x31                  | '1.5'      | ClassFileVersion.JAVA_1_5
		0x32                  | '6'        | ClassFileVersion.JAVA_1_6
		0x33                  | '7'        | ClassFileVersion.JAVA_1_7
		0x34                  | '8'        | ClassFileVersion.JAVA_1_8
		0x35                  | '9'        | ClassFileVersion.JAVA_9
		0x36                  | '10'       | ClassFileVersion.JAVA_10
		0x37                  | '11'       | ClassFileVersion.JAVA_11
		0x38                  | '12'       | ClassFileVersion.JAVA_12
		0x39                  | '13'       | ClassFileVersion.JAVA_13
		0x40                  | '14'       | ClassFileVersion.JAVA_14
		0x41                  | '15'       | ClassFileVersion.JAVA_15
		0x42                  | '16'       | ClassFileVersion.JAVA_16
		0x43                  | '17'       | ClassFileVersion.JAVA_17
		0x44                  | '18'       | ClassFileVersion.JAVA_18
		0x45                  | '19'       | ClassFileVersion.JAVA_19
		0x46                  | '20'       | ClassFileVersion.JAVA_20
		0x47                  | '21'       | ClassFileVersion.JAVA_21
		0x48                  | '22'       | ClassFileVersion.JAVA_22
		0x49                  | '23'       | ClassFileVersion.JAVA_23

		majorVersionAsHex = '0x'+Integer.toHexString(classFileMajorVersion)
    }

    @Unroll
    def 'getBySourceName(\"#sourceName\") returns #classFileVersion'(int classFileMajorVersion, String sourceName, ClassFileVersion classFileVersion) {
        when:
        ClassFileVersion version = ClassFileVersion.getBySourceName(sourceName)

        then:
        version == classFileVersion
        version.sourceName == sourceName
        version.majorVersionCharacter == classFileMajorVersion as char

        where:
		classFileMajorVersion | sourceName | classFileVersion
		0x2D                  | '1.1'      | ClassFileVersion.JAVA_1_1
		0x2E                  | '1.2'      | ClassFileVersion.JAVA_1_2
		0x2F                  | '1.3'      | ClassFileVersion.JAVA_1_3
		0x30                  | '1.4'      | ClassFileVersion.JAVA_1_4
		0x31                  | '1.5'      | ClassFileVersion.JAVA_1_5
		0x32                  | '6'        | ClassFileVersion.JAVA_1_6
		0x33                  | '7'        | ClassFileVersion.JAVA_1_7
		0x34                  | '8'        | ClassFileVersion.JAVA_1_8
		0x35                  | '9'        | ClassFileVersion.JAVA_9
		0x36                  | '10'       | ClassFileVersion.JAVA_10
		0x37                  | '11'       | ClassFileVersion.JAVA_11
		0x38                  | '12'       | ClassFileVersion.JAVA_12
		0x39                  | '13'       | ClassFileVersion.JAVA_13
		0x40                  | '14'       | ClassFileVersion.JAVA_14
		0x41                  | '15'       | ClassFileVersion.JAVA_15
		0x42                  | '16'       | ClassFileVersion.JAVA_16
		0x43                  | '17'       | ClassFileVersion.JAVA_17
		0x44                  | '18'       | ClassFileVersion.JAVA_18
		0x45                  | '19'       | ClassFileVersion.JAVA_19
		0x46                  | '20'       | ClassFileVersion.JAVA_20
		0x47                  | '21'       | ClassFileVersion.JAVA_21
		0x48                  | '22'       | ClassFileVersion.JAVA_22
		0x49                  | '23'       | ClassFileVersion.JAVA_23
	}

    @Unroll
    def 'getBySourceName(#sourceName) returns null'(String sourceName) {
        when:
        ClassFileVersion version = ClassFileVersion.getBySourceName(sourceName)

        then:
        version == null

        where:
        sourceName << ['unknown', null]
    }
}
