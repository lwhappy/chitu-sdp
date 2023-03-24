

package com.chitu.bigdata.sdp.job;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.chitu.bigdata.sdp.api.bo.SdpJobBO;
import com.chitu.bigdata.sdp.api.enums.JobAction;
import com.chitu.bigdata.sdp.api.enums.JobStatusAlertType;
import com.chitu.bigdata.sdp.api.enums.NotifiUserType;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.flink.ResourceValidateResp;
import com.chitu.bigdata.sdp.api.model.*;
import com.chitu.bigdata.sdp.api.vo.JobResp;
import com.chitu.bigdata.sdp.config.CustomMonitorConfigProperties;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.interceptor.EnvHolder;
import com.chitu.bigdata.sdp.mapper.SdpUatJobRunningConfigMapper;
import com.chitu.bigdata.sdp.service.*;
import com.chitu.bigdata.sdp.service.monitor.RuleAlertNotifyService;
import com.chitu.bigdata.sdp.service.notify.EmailNotifyService;
import com.chitu.bigdata.sdp.utils.JobOperationUtils;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.chitu.cloud.config.scheduler.RedisLeaderSchedule;
import com.chitu.cloud.exception.ApplicationException;
import com.xiaoleilu.hutool.date.DateUtil;
import com.xiaoleilu.hutool.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.assertj.core.util.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * TODO 集群挂掉时，自动拉起任务
 *
 * @author chenyun
 * @version 1.0
 * @date 2022/7/11 16:24
 */
@Component
@Slf4j
public class AutoPullTaskJob {

    @Autowired
    private CustomMonitorConfigProperties customMonitorConfig;

    @Autowired
    private EmailNotifyService emailNotifyService;

    @Autowired
    private JobInstanceService jobInstanceService;
    @Autowired
    private EngineService engineService;
    @Autowired
    private JobService jobService;
    @Autowired
    private SdpSysConfigService configService;
    @Autowired
    private RuleAlertNotifyService ruleAlertNotifyService;
    @Autowired
    private UserService userService;
    @Autowired
    private SdpUatJobRunningConfigMapper uatJobConfigMapper;
    @Autowired
    SdpConfig sdpConfig;

    @RedisLeaderSchedule
    @Scheduled(fixedDelayString = "${custom.autoPullJobConf.fixedDelay}")
    public void pullTask(){
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
        CustomMonitorConfigProperties.AutoPullJobConf autoPullJobConf = customMonitorConfig.getCustomConfigAndEnv().getCustomConfig().getAutoPullJobConf();
        //开关没开启，则不执行程序
        if(autoPullJobConf.getStartSwitch() == 0){
            return;
        }
        List<Long> jobs = jobInstanceService.getMapper().queryFailedJobs(autoPullJobConf.getExecuteDuration());
        if(CollectionUtils.isNotEmpty(jobs)){
            for(Long jobId : jobs){
                try{
                    ResourceValidateResp validateResp = engineService.checkResource(jobId);
                    if(validateResp.getSuccess()){
                       /* List<Checkpoint> checkpoints = jobService.getCheckpoints(jobId, 1);
                        if(CollectionUtils.isEmpty(checkpoints)){
                            continue;
                        }
                        Checkpoint checkpoint = checkpoints.stream().findFirst().get();
                        String checkPointPath = checkpoint.getFilePath();
                        if(StringUtils.isEmpty(checkpoint)){
                            continue;
                        }*/
                        log.info("======自动拉起{}任务======",jobId);
                        SdpJobBO jobBO = new SdpJobBO();
                        jobBO.getVo().setId(jobId);
                       // jobBO.setSavepointPath(checkPointPath);
                        jobBO.setUseLatest(false);
                        jobBO.setAutoPull(true);
                        List<SdpUatJobRunningConfig> list = uatJobConfigMapper.queryByJobIds(Lists.newArrayList(jobId));
                        if(CollectionUtils.isNotEmpty(list)){
                            jobBO.setUatJobRunningValidDays(list.get(0).getDays());
                        }
                        configService.validateAction(JobAction.START.toString());
                        try{
                            JobOperationUtils.runInJobLock(jobId, JobOperationUtils.OperationType.RECOVER_JOB, () -> {
                                jobService.validateStart(jobBO, JobAction.RECOVER.toString());
                                jobService.startJob(jobBO, JobAction.RECOVER.toString());
                                return null;
                            });
                        } catch (Exception e){
                            log.error("AutoPullTaskJob#pullTask#启动作业失败===jobId: " + jobId, e);
                            if(e instanceof ApplicationException && ((ApplicationException) e).getAppCode().getCode() == ResponseCode.CONNECT_IS_WRONG.getCode()){
                                throw ((ApplicationException) e);
                            }else {
                                throw new ApplicationException(ResponseCode.CONNECT_IS_WRONG,String.format("AutoPullTaskJob#pullTask#启动作业失败, jobId: %d, message: %s", jobId, e.getMessage()));
                            }
                        }
                    }else{
                        log.info(jobId+"AutoPullTaskJob#pullTask===集群资源不足，无法拉起任务===");
                    }
                }catch (Exception e){
                    log.error(jobId+"AutoPullTaskJob#pullTask#自动拉起任务异常",e);
                    e.printStackTrace();
                }
            }

            //发送告警
            for(Long jobId : jobs){
                sendNotice(jobId);
            }
        }
    }

    private void sendNotice(Long jobId) {
        try{
            SdpJobInstance sdpJobInstance = queryTaskStatus(jobId);
            if(sdpJobInstance != null){
                JobResp jobResp = jobService.getMapper().queryJobById(jobId);
                List<SdpUser> sdpUserList = userService.queryReceiveUserList(jobResp.getProjectId(), NotifiUserType.ALARM_NOTICE_USERS);
                List<String> adminEmployeeNumbers = sdpUserList.stream().map(SdpUser::getEmployeeNumber).collect(Collectors.toList());
                SdpJobAlertRule jobAlertRule = new SdpJobAlertRule();
                jobAlertRule.setJobStatusAlertType(JobStatusAlertType.JOB_PULL_UP.name());
                jobAlertRule.setRuleName("任务异常停止，系统自动拉起");
                SdpProject project = new SdpProject();
                project.setProjectName(jobResp.getProjectName());
                SdpJob jobInfo = new SdpJob();
                jobInfo.setJobName(jobResp.getJobName());
                if (StrUtil.isNotEmpty(jobResp.getUpdatedBy())) {
                    SdpUser sdpUser = userService.get(Long.valueOf(jobResp.getUpdatedBy()));
                    if (sdpUser != null) {
                        jobInfo.setOwner(sdpUser.getUserName());
                    }
                } else if (CollectionUtil.isNotEmpty(adminEmployeeNumbers)) {
                    List<SdpUser> sdpUsers = userService.selectAll(new SdpUser(adminEmployeeNumbers.get(0)));
                    if (CollectionUtil.isNotEmpty(sdpUsers)) {
                        jobInfo.setOwner(sdpUsers.get(0).getUserName());
                    }
                }
                String alertTime = DateUtil.formatDateTime(new Date());
                emailNotifyService.sendMsg(sdpUserList.stream().map(SdpUser::getEmail).collect(Collectors.toList()), jobAlertRule.getRuleName(), JSON.toJSONString(ruleAlertNotifyService.buildTemplateParam(alertTime, project, jobInfo, jobAlertRule, 1)));
            }
        }catch (Exception e){
            log.error("AutoPullTaskJob#pullTask#自动拉起任务，发送跨声通知异常{}",e);
            e.printStackTrace();
        }
    }

    private SdpJobInstance queryTaskStatus(Long jobId) {
        int times = 120 * 1000;
        SdpJobInstance sdpJobInstance = null;
        try{
            long startTime = System.currentTimeMillis();
            do{
                sdpJobInstance = jobInstanceService.getMapper().queryJobById(jobId);
                Thread.sleep(2000);
            }while (sdpJobInstance == null && (System.currentTimeMillis() < startTime + times));
        }catch (Exception e){
            e.printStackTrace();
            log.error("查询任务状态异常==="+e);
        }
        return sdpJobInstance;
    }
}
