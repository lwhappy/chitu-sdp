
/**
 * <pre>
 * 作   者：CHENYUN
 * 创建日期：2021-10-15
 * </pre>
 */

package com.chitu.bigdata.sdp.api.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.chitu.cloud.model.GenericModel;
import com.xiaoleilu.hutool.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.sql.Timestamp;
/**
 * @author 587694
 * @description: TODO
 * @date 2021/10/12 19:28
 */

/**
 * <pre>
 * 作业实例实体类
 * 数据库表名称：sdp_job_instance
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpJobInstance extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：项目ID
     *
     * 数据库字段信息:project_id BIGINT(19)
     */
    private Long projectId;

    /**
     * 字段名称：作业ID
     *
     * 数据库字段信息:job_id BIGINT(19)
     */
    private Long jobId;

    @TableField(exist = false)
    private Long fileId;

    private String flinkJobId;

    /**
     * 字段名称：实例信息
     *
     * 数据库字段信息:instance_info TEXT(65535)
     */
    private String instanceInfo;

    private String applicationId;

    private String configuration;
    private String jobmanagerAddress;

    /**
     * 字段名称：作业状态
     *
     * 数据库字段信息:job_status TINYINT(3)
     */
    private String jobStatus;

    private String rawStatus;

    private String expectStatus;

    /**
     * 字段名称：作业版本
     *
     * 数据库字段信息:job_version VARCHAR(255)
     */
    private String jobVersion;

    private Boolean isLatest;

    /**
     * 字段名称：Flink_web_rul
     *
     * 数据库字段信息:flink_url VARCHAR(255)
     */
    private String flinkUrl;

    /**
     * 字段名称：作业内容
     *
     * 数据库字段信息:job_content TEXT(65535)
     */
    private String jobContent;

    /**
     * 字段名称：配置内容
     *
     * 数据库字段信息:config_content TEXT(65535)
     */
    private String configContent;

    /**
     * 字段名称：资源内容
     *
     * 数据库字段信息:source_content TEXT(65535)
     */
    private String sourceContent;

    /**
     * 字段名称：dataStream配置
     *
     * 数据库字段信息:data_stream_config TEXT(65535)
     */
    private String dataStreamConfig;

    /**
     * 字段名称：执行耗时
     *
     * 数据库字段信息:execute_duration BIGINT(19)
     */
    private Long executeDuration;

    /**
     * 字段名称：开始时间
     *
     * 数据库字段信息:start_time DATETIME(19)
     */
    private Timestamp startTime;

    /**
     * 字段名称：结束时间
     *
     * 数据库字段信息:end_time DATETIME(19)
     */
    private Timestamp endTime;

    private String signStatus;

    private Long version;

    @TableField(exist = false)
    private String projectCode;

    @TableField(exist = false)
    private String jobName;

    @TableField(exist = false)
    private String businessFlag;

    /**
     * 非数据库字段，是否是当天，1是当天，0是非当天
     */
    @TableField(exist = false)
    private Integer isToday;

    @TableField(exist = false)
    private Long engineId;

    @TableField(exist = false)
    private String engineType;

    public SdpJobInstance() {
    }

    public SdpJobInstance(Long jobId) {
        this.jobId = jobId;
    }

    /**
     * 下面字段有变更的话，需要立即更新到数据库
     *                 project_id = 18,
     *                 job_id = 267,
     *                 flink_job_id = '53b78af6853daf17ee2c8fad594c4394',
     *                 instance_info = '{"duration":922265678,"endTime":-1,"id":"53b78af6853daf17ee2c8fad594c4394","lastModification":1651739738213,"name":"log_collect_analyse_dorisdb_qt_apm_log_stat","startTime":1651739723714,"state":"RUNNING","tasks":{"canceled":0,"canceling":0,"created":0,"deploying":0,"failed":0,"finished":0,"reconciling":0,"running":11,"scheduled":0,"total":11}}',
     *                 application_id = 'application_1645159497110_0973',
     *                 job_status = 'RUNNING',
     *                 raw_status = 'RUNNING',
     *                 expect_status = 'RUNNING',
     *                 flink_url = 'http://szzb-bg-prd-sdp-jn01:9111/proxy/application_1645159497110_0973/',
     *                 execute_duration = 922265678,
     *                 start_time = '2022-5-16 8:46:29',
     *                 enabled_flag = 1,
     *                 sign_status = 'RUNNING'
     * @param that
     * @return
     */
    public boolean isChanged(SdpJobInstance that){
        if (
                StrUtil.equals(rawStatus, that.rawStatus)
                        && StrUtil.equals(jobStatus, that.jobStatus)
                        && StrUtil.equals(expectStatus, that.expectStatus)
                        && StrUtil.equals(signStatus, that.signStatus)
                        && StrUtil.equals(flinkJobId, that.flinkJobId)
                        && StrUtil.equals(applicationId, that.applicationId)
        ) {
            return false;
        } else {
            return true;
        }

    }
}