package com.chitu.bigdata.sdp.flink.submit.service.impl

import com.alibaba.fastjson.JSON
import com.alibaba.fastjson.serializer.SerializerFeature
import com.chitu.bigdata.sdp.flink.common.conf.ConfigConst._
import com.chitu.bigdata.sdp.flink.common.enums.DevelopmentMode
import com.chitu.bigdata.sdp.flink.common.util.{DeflaterUtils, HdfsUtils}
import com.chitu.bigdata.sdp.flink.submit.service
import com.chitu.bigdata.sdp.flink.submit.service.`trait`.YarnSubmitTrait
import com.chitu.bigdata.sdp.flink.submit.service.{SubmitRequest, SubmitResponse}
import org.apache.commons.cli.CommandLine
import org.apache.commons.lang3.StringUtils
import org.apache.flink.client.cli.{CustomCommandLine, ExecutionConfigAccessor, ProgramOptions}
import org.apache.flink.client.deployment.DefaultClusterClientServiceLoader
import org.apache.flink.client.deployment.application.ApplicationConfiguration
import org.apache.flink.client.program.PackagedProgramUtils
import org.apache.flink.configuration._
import org.apache.flink.runtime.jobgraph.SavepointConfigOptions
import org.apache.flink.runtime.security.{SecurityConfiguration, SecurityUtils}
import org.apache.flink.runtime.util.HadoopUtils
import org.apache.flink.yarn.configuration.{YarnConfigOptions, YarnDeploymentTarget}
import org.apache.hadoop.security.UserGroupInformation
import org.apache.hadoop.yarn.api.records.ApplicationId

import java.lang.{Boolean => JavaBool}
import java.util.concurrent.Callable
import java.util.{Collections, List => JavaList}
import scala.collection.JavaConverters._
import scala.collection.mutable.{ArrayBuffer, ListBuffer}


/**
 * yarn application mode submit
 */
object YarnApplicationSubmit extends YarnSubmitTrait {

  override def doSubmit(submitRequest: SubmitRequest): SubmitResponse = {

    //获取有效的命令行参数，来自：页面多个参数配置，和动态参数dynamicOption
    //-t yarn-application -Dtaskmanager.memory.process.size=1024mb -Dparallelism.default=1 -Dtaskmanager.numberOfTaskSlots=1 -Djobmanager.memory.process.size=1024mb -Dclassloader.resolve-order=parent-first
    val commandLine = getEffectiveCommandLine(
      submitRequest,
      "-t" -> YarnDeploymentTarget.APPLICATION.getName
    )
    val activeCommandLine = validateAndGetActiveCommandLine(submitRequest.customCommandLines, commandLine)

    val uri = PackagedProgramUtils.resolveURI(submitRequest.flinkUserJar)

    //根据用户配置的命令行参数和flink默认参数来组装提交yarn的flink参数
    val flinkConfig = getEffectiveConfiguration(submitRequest, activeCommandLine, commandLine, Collections.singletonList(uri.toString))

    SecurityUtils.install(new SecurityConfiguration(flinkConfig))
    SecurityUtils.getInstalledContext.runSecured(new Callable[SubmitResponse] {
      override def call(): SubmitResponse = {
        val clusterClientServiceLoader = new DefaultClusterClientServiceLoader
        //获取yarn集群客户端工厂
        val clientFactory = clusterClientServiceLoader.getClusterClientFactory[ApplicationId](flinkConfig)
        val clusterDescriptor = clientFactory.createClusterDescriptor(flinkConfig)
        try {
          val clusterSpecification = clientFactory.getClusterSpecification(flinkConfig)
          logInfo(
            s"""
               |------------------------<<specification>>-------------------------
               |$clusterSpecification
               |------------------------------------------------------------------
               |""".stripMargin)

          //设置用户jar的参数和主类
          val applicationConfiguration = ApplicationConfiguration.fromConfiguration(flinkConfig)
          var applicationId: ApplicationId = null
          //提交任务到集群
          val clusterClient = clusterDescriptor.deployApplicationCluster(clusterSpecification, applicationConfiguration).getClusterClient
          //获取applicationId
          applicationId = clusterClient.getClusterId

          logInfo(
            s"""
               ||-------------------------<<applicationId>>------------------------|
               ||Flink Job Started: applicationId: $applicationId|
               ||__________________________________________________________________|
               |""".stripMargin)

          //返回appId和提交yarn的配置参数
          service.SubmitResponse(applicationId.toString, flinkConfig, null)
        } finally if (clusterDescriptor != null) {
          clusterDescriptor.close()
        }
      }
    })
  }

  private def getEffectiveConfiguration[T](
                                            submitRequest: SubmitRequest,
                                            activeCustomCommandLine: CustomCommandLine,
                                            commandLine: CommandLine,
                                            jobJars: JavaList[String]) = {

    // 这里获取的是部署服务器上的flink conf下的配置，然后使用页面提交参数覆盖更新
    // 页面定义参数优先级 > flink-conf.yaml中配置优先级
    val effectiveConfiguration = super.applyConfiguration(submitRequest, activeCustomCommandLine, commandLine)

    //将命令行参数如-s，转换为执行参数，添加到flink effectiveConfiguration
    val programOptions = ProgramOptions.create(commandLine)
    val executionParameters = ExecutionConfigAccessor.fromProgramOptions(programOptions, jobJars)
    executionParameters.applyToConfiguration(effectiveConfiguration)

    // programArgs 是最终提交yarn集群的jar参数
    // providedLibs 是提交yarn的依赖包
    val (providedLibs, programArgs) = {
      val programArgs = new ArrayBuffer[String]()
      val providedLibs = ListBuffer(
        submitRequest.workspaceEnv.flinkLib,
        submitRequest.workspaceEnv.appJars
        //submitRequest.workspaceEnv.appPlugins
      )
      submitRequest.developmentMode match {
        case DevelopmentMode.FLINKSQL =>
          //Try(submitRequest.args.split("\\s+")).getOrElse(Array()).foreach(x => if (x.nonEmpty) programArgs += x)
          //设置flink默认配置文件 --flink.conf
          programArgs += PARAM_KEY_FLINK_CONF
          programArgs += DeflaterUtils.zipString(submitRequest.flinkYaml)
          //设置job名称 --app.name
          programArgs += PARAM_KEY_APP_NAME
          programArgs += submitRequest.effectiveAppName
          //设置并行度 --parallelism.default
          val parallelism = getParallelism(submitRequest)
          if (parallelism != null) {
            programArgs += PARAM_KEY_FLINK_PARALLELISM
            programArgs += s"$parallelism"
          }
          //--sql
          programArgs += PARAM_KEY_FLINK_SQL
          programArgs += submitRequest.flinkSQL
          //页面flink参数模板appConf，--conf
          if (submitRequest.appConf != null) {
            programArgs += PARAM_KEY_APP_CONF
            programArgs += submitRequest.appConf
          }
          if (!StringUtils.isBlank(submitRequest.udfPath)) {
            val jobLib = s"${HdfsUtils.getDefaultFS}$APP_WORKSPACE${submitRequest.udfPath}"
            providedLibs += jobLib
          }
          //这里将yaml配置封装传参
          val optionsStr = DeflaterUtils.zipString(JSON.toJSONString(submitRequest.property, SerializerFeature.QuoteFieldNames))
          programArgs += PARAM_KEY_YAML_CONF
          programArgs += optionsStr
        case _ =>
          submitRequest.mainArgsMap.asScala.foreach(item => {
            programArgs += "--" + item._1
            programArgs += item._2.toString
          })
        //programArgs += PARAM_KEY_APP_CONF
        //programArgs += submitRequest.appConf
      }
      providedLibs -> programArgs
    }

    if (submitRequest.libVersionConfigs != null) {
      for (item <- submitRequest.libVersionConfigs.split(",")) {
        providedLibs += item
      }
    }


    //获取当前用户
    val currentUser = UserGroupInformation.getCurrentUser
    logDebug(s"UserGroupInformation currentUser: $currentUser")
    if (HadoopUtils.isKerberosSecurityEnabled(currentUser)) {
      logDebug(s"kerberos Security is Enabled...")
      val useTicketCache = getOptionFromDefaultFlinkConfig[JavaBool](submitRequest.flinkHome, SecurityOptions.KERBEROS_LOGIN_USETICKETCACHE)
      if (!HadoopUtils.areKerberosCredentialsValid(currentUser, useTicketCache)) {
        throw new RuntimeException(s"Hadoop security with Kerberos is enabled but the login user ${currentUser} does not have Kerberos credentials or delegation tokens!")
      }
    }

    //yarn.provided.lib.dirs
    effectiveConfiguration.set(YarnConfigOptions.PROVIDED_LIB_DIRS, providedLibs.asJava)
    //flinkDistJar
    effectiveConfiguration.set(YarnConfigOptions.FLINK_DIST_JAR, submitRequest.workspaceEnv.flinkDistJar)
    //pipeline.jars
    effectiveConfiguration.set(PipelineOptions.JARS, Collections.singletonList(submitRequest.flinkUserJar))
    //execution.target
    effectiveConfiguration.set(DeploymentOptions.TARGET, YarnDeploymentTarget.APPLICATION.getName)
    //yarn application name
    effectiveConfiguration.set(YarnConfigOptions.APPLICATION_NAME, submitRequest.effectiveAppName)
    //yarn application Type
    effectiveConfiguration.set(YarnConfigOptions.APPLICATION_TYPE, submitRequest.applicationType)

    /*    if (StringUtils.isNotEmpty(submitRequest.mainClass)) {
          effectiveConfiguration.set(ApplicationConfiguration.APPLICATION_MAIN_CLASS, submitRequest.mainClass)
        }*/

    //arguments...
    //程序参数，优先级最高，这将最终为用户main函数的参数[]String
    effectiveConfiguration.set(ApplicationConfiguration.APPLICATION_ARGS, programArgs.toList.asJava)
    if (submitRequest.property.containsKey(SavepointConfigOptions.SAVEPOINT_IGNORE_UNCLAIMED_STATE.key())) {
      effectiveConfiguration.setBoolean(SavepointConfigOptions.SAVEPOINT_IGNORE_UNCLAIMED_STATE, submitRequest.property.get(SavepointConfigOptions.SAVEPOINT_IGNORE_UNCLAIMED_STATE.key()).toString.toBoolean)
    }

    logInfo(
      s"""
         |------------------------------------------------------------------
         |Effective executor configuration: $effectiveConfiguration
         |------------------------------------------------------------------
         |""".stripMargin)

    effectiveConfiguration
  }

}
