package com.chitu.bigdata.sdp.api.enums;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/19 10:50
 */
public enum FileStatus {
    /**
     * 文件状态:已创建
     */
    CREATED("已创建"),
    /**
     * 文件状态:验证通过
     */
    PASS("验证通过"),
    /**
     * 文件状态:验证失败
     */
    FAILED("验证失败");

    private String status;

    FileStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
