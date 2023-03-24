package com.chitu.bigdata.sdp.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chitu.bigdata.sdp.api.bo.PromResponceInfo;
import com.chitu.bigdata.sdp.api.bo.PromResultInfo;
import com.chitu.bigdata.sdp.api.domain.ConnectInfo;
import com.chitu.bigdata.sdp.api.enums.MetaTableType;
import com.chitu.bigdata.sdp.config.CustomMonitorConfigProperties;
import com.chitu.bigdata.sdp.constant.PromConstant;
import com.chitu.bigdata.sdp.service.datasource.KafkaDataSource;
import com.chitu.bigdata.sdp.service.monitor.PrometheusQueryService;
import com.chitu.bigdata.sdp.utils.DataMonitorUtils;
import com.chitu.cloud.web.test.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author sutao
 * @create 2021-11-10 22:13
 */
@Slf4j
public class PrometheusTest extends BaseTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private PrometheusQueryService prometheusQueryService;

    @Autowired
    KafkaDataSource kafkaDataSource;

    @Test
    public void numberOfCompletedCheckpoints() {
        String promURL = "http://******/api/v1/query?query={promQL}";
        Map<String, String> map = new HashMap<>();
        //delta(flink_jobmanager_job_numberOfCompletedCheckpoints{job_id="0bee2904c3e877824dd3219a341b560b"}[1m])
        String promQL = String.format("delta(%s{job_id=\"%s\"}[%s])", PromConstant.NUMBER_OF_COMPLETED_CHECKPOINTS, "6dea2686d2f0b0bce334bc7096174ad2", "1m");
        map.put("promQL", promQL);
        String result = restTemplate.getForObject(promURL, String.class, map);
        System.out.println(result);
        PromResponceInfo responceInfo = JSON.parseObject(result, PromResponceInfo.class);
        log.info("【checkpoint完成次数】，请求地址：{}，请求QL：{}，返回信息：{}", promURL, promQL, responceInfo);
    }


    @Test
    public void numRestarts() {
        String promURL = "http://******/api/v1/query?query={promQL}";
        Map<String, String> map = new HashMap<>();
        //delta(flink_jobmanager_job_numRestarts{job_id="0bee2904c3e877824dd3219a341b560b"}[1m])
        String promQL = String.format("delta(%s{job_id=\"%s\"}[%s])", PromConstant.NUMRESTARTS, "04af38233988bfb0e58ab0ce77128731", "1m");
        map.put("promQL", promQL);
        String result = restTemplate.getForObject(promURL, String.class, map);
        PromResponceInfo responceInfo = JSON.parseObject(result, PromResponceInfo.class);
        log.info("【重启次数】，请求地址：{}，请求QL：{}，返回信息：{}", promURL, promQL, responceInfo);
    }


    @Test
    public void latency() {

        String promURL = "http://******/api/v1/query?query={promQL}";
        Map<String, String> map = new HashMap<>();
        //flink_taskmanager_job_task_operator_currentEmitEventTimeLag{job_id="70675ddfcea67648196c34501a55850b"}
        String promQL = "flink_taskmanager_job_task_operator_KafkaConsumer_topic_partition_maxLatency{job_id=\"0b1d495457ee245c90d3603d8a2724e7\"}";
        promQL = "flink_taskmanager_job_task_operator_currentEmitEventTimeLag{job_id=\"0bee2904c3e877824dd3219a341b560b\"}";
        map.put("promQL", promQL);
        String result = restTemplate.getForObject(promURL, String.class, map);
        System.out.println(result);
        PromResponceInfo responceInfo = JSON.parseObject(result, PromResponceInfo.class);
        log.info("【Topic消费延迟】，请求地址：{}，请求QL：{}，返回信息：{}", promURL, promQL, responceInfo);
    }


    @Test
    public void deserializefailNum() {
        //最近N小时消费失败数
        String promQL = String.format("delta(%s{job_id=\"%s\"}[%s])", PromConstant.DESERIALIZEFAILNUM, "a45b8bf2098b6013298a38590d334425","60m");
        PromResponceInfo promResponceInfo = prometheusQueryService.metricsQuery(promQL);
        log.info("promResponceInfo：{}", JSONObject.toJSONString(promResponceInfo));
    }

    @Test
    public void pendingRecords() {
        String promQL = String.format("%s{job_id=\"%s\"}", PromConstant.PENDINGRECORDS, "a45b8bf2098b6013298a38590d334425");
        PromResponceInfo promResponceInfo = prometheusQueryService.metricsQuery(promQL);
        log.info("promResponceInfo：{}", JSONObject.toJSONString(promResponceInfo));
    }

    @Test
    public void latelyNHRecordsConsumedTotal() {
        //最近N小时消费数据量
        String promQL = String.format("delta(%s{job_id=\"%s\"}[%s])", PromConstant.RECORDS_CONSUMED_TOTAL, "5d6c3d7b44b70af9eb1f487fbb31998a", "60m");
        PromResponceInfo promResponceInfo = prometheusQueryService.metricsQuery(promQL);
        log.info("promResponceInfo：{}", JSONObject.toJSONString(promResponceInfo));
    }

    @Test
    public void backPressured() {
        //最近10分钟背压均值
        String promQL = String.format("avg_over_time(%s{job_id=\"%s\"}[%s])", PromConstant.BACKPRESSURED, "f7ca817cd92e5b0ec4f51ed874dbc919", "10m");
        PromResponceInfo promResponceInfo = prometheusQueryService.metricsQuery(promQL);
        log.info("promResponceInfo：{}", JSONObject.toJSONString(promResponceInfo));
    }

    @Test
    public void getPromethosData(){
        String prex = "http://******/api/v1/query?query={promQL}";
        String promQL = "flink_taskmanager_job_task_operator_KafkaSourceReader_KafkaConsumer_records_consumed_total";
        Map<String, String> map = new HashMap<>(3);
        map.put("promQL", promQL);
        PromResponceInfo responceInfo = null;
        try {
            String result = restTemplate.getForObject(prex, String.class, map);
            responceInfo = JSON.parseObject(result, PromResponceInfo.class);
            if (log.isDebugEnabled()) {
                log.debug("请求QL：{}，返回信息：{}", promQL, JSONObject.toJSONString(responceInfo));
            }
        } catch (RestClientException e) {
            DataMonitorUtils.monitorError("监控告警规则调用Prometheus接口异常");
            log.error(String.format("请求QL：%s 调用Prometheus接口异常", promQL), e);
        }
        List<PromResultInfo> result = responceInfo.getData().getResult();
        for (PromResultInfo promResultInfo : result) {
            System.out.println(JSONObject.toJSONString(promResultInfo));
        }

    }

    @Test
    public void testTopicPartitionCurrentoffset() {
        String promURL = "http://******/api/v1/query?query={promQL}";
        Map<String, String> map = new HashMap<>();
        String promQL = PromConstant.TOPIC_PARTITION_COMMITTEDOFFSET + "{job_id=\"9f693949d9990c2e478156a6e8430b47\"}";
        map.put("promQL", promQL);
        String result = restTemplate.getForObject(promURL, String.class, map);
        System.out.println(result);
        PromResponceInfo responceInfo = JSON.parseObject(result, PromResponceInfo.class);
        log.info("【当前kafkaoffset】，请求地址：{}，请求QL：{}，返回信息：{}", promURL, promQL, responceInfo);

        ConnectInfo connectInfo = new ConnectInfo();
        connectInfo.setAddress("szzb-bg-uat-etl-10:9092,szzb-bg-uat-etl-11:9092,szzb-bg-uat-etl-12:9092");
        //kafkaDataSource.partitionTimestampMap(connectInfo,"xxxx","test_ts_001");
    }


    @Autowired
    CustomMonitorConfigProperties customMonitorConfig;

    @Test
    public void hbase_cdc_tableDelay() {

        System.out.println(JSONObject.toJSONString(customMonitorConfig));
        String promQL = PromConstant.HBASE_CDC_TABLE_DELAY;
        List<PromResultInfo> metrics = prometheusQueryService.getMetrics(promQL);
        log.info("promResponceInfo：{}", JSONObject.toJSONString(metrics));

    }


    @Test
    public void hbase_cdc_tableNullSize() {

        List<String> lableValues = prometheusQueryService.getLableValues("label/hbase_table/values");
        System.out.println(lableValues);

    }

    @Test
    public void numrecordsout() {

        //1103509ea9aff20b806a8d00b59680f3
        //4d06f987cbc5e51b2805767454acc84e
        String promQL = String.format("%s{job_id=\"%s\"}", PromConstant.KAFKAPRODUCER_RECORD_SEND_TOTAL, "a351fcc5a3536e70c117a6dddd565efb");
        Map<String, Long> metricTableValueSum = prometheusQueryService.getMetricTableValueSum(promQL, MetaTableType.SINK.getType());
        System.out.println(metricTableValueSum);

    }




    @Test
    public void testSub() {


        String promQL = "flink_taskmanager_job_task_numRecordsOut{job_id=\"6880ed2b33f2260e0094490472f15bc2\"}";
        //Map<String, Long> metricTableValueSum = prometheusQueryService.getMetricTableValueSum(promQL);
        Map<String, Long> metricTableValueSum1 = prometheusQueryService.getMetricTableValueSum(promQL, MetaTableType.SINK.getType());


        String taskName = "Source:_KafkaSource_default_catalog_default_database_source_sdp_flink_log____MiniBatchAssigner_interval__10000ms___mode__ProcTime______Calc_select__JSON_VALUE_message___UTF_16LE___flinkJobId___AS_flink_job_id__JSON_VALUE_message___UTF_16LE___type___AS__f1__JSON_VALUE_message___UTF_16LE___sourceType___AS_source_type__JSON_VALUE_message___UTF_16LE___databaseTableName___AS_database_table_name__CAST_JSON_VALUE_message___UTF_16LE___num____AS__f4___where____level____UTF_16LE_INFO_:VARCHAR_2147483647__CHARACTER_SET__UTF_16LE___AND_JSON_EXISTS_message___UTF_16LE___sdpRunningResultLogFlag___AND_JSON_VALUE_message___UTF_16LE___flinkJobId___IS_NOT_NULL_AND_JSON_VALUE_message___UTF_16LE___databaseTableName___IS_NOT_NULL_______LocalGroupAggregate_groupBy__flink_job_id___f1__source_type__database_table_name___select__flink_job_id___f1__source_type__database_table_name__SUM__f4__AS_sum_0__";
        String opName = taskName.split("____")[0];
        String tableName = opName.substring(opName.lastIndexOf("database_") + "database_".length());
        System.out.println(tableName);

        taskName = "Source:_source_sdp_flink_log_1_____MiniBatchAssigner_2_____Calc_3_____LocalGroupAggregate_4_";
        opName = taskName.split("_1")[0];
        tableName = opName.substring(opName.lastIndexOf("Source:_") + "Source:_".length());

        System.out.println(tableName);
    }




}
