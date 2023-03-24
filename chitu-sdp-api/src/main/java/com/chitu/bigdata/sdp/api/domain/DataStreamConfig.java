package com.chitu.bigdata.sdp.api.domain;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author 587694
 * @description: TODO
 * @date 2021/12/21 19:15
 */
@Data
@ApiModel(value = "dataStream配置实体")
public class DataStreamConfig {

    @ApiModelProperty("dataStream jar的id")
    private Long jarId;
    @ApiModelProperty("dataStream  jar的名称")
    private String jarName;
    @ApiModelProperty("dataStream  jar的版本")
    private String jarVersion;
    @ApiModelProperty("dataStream  jar的主函数包名")
    private String mainClass;
    @ApiModelProperty("dataStream  jar的存储地址")
    private String url;
    @ApiModelProperty("dataStream  jar的git地址")
    private String git;
    @ApiModelProperty("dataStream  jar的版本描述")
    private String description;

}
