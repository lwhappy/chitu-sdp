package com.chitu.bigdata.sdp.service.monitor;

import com.chitu.bigdata.sdp.api.domain.RuleCehckResp;
import com.chitu.bigdata.sdp.api.model.RuleContent;
import com.chitu.bigdata.sdp.api.model.SdpJob;
import com.chitu.bigdata.sdp.api.model.SdpMetaTableConfig;
import com.chitu.bigdata.sdp.constant.PromConstant;
import com.chitu.bigdata.sdp.service.MetaTableConfigService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author sutao
 * @create 2022-03-09 19:29
 * 最近N分钟消费数监控
 */
@Component("CONSUME_RECORDS")
@Slf4j
public class ConsumeRecordsMonitor extends AbstractRuleIndexMonitor {

    @Autowired
    private PrometheusQueryService prometheusQueryService;

    @Autowired
    private MetaTableConfigService metaTableConfigService;


    @Override
    protected RuleCehckResp checkMonitorIndexValue(SdpJob jobInfo, RuleContent ruleContent) {
        AtomicBoolean ruleHitflag = new AtomicBoolean(false);
        Set<String> ruleHitTable = new HashSet<>();
        String promQL = String.format("delta(%s{job_id=\"%s\"}[%s])", PromConstant.RECORDS_CONSUMED_TOTAL, jobInfo.getJobInstance().getFlinkJobId(), ruleContent.getTimeDiff() + "m");
        Map<String, Long> metricTableValue = prometheusQueryService.getMetricTableValueSum(promQL);
        metricTableValue.forEach((t, v) -> {
            if (super.checkHitRule(ruleContent, v)) {
                ruleHitflag.set(true);
                ruleHitTable.add(t);
            }
        });
        StringBuilder sb = new StringBuilder();
        if (ruleHitflag.get()) {
            List<SdpMetaTableConfig> metaTableConfigList = metaTableConfigService.selectAll(new SdpMetaTableConfig(jobInfo.getFileId()));
            ruleHitTable.forEach(t -> {
                metaTableConfigList.stream().forEach(item -> {
                    if (t.equals(item.getFlinkTableName())) {
                        sb.append(String.format("topic:%s最近%d分钟消费数据%d条;", item.getMetaTableName(), ruleContent.getTimeDiff(), metricTableValue.get(t)));
                    }
                });
            });
        }
        return RuleCehckResp.builder().hisFalg(ruleHitflag.get()).desc(sb.toString()).build();
    }

}
