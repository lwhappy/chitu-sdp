package com.chitu.bigdata.sdp.service.monitor;

import com.alibaba.fastjson.JSON;
import com.chitu.bigdata.sdp.api.enums.JobStatus;
import com.chitu.bigdata.sdp.api.enums.JobStatusAlertType;
import com.chitu.bigdata.sdp.api.enums.NotifiType;
import com.chitu.bigdata.sdp.api.enums.NotifiUserType;
import com.chitu.bigdata.sdp.api.model.SdpJob;
import com.chitu.bigdata.sdp.api.model.SdpJobAlertRule;
import com.chitu.bigdata.sdp.api.model.SdpProject;
import com.chitu.bigdata.sdp.api.model.SdpUser;
import com.chitu.bigdata.sdp.service.UserService;
import com.chitu.bigdata.sdp.service.notify.EmailNotifyService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * @author sutao
 * @create 2021-11-16 9:56
 */
@Service
@Slf4j
@RefreshScope
public class RuleAlertNotifyService {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailNotifyService emailNotifyService;

    /**
     * 告警
     *
     * @param alertTime       时间
     * @param employeeNumbers 人
     * @param notifiTypes     通知类型
     * @param jobInfo         事件属性
     * @param jobAlertRule    事件属性
     * @return 返回不同类型消息发送是否成功状态
     */
    public Map<String, Boolean> alert(String alertTime, List<String> employeeNumbers, List<String> notifiTypes, SdpJob jobInfo, SdpJobAlertRule jobAlertRule,NotifiUserType notifiUserType,Integer alertNum) {

        Map<String, Boolean> sendFlagMap = new HashMap<>(3);
        //获取用户信息
        List<SdpUser> sdpUsers = userService.queryListByNumbers(employeeNumbers);
        //消息发送状态
        Boolean sendFlag;
        for (String notifiType : notifiTypes) {
            if (NotifiType.EMAIL.toString().equals(notifiType)) {
                //通知默认添加项目+系统配置名单
                List<String> adminEmails = userService.queryReceiveUserList(jobInfo.getProject().getId(), notifiUserType).stream().map(SdpUser::getEmail).collect(Collectors.toList());
                adminEmails.addAll(sdpUsers.stream().map(SdpUser::getEmail).distinct().collect(Collectors.toList()));
                List<String> customEmails = adminEmails.stream().distinct().collect(Collectors.toList());
                sendFlag = emailNotifyService.sendMsg(customEmails,jobAlertRule.getRuleName(), JSON.toJSONString(buildTemplateParam(alertTime, jobInfo.getProject(), jobInfo, jobAlertRule, alertNum)));
                sendFlagMap.put(notifiType, sendFlag);
            }
        }

        return sendFlagMap;

    }


    /**
     * 构建告警事件模板参数
     *
     * @param alertTime
     * @param projectInfo
     * @param jobInfo
     * @param jobAlertRule
     * @return
     */
    public Map<String, String> buildTemplateParam(String alertTime, SdpProject projectInfo, SdpJob jobInfo, SdpJobAlertRule jobAlertRule,Integer alertNum) {
        Map<String, String> templateParamMap = new HashMap<>(10);
        String triggerTime = null;
        if (Objects.nonNull(alertNum) && alertNum > 1){
            StringBuffer alertStr = new StringBuffer();
            alertStr.append(alertTime).append(" 连续预警第").append(alertNum).append("次");
            triggerTime = alertStr.toString();
        }
        templateParamMap.put("projectName", projectInfo.getProjectName());
        templateParamMap.put("jobName", jobInfo.getJobName());
        if (Objects.nonNull(jobAlertRule.getJobStatusAlertType()) && JobStatusAlertType.JOB_STATUS_CHANGE.name().equals(jobAlertRule.getJobStatusAlertType())) {
            templateParamMap.put("statusChange", String.format("由%s变更为%s", JobStatus.fromStatus(jobInfo.getJobStatus() == null ? "" : jobInfo.getJobStatus()).getStatus(), JobStatus.fromStatus(jobInfo.getNewStatus() == null ? "" : jobInfo.getNewStatus()).getStatus()));
            templateParamMap.put("updatedBy", jobInfo.getUpdatedBy());
        } else {
            templateParamMap.put("ruleName", jobAlertRule.getRuleName());
            templateParamMap.put("owner", jobInfo.getOwner());
        }
        if (Objects.isNull(triggerTime)) {
            templateParamMap.put("triggerTime", alertTime);
        }else{
            templateParamMap.put("triggerTime",triggerTime);
        }
        return templateParamMap;
    }


}
