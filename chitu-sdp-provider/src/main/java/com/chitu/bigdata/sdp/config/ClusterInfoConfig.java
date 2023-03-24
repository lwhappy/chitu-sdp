

package com.chitu.bigdata.sdp.config;

import com.chitu.bigdata.sdp.api.domain.ClusterInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

/**
 * TODO 类功能描述
 *
 * @author chenyun
 * @version 1.0
 * @date 2022/7/18 11:15
 */
@Data
@Component
@ConfigurationProperties(prefix = "yarn")
@Slf4j
public class ClusterInfoConfig {

    @Autowired
    SdpConfig sdpConfig;

    private Map<String,YarnConfig> envMap;

    @Data
    public static class YarnConfig{
        private List<ClusterInfo> clusters;
        private String clusterSyncLocalDir;
        private DefaultConfig defaultConfig;

        @Data
        public static class DefaultConfig{
            private String queue;
            private Container container;

            @Data
            public static class Container{
                private double minVcores;
                private int minMem;
            }
        }
    }

    @Data
    public static class YarnConfigAndEnv{
        private String env;
        private YarnConfig yarnConfig;
    }

    public YarnConfigAndEnv getYarnConfigAndEnv(){
        String env = sdpConfig.getEnvFromEnvHolder(log);
        YarnConfig yarnConfig = envMap.get(env);

        YarnConfigAndEnv yarnConfigAndEnv = new YarnConfigAndEnv();
        yarnConfigAndEnv.setEnv(env);
        yarnConfigAndEnv.setYarnConfig(yarnConfig);
        return yarnConfigAndEnv;
    }
}
