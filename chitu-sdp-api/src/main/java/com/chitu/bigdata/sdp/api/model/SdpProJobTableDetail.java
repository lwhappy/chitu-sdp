

/**
  * <pre>
  * 作   者：chenyun
  * 创建日期：2022-8-17
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.List;

/**
 * <pre>
 * 项目作业表明细实体类
 * 数据库表名称：sdp_pro_job_table_detail
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpProJobTableDetail extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：项目ID
     * 
     * 数据库字段信息:project_id BIGINT(19)
     */
    private Long projectId;

    /**
     * 字段名称：项目名称
     * 
     * 数据库字段信息:project_name VARCHAR(255)
     */
    private String projectName;

    /**
     * 字段名称：项目等级
     * 
     * 数据库字段信息:project_level VARCHAR(255)
     */
    private String projectLevel;

    /**
     * 字段名称：业务线code
     * 
     * 数据库字段信息:product_line_code VARCHAR(128)
     */
    private String productLineCode;

    /**
     * 字段名称：业务线名称
     * 
     * 数据库字段信息:product_line_name VARCHAR(128)
     */
    private String productLineName;

    /**
     * 字段名称：文件ID
     * 
     * 数据库字段信息:file_id BIGINT(19)
     */
    private Long fileId;

    /**
     * 字段名称：文件名称
     * 
     * 数据库字段信息:file_name VARCHAR(255)
     */
    private String fileName;

    /**
     * 字段名称：作业ID
     * 
     * 数据库字段信息:job_id BIGINT(19)
     */
    private Long jobId;

    /**
     * 字段名称：作业类型
     * 
     * 数据库字段信息:job_type VARCHAR(255)
     */
    private String jobType;

    /**
     * 字段名称：作业名称
     * 
     * 数据库字段信息:job_name VARCHAR(255)
     */
    private String jobName;

    /**
     * 字段名称：作业负责人
     * 
     * 数据库字段信息:job_owner VARCHAR(128)
     */
    private String jobOwner;
    private String jobOwnerNumber;

    /**
     * 字段名称：作业job manager内存
     * 
     * 数据库字段信息:job_jm_memory VARCHAR(128)
     */
    private String jobJmMemory;

    /**
     * 字段名称：作业task manager内存
     * 
     * 数据库字段信息:job_tm_memory VARCHAR(128)
     */
    private String jobTmMemory;

    /**
     * 字段名称：作业并行度
     * 
     * 数据库字段信息:job_parallelism VARCHAR(128)
     */
    private Integer jobParallelism;

    /**
     * 字段名称：作业计算引擎
     * 
     * 数据库字段信息:job_engine_name VARCHAR(128)
     */
    private String jobEngineName;

    /**
     * 字段名称：作业计算引擎版本
     * 
     * 数据库字段信息:job_engine_version VARCHAR(128)
     */
    private String jobEngineVersion;

    /**
     * 字段名称：作业udx资源文件名
     * 
     * 数据库字段信息:job_jar_name VARCHAR(128)
     */
    private String jobJarName;

    /**
     * 字段名称：作业udx资源文件版本
     * 
     * 数据库字段信息:job_jar_version VARCHAR(128)
     */
    private String jobJarVersion;

    /**
     * 字段名称：作业创建人
     * 
     * 数据库字段信息:job_created_by VARCHAR(128)
     */
    private String jobCreatedBy;

    /**
     * 字段名称：作业更新人
     * 
     * 数据库字段信息:job_updated_by VARCHAR(128)
     */
    private String jobUpdatedBy;

    /**
     * 字段名称：作业创建时间
     * 
     * 数据库字段信息:job_creation_date TIMESTAMP(19)
     */
    private Timestamp jobCreationDate;

    /**
     * 字段名称：作业修改时间
     * 
     * 数据库字段信息:job_updation_date TIMESTAMP(19)
     */
    private Timestamp jobUpdationDate;

    /**
     * 字段名称：数据源类型
     * 
     * 数据库字段信息:data_source_type VARCHAR(1000)
     */
    private String dataSourceType;

    /**
     * 字段名称：数据源地址
     * 
     * 数据库字段信息:data_source_url VARCHAR(1000)
     */
    private String dataSourceUrl;

    /**
     * 字段名称：数据库
     * 
     * 数据库字段信息:database_name VARCHAR(255)
     */
    private String databaseName;

    /**
     * 字段名称：flink元表引用名称
     * 
     * 数据库字段信息:flink_table_name VARCHAR(255)
     */
    private String flinkTableName;

    /**
     * 字段名称：物理表名
     * 
     * 数据库字段信息:meta_table_name VARCHAR(255)
     */
    private String metaTableName;

    /**
     * 字段名称：元表类型(source/sink/join)
     * 
     * 数据库字段信息:meta_table_type VARCHAR(50)
     */
    private String metaTableType;

    @TableField(exist = false)
    private List<SdpLineageTableRelation> tableRelations;

    @TableField(exist = false)
    private String startTime;

    @TableField(exist = false)
    private String endTime;

    public SdpProJobTableDetail() {
    }

    public SdpProJobTableDetail(Long jobId) {
        this.jobId= jobId;
    }

}