

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <pre>
 * 项目用户关系表实体类
 * 数据库表名称：sdp_project_user
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpProjectUser extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：项目ID
     * 
     * 数据库字段信息:project_id BIGINT(19)
     */
    private Long projectId;

    /**
     * 字段名称：用户ID
     * 
     * 数据库字段信息:user_id BIGINT(19)
     */
    private Long userId;

    /**
     * 字段名称：是否是负责人（1：是，0：否）
     *
     * 数据库字段信息:is_leader BIGINT(19)
     */
    private Integer isLeader;

    public SdpProjectUser() {
    }

    public SdpProjectUser(Long projectId, Integer isLeader) {
        this.projectId = projectId;
        this.isLeader = isLeader;
    }
}