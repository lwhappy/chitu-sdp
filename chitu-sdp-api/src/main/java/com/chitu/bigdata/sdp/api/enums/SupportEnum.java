package com.chitu.bigdata.sdp.api.enums;

/**
 * 是否支持
 * @author zouchangzhen
 * @date 2022/9/8
 */
public enum SupportEnum {
    /**
     * 支持
     */
    SUPPORTED(1),
    /**
     * 不支持
     */
    NOTSUPPORT(0);

    private Integer code;

    SupportEnum(Integer code) {
        this.code = code;
    }

    public Integer getCode() {
        return code;
    }

    public void setType(Integer code) {
        this.code = code;
    }
}
