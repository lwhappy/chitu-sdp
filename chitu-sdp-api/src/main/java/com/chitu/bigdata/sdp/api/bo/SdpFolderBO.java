

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

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

/**
 * <pre>
 * 目录业务实体类
 * </pre>
 * @author chenyun
 */
@Data
@ApiModel(value = "目录入参实体")
public class SdpFolderBO {
    @ApiModelProperty("目录ID")
    private Long id;
    @Valid
    @ApiModelProperty("目录名称")
    @NotNull(message = "目录名称不能为空")
    private String folderName;
    @ApiModelProperty("父目录ID")
    private Long parentId;
    @ApiModelProperty("项目ID")
    private Long projectId;
    private String businessFlag = "SDP";
}