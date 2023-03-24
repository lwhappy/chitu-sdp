package com.chitu.bigdata.sdp.service.monitor;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.chitu.bigdata.sdp.api.domain.RuleCehckResp;
import com.chitu.bigdata.sdp.api.enums.AlertEffectiveStatus;
import com.chitu.bigdata.sdp.api.enums.NotifiUserType;
import com.chitu.bigdata.sdp.api.enums.Operators;
import com.chitu.bigdata.sdp.api.model.*;
import com.chitu.bigdata.sdp.config.CustomMonitorConfigProperties;
import com.chitu.bigdata.sdp.constant.RedisKeyConstant;
import com.chitu.bigdata.sdp.service.SdpJobAlertRecordService;
import com.chitu.bigdata.sdp.service.UserService;
import com.chitu.bigdata.sdp.utils.DateUtils;
import com.xiaoleilu.hutool.date.DateUnit;
import com.xiaoleilu.hutool.date.DateUtil;
import com.xiaoleilu.hutool.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author sutao
 * @create 2021-11-15 15:43
 */
@Slf4j
public abstract class AbstractRuleIndexMonitor {

    @Autowired
    private RuleAlertNotifyService alertNotifyService;

    @Autowired
    private SdpJobAlertRecordService jobAlertRecordService;

    @Autowired
    private UserService userService;

    @Autowired
    private RedisTemplate<String, String> redisTmplate;

    @Autowired
    private CustomMonitorConfigProperties customMonitorConfig;

    @Value("${custom.time.fixedDelay}")
    private Integer monitorTime;

    /**
     * 指标监控方法
     *
     * @param jobInfo
     * @param jobAlertRule
     */
    public final void monitor(SdpJob jobInfo, SdpJobAlertRule jobAlertRule) {

        if (!ruleValid(jobInfo, jobAlertRule)) {
            return;
        }

        RuleContent ruleContent = JSON.parseObject(jobAlertRule.getRuleContent(), RuleContent.class);
        RuleCehckResp ruleCehckResp = checkMonitorIndexValue(jobInfo, ruleContent);
        if (!ruleCehckResp.isHisFalg()) {
            return;
        }
        log.warn("触发告警：job[{}]，命中规则[{}]，事件描述[{}]", jobInfo.getJobName(), jobAlertRule.getRuleContent(), ruleCehckResp.getDesc());

        // 告警事件时间
        String alertTime = DateUtil.formatDateTime(new Date());
        Integer alertNum = 1;
        // 校验告警频率：连续N分钟内只发一次告警
        if (!checkSatisfyAlertRate(alertTime, jobInfo, jobAlertRule)) {
            return;
        }

        alertNum = getAlertNum(jobInfo, jobAlertRule, alertTime, alertNum);


        // 开始发送告警
        Map<String, Boolean> sendFlagMap = sendAlert(alertTime, jobInfo, jobAlertRule,alertNum);
        sendFlagMap.forEach((notifiType, flag) -> {
            if (!flag) {
                return;
            }
            String key = RedisKeyConstant.MONITOR_ALERT.getKey() + jobInfo.getId() + ":" + jobAlertRule.getId() + ":" + notifiType.toLowerCase();
            redisTmplate.opsForValue().set(key, alertTime, 1, TimeUnit.DAYS);
        });

        //TODO 目前没有保存通知类型发送是否成功状态，即不管发送成功失败都会保存db告警记录
        saveJobAlertRecord(alertTime, ruleCehckResp.getDesc(), jobInfo, jobAlertRule,alertNum);

    }

    public Integer getAlertNum(SdpJob jobInfo, SdpJobAlertRule jobAlertRule, String alertTime, Integer alertNum) {
        //根据上一次的预警时间判断是否已进行处理
        List<SdpJobAlertRecord> jobAlertRecordList = jobAlertRecordService.selectAll(new SdpJobAlertRecord(jobInfo.getId(), jobAlertRule.getId()));
        if (CollectionUtil.isNotEmpty(jobAlertRecordList)) {
            // 取最新告警时间
            SdpJobAlertRecord jobAlertRecord = jobAlertRecordList.stream().max(Comparator.comparing(SdpJobAlertRecord::getAlertTime)).get();
            long betweenMin = DateUtil.between(DateUtil.parseDateTime(alertTime), DateUtil.date(jobAlertRecord.getAlertTime().getTime()), DateUnit.MINUTE);
            // 当预警的时间超过设定预警频率加1分钟，则任务是有处理
            if (betweenMin < jobAlertRule.getAlertRate() + 2*TimeUnit.MILLISECONDS.toMinutes(monitorTime)) {
                if (Objects.nonNull(jobAlertRecord.getAlertNum())) {
                    alertNum = jobAlertRecord.getAlertNum() + 1;
                }
            }
        }
        return alertNum;
    }


    /**
     * 校验是否满足告警频率
     *
     * @param alertTime
     * @param jobInfo
     * @param jobAlertRule
     * @return
     */
    protected boolean checkSatisfyAlertRate(String alertTime, SdpJob jobInfo, SdpJobAlertRule jobAlertRule) {
        if(StringUtils.isEmpty(jobAlertRule.getNotifiType())){
            return false;
        }
        List<String> notifiTypes = Stream.of(jobAlertRule.getNotifiType().split(",")).collect(Collectors.toList());
        notifiTypes = notifiTypes.stream().filter(notifiType -> {
            String key = RedisKeyConstant.MONITOR_ALERT.getKey() + jobInfo.getId() + ":" + jobAlertRule.getId() + ":" + notifiType.toLowerCase();
            String hisAlertTime = redisTmplate.opsForValue().get(key);
            //如果已经发送过告警
            if (StrUtil.isNotEmpty(hisAlertTime)) {
                //校验时间差
                long betweenMin = DateUtil.between(DateUtil.parseDateTime(alertTime), DateUtil.parseDateTime(hisAlertTime), DateUnit.MINUTE);
                if (betweenMin < jobAlertRule.getAlertRate()) {
                    log.warn("redis存在告警记录，key[{}]，value[{}]，时间分钟差[{}]，告警频率[{}]，不发送告警.", key, hisAlertTime, betweenMin, jobAlertRule.getAlertRate());
                    return false;
                }
            }
            return true;
        }).collect(Collectors.toList());

        if (CollectionUtil.isEmpty(notifiTypes)) {
            return false;
        }
        // 重新赋值
        // 场景1：当其中一种通知类型发送失败时(redis中不存在告警记录)，支持再次发送
        // 场景2：当其中一种通知类型已经发送过且在告警频率时间内，则不发送告警
        jobAlertRule.setNotifiType(StringUtils.join(notifiTypes, ","));
        return true;
    }

    /**
     * 规则校验
     *
     * @param jobAlertRule
     */
    private boolean ruleValid(SdpJob jobInfo, SdpJobAlertRule jobAlertRule) {
        //校验规则是否生效
        if (AlertEffectiveStatus.STOP.toString().equals(jobAlertRule.getEffectiveState())) {
            if (log.isDebugEnabled()) {
                log.debug("job[{}]，规则[{}]未启动.", jobInfo.getJobName(), jobAlertRule.getRuleName());
            }
            return false;
        }

        CustomMonitorConfigProperties.Time timeConf = customMonitorConfig.getCustomConfigAndEnv().getCustomConfig().getTime();

        //任务启动后，需要在时间差之后才做监控，避免启动时监控误告警
        long betweenMin = DateUtil.between(new Date(), DateUtil.date(jobInfo.getJobInstance().getStartTime().getTime()), DateUnit.MINUTE);
        if (betweenMin < timeConf.getFirstMonitorInterval()) {
            return false;
        }

        //校验是否在生效时间内
        String[] splitTimeArray = jobAlertRule.getEffectiveTime().split("-");
        boolean isEffective = DateUtils.isEffectiveDate(DateUtils.getCurrentDateHM(), splitTimeArray[0], splitTimeArray[1]);
        if (!isEffective) {
            log.info("job[{}]，规则[{}]，生效时间[{}]，不在生效时间内.", jobInfo.getJobName(), jobAlertRule.getRuleName(), jobAlertRule.getEffectiveTime());
            return false;
        }
        return true;
    }


    /**
     * 校验监控指标值
     *
     * @param jobInfo
     * @param ruleContent
     * @return
     */
    protected abstract RuleCehckResp checkMonitorIndexValue(SdpJob jobInfo, RuleContent ruleContent);


    /**
     * 校验是否命中规则
     *
     * @param ruleContent
     * @param indexValue
     * @return
     */
    protected boolean checkHitRule(RuleContent ruleContent, Long indexValue) {
        if (Operators.GREATER_THAN_EQUAL.toString().equals(ruleContent.getOperator())) {
            if (indexValue >= Long.parseLong(ruleContent.getThreshold())) {
                return true;
            }
        }
        if (Operators.LESS_THAN_EQUAL.toString().equals(ruleContent.getOperator())) {
            if (indexValue <= Long.parseLong(ruleContent.getThreshold())) {
                return true;
            }
        }
        return false;
    }


    /**
     * 告警
     *
     * @param jobInfo
     * @param jobAlertRule
     * @return 返回不同类型消息发送是否成功状态
     */
    protected Map<String, Boolean> sendAlert(String alertTime, SdpJob jobInfo, SdpJobAlertRule jobAlertRule,Integer alertNum) {
        List<Map> notifyUsers = JSON.parseArray(jobAlertRule.getNotifyUsers(), Map.class);
        List<String> employeeNumbers = notifyUsers.stream().map(item -> item.get("employee_number").toString()).collect(Collectors.toList());
        List<String> notifiTypes = Stream.of(jobAlertRule.getNotifiType().split(",")).collect(Collectors.toList());

        if(StrUtil.isEmpty(jobInfo.getOwner())) {
            // 赋值作业负责人(job更新人/告警通知第一人)
            if (StrUtil.isNotEmpty(jobInfo.getUpdatedBy())) {
                SdpUser sdpUser = userService.get(Long.valueOf(jobInfo.getUpdatedBy()));
                if (sdpUser != null) {
                    jobInfo.setOwner(sdpUser.getUserName());
                }
            } else if (CollectionUtil.isNotEmpty(employeeNumbers)) {
                List<SdpUser> sdpUsers = userService.selectAll(new SdpUser(employeeNumbers.get(0)));
                if (CollectionUtil.isNotEmpty(sdpUsers)) {
                    jobInfo.setOwner(sdpUsers.get(0).getUserName());
                }
            }
        }

        return alertNotifyService.alert(alertTime, employeeNumbers, notifiTypes, jobInfo, jobAlertRule, NotifiUserType.ALARM_NOTICE_USERS,alertNum);
    }


    protected void saveJobAlertRecord(String alertTime, String desc, SdpJob jobInfo, SdpJobAlertRule jobAlertRule,Integer alertNum) {
        // 校验规则是否再db中保存过，避免重复保存
        List<SdpJobAlertRecord> jobAlertRecordList = jobAlertRecordService.selectAll(new SdpJobAlertRecord(jobInfo.getId(), jobAlertRule.getId()));
        if (CollectionUtil.isNotEmpty(jobAlertRecordList)) {
            // 取最新告警时间
            SdpJobAlertRecord jobAlertRecord = jobAlertRecordList.stream().max(Comparator.comparing(SdpJobAlertRecord::getAlertTime)).get();
            long betweenMin = DateUtil.between(DateUtil.parseDateTime(alertTime), DateUtil.date(jobAlertRecord.getAlertTime().getTime()), DateUnit.MINUTE);
            // 如果当前这次告警时间-历史最近告警时间
            if (betweenMin < jobAlertRule.getAlertRate()) {
                log.info("db存在告警记录，job[{}]，规则[{}]，时间分钟差[{}]，告警频率[{}]，不保存告警记录", jobInfo.getId(), jobAlertRule.getJobId(), betweenMin, jobAlertRule.getAlertRate());
                return;
            }
        }
        SdpJobAlertRecord jobAlertRecord = new SdpJobAlertRecord();
        jobAlertRecord.setJobId(jobInfo.getId());
        jobAlertRecord.setRuleId(jobAlertRule.getId());
        jobAlertRecord.setAlertTime(DateUtil.parseDateTime(alertTime).toTimestamp());
        jobAlertRecord.setAlertContent(desc);
        jobAlertRecord.setCreatedBy("System");
        jobAlertRecord.setCreationDate(new Timestamp(System.currentTimeMillis()));
        jobAlertRecord.setUpdatedBy("System");
        jobAlertRecord.setUpdationDate(new Timestamp(System.currentTimeMillis()));
        jobAlertRecord.setEnabledFlag(1L);
        jobAlertRecord.setAlertNum(alertNum);
        jobAlertRecordService.getMapper().insert(jobAlertRecord);
    }


}
