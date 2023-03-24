package com.chitu.bigdata.sdp.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author sutao
 * @create 2022-05-11 10:43
 */
@ConfigurationProperties(prefix = "custom")
@Component
@Data
@Slf4j
public class CustomMonitorConfigProperties {
    @Autowired
    SdpConfig sdpConfig;

    Map<String,CustomConfig> envMap;

    @Data
    public static class CustomConfig{

        private TableDelayAlertRule tableDelayAlertRule;

        private TableNullSizeAlertRule tableNullSizeAlertRule;

        private HbaseCDCCheckAlertRule hbaseCDCCheckAlertRule;

        private AutoPullJobConf autoPullJobConf;

        private Time time;

        private JobStatusSyncJobConfig jobStatusSyncJob = new JobStatusSyncJobConfig();

        private CpSpClearJobConfig cpSpClearJob = new CpSpClearJobConfig();

        private EndToEndDelayMonitorConfig endToEndDelayMonitorJob = new EndToEndDelayMonitorConfig();
    }

    @Data
    public static class TableDelayAlertRule {
        private Integer startSwitch;
        private Integer threshold;
        private Integer IntervalTime;
    }

    @Data
    public static class TableNullSizeAlertRule {
        private Integer startSwitch;
        private Integer threshold;
        private Integer diffTime;
        private Integer IntervalTime;
    }

    @Data
    public static class HbaseCDCCheckAlertRule {
        private Integer startSwitch;
        private Integer IntervalTime;
    }

    @Data
    public static class AutoPullJobConf {
        private Integer startSwitch;
        private Long executeDuration;
    }

    @Data
    public static class Time {
        private Integer firstMonitorInterval;
    }

    @Data
    public static class JobStatusSyncJobConfig{
        /**
         * 更新频率
         */
        private Integer updateFrequency = 10;

        /**
         * 历史任务同步频率
         */
        private Integer queryFrequency = 12;
    }

    @Data
    public static class CpSpClearJobConfig{
        private String param = "";
    }

    @Data
    public static class EndToEndDelayMonitorConfig {
        /**
         * 是否开启端到端延迟监控预警
         */
        private boolean mSwitch  = false;
        /**
         * 预警阈值,单位毫秒
         */
        private Integer threshold;

        /**
         *发告警间隔，单位分钟
         */
        private Integer intervalTime = 10;
    }


    @Data
    public static class CustomConfigAndEnv{
        private String env;
        private CustomConfig customConfig;
    }

    public CustomConfigAndEnv getCustomConfigAndEnv(){
        String env = sdpConfig.getEnvFromEnvHolder(log);
        CustomConfig customConfig = envMap.get(env);

        CustomConfigAndEnv customConfigAndEnv = new CustomConfigAndEnv();
        customConfigAndEnv.setEnv(env);
        customConfigAndEnv.setCustomConfig(customConfig);
        return customConfigAndEnv;
    }

}
