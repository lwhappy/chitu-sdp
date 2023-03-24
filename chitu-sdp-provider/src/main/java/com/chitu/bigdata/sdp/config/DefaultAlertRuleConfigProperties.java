package com.chitu.bigdata.sdp.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * @author sutao
 * @create 2021-11-11 17:13
 */
@Component
@ConfigurationProperties(prefix = "alert.default")
@Data
public class DefaultAlertRuleConfigProperties {

    private List<Map<String, String>> rules;

}
