package com.chitu.bigdata.sdp.config;

import com.chitu.bigdata.sdp.api.domain.ClusterInfo;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;


@Data
@Component
@ConfigurationProperties(prefix = "kubernetes")
public class KubernetesClusterConfig {


    //    private String dockerFileWorkspace;
//    private String dockerHubAddress;
    private Long buildImageTimeout;
    private Long startMaxCheckNumber;
    private String logFilterField;
    private String imagePrefix;
    private String dockerFilePrefix;
    private String sqlBaseImage;
    private String flinkUserJarPrefix;
    private String sql114Jar;
    private String sql115Jar;

//    private String masterUrl;
//    private String caCertData;
//    private String clientCertData;
//    private String clientKeyData;


    private String hadoopConfigMapName;
    private String jobManagerServiceAccount;


    private Map<String, Config> envMap;

    @Data
    public static class Config {
        private String k8sLogUrl;
        private String k8sLogIndex;
        private List<ClusterInfo> clusters;
    }

}


