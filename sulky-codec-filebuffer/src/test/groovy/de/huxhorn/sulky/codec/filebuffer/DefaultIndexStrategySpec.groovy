package de.huxhorn.sulky.codec.filebuffer

import spock.lang.Specification

class DefaultIndexStrategySpec
  extends Specification {
  File testFile
  DefaultIndexStrategy instance = new DefaultIndexStrategy()

  def setup() {
    testFile = File.createTempFile("index", "tst");
    testFile.delete();
  }

  def cleanup() {
    testFile.delete();
  }

  def "empty getSize"() {
    setup:
    RandomAccessFile raf = new RandomAccessFile(testFile, "rw")

    when:
    long size = instance.getSize(raf)
    raf.close()

    then:
    0 == size
  }

  def "empty getOffset"() {
    setup:
    RandomAccessFile raf = new RandomAccessFile(testFile, "rw")

    when:
    long offset = instance.getOffset(raf, 17)
    raf.close()

    then:
    -1 == offset
  }

  def "first offset"() {
    setup:
    RandomAccessFile raf = new RandomAccessFile(testFile, "rw")

    when:
    instance.setOffset(raf, 0, 17)
    long size = instance.getSize(raf)
    long offset = instance.getOffset(raf, 0)
    raf.close()

    then:
    1 == size
    17 == offset
  }

  def "any offset"() {
    setup:
    RandomAccessFile raf = new RandomAccessFile(testFile, "rw")
    long index = 17
    long value = 42

    when:
    instance.setOffset(raf, index, value)
    long size = instance.getSize(raf)
    long offset = instance.getOffset(raf, index)
    for(int i=0;i<index;i++) {
      assert -1 == instance.getOffset(raf, i)
    }
    raf.close()

    then:
    index+1 == size
    42 == offset
  }

  def "any offset, twice"() {
    setup:
    RandomAccessFile raf = new RandomAccessFile(testFile, "rw")
    long index1 = 17
    long value1 = 1
    long index2 = 42
    long value2 = 2

    when:
    instance.setOffset(raf, index1, value1)
    instance.setOffset(raf, index2, value2)
    long size = instance.getSize(raf)
    long offset1 = instance.getOffset(raf, index1)
    long offset2 = instance.getOffset(raf, index2)
    for(int i=0;i<index2;i++) {
      if(i == index1) {
        continue
      }
      assert -1 == instance.getOffset(raf, i)
    }
    raf.close()

    then:
    index2+1 == size
    value1 == offset1
    value2 == offset2
  }
}
