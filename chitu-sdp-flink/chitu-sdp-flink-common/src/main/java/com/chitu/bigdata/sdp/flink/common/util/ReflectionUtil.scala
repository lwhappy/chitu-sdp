package com.chitu.bigdata.sdp.flink.common.util

/**
 * @author sutao
 * @create 2022-03-29 9:46
 */
object ReflectionUtil {


  private val KAFKA_SOURCE_CLASS = "org.apache.flink.connector.kafka.source.reader.DeserializeException"

  private val FLINK_STREAMING_SOURCE_CLASS = "org.apache.flink.streaming.api.functions.sink.filesystem.CustomFileSystemFactory"


  def checkOpenSourceClassExist() {
    try Class.forName(KAFKA_SOURCE_CLASS)
    catch {
      case e: ClassNotFoundException =>
        throw new RuntimeException("kafka connector 依赖的是开源jar，非修改后的jar，请检查！")
    }

    try Class.forName(FLINK_STREAMING_SOURCE_CLASS)
    catch {
      case e: ClassNotFoundException =>
        throw new RuntimeException("flink-streaming 依赖的是开源jar，非修改后的jar，请检查！")
    }

  }



}
