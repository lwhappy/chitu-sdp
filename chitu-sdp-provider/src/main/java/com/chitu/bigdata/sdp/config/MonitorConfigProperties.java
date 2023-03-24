package com.chitu.bigdata.sdp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * @author sutao
 * @create 2022-05-11 10:43
 */
@ConfigurationProperties(prefix = "monitor.time")
@Component
@Data
public class MonitorConfigProperties {

//    private Integer firstMonitorInterval;

}
