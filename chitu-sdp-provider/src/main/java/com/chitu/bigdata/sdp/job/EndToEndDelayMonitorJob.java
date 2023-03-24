package com.chitu.bigdata.sdp.job;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chitu.bigdata.sdp.api.bo.PromResultInfo;
import com.chitu.bigdata.sdp.api.domain.JobConfig;
import com.chitu.bigdata.sdp.api.enums.NotifiUserType;
import com.chitu.bigdata.sdp.api.model.SdpJob;
import com.chitu.bigdata.sdp.api.model.SdpJobInstance;
import com.chitu.bigdata.sdp.api.model.SdpProject;
import com.chitu.bigdata.sdp.api.model.SdpUser;
import com.chitu.bigdata.sdp.config.CustomMonitorConfigProperties;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.constant.FlinkConfigKeyConstant;
import com.chitu.bigdata.sdp.constant.PromConstant;
import com.chitu.bigdata.sdp.interceptor.EnvHolder;
import com.chitu.bigdata.sdp.mapper.SdpJobInstanceMapper;
import com.chitu.bigdata.sdp.mapper.SdpJobMapper;
import com.chitu.bigdata.sdp.mapper.SdpProjectMapper;
import com.chitu.bigdata.sdp.service.UserService;
import com.chitu.bigdata.sdp.service.monitor.PrometheusQueryService;
import com.chitu.bigdata.sdp.service.notify.EmailNotifyService;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.chitu.bigdata.sdp.utils.YamlUtils;
import com.chitu.cloud.config.scheduler.RedisLeaderSchedule;
import com.xiaoleilu.hutool.date.DateUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 端到端延迟监控任务
 * <p>
 * 如果需要端到端延迟监控，需要在高级配置加上该属性metrics.latency.interval: 300000 。
 * 这个间隔尽量设置长一点（5分钟=300000毫秒），这个发送LatencyMarker间隔太短的话，会影响任务性能。
 * </p>
 *
 * @author zouchangzhen
 * @date 2022/7/18
 */
@Slf4j
@Component
public class EndToEndDelayMonitorJob {

    @Autowired
    SdpJobInstanceMapper sdpJobInstanceMapper;
    @Autowired
    PrometheusQueryService prometheusQueryService;

    @Autowired
    private EmailNotifyService emailNotifyService;

    @Autowired
    private UserService userService;
    @Autowired
    SdpJobMapper sdpJobMapper;
    @Autowired
    SdpProjectMapper sdpProjectMapper;
    @Autowired
    private RedisTemplate<String, String> redisTmplate;
    @Autowired
    SdpConfig sdpConfig;
    @Autowired
    CustomMonitorConfigProperties customMonitorConfigProperties;

    private final static String MSG = "===EndToEndDelay[{}]";
    private final static String REDIS_KEY = "monitor:endtoend:";

    @RedisLeaderSchedule
    @Scheduled(fixedDelayString = "${custom.endToEndDelayMonitorJob.fixedDelay:300000}")
    public void delayMonitor() {
        String osName = System.getProperties().getProperty("os.name");
        if (osName.equals("Windows 10")) {
            return;
        }
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
        CustomMonitorConfigProperties.EndToEndDelayMonitorConfig endToEndDelayMonitorJob = customMonitorConfigProperties.getCustomConfigAndEnv().getCustomConfig().getEndToEndDelayMonitorJob();
        if (!endToEndDelayMonitorJob.isMSwitch()) {
            log.info(MSG, "端到端延迟监控预警未启用...");
            return;
        }
        if (Objects.isNull(endToEndDelayMonitorJob.getThreshold())) {
            log.info(MSG, "端到端延迟监控预警阈值未配置...");
            return;
        }
        if (endToEndDelayMonitorJob.getThreshold() <= 0) {
            log.info(MSG, "端到端延迟监控预警阈值必须大于0才生效...");
            return;
        }
        List<SdpJobInstance> sdpJobInstances = sdpJobInstanceMapper.queryRunningJob();
        if (CollectionUtils.isEmpty(sdpJobInstances)) {
            return;
        }

        for (SdpJobInstance sdpJobInstance : sdpJobInstances) {
            if (Objects.isNull(sdpJobInstance) || StrUtil.isBlank(sdpJobInstance.getConfigContent())) {
                log.info(MSG + ": {}", sdpJobInstance.getId(), "高级配置内容为空不处理");
                continue;
            }
            long start = System.currentTimeMillis();
            try {
                JobConfig jobConfig = JSONObject.parseObject(sdpJobInstance.getConfigContent(), JobConfig.class);
                Map<String, String> yamlMap = YamlUtils.parseYaml(jobConfig.getFlinkYaml());
                String interval = yamlMap.getOrDefault(FlinkConfigKeyConstant.METRICS_LATENCY_INTERVAL, "");
                if (StrUtil.isBlank(interval)) {
                    if (log.isTraceEnabled()) {
                        log.trace(MSG + ": {}", sdpJobInstance.getId(), "端到端延迟监控没有在高级配置中配置");
                    }
                    continue;
                }

                //根据source_id分组查询最大值
                String promQL = String.format("max by(source_id)(%s{job_id=\"%s\",quantile=\"0.999\"})", PromConstant.JOB_LATENCY, sdpJobInstance.getFlinkJobId());
                List<PromResultInfo> metrics = prometheusQueryService.getMetrics(promQL);

                if (org.apache.commons.collections.CollectionUtils.isEmpty(metrics)) {
                    log.info(MSG + ": {}", sdpJobInstance.getId(), "prometheus返回值为空,不处理");
                    continue;
                }

                for (PromResultInfo metric : metrics) {
                    if (Objects.isNull(metric)) {
                        continue;
                    }
                    String[] values = metric.getValue();
                    if (Objects.isNull(values) || values.length <= 1) {
                        continue;
                    }

                    if (Integer.valueOf(values[1]) <= endToEndDelayMonitorJob.getThreshold()) {
                        if (log.isTraceEnabled()) {
                            log.trace(MSG + ": {} - [{}]", sdpJobInstance.getId(), "小于阈值不处理",JSON.toJSONString(metric));
                        }
                        continue;
                    }

                    String key = REDIS_KEY + sdpJobInstance.getId();
                    String val = redisTmplate.opsForValue().get(key);
                    if (StrUtil.isBlank(val)) {
                        SdpJob sdpJob = sdpJobMapper.selectById(sdpJobInstance.getJobId());
                        SdpProject sdpProject = sdpProjectMapper.selectById(sdpJob.getProjectId());

                        List<String> adminEmails = userService.queryReceiveUserList(NotifiUserType.ALARM_NOTICE_USERS).stream().map(SdpUser::getEmail).collect(Collectors.toList());
                        Map<String, String> templateParamMap = new HashMap<>(10);
                        templateParamMap.put("projectName", sdpProject.getProjectName());
                        templateParamMap.put("jobName", sdpJob.getJobName());
                        templateParamMap.put("ruleName", String.format("%s端到端延迟超过%d毫秒", sdpJob.getJobName(), endToEndDelayMonitorJob.getThreshold()));
                        templateParamMap.put("triggerTime", DateUtil.formatDateTime(new Date()));
                        boolean sendFlag = emailNotifyService.sendMsg(adminEmails,templateParamMap.get("ruleName"),JSON.toJSONString(templateParamMap));
                        if (sendFlag) {
                            redisTmplate.opsForValue().set(key, String.valueOf(System.currentTimeMillis()), endToEndDelayMonitorJob.getIntervalTime(), TimeUnit.MINUTES);
                        }
                        log.info(MSG + ": 超过阈值[{}]预警,指标：{}", sdpJobInstance.getId(), endToEndDelayMonitorJob.getThreshold(), JSON.toJSONString(metric));
                    } else {
                        log.info(MSG + ": {}分钟内不连续发告警", sdpJobInstance.getId(), endToEndDelayMonitorJob.getIntervalTime());
                    }

                    //如果存在多个sql同步的情况，只预警一次
                    break;
                }
            } catch (Exception e) {
                String errMsg = StrUtils.parse1(MSG + ": 异常", sdpJobInstance.getId());
                log.error(errMsg, e);
            }finally {
                if (log.isTraceEnabled()) {
                    log.trace(MSG + ": 耗时 -> {}", sdpJobInstance.getId(), (System.currentTimeMillis() - start));
                }
            }
        }
    }
}
