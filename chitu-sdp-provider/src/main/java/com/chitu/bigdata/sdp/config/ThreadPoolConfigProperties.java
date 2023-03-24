package com.chitu.bigdata.sdp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@ConfigurationProperties(prefix = "thread")
@Component
@Data
public class ThreadPoolConfigProperties {

    private MonitorPoolConfig monitor;

    @Data
    public static class MonitorPoolConfig{
        private Integer coreSize;
        private Integer maxSize;
        private Integer keepAliveTime;
        private Integer queueCapacity;
        private String threadNamePrefix;
    }



}



