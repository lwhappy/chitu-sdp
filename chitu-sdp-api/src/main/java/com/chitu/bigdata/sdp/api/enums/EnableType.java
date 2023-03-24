package com.chitu.bigdata.sdp.api.enums;

/**
 * @author 587694
 * @description: TODO
 * @date 2021/10/25 19:34
 */
public enum EnableType {
    /**
     * 是否可用标识
     */
    ENABLE(1L),
    UNABLE(0L);

    private Long code;

    EnableType(Long code) {
        this.code = code;
    }

    public Long getCode() {
        return code;
    }

    public void setType(Long code) {
        this.code = code;
    }
}
