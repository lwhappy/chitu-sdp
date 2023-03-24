
package com.chitu.bigdata.sdp.flink.common.enums

object RestartStrategy extends Enumeration {
  type RestartStrategy = Value
  val `fixed-delay`, `failure-rate`, `none` = Value

  /**
   *
   * @param name
   * @return
   */
  def byName(name: String): Value = {
    if (name == null) null else {
      values.find(_.toString.replace("$minus", "-").equalsIgnoreCase(name)) match {
        case Some(v) => v
        case _ => throw new IllegalArgumentException("[SDP] RestartStrategy must be (fixed-delay|failure-rate|none)")
      }
    }
  }

}
