

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.chitu.bigdata.sdp.api.domain.ProjectUser;
import com.chitu.bigdata.sdp.api.domain.SdpEngineInfo;
import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * <pre>
 * 项目实体类
 * 数据库表名称：sdp_project
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpProject extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

    /**
     * 字段名称：项目名称
     * 
     * 数据库字段信息:project_name VARCHAR(255)
     */
    private String projectName;

    private String projectCode;

    private String productLineName;

    private String productLineCode;

    private String businessFlag = "SDP";

    @TableField(exist = false)
    private List<ProjectUser> userList;

    @TableField(exist = false)
    private List<SdpEngineInfo> engineList;

    private Integer priority;

    private String appKey;
    private String appSecret;

    private Integer forbidUdxUpdation;
    private Integer allowJobAddEdit;

    public SdpProject() {
    }	
}