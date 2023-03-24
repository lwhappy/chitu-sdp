

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-12-10
  * </pre>
  */

package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.api.bo.SdpApproveBO;
import com.chitu.bigdata.sdp.service.ApproveService;
import com.chitu.cloud.model.ResponseData;
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
@RequestMapping(value = "/approve")
public class ApproveController {

    @Autowired
    private ApproveService approveService;

    @ApiOperation(value="新增申请[审批]")
    @RequestMapping(value="/submitApply",method= RequestMethod.POST)
    public ResponseData submitApply(@RequestBody SdpApproveBO approveBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(approveService.submitApply(approveBO));
        return data;
    }

    @ApiOperation(value="查询我的申请/审批")
    @RequestMapping(value="/queryApply",method= RequestMethod.POST)
    public ResponseData queryApply(@RequestBody SdpApproveBO approveBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(approveService.queryApply(approveBO));
        return data;
    }

    @ApiOperation(value="撤销申请")
    @RequestMapping(value="/cancelApply",method= RequestMethod.POST)
    public ResponseData cancelApply(@RequestBody SdpApproveBO approveBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(approveService.cancelApply(approveBO));
        return data;
    }

    @ApiOperation(value="申请详情")
    @RequestMapping(value="/detailApply",method= RequestMethod.POST)
    public ResponseData detailApply(@RequestBody SdpApproveBO approveBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(approveService.detailApply(approveBO));
        return data;
    }

    @ApiOperation(value="审批")
    @RequestMapping(value="/executeApprove",method= RequestMethod.POST)
    public ResponseData executeApprove(@RequestBody SdpApproveBO approveBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(approveService.executeApprove(approveBO));
        return data;
    }

    @ApiOperation(value="查询待审批")
    @RequestMapping(value="/queryPending",method= RequestMethod.POST)
    public ResponseData queryPending(@RequestBody SdpApproveBO approveBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(approveService.queryPending(approveBO));
        return data;
    }

}