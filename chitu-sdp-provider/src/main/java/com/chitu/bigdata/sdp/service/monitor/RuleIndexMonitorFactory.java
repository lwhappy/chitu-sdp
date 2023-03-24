package com.chitu.bigdata.sdp.service.monitor;


import com.xiaoleilu.hutool.util.StrUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sutao
 * @create 2021-07-28 9:30
 */
@Component
public class RuleIndexMonitorFactory {

    @Autowired
    Map<String, AbstractRuleIndexMonitor> ruleIndexMonitorMap = new ConcurrentHashMap<>(10);

    public AbstractRuleIndexMonitor getRuleIndexMonitor(String indexName) {
        AbstractRuleIndexMonitor ruleIndexMonitor = ruleIndexMonitorMap.get(indexName);
        if (ruleIndexMonitor == null) {
            throw new RuntimeException("[" + (StrUtil.isNotBlank(indexName)?indexName:"null") + "]规则不存在!");
        }
        return ruleIndexMonitor;
    }

}
