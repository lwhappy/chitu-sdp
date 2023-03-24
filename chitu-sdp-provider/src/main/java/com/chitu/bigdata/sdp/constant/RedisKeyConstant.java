package com.chitu.bigdata.sdp.constant;

import lombok.Getter;

@Getter
public enum RedisKeyConstant {

    MONITOR_ALERT("monitor:alert:", "监控告警记录的key"),
    MONITOR_LASTCK("monitor:lastck:", "监控最近一次ck值"),
    MONITOR_LAST_DELAY("monitor:lastdelay", "监控最近一次topic延迟值"),
    MONITOR_LAST_HBASE_CDC_CK_TYPE("monitor:lasthctype:", "记录最近一次校验hbase cdc告警时间"),
    MONITOR_LAST_TABLE_DELAY_TIME("monitor:cdcdelay:", "记录最近一次hbase cdc table延迟告警时间"),
    MONITOR_LAST_TABLE_DELAY_VALUE("monitor:cdclastdelay:", "记录最近一次hbase cdc table延迟值"),
    MONITOR_LAST_TABLE_NULLSIZE("monitor:tablenullsize:", "记录最近一次hbase cdc table反查为空告警时间")
    ;

    private String key;
    private String desc;

    RedisKeyConstant(String key, String desc) {
        this.key = key;
        this.desc = desc;
    }

}