package com.chitu.bigdata.sdp.config;

import cn.hutool.core.util.StrUtil;
import com.chitu.bigdata.sdp.api.enums.EnvironmentEnum;
import com.chitu.bigdata.sdp.interceptor.EnvHolder;
import lombok.Data;
import org.slf4j.Logger;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author zouchangzhen
 * @date 2022/10/11
 */
@Component
@ConfigurationProperties(prefix = "sdp")
@Data
public class SdpConfig {

    private String env;

    /**
     * 平台配置了多少套环境：该配置主要是给定时任务使用
     */
    private List<String> envList;


    /**
     * <p>应用于登录 、获取用户信息、发送告警等</p>
     * 从配置中获取环境变量。
     * dev需要转到uat上执行的，需要转换一下
     * @return
     */
    public String getEnvFromConfig(){
        return  getOrDefault(env);
    }


    /**
     * <p>应用于根据环境获取配置信息</p>
     * 从EnvHolder获取env，如果没有则取配置文件里面的env.
     *
     * <p>EnvHolder中的env来源：1.http请求头，2.定时任务指定</p>
     * @param log
     * @return
     */
    public String getEnvFromEnvHolder(Logger log){
        String env = EnvHolder.getEnv();
        if(Objects.nonNull(log) && log.isTraceEnabled()){
            log.trace("【env环境变量1】: {}", Objects.nonNull(env)?env:"null");
        }
        env = getOrDefault(env);
        if(Objects.nonNull(log) && log.isTraceEnabled()){
            log.trace("【env环境变量2】: {}",Objects.nonNull(env)?env:"null");
        }
        return env;
    }

    /**
     * 平台配置了多少套环境：该配置主要是给定时任务使用
     * @return
     */
    public  List<String> getEnvList(){
        List<String> envs = envList.stream().map(this::getOrDefault).distinct().collect(Collectors.toList());
        return envs;
    }


    /**
     * 如果mEnv为空则取平台配置的环境变量
     * @return
     */
    public String getOrDefault(String mEnv){
        if(StrUtil.isBlank(mEnv)){
            mEnv = env;
        }
        //dev需要转到uat上执行并且入库的env字段值也是uat
        return  EnvironmentEnum.DEV.getCode().equalsIgnoreCase(mEnv)? EnvironmentEnum.UAT.getCode() : mEnv;
    }


    /**
     *  <p>应用于需要指定环境请求 例如：创建应用、请求datahub</p>
     * @param
     * @return
     */
    public String getEnv4SpecifyEnvRequest(){
        //页面环境配置
        String mEnv = getEnvFromEnvHolder(null);
        //如果配置文件配置的env是uat或者dev && 页面传的是prod
        return isUatDomainAndProdEnv(mEnv) ? EnvironmentEnum.UAT.getCode() : isProdDomainAndUatEnv(mEnv) ? EnvironmentEnum.UAT.getCode() : mEnv;
    }

    /**
     * 是否是uat(dev)域名、prod环境
     * @param pageEnv
     * @return
     */
    public boolean isUatDomainAndProdEnv(String pageEnv){
        return
                (
                EnvironmentEnum.UAT.getCode().equalsIgnoreCase(env)
                        || EnvironmentEnum.DEV.getCode().equalsIgnoreCase(env)
                )
                && EnvironmentEnum.PROD.getCode().equalsIgnoreCase(pageEnv);
    }

    /**
     * 是否是prod域名、uat环境
     * @param pageEnv
     * @return
     */
    public boolean isProdDomainAndUatEnv(String pageEnv){
        return
                 EnvironmentEnum.PROD.getCode().equalsIgnoreCase(env)
                 && EnvironmentEnum.UAT.getCode().equalsIgnoreCase(pageEnv);
    }

}
