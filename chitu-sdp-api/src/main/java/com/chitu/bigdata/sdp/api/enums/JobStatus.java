package com.chitu.bigdata.sdp.api.enums;

/**
 * @author: 陈赟
 * @Date: 2021/10/19/10:52
 */
public enum JobStatus {
    INITIALIZE("初始化"),
    RUNNING("运行中"),
    FINISHED("成功"),
    SFAILED("启动失败"),
    RFAILED("恢复失败"),
    PAUSED("暂停"),
    TERMINATED("停止");

    private String status;

    JobStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    public static JobStatus fromStatus(String jobStatus){
        for(JobStatus js : values()){
            if (js.name().equalsIgnoreCase(jobStatus)){
                return js;
            }
        }
        return null;
    }
}
