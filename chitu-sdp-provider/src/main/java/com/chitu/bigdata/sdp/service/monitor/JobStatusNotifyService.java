package com.chitu.bigdata.sdp.service.monitor;

import com.chitu.bigdata.sdp.api.domain.RuleCehckResp;
import com.chitu.bigdata.sdp.api.enums.*;
import com.chitu.bigdata.sdp.api.model.*;
import com.chitu.bigdata.sdp.constant.RedisKeyConstant;
import com.chitu.bigdata.sdp.mapper.*;
import com.chitu.bigdata.sdp.service.ProjectService;
import com.chitu.bigdata.sdp.service.RedisTemplateService;
import com.chitu.bigdata.sdp.service.UserService;
import com.xiaoleilu.hutool.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * @author 587694
 * @create 2021-1-18 15:48
 */
@Component("INTERRUPT_OPERATION")
@Slf4j
public class JobStatusNotifyService extends AbstractRuleIndexMonitor {
    @Autowired
    private RuleAlertNotifyService alertService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private SdpProjectMapper sdpProjectMapper;

    @Autowired
    private SdpJobInstanceMapper sdpJobInstanceMapper;

//    @Autowired
//    private RuleIndexMonitorFactory ruleIndexFactory;

    @Autowired
    private SdpJobMapper sdpJobMapper;
    @Autowired
    private SdpUserMapper sdpUserMapper;

    @Autowired
    private RedisTemplate<String, String> redisTmplate;

    @Autowired
    private SdpOperationLogMapper sdpOperationLogMapper;
    @Autowired
    UserService userService;
    @Autowired
    RedisTemplateService redisTemplateService;


    public void buildStatusParam(SdpJobInstance jobInstance, SdpJob sdpJob){
        //修正作业状态变更
        if (fixStatus(sdpJob,jobInstance)) {
            String alertTime = DateUtil.formatDateTime(new Date());
            HashSet<String> userNums = new HashSet<>();

            List<SdpUser> sdpUsers = userService.queryReceiveUserList(jobInstance.getProjectId(), null);
            sdpUsers.forEach(item -> {
                 userNums.add(item.getEmployeeNumber());
            });

            SdpJob sj = sdpJobMapper.selectById(jobInstance.getJobId());
            String updatedBy = sj.getUpdatedBy();
            if(!StringUtils.isEmpty(updatedBy)){
                SdpUser sdpUser = sdpUserMapper.selectById(Long.valueOf(updatedBy));
                if(null != sdpUser){
                    userNums.add(sdpUser.getEmployeeNumber());
                }
            }
            Long id = jobInstance.getId();
            SdpOperationLog sol = sdpOperationLogMapper.getLogByInstanceId(id);
            if (Objects.nonNull(sol)) {
                String createdBy = sol.getCreatedBy();
                if(!StringUtils.isEmpty(createdBy)){
                    SdpUser user = sdpUserMapper.selectById(Long.valueOf(createdBy));
                    if(null != user){
                        String userName = user.getUserName();
                        sdpJob.setUpdatedBy(userName);
                    }
                }
            }
            ArrayList<String> users = new ArrayList<>(userNums);
            ArrayList<String> alertType = new ArrayList<>();
            alertType.add(NotifiType.EMAIL.name());

            // V2.5 优化sql
            //SdpProject sp = sdpProjectMapper.get(jobInstance.getProjectId());
            SdpProject sp  = redisTemplateService.getObject("SdpProject", jobInstance.getProjectId(), 10, TimeUnit.MINUTES, () -> {
                return sdpProjectMapper.selectById(jobInstance.getProjectId());
            },SdpProject.class);

            sdpJob.setProject(sp);
            sdpJob.setJobName(sj.getJobName());
            sdpJob.setNewStatus(jobInstance.getJobStatus());
            SdpJobAlertRule sdpJobAlertRule = new SdpJobAlertRule();
            sdpJobAlertRule.setJobStatusAlertType(JobStatusAlertType.JOB_STATUS_CHANGE.name());
            alertService.alert(alertTime, users, alertType, sdpJob, sdpJobAlertRule, NotifiUserType.COMMON_NOTICE_USERS,null);
        }
    }

    private boolean fixStatus(SdpJob sdpJob, SdpJobInstance jobInstance) {
//
//        Long id = jobInstance.getId();
//        ArrayList<SdpOperationLog> sols = sdpOperationLogMapper.selectByInstanceId(id);
//        if (!CollectionUtils.isEmpty(sols)) {
//            if (JobStatus.RFAILED.name().equals(jobInstance.getJobStatus())) {
//                if (JobAction.START.name().equalsIgnoreCase(sols.get(0).getAction())){
//                    return;
//                }
//            }
//            if (JobStatus.RFAILED.name().equals(sdpJob.getJobStatus())) {
//                if (JobAction.START.name().equalsIgnoreCase(sols.get(0).getAction())){
//                    sdpJob.setJobStatus(JobStatus.RUNNING.name());
//                }
//            }
//            if (JobStatus.INITIALIZE.name().equalsIgnoreCase(sdpJob.getJobStatus())) {
//                if (sols.size() >= 2) {
//                 if (JobAction.STOP.name().equalsIgnoreCase(sols.get(1).getAction())){
//                     sdpJob.setJobStatus(JobStatus.TERMINATED.name());
//                 }
//                }
//            }
//        }
        if (JobStatus.RFAILED.name().equals(jobInstance.getJobStatus()) && JobStatus.RUNNING.name().equals(jobInstance.getSignStatus())){
            return false;
        }
        if (JobStatus.RFAILED.name().equals(sdpJob.getJobStatus())
                && JobStatus.TERMINATED.name().equals(jobInstance.getJobStatus())
                && JobStatus.RUNNING.name().equals(jobInstance.getSignStatus())){
            sdpJob.setJobStatus(JobStatus.RUNNING.name());
        }
        if (JobStatus.RUNNING.name().equals(jobInstance.getJobStatus()) && JobStatus.TERMINATED.name().equals(jobInstance.getSignStatus())){
            sdpJob.setJobStatus(JobStatus.TERMINATED.name());
        }
        return true;
    }

    public void monitorJobStatus(SdpJobAlertRule rule) {
        //通过jobId获取作业状态与规则中jobStatus对比预警
        Long jobId = rule.getJobId();
        SdpJob sdpJob = sdpJobMapper.selectById(jobId);
        if(sdpJob == null){
            return;
        }

        //V2.5 sql优化
        // SdpProject sdpProject = sdpProjectMapper.get(sdpJob.getProjectId());
        SdpProject sdpProject  = redisTemplateService.getObject("SdpProject", sdpJob.getProjectId(), 10, TimeUnit.MINUTES, () -> {
            return sdpProjectMapper.selectById(sdpJob.getProjectId());
        },SdpProject.class);
        sdpJob.setProject(sdpProject);
        SdpJobInstance sdpJobInstance = sdpJobInstanceMapper.queryByJobId(jobId);
        if (Objects.isNull(sdpJobInstance)){
            return;
        }
        if (!checkHitRule(sdpJobInstance,sdpJob)) {
            return;
        }
        log.warn("触发告警：job[{}]，规则[{}]，命中规则[{}]", sdpJob.getJobName(), rule.getRuleName(), rule.getRuleContent());
        // 触发告警时间
        String alertTime = DateUtil.formatDateTime(new Date());
        Integer alertNum = 1;
        // 校验告警频率：连续N分钟内只发一次告警
        if (!checkSatisfyAlertRate(alertTime, sdpJob, rule)) {
            return;
        }
        alertNum = getAlertNum(sdpJob,rule,alertTime,alertNum);
        // 开始发送告警
        Map<String, Boolean> sendFlagMap = sendAlert(alertTime, sdpJob, rule, alertNum);
          sendFlagMap.forEach((notifiType, flag) -> {
            if (!flag) {
                return;
            }
            String key = RedisKeyConstant.MONITOR_ALERT.getKey() + sdpJob.getId() + ":" + rule.getId() + ":" + notifiType.toLowerCase();
            redisTmplate.opsForValue().set(key, alertTime, 1, TimeUnit.DAYS);
        });

        //TODO 目前没有保存通知类型发送是否成功状态，即不管发送成功失败都会保存db告警记录
        String desc = String.format("作业运行异常，状态为%s",sdpJob.getJobStatus());
        saveJobAlertRecord(alertTime, desc, sdpJob, rule,alertNum);
    }

    private boolean checkHitRule(SdpJobInstance sdpJobInstance, SdpJob sdpJob) {
        if (JobStatus.RUNNING.name().equals(sdpJobInstance.getSignStatus())) {
            //判断操作日志，规避错误预警
            ArrayList<SdpOperationLog> sdpOperationLogs = sdpOperationLogMapper.selectByInstanceId(sdpJobInstance.getId());
            if (!CollectionUtils.isEmpty(sdpOperationLogs)) {
                SdpOperationLog sdpOperationLog = sdpOperationLogs.get(0);
                if (OperationStage.SUCCESS.name().equalsIgnoreCase(sdpOperationLog.getStatus()) &&
                        (JobAction.START.name().equalsIgnoreCase(sdpOperationLog.getAction())
                                || JobAction.RECOVER.name().equalsIgnoreCase(sdpOperationLog.getAction()))) {
                    JobStatus jobStatus = JobStatus.fromStatus(sdpJobInstance.getJobStatus());
                    switch (jobStatus) {
                        case SFAILED:
                        case RFAILED:
                        case PAUSED:
                        case TERMINATED:
                            sdpJob.setJobStatus(jobStatus.getStatus());
                            return true;
                        default:
                            return false;
                    }
                }
            }

        }
        return false;
    }

    @Override
    protected RuleCehckResp checkMonitorIndexValue(SdpJob jobInfo, RuleContent ruleContent) {
        return null;
    }
}
