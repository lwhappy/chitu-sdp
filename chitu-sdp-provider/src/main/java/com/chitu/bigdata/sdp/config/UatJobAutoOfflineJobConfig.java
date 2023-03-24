package com.chitu.bigdata.sdp.config;

import com.chitu.bigdata.sdp.api.enums.EnvironmentEnum;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Configuration;

/**
 * @author zouchangzhen
 * @date 2022/5/18
 */
@Configuration
@RefreshScope
@Data
public class UatJobAutoOfflineJobConfig {

    @Autowired
    SdpConfig sdpConfig;


    /**
     * 是否是uat或者dev环境
     * @return
     */
    public boolean isUatOrDev(){
        String env = sdpConfig.getEnvFromEnvHolder(null);
        return (EnvironmentEnum.UAT.getCode().equalsIgnoreCase(env) || EnvironmentEnum.DEV.getCode().equalsIgnoreCase(env));
    }
}
