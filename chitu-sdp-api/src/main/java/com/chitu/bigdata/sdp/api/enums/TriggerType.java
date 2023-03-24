package com.chitu.bigdata.sdp.api.enums;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/25 19:34
 */
public enum TriggerType {
    SDP("SDP"),
    YARN("YARN"),
    FLINK("FLINK");

    private String type;

    TriggerType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
