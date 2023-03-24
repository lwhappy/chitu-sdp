

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <pre>
 * 引擎实体类
 * 数据库表名称：sdp_engine
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpEngine extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;

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
     * 字段名称：引擎集群
     *
     * 数据库字段信息:engine_cluster VARCHAR(255)
     */
    private String engineCluster;
    /**
     * 字段名称：引擎队列
     *
     * 数据库字段信息:engine_queue VARCHAR(255)
     */
    private String engineQueue;

    private String engineType;
    private String uatEngineCluster;
    private String uatEngineQueue;

    private String namespace;
    private String uatNamespace;

    @TableField(exist = false)
    private String businessFlag = "SDP";

    @TableField(exist = false)
    private Long projectId;

    public SdpEngine() {
    }
    public SdpEngine(String engineName) {
        this.engineName = engineName;
    }
}