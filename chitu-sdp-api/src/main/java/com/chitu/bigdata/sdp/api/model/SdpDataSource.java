

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-12-9
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;


import com.baomidou.mybatisplus.annotation.TableField;
import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.Map;

/**
 * <pre>
 * 实体类
 * 数据库表名称：sdp_data_source
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpDataSource extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;


    private Long id;
    /**
     * 字段名称：实例名称
     * 
     * 数据库字段信息:data_source_name VARCHAR(255)
     */
    private String dataSourceName;

    /**
     * 字段名称：数据源类型
     * 
     * 数据库字段信息:data_source_type VARCHAR(255)
     */
    private String dataSourceType;

    /**
     * 字段名称：描述
     * 
     * 数据库字段信息:remark TEXT(65535)
     */
    private String remark;
    /**
     * 字段名称：项目id
     *
     * 数据库字段信息:projectId BIGINT(20)
     */
    private Long projectId;
    /**
     * 字段名称：责任人
     * 
     * 数据库字段信息:owner VARCHAR(255)
     */
    private Long owner;

    /**
     * 字段名称：stream load地址
     *
     * 数据库字段信息:stream_load_url VARCHAR(255)
     */
    private String  streamLoadUrl;

    /**
     * 字段名称：数据源地址
     * 
     * 数据库字段信息:data_source_url VARCHAR(255)
     */
    private String dataSourceUrl;

    /**
     * 字段名称：数据库
     * 
     * 数据库字段信息:database_name VARCHAR(255)
     */
    private String databaseName;
    /**
     * 字段名称：认证方式
     *
     * 数据库字段信息:certify_type VARCHAR(255)
     */
    private String certifyType;

    /**
     * 字段名称：用户名
     * 
     * 数据库字段信息:user_name VARCHAR(255)
     */
    private String userName;

    /**
     * 字段名称：密码
     * 
     * 数据库字段信息:password VARCHAR(255)
     */
    private String password;

    /**
     * hbase专用属性
     */
    private String hbaseZnode;

    /**
     * hive hadoop配置目录
     */
    private String hadoopConfDir;

    
    private String hiveCluster;

    /**
     * 是否使用datahub白名单应用和密码,非数据库字段
     */
    @TableField(exist = false)
    private boolean isWhiteList = false;


    private String hudiCatalogPath;

    private String env;

    /**
     * 非数据库字段，flinkTable 和 sql的映射关系
     */
    @TableField(exist = false)
    private Map<String,String> kafkaFlinkTableAndCreateTableMap;
}