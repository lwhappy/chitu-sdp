

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-11-8
  * </pre>
  */

package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.api.bo.SdpJobAlertRecordBO;
import com.chitu.bigdata.sdp.service.SdpJobAlertRecordService;
import com.chitu.cloud.model.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * <pre>
 * 表现层控制类
 * </pre>
 */

@RestController
@Api(tags="JOB告警记录")
@RequestMapping(value = "/alert/record")
public class JobAlertRecordController {

    @Autowired
    private SdpJobAlertRecordService sdpJobAlertRecordService;


    @ApiOperation(value="事件列表")
    @RequestMapping(value="/list",method= RequestMethod.POST)
    public ResponseData list(@RequestBody SdpJobAlertRecordBO sdpJobAlertRecordBO) {
        return sdpJobAlertRecordService.queryList(sdpJobAlertRecordBO);
    }

}