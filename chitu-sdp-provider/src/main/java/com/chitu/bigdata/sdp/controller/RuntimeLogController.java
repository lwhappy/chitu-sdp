

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-11-16
  * </pre>
  */

package com.chitu.bigdata.sdp.controller;


import com.chitu.bigdata.sdp.api.bo.SdpRuntimeLogBO;
import com.chitu.bigdata.sdp.service.SdpRuntimeLogService;
import com.chitu.cloud.model.ResponseData;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping(value = "/runtimeLog")
public class RuntimeLogController {
    private static final Logger logger = LoggerFactory.getLogger(RuntimeLogController.class);

    @Autowired
    private SdpRuntimeLogService sdpRuntimeLogService;

    @ApiOperation(value = "查询运行日志")
    @RequestMapping(value = "/queryRuntimeLog", method = RequestMethod.POST)
    public ResponseData runtimeLog(@RequestBody SdpRuntimeLogBO job) {

        return sdpRuntimeLogService.queryRuntimeLog(job);
    }
}