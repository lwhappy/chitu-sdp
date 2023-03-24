

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.domain;

import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <pre>
 * 用户实体类
 * 数据库表名称：sdp_user
 * </pre>
 * @author 587694
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ProjectUser extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 项目id
     */
    private Long projectId;
    /**
     * 用户id信息
     */

    private Long id;


    /**
     * 字段名称：用户姓名
     *
     * 数据库字段信息:user_name VARCHAR(255)
     */
    private String userName;

    /**
     * 字段名称：员工工号
     *
     * 数据库字段信息:employee_number BIGINT(19)
     */
    private String employeeNumber;

    /**
     * 字段名称：是否管理员
     *
     * 数据库字段信息:is_admin TINYINT(3)
     */
    private Integer isAdmin;

    /**
     * 字段名称：项目角色
     * 2：负责人，1：项目管理员，0：普通用户
     */
    private Integer isLeader;
    /**
     * 项目名称
     */
   private String projectName;

    /**
     * 员工手机
     */
    private String privateMobile;

   private String projectCode;

   private Long userId;

   private Integer projectLeader;

    public ProjectUser() {
    }	
}