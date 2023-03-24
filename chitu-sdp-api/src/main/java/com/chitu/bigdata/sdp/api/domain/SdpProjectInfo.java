package com.chitu.bigdata.sdp.api.domain;

import com.chitu.bigdata.sdp.api.model.SdpEngine;
import com.chitu.bigdata.sdp.api.model.SdpUser;
import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;


/**
 * 项目对象入参出参类
 *  @author 587694
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpProjectInfo extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：项目名称
     *
     * 数据库字段信息:project_name VARCHAR(255)
     *
     */
    private String projectName;
    /**
     * 入参模糊搜索名字还是编号
     */
    private String nameOrNumber;
    /**
     * 出参引擎数组
     */
    private List<SdpEngine> projectEngines;
    /**
     * 出参项目负责人
     */
    private List<SdpUser> projectLeader;
    /**
     * 出参项目使用者和管理者
     */
    private List<ProjectUser> projectUsers;
    /**
     * 项目编码
     */
    private String projectCode;

    private String businessFlag;

    private String productLineName;

    private String productLineCode;

    private Integer priority;

    private String env;

    private Integer forbidUdxUpdation;
    private Integer allowJobAddEdit;

}
