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

package de.huxhorn.sulky.version

import de.huxhorn.sulky.version.mappers.DuplicateClassMapper
import de.huxhorn.sulky.version.mappers.HighestVersionMapper
import de.huxhorn.sulky.version.mappers.PackageVersionMapper
import de.huxhorn.sulky.version.mappers.SourceVersionMapper
import org.apache.commons.io.FileUtils
import org.apache.commons.io.IOUtils
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import spock.lang.Specification
import spock.lang.Subject

import java.util.zip.ZipEntry
import java.util.zip.ZipFile

@Subject(ClassFileScanner)
class ClassFileScannerSpec
    extends Specification {

    private static final String SLF4J_API_179_JAR_NAME = 'slf4j-api-1.7.9.jar'
    private static final String SLF4J_API_1710_JAR_NAME = 'slf4j-api-1.7.10.jar'
    private static final String FOO_JAR_NAME = 'foo.jar'
    private static final String FOO_DIRECTORY_NAME = 'fooDirectory'
    private static final String SLF4J_DIRECTORY_NAME = 'slf4j-api'

    @Rule
    TemporaryFolder temporaryFolder

    File slf4jApi179File
    File slf4jApi1710File
    File fooJarFile
    File unzippedFooDirectory
    File unzippedSlf4jApiDirectory

    def setup() {
        slf4jApi179File = temporaryFolder.newFile(SLF4J_API_179_JAR_NAME);
        FileUtils.copyInputStreamToFile(this.class.getResourceAsStream('/'+SLF4J_API_179_JAR_NAME), slf4jApi179File)

        slf4jApi1710File = temporaryFolder.newFile(SLF4J_API_1710_JAR_NAME);
        FileUtils.copyInputStreamToFile(this.class.getResourceAsStream('/'+SLF4J_API_1710_JAR_NAME), slf4jApi1710File)

        fooJarFile = temporaryFolder.newFile(FOO_JAR_NAME);
        FileUtils.copyInputStreamToFile(this.class.getResourceAsStream('/'+FOO_JAR_NAME), fooJarFile)

        unzippedFooDirectory = temporaryFolder.newFolder(FOO_DIRECTORY_NAME);
        unzip(fooJarFile, unzippedFooDirectory)

        unzippedSlf4jApiDirectory = temporaryFolder.newFolder(SLF4J_DIRECTORY_NAME);
        unzip(slf4jApi1710File, unzippedSlf4jApiDirectory)
    }

    def unzip(File file, File outputDirectory) {
        ZipFile zipFile = new ZipFile(file);
        Enumeration<? extends ZipEntry> entries = zipFile.entries();
        while (entries.hasMoreElements()) {
            ZipEntry entry = entries.nextElement();
            File entryDestination = new File(outputDirectory,  entry.getName());
            entryDestination.getParentFile().mkdirs();
            if (entry.isDirectory()) {
                entryDestination.mkdirs();
                continue;
            }
            FileUtils.copyInputStreamToFile(zipFile.getInputStream(entry), entryDestination);
        }
    }

    def "scan slf4j-api jar"() {
        when:
        ClassFileScanner scanner = new ClassFileScanner()
        HighestVersionMapper highestVersionMapper = new HighestVersionMapper()
        scanner.classStatisticMappers.add(highestVersionMapper)
        PackageVersionMapper packageVersionMapper = new PackageVersionMapper()
        scanner.classStatisticMappers.add(packageVersionMapper)
        scanner.scanJar(slf4jApi1710File)

        then:
        highestVersionMapper.highestVersionChar                          == 0x31 as char
        highestVersionMapper.highestVersion                              == ClassFileVersion.JAVA_1_5
        packageVersionMapper.packageVersions.size()                      == 3
        packageVersionMapper.packageVersions['org.slf4j'].size()         == 1
        packageVersionMapper.packageVersions['org.slf4j'][0]             == 0x31 as char
        packageVersionMapper.packageVersions['org.slf4j.spi'].size()     == 1
        packageVersionMapper.packageVersions['org.slf4j.spi'][0]         == 0x31 as char
        packageVersionMapper.packageVersions['org.slf4j.helpers'].size() == 1
        packageVersionMapper.packageVersions['org.slf4j.helpers'][0]     == 0x31 as char
    }

    def "scan foo jar"() {
        when:
        ClassFileScanner scanner = new ClassFileScanner()
        HighestVersionMapper highestVersionMapper = new HighestVersionMapper()
        scanner.classStatisticMappers.add(highestVersionMapper)
        PackageVersionMapper packageVersionMapper = new PackageVersionMapper()
        scanner.classStatisticMappers.add(packageVersionMapper)
        scanner.scanJar(fooJarFile)

        then:
        highestVersionMapper.highestVersionChar                 == 0x35 as char
        highestVersionMapper.highestVersion                     == ClassFileVersion.JAVA_9
        packageVersionMapper.packageVersions.size()             == 2
        packageVersionMapper.packageVersions[''].size()         == 5
        packageVersionMapper.packageVersions[''][0]             == 0x31 as char
        packageVersionMapper.packageVersions[''][1]             == 0x32 as char
        packageVersionMapper.packageVersions[''][2]             == 0x33 as char
        packageVersionMapper.packageVersions[''][3]             == 0x34 as char
		packageVersionMapper.packageVersions[''][4]             == 0x35 as char
        packageVersionMapper.packageVersions['some.pkg'].size() == 4
        packageVersionMapper.packageVersions['some.pkg'][0]     == 0x31 as char
        packageVersionMapper.packageVersions['some.pkg'][1]     == 0x32 as char
        packageVersionMapper.packageVersions['some.pkg'][2]     == 0x33 as char
        packageVersionMapper.packageVersions['some.pkg'][3]     == 0x34 as char
    }

    def "scan multiple files"() {
        when:
        ClassFileScanner scanner = new ClassFileScanner()
        HighestVersionMapper highestVersionMapper = new HighestVersionMapper()
        scanner.classStatisticMappers.add(highestVersionMapper)
        PackageVersionMapper packageVersionMapper = new PackageVersionMapper()
        scanner.classStatisticMappers.add(packageVersionMapper)
        SourceVersionMapper sourceVersionMapper = new SourceVersionMapper()
        scanner.classStatisticMappers.add(sourceVersionMapper)
        DuplicateClassMapper duplicateClassMapper = new DuplicateClassMapper()
        scanner.classStatisticMappers.add(duplicateClassMapper)
        scanner.scanJar(slf4jApi179File)
        scanner.scanJar(slf4jApi1710File)
        scanner.scanJar(fooJarFile)
        //println("Duplicate classes: "+duplicateClassMapper.duplicates)
        //duplicateClassMapper.duplicates.each { println("Duplicate class: " + it + ":\n\t"+ duplicateClassMapper.classSourceMapping[it]) }

        then:
        highestVersionMapper.highestVersionChar                          == 0x35 as char
        highestVersionMapper.highestVersion                              == ClassFileVersion.JAVA_9
        packageVersionMapper.packageVersions.size()                      == 5
        packageVersionMapper.packageVersions[''].size()                  == 5
        packageVersionMapper.packageVersions[''][0]                      == 0x31 as char
        packageVersionMapper.packageVersions[''][1]                      == 0x32 as char
        packageVersionMapper.packageVersions[''][2]                      == 0x33 as char
        packageVersionMapper.packageVersions[''][3]                      == 0x34 as char
		packageVersionMapper.packageVersions[''][4]                      == 0x35 as char
        packageVersionMapper.packageVersions['some.pkg'].size()          == 4
        packageVersionMapper.packageVersions['some.pkg'][0]              == 0x31 as char
        packageVersionMapper.packageVersions['some.pkg'][1]              == 0x32 as char
        packageVersionMapper.packageVersions['some.pkg'][2]              == 0x33 as char
        packageVersionMapper.packageVersions['some.pkg'][3]              == 0x34 as char
        packageVersionMapper.packageVersions['org.slf4j'].size()         == 1
        packageVersionMapper.packageVersions['org.slf4j'][0]             == 0x31 as char
        packageVersionMapper.packageVersions['org.slf4j.spi'].size()     == 1
        packageVersionMapper.packageVersions['org.slf4j.spi'][0]         == 0x31 as char
        packageVersionMapper.packageVersions['org.slf4j.helpers'].size() == 1
        packageVersionMapper.packageVersions['org.slf4j.helpers'][0]     == 0x31 as char
        sourceVersionMapper.sourceVersions[SLF4J_API_179_JAR_NAME][0]    == 0x31 as char
        sourceVersionMapper.sourceVersions[SLF4J_API_1710_JAR_NAME][0]   == 0x31 as char
        sourceVersionMapper.sourceVersions[FOO_JAR_NAME][0]              == 0x31 as char
        sourceVersionMapper.sourceVersions[FOO_JAR_NAME][1]              == 0x32 as char
        sourceVersionMapper.sourceVersions[FOO_JAR_NAME][2]              == 0x33 as char
        sourceVersionMapper.sourceVersions[FOO_JAR_NAME][3]              == 0x34 as char
        duplicateClassMapper.duplicates.size() != 0
    }

    def "scan with reset"() {
        when:
        ClassFileScanner scanner = new ClassFileScanner()
        HighestVersionMapper highestVersionMapper = new HighestVersionMapper()
        scanner.classStatisticMappers.add(highestVersionMapper)
        PackageVersionMapper packageVersionMapper = new PackageVersionMapper()
        scanner.classStatisticMappers.add(packageVersionMapper)
        DuplicateClassMapper duplicateClassMapper = new DuplicateClassMapper()
        scanner.classStatisticMappers.add(duplicateClassMapper)
        scanner.scanJar(fooJarFile)
        scanner.scanJar(slf4jApi179File)
        scanner.scanJar(slf4jApi1710File)
        scanner.reset()

        then:
        highestVersionMapper.highestVersionChar                 == 0
        highestVersionMapper.highestVersion                     == null
        packageVersionMapper.packageVersions.size()             == 0
        duplicateClassMapper.classSourceMapping.size()          == 0
        duplicateClassMapper.duplicates.size()                  == 0
    }

    def "scan directories"() {
        when:
        ClassFileScanner scanner = new ClassFileScanner()
        HighestVersionMapper highestVersionMapper = new HighestVersionMapper()
        scanner.classStatisticMappers.add(highestVersionMapper)
        PackageVersionMapper packageVersionMapper = new PackageVersionMapper()
        scanner.classStatisticMappers.add(packageVersionMapper)
        SourceVersionMapper sourceVersionMapper = new SourceVersionMapper()
        scanner.classStatisticMappers.add(sourceVersionMapper)
        scanner.scanDirectory(unzippedFooDirectory)
        scanner.scanDirectory(unzippedSlf4jApiDirectory)
        scanner.scanDirectory(unzippedSlf4jApiDirectory, 'manualSource')

        then:
        highestVersionMapper.highestVersionChar                          == 0x35 as char
        highestVersionMapper.highestVersion                              == ClassFileVersion.JAVA_9
        packageVersionMapper.packageVersions.size()                      == 5
        packageVersionMapper.packageVersions[''].size()                  == 5
        packageVersionMapper.packageVersions[''][0]                      == 0x31 as char
        packageVersionMapper.packageVersions[''][1]                      == 0x32 as char
        packageVersionMapper.packageVersions[''][2]                      == 0x33 as char
        packageVersionMapper.packageVersions[''][3]                      == 0x34 as char
		packageVersionMapper.packageVersions[''][4]                      == 0x35 as char
        packageVersionMapper.packageVersions['some.pkg'].size()          == 4
        packageVersionMapper.packageVersions['some.pkg'][0]              == 0x31 as char
        packageVersionMapper.packageVersions['some.pkg'][1]              == 0x32 as char
        packageVersionMapper.packageVersions['some.pkg'][2]              == 0x33 as char
        packageVersionMapper.packageVersions['some.pkg'][3]              == 0x34 as char
        packageVersionMapper.packageVersions['org.slf4j'].size()         == 1
        packageVersionMapper.packageVersions['org.slf4j'][0]             == 0x31 as char
        packageVersionMapper.packageVersions['org.slf4j.spi'].size()     == 1
        packageVersionMapper.packageVersions['org.slf4j.spi'][0]         == 0x31 as char
        packageVersionMapper.packageVersions['org.slf4j.helpers'].size() == 1
        packageVersionMapper.packageVersions['org.slf4j.helpers'][0]     == 0x31 as char
        sourceVersionMapper.sourceVersions[SLF4J_DIRECTORY_NAME][0]      == 0x31 as char
        sourceVersionMapper.sourceVersions[FOO_DIRECTORY_NAME][0]        == 0x31 as char
        sourceVersionMapper.sourceVersions[FOO_DIRECTORY_NAME][1]        == 0x32 as char
        sourceVersionMapper.sourceVersions[FOO_DIRECTORY_NAME][2]        == 0x33 as char
        sourceVersionMapper.sourceVersions[FOO_DIRECTORY_NAME][3]        == 0x34 as char
        sourceVersionMapper.sourceVersions['manualSource'][0]            == 0x31 as char
        sourceVersionMapper.sourceVersions.size()                        == 3
    }
}
