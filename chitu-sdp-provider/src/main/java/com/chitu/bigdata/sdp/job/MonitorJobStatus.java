package com.chitu.bigdata.sdp.job;

import com.chitu.bigdata.sdp.api.enums.AlertIndex;
import com.chitu.bigdata.sdp.api.model.SdpJobAlertRule;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.interceptor.EnvHolder;
import com.chitu.bigdata.sdp.mapper.SdpJobAlertRuleMapper;
import com.chitu.bigdata.sdp.service.monitor.JobStatusNotifyService;
import com.chitu.bigdata.sdp.service.monitor.RuleIndexMonitorFactory;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.chitu.cloud.config.scheduler.RedisLeaderSchedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;

/**
 * @author 587694
 * @create 2021-1-19 14:09
 */
@Component
@Slf4j
public class MonitorJobStatus {
    @Autowired
    private SdpJobAlertRuleMapper sdpJobAlertRuleMapper;

    @Autowired
    private JobStatusNotifyService jobStatusNotifyService;

    @Autowired
    private Executor jobStatusExecutor;

    @Autowired
    private RuleIndexMonitorFactory ruleIndexFactory;
    @Autowired
    SdpConfig sdpConfig;

    @RedisLeaderSchedule
    @Scheduled(fixedDelayString = "${custom.monitorJobStatus.fixedDelay}")
    public void jobStatusMonitor() {
        List<String> envList = sdpConfig.getEnvList();
        for (String env : envList) {
            EnvHolder.addEnv(env);
            try {
                handleSingleEnv();
            } catch (Exception e) {
                String errMsg = StrUtils.parse1("【job环境env】: {}", env);
                log.warn(errMsg,e);
            }finally {
                EnvHolder.clearEnv();
            }
        }
    }

    private void handleSingleEnv() {
        List<SdpJobAlertRule> jobAlertRuleList = sdpJobAlertRuleMapper.getRuleByIndexName(AlertIndex.INTERRUPT_OPERATION.name());
        int ruleListSize = jobAlertRuleList.size();
        log.info("【job状态监控开始】，当前运行总任务数[{}]", ruleListSize);
        long starTime = System.currentTimeMillis();
        JobStatusNotifyService ruleIndexMonitor = (JobStatusNotifyService)ruleIndexFactory.getRuleIndexMonitor(AlertIndex.INTERRUPT_OPERATION.name());
        CountDownLatch countDownLatch = new CountDownLatch(ruleListSize);
        jobAlertRuleList.forEach(rule->{
            CompletableFuture.runAsync(() -> {
                ruleIndexMonitor.monitorJobStatus(rule);
            },jobStatusExecutor).whenComplete((v,t)->{
                countDownLatch.countDown();
            }).exceptionally((t)->{
                // TODO 需要告警
                log.error("job状态监控异常", t);
                return null;
            });
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        log.info("【job状态监控结束】，总耗时[{}]", (endTime - starTime));
    }
}
