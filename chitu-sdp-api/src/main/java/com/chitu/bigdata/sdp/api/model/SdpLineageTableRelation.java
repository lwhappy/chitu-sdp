

/**
  * <pre>
  * 作   者：chenyun
  * 创建日期：2022-8-17
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <pre>
 * 表关系实体类
 * 数据库表名称：sdp_lineage_table_relation
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpLineageTableRelation extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：文件ID
     * 
     * 数据库字段信息:file_id BIGINT(19)
     */
    private Long fileId;

    /**
     * 字段名称：作业ID
     * 
     * 数据库字段信息:job_id BIGINT(19)
     */
    private Long jobId;

    /**
     * 字段名称：作业名称
     * 
     * 数据库字段信息:job_name VARCHAR(255)
     */
    private String jobName;

    /**
     * 字段名称：source数据源类型
     * 
     * 数据库字段信息:source_data_source_type VARCHAR(1000)
     */
    private String sourceDataSourceType;

    /**
     * 字段名称：sink数据源类型
     * 
     * 数据库字段信息:sink_data_source_type VARCHAR(1000)
     */
    private String sinkDataSourceType;

    /**
     * 字段名称：source数据源地址
     * 
     * 数据库字段信息:source_data_source_url VARCHAR(1000)
     */
    private String sourceDataSourceUrl;

    /**
     * 字段名称：sink数据源地址
     * 
     * 数据库字段信息:sink_data_source_url VARCHAR(1000)
     */
    private String sinkDataSourceUrl;

    /**
     * 字段名称：source数据库
     * 
     * 数据库字段信息:source_database_name VARCHAR(255)
     */
    private String sourceDatabaseName;

    /**
     * 字段名称：sink数据库
     * 
     * 数据库字段信息:sink_database_name VARCHAR(255)
     */
    private String sinkDatabaseName;

    /**
     * 字段名称：source表名
     * 
     * 数据库字段信息:source_table_name VARCHAR(255)
     */
    private String sourceTableName;

    /**
     * 字段名称：sink表名
     * 
     * 数据库字段信息:sink_table_name VARCHAR(255)
     */
    private String sinkTableName;

    public SdpLineageTableRelation() {
    }
    public SdpLineageTableRelation(Long jobId) {
        this.jobId= jobId;
    }
}