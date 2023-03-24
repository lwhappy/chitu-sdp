package com.chitu.bigdata.sdp.api.vo;

import lombok.Data;

/**
 * @author zouchangzhen
 * @date 2022/9/13
 */
@Data
public class AppCenterResp {
    private Integer code;
    private String msg;
    private boolean success;
    private CreateAppInfo data;

    @Data
    public static class CreateAppInfo{
        private String appName;
        private String function;
        private String appKey;
        private String appSecret;
    }
}
