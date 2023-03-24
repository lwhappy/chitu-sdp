package com.chitu.bigdata.sdp.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author zouchangzhen
 * @date 2022/10/11
 */
@Component
@ConfigurationProperties(prefix = "prometheus")
@Data
@Slf4j
public class PrometheusConfigProperties {
    @Autowired
    SdpConfig sdpConfig;

    Map<String ,PrometheusConfig> envMap;

    @Data
    public static class PrometheusConfig{
      private String urlPrefix;
      private String baseUrl;
      private String queryUrl;
    }

    @Data
    public static class PrometheusConfigAndEnv{
        private String env;
        private PrometheusConfig prometheusConfig;
    }

    public PrometheusConfigAndEnv getPrometheusConfigAndEnv(){
        String env = sdpConfig.getEnvFromEnvHolder(log);
        PrometheusConfig prometheusConfig = envMap.get(env);

        PrometheusConfigAndEnv prometheusConfigAndEnv = new PrometheusConfigAndEnv();
        prometheusConfigAndEnv.setEnv(env);
        prometheusConfigAndEnv.setPrometheusConfig(prometheusConfig);
        return prometheusConfigAndEnv;
    }
}
