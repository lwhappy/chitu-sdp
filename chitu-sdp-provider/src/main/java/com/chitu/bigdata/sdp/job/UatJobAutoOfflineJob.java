package com.chitu.bigdata.sdp.job;

import com.chitu.bigdata.sdp.api.bo.SdpJobBO;
import com.chitu.bigdata.sdp.api.enums.EnvironmentEnum;
import com.chitu.bigdata.sdp.api.enums.JobAction;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.SdpJobInstance;
import com.chitu.bigdata.sdp.api.model.SdpUatJobRunningConfig;
import com.chitu.bigdata.sdp.config.UatJobAutoOfflineJobConfig;
import com.chitu.bigdata.sdp.constant.CommonConstant;
import com.chitu.bigdata.sdp.interceptor.EnvHolder;
import com.chitu.bigdata.sdp.mapper.SdpJobInstanceMapper;
import com.chitu.bigdata.sdp.mapper.SdpOperationLogMapper;
import com.chitu.bigdata.sdp.mapper.SdpUatJobRunningConfigMapper;
import com.chitu.bigdata.sdp.service.FileService;
import com.chitu.bigdata.sdp.service.JobService;
import com.chitu.bigdata.sdp.service.SdpSysConfigService;
import com.chitu.bigdata.sdp.utils.JobOperationUtils;
import com.chitu.cloud.config.scheduler.RedisLeaderSchedule;
import com.chitu.cloud.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * uat作业自动下线，释放资源，提高利用率
 *
 * @author zouchangzhen
 * @date 2022/5/17
 */
@Slf4j
@Component
//@ConditionalOnProperty(prefix = "sdp", name = "uat", havingValue = "true", matchIfMissing = false)
public class UatJobAutoOfflineJob {

    @Autowired
    SdpJobInstanceMapper sdpJobInstanceMapper;
    @Autowired
    SdpUatJobRunningConfigMapper sdpUatJobRunningConfigMapper;
    @Autowired
    private FileService fileService;
    @Autowired
    private SdpOperationLogMapper sdpOperationLogMapper;
    @Autowired
    UatJobAutoOfflineJobConfig uatJobAutoOfflineJobConfig;
    @Autowired
    private SdpSysConfigService configService;
    @Autowired
    private JobService jobService;


    /**
     * 双重开关，防止误操作
     * sys.uat: true|false。生产环境不需要配置该值，默认是false。该开关控制bean创建和代码判断
     * spring.profiles.active: dev|uat|prod
     */
    @RedisLeaderSchedule
    @Scheduled(fixedDelay = 5*60*1000)
    public void jobAutoOfflineJob() {
        String osName = System.getProperties().getProperty("os.name");
        if(osName.equals("Windows 10")){
            return;
        }
        //该任务只需要对uat执行操作
        EnvHolder.addEnv(EnvironmentEnum.UAT.getCode());
        try {
            if (!uatJobAutoOfflineJobConfig.isUatOrDev()) {
                return;
            }
            handleSingleEnv();
        }finally {
            EnvHolder.clearEnv();
        }
    }

    private void handleSingleEnv() {
        log.info("===>>>uat自动释放资源<<<===");
        //1.查询正在运行的job
        List<SdpJobInstance> sdpJobInstances = sdpJobInstanceMapper.queryRunningJob();
        if (CollectionUtils.isEmpty(sdpJobInstances)) {
            return;
        }

        List<Long> jobIds = Optional.ofNullable(sdpJobInstances).orElse(new ArrayList<>()).stream().filter(f -> Objects.nonNull(f) && Objects.nonNull(f.getJobId())).map(m -> m.getJobId()).collect(Collectors.toList());
        List<SdpUatJobRunningConfig> sdpUatJobRunningConfigs = sdpUatJobRunningConfigMapper.queryByJobIds(jobIds);
        Map<Long, Integer> jobDayMap = Optional.ofNullable(sdpUatJobRunningConfigs).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(SdpUatJobRunningConfig::getJobId, SdpUatJobRunningConfig::getDays, (k1, k2) -> k2));

        for (SdpJobInstance sdpJobInstance : sdpJobInstances) {
            //兼容之前没有设置规则,已经启动起来的任务，默认-1天
            Integer days = jobDayMap.getOrDefault(sdpJobInstance.getJobId(), CommonConstant.MINUS_ONE);
            if (days < 0) {
                log.info(String.format("===>>>jobId:%s永久不下线", sdpJobInstance.getJobId()));
                //小于0永久不下线
                continue;
            }
            boolean invalid = sdpJobInstance.getExecuteDuration() >= days * CommonConstant.ONE_DAY_MS;
            if (invalid) {
                try {
                    configService.validateAction(JobAction.STOP.toString());
                    SdpJobBO sdpJobBO = new SdpJobBO();
                    Long jobId = sdpJobInstance.getJobId();
                    sdpJobBO.getVo().setId(jobId);
                    log.info("======uat自动释放资源{}任务======",jobId);
                    try {
                        JobOperationUtils.runInJobLock(jobId, JobOperationUtils.OperationType.STOP_JOB, () -> {
                            jobService.validateStop(sdpJobBO, JobAction.STOP.toString());
                            jobService.stopJob(sdpJobBO, JobAction.STOP.toString());
                            return null;
                        });
                    } catch (Exception e) {
                        throw new ApplicationException(ResponseCode.ERROR,
                                String.format("停止作业失败, jobId: %d, message: %s", jobId, e.getMessage()),e);
                    }
                } catch (Exception e) {
                    log.warn("uat自动释放资源异常", e);
                }
            }
        }
    }

}
