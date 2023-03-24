package com.chitu.bigdata.sdp.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author zouchangzhen
 * @date 2022/12/13
 */
@Component
@ConfigurationProperties(prefix = "business-line")
@Data
@Slf4j
public class BusinessLineConfigProperties {

    List<BusinessLine> bls;

    @Data
    public static class BusinessLine{
        private String code;
        private String name;
    }
}
