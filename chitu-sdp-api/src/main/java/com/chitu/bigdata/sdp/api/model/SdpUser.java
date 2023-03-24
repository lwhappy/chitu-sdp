

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.chitu.cloud.model.GenericModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * <pre>
 * 用户实体类
 * 数据库表名称：sdp_user
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpUser extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Timestamp lastLogin;

    private String userName;

    @TableField(exist = false)
    private String name;

    private String employeeNumber;

    private String password;

    private String employeeId;

    private String privateMobile;

    private String email;

    private Integer isAdmin;

    private Long projectId;

    public SdpUser() {
    }
    public SdpUser(Integer isAdmin) {
        this.isAdmin = isAdmin;
    }
    public SdpUser(String employeeNumber) {
        this.employeeNumber = employeeNumber;
    }
    public SdpUser(Long projectId) {
        this.projectId = projectId;
    }
}