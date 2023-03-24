package com.chitu.bigdata.sdp.service.notify;

import com.alibaba.fastjson.JSON;
import com.chitu.bigdata.sdp.api.enums.ApproveNotifyType;
import com.chitu.bigdata.sdp.api.enums.ApproveStatus;
import com.chitu.bigdata.sdp.api.model.SdpApprove;
import com.chitu.bigdata.sdp.api.model.SdpUser;
import com.chitu.bigdata.sdp.service.UserService;
import com.xiaoleilu.hutool.date.DateUtil;
import com.xiaoleilu.hutool.util.StrUtil;
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
 * @author 587694
 * @create 2021-1-21 9:56
 *
 */
@Service
@Slf4j
@RefreshScope
public class ApproveNotifyService {


    @Autowired
    private UserService userService;

    @Autowired
    private EmailNotifyService emailNotifyService;

    public Boolean notifySend(List<String> employeeNumbers, SdpApprove sdpApprove, String type) {
        List<SdpUser> sdpUsers = userService.queryListByNumbers(employeeNumbers);
        List<String> userEmails = sdpUsers.stream().map(SdpUser::getEmail).distinct().collect(Collectors.toList());
        Boolean sendFlag = emailNotifyService.sendMsg(userEmails,"审核通知", JSON.toJSONString(buildApproveParam(sdpApprove, type)));
        return sendFlag;
    }

    /**
     * 构建审核模板参数
     *
     * @param sdpApprove
     * @return
     */
    private Map<String, String> buildApproveParam( SdpApprove sdpApprove,String type) {
        Map<String, String> templateParamMap = new HashMap<>(10);
        templateParamMap.put("projectName", sdpApprove.getProjectName());
        templateParamMap.put("jobName", sdpApprove.getJobName());
        if (ApproveNotifyType.SUBMIT.getType().equals(type)) {
            String createdBy = sdpApprove.getCreatedBy();
            if (StrUtil.isNotBlank(createdBy)) {
                SdpUser sdpUser = userService.get(Long.valueOf(createdBy));
                templateParamMap.put("approveApplicator", sdpUser.getUserName());
            }
            templateParamMap.put("approveStatus", ApproveStatus.of(sdpApprove.getStatus()).getStatus());
            templateParamMap.put("triggerTime", DateUtil.formatDateTime(sdpApprove.getCreationDate()));
        }else {
            if (Objects.nonNull(sdpApprove.getUpdatedBy2())) {
                templateParamMap.put("approveOpt", sdpApprove.getOpinion2());
                templateParamMap.put("approveStatus", ApproveStatus.of(sdpApprove.getStatus()).getStatus());
                templateParamMap.put("triggerTime", DateUtil.formatDateTime(sdpApprove.getUpdationDate2()));
            } else {
                templateParamMap.put("approveOpt", sdpApprove.getOpinion());
                templateParamMap.put("approveStatus", ApproveStatus.of(sdpApprove.getStatus()).getStatus());
                templateParamMap.put("triggerTime", DateUtil.formatDateTime(sdpApprove.getUpdationDate()));
            }
        }
        return templateParamMap;
    }
}
