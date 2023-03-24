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
 * @create 2022-03-09 19:26
 * 待消费数监控
 */
@Component("WAIT_RECORDS")
@Slf4j
public class WaitRecordsMonitor extends AbstractRuleIndexMonitor {

    @Autowired
    private PrometheusQueryService prometheusQueryService;

    @Autowired
    private MetaTableConfigService metaTableConfigService;


    @Override
    protected RuleCehckResp checkMonitorIndexValue(SdpJob jobInfo, RuleContent ruleContent) {
        AtomicBoolean ruleHitflag = new AtomicBoolean(false);
        Set<String> ruleHitTable = new HashSet<>();
        String promQL = String.format("%s{job_id=\"%s\"}", PromConstant.PENDINGRECORDS, jobInfo.getJobInstance().getFlinkJobId());
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
                        sb.append(String.format("topic:%s待消费数据%d条;", item.getMetaTableName(), metricTableValue.get(t)));
                    }
                });
            });
        }
        return RuleCehckResp.builder().hisFalg(ruleHitflag.get()).desc(sb.toString()).build();
    }

}
