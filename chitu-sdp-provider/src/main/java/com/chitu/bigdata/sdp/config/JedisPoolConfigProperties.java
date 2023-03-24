package com.chitu.bigdata.sdp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author chenyun
 * @description: TODO
 * @date 2022/3/23 11:12
 */
@ConfigurationProperties(prefix = "spring.redis")
@Component
@Data
public class JedisPoolConfigProperties {

    private String host;
    private Integer port;
    private String password;
    private Integer database;
    private Integer timeout;//底层默认2s，我们给3s
    private Pool pool;

    @Data
    public static class Pool{
        private Integer maxActive;//底层默认8，我们给2000
        private Integer maxIdle;//底层默认8，我们给20
    }

}