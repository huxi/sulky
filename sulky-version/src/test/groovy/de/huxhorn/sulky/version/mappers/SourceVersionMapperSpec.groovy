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

package de.huxhorn.sulky.version.mappers

import de.huxhorn.sulky.version.ClassFileVersion
import spock.lang.Subject

@Subject(SourceVersionMapper)
class SourceVersionMapperSpec extends AbstractMapperSpec {

    @Override
    SourceVersionMapper createInstance() {
        return new SourceVersionMapper()
    }

    def "source versions are collected"() {
        when:
        SourceVersionMapper instance = createInstance()
        instance.evaluate('source1', 'packageName', 'className1', ClassFileVersion.JAVA_1_5.majorVersionCharacter)
        instance.evaluate('source1', 'packageName', 'className2', ClassFileVersion.JAVA_1_6.majorVersionCharacter)
        instance.evaluate('source2', 'packageName', 'className3', ClassFileVersion.JAVA_1_6.majorVersionCharacter)

        then:
        instance.sourceVersions.size() == 2
        instance.sourceVersions['source1'] == [ClassFileVersion.JAVA_1_5.majorVersionCharacter, ClassFileVersion.JAVA_1_6.majorVersionCharacter] as Set
        instance.sourceVersions['source2'] == [ClassFileVersion.JAVA_1_6.majorVersionCharacter] as Set
    }

    def "reset works as expected"() {
        when:
        SourceVersionMapper instance = createInstance()
        instance.evaluate('source1', 'packageName', 'className', ClassFileVersion.JAVA_1_5.majorVersionCharacter)
        instance.evaluate('source2', 'packageName', 'className', ClassFileVersion.JAVA_1_6.majorVersionCharacter)
        instance.reset()

        then:
        instance.sourceVersions.size() == 0
    }

    def "evaluation after reset works as expected"() {
        when:
        SourceVersionMapper instance = createInstance()
        instance.evaluate('source1', 'packageName', 'className', ClassFileVersion.JAVA_1_5.majorVersionCharacter)
        instance.evaluate('source2', 'packageName', 'className', ClassFileVersion.JAVA_1_6.majorVersionCharacter)
        instance.reset()
        instance.evaluate('source1', 'packageName', 'className', ClassFileVersion.JAVA_1_5.majorVersionCharacter)
        instance.evaluate('source2', 'packageName', 'className', ClassFileVersion.JAVA_1_6.majorVersionCharacter)

        then:
        instance.sourceVersions.size() == 2
    }
}
