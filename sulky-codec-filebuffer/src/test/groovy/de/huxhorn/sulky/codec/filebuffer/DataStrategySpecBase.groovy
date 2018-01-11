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
package de.huxhorn.sulky.codec.filebuffer

import de.huxhorn.sulky.codec.Codec
import de.huxhorn.sulky.codec.SerializableCodec
import spock.lang.Specification

abstract class DataStrategySpecBase
  extends Specification {

  Codec<String> codec = new SerializableCodec<String>()
  IndexStrategy indexStrategy = new DefaultIndexStrategy()

  File indexFile
  File dataFile
  DataStrategy<String> instance

  def setup() {
    indexFile = File.createTempFile("index", "tst")
    indexFile.delete()
    dataFile = File.createTempFile("data", "tst")
    dataFile.delete()
    initInstance()
  }

  abstract void initInstance()

  def cleanup() {
    indexFile.delete()
    dataFile.delete()
  }

  def "add and get"() {
    setup:
    String value1 = "Foo"
    String value2 = "Bar"
    RandomAccessFile randomIndexFile = new RandomAccessFile(indexFile, "rw")
    RandomAccessFile randomDataFile = new RandomAccessFile(dataFile, "rw")

    when:
    instance.add(value1, randomIndexFile, randomDataFile, codec, indexStrategy)
    instance.add(value2, randomIndexFile, randomDataFile, codec, indexStrategy)
    String readValue1 = instance.get(0, randomIndexFile, randomDataFile, codec, indexStrategy)
    String readValue2 = instance.get(1, randomIndexFile, randomDataFile, codec, indexStrategy)
    closeQuietly(randomIndexFile)
    closeQuietly(randomDataFile)

    then:
    value1 == readValue1
    value2 == readValue2
  }

  def "addAll and get"() {
    setup:
    String value1 = "Foo"
    String value2 = "Bar"
    List<String> list = [value1, value2]
    RandomAccessFile randomIndexFile = new RandomAccessFile(indexFile, "rw")
    RandomAccessFile randomDataFile = new RandomAccessFile(dataFile, "rw")

    when:
    instance.addAll(list, randomIndexFile, randomDataFile, codec, indexStrategy)
    String readValue1 = instance.get(0, randomIndexFile, randomDataFile, codec, indexStrategy)
    String readValue2 = instance.get(1, randomIndexFile, randomDataFile, codec, indexStrategy)
    closeQuietly(randomIndexFile)
    closeQuietly(randomDataFile)

    then:
    value1 == readValue1
    value2 == readValue2
  }

  static void closeQuietly(RandomAccessFile raf) {
    if(raf != null) {
      try {
        raf.close()
      }
      catch(IOException e) {
        e.printStackTrace()
      }
    }
  }
}
