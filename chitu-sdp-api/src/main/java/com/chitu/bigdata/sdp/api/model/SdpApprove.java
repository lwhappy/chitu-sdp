

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-12-10
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.chitu.cloud.model.GenericModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * <pre>
 * 实体类
 * 数据库表名称：sdp_approve
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpApprove extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：项目名称
     * 
     * 数据库字段信息:project_name VARCHAR(255)
     */
    @ApiModelProperty("项目名称")
    private String projectName;

    /**
     * 字段名称：作业名称
     * 
     * 数据库字段信息:job_name VARCHAR(255)
     */
    @ApiModelProperty("作业名称")
    private String jobName;

    @ApiModelProperty("审批人")
    private String approver;

    @ApiModelProperty("二级审批人")
    private String approver2;

    /**
     * 字段名称：审批状态
     * 
     * 数据库字段信息:status VARCHAR(255)
     */
    @ApiModelProperty("审批状态")
    private String status;

    @ApiModelProperty("审批状态")
    @TableField(exist = false)
    private List<String> statuss;

    /**
     * 字段名称：项目ID
     * 
     * 数据库字段信息:project_id BIGINT(19)
     */
    @ApiModelProperty("项目ID")
    private Long projectId;

    @ApiModelProperty("文件ID")
    private Long fileId;

    /**
     * 字段名称：描述
     * 
     * 数据库字段信息:description VARCHAR(255)
     */
    @ApiModelProperty("用户填写的描述")
    private String remark;

    @ApiModelProperty("自动生成的描述")
    private String description;

    @ApiModelProperty("审批意见")
    private String opinion;

    @ApiModelProperty("二级审批意见")
    private String opinion2;

    @ApiModelProperty("查询类型")
    @TableField(exist = false)
    private String type;

    @TableField(exist = false)
    private String currentUser;

    @ApiModelProperty("开始时间")
    @TableField(exist = false)
    private String startTime;

    @ApiModelProperty("结束时间")
    @TableField(exist = false)
    private String endTime;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Timestamp updationDate2;

    private String updatedBy2;

    @TableField(exist = false)
    private String employeeNumber;

    @TableField(exist = false)
    private Boolean canApprove = true;

    @TableField(exist = false)
    private Long folderId;

    public SdpApprove() {
    }	
}