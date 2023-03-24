

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-11-8
  * </pre>
  */

package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.api.bo.SdpJobAlertRuleBO;
import com.chitu.bigdata.sdp.service.SdpJobAlertRuleService;
import com.chitu.cloud.model.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
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
@Api(tags="JOB告警规则")
@RequestMapping(value = "/alert/rule")
public class JobAlertRuleController {

    @Autowired
    private SdpJobAlertRuleService sdpJobAlertRuleService;


    @ApiOperation(value="查询规则列表")
    @RequestMapping(value="/list",method= RequestMethod.POST)
    public ResponseData list(@RequestBody SdpJobAlertRuleBO sdpJobAlertRuleBO) {
        return sdpJobAlertRuleService.queryList(sdpJobAlertRuleBO);
    }


    @ApiOperation(value="添加规则")
    @RequestMapping(value="/add",method= RequestMethod.POST)
    public ResponseData add(@RequestBody @Validated(SdpJobAlertRuleBO.Save.class) SdpJobAlertRuleBO sdpJobAlertRuleBO) {
        return sdpJobAlertRuleService.addRule(sdpJobAlertRuleBO);
    }

    @ApiOperation(value="修改规则")
    @RequestMapping(value="/update",method= RequestMethod.POST)
    public ResponseData update(@RequestBody @Validated(SdpJobAlertRuleBO.Update.class) SdpJobAlertRuleBO sdpJobAlertRuleBO) {
        return sdpJobAlertRuleService.updateRule(sdpJobAlertRuleBO);
    }

    @ApiOperation(value="删除规则")
    @RequestMapping(value="/delete",method= RequestMethod.POST)
    public ResponseData delete(@RequestBody SdpJobAlertRuleBO sdpJobAlertRuleBO) {
        return sdpJobAlertRuleService.delRule(sdpJobAlertRuleBO);
    }



}