

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <pre>
 * 版本业务实体类
 * </pre>
 * @author 587694
 */
@Data
@ApiModel(value = "作业版本入参")
public class SdpVersionBO {
    @ApiModelProperty("版本ID")
    private Long id;
    @ApiModelProperty("文件名称")
    private String fileName;
    @ApiModelProperty("文件版本")
    private String fileVersion;
    @ApiModelProperty("文件内容")
    private String fileContent;
    @ApiModelProperty("作业配置")
    private String configContent;
    @ApiModelProperty("资源配置")
    private String sourceContent;
    @ApiModelProperty("dataStream配置")
    private String dataStreamConfig;
    @ApiModelProperty("备注")
    private String remark;
    @ApiModelProperty("项目ID")
    private Long projectId;
    @ApiModelProperty("文件ID")
    private Long fileId;
    @ApiModelProperty("比较版本源标识，‘file’：file的版本对比，‘job’：作业新版本对比")
    private String compareSource;
}