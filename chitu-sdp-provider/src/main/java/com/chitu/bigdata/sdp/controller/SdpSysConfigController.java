

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-3-29
  * </pre>
  */

package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.annotation.ValidateAdmin;
import com.chitu.bigdata.sdp.api.bo.SysconfigBO;
import com.chitu.bigdata.sdp.service.SdpSysConfigService;
import com.chitu.cloud.model.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.validation.Valid;

/**
 * <pre>
 * 表现层控制类
 * </pre>
 */
//@RefreshScope
@Api(tags = "系统配置")
@RestController
@RequestMapping(value = "/sysconfig")
@Validated
public class SdpSysConfigController {

    @Autowired
    private SdpSysConfigService sdpSysConfigService;


    @ApiOperation(value="系统运维查询")
    @ValidateAdmin
    @RequestMapping(value="/getSysoper",method= RequestMethod.GET)
    public ResponseData getSysoper(){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(sdpSysConfigService.querySysoper());
        return data;
    }

    @ApiOperation(value="系统运维保存")
    @ValidateAdmin
    @RequestMapping(value="/saveSysoper",method= RequestMethod.POST)
    public ResponseData saveSysoper(@RequestBody @Valid SysconfigBO sysconfigBO) {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(sdpSysConfigService.saveSysoper(sysconfigBO));
        return  data;
    }

    @ApiOperation(value="当前用户是否是白名单")
    @RequestMapping(value="/isWhiteList",method= RequestMethod.GET)
    public ResponseData isWhiteList() {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(sdpSysConfigService.isWhiteList());
        return  data;
    }

}