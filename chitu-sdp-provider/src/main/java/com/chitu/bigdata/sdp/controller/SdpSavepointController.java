

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-4-18
  * </pre>
  */

package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.api.enums.LogStatus;
import com.chitu.bigdata.sdp.api.flink.Checkpoint;
import com.chitu.bigdata.sdp.api.model.SdpSavepoint;
import com.chitu.bigdata.sdp.api.vo.CheckpointAndSavepoint;
import com.chitu.bigdata.sdp.service.JobService;
import com.chitu.bigdata.sdp.service.SdpSavepointService;
import com.chitu.cloud.model.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * <pre>
 * 表现层控制类
 * </pre>
 */
@Api(tags="保存点")
@RestController
@RequestMapping(value = "/savepoint")
@Validated
public class SdpSavepointController {

    @Autowired
    private SdpSavepointService sdpSavepointService;
    @Autowired
    JobService jobService;

    @ApiOperation(value="保存点信息查询")
    @RequestMapping(value = "/query",method= RequestMethod.GET)
    public ResponseData query(@RequestParam("jobId") Long jobId) {
        ResponseData responseData = new ResponseData<>();
        responseData.ok();
        responseData.setData(sdpSavepointService.queryByJobId(jobId, null, 5));
        return responseData;
    }

    @ApiOperation(value="查询检查点和保存点用于作业启动")
    @RequestMapping(value = "/queryCheckpointAndSavepoint",method= RequestMethod.GET)
    public ResponseData queryCheckpointAndSavepoint(@RequestParam("jobId") Long jobId,@RequestParam("selectJobId") Long selectJobId) {
        ResponseData responseData = new ResponseData<>();
        responseData.ok();
        List<SdpSavepoint> savepoints = sdpSavepointService.queryByJobId(selectJobId, LogStatus.SUCCESS.name(), 5);
        List<Checkpoint> checkpoints = jobService.getCheckpoints(jobId, 5);
        CheckpointAndSavepoint checkpointAndSavepoint = new CheckpointAndSavepoint();
        checkpointAndSavepoint.setSavepoints(savepoints);
        checkpointAndSavepoint.setCheckpoints(checkpoints);
        responseData.setData(checkpointAndSavepoint);
        return responseData;
    }
}