/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2016 Joern Huxhorn
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
 * Copyright 2007-2016 Joern Huxhorn
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

package de.huxhorn.sulky.junit

import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Unroll

import java.time.DayOfWeek

class JUnitToolsSpec extends Specification {

	@Rule
	public TemporaryFolder folder = new TemporaryFolder()

	def validDifferentCloneValues() {
		[
				new WorkingCloneableClass('foo'),
		]
	}

	def validSameCloneValues() {
		[
				HackyCloneableSingletonClass.INSTANCE,
		]
	}

	def validCloneValues() {
		def result = []
		validDifferentCloneValues().each {
			result << it
		}
		validSameCloneValues().each {
			result << it
		}
		println result
		return result
	}

	def validCloneValuesSame() {
		def result = []
		validDifferentCloneValues().each {
			result << false
		}
		validSameCloneValues().each {
			result << true
		}
		return result
	}

	def validDifferentValues() {
		[
				new Integer(17),
				'a string',
		]
	}

	def validSameValues() {
		[
				DayOfWeek.FRIDAY,
		]
	}

	def validValues() {
		def result = []
		validDifferentValues().each {
			result << it
		}
		validSameValues().each {
			result << it
		}
		return result
	}

	def validValuesSame() {
		def result = []
		validDifferentValues().each {
			result << false
		}
		validSameValues().each {
			result << true
		}
		return result
	}

	public static class WorkingCloneableClass
		implements Cloneable
	{
		private String value

		WorkingCloneableClass(String value) {
			this.value = value
		}


		@Override
		public Object clone() throws CloneNotSupportedException {
			return super.clone()
		}

		boolean equals(o) {
			if (this.is(o)) return true
			if (getClass() != o.class) return false

			WorkingCloneableClass that = (WorkingCloneableClass) o

			if (value != that.value) return false

			return true
		}

		int hashCode() {
			return (value != null ? value.hashCode() : 0)
		}

		@Override
		public String toString() {
			return 'WorkingCloneableClass{' +
					'value=\'' + value + '\'' +
					'}'
		}
	}

	public static class HackyCloneableSingletonClass
			implements Cloneable
	{
		public static HackyCloneableSingletonClass INSTANCE = new HackyCloneableSingletonClass('Connor MacLeod')
		private String value

		private HackyCloneableSingletonClass(String value) {
			this.value = value
		}


		@Override
		public Object clone() throws CloneNotSupportedException {
			return INSTANCE
		}

		boolean equals(o) {
			if (this.is(o)) return true
			if (getClass() != o.class) return false

			WorkingCloneableClass that = (WorkingCloneableClass) o

			if (value != that.value) return false

			return true
		}

		int hashCode() {
			return (value != null ? value.hashCode() : 0)
		}

		@Override
		public String toString() {
			return 'HackyCloneableSingletonClass{' +
					'value=\'' + value + '\'' +
					'}'
		}
	}

	public static class ClassWithoutDefaultConstructor
			implements Serializable {
		private String value

		public ClassWithoutDefaultConstructor(String value) {
			this.value = value
		}

		boolean equals(o) {
			if (this.is(o)) return true
			if (getClass() != o.class) return false

			ClassWithoutDefaultConstructor that = (ClassWithoutDefaultConstructor) o

			if (value != that.value) return false

			return true
		}

		int hashCode() {
			return (value != null ? value.hashCode() : 0)
		}

		@Override
		public String toString() {
			return 'ClassWithoutDefaultConstructor{' +
					'value=\'' + value + '\'' +
					'}'
		}
	}

	@Unroll
	def 'testClone(#value, #same) works as expected.'() {
		when:
		def result = JUnitTools.testClone(value, same)
		then:
		result != null

		when:
		JUnitTools.testClone(value, !same)
		then:
		thrown(AssertionError)

		where:
		value << validCloneValues()
		same << validCloneValuesSame()
	}

	@Unroll
	def 'testClone(#value) does not fail.'() {
		expect:
		JUnitTools.testClone(value) != null

		where:
		value << validDifferentCloneValues()
	}

	@Unroll
	def 'testClone(#value) fails.'() {
		when:
		JUnitTools.testClone(value)

		then:
		thrown(AssertionError)

		where:
		value << validSameCloneValues()
	}

	@Unroll
	def 'testSerialization(#value, #same) works as expected.'() {
		when:
		def result = JUnitTools.testXmlSerialization(value, same)
		then:
		result != null

		when:
		JUnitTools.testSerialization(value, !same)
		then:
		thrown(AssertionError)

		where:
		value << validValues()
		same << validValuesSame()
	}

	@Unroll
	def 'testSerialization(#value) does not fail.'() {
		expect:
		JUnitTools.testSerialization(value) != null

		where:
		value << validDifferentValues()
	}

	@Unroll
	def 'testSerialization(#value) fails.'() {
		when:
		JUnitTools.testSerialization(value)

		then:
		thrown(AssertionError)

		where:
		value << validSameValues()
	}

	@Unroll
	def 'testXmlSerialization(#value, #same) works as expected.'() {
		when:
		def result = JUnitTools.testXmlSerialization(value, same)
		then:
		result != null

		when:
		JUnitTools.testXmlSerialization(value, !same)
		then:
		thrown(AssertionError)

		where:
		value << validValues()
		same << validValuesSame()
	}

	@Unroll
	def 'testXmlSerialization(#value) does not fail.'() {
		expect:
		JUnitTools.testXmlSerialization(value) != null

		where:
		value << validDifferentValues()
	}

	@Unroll
	def 'testXmlSerialization(#value) fails.'() {
		when:
		JUnitTools.testXmlSerialization(value)

		then:
		thrown(AssertionError)

		where:
		value << validSameValues()
	}

	@Unroll
	def 'testXmlSerialization(#value) also fails.'() {
		when:
		JUnitTools.testXmlSerialization(value)

		then:
		thrown(Throwable)

		where:
		value << [
				new ClassWithoutDefaultConstructor('nope'),
		]
	}

	@Unroll
	def 'equal(null, null, same) works regardless of "same" value.'() {
		expect:
		JUnitTools.equal(null, null, true)
		JUnitTools.equal(null, null, false)
	}

	@Unroll
	def 'copyResourceToFile("/#resourceName", ..., specificTime) works as expected.'() {
		setup:
		File rootDirectory = folder.newFolder()
		println rootDirectory.absolutePath
		File targetFile = new File(rootDirectory, resourceName)
		long lastModified = System.currentTimeMillis()

		// we just aim for seconds precision because some OS/FS suck
		lastModified = ((long)(lastModified/1000L))*1000L
		lastModified = lastModified - (3600 * 1000) // subtract one hour for good measures

		when:
		JUnitTools.copyResourceToFile('/'+resourceName, targetFile, lastModified)

		then:
		targetFile.isFile()
		targetFile.lastModified() == lastModified

		where:
		resourceName << ['logback-test.xml']
	}

	@Unroll
	def 'copyResourceToFile("/#resourceName", ..., negativeValue) works as expected.'() {
		setup:
		File rootDirectory = folder.newFolder()
		println rootDirectory.absolutePath
		File targetFile = new File(rootDirectory, resourceName)
		long lastModified = System.currentTimeMillis()

		// we just aim for seconds precision because some OS/FS suck
		lastModified = ((long)(lastModified/1000L))*1000L

		when:
		JUnitTools.copyResourceToFile('/'+resourceName, targetFile, -1)

		then:
		targetFile.isFile()
		targetFile.lastModified() >= lastModified

		where:
		resourceName << ['logback-test.xml']
	}

	@Unroll
	def 'copyResourceToFile("/#resourceName", ...) works as expected.'() {
		setup:
		File rootDirectory = folder.newFolder()
		println rootDirectory.absolutePath
		File targetFile = new File(rootDirectory, resourceName)
		long lastModified = System.currentTimeMillis()

		// we just aim for seconds precision because some OS/FS suck
		lastModified = ((long)(lastModified/1000L))*1000L

		when:
		JUnitTools.copyResourceToFile('/'+resourceName, targetFile)

		then:
		targetFile.isFile()
		targetFile.lastModified() >= lastModified

		where:
		resourceName << ['logback-test.xml']
	}

	@Unroll
	def 'copyResourceToFile("/#resourceName", ..., specificTime) fails as expected.'() {
		setup:
		File rootDirectory = folder.newFolder()
		println rootDirectory.absolutePath
		File targetFile = new File(rootDirectory, resourceName)

		when:
		JUnitTools.copyResourceToFile('/'+resourceName, targetFile, System.currentTimeMillis())

		then:
		IllegalArgumentException ex = thrown()
		ex.message == 'Could not find resource \'/'+resourceName+'\' in classpath!'

		where:
		resourceName << ['missing-resource.snafu']
	}

}
