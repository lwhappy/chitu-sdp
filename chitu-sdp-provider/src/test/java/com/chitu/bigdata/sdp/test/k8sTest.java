package com.chitu.bigdata.sdp.test;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.chitu.bigdata.sdp.api.flink.Application;
import com.chitu.bigdata.sdp.config.KubernetesClientConfig;
import com.chitu.cloud.web.test.BaseTest;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class k8sTest extends BaseTest {

    @Autowired
    KubernetesClientConfig kubernetesClientConfig;

    @Test
    public void k8sTest() {
        KubernetesClient kubernetesClient = kubernetesClientConfig.getKubernetesClient("uat");
        PodList list = kubernetesClient.pods().inNamespace("hadoop-test-ha").list();
        List<Pod> podList1 = list.getItems().stream().filter(item -> item.getMetadata().getName().contains("product-test-connector-test-es-")).filter(item -> !item.getMetadata().getName().contains("product-test-connector-test-es-taskmanager")).collect(Collectors.toList());
        List<Pod> podList2 = list.getItems().stream().filter(item -> item.getMetadata().getName().contains("product-test-connector-test-es-")).collect(Collectors.toList());;
        System.out.println(podList1);
    }


    @Test
    public void updateConfigMap() {
        Application application = new Application();
        application.setNamespace("hadoop-test-ha");
        application.setJobName("product_test_connector_test_es6-yarn".replace("_", "-"));
        KubernetesClient kubernetesClient = kubernetesClientConfig.getKubernetesClient("uat");
        Resource<ConfigMap> configMapResource = kubernetesClient.configMaps().inNamespace(application.getNamespace()).withName("flink-config-" + application.getJobName());
        ConfigMap configMap = configMapResource.get();
        Map<String, String> data = configMap.getData();
        if (data != null) {
            String logKey = "log4j-console.properties";
            String oldLogStr = data.get(logKey);
            System.out.println(oldLogStr);
            String newLogStr = FileUtil.readString(new File("D:\\opt\\apache\\flink-1.14.3\\conf\\log4j-console.properties"), StandardCharsets.UTF_8);
            if (StrUtil.isNotEmpty(newLogStr)) {
                data.put(logKey, newLogStr);
                kubernetesClient.configMaps().resource(configMap).createOrReplace();
            }
        }


    }


}
