package de.huxhorn.sulky.codec.filebuffer

import spock.lang.Specification

class MetaDataSpec
  extends Specification {

  def "empty not sparse"() {
    when:
    boolean sparse = false
    MetaData instance = new MetaData(sparse)

    then:
    instance == new MetaData(sparse)
    0 == instance.data.size()
    instance != new MetaData(!sparse)
  }

  def "empty sparse"() {
    when:
    boolean sparse = true
    MetaData instance = new MetaData(sparse)

    then:
    instance == new MetaData(sparse)
    0 == instance.data.size()
    instance != new MetaData(!sparse)
  }
}
