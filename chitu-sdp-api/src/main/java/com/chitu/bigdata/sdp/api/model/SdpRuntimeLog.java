

/**
 * <pre>
 * 作   者：CHENYUN
 * 创建日期：2021-11-16
 * </pre>
 */

package com.chitu.bigdata.sdp.api.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.chitu.bigdata.sdp.api.enums.JobAction;
import com.chitu.bigdata.sdp.api.enums.JobRunStep;
import com.chitu.bigdata.sdp.api.enums.OperationStage;
import com.chitu.cloud.model.GenericModel;
import com.chitu.cloud.utils.StringUtils;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.xiaoleilu.hutool.util.StrUtil;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * <pre>
 * 运行日志实体类
 * 数据库表名称：sdp_runtime_log
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpRuntimeLog extends GenericModel<Long> implements Serializable {
    public static final String LOG_TYPE = "作业%s中...";
    public static final String YARN_URL = "《查看YARN日志》【%s】";
    public static final String K8S_URL = "《查看Kubernetes日志》【%s】";
    public static final String FLINK_URL = "《查看Flink运行日志》【%s】";
    public static final String FLINK_HISTORY = "《查看Flink历史日志》【%s】";
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：作业id
     *
     * 数据库字段信息:job_id BIGINT(19)
     */
    private Long jobId;

    /**
     * 字段名称：作业实例id
     *
     * 数据库字段信息:job_instance_id BIGINT(19)
     */
    private Long jobInstanceId;

    /**
     * 字段名称：运行操作
     *
     * 数据库字段信息:operation VARCHAR(255)
     */
    private String operation;

    /**
     * 字段名称：运行步骤
     *
     * 数据库字段信息:step_action VARCHAR(255)
     */
    private String stepAction;

    /**
     * 字段名称：运行状态(PENDING/SUCCEED/FAILED)
     *
     * 数据库字段信息:operation_stage VARCHAR(255)
     */
    private String operationStage;

    /**
     * 字段名称：yarn日志链接
     *
     * 数据库字段信息:yarn_log_url VARCHAR(255)
     */
    private String yarnLogUrl;

    /**
     * 字段名称：k8s日志链接
     *
     * 数据库字段信息:k8s_log_url VARCHAR(255)
     */
    private String k8sLogUrl;

    /**
     * 字段名称：flink日志链接
     *
     * 数据库字段信息:flink_log_url VARCHAR(255)
     */
    private String flinkLogUrl;

    /**
     * 字段名称：堆栈信息
     *
     * 数据库字段信息:stack_trace TEXT(65535)
     */
    private String stackTrace;

    @TableField(exist = false)
    private String engineCluster;
    /**
     * 字段名称：操作时间格式为“yyyy-MM-dd HH:mm:ss.SSS”
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS",timezone = "GMT+8")
    private Timestamp operationTime;

    public String toString(String prodFlinkProxy,String env) {
        StringBuffer result = new StringBuffer();
        Timestamp creationTime = this.operationTime;
        result.append(creationTime).append("  ");
        String format = null;
        if (Objects.isNull(this.operationStage)){
            format = String.format(LOG_TYPE, JobRunStep.getStepType(this.stepAction));
            result.append(format);
        }else{
            result.append(JobRunStep.getStepType(this.stepAction)).append(OperationStage.getStepType(this.operationStage));
            if (Objects.nonNull(this.stackTrace)){
                result.append("\r\n").append(creationTime).append("  ").append(this.stackTrace);
            }
            if (Objects.nonNull(this.yarnLogUrl)){
                format = String.format(YARN_URL, this.yarnLogUrl);
                result.append("\r\n").append(creationTime).append("  ").append(format);
            }
            if (Objects.nonNull(this.k8sLogUrl)){
                format = String.format(K8S_URL, this.k8sLogUrl);
                result.append("\r\n").append(creationTime).append("  ").append(format);
            }
            if (StrUtil.isNotEmpty(this.flinkLogUrl)){
                if (JobAction.PAUSE.name().equalsIgnoreCase(operation) ||JobAction.STOP.name().equalsIgnoreCase(operation)){
                    format = String.format(FLINK_HISTORY, this.flinkLogUrl);
                }else {
                    //将生产环境的flink_url替换为代理地址
                    String myFlinkLogUrl = this.flinkLogUrl;
                    if(env.equals("prod") && StringUtils.isNotEmpty(prodFlinkProxy)){
                        String[] urlSplit = flinkLogUrl.split("proxy");
                        if(urlSplit.length >= 2){
                            String jobApplicationId = this.flinkLogUrl.split("proxy")[1];
                            myFlinkLogUrl = prodFlinkProxy + jobApplicationId;
                        }
                    }
                    format = String.format(FLINK_URL, myFlinkLogUrl);
                }
                result.append("\r\n").append(creationTime).append("  ").append(format);
            }
        }

        return result.toString();
    }

}