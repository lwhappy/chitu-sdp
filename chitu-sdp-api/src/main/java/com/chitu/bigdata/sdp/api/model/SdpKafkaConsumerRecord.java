

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-6-7
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.chitu.cloud.model.GenericModel;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * <pre>
 * kafka 消费位置记录表实体类
 * 数据库表名称：sdp_kafka_consumer_record
 * </pre>
 */
public class SdpKafkaConsumerRecord extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：sdp_job.id
     * 
     * 数据库字段信息:job_id BIGINT(19)
     */
    private Long jobId;

    /**
     * 字段名称：主题名称
     * 
     * 数据库字段信息:topic_name VARCHAR(255)
     */
    private String topicName;



    /**
     * 字段名称：消费位置时间戳
     * 
     * 数据库字段信息:ts TIMESTAMP(19)
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss", timezone="GMT+8")
    private Timestamp ts;

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

    public SdpKafkaConsumerRecord() {
    }	
    public Long getJobId() {
        return this.jobId;
    }

    public void setJobId(Long jobId) {
        this.jobId = jobId;
    }
	
    public String getTopicName() {
        return this.topicName;
    }

    public void setTopicName(String topicName) {
        this.topicName = topicName;
    }
	

	
    public Timestamp getTs() {
        return this.ts;
    }

    public void setTs(Timestamp ts) {
        this.ts = ts;
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