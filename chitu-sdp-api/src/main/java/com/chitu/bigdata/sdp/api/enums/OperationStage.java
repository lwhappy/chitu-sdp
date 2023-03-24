package com.chitu.bigdata.sdp.api.enums;
/**
 * @author 587694
 * @description: TODO 作业运行内部步骤拆解
 * @date 2021/11/16 15:56
 */
public enum OperationStage {

    SUCCESS("成功"),

    FAILED("失败");

    private String type;

    OperationStage(String type) {
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
        for (OperationStage value : values()){
            if (value.name().equals(value1)){
                type = value.type;
            }
        }
        return type;
    }
}
