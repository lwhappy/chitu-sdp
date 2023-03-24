package com.chitu.bigdata.sdp.api.flink;

import lombok.Data;

@Data
public class Application {

    /**
     * 本地库job配置主键id
     */
    private Long id;

    /**
     * job类型：flink sql / 自定义dstream编码
     */
    private Integer jobType;

    /**
     * dstream用户jar路径
     */
    private String flinkUserJar;

    /**
     * dstream用户jar main启动类全类名
     */
    private String mainClass;

    /**
     * job名称
     */
    private String jobName;

    /**
     * flink sql 需要压缩
     */
    private String flinkSql;

    /**
     * 执行模式，参考枚举 ExecutionMode
     */
    private Integer executionMode;

    /**
     * yarn显示的 appType,参考枚举 ApplicationType
     */
    private Integer appType;

    private String args;
    /**
     * 是否开启火焰图
     */
    private Boolean flameGraph;

    /**
     * Flink执行时类加载顺序
     */
    private Integer resolveOrder;

    /**
     * 保存点路径url：hdfs://bigbigworld/sdp/savepoints/savepoint-cdee6b-6fc4c0425297
     */
    private String savePoint;
    /**
     * 是否开启保存点
     */
    private Boolean savePointed = false;
    private Boolean drain = false;

    private String options;
    private Boolean allowNonRestored = false;
    private String dynamicOptions;

    /**
     * yarn appId
     */
    private String appId;
    /**
     * flink jobId
     */
    private String jobId;

    private String udfPath;

    private String containerImage;
    private String namespace;
    private String hadoopConfigMapName;
    private String jobManagerServiceAccount;

}
