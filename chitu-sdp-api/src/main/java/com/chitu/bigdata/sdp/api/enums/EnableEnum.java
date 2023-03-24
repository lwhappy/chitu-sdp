package com.chitu.bigdata.sdp.api.enums;

/**
 * 是否开启
 * @author zouchangzhen
 * @date 2022/9/8
 */
public enum EnableEnum {
    /**
     * 开启
     */
    ENABLE(1),
    /**
     * 未开启
     */
    UNABLE(0);

    private Integer code;

    EnableEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setType(Integer code) {
        this.code = code;
    }
}
