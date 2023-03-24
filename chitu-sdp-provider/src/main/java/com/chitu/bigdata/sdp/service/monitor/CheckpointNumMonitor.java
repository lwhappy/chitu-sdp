package com.chitu.bigdata.sdp.service.monitor;

import com.chitu.bigdata.sdp.api.domain.RuleCehckResp;
import com.chitu.bigdata.sdp.api.model.RuleContent;
import com.chitu.bigdata.sdp.api.model.SdpJob;
import com.chitu.bigdata.sdp.constant.PromConstant;
import com.chitu.bigdata.sdp.constant.RedisKeyConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * @author sutao
 * @create 2021-11-15 15:48
 * 检查点次数监控
 */
@Component("NUMBER_CHECKPOINT")
@Slf4j
public class CheckpointNumMonitor extends AbstractRuleIndexMonitor {

    @Autowired
    private PrometheusQueryService prometheusQueryService;

    @Autowired
    private RedisTemplate<String, String> redisTmplate;


    /**
     * prometheus 重启后返回的异常值
     */
    private final String PROMETHEUS_REBOOT_ERROR_VALUE = "0";


    @Override
    protected RuleCehckResp checkMonitorIndexValue(SdpJob jobInfo, RuleContent ruleContent) {
        String promQL = String.format("delta(%s{job_id=\"%s\"}[%s])", PromConstant.NUMBER_OF_COMPLETED_CHECKPOINTS, jobInfo.getJobInstance().getFlinkJobId(), ruleContent.getTimeDiff() + "m");
        Long value = prometheusQueryService.getMetricSingleValue(promQL);
        if (value == null) {
            return RuleCehckResp.builder().hisFalg(false).build();
        }
        String key = RedisKeyConstant.MONITOR_LASTCK.getKey() + jobInfo.getId();
        String lastValue = redisTmplate.opsForValue().get(key);
        // 最近一次返回ck不等于0，当前返回为0则认为数据有异常
        if (!PROMETHEUS_REBOOT_ERROR_VALUE.equals(lastValue) && PROMETHEUS_REBOOT_ERROR_VALUE.equals(value.toString())) {
            return RuleCehckResp.builder().hisFalg(false).build();
        }
        // 保存最近一次返回ck到redis
        redisTmplate.opsForValue().set(key, value.toString(), 30, TimeUnit.DAYS);

        boolean flag = super.checkHitRule(ruleContent, value);
        String desc = String.format("%s分钟内检查点完成%s次", ruleContent.getTimeDiff(), value);
        return RuleCehckResp.builder().hisFalg(flag).desc(desc).build();
    }


}
