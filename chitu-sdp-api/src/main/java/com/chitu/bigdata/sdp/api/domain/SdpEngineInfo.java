

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.domain;


import com.chitu.bigdata.sdp.api.model.SdpEngine;
import com.chitu.bigdata.sdp.api.model.SdpUser;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;


/**
 * <pre>
 * 引擎实体类
 * 数据库表名称：sdp_engine
 * </pre>
 * @author 587694
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpEngineInfo extends SdpEngine implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：引擎名称
     *
     * 数据库字段信息:engine_name VARCHAR(255)
     */
    private String engineName;

    /**
     * 字段名称：引擎版本
     *
     * 数据库字段信息:engine_version VARCHAR(255)
     */
    private String engineVersion;

    /**
     * 字段名称：引擎url
     *
     * 数据库字段信息:engine_url VARCHAR(255)
     */
    private String engineUrl;

    /**
     * 字段名称：用户json
     */
    private List<SdpUser> userInfo;
    /**
     * 引擎用户
     */
    private List<EngineUserInfo> engineUsers;
    /**
     * 引擎用户总数，用于引擎列表对用户数的排序
     */
    private Long userCount;

    /**
     * 引擎引用的项目总数
     */
    private Long referProjectCount;

    /**
     * 字段名称：引擎集群
     *
     * 数据库字段信息:engine_cluster VARCHAR(255)
     */
    private String engineCluster;

    private String clusterName;
    /**
     * 字段名称：引擎队列
     *
     * 数据库字段信息:engine_queue VARCHAR(255)
     */
    private String engineQueue;

    private Long engineId;
    private String engineType;
    private String uatEngineCluster;
    private String uatEngineQueue;
    private String uatClusterName;
    private String namespace;
    private String uatNamespace;


    public SdpEngineInfo() {
    }	
}