package com.chitu.bigdata.sdp.api.bo;

import lombok.Data;

/**
 * @author sutao
 * @create 2021-11-10 22:45
 */
@Data
public class PromMetricInfo {

    private String __name__;
    private String exported_job;
    private String host;
    private String instance;
    private String job;
    private String job_id;
    private String job_name;

    private String operator_id;
    private String operator_name;
    private String partition;
    private String subtask_index;
    private String task_attempt_id;
    private String task_attempt_num;
    private String task_id;
    private String task_name;
    private String tm_id;
    private String topic;
    private String source_id;


    /**
     * hbase cdc 自定义key
     */
    private String hbase_table;
    private String cluster;

}
