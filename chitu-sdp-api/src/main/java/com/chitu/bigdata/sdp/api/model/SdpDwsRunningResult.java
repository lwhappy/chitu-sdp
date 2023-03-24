

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-5-9
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.chitu.cloud.model.GenericModel;
import lombok.Data;

import java.io.Serializable;

/**
 * <pre>
 * 运行结果统计表实体类
 * 数据库表名称：sdp_dws_running_result
 * </pre>
 */
@Data
public class SdpDwsRunningResult extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：flink job id
     * 
     * 数据库字段信息:flink_job_id VARCHAR(255)
     */
    private String flinkJobId;

    /**
     * 字段名称：数据源类型
     * 
     * 数据库字段信息:source_type VARCHAR(50)
     */
    private String sourceType;

    /**
     * 字段名称：0失败 1成功
     * 
     * 数据库字段信息:type TINYINT(3)
     */
    private Integer type;

    /**
     * 字段名称：成功数或者失败数
     * 
     * 数据库字段信息:num BIGINT(19)
     */
    private Long num;

    @TableField(exist = false)
    private Long failedNum;

    @TableField(exist = false)
    private Long successNum;

    /**
     * 字段名称：日志追踪ID
     * 
     * 数据库字段信息:trace_id VARCHAR(128)
     */
    private String traceId;
    private String databaseTableName;

}