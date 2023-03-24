package com.chitu.bigdata.sdp.api.enums;
/**
 * @author chenyun
 * @description: TODO
 * @date 2021/12/10 15:19
 */
public enum CertifyType {
    /**
     * kafka的认证模式
     */
    SASL("sasl");
    private String type;
    CertifyType (String type){this.type = type;}
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

}
