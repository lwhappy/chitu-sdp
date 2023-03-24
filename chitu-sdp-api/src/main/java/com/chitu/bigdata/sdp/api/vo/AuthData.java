package com.chitu.bigdata.sdp.api.vo;

import lombok.Data;

/**
 * @author chenyun
 * @create 2021-10-12 11:01
 */
@Data
public class AuthData {
    private String access_token;
    private Long expires_in;
    private String business_id;
    private String accessToken;
    private Long expireAt;
    private CasToken casToken;
    private String userId;
    private String userNumber;
    //{"code":0,"data":{"accessToken":"94584cc7efb7508edd1b74d39b178c44","expireAt":1656424363614,"userId":"152336823463296577"},"m
    //sg":"OK","success":true}
    @Data
    public class AccessToken{
       private String accessToken;
       private Long expireAt;
       private String userId;
    }
    @Data
    public class CasToken{
        private String token;
        private Long expireIn;
    }
}
