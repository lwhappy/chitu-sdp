package com.chitu.bigdata.sdp.api.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/17 10:52
 */
@Data
@ApiModel(value = "资源配置实体")
public class SourceConfig {
    @ApiModelProperty("配置模式")
    @NotNull(message = "配置模式不能为空",groups = {ADD.class})
    private String mode = "基础模式";
    @ApiModelProperty("并发度")
    @NotNull(message = "并发度不能为空",groups = {ADD.class})
    private Integer parallelism = 1;
    @ApiModelProperty("jobManager CPU")
    private Integer jobManagerCpu = 1;
    @ApiModelProperty("jobManager 内存")
    @NotNull(message = "jobManager内存不能为空",groups = {ADD.class})
    private String jobManagerMem = "1";
    @ApiModelProperty("taskManager CPU")
    private Integer taskManagerCpu = 1;
    @ApiModelProperty("taskManager 内存")
    @NotNull(message = "taskManager内存不能为空",groups = {ADD.class})
    private String taskManagerMem = "1";

    public interface ADD{}
}
