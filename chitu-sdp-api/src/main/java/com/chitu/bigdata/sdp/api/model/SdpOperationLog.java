

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

/**
 * <pre>
 * 操作日志实体类
 * 数据库表名称：sdp_operation_log
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpOperationLog extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：实例ID
     * 
     * 数据库字段信息:instance_id BIGINT(19)
     */
    private Long instanceId;

    /**
     * 
     * 
     * 数据库字段信息:from_expect_status VARCHAR(255)
     */
    private String fromExpectStatus;

    /**
     * 
     * 
     * 数据库字段信息:to_expect_status VARCHAR(255)
     */
    private String toExpectStatus;

    /**
     * 
     * 
     * 数据库字段信息:status VARCHAR(255)
     */
    private String status;

    /**
     * 
     * 
     * 数据库字段信息:action VARCHAR(255)
     */
    private String action;

    /**
     * 
     * 
     * 数据库字段信息:message TEXT(65535)
     */
    private String message;

    /**
     * 
     * 
     * 数据库字段信息:flink_log LONGTEXT(2147483647)
     */
    private String flinkLog;

    public SdpOperationLog() {
    }	
}