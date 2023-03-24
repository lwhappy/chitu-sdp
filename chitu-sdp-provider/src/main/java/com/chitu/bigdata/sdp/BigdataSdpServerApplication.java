package com.chitu.bigdata.sdp;


import com.chitu.cloud.config.scheduler.SchedulerConfiguration;
import com.chitu.cloud.web.config.SdpFrameworkConfiguration;
import org.redisson.spring.starter.RedissonAutoConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication(exclude ={RedissonAutoConfiguration.class})
@Import({SdpFrameworkConfiguration.class, SchedulerConfiguration.class})
public class BigdataSdpServerApplication {
    public static void main(String[] args) {
        SpringApplication.run(BigdataSdpServerApplication.class, args);
    }
}