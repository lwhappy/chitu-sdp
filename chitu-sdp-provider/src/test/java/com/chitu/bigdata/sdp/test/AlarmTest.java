package com.chitu.bigdata.sdp.test;

import com.chitu.bigdata.sdp.api.enums.AlertIndex;
import com.chitu.bigdata.sdp.api.model.SdpJob;
import com.chitu.bigdata.sdp.config.FlinkConfigProperties;
import com.chitu.bigdata.sdp.flink.common.util.HdfsUtils;
import com.chitu.bigdata.sdp.service.JobService;
import com.chitu.bigdata.sdp.service.UserService;
import com.chitu.bigdata.sdp.service.monitor.RuleIndexMonitorFactory;
import com.chitu.cloud.web.test.BaseTest;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * @author sutao
 * @create 2021-11-12 14:31
 */
@Slf4j
public class AlarmTest extends BaseTest {


    @Value("${alert.ks.templateCode}")
    private Long ksTemplateCode;

    @Value("${alert.sms.templateCode}")
    private String smsTemplateCode;

    @Value("${alert.call.appId}")
    private String callAppId;

    @Value("${alert.call.templateCode}")
    private String callTemplateCode;

    @Autowired
    UserService userService;

    @Autowired
    JobService jobService;

    @Autowired
    RuleIndexMonitorFactory ruleIndexFactory;

    @Autowired
    RedisTemplate redisTmplate;

    @Autowired
    private Executor monitorExecutor;

    @Autowired
    FlinkConfigProperties flinkConfigProperties;

    @Test
    public void testHdfs() {
        System.setProperty("HADOOP_USER_NAME", "admin");
        String checkPointPrefix = "hdfs://bigbigworld/sdp/ck/" + "659ef93b130cfdf2180664f178d7740c/";
        if (!HdfsUtils.exists(checkPointPrefix)) {
            System.out.println("文件不存在");
        }
        String checkPointPath = HdfsUtils.getCheckPointPath(checkPointPrefix);
        System.out.println(checkPointPrefix + checkPointPath);
        HdfsUtils.delete(checkPointPrefix);
    }





    @Test
    public void ruleMonitorAlarmAsync() throws InterruptedException {
        List<SdpJob> runningJobs = jobService.getMapper().getRunningStatusJob();
        //runningJobs = runningJobs.stream().filter(item -> item.getJobName().equals("kafka_sink_mysql")).collect(Collectors.toList());
        int jobRuleSize = runningJobs.stream().mapToInt(item -> item.getJobAlertRuleList().size()).sum();
        CountDownLatch countDownLatch = new CountDownLatch(jobRuleSize);
        long starTime = System.currentTimeMillis();
        runningJobs.stream().forEach(job -> {
            job.getJobAlertRuleList().stream().filter(x->!AlertIndex.INTERRUPT_OPERATION.equals(x.getIndexName())).forEach(jobAlertRule -> {
                CompletableFuture.runAsync(() -> {
                    ruleIndexFactory.getRuleIndexMonitor(jobAlertRule.getIndexName()).monitor(job, jobAlertRule);
                }, monitorExecutor).whenComplete((v, t) -> {
                    countDownLatch.countDown();
                }).exceptionally((t) -> {
                    log.error("执行规则监控异常", t);
                    return null;
                });
            });
        });
        countDownLatch.await();
        long endTime = System.currentTimeMillis();
        System.out.println("总耗时:" + (endTime - starTime));
    }




    @Test
    public void sms() {
        List<String> mobiles = new ArrayList<>();
        mobiles.add("13434799005");
        mobiles.add("19146468757");

        Map<String, String> templateParamMap = new HashMap<>();
        templateParamMap.put("projectName", "测试项目");
        templateParamMap.put("jobName", "test_job");
        templateParamMap.put("ruleName", "一分钟重启次数大于3次");
        templateParamMap.put("triggerTime", "2021-11-12 14:00:00");

//        smsNotifyService.sendMsg(mobiles, smsTemplateCode, templateParamMap);
    }


    @Test
    public void phone() {
        List<String> mobiles = new ArrayList<>();
        mobiles.add("13434799005");
        mobiles.add("19146468757");

        Map<String, String> templateParamMap = new HashMap<>();
        templateParamMap.put("projectName", "测试项目");
        templateParamMap.put("jobName", "test_job");
        templateParamMap.put("ruleName", "一分钟重启次数大于3次");
        templateParamMap.put("triggerTime", "2021-11-12 14:00:00");

//        phoneAlarmService.sendMsg(mobiles, callAppId, callTemplateCode, templateParamMap);
    }


    @Test
    public void ks() {

        List<String> mobiles = new ArrayList<>();
        mobiles.add("630630");
        mobiles.add("631716");
        mobiles.add("631723");
        mobiles.add("632743");

        Map<String, String> templateParamMap = new HashMap<>();
        templateParamMap.put("projectName", "测试项目");
        templateParamMap.put("jobName", "test_job");
        templateParamMap.put("ruleName", "一分钟重启次数大于3次");
        templateParamMap.put("triggerTime", "2021-11-12 14:00:00");

//        kSAlarmService.sendMsg(mobiles, ksTemplateCode, templateParamMap);
    }
}
