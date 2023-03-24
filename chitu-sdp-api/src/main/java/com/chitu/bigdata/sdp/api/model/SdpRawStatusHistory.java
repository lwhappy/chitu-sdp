

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-25
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <pre>
 * 作业状态历史实体类
 * 数据库表名称：sdp_raw_status_history
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpRawStatusHistory extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：作业ID
     * 
     * 数据库字段信息:job_id BIGINT(19)
     */
    private Long jobId;

    /**
     * 字段名称：实例ID
     * 
     * 数据库字段信息:instance_id BIGINT(19)
     */
    private Long instanceId;

    /**
     * 
     * 
     * 数据库字段信息:from_status VARCHAR(255)
     */
    private String fromStatus;

    /**
     * 
     * 
     * 数据库字段信息:to_status VARCHAR(255)
     */
    private String toStatus;

    /**
     * 
     * 
     * 数据库字段信息:trigger VARCHAR(255)
     */
    private String trigger;

    /**
     * 
     * 
     * 数据库字段信息:remark VARCHAR(255)
     */
    private String remark;

    public SdpRawStatusHistory() {
    }	
}