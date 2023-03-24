

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-12-10
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.model.SdpApprove;
import com.chitu.cloud.model.GenericBO;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * <pre>
 * 业务实体类
 * </pre>
 * @author chenyun
 */
@Data
@ApiModel(value = "申请审批入参")
public class SdpApproveBO extends GenericBO<SdpApprove> {
    public SdpApproveBO() {
        setVo(new SdpApprove());
    }
    
    public SdpApprove getSdpApprove() {
        return (SdpApprove) getVo();
    }
    
    public void setSdpApprove(SdpApprove vo) {
    	setVo(vo);
    }

    @ApiModelProperty("项目名称")
    private String projectName;

    @ApiModelProperty("作业名称")
    private String jobName;

    @ApiModelProperty("审批状态")
    private String status;

    @ApiModelProperty("项目ID")
    private Long projectId;

    @ApiModelProperty("文件ID")
    private Long fileId;

    @ApiModelProperty("用户填写的描述")
    private String remark;

    @ApiModelProperty("自动生成的描述")
    private String description;

    @ApiModelProperty("审批意见")
    private String opinion;

    @ApiModelProperty("用户ID")
    private String userId;

}