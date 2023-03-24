package com.chitu.cloud.web.config;

import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @Author liubin
 * @Date 2020/2/6 2:04
 **/
@EnableDiscoveryClient
@EnableFeignClients(basePackages={"com.chitu"})
@ComponentScan(basePackages={"com.chitu"})
@MapperScan(basePackages = {"com.chitu"}, annotationClass = Mapper.class)
public class SdpFrameworkConfiguration {
}
