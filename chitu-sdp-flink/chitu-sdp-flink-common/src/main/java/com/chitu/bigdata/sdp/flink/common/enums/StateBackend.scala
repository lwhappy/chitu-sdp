
package com.chitu.bigdata.sdp.flink.common.enums

object StateBackend extends Enumeration {
  type StateBackend = Value
  val jobmanager, filesystem, rocksdb, hashmap = Value
}
