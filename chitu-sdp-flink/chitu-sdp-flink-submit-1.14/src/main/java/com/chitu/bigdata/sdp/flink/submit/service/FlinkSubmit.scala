package com.chitu.bigdata.sdp.flink.submit.service

import com.chitu.bigdata.sdp.flink.common.enums.ExecutionMode
import com.chitu.bigdata.sdp.flink.submit.service.impl.{LocalSubmit, YarnApplicationSubmit, YarnPreJobSubmit}

import java.lang.{Boolean => JavaBool}

object FlinkSubmit {

  def submit(submitInfo: SubmitRequest): SubmitResponse = {
    submitInfo.executionMode match {
      case ExecutionMode.APPLICATION => YarnApplicationSubmit.submit(submitInfo)
      case ExecutionMode.YARN_PRE_JOB => YarnPreJobSubmit.submit(submitInfo)
      case ExecutionMode.LOCAL => LocalSubmit.submit(submitInfo)
      case _ => throw new UnsupportedOperationException(s"Unsupported ${submitInfo.executionMode} Submit ")
    }
  }

  def stop(flinkHome: String, executionMode: ExecutionMode, appId: String, jobStringId: String, savePoint: JavaBool, drain: JavaBool, cancelTimeout: Long): String = {
    executionMode match {
      case ExecutionMode.APPLICATION | ExecutionMode.YARN_PRE_JOB | ExecutionMode.YARN_SESSION =>
        YarnPreJobSubmit.stop(flinkHome, appId, jobStringId, savePoint, drain, cancelTimeout)
      case ExecutionMode.LOCAL => LocalSubmit.stop(flinkHome, appId, jobStringId, savePoint, drain, cancelTimeout)
      case _ => throw new UnsupportedOperationException(s"Unsupported ${executionMode} Submit ")
    }
  }

  def triggerSavepoint(flinkHome: String, executionMode: ExecutionMode, appId: String, jobStringId: String, savePoint: JavaBool, cancelTimeout: Long, savepointDir: String): String = {
    executionMode match {
      case ExecutionMode.APPLICATION | ExecutionMode.YARN_PRE_JOB | ExecutionMode.YARN_SESSION =>
        YarnPreJobSubmit.triggerSavepoint(flinkHome, appId, jobStringId, savePoint, cancelTimeout, savepointDir)
      case ExecutionMode.LOCAL => LocalSubmit.triggerSavepoint(flinkHome, appId, jobStringId, savePoint, cancelTimeout, savepointDir)
      case _ => throw new UnsupportedOperationException(s"Unsupported ${executionMode} triggerSavepoint ")
    }
  }

}
