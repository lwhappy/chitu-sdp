

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-11-8
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
 * jar管理实体类
 * 数据库表名称：sdp_jar
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpJar extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：jar名称
     * 
     * 数据库字段信息:name VARCHAR(255)
     */
    private String name;

    /**
     * 字段名称：jar版本
     * 
     * 数据库字段信息:version VARCHAR(255)
     */
    private String version;

    private String flinkVersion;

    @TableField(exist = false)
    private String newVersion;

    /**
     * 字段名称：git地址
     * 
     * 数据库字段信息:git VARCHAR(255)
     */
    private String git;

    private String url;

    /**
     * 字段名称：jar描述
     * 
     * 数据库字段信息:describe VARCHAR(255)
     */
    private String description;

    private Long projectId;

    @TableField(exist = false)
    private Integer jobs = 0;


    @TableField(exist = false)
    private String type;


    @TableField(exist = false)
    private String mainClass;


    @TableField(exist = false)
    private Integer forbidUdxUpdation;

    public SdpJar() {
    }	
}