package com.chitu.bigdata.sdp.flink.submit.constant;

import java.util.regex.Pattern;

/**
 * @author sutao
 * @create 2021-10-22 17:38
 */
public class FlinkConstant {


    public static final String SQL_DIST_JAR = "bigdata-sdp-flink-1.15-1.0.0-SNAPSHOT.jar";

    public static final String FLINK_HOME = "/opt/apache/flink-1.15.2";

    public static final Pattern FLINK_VERSION_PATTERN = Pattern.compile("^Version: (.*), Commit ID: (.*)$");

    public static final String MAIN_ARGS_PREFIX = "main.param.";



}
