package com.chitu.bigdata.sdp.constant;

/**
 * @author sutao
 * @create 2021-11-10 22:41
 */
public class PromConstant {

    /**
     * prometheus-查询SUCCESS
     */
    public static final String SUCCESS = "success";

    /**
     * prometheus-查询参数
     */
    public static final String QUERY = "query";

    /**
     *  checkpoint完成次数
     */
    public static final String NUMBER_OF_COMPLETED_CHECKPOINTS = "flink_jobmanager_job_numberOfCompletedCheckpoints";

    /**
     *  重启次数
     */
    public static final String NUMRESTARTS = "flink_jobmanager_job_numRestarts";

    /**
     *  Flink1.13.2 自定义Topic消费延迟
     */
    @Deprecated
    public static final String TOPIC_PARTITION_MAXLATENCY = "flink_taskmanager_job_task_operator_KafkaConsumer_topic_partition_maxLatency";

    /**
     *  Flink1.14.0 Kafka Source 自带的Topic消费延迟
     */
    public static final String CURRENT_EMIT_EVENTTIMELAG = "flink_taskmanager_job_task_operator_currentEmitEventTimeLag";


    /**
     * 消费总条数
     */
    public static final String RECORDS_CONSUMED_TOTAL = "flink_taskmanager_job_task_operator_KafkaSourceReader_KafkaConsumer_records_consumed_total";

    /**
     * 解析异常总条数
     */
    public static final String DESERIALIZEFAILNUM = "flink_taskmanager_job_task_operator_deserializeFailNum";

    /**
     * 待消费数
     */
    public static final String PENDINGRECORDS = "flink_taskmanager_job_task_operator_pendingRecords";

    /**
     * 背压
     */
    public static final String BACKPRESSURED = "flink_taskmanager_job_task_backPressuredTimeMsPerSecond";

    /**
     * 写入成功数(kafka通过该方式获取)
     */
    @Deprecated
    public static final String NUMRECORDSOUT = "flink_taskmanager_job_task_numRecordsOut";


    /**
     * 写入kafka成功数
     */
    public static final String KAFKAPRODUCER_RECORD_SEND_TOTAL = "flink_taskmanager_job_task_operator_KafkaProducer_record_send_total";


    /**
     * 写入总数。目前这个是doris的，其他connector的埋点也希望使用该名称，通过promSql请求的时候，只需要请求一遍获取全部的metric回来
     */
    public static final String TOTAL_FLUSH_ROWS = "flink_taskmanager_job_task_operator_totalFlushRows";

    /**
     * 写入失败数。目前这个是doris的，其他connector的埋点也希望使用该名称，通过promSql请求的时候，只需要请求一遍获取全部的metric回来
     */
    public static final String TOTAL_FAILED_ROWS = "flink_taskmanager_job_task_operator_totalFailedRows";

    /**
     * flink_taskmanager_job_task_operator_KafkaSourceReader_topic_partition_committedOffset{job_id="189ca2ea075e75e063f92cf75424b5c9"}
     */
    public static final String TOPIC_PARTITION_COMMITTEDOFFSET = "flink_taskmanager_job_task_operator_KafkaSourceReader_topic_partition_committedOffset";

    /**
     * hbase cdc table延迟
     */
    public static final String HBASE_CDC_TABLE_DELAY = "hbase_cdc_tableDelay";


    /**
     * hbase cdc table 反查为空
     */
    public static final String HBASE_CDC_TABLENULLSIZE = "hbase_cdc_tableNullSize";

    /**
     * 写入hive成功数
     */
    public static final String SINK_HIVE_NUMRECORDSIN = "flink_taskmanager_job_task_operator_numRecordsIn";

    public static final String JOB_LATENCY = "flink_taskmanager_job_latency_source_id_operator_id_operator_subtask_index_latency";

}
