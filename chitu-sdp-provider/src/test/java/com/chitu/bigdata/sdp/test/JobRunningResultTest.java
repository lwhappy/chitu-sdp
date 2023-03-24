package com.chitu.bigdata.sdp.test;

import com.alibaba.fastjson.JSON;
import com.chitu.bigdata.sdp.api.bo.JobRunResultFailDataBO;
import com.chitu.bigdata.sdp.api.bo.PromResponceInfo;
import com.chitu.bigdata.sdp.service.*;
import com.chitu.bigdata.sdp.service.monitor.PrometheusQueryService;
import com.chitu.cloud.web.test.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.client.RestHighLevelClient;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * @author sutao
 * @create 2022-03-02 9:18
 */
@Slf4j
public class JobRunningResultTest extends BaseTest {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private MetaTableConfigService metaTableConfigService;

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private JobService jobService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private JobInstanceService jobInstanceService;

    @Autowired
    private PrometheusQueryService prometheusQueryService;

    @Autowired
    RestHighLevelClient restHighLevelClient;

    @Autowired
    JobRunResultService jobRunResultService;

    @Test
    public void queryFailData() {
        JobRunResultFailDataBO jobRunResultFailData = new JobRunResultFailDataBO();
        jobRunResultFailData.setJobId(943L);
        jobRunResultFailData.setSourceType("kafka");
        jobRunResultFailData.setTableName("test_t2");
        jobRunResultFailData.setPage(1);
        jobRunResultFailData.setPageSize(10);
        jobRunResultService.queryFailData(jobRunResultFailData);
    }


    @Test
    public void queryJobRunningResult() {
        jobRunResultService.queryJobRunningResult(943L);
    }


    @Test
    public void consumedTotal() {
        String promURL = "http://******/api/v1/query?query={promQL}";
        Map<String, String> map = new HashMap<>();
        String promQL = String.format("%s{job_id=\"%s\"}", "flink_taskmanager_job_task_operator_KafkaSourceReader_KafkaConsumer_records_consumed_total", "614f10d64a984a177c805a93ea822c26");
        map.put("promQL", promQL);
        String result = restTemplate.getForObject(promURL, String.class, map);
        System.out.println(result);
        PromResponceInfo responceInfo = JSON.parseObject(result, PromResponceInfo.class);
        log.info("【消费总条数】，请求地址：{}，请求QL：{}，返回信息：{}", promURL, promQL, responceInfo);
    }

}
