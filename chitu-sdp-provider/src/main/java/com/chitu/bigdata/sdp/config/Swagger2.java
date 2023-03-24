package com.chitu.bigdata.sdp.config;

import com.github.xiaoymin.knife4j.spring.annotations.EnableKnife4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.ParameterBuilder;
import springfox.documentation.builders.PathSelectors;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.schema.ModelRef;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Parameter;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;
import springfox.documentation.swagger2.annotations.EnableSwagger2;

import java.util.ArrayList;
import java.util.List;

/**
 * @author: chenyun
 * @Date: 2021/10/20/16:46
 * @Description:
 */
@Configuration
@EnableSwagger2
@EnableKnife4j
public class Swagger2 {
    @Bean
    public Docket createRestApi() {
        List<Parameter> parameters = new ArrayList<>();
        parameters.add(new ParameterBuilder()
                .name("projectId")
                .description("项目ID")
                .modelRef(new ModelRef("long"))
                .parameterType("header")
                .required(false)
                .build());

        parameters.add(new ParameterBuilder()
                .name("X-uid")
                .description("用户ID")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false)
                .build());

        parameters.add(new ParameterBuilder()
                .name("token")
                .description("token")
                .modelRef(new ModelRef("string"))
                .parameterType("header")
                .required(false)
                .build());

        return new Docket(DocumentationType.SWAGGER_2)
                .apiInfo(apiInfo())
                .globalOperationParameters(parameters)
                .select()
                .apis(RequestHandlerSelectors.basePackage("com.chitu.bigdata.sdp.controller"))
                .paths(PathSelectors.any())
                .build();
    }

    private ApiInfo apiInfo() {
        return new ApiInfoBuilder()
                .title("赤兔实时计算平台平台")
                .description("赤兔实时计算平台平台提供端到端亚秒级实时数据分析能力，并通过标准SQL降低开发门槛，提高数开人员开发能力，提高数据分析及处理效率")
                .termsOfServiceUrl("https://*****")
                .version("1.0")
                .build();
    }
}
