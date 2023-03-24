
package com.chitu.bigdata.sdp.flink.common.enums

object CheckpointStorage extends Enumeration {
  type CheckpointStorage = Value
  val jobmanager, filesystem = Value
}
