package com.chitu.bigdata.sdp.flink.submit;


import com.chitu.cloud.web.config.SdpFrameworkConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;

@SpringBootApplication
@Import({SdpFrameworkConfiguration.class})
public class BigdataSdpFlinkSubmit115Application {
    public static void main(String[] args) {
        SpringApplication.run(BigdataSdpFlinkSubmit115Application.class, args);
    }
}