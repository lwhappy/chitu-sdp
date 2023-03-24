package com.chitu.bigdata.sdp.flink.submit.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "flink")
public class FlinkConfig {

    private String sqlJar;
    private String flinkHome;
    private Long cancelTimeout;
    private Long triggerSavepointTimeout;

}
