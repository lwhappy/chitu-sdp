package com.chitu.bigdata.sdp.api.enums;

/**
 * @author zouchangzhen
 * @date 2022/9/19
 */
public enum ExplainInfoEnum {
    /**
     * topic OA订阅
     */
    DATAHUB_SUBSCRIBE_OA(1);

    private Integer code;

    ExplainInfoEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setType(Integer code) {
        this.code = code;
    }
}
