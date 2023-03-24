package com.chitu.bigdata.sdp.api.enums;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/25 10:50
 */
public enum ExpectStatus {
    /**
     * 希望状态:初始化
     */
    INITIALIZE("初始化"),
    /**
     * 希望状态:运行中
     */
    RUNNING("运行中"),
    /**
     * 希望状态:暂停
     */
    PAUSED("暂停"),
    /**
     * 希望状态:停止
     */
    TERMINATED("停止");

    private String status;

    ExpectStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public boolean isOffline(){
        switch (this) {
            case PAUSED:
            case TERMINATED:
                return true;
            default:
                return false;
        }
    }
    public static ExpectStatus of(String status){
        for (ExpectStatus value : values()) {
            if (value.name().equalsIgnoreCase(status)){
                return value;
            }
        }
        return null;
    }
}
