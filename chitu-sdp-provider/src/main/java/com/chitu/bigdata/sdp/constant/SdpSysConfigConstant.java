package com.chitu.bigdata.sdp.constant;

import com.chitu.bigdata.sdp.api.enums.NotifiUserType;

/**
 * @author zouchangzhen
 * @date 2022/4/22
 */
public class SdpSysConfigConstant {
    /**
     * 系统白名单用户
     */
    public final static String WHITE_LIST_KEY = "WHITE_LIST";
    /**
     * 作业运维开关
     */
    public final static String OPERATION_MAINTENANCE_KEY = "OPERATION_MAINTENANCE";
    /**
     * 常规通知用户
     */
    public final static String COMMON_NOTICE_USERS = NotifiUserType.COMMON_NOTICE_USERS.name();
    /**
     * 告警通知用户
     */
    public final static String ALARM_NOTICE_USERS = NotifiUserType.ALARM_NOTICE_USERS.name();
    /**
     * 数据库写入控制
     */
    public final static String DATASOURCE_WRITE_CONTRLS = "DATASOURCE_WRITE_CONTRLS";

    //集群资源校验开关
    public final static String RESOURCE_VALIDATE = "RESOURCE_VALIDATE";


    /**
     * 告警全局开关
     */
    public final static String ALERT_GLOBAL_SWITCH = "ALERT_GLOBAL_SWITCH";

    /**
     * 是否需要二级审批
     */
    public final static String NEED_TWO_APPROVE = "NEED_TWO_APPROVE";


}
