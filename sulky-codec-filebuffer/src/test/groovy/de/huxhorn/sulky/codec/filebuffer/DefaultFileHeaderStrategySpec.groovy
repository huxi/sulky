package de.huxhorn.sulky.codec.filebuffer

import spock.lang.Specification
import spock.lang.Unroll

public class DefaultFileHeaderStrategySpec
  extends Specification {

  int magicValue = 0xDEADBEEF

  File file
  DefaultFileHeaderStrategy instance

  def setup() {
    file = File.createTempFile("DFHS", "test");
    instance = new DefaultFileHeaderStrategy()
  }

  def cleanup() {
    file.delete()
  }

  @Unroll
  def "write header and read it. sparse=#sparse, metaDataMap=#metaDataMap"() {
    when:
    FileHeader fileHeader = instance.writeFileHeader(file, magicValue, metaDataMap, sparse)
    Integer readMagicValue = instance.readMagicValue(file)
    FileHeader readFileHeader = instance.readFileHeader(file)

    then:
    null != fileHeader
    fileHeader.metaData == new MetaData(metaDataMap, sparse)
    magicValue == fileHeader.magicValue

    null != readMagicValue
    magicValue == readMagicValue

    fileHeader == readFileHeader

    where:
    sparse | metaDataMap
    false  | null
    false  | [foo: 'bar', foo2: 'bar2']
    true   | null
    true   | [foo: 'bar', foo2: 'bar2']
  }
}
