

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.vo;

import com.chitu.bigdata.sdp.api.domain.ProjectUser;
import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <pre>
 * 项目实体类
 * 数据库表名称：sdp_project
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpProjectResp extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    /**
     * 字段名称：项目名称
     *
     * 数据库字段信息:project_name VARCHAR(255)
     */
    private String projectName;

    private String projectCode;

    /**
     * 字段名称：责任人
     */
    private String projectOwner;
    /**
     * 字段名称：项目用户
     */
    private String projectUsers;
    /**
     * 字段名称：项目引擎
     */
    private String projectEngines;
    /**
     * 字段名称：项目用户角色
     */
    private ProjectUser projectUserRole;

    private String productLineName;

    private String productLineCode;

    private Integer userCount;

    private String ownerName;

    /**
     * 在线作业数
     */
    private Integer onlineJobNum;

    private Integer priority;

    private Integer forbidUdxUpdation;
    private Integer allowJobAddEdit;

    public SdpProjectResp() {
    }	
}