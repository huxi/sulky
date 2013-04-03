/*
 * sulky-modules - several general-purpose modules.
 * Copyright (C) 2007-2011 Joern Huxhorn
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
 * Copyright 2007-2011 Joern Huxhorn
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

class SparseDataStrategySpec
  extends DataStrategySpecBase {

  @Override
  void initInstance() {
    instance = new SparseDataStrategy<String>();
  }

  def "set"() {
    setup:
    String value1 = "Foo"
    String value2 = "Bar"
    long index1 = 17
    long index2 = 42
    RandomAccessFile randomIndexFile = new RandomAccessFile(indexFile, "rw")
    RandomAccessFile randomDataFile = new RandomAccessFile(dataFile, "rw")

    when:
    boolean result1 = instance.set(index1, value1, randomIndexFile, randomDataFile, codec, indexStrategy)
    boolean result2 = instance.set(index2, value2, randomIndexFile, randomDataFile, codec, indexStrategy)
    String readValue1 = instance.get(index1, randomIndexFile, randomDataFile, codec, indexStrategy)
    String readValue2 = instance.get(index2, randomIndexFile, randomDataFile, codec, indexStrategy)
    int size = indexStrategy.getSize(randomIndexFile)

    then:
    result1
    result2
    size == index2+1
    value1 == readValue1
    value2 == readValue2
    for(int i = 0; i < index2; i++) {
      if(i != index1) {
        assert null == instance.get(i, randomIndexFile, randomDataFile, codec, indexStrategy)
      }
    }
  }

  def "set with overwrite"() {
    setup:
    String value1 = "Foo"
    String value2 = "Bar"
    long index1 = 17
    long index2 = 42
    RandomAccessFile randomIndexFile = new RandomAccessFile(indexFile, "rw")
    RandomAccessFile randomDataFile = new RandomAccessFile(dataFile, "rw")

    when:
    boolean result1 = instance.set(index1, value1, randomIndexFile, randomDataFile, codec, indexStrategy)
    boolean result2 = instance.set(index2, value2, randomIndexFile, randomDataFile, codec, indexStrategy)
    boolean result3 = instance.set(index2, value1, randomIndexFile, randomDataFile, codec, indexStrategy)
    String readValue1 = instance.get(index1, randomIndexFile, randomDataFile, codec, indexStrategy)
    String readValue2 = instance.get(index2, randomIndexFile, randomDataFile, codec, indexStrategy)
    int size = indexStrategy.getSize(randomIndexFile)

    then:
    result1
    result2
    result3
    size == index2+1
    value1 == readValue1
    value1 == readValue2
    for(int i = 0; i < index2; i++) {
      if(i != index1) {
        assert null == instance.get(i, randomIndexFile, randomDataFile, codec, indexStrategy)
      }
    }
  }

  def "set with overwrite disabled"() {
    setup:
    ((SparseDataStrategy)instance).setSupportingOverwrite(false);
    String value1 = "Foo"
    String value2 = "Bar"
    long index1 = 17
    long index2 = 42
    RandomAccessFile randomIndexFile = new RandomAccessFile(indexFile, "rw")
    RandomAccessFile randomDataFile = new RandomAccessFile(dataFile, "rw")

    when:
    boolean result1 = instance.set(index1, value1, randomIndexFile, randomDataFile, codec, indexStrategy)
    boolean result2 = instance.set(index2, value2, randomIndexFile, randomDataFile, codec, indexStrategy)
    boolean result3 = instance.set(index2, value1, randomIndexFile, randomDataFile, codec, indexStrategy)
    String readValue1 = instance.get(index1, randomIndexFile, randomDataFile, codec, indexStrategy)
    String readValue2 = instance.get(index2, randomIndexFile, randomDataFile, codec, indexStrategy)
    int size = indexStrategy.getSize(randomIndexFile)

    then:
    result1
    result2
    !result3
    size == index2+1
    value1 == readValue1
    value2 == readValue2
    for(int i = 0; i < index2; i++) {
      if(i != index1) {
        assert null == instance.get(i, randomIndexFile, randomDataFile, codec, indexStrategy)
      }
    }
  }

  def "set null with overwrite"() {
    setup:
    String value1 = "Foo"
    String value2 = "Bar"
    long index1 = 17
    long index2 = 42
    RandomAccessFile randomIndexFile = new RandomAccessFile(indexFile, "rw")
    RandomAccessFile randomDataFile = new RandomAccessFile(dataFile, "rw")

    when:
    boolean result1 = instance.set(index1, value1, randomIndexFile, randomDataFile, codec, indexStrategy)
    boolean result2 = instance.set(index2, value2, randomIndexFile, randomDataFile, codec, indexStrategy)
    boolean result3 = instance.set(index2, null, randomIndexFile, randomDataFile, codec, indexStrategy)
    String readValue1 = instance.get(index1, randomIndexFile, randomDataFile, codec, indexStrategy)
    String readValue2 = instance.get(index2, randomIndexFile, randomDataFile, codec, indexStrategy)
    int size = indexStrategy.getSize(randomIndexFile)

    then:
    result1
    result2
    result3
    size == index2+1
    value1 == readValue1
    null == readValue2
    for(int i = 0; i < index2; i++) {
      if(i != index1) {
        assert null == instance.get(i, randomIndexFile, randomDataFile, codec, indexStrategy)
      }
    }
  }

  def "isSetSupported"() {
    expect:
    instance.isSetSupported()
  }
}