package com.chitu.bigdata.sdp.job;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chitu.bigdata.sdp.api.domain.ClusterInfo;
import com.chitu.bigdata.sdp.api.domain.JobConfig;
import com.chitu.bigdata.sdp.api.enums.*;
import com.chitu.bigdata.sdp.api.flink.App;
import com.chitu.bigdata.sdp.api.flink.AppInfo;
import com.chitu.bigdata.sdp.api.flink.Application;
import com.chitu.bigdata.sdp.api.flink.JobsOverview;
import com.chitu.bigdata.sdp.api.model.*;
import com.chitu.bigdata.sdp.config.CustomMonitorConfigProperties;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.flink.common.enums.ExecutionMode;
import com.chitu.bigdata.sdp.flink.common.util.HadoopUtils;
import com.chitu.bigdata.sdp.mapper.SdpJobInstanceMapper;
import com.chitu.bigdata.sdp.mapper.SdpOperationLogMapper;
import com.chitu.bigdata.sdp.mapper.SdpRawStatusHistoryMapper;
import com.chitu.bigdata.sdp.service.*;
import com.chitu.bigdata.sdp.service.monitor.JobStatusNotifyService;
import com.chitu.bigdata.sdp.utils.JobOperationUtils;
import com.chitu.cloud.model.ResponseData;
import com.chitu.cloud.utils.SpringUtils;
import com.xiaoleilu.hutool.date.DateUtil;
import io.fabric8.kubernetes.api.model.ContainerStatus;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.api.model.PodList;
import io.fabric8.kubernetes.client.KubernetesClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/11/22 10:25
 */
@Slf4j
@Component
public class JobStatusSyncThread implements Runnable{

    private JobStatusNotifyService jobStatusNotifyService;

    private String flinkHost;
    private String finkUI = "%s/proxy/%s/";
    private String finkUIK8s = "%s/#/overview";

    private SdpJobInstance jobInstance;
    private CountDownLatch downLatch;
    private SdpJobInstanceMapper instanceMapper;
    private SdpRawStatusHistoryMapper historyMapper;
    private JobService jobService;
    private KubernetesClient kubernetesClient;
    private SdpConfig sdpConfig;
    private EngineService engineService;
    private RestTemplate restTemplate;
    private SdpRuntimeLogService sdpRuntimeLogService;

    private final static String UPDATE_COUNT_REDIS_KEY = "bigdata:sdp:JobStatusSync:%s:%s";
    private final  static String UPDATESELECTIVE_LOG = "JobStatusSyncThread-updateSelective:%s -> %s";
    private final static Integer TEN = 10;

    public JobStatusSyncThread(){}

    public JobStatusSyncThread(SdpJobInstance jobInstance, CountDownLatch downLatch, SdpJobInstanceMapper instanceMapper, SdpRawStatusHistoryMapper historyMapper, JobService jobService, JobStatusNotifyService jobStatusNotifyService,KubernetesClient kubernetesClient,SdpConfig sdpConfig,EngineService engineService,RestTemplate restTemplate,SdpRuntimeLogService sdpRuntimeLogService) {
        this.jobInstance = jobInstance;
        this.downLatch = downLatch;
        this.instanceMapper = instanceMapper;
        this.historyMapper = historyMapper;
        this.jobService = jobService;
        this.jobStatusNotifyService = jobStatusNotifyService;
        this.kubernetesClient = kubernetesClient;
        this.sdpConfig = sdpConfig;
        this.engineService = engineService;
        this.restTemplate = restTemplate;
        this.sdpRuntimeLogService = sdpRuntimeLogService;
    }

    @Override
    public void run() {
        try{
            boolean success = JobOperationUtils.runInJobLock(
                    jobInstance.getJobId(),
                    JobOperationUtils.OperationType.SYNC_JOB_STATUS,
                    () -> {
                        if(EngineTypeEnum.KUBERNETES.getType().equals(jobInstance.getEngineType())){
                            executeK8s(jobInstance);
                        }else{
                            execute(jobInstance);
                        }
                        return null;
                    }
            );
            if(!success) {
                log.warn("jobId: {}, jobName: {} 同步状态失败, 等待下次调度进行同步", jobInstance.getJobId(), jobInstance.getJobName());
            }
        } catch (Exception e){
            log.error("同步作业状态报错===, jobId: " + jobInstance.getJobId(), e);
        } finally {
            downLatch.countDown();
        }
    }

    // 判断执行引擎
    public void execute(SdpJobInstance x) {
        //本来想优化掉这个查询的，发现传参的方式不在锁里面，如果有人在操作会导致状态不一致
        //List<SdpJobInstance> list = instanceMapper.queryById(x);
        final SdpJobInstance mSdpJobInstance = x;
        RedisTemplateService redisTemplateService = SpringUtils.getBean(RedisTemplateService.class);
        List<SdpJobInstance> list = redisTemplateService.getList(RedisTemplateService.SDP_JOB_INSTANCE_LIST_CACHE, x.getJobId(), 1, TimeUnit.MINUTES, () -> {
            return instanceMapper.queryById(mSdpJobInstance);
        }, SdpJobInstance.class);
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        x = list.get(0);
        SdpJobInstance cloneInstance = new SdpJobInstance();
        BeanUtils.copyProperties(x,cloneInstance,new String[]{"instanceInfo","executeDuration","startTime","endTime","creationDate","updationDate"});

        Boolean status = true;
        String yarnState = null;
        SdpRawStatusHistory history = new SdpRawStatusHistory();
        String rawStatus1 = x.getRawStatus();
        history.setFromStatus(rawStatus1);

        // V2.5 sql优化
        // String hadoopConfDir = jobService.getHadoopConfDir(x.getJobId());
        final Long jobId = x.getJobId();
        String hadoopConfDir = redisTemplateService.getObject(RedisTemplateService.HADOOP_CONF_DIR, jobId, 2, TimeUnit.MINUTES, () -> {
            return jobService.getHadoopConfDir(jobId);
        }, String.class);

        long start = System.currentTimeMillis();
        try {
            JobsOverview jobsOverview = httpJobsOverview(x.getApplicationId(),hadoopConfDir);
            log.info(String.format(UPDATESELECTIVE_LOG,x.getId(), "请求flink resp: "+ (Objects.isNull(jobsOverview)?"":JSON.toJSONString(jobsOverview)) + ", 耗时："+(System.currentTimeMillis() - start)));
            Optional<JobsOverview.Job> optional = jobsOverview.getJobs().stream().findFirst();
            if (optional.isPresent()) {
                JobsOverview.Job jobOverview = optional.get();
                long startTime = jobOverview.getStartTime();
                long endTime = jobOverview.getEndTime();
                x.setStartTime(new Timestamp(startTime));
                if (endTime != -1) {
                    x.setEndTime(new Timestamp(endTime));
                }
                x.setInstanceInfo(JSON.toJSONString(jobOverview));
                x.setFlinkJobId(jobOverview.getId());
                x.setExecuteDuration(jobOverview.getDuration());
                x.setFlinkUrl(String.format(finkUI,flinkHost, x.getApplicationId()));
                String state = jobOverview.getState();
                assembleJobStatus(x, state);
                //封装作业状态变更历史
                history.setJobId(x.getJobId());
                history.setInstanceId(x.getId());
                history.setTrigger(TriggerType.FLINK.toString());
                history.setToStatus(state);
            }
        }catch (Exception flinkException){
            try {
                String appName = x.getProjectCode()+"_"+x.getJobName();
                if(x.getExpectStatus().equals(ExpectStatus.INITIALIZE.toString())){
                    //此时，任务刚刚通过审批，所以，所有状态都为初始化，无需同步状态
                    appName = null;
                }
                start = System.currentTimeMillis();
                AppInfo appInfo = com.chitu.bigdata.sdp.utils.HadoopUtils.httpYarnAppInfo(x.getApplicationId(),appName,hadoopConfDir);
                if(log.isTraceEnabled()){
                    log.trace(String.format(UPDATESELECTIVE_LOG,x.getId(), "请求yarn resp: "+ (Objects.isNull(appInfo)?"":JSON.toJSONString(appInfo)) + ", 耗时："+(System.currentTimeMillis() - start)));
                }
                if(appInfo != null){
                    App app = appInfo.getApp();
                    if(app != null){
                        String finalStatus = app.getFinalStatus();
                        //封装作业状态变更历史
                        history.setJobId(x.getJobId());
                        history.setInstanceId(x.getId());
                        history.setTrigger(TriggerType.YARN.toString());
                        history.setToStatus(finalStatus);
                        yarnState = app.getState();
                        if(RawStatus.ACCEPTED.toString().equals(yarnState)){
                            finalStatus = yarnState;
                        }
                        assembleJobStatus(x, finalStatus);
                        x.setEndTime(new Timestamp(app.getFinishedTime()));
                        x.setStartTime(new Timestamp(app.getStartedTime()));
                        x.setApplicationId(app.getId());
                        x.setExecuteDuration(app.getElapsedTime());
                    }else{
                        //说明yarn不存在该任务，有可能是yarn集群重启了导致该情况,则默认给停止状态
                        x.setExpectStatus(ExpectStatus.TERMINATED.toString());
                        String finalStatus = RawStatus.KILLED.toString();
                        //封装作业状态变更历史
                        history.setJobId(x.getJobId());
                        history.setInstanceId(x.getId());
                        history.setTrigger(TriggerType.YARN.toString());
                        history.setToStatus(finalStatus);
                        yarnState = RawStatus.KILLED.toString();
                        assembleJobStatus(x, finalStatus);
                        x.setEndTime(new Timestamp(System.currentTimeMillis()));
                    }
                }else{
                    //如果yarn上没有查询到该任务，有可能是人为手动kill了该任务，则默认给KILLED状态
                    if(!x.getRawStatus().equals(RawStatus.INITIALIZE.toString()) && !x.getExpectStatus().equals(ExpectStatus.INITIALIZE.toString())){
                        String finalStatus = RawStatus.KILLED.toString();
                        yarnState = RawStatus.KILLED.toString();
                        assembleJobStatus(x, finalStatus);
                        x.setEndTime(new Timestamp(System.currentTimeMillis()));
                        //封装作业状态变更历史
                        history.setJobId(x.getJobId());
                        history.setInstanceId(x.getId());
                        history.setTrigger(TriggerType.YARN.toString());
                        history.setToStatus(finalStatus);
                    }
                }
            }catch (Exception yarnException){
                status = false;
                log.error(x.getApplicationId()+"===调用Yarn接口同步作业状态异常==={}",yarnException);
            }
        }
        if(status){
            if(yarnState != null && yarnState.equals(RawStatus.KILLED.toString())){
                //此时为用户在yarn页面上点击kill任务
                x.setExpectStatus(ExpectStatus.TERMINATED.toString());
                //既然上面是在yarn上面kill掉的，按道理job statue也是停止状态
                x.setJobStatus(JobStatus.TERMINATED.toString());
            }
            x.setIsLatest(null);
            boolean changed = cloneInstance.isChanged(x);

            Long count4Instance = countUpdateSelective(x.getId());

            CustomMonitorConfigProperties customMonitorConfigProperties = SpringUtils.getBean(CustomMonitorConfigProperties.class);
            Integer updateFrequency = Optional.ofNullable(customMonitorConfigProperties).map(m -> m.getCustomConfigAndEnv()).map(m->m.getCustomConfig()).map(m->m.getJobStatusSyncJob()).map(m->m.getUpdateFrequency()).orElse(TEN);

            if (log.isTraceEnabled()) {
                log.trace(String.format(UPDATESELECTIVE_LOG,x.getId(),"实例是否有变更:" + changed + ",实例更新次数是否已经达到:" + (count4Instance % updateFrequency == 0)+",SdpJobInstance: " + JSON.toJSONString(cloneInstance)));
            }

            //根据条件查询，如果查不到，证明实例有变更，需要及时更新
            //根据次数判断更新
            if(changed || count4Instance % updateFrequency == 0){
                redisTemplateService.delete(RedisTemplateService.SDP_JOB_INSTANCE_LIST_CACHE,x.getJobId());
                if (log.isTraceEnabled()) {
                    log.trace(String.format(UPDATESELECTIVE_LOG,x.getId(),"实例信息更新:" + JSON.toJSONString(x)));
                }
                JobInstanceService jobInstanceService = SpringUtils.getBean(JobInstanceService.class);
                jobInstanceService.updateSelective(x);
            }
            if(history.getJobId() != null){
                if(!history.getFromStatus().equals(history.getToStatus())){
                    historyMapper.insert(history);
                }
            }
        }
    }

    // k8s环境执行逻辑
    public void executeK8s(SdpJobInstance sdpJobInstance) {
        final SdpJobInstance mSdpJobInstance = sdpJobInstance;
        RedisTemplateService redisTemplateService = SpringUtils.getBean(RedisTemplateService.class);
        List<SdpJobInstance> list = redisTemplateService.getList(RedisTemplateService.SDP_JOB_INSTANCE_LIST_CACHE, sdpJobInstance.getJobId(), 1, TimeUnit.MINUTES, () -> {
            return instanceMapper.queryById(mSdpJobInstance);
        }, SdpJobInstance.class);
        if(CollectionUtils.isEmpty(list)){
            return;
        }
        list.get(0).setEngineId(sdpJobInstance.getEngineId());
        list.get(0).setEngineType(sdpJobInstance.getEngineType());
        list.get(0).setProjectCode(sdpJobInstance.getProjectCode());
        list.get(0).setJobName(sdpJobInstance.getJobName());
        sdpJobInstance = list.get(0);
        SdpJobInstance cloneInstance = new SdpJobInstance();
        BeanUtils.copyProperties(sdpJobInstance,cloneInstance,new String[]{"instanceInfo","executeDuration","startTime","endTime","creationDate","updationDate"});

        Boolean status = true;

        long start = System.currentTimeMillis();
        try {
            JobsOverview jobsOverview = httpJobsOverviewK8s(sdpJobInstance);
            if(log.isTraceEnabled()){
                log.trace(String.format(UPDATESELECTIVE_LOG,sdpJobInstance.getId(), "请求flink resp: "+ (Objects.isNull(jobsOverview)?"":JSON.toJSONString(jobsOverview)) + ", 耗时："+(System.currentTimeMillis() - start)));
            }
            Optional<JobsOverview.Job> optional = jobsOverview.getJobs().stream().findFirst();
            if (optional.isPresent()) {
                JobsOverview.Job jobOverview = optional.get();
                long startTime = jobOverview.getStartTime();
                long endTime = jobOverview.getEndTime();
                sdpJobInstance.setStartTime(new Timestamp(startTime));
                if (endTime != -1) {
                    sdpJobInstance.setEndTime(new Timestamp(endTime));
                }
                sdpJobInstance.setInstanceInfo(JSON.toJSONString(jobOverview));
                sdpJobInstance.setFlinkJobId(jobOverview.getId());
                sdpJobInstance.setExecuteDuration(jobOverview.getDuration());
                sdpJobInstance.setFlinkUrl(String.format(finkUIK8s,sdpJobInstance.getJobmanagerAddress()));
                String state = jobOverview.getState();
                if(RawStatus.RESTARTING.name().equals(state)){
                    assembleJobStatus(sdpJobInstance, RawStatus.RUNNING.name());
                }else{
                    assembleJobStatus(sdpJobInstance, state);
                }
            }else{
                analyseByJobManagerRestartCount(sdpJobInstance);
            }
        }catch (Exception flinkException){
            try {
                analyseByJobManagerRestartCount(sdpJobInstance);
            }catch (Exception exception){
                status = false;
                log.error(sdpJobInstance.getApplicationId()+"===调用k8s接口同步作业状态异常==={}",exception);
            }
        }
        if(status){
            sdpJobInstance.setIsLatest(null);
            boolean changed = cloneInstance.isChanged(sdpJobInstance);

            Long count4Instance = countUpdateSelective(sdpJobInstance.getId());

            CustomMonitorConfigProperties customMonitorConfigProperties = SpringUtils.getBean(CustomMonitorConfigProperties.class);
            Integer updateFrequency = Optional.ofNullable(customMonitorConfigProperties).map(m -> m.getCustomConfigAndEnv()).map(m->m.getCustomConfig()).map(m->m.getJobStatusSyncJob()).map(m->m.getUpdateFrequency()).orElse(TEN);

            if (log.isTraceEnabled()) {
                log.trace(String.format(UPDATESELECTIVE_LOG,sdpJobInstance.getId(),"实例是否有变更:" + changed + ",实例更新次数是否已经达到:" + (count4Instance % updateFrequency == 0)+",SdpJobInstance: " + JSON.toJSONString(cloneInstance)));
            }

            //根据条件查询，如果查不到，证明实例有变更，需要及时更新
            //根据次数判断更新
            if(changed || count4Instance % updateFrequency == 0){
                redisTemplateService.delete(RedisTemplateService.SDP_JOB_INSTANCE_LIST_CACHE,sdpJobInstance.getJobId());
                if (log.isTraceEnabled()) {
                    log.trace(String.format(UPDATESELECTIVE_LOG,sdpJobInstance.getId(),"实例信息更新:" + JSON.toJSONString(sdpJobInstance)));
                }

                JobInstanceService jobInstanceService = SpringUtils.getBean(JobInstanceService.class);
                jobInstanceService.updateSelective(sdpJobInstance);
            }
        }
    }


    // k8s判断pod重启次数
    private void analyseByJobManagerRestartCount(SdpJobInstance sdpJobInstance){
        Application namespaceAndJobName = getNamespaceAndJobName(sdpJobInstance);
        Integer jobManagerRestartCount = null;
        PodList list = kubernetesClient.pods().inNamespace(namespaceAndJobName.getNamespace()).list();
        // 项目表的项目code,+job表的jobName
        if(!CollectionUtils.isEmpty(list.getItems())){
            List<Pod> podList = list.getItems().stream().filter(item -> item.getMetadata().getName().contains(namespaceAndJobName.getAppId()+"-")).filter(item -> !item.getMetadata().getName().contains(namespaceAndJobName.getAppId()+"-taskmanager")).collect(Collectors.toList());
            if(!CollectionUtils.isEmpty(podList)){
                Pod pod = podList.get(0);
                List<ContainerStatus> containerStatuses = pod.getStatus().getContainerStatuses();
                jobManagerRestartCount = containerStatuses.get(0).getRestartCount();
            }
        }
        // jobManagerRestartCount为空flink restAPI没查到，pod也没查到说明任务正在启动中或者停止了，expectStatus为RUNNING为正在启动中，启动中不用管
        String expectStatus = sdpJobInstance.getExpectStatus();
        if(jobManagerRestartCount == null){
            if(!ExpectStatus.RUNNING.toString().equals(expectStatus)){
                if(ExpectStatus.PAUSED.name().equals(expectStatus)){
                    // 暂停修改状态
                    assembleJobStatus(sdpJobInstance,RawStatus.KILLED.name());
                }else if(ExpectStatus.TERMINATED.name().equals(expectStatus)){
                    // 停止修改状态
                    assembleJobStatus(sdpJobInstance,RawStatus.KILLED.name());
                }
            }
            // 运行结束停止
            if((JobStatus.RUNNING.name().equals(sdpJobInstance.getJobStatus()) && RawStatus.RUNNING.name().equals(sdpJobInstance.getRawStatus()))
                    // 暂停操作(防止暂停后还显示运行中)
                    || (JobStatus.PAUSED.name().equals(sdpJobInstance.getSignStatus()) && JobStatus.RUNNING.name().equals(sdpJobInstance.getJobStatus()))
                    // 停止操作(防止停止后还显示运行中)
                    || (JobStatus.TERMINATED.name().equals(sdpJobInstance.getSignStatus()) && JobStatus.RUNNING.name().equals(sdpJobInstance.getJobStatus())))
            {
                sdpJobInstance.setEndTime(new Timestamp(System.currentTimeMillis()));
                assembleJobStatus(sdpJobInstance, RawStatus.KILLED.toString());
            }
        }else{
            if(jobManagerRestartCount > 0){
                // pod有重启说明启动异常，更新状态为启动失败
                sdpJobInstance.setEndTime(new Timestamp(System.currentTimeMillis()));
                assembleJobStatus(sdpJobInstance, RawStatus.FAILED.toString());
                // JobAction
                SdpOperationLogMapper sdpOperationLogMapper = SpringUtils.getBean(SdpOperationLogMapper.class);
                SdpOperationLog sdpOperationLog = sdpOperationLogMapper.getByInstanceId4JobStatus(sdpJobInstance.getId());
                String actionType = Optional.ofNullable(sdpOperationLog).map(m -> m.getAction()).orElse("");
                if(JobAction.START.toString().equals(actionType)){
                    sdpRuntimeLogService.insertFailLog(sdpJobInstance.getJobId(),sdpJobInstance.getApplicationId(),JobAction.START.name());
                }else if(JobAction.RECOVER.toString().equals(actionType)) {
                    sdpRuntimeLogService.insertFailLog(sdpJobInstance.getJobId(),sdpJobInstance.getApplicationId(),JobAction.RECOVER.name());
                }
                // 停止集群
                killClusterK8s(namespaceAndJobName,sdpJobInstance.getJobId());
            }
        }
    }

    private void killClusterK8s(Application application,Long jobId){
        // 停止集群
        application.setExecutionMode(ExecutionMode.KUBERNETES_NATIVE_APPLICATION.getMode());
        // http请求
        SdpJob sdpJob = jobService.get(jobId);
        JobConfig jobConfig = JSONObject.parseObject(sdpJob.getConfigContent(), JobConfig.class);
        String flinkClientUrl = getFlinkClientUrl(jobConfig);
        restTemplate.postForObject(flinkClientUrl + "/flink/killClusterK8s", application, ResponseData.class);
        log.info("===  killClusterK8s {}  ===",application.getAppId());
    }

    private Application getNamespaceAndJobName(SdpJobInstance sdpJobInstance){
        String env = sdpConfig.getEnvFromEnvHolder(log);
        SdpEngine engine = engineService.get(sdpJobInstance.getEngineId());
        String nameSpace = EnvironmentEnum.UAT.getCode().equals(env) ? engine.getUatNamespace() : engine.getNamespace();
//        String jobName = sdpJobInstance.getProjectCode()+"_"+sdpJobInstance.getJobName();
//        String jobNameToLowerCase = jobName.toLowerCase().replace("_","-");

        Application application = new Application();
        application.setNamespace(nameSpace);
//        application.setJobName(jobNameToLowerCase);
        application.setJobId(sdpJobInstance.getJobId().toString());
//        application.setJobName(subStrJobName(sdpJobInstance.getApplicationId(),application.getJobId()));
        application.setAppId(sdpJobInstance.getApplicationId());
        return application;
    }


    private String getFlinkClientUrl(JobConfig jobConfig) {
        String env = sdpConfig.getEnvFromEnvHolder(log);
        SdpEngine engine = engineService.get(jobConfig.getEngineId());

        ClusterInfo clusterInfo = engineService.getClusterInfo(env, engine);

        if (FlinkVersion.VERSION_115.getVersion().equals(jobConfig.getFlinkVersion())) {
            return clusterInfo.getFlinkClient115Url();
        } else if (FlinkVersion.VERSION_114.getVersion().equals(jobConfig.getFlinkVersion()) || StrUtil.isEmpty(jobConfig.getFlinkVersion())) {
            return clusterInfo.getFlinkClient114Url();
        } else {
            return null;
        }
    }

    // k8s环境通过flink rest api获取任务信息
    public JobsOverview httpJobsOverviewK8s(SdpJobInstance sdpJobInstance) {
        JobsOverview result = null;
        try {
            String format = "%s/jobs/overview";
            String url = String.format(format, sdpJobInstance.getJobmanagerAddress());
            result = com.chitu.bigdata.sdp.utils.HadoopUtils.httpGetDoResult(url, JobsOverview.class);
        }catch (Exception e){
//            log.error("同步作业状态通过flink rest api获取任务信息报错，可能任务已停止请求超时，sdpJobInstance: " + sdpJobInstance.getId());
        }
        return result;
    }


    private void assembleJobStatus(SdpJobInstance x, String finalStatus) {
        String oldStatus = x.getJobStatus();
        String signStatus = x.getSignStatus();
        if(!finalStatus.equals("UNDEFINED")){
            RawStatus rawStatus = RawStatus.fromStatus(finalStatus);
            if(rawStatus != null){
                if(rawStatus.isRunning()){
                    x.setJobStatus(JobStatus.RUNNING.toString());
                    if (!JobStatus.RUNNING.name().equals(x.getSignStatus())){
                        x.setSignStatus(JobStatus.RUNNING.name());
                    }
                }else if(rawStatus.isInitialize()){
                    x.setJobStatus(JobStatus.INITIALIZE.toString());
                }else if(rawStatus.isFailed()){
                    SdpOperationLogMapper sdpOperationLogMapper = SpringUtils.getBean(SdpOperationLogMapper.class);
                    SdpOperationLog sdpOperationLog = sdpOperationLogMapper.getByInstanceId4JobStatus(x.getId());
                    String actionType = Optional.ofNullable(sdpOperationLog).map(m -> m.getAction()).orElse("");
                    if(JobAction.START.toString().equals(actionType)){
                        x.setJobStatus(JobStatus.SFAILED.toString());
                        x.setExpectStatus(JobStatus.SFAILED.toString());
                    }else if(JobAction.RECOVER.toString().equals(actionType)){
                        x.setJobStatus(JobStatus.RFAILED.toString());
                        x.setExpectStatus(JobStatus.RFAILED.toString());
                    }else {
                        x.setJobStatus(JobStatus.TERMINATED.toString());
                        x.setExpectStatus(JobStatus.TERMINATED.toString());
                    }
                }else if(rawStatus.isFinished()){
                    if(x.getExpectStatus().equals(ExpectStatus.PAUSED.toString())){
                        x.setJobStatus(JobStatus.PAUSED.toString());
                    }else{
                        x.setJobStatus(JobStatus.TERMINATED.toString());
                        x.setExpectStatus(JobStatus.TERMINATED.toString());
                    }
                }else if(rawStatus.isTerminated()){
                    if(x.getExpectStatus().equals(ExpectStatus.PAUSED.toString())
                            ||  (x.getJobStatus().equals(JobStatus.RUNNING.name()) && x.getExpectStatus().equals(ExpectStatus.RUNNING.toString()) && JobStatus.PAUSED.name().equals(x.getSignStatus()))
                    ){
                        x.setJobStatus(JobStatus.PAUSED.toString());
                        x.setExpectStatus(ExpectStatus.PAUSED.toString());
                    }else {
                        x.setJobStatus(JobStatus.TERMINATED.toString());
                        x.setExpectStatus(ExpectStatus.TERMINATED.toString());
                    }
                }
            }
            x.setRawStatus(finalStatus);
            if (!x.getJobStatus().equals(oldStatus) && !JobStatus.INITIALIZE.name().equals(x.getJobStatus())){
                SdpJob sdpJob = new SdpJob();
                sdpJob.setId(x.getJobId());
                sdpJob.setJobStatus(oldStatus);
                SdpJobInstance sdpJobInstance = new SdpJobInstance();
                BeanUtils.copyProperties(x,sdpJobInstance);
                sdpJobInstance.setSignStatus(signStatus);
                jobStatusNotifyService.buildStatusParam(sdpJobInstance,sdpJob);
            }
        }
    }

    //通过flink rest api获取任务信息
    public JobsOverview httpJobsOverview(String appId,String hadoopConfDir) {
        JobsOverview result = null;
        try {
            String format = "%s/proxy/%s/jobs/overview";
            flinkHost = HadoopUtils.getFlinkWebAppURL(hadoopConfDir);
            log.info("flinkHost: "+flinkHost);
            String url = String.format(format, flinkHost, appId);
            log.info("url: "+url);
            result = com.chitu.bigdata.sdp.utils.HadoopUtils.httpGetDoResult(url, JobsOverview.class);
        }catch (Exception e){
            //e.printStackTrace();
        }
        return result;
    }

    /**
     * 统计次数。原子性要求不是很高的情况，get出来再自增
     * @param instanceId
     * @return
     */
    Long countUpdateSelective(Long instanceId){
        RedisTemplate redisTemplate = SpringUtils.getBean(StringRedisTemplate.class);
        ValueOperations valueOperations = redisTemplate.opsForValue();

        String redisKey = String.format(UPDATE_COUNT_REDIS_KEY, DateUtil.format(new Date(), "yyyyMMdd"), instanceId);
        Object valObj = valueOperations.get(redisKey);
        Long count = Optional.ofNullable(valObj).map(m -> Long.valueOf(m.toString())).orElse(0L) + 1;
        valueOperations.set(redisKey,count.toString(),2, TimeUnit.DAYS);

        return  count;
    }
}
