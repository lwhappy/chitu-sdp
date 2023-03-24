package com.chitu.bigdata.sdp.service.monitor;

import com.chitu.bigdata.sdp.api.domain.RuleCehckResp;
import com.chitu.bigdata.sdp.api.model.RuleContent;
import com.chitu.bigdata.sdp.api.model.SdpJob;
import com.chitu.bigdata.sdp.api.model.SdpMetaTableConfig;
import com.chitu.bigdata.sdp.config.CustomMonitorConfigProperties;
import com.chitu.bigdata.sdp.constant.PromConstant;
import com.chitu.bigdata.sdp.constant.RedisKeyConstant;
import com.chitu.bigdata.sdp.service.MetaTableConfigService;
import com.xiaoleilu.hutool.date.DateUnit;
import com.xiaoleilu.hutool.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author sutao
 * @create 2021-11-15 15:48
 * 消费延迟监控
 */
@Component("DELAY")
@Slf4j
public class TopicDelayMonitor extends AbstractRuleIndexMonitor {

    @Autowired
    private PrometheusQueryService prometheusQueryService;

    @Autowired
    private MetaTableConfigService metaTableConfigService;

    @Autowired
    private RedisTemplate<String, String> redisTmplate;

    @Autowired
    private CustomMonitorConfigProperties customMonitorConfig;


    @Override
    protected RuleCehckResp checkMonitorIndexValue(SdpJob jobInfo, RuleContent ruleContent) {
        AtomicBoolean ruleHitflag = new AtomicBoolean(false);
        Set<String> ruleHitTable = new HashSet<>();
        String promQL = String.format("%s{job_id=\"%s\"}", PromConstant.CURRENT_EMIT_EVENTTIMELAG, jobInfo.getJobInstance().getFlinkJobId());
        Map<String, Long> metricTableValue = prometheusQueryService.getMetricTableValueMax(promQL);
        metricTableValue.forEach((t, v) -> {
            Long maxlatencyMin = v / 1000 / 60;
            metricTableValue.put(t, maxlatencyMin);
            String key = RedisKeyConstant.MONITOR_LAST_DELAY.getKey();
            String hashKey = jobInfo.getId() + ":" + t;
            Object lastValue = redisTmplate.opsForHash().get(key, hashKey);
            // 命中规则 且 最近一次延迟和当前延迟值不相等 (由于kafka出现一次延迟告警后，没生产数据导致监控值一直处于延迟，避免误告警)
            if (super.checkHitRule(ruleContent, maxlatencyMin) && !maxlatencyMin.toString().equals(lastValue)) {
                redisTmplate.opsForHash().put(key, hashKey, maxlatencyMin.toString());
                // 如果实例启动时间在 firstMonitorInterval + 5 分钟之内则跳过告警 (因为任务启动时消费完，最新一条数据的时间戳是在阈值范围内，期间这个topic没有产生新数据，那么就会触发告警)
                // 对于刷历史数据 超过 firstMonitorInterval + 5 分钟时间还没消费完，还是会触发告警！
                long betweenMin = DateUtil.between(new Date(), DateUtil.date(jobInfo.getJobInstance().getStartTime().getTime()), DateUnit.MINUTE);

                CustomMonitorConfigProperties.Time timeConf = customMonitorConfig.getCustomConfigAndEnv().getCustomConfig().getTime();
                if (betweenMin > timeConf.getFirstMonitorInterval() + 5) {
                    ruleHitflag.set(true);
                    ruleHitTable.add(t);
                } else {
                    log.warn("job:{}, 实例启动时间差:{}, 在{}分钟之内, 命中规则不告警!", jobInfo.getJobName(), betweenMin, timeConf.getFirstMonitorInterval() + 5);
                }
            }
        });
        StringBuilder sb = new StringBuilder();
        if (ruleHitflag.get()) {
            List<SdpMetaTableConfig> metaTableConfigList = metaTableConfigService.selectAll(new SdpMetaTableConfig(jobInfo.getFileId()));
            ruleHitTable.forEach(t -> {
                metaTableConfigList.stream().forEach(item -> {
                    if (t.equals(item.getFlinkTableName())) {
                        sb.append(String.format("topic: %s 消费延迟%d分钟;", item.getMetaTableName(), metricTableValue.get(t)));
                    }
                });
            });
        }
        return RuleCehckResp.builder().hisFalg(ruleHitflag.get()).desc(sb.toString()).build();
    }


}
