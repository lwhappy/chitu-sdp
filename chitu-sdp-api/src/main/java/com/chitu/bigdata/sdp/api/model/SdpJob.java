

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * <pre>
 * 作业实体类
 * 数据库表名称：sdp_job
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpJob extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：作业名称
     * 
     * 数据库字段信息:job_name VARCHAR(255)
     */
    private String jobName;

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
     * 字段名称：运行版本
     * 
     * 数据库字段信息:running_version VARCHAR(255)
     */
    private String runningVersion;

    /**
     * 字段名称：最新版本
     * 
     * 数据库字段信息:latest_version VARCHAR(255)
     */
    private String latestVersion;

    /**
     * 字段名称：项目ID
     * 
     * 数据库字段信息:project_id BIGINT(19)
     */
    private Long projectId;

    /**
     * 字段名称：文件ID
     * 
     * 数据库字段信息:file_id BIGINT(19)
     */
    private Long fileId;

    /**
     * 字段名称：作业优先级
     *
     * 数据库字段信息:priority VARCHAR(255)
     */
    private Integer priority;

    @TableField(exist = false)
    private String prioritys;


    @TableField(exist = false)
    private List<String> searchParams;


    /**
     * 关联的项目信息
     */
    @TableField(exist = false)
    private SdpProject project;

    /**
     * 关联的实例信息
     */
    @TableField(exist = false)
    private SdpJobInstance jobInstance;

    /**
     * 关联查询的规则信息
     */
    @TableField(exist = false)
    private List<SdpJobAlertRule> jobAlertRuleList;
    //是否使用最新版本恢复
    @TableField(exist = false)
    public Boolean useLatest;
    /**
     * prometheus监控job指标
     */
    @TableField(exist = false)
    public String grafanaUrl;
    /**
     * job运行状态
     */
    @TableField(exist = false)
    public String jobStatus;

    @TableField(exist = false)
    public String newStatus;

    private String businessFlag;
    @TableField(exist = false)
    private String projectName;
    @TableField(exist = false)
    private String employeeNumber;
    @TableField(exist = false)
    private String email;

    /**
     * job 负责人(用于告警模板参数)
     */
    @TableField(exist = false)
    private String owner;

    /**
     * 目录id
     */
    @TableField(exist = false)
    public Long folderId;

    @TableField(exist = false)
    public Boolean updateRule;


    /**
     * 字段名称：cpu数
     *
     */
    @TableField(exist = false)
    private String cpuCore;

    /**
     * 字段名称：内存数
     *
     */
    @TableField(exist = false)
    private String memoryGb;

    /**
     * 字段名称：是否有更新版本
     *
     */
    @TableField(exist = false)
    private Integer isNewVersion;

    /**
     * 希望状态
     */
    @TableField(exist = false)
    private String expectStatus;

    @TableField(exist = false)
    private String flinkUrl;

    @TableField(exist = false)
    private String fileType;

    /**
     * 并行度
     */
    @TableField(exist = false)
    private String parallelism;

    /**
     * 插槽数
     */
    @TableField(exist = false)
    private String slots;

    /**
     * 目录全路径
     */
    @TableField(exist = false)
    private String fullPath;

    @TableField(exist = false)
    private String engineCluster;

    @TableField(exist = false)
    private String engineType;

    @TableField(exist = false)
    private List<Long> folderIds;

    public SdpJob() {
    }
    public SdpJob(Long id) {
        this.id = id;
    }
}