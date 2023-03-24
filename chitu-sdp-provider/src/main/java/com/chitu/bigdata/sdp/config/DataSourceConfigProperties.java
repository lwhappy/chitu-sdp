package com.chitu.bigdata.sdp.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Data
@Component
@ConfigurationProperties(prefix = "datasourcecfg")
@Slf4j
public class DataSourceConfigProperties {
    @Autowired
    SdpConfig sdpConfig;

    private Map<String,HiveConfig> envMap;


    @Data
    public static class HiveConfig{
        private List<HiveClusterInfo> hive;
        private String defaultHiveLineageAddress;
    }

    @Data
    public static class HiveClusterInfo {
        private String hiveCluster;
        private String dataSourceUrl;
        private String hadoopConfDir;
        private String address;
    }

    @Data
    public static class HiveConfigAndEnv{
        private String env;
        private HiveConfig hiveConfig;
    }

    public HiveConfigAndEnv getHiveConfigAndEnv(){
        String env = sdpConfig.getEnvFromEnvHolder(log);
        HiveConfig hiveConfig = envMap.get(env);

        HiveConfigAndEnv hiveConfigAndEnv = new HiveConfigAndEnv();
        hiveConfigAndEnv.setEnv(env);
        hiveConfigAndEnv.setHiveConfig(hiveConfig);
        return hiveConfigAndEnv;
    }
}
