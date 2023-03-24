

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-5-16
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.chitu.cloud.model.GenericModel;

import java.io.Serializable;

/**
 * <pre>
 * UAT环境作业运行配置实体类
 * 数据库表名称：sdp_uat_job_running_config
 * </pre>
 */
public class SdpUatJobRunningConfig extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：sdp_job.id
     * 
     * 数据库字段信息:job_id BIGINT(19)
     */
    private Long jobId;

    /**
     * 字段名称：运行时长,单位：天,值是-1代表永久有效
     * 
     * 数据库字段信息:days INT(10)
     */
    private Integer days;

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

    public SdpUatJobRunningConfig() {
    }	
    public Long getJobId() {
        return this.jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }
	
    public Integer getDays() {
        return this.days;
    }

    public void setDays(Integer days) {
        this.days = days;
    }
	
    public String getEnv() {
        return this.env;
    }

    public void setEnv(String env) {
        this.env = env;
    }
	
    public String getTraceId() {
        return this.traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}