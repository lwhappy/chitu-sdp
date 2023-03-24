package com.chitu.bigdata.sdp.flink.submit.service.`trait`

import com.chitu.bigdata.sdp.flink.common.conf.ConfigConst.{APP_SAVEPOINTS, KEY_FLINK_PARALLELISM}
import com.chitu.bigdata.sdp.flink.common.enums.DevelopmentMode
import com.chitu.bigdata.sdp.flink.common.util.ExceptionUtils
import com.chitu.bigdata.sdp.flink.submit.service.SubmitRequest
import org.apache.commons.cli.CommandLine
import org.apache.flink.client.cli.{ClientOptions, CustomCommandLine}
import org.apache.flink.client.deployment.ClusterSpecification
import org.apache.flink.client.deployment.application.ApplicationConfiguration
import org.apache.flink.client.program.ClusterClientProvider
import org.apache.flink.configuration.{CheckpointingOptions, ConfigOptions, Configuration, CoreOptions}
import org.apache.flink.core.execution.SavepointFormatType
import org.apache.flink.runtime.jobgraph.JobGraph
import org.apache.flink.util.FlinkException
import org.apache.flink.yarn.configuration.YarnConfigOptions
import org.apache.flink.yarn.{YarnClusterClientFactory, YarnClusterDescriptor}
import org.apache.hadoop.yarn.api.records.ApplicationId

import java.lang.reflect.Method
import java.lang.{Boolean => JavaBool}
import java.util.concurrent.TimeUnit
import scala.collection.JavaConversions._
import scala.util.Try

/**
 * yarn application mode submit
 */
trait YarnSubmitTrait extends FlinkSubmitTrait {

  override def doStop(flinkHome: String, appId: String, jobStringId: String, savePoint: JavaBool, drain: JavaBool, cancelTimeout: Long): String = {

    val jobID = getJobID(jobStringId)

    val clusterClient = {
      val flinkConfiguration = new Configuration
      flinkConfiguration.set(YarnConfigOptions.APPLICATION_ID, appId)
      val clusterClientFactory = new YarnClusterClientFactory
      val applicationId = clusterClientFactory.getClusterId(flinkConfiguration)
      if (applicationId == null) {
        throw new FlinkException("[SDP] getClusterClient error. No cluster id was specified. Please specify a cluster to which you would like to connect.")
      }
      val clusterDescriptor = clusterClientFactory.createClusterDescriptor(flinkConfiguration)
      clusterDescriptor.retrieve(applicationId).getClusterClient
    }

    val savePointDir = getOptionFromDefaultFlinkConfig(
      flinkHome,
      ConfigOptions.key(CheckpointingOptions.SAVEPOINT_DIRECTORY.key())
        .stringType()
        .defaultValue(s"hdfs://$APP_SAVEPOINTS")
    )

    val savepointPathFuture = (Try(savePoint.booleanValue()).getOrElse(false), Try(drain.booleanValue()).getOrElse(false)) match {
      case (false, false) =>
        clusterClient.cancel(jobID).get(cancelTimeout, TimeUnit.SECONDS)
        null
      case (true, false) => clusterClient.cancelWithSavepoint(jobID, savePointDir, SavepointFormatType.DEFAULT)
      case (_, _) => clusterClient.stopWithSavepoint(jobID, drain, savePointDir, SavepointFormatType.DEFAULT)
    }

    if (savepointPathFuture == null) null else try {
      val clientTimeout = getOptionFromDefaultFlinkConfig(flinkHome, ClientOptions.CLIENT_TIMEOUT)
      savepointPathFuture.get(clientTimeout.toMillis, TimeUnit.MILLISECONDS)
    } catch {
      case e: Exception =>
        val cause = ExceptionUtils.stringifyException(e)
        throw new FlinkException(s"[SDP] Triggering a savepoint for the job $jobStringId failed. $cause");
    }

  }

  override def doTriggerSavepoint(flinkHome: String, appId: String, jobStringId: String, savePoint: JavaBool, cancelTimeout: Long, savepointDirectory: String): String = {

    val jobID = getJobID(jobStringId)

    val clusterClient = {
      val flinkConfiguration = new Configuration
      flinkConfiguration.set(YarnConfigOptions.APPLICATION_ID, appId)
      val clusterClientFactory = new YarnClusterClientFactory
      val applicationId = clusterClientFactory.getClusterId(flinkConfiguration)
      if (applicationId == null) {
        throw new FlinkException("[SDP] getClusterClient error. No cluster id was specified. Please specify a cluster to which you would like to connect.")
      }
      val clusterDescriptor = clusterClientFactory.createClusterDescriptor(flinkConfiguration)
      clusterDescriptor.retrieve(applicationId).getClusterClient
    }

    /* val savePointDir = getOptionFromDefaultFlinkConfig(
       flinkHome,
       ConfigOptions.key(CheckpointingOptions.SAVEPOINT_DIRECTORY.key())
         .stringType()
         .defaultValue(s"hdfs://$APP_SAVEPOINTS")
     )*/

    logInfo("yarn savePointDir: " + savepointDirectory)

    /* val savepointPathFuture = (Try(savePoint.booleanValue()).getOrElse(false), Try(drain.booleanValue()).getOrElse(false)) match {
       case (false, false) =>
         clusterClient.cancel(jobID).get(cancelTimeout, TimeUnit.SECONDS)
         null
       case (true, false) => clusterClient.cancelWithSavepoint(jobID, savePointDir)
       case (_, _) => clusterClient.stopWithSavepoint(jobID, drain, savePointDir)
     }
    */
    //是否开启savePoint保存
    val isSavePointed = Try(savePoint.booleanValue()).getOrElse(false)
    if (!isSavePointed) {
      var cause = "The savepoint switch is not open"
      throw new FlinkException(s"[SDP] Triggering a savepoint for the job $jobStringId failed. $cause");
    }
    val savepointPathFuture =  clusterClient.triggerSavepoint(jobID,savepointDirectory, SavepointFormatType.DEFAULT)

    val isSavePointFuture = savepointPathFuture == null;
    logInfo("savePointPathFuture: "+isSavePointFuture)
    if (isSavePointFuture) null else try {
      val clientTimeout = getOptionFromDefaultFlinkConfig(flinkHome, ClientOptions.CLIENT_TIMEOUT)
      savepointPathFuture.get(cancelTimeout, TimeUnit.SECONDS)
    } catch {
      case e: Exception =>
        val cause = ExceptionUtils.stringifyException(e)
        throw new FlinkException(s"[SDP] Triggering a savepoint for the job $jobStringId failed. $cause");
    }

  }

  lazy private val deployInternalMethod: Method = {
    val paramClass = Array(
      classOf[ClusterSpecification],
      classOf[String],
      classOf[String],
      classOf[JobGraph],
      Boolean2boolean(true).getClass // get boolean class.
    )
    val deployInternal = classOf[YarnClusterDescriptor].getDeclaredMethod("deployInternal", paramClass: _*)
    deployInternal.setAccessible(true)
    deployInternal
  }

  private[submit] def getParallelism(submitRequest: SubmitRequest): Integer = {
    if (submitRequest.property.containsKey(KEY_FLINK_PARALLELISM())) {
      Integer.valueOf(submitRequest.property.get(KEY_FLINK_PARALLELISM()).toString)
    } else {
      val parallelism = submitRequest.flinkDefaultConfiguration.getInteger(CoreOptions.DEFAULT_PARALLELISM, -1)
      if (parallelism == -1) null else parallelism
    }
  }

  /**
   * 页面定义参数优先级 > flink-conf.yaml中配置优先级
   *
   * @param submitRequest
   * @param activeCustomCommandLine
   * @param commandLine
   * @return
   */
  private[submit] def applyConfiguration(submitRequest: SubmitRequest,
                                         activeCustomCommandLine: CustomCommandLine,
                                         commandLine: CommandLine): Configuration = {

    require(activeCustomCommandLine != null)
    val executorConfig = activeCustomCommandLine.toConfiguration(commandLine)
    val customConfiguration = new Configuration(executorConfig)
    //实际就是一个map
    val configuration = new Configuration()
    //flink-conf.yaml配置
    submitRequest.flinkDefaultConfiguration.keySet.foreach(x => {
      submitRequest.flinkDefaultConfiguration.getString(x, null) match {
        case v if v != null => configuration.setString(x, v)
        case _ =>
      }
    })
    //使用用户配置覆盖更新flink conf默认配置
    configuration.addAll(customConfiguration)
    //main class
    if (submitRequest.developmentMode == DevelopmentMode.CUSTOMCODE) {
      configuration.set(ApplicationConfiguration.APPLICATION_MAIN_CLASS, submitRequest.mainClass)
    }
    configuration
  }

  private[submit] def deployInternal(clusterDescriptor: YarnClusterDescriptor,
                                     clusterSpecification: ClusterSpecification,
                                     applicationName: String,
                                     yarnClusterEntrypoint: String,
                                     jobGraph: JobGraph,
                                     detached: JavaBool): ClusterClientProvider[ApplicationId] = {
    deployInternalMethod.invoke(
      clusterDescriptor,
      clusterSpecification,
      applicationName,
      yarnClusterEntrypoint,
      jobGraph,
      detached
    ).asInstanceOf[ClusterClientProvider[ApplicationId]]
  }

}
