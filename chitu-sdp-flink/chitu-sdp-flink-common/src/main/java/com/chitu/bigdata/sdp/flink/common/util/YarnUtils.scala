package com.chitu.bigdata.sdp.flink.common.util

import org.apache.hadoop.yarn.api.records.YarnApplicationState._
import org.apache.hadoop.yarn.api.records._
import org.apache.hadoop.yarn.util.ConverterUtils

import java.util
import java.util.List
import scala.collection.JavaConversions._
import scala.collection.mutable.ArrayBuffer

object YarnUtils {


  /**
   *
   * @param appName
   * @return
   */
  def getAppId(appName: String): List[ApplicationId] = {
    val appStates = util.EnumSet.of(RUNNING, ACCEPTED, SUBMITTED)
    val appIds = try {
      HadoopUtils.yarnClient.getApplications(appStates).filter(_.getName == appName).map(_.getApplicationId)
    } catch {
      case e: Exception => e.printStackTrace()
        ArrayBuffer.empty[ApplicationId]
    }
    appIds.toList
  }

  /**
   * 查询 state
   *
   * @param appId
   * @return
   */
  def getState(appId: String): YarnApplicationState = {
    val applicationId = ConverterUtils.toApplicationId(appId)
    val state = try {
      val applicationReport = HadoopUtils.yarnClient.getApplicationReport(applicationId)
      applicationReport.getYarnApplicationState
    } catch {
      case e: Exception => e.printStackTrace()
        null
    }
    state
  }

  /**
   * 判断任务名为appName的任务，是否在yarn中运行，状态为RUNNING
   *
   * @return boolean
   * @param appName
   * @return
   */
  def isContains(appName: String): Boolean = {
    val runningApps = HadoopUtils.yarnClient.getApplications(util.EnumSet.of(RUNNING))
    if (runningApps != null) {
      runningApps.exists(_.getName == appName)
    } else {
      false
    }
  }

}
