package com.chitu.bigdata.sdp.api.domain;

import com.alibaba.fastjson.JSONObject;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.LinkedHashMap;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/17 10:51
 */
@Data
@ApiModel(value = "作业配置实体")
public class JobConfig {
    @ApiModelProperty("引擎id")
    private Long engineId;
    @ApiModelProperty("引擎")
    @NotNull(message = "引擎不能为空",groups = {ADD.class})
    private String engine;
    @ApiModelProperty("引擎版本")
    @NotNull(message = "引擎版本不能为空",groups = {ADD.class})
    private String version;

    private String flinkVersion;

    @ApiModelProperty("flink Yaml配置")
    private String flinkYaml;
    @ApiModelProperty("jar的id")
    private Long jarId;
    @ApiModelProperty("jar的名称")
    private String jarName;
    @ApiModelProperty("jar的版本")
    private String jarVersion;
    @ApiModelProperty("flink Yaml配置")
    private JSONObject flinkYamlJson;
    @ApiModelProperty("flink Yaml配置Map")
    private LinkedHashMap<String, String> flinkYamlMap;

    private String engineType;

    private String namespace;

    private String engineQueue;

    private String engineCluster;

    public interface ADD{}
}
