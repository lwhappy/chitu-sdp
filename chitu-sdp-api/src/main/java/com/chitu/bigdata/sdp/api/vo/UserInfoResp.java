package com.chitu.bigdata.sdp.api.vo;

import lombok.Data;

/**
 * @author chenyun
 * @create 2021-10-12 11:01
 */
@Data
public class UserInfoResp {
    private Integer code;
    private String msg;
    private UserDetail data;
}
