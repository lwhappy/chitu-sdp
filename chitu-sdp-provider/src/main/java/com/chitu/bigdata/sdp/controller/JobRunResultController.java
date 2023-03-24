package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.api.bo.JobRunResultFailDataBO;
import com.chitu.bigdata.sdp.service.JobRunResultService;
import com.chitu.cloud.model.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * @author sutao
 * @create 2022-03-03 14:24
 */
@RestController
@Api(tags = "job运行结果")
@RequestMapping(value = "/runResult")
public class JobRunResultController {

    @Autowired
    private JobRunResultService jobRunResultService;

    @ApiOperation(value = "查询运行结果")
    @RequestMapping(value = "/queryRunningResult", method = RequestMethod.GET)
    public ResponseData runtimeLog(@RequestParam("jobId") Long jobId) {
        return jobRunResultService.queryJobRunningResult(jobId);
    }

    @ApiOperation(value = "查询失败数据")
    @RequestMapping(value = "/queryFailData", method = RequestMethod.POST)
    public ResponseData queryFailData(@RequestBody @Validated JobRunResultFailDataBO jobRunResultFailDataBO) {
        return jobRunResultService.queryFailData(jobRunResultFailDataBO);
    }

}
