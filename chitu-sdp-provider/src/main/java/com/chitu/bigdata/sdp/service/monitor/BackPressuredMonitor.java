package com.chitu.bigdata.sdp.service.monitor;

import com.alibaba.fastjson.JSONObject;
import com.chitu.bigdata.sdp.api.bo.PromResponceInfo;
import com.chitu.bigdata.sdp.api.bo.PromResultInfo;
import com.chitu.bigdata.sdp.api.domain.RuleCehckResp;
import com.chitu.bigdata.sdp.api.model.RuleContent;
import com.chitu.bigdata.sdp.api.model.SdpJob;
import com.chitu.bigdata.sdp.constant.PromConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author sutao
 * @create 2022-03-11 16:04
 * 背压监控
 */
@Component("BACKPRESSURED")
@Slf4j
public class BackPressuredMonitor extends AbstractRuleIndexMonitor {

    @Autowired
    private PrometheusQueryService prometheusQueryService;

    /**
     * 出现背压时的值
     */
    private final Long BACKPRESSURED_TIMEMSPERSECOND = 900L;


    @Override
    protected RuleCehckResp checkMonitorIndexValue(SdpJob jobInfo, RuleContent ruleContent) {
        AtomicBoolean ruleHitflag = new AtomicBoolean(false);
        String promQL = String.format("avg_over_time(%s{job_id=\"%s\"}[%s])", PromConstant.BACKPRESSURED, jobInfo.getJobInstance().getFlinkJobId(), ruleContent.getTimeDiff() + "m");
        PromResponceInfo promResponceInfo = prometheusQueryService.metricsQuery(promQL);
        if (promResponceInfo == null || !PromConstant.SUCCESS.equals(promResponceInfo.getStatus())) {
            log.error("prometheus接口查询数据，返回状态异常，[{}]", JSONObject.toJSONString(promResponceInfo));
            return RuleCehckResp.builder().hisFalg(ruleHitflag.get()).build();
        }
        List<PromResultInfo> resultList = promResponceInfo.getData().getResult();
        resultList.forEach(item -> {
            if (item.getValue() != null && item.getValue().length > 0) {
                Long value = Double.valueOf(item.getValue()[1]).longValue();
                if (value >= BACKPRESSURED_TIMEMSPERSECOND) {
                    ruleHitflag.set(true);
                    return;
                }
            }
        });
        String desc = String.format("任务背压已持续%s分钟以上", ruleContent.getTimeDiff());
        return RuleCehckResp.builder().hisFalg(ruleHitflag.get()).desc(desc).build();
    }
}
