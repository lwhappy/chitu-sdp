

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.domain.DataStreamConfig;
import com.chitu.bigdata.sdp.api.domain.JobConfig;
import com.chitu.bigdata.sdp.api.domain.SourceConfig;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <pre>
 * 文件业务实体类
 * </pre>
 * @author chenyun
 */
@Data
@ApiModel(value = "文件入参实体")
public class SdpFileBO {
    @ApiModelProperty("文件ID")
    private Long id;
    @ApiModelProperty("文件名称")
    @NotNull(message = "文件名称不能为空",groups = {ADD.class})
    private String fileName;
    @ApiModelProperty("文件类型")
    private String fileType;
    @ApiModelProperty("文件内容")
    private String content;
    @ApiModelProperty("元表sql")
    private String metaTableContent;
    @ApiModelProperty("转换语句sql")
    private String etlContent;
    @Valid
    @ApiModelProperty("作业配置")
    @NotNull(message = "作业配置不能为空",groups = {ADD.class})
    private JobConfig jobConfig;
    @Valid
    @ApiModelProperty("资源配置")
    @NotNull(message = "资源配置不能为空",groups = {ADD.class})
    private SourceConfig sourceConfig;
    @ApiModelProperty("ds配置")
    private DataStreamConfig dataStreamConfig;
    @ApiModelProperty("项目ID")
    private Long projectId;
    @ApiModelProperty("目录ID")
    private Long folderId;
    @ApiModelProperty("备注")
    private String remark;
    @ApiModelProperty("当前用户id")
    private Long userId;
    @ApiModelProperty("锁定人")
    private String lockedBy;

    /**
     *DB中转以jso格式存储
     */
    private String configContent;
    private String sourceContent;
    private String dataStreamConf;
    /**
     * 对接第三方业务系统参数
     */
    private String businessFlag = "SDP";
    private Long jobId;

    private String businessValue;
    private String techSpecifications;
    private Long fileId;

    private List<String> fileNames;

    private Integer priority;


    public interface ADD{}
}