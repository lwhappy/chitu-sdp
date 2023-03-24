package com.chitu.bigdata.sdp.constant;

import java.util.regex.Pattern;

/**
 * @author sutao
 * @create 2021-10-22 17:38
 */
public class FlinkConstant {

    public static final String SQL_DIST_JAR = "bigdata-sdp-flink-sqlclient-2.0.0-SNAPSHOT.jar";

    public static final String FLINK_HOME = "/opt/apache/flink-1.14.3";

    public static final Pattern FLINK_VERSION_PATTERN = Pattern.compile("^Version: (.*), Commit ID: (.*)$");

    public static final String MAIN_ARGS_PREFIX = "main.param.";

    public static final String SAVEPOINTS_DIR = "/sdp/savepoints";

    /**
     * 高级配置 检查点间隔时间
     */
    public static final String EXECUTION_CHECKPOINTING_INTERVAL = "execution.checkpointing.interval";


    public static final String DEFAULT_CATALOG = "default_catalog";

    public static final String DEFAULT_DATABASE = "default_database";

}
