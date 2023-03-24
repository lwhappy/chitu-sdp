package com.chitu.bigdata.sdp.flink.submit.service;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sutao
 * @create 2021-07-28 9:30
 */
@Component
public class FlinkAppServiceFactory {

    @Autowired
    Map<String, FlinkAppService> flinkAppServiceMap = new ConcurrentHashMap<>(4);

    public FlinkAppService getFlinkAppService(String beanName) {
        FlinkAppService flinkAppService = flinkAppServiceMap.get(beanName);
        if (flinkAppService == null) {
            throw new RuntimeException("部署模式不存在!");
        }
        return flinkAppService;
    }

}
