package de.huxhorn.sulky.codec.filebuffer

import spock.lang.Specification
import de.huxhorn.sulky.codec.Codec
import de.huxhorn.sulky.codec.SerializableCodec
import spock.lang.Unroll
import de.huxhorn.sulky.buffers.ElementProcessor

class CodecFileBufferSpec
  extends Specification {

  File tempOutputPath;
  File dataFile;
  File indexFile;

  String[] values = [
      'Null, sort of nothing',
      'One',
      'Two',
      'Three',
      'Four',
      'Five',
      'Six',
      'Seven',
      'Eight',
      'Nine',
      'Ten'
    ]

  int magicValue = 0xDEADBEEF

  Codec<String> codec = new SerializableCodec<String>()
  FileHeaderStrategy fileHeaderStrategy = new DefaultFileHeaderStrategy()

  def sparseAndMetaDataData() {
    [
            [false, null],
            [true, null],
            [false, [ foo1: 'bar1', foo2: 'bar2' ]],
            [true, [ foo1: 'bar1', foo2: 'bar2' ]],
    ]
  }

  def metaData() {
    [
      null,
      [ foo1: 'bar1', foo2: 'bar2' ]
    ]
  }

  def setup() {
    tempOutputPath = File.createTempFile("sfb-testing", "rulez");
    tempOutputPath.delete();
    tempOutputPath.mkdirs();
    dataFile = new File(tempOutputPath, "dump");
    indexFile = new File(tempOutputPath, "dump.index");
  }

  def cleanup() {
    dataFile.delete();
    indexFile.delete();
    tempOutputPath.delete();
  }

  @Unroll
  def "add(), then get() and Iterable. (sparse=#sparse, metaData=#metaDataData)"(boolean sparse, Map<String, String> metaDataData) {
    when:
    CodecFileBuffer<String> instance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy)
    for(String current : values) {
      instance.add(current);
    }


    then:
    values.length == instance.size
    for(int i = 0; i < values.length; i++) {
      assert values[i] == instance.get(i)
    }

    int index = 0;
    for(String value : instance) {
      assert value == values[index]
      index++
    }

    instance.fileHeader.metaData == new MetaData(metaDataData, sparse)
    instance.fileHeader.magicValue == magicValue

    CodecFileBuffer<String> otherInstance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy)
    values.length == otherInstance.size
    otherInstance.fileHeader.metaData == new MetaData(metaDataData, sparse)
    otherInstance.fileHeader.magicValue == magicValue

    where:
    [sparse, metaDataData] << sparseAndMetaDataData()
  }

  @Unroll
  def "addAll(), then get() and Iterable. (sparse=#sparse, metaData=#metaDataData)"(boolean sparse, Map<String, String> metaDataData) {
    when:
    CodecFileBuffer<String> instance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy)
    instance.addAll(values);

    then:
    values.length == instance.size
    for(int i = 0; i < values.length; i++) {
      assert values[i] == instance.get(i)
    }

    int index = 0;
    for(String value : instance) {
      assert value == values[index]
      index++
    }

    instance.fileHeader.metaData == new MetaData(metaDataData, sparse)
    instance.fileHeader.magicValue == magicValue

    CodecFileBuffer<String> otherInstance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy)
    values.length == otherInstance.size
    otherInstance.fileHeader.metaData == new MetaData(metaDataData, sparse)
    otherInstance.fileHeader.magicValue == magicValue

    where:
    [sparse, metaDataData] << sparseAndMetaDataData()
  }

  @Unroll
  def "mixed add() and addAll(), then get() and Iterable. (sparse=#sparse, metaData=#metaDataData)"(boolean sparse, Map<String, String> metaDataData) {
    when:
    CodecFileBuffer<String> instance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy)
    instance.addAll(values)
    for(String current : values) {
      instance.add(current)
    }
    instance.addAll(values)
    for(String current : values) {
      instance.add(current)
    }

    then:
    values.length*4 == instance.size
    for(int i = 0; i < values.length*4; i++) {
      assert values[i%values.length] == instance.get(i)
    }

    int index = 0;
    for(String value : instance) {
      assert value == values[index%values.length]
      index++
    }

    instance.fileHeader.metaData == new MetaData(metaDataData, sparse)
    instance.fileHeader.magicValue == magicValue

    CodecFileBuffer<String> otherInstance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy)
    values.length*4 == otherInstance.size
    otherInstance.fileHeader.metaData == new MetaData(metaDataData, sparse)
    otherInstance.fileHeader.magicValue == magicValue

    where:
    [sparse, metaDataData] << sparseAndMetaDataData()
  }

  @Unroll
  def "read invalid entry from empty file. (sparse=#sparse, metaData=#metaDataData)"(boolean sparse, Map<String, String> metaDataData) {
    when:
    CodecFileBuffer<String> instance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy);

    then:
    null == instance.get(0)
    null == instance.get(10)

    where:
    [sparse, metaDataData] << sparseAndMetaDataData()
  }

  @Unroll
  def "read invalid entry from file with data. (sparse=#sparse, metaData=#metaDataData)"(boolean sparse, Map<String, String> metaDataData) {
    when:
    CodecFileBuffer<String> instance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy);
    instance.addAll(values)

    then:
    null != instance.get(0)
    null == instance.get(values.length)
    null != instance.get(values.length-1)

    where:
    [sparse, metaDataData] << sparseAndMetaDataData()
  }

  @Unroll
  def "Check headers of new instances. (sparse=#sparse, metaData=#metaDataData)"(boolean sparse, Map<String, String> metaDataData) {
    when:
    CodecFileBuffer<String> instance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy);

    then:
    0 == instance.size
    instance.fileHeader.metaData == new MetaData(metaDataData, sparse)
    instance.fileHeader.magicValue == magicValue

    CodecFileBuffer<String> otherInstance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy);
    0 == otherInstance.size
    otherInstance.fileHeader.metaData == new MetaData(metaDataData, sparse)
    otherInstance.fileHeader.magicValue == magicValue

    where:
    [sparse, metaDataData] << sparseAndMetaDataData()
  }

  @Unroll
  def "isSetSupported() (sparse=#sparse, metaData=#metaDataData)"(boolean sparse, Map<String, String> metaDataData) {
    when:
    CodecFileBuffer<String> instance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy);

    then:
    sparse == instance.setSupported

    where:
    [sparse, metaDataData] << sparseAndMetaDataData()
  }

  @Unroll
  def "set() on non-sparse instance (metaData=#metaDataData)"(Map<String, String> metaDataData) {
    when:
    CodecFileBuffer<String> instance = new CodecFileBuffer<String>(magicValue, false, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy);
    instance.set(0, "Will fail!");

    then:
    UnsupportedOperationException e = thrown()
    "DefaultDataStrategy does not support set!" == e.message
    where:
    metaDataData << metaData()
  }

  @Unroll
  def "set(), then then get() and Iterable, on sparse instance (metaData=#metaDataData)"(Map<String, String> metaDataData) {
    when:
    CodecFileBuffer<String> instance = new CodecFileBuffer<String>(magicValue, true, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy);
    for(int i = values.length - 1; i >= 0; i--) {
      instance.set(i, values[i]);
    }

    then:
    values.length == instance.size
    for(int i = 0; i < values.length; i++) {
      assert values[i] == instance.get(i)
    }

    int index = 0;
    for(String value : instance) {
      assert value == values[index]
      index++
    }

    instance.fileHeader.metaData == new MetaData(metaDataData, true)
    instance.fileHeader.magicValue == magicValue

    CodecFileBuffer<String> otherInstance = new CodecFileBuffer<String>(magicValue, true, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy)
    values.length == otherInstance.size
    otherInstance.fileHeader.metaData == new MetaData(metaDataData, true)
    otherInstance.fileHeader.magicValue == magicValue

    where:
    metaDataData << metaData()
  }

  @Unroll
  def "add() with element processor (sparse=#sparse, metaData=#metaDataData)"(boolean sparse, Map<String, String> metaDataData) {
    setup:
    CapturingStringElementProcessor processor = new CapturingStringElementProcessor()
    List<ElementProcessor<String>> elementProcessors = [processor]

    when:
    CodecFileBuffer<String> instance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy);
    instance.setElementProcessors(elementProcessors)

    for(String current : values) {
      instance.add(current);
    }

    then:
    values == processor.list

    where:
    [sparse, metaDataData] << sparseAndMetaDataData()
  }

  @Unroll
  def "addAll() with element processor (sparse=#sparse, metaData=#metaDataData)"(boolean sparse, Map<String, String> metaDataData) {
    setup:
    CapturingStringElementProcessor processor = new CapturingStringElementProcessor()
    List<ElementProcessor<String>> elementProcessors = [processor]

    when:
    CodecFileBuffer<String> instance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy);
    instance.setElementProcessors(elementProcessors)

    instance.addAll(values);

    then:
    values == processor.list

    where:
    [sparse, metaDataData] << sparseAndMetaDataData()
  }

  @Unroll
  def "reset non-empty buffer (sparse=#sparse, metaData=#metaDataData)"(boolean sparse, Map<String, String> metaDataData) {
    when:
    CodecFileBuffer<String> instance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy)
    instance.addAll(values)
    instance.reset()

    then:
    instance.fileHeader.metaData == new MetaData(metaDataData, sparse)
    instance.fileHeader.magicValue == magicValue

    0 == instance.size

    where:
    [sparse, metaDataData] << sparseAndMetaDataData()
  }

  @Unroll
  def "reset, then add to non-empty buffer (sparse=#sparse, metaData=#metaDataData)"(boolean sparse, Map<String, String> metaDataData) {
    when:
    CodecFileBuffer<String> instance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy)
    instance.addAll(values)
    instance.reset()
    instance.addAll(values)

    then:
    instance.fileHeader.metaData == new MetaData(metaDataData, sparse)
    instance.fileHeader.magicValue == magicValue

    values.length == instance.size
    for(int i = 0; i < values.length; i++) {
      assert values[i] == instance.get(i)
    }

    int index = 0;
    for(String value : instance) {
      assert value == values[index]
      index++
    }

    where:
    [sparse, metaDataData] << sparseAndMetaDataData()
  }

  @Unroll
  def "add, delete data file, then add again (sparse=#sparse, metaData=#metaDataData)"(boolean sparse, Map<String, String> metaDataData) {
    when:
    CodecFileBuffer<String> instance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy)
    for(String current : values) {
      instance.add(current)
    }
    assert dataFile.delete()
    for(String current : values) {
      instance.add(current)
    }

    then:
    8*values.length == indexFile.length()
    instance.fileHeader.metaData == new MetaData(metaDataData, sparse)
    instance.fileHeader.magicValue == magicValue

    values.length == instance.size
    for(int i = 0; i < values.length; i++) {
      assert values[i] == instance.get(i)
    }

    int index = 0;
    for(String value : instance) {
      assert value == values[index]
      index++
    }

    where:
    [sparse, metaDataData] << sparseAndMetaDataData()
  }

  @Unroll
  def "addAll, delete data file, then addAll again (sparse=#sparse, metaData=#metaDataData)"(boolean sparse, Map<String, String> metaDataData) {
    when:
    CodecFileBuffer<String> instance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy)
    instance.addAll(values)
    assert dataFile.delete()
    instance.addAll(values)

    then:
    8*values.length == indexFile.length()
    instance.fileHeader.metaData == new MetaData(metaDataData, sparse)
    instance.fileHeader.magicValue == magicValue

    values.length == instance.size
    for(int i = 0; i < values.length; i++) {
      assert values[i] == instance.get(i)
    }

    int index = 0;
    for(String value : instance) {
      assert value == values[index]
      index++
    }

    where:
    [sparse, metaDataData] << sparseAndMetaDataData()
  }

  @Unroll
  def "addAll, delete data file, then get (sparse=#sparse, metaData=#metaDataData)"(boolean sparse, Map<String, String> metaDataData) {
    when:
    CodecFileBuffer<String> instance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy)
    instance.addAll(values)
    assert dataFile.delete()

    then:
    null == instance.get(0)
    //0 == instance.size

    where:
    [sparse, metaDataData] << sparseAndMetaDataData()
  }

  @Unroll
  def "add, delete index file, then add (sparse=#sparse, metaData=#metaDataData)"(boolean sparse, Map<String, String> metaDataData) {
    when:
    CodecFileBuffer<String> instance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy)
    for(String current : values) {
      instance.add(current)
    }
    assert indexFile.delete()
    for(String current : values) {
      instance.add(current)
    }

    then:
    8*values.length == indexFile.length()
    instance.fileHeader.metaData == new MetaData(metaDataData, sparse)
    instance.fileHeader.magicValue == magicValue

    values.length == instance.size
    for(int i = 0; i < values.length; i++) {
      assert values[i] == instance.get(i)
    }

    int index = 0;
    for(String value : instance) {
      assert value == values[index]
      index++
    }

    where:
    [sparse, metaDataData] << sparseAndMetaDataData()
  }

  @Unroll
  def "addAll, delete index file, then addAll (sparse=#sparse, metaData=#metaDataData)"(boolean sparse, Map<String, String> metaDataData) {
    when:
    CodecFileBuffer<String> instance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy)
    instance.addAll(values)
    assert indexFile.delete()
    instance.addAll(values)

    then:
    8*values.length == indexFile.length()
    instance.fileHeader.metaData == new MetaData(metaDataData, sparse)
    instance.fileHeader.magicValue == magicValue

    values.length == instance.size
    for(int i = 0; i < values.length; i++) {
      assert values[i] == instance.get(i)
    }

    int index = 0;
    for(String value : instance) {
      assert value == values[index]
      index++
    }

    where:
    [sparse, metaDataData] << sparseAndMetaDataData()
  }

  /**
   * This is the case that an existing file containing data is reopened. In that case, an IllegalArgumentException is
   * thrown instead of simply overwriting the previous data (which is done if the indexFile is deleted while
   * "in production").
   * @param sparse
   * @param metaDataData
   * @return
   */
  @Unroll
  def "delete index file and reopen (sparse=#sparse, metaData=#metaDataData)"(boolean sparse, Map<String, String> metaDataData) {
    setup:
    String indexFileName = indexFile.absolutePath
    when:
    CodecFileBuffer<String> instance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy)
    instance.addAll(values)
    assert indexFile.delete()
    CodecFileBuffer<String> otherInstance = new CodecFileBuffer<String>(magicValue, sparse, metaDataData, codec, dataFile, indexFile, fileHeaderStrategy)

    then:
    IllegalArgumentException e = thrown()
    e.message == "dataFile contains data but indexFile ${indexFileName} is not valid!"


    where:
    [sparse, metaDataData] << sparseAndMetaDataData()
  }

  static class CapturingStringElementProcessor
    implements ElementProcessor<String> {

    List<String> list = new ArrayList<String>();

    public void processElement(String element) {
      list.add(element);
    }

    public void processElements(List<String> element) {
      list.addAll(element);
    }
  }
}

