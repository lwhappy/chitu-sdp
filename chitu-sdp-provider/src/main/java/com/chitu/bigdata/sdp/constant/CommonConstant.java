package com.chitu.bigdata.sdp.constant;

import java.math.BigDecimal;

/**
 * @author zouchangzhen
 * @date 2022/4/13
 */
public class CommonConstant {


    public final static Integer ZERO_FOR_INT = 0;

    public final static Long ZERO_FOR_LONG = 0L;
    public final static Integer ONE_FOR_INT = 1;

    public final static BigDecimal _1024 = new BigDecimal("1024");

    public final static Long ONE_DAY_MS = 24 * 60 * 60 * 1000L;

    public final static Integer THREE = 3;

    public final static Integer MINUS_ONE = -1;

    public final static String FLINK_CLIENT = "flink";

    public final static String SPRING_CLIENT = "spring";

    public final static String DATAHUB_MECHANISM = "DATAHUB";

    public final static String ANONYMOUS_CLIENT= "ANONYMOUS";

    public final static String SYNC_JOB_INSTANCE = "syncJobInstance_";

    public final static String GROUP_ID_FORMAT = "sdp-%s-%s";


}
