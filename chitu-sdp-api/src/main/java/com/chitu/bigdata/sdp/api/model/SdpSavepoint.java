

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-4-18
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.chitu.cloud.model.GenericModel;
import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * <pre>
 * 保存点记录信息实体类
 * 数据库表名称：sdp_savepoint
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpSavepoint extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：sdp_job.id
     * 
     * 数据库字段信息:job_id BIGINT(19)
     */
    private Long jobId;

    /**
     * 字段名称：保存点名称
     * 
     * 数据库字段信息:savepoint_name VARCHAR(255)
     */
    private String savepointName;

    /**
     * 字段名称：触发时间
     * 
     * 数据库字段信息:trigger_time DATETIME(19)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Timestamp triggerTime;

    /**
     * 字段名称：保存点路径
     * 
     * 数据库字段信息:file_path VARCHAR(1000)
     */
    private String filePath;

    /**
     * 字段名称：操作状态
     * 
     * 数据库字段信息:operate_status VARCHAR(255)
     */
    private String operateStatus;

    /**
     * 字段名称：操作异常信息
     * 
     * 数据库字段信息:operate_err_msg TEXT(65535)
     */
    private String operateErrMsg;

    /**
     * 字段名称：环境
     * 
     * 数据库字段信息:env VARCHAR(10)
     */
    private String env;

    /**
     * 字段名称：日志追踪ID
     * 
     * 数据库字段信息:trace_id VARCHAR(128)
     */
    private String traceId;

    /**
     *序号
     */
    @TableField(exist = false)
    private Integer orderNum;

    public SdpSavepoint() {
    }	
}