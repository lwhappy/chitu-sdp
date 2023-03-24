package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.service.JarService;
import com.chitu.bigdata.sdp.service.SdpJobAlertRuleService;
import com.chitu.cloud.model.ResponseData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.CompletableFuture;

/**
 * @author sutao
 * @create 2022-06-14 20:05
 */
@RestController
@RequestMapping(value = "/data/sync")
public class DataSyncController {


    @Autowired
    private SdpJobAlertRuleService sdpJobAlertRuleService;

    @Autowired
    private JarService jarService;

    @RequestMapping(value = "/alertRule", method = RequestMethod.POST)
    public ResponseData alertRule() {
        return sdpJobAlertRuleService.syncJobAlertRule();
    }


    @RequestMapping(value = "/jarFlinkVersion", method = RequestMethod.POST)
    public ResponseData jarFlinkVersion() {
        CompletableFuture.runAsync(() -> jarService.syncJarFlinkVersion());
        ResponseData responseData = new ResponseData<>();
        responseData.setData("1").ok();
        return responseData;
    }


}
