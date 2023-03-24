

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-11-8
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * <pre>
 * job告警记录实体类
 * 数据库表名称：sdp_job_alert_record
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpJobAlertRecord extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：作业ID
     * 
     * 数据库字段信息:job_id BIGINT(19)
     */
    private Long jobId;

    /**
     * 字段名称：规则ID
     * 
     * 数据库字段信息:rule_id BIGINT(19)
     */
    private Long ruleId;

    /**
     * 字段名称：告警时间
     * 
     * 数据库字段信息:alert_time TIMESTAMP(19)
     */
    private Timestamp alertTime;
    /**
     * 字段名称：告警次数（告警后没有手动修复的连续告警次数）
     *
     * 数据库字段信息:alert_content VARCHAR(255)
     */
    private Integer alertNum;
    /**
     * 字段名称：告警内容
     *
     * 数据库字段信息:alert_num BIGINT(19)
     */
    private String alertContent;

    /**
     * 字段名称：日志跟踪ID
     * 
     * 数据库字段信息:trace_id VARCHAR(128)
     */
    private String traceId;

    public SdpJobAlertRecord() {
    }

    public SdpJobAlertRecord(Long jobId, Long ruleId) {
        this.jobId = jobId;
        this.ruleId = ruleId;
    }
}