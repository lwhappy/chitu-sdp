package com.chitu.bigdata.sdp.api.domain;

import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * 添加引擎用户类
 * @author 587694
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EngineUserInfo extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 引擎id
     */
    private Long engineId;
    /**
     * 字段名称：用户姓名
     *
     * 数据库字段信息:user_name VARCHAR(255)
     */
    private String userName;


    private String nameOrNumber;
    /**
     * 字段名称：员工工号
     *
     * 数据库字段信息:employee_number BIGINT(19)
     */
    private String employeeNumber;
    /**
     * 用户表id
     */
    private Long id;



}
