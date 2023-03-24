package com.chitu.bigdata.sdp.test;

import com.chitu.bigdata.sdp.api.model.SdpJob;
import com.chitu.bigdata.sdp.service.SdpJobAlertRuleService;
import com.chitu.cloud.web.test.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author sutao
 * @create 2021-11-13 16:43
 */
public class AlertRuleTest extends BaseTest {

    @Autowired
    SdpJobAlertRuleService sdpJobAlertRuleService;

    @Test
    public void addDefaultRule() {
        SdpJob sdpJob = new SdpJob();
        sdpJob.setId(222L);
        sdpJob.setCreatedBy("123");
        sdpJobAlertRuleService.addDefaultRule(sdpJob);
    }

}
