

/**
  * <pre>
  * 作   者：LIZHENYONG
  * 创建日期：2022-4-24
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <pre>
 * connector数据集成测试结果实体类
 * 数据库表名称：regression_result
 * </pre>
 * @author 587694
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class RegressionResult extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：数据源类型
     * 
     * 数据库字段信息:data_source_type VARCHAR(255)
     */
    private String dataSourceType;

    /**
     * 字段名称：检测类型（数据一致性，是否丢失）
     * 
     * 数据库字段信息:regression_type VARCHAR(255)
     */
    private String regressionType;

    /**
     * 字段名称：作业类型（普通kafka结构，cannal结构，表关联）
     * 
     * 数据库字段信息:job_type VARCHAR(255)
     */
    private String jobType;

    /**
     * 字段名称：检测对应的表名称
     * 
     * 数据库字段信息:table_name VARCHAR(255)
     */
    private String tableName;

    /**
     * 字段名称：检测的结果
     * 
     * 数据库字段信息:result VARCHAR(255)
     */
    private String result;

    /**
     * 字段名称：错误明细内容
     * 
     * 数据库字段信息:fail_detail LONGTEXT(2147483647)
     */
    private String failDetail;

    /**
     * 字段名称：日志追踪ID
     * 
     * 数据库字段信息:trace_id VARCHAR(128)
     */
    private String traceId;


}