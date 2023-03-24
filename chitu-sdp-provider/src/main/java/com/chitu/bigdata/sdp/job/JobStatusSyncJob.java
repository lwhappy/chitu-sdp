package com.chitu.bigdata.sdp.job;

import com.chitu.bigdata.sdp.api.enums.EnvironmentEnum;
import com.chitu.bigdata.sdp.api.model.SdpEngine;
import com.chitu.bigdata.sdp.api.model.SdpJobInstance;
import com.chitu.bigdata.sdp.api.model.SdpProject;
import com.chitu.bigdata.sdp.config.CustomMonitorConfigProperties;
import com.chitu.bigdata.sdp.config.KubernetesClientConfig;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.constant.CommonConstant;
import com.chitu.bigdata.sdp.interceptor.EnvHolder;
import com.chitu.bigdata.sdp.mapper.SdpEngineMapper;
import com.chitu.bigdata.sdp.mapper.SdpJobInstanceMapper;
import com.chitu.bigdata.sdp.mapper.SdpProjectMapper;
import com.chitu.bigdata.sdp.mapper.SdpRawStatusHistoryMapper;
import com.chitu.bigdata.sdp.service.EngineService;
import com.chitu.bigdata.sdp.service.JobService;
import com.chitu.bigdata.sdp.service.RedisTemplateService;
import com.chitu.bigdata.sdp.service.SdpRuntimeLogService;
import com.chitu.bigdata.sdp.service.monitor.JobStatusNotifyService;
import com.chitu.bigdata.sdp.utils.DataMonitorUtils;
import com.chitu.bigdata.sdp.utils.RedisLocker;
import com.chitu.cloud.config.scheduler.RedisLeaderSchedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;
import redis.clients.jedis.Jedis;

import java.util.*;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/21 20:06
 */
@Slf4j
@Component
public class JobStatusSyncJob {
    @Autowired
    private JobStatusNotifyService jobStatusNotifyService;
    @Autowired
    private SdpJobInstanceMapper instanceMapper;
    @Autowired
    private Executor jobSyncExecutor4Uat;
    @Autowired
    private Executor jobSyncExecutor4Prod;
    @Autowired
    private SdpRawStatusHistoryMapper historyMapper;
    @Autowired
    private JobService jobService;
    @Autowired
    private RedisLocker redisLocker;
    @Autowired
    RedisTemplateService redisTemplateService;
    @Autowired
//    private KubernetesClient kubernetesClient;
    private KubernetesClientConfig kubernetesClientConfig;
    @Autowired
    private SdpEngineMapper sdpEngineMapper;
    @Autowired
    private SdpProjectMapper sdpProjectMapper;
    @Autowired
    private EngineService engineService;
    @Autowired
    @Qualifier("restfulTemplate")
    private RestTemplate restTemplate;
    @Autowired
    private SdpRuntimeLogService sdpRuntimeLogService;
    /**
     * 一天内没有操作
     */
    private final static String NO_OPS_IN_ONE_DAY = "noOpsInOneDay";

    @Autowired
    SdpConfig sdpConfig;
    @Autowired
    CustomMonitorConfigProperties customMonitorConfigProperties;

    @RedisLeaderSchedule
    @Scheduled(fixedDelayString = "${custom.envMap.prod.jobStatusSyncJob.fixedDelay:5000}")
    public void syncJobInstance4Prod(){
        String osName = System.getProperties().getProperty("os.name");
        if(osName.equals("Windows 10")){
            return;
        }
        //prod任务同步
        String env = EnvironmentEnum.PROD.getCode();
        EnvHolder.addEnv(env);
        try {
            handleSingleEnv(env);
        }finally {
            EnvHolder.clearEnv();
        }

    }

    @RedisLeaderSchedule
    @Scheduled(fixedDelayString = "${custom.envMap.uat.jobStatusSyncJob.fixedDelay:5000}")
    public void syncJobInstance4Uat(){
        String osName = System.getProperties().getProperty("os.name");
        if (osName.equals("Windows 10")) {
            return;
        }
        //uat任务同步
        String env = EnvironmentEnum.UAT.getCode();
        EnvHolder.addEnv(env);
        try {
            handleSingleEnv(env);
        }finally {
            EnvHolder.clearEnv();
        }
    }

    private void handleSingleEnv(String env) {
        CustomMonitorConfigProperties.JobStatusSyncJobConfig jobStatusSyncJob = customMonitorConfigProperties.getCustomConfigAndEnv().getCustomConfig().getJobStatusSyncJob();
        Jedis jedis = null;
        long start = System.currentTimeMillis();
        List<SdpJobInstance> list = null;
        try {
            jedis = redisLocker.getJedis();
            jedis.set(CommonConstant.SYNC_JOB_INSTANCE + env, String.valueOf(System.currentTimeMillis()));
            SdpJobInstance param1 = new SdpJobInstance();
            param1.setIsToday(1);
            list = instanceMapper.queryInstance4Sync(param1);

            Long noOpsCount = redisTemplateService.countWithTtl(NO_OPS_IN_ONE_DAY, env, 2, TimeUnit.DAYS);
            Integer queryFrequency = Optional.ofNullable(jobStatusSyncJob).map(m -> m.getQueryFrequency()).orElse(12);
            if(noOpsCount % queryFrequency == 0){
                SdpJobInstance param2 = new SdpJobInstance();
                param2.setIsToday(0);
                List<SdpJobInstance> noOpsList = instanceMapper.queryInstance4Sync(param2);
                if (!CollectionUtils.isEmpty(noOpsList)) {
                    list.addAll(noOpsList);
                }
            }

            if (!CollectionUtils.isEmpty(list)) {
                CountDownLatch downLatch = new CountDownLatch(list.size());
                // 获取engineType,ProjectCode
                Set<Long> engineSet = new HashSet<>();
                Set<Long> projectSet = new HashSet<>();
                list.forEach(item -> {engineSet.add(item.getEngineId());projectSet.add(item.getProjectId());});
                List<SdpEngine> engines = sdpEngineMapper.queryEngineTypeByIds(engineSet);
                List<SdpProject> projects = sdpProjectMapper.getProjectCodeByIds(projectSet);
                Map<Long, String> engineMap = engines.stream().collect(Collectors.toMap(SdpEngine::getId, SdpEngine::getEngineType));
                Map<Long, String> projectMap = projects.stream().collect(Collectors.toMap(SdpProject::getId, SdpProject::getProjectCode));
                list.forEach(x -> {
                    try {
                        x.setEngineType(engineMap.get(x.getEngineId()));
                        x.setProjectCode(projectMap.get(x.getProjectId()));
                        if(EnvironmentEnum.UAT.getCode().equalsIgnoreCase(env)){
                            jobSyncExecutor4Uat.execute(new JobStatusSyncThread(x, downLatch, instanceMapper, historyMapper, jobService, jobStatusNotifyService, kubernetesClientConfig.getKubernetesClient(env),sdpConfig,engineService,restTemplate,sdpRuntimeLogService));
                        }else {
                            jobSyncExecutor4Prod.execute(new JobStatusSyncThread(x, downLatch, instanceMapper, historyMapper, jobService, jobStatusNotifyService,kubernetesClientConfig.getKubernetesClient(env),sdpConfig,engineService,restTemplate,sdpRuntimeLogService));
                        }
                    } catch (Exception e) {
                        downLatch.countDown();
                        String errorMessage = String.format("===执行作业状态同步任务异常===, jobId=%d, jobName=%s", x.getJobId(), x.getJobName());
                        DataMonitorUtils.monitorError(errorMessage);
                        log.error(errorMessage, e);
                    }
                });
                try {
                    boolean success = downLatch.await(180L, TimeUnit.SECONDS);
                    if (!success) {
                        DataMonitorUtils.monitorError("===作业状态同步await超时===");
                    }
                } catch (Exception e) {
                    DataMonitorUtils.monitorError("===作业状态同步await出现异常===");
                    log.error("===作业状态同步await出现异常===", e);
                }
            }
        }catch (Exception e){
            log.error("同步作业状态的定时任务运行时异常==={}",e);
        }finally {
            if(jedis != null){
                jedis.close();
            }
            log.info("JobStatusSyncJob 耗时: {}实例,{}毫秒",CollectionUtils.isEmpty(list)?0:list.size(),System.currentTimeMillis() - start);
        }
    }
}
