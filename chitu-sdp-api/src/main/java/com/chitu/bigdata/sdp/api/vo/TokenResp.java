package com.chitu.bigdata.sdp.api.vo;

import lombok.Data;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/12 16:36
 */
@Data
public class TokenResp {
    private String token;
    private Long expire;
    private String ticket;
    private UserDetail userInfo;
    private String type;
    private String environment;
    private String employeeNumber;
    private Long userId;
    /**
     * cas返回的token
     */
    private String erpToken;
}
