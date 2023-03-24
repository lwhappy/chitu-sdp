

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-11-8
  * </pre>
  */

package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.api.bo.SdpOperationLogBO;
import com.chitu.bigdata.sdp.service.OperationLogService;
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
@RequestMapping(value = "/log")
public class OperationLogController {

    @Autowired
    private OperationLogService operationLogService;

    @ApiOperation(value="查询操作日志")
    @RequestMapping(value="/searchByJobId",method= RequestMethod.POST)
   public ResponseData searchByJobId(@RequestBody SdpOperationLogBO operationLogBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(operationLogService.searchByJobId(operationLogBO));
        return data;
    }

}