package com.chitu.bigdata.sdp.api.enums;
/**
 * @author 587694
 * @description: TODO 作业运行内部步骤拆解
 * @date 2021/11/16 15:56
 */
public enum JobRunStep {

    VALIDATE("校验"),

    COMMIT("提交"),

    PAUSE("暂停"),

    RECOVER("恢复"),

    RETRY("重试"),

    EXAMINE("状态检测"),

    RUNNING("作业运行"),

    BUILD_PUSH_IMAGE("构建镜像"),

    STOP("停止");

    private String type;

    JobRunStep(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static String getStepType(String value1){
        String type = null;
        for (JobRunStep value : values()){
            if (value.name().equals(value1)){
                type = value.type;
            }
        }
        return type;
    }

    public static JobRunStep getStepValue(String value1){
        JobRunStep type = null;
        for (JobRunStep value : values()){
            if (value.name().equals(value1)){
                type = value;
            }
        }
        return type;
    }
}
