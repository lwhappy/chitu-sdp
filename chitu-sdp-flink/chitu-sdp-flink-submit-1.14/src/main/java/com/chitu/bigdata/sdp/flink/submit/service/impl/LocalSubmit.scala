package com.chitu.bigdata.sdp.flink.submit.service.impl

import com.chitu.bigdata.sdp.flink.submit.service.`trait`.FlinkSubmitTrait
import com.chitu.bigdata.sdp.flink.submit.service.{SubmitRequest, SubmitResponse}

import java.lang

object LocalSubmit extends FlinkSubmitTrait {
  override def doSubmit(submitInfo: SubmitRequest): SubmitResponse = {
    throw new UnsupportedOperationException("Unsupported local Submit ")
  }

  override def doStop(flinkHome: String, appId: String, jobStringId: String, savePoint: lang.Boolean, drain: lang.Boolean,cancelTimeout: Long): String = {
    throw new UnsupportedOperationException("Unsupported local Submit ")
  }

  override def doTriggerSavepoint(flinkHome: String, appId: String, jobStringId: String, savePoint: lang.Boolean, cancelTimeout: Long,savepointDirectory:String):   String =  {
    throw new UnsupportedOperationException("Unsupported local TriggerSavepoint ")
  }
}
