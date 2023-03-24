package com.chitu.bigdata.sdp.job;

import com.chitu.bigdata.sdp.api.enums.AlertIndex;
import com.chitu.bigdata.sdp.api.model.SdpJob;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.constant.SdpSysConfigConstant;
import com.chitu.bigdata.sdp.interceptor.EnvHolder;
import com.chitu.bigdata.sdp.service.JobService;
import com.chitu.bigdata.sdp.service.SdpSysConfigService;
import com.chitu.bigdata.sdp.service.monitor.RuleIndexMonitorFactory;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.chitu.cloud.config.scheduler.RedisLeaderSchedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * @author sutao
 * @create 2021-11-15 14:09
 */
@Component
@Slf4j
public class MonitorAlarmJob {


    @Autowired
    JobService jobService;

    @Autowired
    RuleIndexMonitorFactory ruleIndexFactory;

    @Autowired
    private Executor monitorExecutor;

    @Autowired
    SdpSysConfigService sdpSysConfigService;
    @Autowired
    SdpConfig sdpConfig;


    @RedisLeaderSchedule
    @Scheduled(fixedDelayString = "${custom.time.fixedDelay}")
    public void ruleMonitor() {
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
        Boolean globalSwitch = sdpSysConfigService.getValByKey(SdpSysConfigConstant.ALERT_GLOBAL_SWITCH, Boolean.class);
        globalSwitch = Optional.ofNullable(globalSwitch).orElse(true);
        if(!globalSwitch){
            log.info("job规则监控-全局关闭");
            return;
        }

        List<SdpJob> runningJobs = jobService.getMapper().getRunningStatusJob();
        //过滤状态异常检测规则
        runningJobs.forEach(runningJob -> {
            runningJob.setJobAlertRuleList(runningJob.getJobAlertRuleList().stream().filter(rule -> !AlertIndex.INTERRUPT_OPERATION.name().equals(rule.getIndexName())).collect(Collectors.toList()));
        });
        int jobRuleSize = runningJobs.stream().mapToInt(item -> item.getJobAlertRuleList().size()).sum();
        log.info("【job规则监控开始】，当前运行总任务数[{}]，总规则数[{}]", runningJobs.size(), jobRuleSize);
        CountDownLatch countDownLatch = new CountDownLatch(jobRuleSize);
        long starTime = System.currentTimeMillis();
        runningJobs.stream().forEach(job -> {
            job.getJobAlertRuleList().stream().forEach(jobAlertRule -> {
                CompletableFuture.runAsync(() -> {
                    ruleIndexFactory.getRuleIndexMonitor(jobAlertRule.getIndexName()).monitor(job, jobAlertRule);
                }, monitorExecutor).whenComplete((v, t) -> {
                    countDownLatch.countDown();
                }).exceptionally((t) -> {
                    // TODO 需要告警
                    log.error("执行规则监控异常", t);
                    return null;
                });
            });
        });
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        long endTime = System.currentTimeMillis();
        log.info("【job规则监控结束】，总耗时[{}]", (endTime - starTime));
    }

}
