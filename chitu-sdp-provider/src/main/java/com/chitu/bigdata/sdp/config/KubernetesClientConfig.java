package com.chitu.bigdata.sdp.config;

import com.chitu.bigdata.sdp.api.domain.ClusterInfo;
import com.chitu.bigdata.sdp.api.enums.EnvironmentEnum;
import com.chitu.cloud.utils.SpringUtils;
import io.fabric8.kubernetes.client.Config;
import io.fabric8.kubernetes.client.ConfigBuilder;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

@Component
@Configuration
public class KubernetesClientConfig {


    @Bean
    public KubernetesClient kubernetesUatClient(KubernetesClusterConfig kubernetesClusterConfig){
        ClusterInfo clusterInfo = kubernetesClusterConfig.getEnvMap().get(EnvironmentEnum.UAT.getCode()).getClusters().get(0);
        Config config = new ConfigBuilder().withMasterUrl(clusterInfo.getMasterUrl()).withTrustCerts(true)
                .withCaCertData(clusterInfo.getCaCertData())
                .withClientCertData(clusterInfo.getClientCertData())
                .withClientKeyData(clusterInfo.getClientKeyData())
                .build();
        return new KubernetesClientBuilder().withConfig(config).build();
    }

    @Bean
    public KubernetesClient kubernetesProdClient(KubernetesClusterConfig kubernetesClusterConfig){
        ClusterInfo clusterInfo = kubernetesClusterConfig.getEnvMap().get(EnvironmentEnum.PROD.getCode()).getClusters().get(0);
        Config config = new ConfigBuilder().withMasterUrl(clusterInfo.getMasterUrl()).withTrustCerts(true)
                .withCaCertData(clusterInfo.getCaCertData())
                .withClientCertData(clusterInfo.getClientCertData())
                .withClientKeyData(clusterInfo.getClientKeyData())
                .build();
        return new KubernetesClientBuilder().withConfig(config).build();
    }

    public KubernetesClient getKubernetesClient(String env){
        if(EnvironmentEnum.UAT.getCode().equalsIgnoreCase(env)){
            return (KubernetesClient)SpringUtils.getBean("kubernetesUatClient");
        }else if(EnvironmentEnum.PROD.getCode().equalsIgnoreCase(env)){
            return (KubernetesClient)SpringUtils.getBean("kubernetesProdClient");
        }
        return null;
    }

}
