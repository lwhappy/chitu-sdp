package com.chitu.bigdata.sdp.api.enums;

import java.util.HashMap;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/11/8 20:04
 */
public enum LogStatus {
    SUCCESS("成功"),
    FAILED("失败");

    private String status;

    LogStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public static HashMap<String, String> getLogStatusMap() {
        HashMap<String, String> map = new HashMap<>();
        for (LogStatus value : values()) {
            map.put(value.name(),value.status);
        }
        return map;
    }

}
