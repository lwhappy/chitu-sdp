package com.chitu.bigdata.sdp.flink.submit.service

import com.chitu.bigdata.sdp.flink.common.conf.ConfigConst._
import com.chitu.bigdata.sdp.flink.common.enums.{DevelopmentMode, ExecutionMode, ResolveOrder}
import com.chitu.bigdata.sdp.flink.common.util.{DeflaterUtils, HdfsUtils, PropertiesUtils}
import com.chitu.bigdata.sdp.flink.submit.service.`trait`.WorkspaceEnv
import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.flink.client.cli.CliFrontend
import org.apache.flink.client.cli.CliFrontend.loadCustomCommandLines
import org.apache.flink.client.deployment.application.ApplicationConfiguration
import org.apache.flink.configuration.{Configuration, GlobalConfiguration}

import java.io.File
import java.util.{Map => JavaMap}
import scala.collection.JavaConversions._

case class SubmitRequest(flinkHome: String, //flink安装目录，如/opt/apache/flink
                         flinkVersion: String, //flink版本，如1.13.2
                         flinkYaml: String, //flink默认配置
                         flinkUserJar: String, //提交yarn的用户jar的hdfs路径
                         mainClass: String, //用户jar 启动main全类名
                         mainArgsMap: JavaMap[String, Any], //dstream main参数
                         developmentMode: DevelopmentMode, //开发job模式，参考枚举DevelopmentMode
                         executionMode: ExecutionMode, //执行模式，参考枚举ExecutionMode
                         resolveOrder: ResolveOrder, //类加载顺序
                         appName: String, //job名称
                         appConf: String, //模板配置
                         applicationType: String, //yarn appType,参考枚举ApplicationType
                         savePoint: String, //保存点路径
                         flameGraph: JavaMap[String, java.io.Serializable], //是否开启火焰图
                         property: JavaMap[String, Any], //页面多个参数配置项
                         udfPath: String, //用户udf函数jar路径
                         libVersionConfigs: String
                        ) {

  lazy val appProperties: Map[String, String] = getParameterMap(KEY_FLINK_DEPLOYMENT_PROPERTY_PREFIX)

  lazy val appOption: Map[String, String] = getParameterMap(KEY_FLINK_DEPLOYMENT_OPTION_PREFIX)

  lazy val appMain: String = appProperties(ApplicationConfiguration.APPLICATION_MAIN_CLASS.key())

  lazy val effectiveAppName: String = if (this.appName == null) appProperties(KEY_FLINK_APP_NAME) else this.appName

  lazy val flinkSQL: String = property.remove(KEY_FLINK_SQL()).toString

  lazy val jobID: String = property.remove(KEY_JOB_ID).toString

  private[this] def getParameterMap(prefix: String = ""): Map[String, String] = {
    if (this.appConf == null) Map.empty[String, String] else {
      val map = this.appConf match {
        case x if x.trim.startsWith("yaml://") =>
          PropertiesUtils.fromYamlText(DeflaterUtils.unzipString(x.trim.drop(7)))
        case x if x.trim.startsWith("prop://") =>
          PropertiesUtils.fromPropertiesText(DeflaterUtils.unzipString(x.trim.drop(7)))
        case x if x.trim.startsWith("hdfs://") =>
          /*
           * 如果配置文件为hdfs方式,则需要用户将hdfs相关配置文件copy到resources下...
           */
          val text = HdfsUtils.read(this.appConf)
          val extension = this.appConf.split("\\.").last.toLowerCase
          extension match {
            case "properties" => PropertiesUtils.fromPropertiesText(text)
            case "yml" | "yaml" => PropertiesUtils.fromYamlText(text)
            case _ => throw new IllegalArgumentException("[SDP] Usage:flink.conf file error,must be properties or yml")
          }
        case x if x.trim.startsWith("json://") =>
          val json = x.trim.drop(7)
          new ObjectMapper().readValue[JavaMap[String, String]](json, classOf[JavaMap[String, String]]).toMap.filter(_._2 != null)
        case _ => throw new IllegalArgumentException("[SDP] appConf format error.")
      }
      if (this.appConf.trim.startsWith("json://")) map else {
        prefix match {
          case "" | null => map
          case other => map
            .filter(_._1.startsWith(other)).filter(_._2.nonEmpty)
            .map(x => x._1.drop(other.length) -> x._2)
        }
      }
    }
  }

  private[submit] lazy val workspaceEnv = {
    /**
     * 必须保持本机flink和hdfs里的flink版本和配置都完全一致.
     */
    val flinkName = new File(flinkHome).getName
    val flinkHdfsHome = s"${HdfsUtils.getDefaultFS}$APP_FLINK/$flinkName"
    WorkspaceEnv(
      flinkName,
      flinkHome,
      flinkLib = s"$flinkHdfsHome/lib",
      flinkDistJar = new File(s"$flinkHome/lib").list().filter(_.matches("flink-dist.*\\.jar")) match {
        case Array() => throw new IllegalArgumentException(s"[SDP] can no found flink-dist jar in $flinkHome/lib")
        case array if array.length == 1 => s"$flinkHdfsHome/lib/${array.head}"
        case more => throw new IllegalArgumentException(s"[SDP] found multiple flink-dist jar in $flinkHome/lib,[${more.mkString(",")}]")
      },
      appJars = s"${HdfsUtils.getDefaultFS}$APP_JARS",
      appPlugins = s"${HdfsUtils.getDefaultFS}$APP_PLUGINS"
      //      appUdfs = s"${HdfsUtils.getDefaultFS}$APP_UDFS"
    )
  }

  private[submit] lazy val flinkDefaultConfiguration: Configuration = {
    GlobalConfiguration.loadConfiguration(s"$flinkHome/conf")
  }

  private[submit] lazy val customCommandLines = {
    // 1. find the configuration directory
    val configurationDirectory = s"$flinkHome/conf"
    // 2. load the custom command lines
    val customCommandLines = loadCustomCommandLines(flinkDefaultConfiguration, configurationDirectory)
    new CliFrontend(flinkDefaultConfiguration, customCommandLines)
    customCommandLines
  }

}
