package com.chitu.bigdata.sdp.interceptor;

import com.chitu.bigdata.sdp.config.SdpConfig;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Slf4j
public class EnvInterceptor implements HandlerInterceptor {

    @Autowired
    SdpConfig sdpConfig;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        //先清除
        EnvHolder.clearEnv();
        String env = request.getHeader("env");
        if (StringUtils.isEmpty(env)) {
            env = sdpConfig.getEnv();
            log.warn("请求地址 [{}],请求头env为空,取配置文件中的env配置值: {}",request.getRequestURI(),env);
        }
        //log.info("请求地址 [{}], 环境 [{}]", request.getRequestURI(), env);
        if (StringUtils.isBlank(env)) return true;
        EnvHolder.addEnv(sdpConfig.getOrDefault(env));
        return true;
    }
}
