package com.chitu.cloud.web.config;

import com.chitu.cloud.web.interceptor.ContextInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Created by henry on 2017/1/12.
 */
@Configuration
public class SdpWebMvcConfigurer implements WebMvcConfigurer {


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ContextInterceptor())
                .addPathPatterns("/**")
                .excludePathPatterns("/favicon.ico")
                .order(0);
    }


}
