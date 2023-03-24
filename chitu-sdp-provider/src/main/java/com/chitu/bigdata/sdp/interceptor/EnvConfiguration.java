package com.chitu.bigdata.sdp.interceptor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class EnvConfiguration {

    @Bean
    public EnvInterceptor envInterceptor() {
        return new EnvInterceptor();
    }

    @Bean
    public WebMvcConfigurer envWebMvcConfigurer(EnvInterceptor interceptor) {
        return new WebMvcConfigurer() {
            @Override
            public void addInterceptors(InterceptorRegistry registry) {
                registry.addInterceptor(interceptor)
                        .addPathPatterns("/**");
//                        .excludePathPatterns("/api/**");
            }
        };
    }

    @Bean
    public EnvInterceptorInit envInterceptorInit(Environment environment) {
        return new EnvInterceptorInit(environment);
    }

}
