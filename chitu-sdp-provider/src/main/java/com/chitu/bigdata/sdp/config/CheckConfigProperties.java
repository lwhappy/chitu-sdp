package com.chitu.bigdata.sdp.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author zouchangzhen
 * @date 2022/10/12
 */
@Component
@ConfigurationProperties(prefix = "check")
@Data
@Slf4j
public class CheckConfigProperties {
    @Autowired
    SdpConfig sdpConfig;

    Map<String, CheckConfig> envMap;

    @Data
    public static class CheckConfig {
        KafkaConfig kafka = new KafkaConfig();
        HiveConfig hive = new HiveConfig();
        ResourceConfig resource = new ResourceConfig();
        MetatablerelationConfig metatablerelation = new MetatablerelationConfig();
        StartjobConfig startjob = new StartjobConfig();
    }

    @Data
    public static class KafkaConfig{
        private Integer fetchColNumOfTimes = 15;
    }

    @Data
    public static class HiveConfig{
        private boolean mSwitch = true;
        private Integer executionCheckpointingInterval = 300000;
    }


    @Data
    public static class ResourceConfig{
        private boolean mSwitch = true;
        private String notice = "管理员";
    }


    @Data
    public static class MetatablerelationConfig{
        private boolean mSwitch = true;
    }

    @Data
    public static class StartjobConfig{
        private boolean mSwitch = true;
    }



    @Data
    public static class CheckConfigAndEnv{
        private String env;
        private CheckConfig checkConfig;
    }

    public CheckConfigAndEnv getCheckConfigAndEnv(){
        String env = sdpConfig.getEnvFromEnvHolder(log);
        CheckConfig checkConfig = envMap.get(env);

        CheckConfigAndEnv checkConfigAndEnv = new CheckConfigAndEnv();
        checkConfigAndEnv.setEnv(env);
        checkConfigAndEnv.setCheckConfig(checkConfig);
        return checkConfigAndEnv;
    }
}
