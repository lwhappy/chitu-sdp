

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.vo;

import com.chitu.cloud.model.GenericModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.sql.Timestamp;
/**
 * @author 587694
 * @description: TODO
 * @date 2021/10/12 19:28
 */
/**
 * <pre>
 * 用户列表类
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpUserResp extends GenericModel<Long> implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 用户id信息
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Timestamp creationDate;
    /**
     * 字段名称：用户姓名
     * <p>
     * 数据库字段信息:user_name VARCHAR(255)
     */
    private String userName;

    /**
     * 字段名称：员工工号
     * <p>
     * 数据库字段信息:employee_number BIGINT(19)
     */
    private String employeeNumber;

    /**
     * 字段名称：是否管理员
     * <p>
     * 数据库字段信息:is_admin TINYINT(3)
     */
    private Integer isAdmin;

    public SdpUserResp() {
    }	
}