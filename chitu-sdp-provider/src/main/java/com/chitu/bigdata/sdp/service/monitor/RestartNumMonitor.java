package com.chitu.bigdata.sdp.service.monitor;

import com.chitu.bigdata.sdp.api.domain.RuleCehckResp;
import com.chitu.bigdata.sdp.api.model.RuleContent;
import com.chitu.bigdata.sdp.api.model.SdpJob;
import com.chitu.bigdata.sdp.constant.PromConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author sutao
 * @create 2021-11-15 15:46
 * 重启次数监控
 */
@Component("NUMBER_RESTARTS")
@Slf4j
public class RestartNumMonitor extends AbstractRuleIndexMonitor {

    @Autowired
    private PrometheusQueryService prometheusQueryService;


    @Override
    protected RuleCehckResp checkMonitorIndexValue(SdpJob jobInfo, RuleContent ruleContent) {
        String promQL = String.format("delta(%s{job_id=\"%s\"}[%s])", PromConstant.NUMRESTARTS, jobInfo.getJobInstance().getFlinkJobId(), ruleContent.getTimeDiff() + "m");
        Long value = prometheusQueryService.getMetricSingleValue(promQL);
        if(value == null){
            return RuleCehckResp.builder().hisFalg(false).build();
        }
        boolean flag = super.checkHitRule(ruleContent, value);
        String desc = String.format("%d分钟内重启%d次", ruleContent.getTimeDiff(), value);
        return RuleCehckResp.builder().hisFalg(flag).desc(desc).build();
    }


}
