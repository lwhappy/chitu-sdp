package com.chitu.bigdata.sdp.job;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.chitu.bigdata.sdp.api.bo.PromResultInfo;
import com.chitu.bigdata.sdp.api.enums.NotifiUserType;
import com.chitu.bigdata.sdp.api.model.SdpUser;
import com.chitu.bigdata.sdp.config.CustomMonitorConfigProperties;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.constant.PromConstant;
import com.chitu.bigdata.sdp.constant.RedisKeyConstant;
import com.chitu.bigdata.sdp.interceptor.EnvHolder;
import com.chitu.bigdata.sdp.service.UserService;
import com.chitu.bigdata.sdp.service.monitor.PrometheusQueryService;
import com.chitu.bigdata.sdp.service.notify.EmailNotifyService;
import com.chitu.cloud.config.scheduler.RedisLeaderSchedule;
import com.xiaoleilu.hutool.date.DateUtil;
import com.xiaoleilu.hutool.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author sutao
 * @create 2022-06-10 11:33
 */
@Component
@Slf4j
public class HbaseCDCTableMonitorJob {


    @Autowired
    private PrometheusQueryService prometheusQueryService;

    @Autowired
    private CustomMonitorConfigProperties customMonitorConfig;

    @Autowired
    private RedisTemplate<String, String> redisTmplate;

    @Autowired
    private EmailNotifyService emailNotifyService;

    @Autowired
    private UserService userService;

    @Autowired
    SdpConfig sdpConfig;

    @RedisLeaderSchedule
    @Scheduled(fixedDelayString = "${custom.tableDelayAlertRule.scheduledTime}")
    public void tableDelayMonitor() {
        List<String> envList = sdpConfig.getEnvList();
        for (String env : envList) {
            EnvHolder.addEnv(env);
            try {
                handleSingleEnv4TableDelay();
            }finally {
                EnvHolder.clearEnv();
            }
        }
    }

    @RedisLeaderSchedule
    @Scheduled(fixedDelayString = "${custom.tableNullSizeAlertRule.scheduledTime}")
    public void tableNullSizeMonitor() {
        List<String> envList = sdpConfig.getEnvList();
        for (String env : envList) {
            EnvHolder.addEnv(env);
            try {
                handleSingleEnv4TableNull();
            }finally {
                EnvHolder.clearEnv();
            }
        }
    }

    private void handleSingleEnv4TableDelay() {
        try {
            CustomMonitorConfigProperties.TableDelayAlertRule tableDelayAlertRule = customMonitorConfig.getCustomConfigAndEnv().getCustomConfig().getTableDelayAlertRule();
            if (tableDelayAlertRule.getStartSwitch() == 0) {
                log.info("cdc延迟监控未启用...");
                return;
            }
            String promQL = PromConstant.HBASE_CDC_TABLE_DELAY;
            List<PromResultInfo> promResultInfoList = prometheusQueryService.getMetrics(promQL);
            if(CollectionUtil.isEmpty(promResultInfoList)){
                return;
            }
            log.info("【cdc延迟监控开始】，当前表数量[{}]", promResultInfoList.size());
            long starTime = System.currentTimeMillis();
            promResultInfoList.forEach(item -> {
                if (item.getValue() != null && item.getValue().length > 0) {
                    String hbaseTable = item.getMetric().getHbase_table();
                    // 秒
                    Long indexValue = Long.valueOf(item.getValue()[1]) / 1000;
                    // 触发规则
                    if (indexValue >= tableDelayAlertRule.getThreshold()) {
                        String key = RedisKeyConstant.MONITOR_LAST_TABLE_DELAY_TIME.getKey() + hbaseTable;
                        String v = redisTmplate.opsForValue().get(key);
                        // TODO 当cdc表的推送任务停止后，prometheus还会去拉取值，通过保存历史记录值来校验，避免误告警
                        String hisKey = RedisKeyConstant.MONITOR_LAST_TABLE_DELAY_VALUE.getKey() + hbaseTable;
                        String hisv = redisTmplate.opsForValue().get(hisKey);
                        if (StrUtil.isEmpty(v) && !indexValue.toString().equals(hisv)) {
                            List<String> adminEmails = userService.queryReceiveUserList(NotifiUserType.ALARM_NOTICE_USERS).stream().map(SdpUser::getEmail).collect(Collectors.toList());
                            Map<String, String> templateParamMap = new HashMap<>(10);
                            templateParamMap.put("projectName", "hbase_cdc数据同步");
                            templateParamMap.put("jobName", "hbase_cdc_sync_ds");
                            templateParamMap.put("ruleName", String.format("%s延迟超过%d秒", hbaseTable, tableDelayAlertRule.getThreshold()));
                            templateParamMap.put("triggerTime", DateUtil.formatDateTime(new Date()));
                            boolean sendFlag = emailNotifyService.sendMsg(adminEmails, templateParamMap.get("ruleName"), JSON.toJSONString(templateParamMap));
                            if (sendFlag) {
                                redisTmplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()), tableDelayAlertRule.getIntervalTime(), TimeUnit.MINUTES);
                                redisTmplate.opsForValue().set(hisKey, String.valueOf(indexValue));
                            }
                        }
                    }
                }
            });

            long endTime = System.currentTimeMillis();
            log.info("【cdc延迟监控结束】，总耗时[{}]", (endTime - starTime));
        } catch (Exception e) {
            log.error("cdc延迟监控异常", e);
        }
    }




    private void handleSingleEnv4TableNull() {
        try {
            CustomMonitorConfigProperties.TableNullSizeAlertRule tableNullSizeAlertRule = customMonitorConfig.getCustomConfigAndEnv().getCustomConfig().getTableNullSizeAlertRule();
            if (tableNullSizeAlertRule.getStartSwitch() == 0) {
                log.info("cdc反查为空监控未启用...");
                return;
            }
            String suffixUrl = "label/hbase_table/values";
            List<String> tableNameList = prometheusQueryService.getLableValues(suffixUrl);
            if(CollectionUtil.isEmpty(tableNameList)){
                return;
            }
            log.info("【cdc反查为空监控开始】，当前表数量[{}]", tableNameList.size());
            long starTime = System.currentTimeMillis();
            tableNameList.forEach(hbaseTable -> {
                String promQL = String.format("delta(%s{hbase_table=\"%s\"}[%s])", PromConstant.HBASE_CDC_TABLENULLSIZE, hbaseTable, tableNullSizeAlertRule.getDiffTime() + "m");
                Long metricSingleValue = prometheusQueryService.getMetricSingleValue(promQL);
                if (metricSingleValue == null) {
                    return;
                }
                // 触发规则
                if (metricSingleValue >= tableNullSizeAlertRule.getThreshold()) {
                    String key = RedisKeyConstant.MONITOR_LAST_TABLE_NULLSIZE.getKey() + hbaseTable;
                    String v = redisTmplate.opsForValue().get(key);
                    if (StrUtil.isEmpty(v)) {
                        List<String> adminEmails = userService.queryReceiveUserList(NotifiUserType.ALARM_NOTICE_USERS).stream().map(SdpUser::getEmail).collect(Collectors.toList());
                        Map<String, String> templateParamMap = new HashMap<>(10);
                        templateParamMap.put("projectName", "hbase_cdc数据同步");
                        templateParamMap.put("jobName", "hbase_cdc_sync_ds");
                        templateParamMap.put("ruleName", String.format("%s最近%d分钟反查为空%d条",
                                hbaseTable,
                                tableNullSizeAlertRule.getDiffTime(),
                                tableNullSizeAlertRule.getThreshold()
                        ));
                        templateParamMap.put("triggerTime", DateUtil.formatDateTime(new Date()));
                        boolean sendFlag = emailNotifyService.sendMsg(adminEmails, templateParamMap.get("ruleName"), JSON.toJSONString(templateParamMap));
                        if (sendFlag) {
                            redisTmplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()), tableNullSizeAlertRule.getIntervalTime(), TimeUnit.MINUTES);
                        }
                    }
                }
            });
            long endTime = System.currentTimeMillis();
            log.info("【cdc反查为空监控结束】，总耗时[{}]", (endTime - starTime));
        } catch (Exception e) {
            log.error("cdc反查为空监控异常", e);
        }
    }


}
