package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.api.bo.SdpDataSourceBO;
import com.chitu.bigdata.sdp.api.bo.SdpJobBO;
import com.chitu.bigdata.sdp.api.domain.SourceKafkaInfo;
import com.chitu.bigdata.sdp.api.enums.JobAction;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.SdpJob;
import com.chitu.bigdata.sdp.api.model.SdpJobInstance;
import com.chitu.bigdata.sdp.mapper.SdpJobInstanceMapper;
import com.chitu.bigdata.sdp.service.EngineService;
import com.chitu.bigdata.sdp.service.JobImageService;
import com.chitu.bigdata.sdp.service.JobService;
import com.chitu.bigdata.sdp.service.SdpSysConfigService;
import com.chitu.bigdata.sdp.utils.JobOperationUtils;
import com.chitu.bigdata.sdp.utils.ResponseUtils;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.model.Pagination;
import com.chitu.cloud.model.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.shaded.guava30.com.google.common.collect.Lists;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/13 14:06
 */
@Slf4j
@RestController
@Api(tags = "作业管理")
@RequestMapping("/job")
public class JobController {
    @Autowired
    private JobService jobService;
    @Autowired
    private SdpSysConfigService configService;
    @Autowired
    EngineService engineService;
    @Autowired
    JobImageService dockerImageService;
    @Autowired
    private SdpJobInstanceMapper instanceMapper;

    @ApiOperation(value = "作业列表搜索")
    @RequestMapping(value = "/searchJob", method = RequestMethod.POST)
    public ResponseData searchJob(@RequestBody SdpJobBO jobBO, @RequestHeader(value = "projectId") Long projectId) {
        ResponseData data = new ResponseData<>();
        data.ok();
        jobBO.getVo().setProjectId(projectId);
        data.setData(jobService.searchJob(jobBO));
        return data;
    }

    @ApiOperation(value = "查询作业列表")
    @RequestMapping(value = "/queryJob", method = RequestMethod.POST)
    public ResponseData<Pagination<SdpJob>> queryJob(@RequestBody SdpJobBO jobBO, @RequestHeader(value = "projectId") String projectId, @RequestHeader(value = "env") String env) {
        ResponseData<Pagination<SdpJob>> data = new ResponseData<>();
        data.ok();
        jobBO.getVo().setProjectId(Long.valueOf(projectId == null ? "0" : projectId));
        data.setData(jobService.queryJob(jobBO));
        return data;
    }

    @ApiOperation(value = "删除作业")
    @RequestMapping(value = "/deleteJob", method = RequestMethod.POST)
    public ResponseData deleteJob(@RequestBody List<SdpJob> jobBO) {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(jobService.deleteJob(jobBO));
        return data;
    }

    @ApiOperation(value = "启动作业")
    @RequestMapping(value = "/startJob", method = RequestMethod.POST)
    public ResponseData startJob(@RequestBody List<SdpJobBO> jobs) {
        ResponseData data = new ResponseData<>();
        data.ok();
        configService.validateAction(JobAction.START.toString());
        for (SdpJobBO jobBO : jobs) {
            Long jobId = jobBO.getVo().getId();
            try {
                //kafka源表修改消费模式处理
                jobService.handleParam(jobBO);

                JobOperationUtils.runInJobLock(jobId, JobOperationUtils.OperationType.START_JOB, () -> {
                    jobService.validateStart(jobBO, JobAction.START.toString());
                    jobService.startJob(jobBO, JobAction.START.toString());
                    return null;
                });
            } catch (Exception e) {
                log.error("启动作业失败===jobId: " + jobId, e);
                if (e instanceof ApplicationException && ((ApplicationException) e).getAppCode().getCode() == ResponseCode.CONNECT_IS_WRONG.getCode()) {
                    throw ((ApplicationException) e);
                } else {
                    throw new ApplicationException(ResponseCode.CONNECT_IS_WRONG,
                            String.format("启动作业失败, jobId: %d, message: %s", jobId, e.getMessage()));
                }
            }
        }
        return data;
    }

    @ApiOperation(value = "停止作业")
    @RequestMapping(value = "/stopJob", method = RequestMethod.POST)
    public ResponseData stopJob(@RequestBody List<SdpJobBO> jobs) {
        ResponseData data = new ResponseData<>();
        data.ok();
        configService.validateAction(JobAction.STOP.toString());
        for (SdpJobBO jobBO : jobs) {
            Long jobId = jobBO.getVo().getId();
            try {
                JobOperationUtils.runInJobLock(jobId, JobOperationUtils.OperationType.STOP_JOB, () -> {
                    jobService.validateStop(jobBO, JobAction.STOP.toString());
                    jobService.stopJob(jobBO, JobAction.STOP.toString());
                    return null;
                });
            } catch (Exception e) {
                log.error("停止作业失败===, jobId: " + jobId, e);
                throw new ApplicationException(ResponseCode.ERROR,
                        String.format("停止作业失败, jobId: %d, message: %s", jobId, e.getMessage()));
            }
        }
        return data;
    }

    @ApiOperation(value = "暂停作业")
    @RequestMapping(value = "/pauseJob", method = RequestMethod.POST)
    public ResponseData pauseJob(@RequestBody SdpJobBO jobBO) {
        ResponseData data = new ResponseData<>();
        data.ok();
        configService.validateAction(JobAction.STOP.toString());
        Long jobId = jobBO.getVo().getId();
        try {
            JobOperationUtils.runInJobLock(jobId, JobOperationUtils.OperationType.PAUSE_JOB, () -> {
                jobService.validateStop(jobBO, JobAction.PAUSE.toString());
                jobService.stopJob(jobBO, JobAction.PAUSE.toString());
                return null;
            });
        } catch (Exception e) {
            log.error("暂停作业失败===, jobId: " + jobId, e);
            throw new ApplicationException(ResponseCode.ERROR,
                    String.format("暂停作业失败, jobId: %d, message: %s", jobId, e.getMessage()));
        }
        return data;
    }

    @ApiOperation(value = "恢复作业")
    @RequestMapping(value = "/recoverJob", method = RequestMethod.POST)
    public ResponseData recoverJob(@RequestBody SdpJobBO jobBO) {
        ResponseData data = new ResponseData<>();
        data.ok();
        configService.validateAction(JobAction.START.toString());
        Long jobId = jobBO.getVo().getId();
        try {
//            if (engineService.checkResource(jobId).getSuccess()) {
            JobOperationUtils.runInJobLock(jobId, JobOperationUtils.OperationType.RECOVER_JOB, () -> {
                jobService.validateStart(jobBO, JobAction.RECOVER.toString());
                jobService.startJob(jobBO, JobAction.RECOVER.toString());
                return null;
            });
//            }
        } catch (Exception e) {
            log.error("恢复作业失败===, jobId: " + jobId, e);
            if (e instanceof ApplicationException && ((ApplicationException) e).getAppCode().getCode() == ResponseCode.CONNECT_IS_WRONG.getCode()) {
                throw ((ApplicationException) e);
            } else {
                throw new ApplicationException(ResponseCode.ERROR,
                        String.format("恢复作业失败, jobId: %d, message: %s", jobId, e.getMessage()));
            }

        }
        return data;
    }


    @ApiOperation(value = "作业配置明细")
    @RequestMapping(value = "/jobConf", method = RequestMethod.POST)
    public ResponseData jobConf(@RequestBody SdpJobBO jobBO) {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(jobService.getJobConf(jobBO));
        return data;
    }

    @ApiOperation(value = "修改作业优先等级")
    @RequestMapping(value = "/setPriorityLevel", method = RequestMethod.POST)
    public ResponseData setPriorityLevel(@RequestBody SdpJob jobBO) {
        ResponseData data = new ResponseData<>();
        data.ok();
        jobService.updatePriority(Lists.newArrayList(jobBO));
        return data;
    }

    @ApiOperation(value = "获取项目下job的更新人")
    @RequestMapping(value = "/selectUpdatedBy", method = RequestMethod.POST)
    public ResponseData selectUpdatedBy(@RequestHeader(value = "projectId") Long projectId) {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(jobService.selectUpdatedBy(projectId));
        return data;
    }

    @ApiOperation(value = "添加保存点")
    @RequestMapping(value = "/triggerSavepoint", method = RequestMethod.POST)
    public ResponseData triggerSavepoint(@RequestBody List<SdpJobBO> jobs) {
        ResponseData data = new ResponseData<>();
        data.ok();
        for (SdpJobBO jobBO : jobs) {
            Long jobId = jobBO.getVo().getId();
            try {
                //JobOperationUtils.runInJobLock(jobId, JobOperationUtils.OperationType.ADD_SAVEPOINT, () -> {
                jobService.triggerSavepoint4Job(jobBO, JobAction.ADD_SAVEPOINT.toString());
                // });
            } catch (Exception e) {
                log.error("添加保存点失败===, jobId: " + jobId, e);
                throw new ApplicationException(ResponseCode.COMMON_ERR,
                        String.format("添加保存点失败, jobId: %d, message: %s", jobId, e.getMessage()));
            }
        }
        return data;
    }

    @ApiOperation(value = "查询作业下拉列表")
    @RequestMapping(value = "/selectJob", method = RequestMethod.POST)
    public ResponseData selectJob(@RequestBody SdpJobBO sdpJobBO, @RequestHeader(value = "projectId") Long projectId) {
        ResponseData data = new ResponseData<>();
        data.ok();
        sdpJobBO.getVo().setProjectId(projectId);
        data.setData(jobService.getMapper().selectLists(sdpJobBO));
        return data;
    }

    @ApiOperation(value = "查询直接启动的数据")
    @RequestMapping(value = "/queryDirectStartData", method = RequestMethod.GET)
    public ResponseData queryDirectStartData(@RequestParam("jobId") Long jobId) {
        ResponseData responseData = new ResponseData<>();
        responseData.ok();
        responseData.setData(jobService.queryDirectStartData(jobId));
        return responseData;
    }

    @ApiOperation(value = "查询运行配置")
    @RequestMapping(value = "/queryRunningConfig", method = RequestMethod.GET)
    public ResponseData queryRunningConfig(@RequestParam("jobId") Long jobId) {
        ResponseData responseData = new ResponseData<>();
        responseData.ok();
        responseData.setData(jobService.queryRunningConfig(jobId));
        return responseData;
    }

    @ApiOperation(value = "查询待消费数")
    @RequestMapping(value = "/queryWaitConsumedNum", method = RequestMethod.POST)
    public ResponseData selectWaitConsumedSum(@RequestBody @Validated(SourceKafkaInfo.QUERY.class) SourceKafkaInfo sourceKafkaInfo, @RequestHeader(value = "projectId") Long projectId) {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(jobService.waitConsumedNum(sourceKafkaInfo));
        return data;
    }

    @ApiOperation(value = "数据源管理,查询作业列表")
    @RequestMapping(value = "/select", method = RequestMethod.POST)
    public ResponseData JobListByDataSource(@RequestBody @Validated SdpDataSourceBO datasourceBo, @RequestHeader(value = "projectId") Long projectId) {
        ResponseData data = new ResponseData<>();
        data.ok();
        datasourceBo.setProjectId(projectId);
        return data.setData(jobService.JobListByDataSource(datasourceBo));
    }

    @ApiOperation(value = "排查canal问题数据")
    @RequestMapping(value = "/tempCheckCanal", method = RequestMethod.POST)
    @Deprecated
    public ResponseData tempCheckCanal(@RequestHeader(value = "projectId") Long projectId) {
        ResponseData data = new ResponseData<>();
        data.ok();
        return data.setData(jobService.tempCheckCanal(projectId));
    }

    @ApiOperation(value = "是否需要构建镜像")
    @RequestMapping(value = "/checkBuildImage", method = RequestMethod.POST)
    public ResponseData checkBuildImage(@RequestParam Long jobId, @RequestParam String jobActionType, @RequestParam boolean useLatest) {
        return ResponseUtils.success(dockerImageService.needBuildPushImage(jobId, jobActionType, useLatest));
    }

    @ApiOperation(value = "重新构建镜像")
    @RequestMapping(value = "/rebuildImage", method = RequestMethod.POST)
    public void rebuildImage() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                // 查询需要构建镜像的列表
                List<SdpJobInstance> sdpJobInstances = instanceMapper.queryInstance4Sync(new SdpJobInstance());
                // 需要构建镜像的job统计
                Integer needCnt =  0;
                // 成功构建镜像的统计
                Integer successCnt = 0;
                for(SdpJobInstance sdpJobInstance : sdpJobInstances){
                    try{
                        // 判断是否需要构建镜像
                        boolean needRebuildRes = dockerImageService.needRebuildPushImage(sdpJobInstance.getJobId());
                        // 构建镜像
                        if(needRebuildRes){
                            needCnt ++;
                            // 删除本地镜像
                            dockerImageService.deleteImage(Collections.singletonList(jobService.get(sdpJobInstance.getJobId())));
                            // 构建镜像
                            dockerImageService.rebuildPushImage(sdpJobInstance.getJobId());
                            successCnt ++;
                        }
                    }catch (Exception e){
                        log.error("=== 重新构建镜像失败 ===, jobId: " + sdpJobInstance.getJobId(), e);
                    }
                }
                log.info("重新构建镜像结果[ 需要重新构建数量:"+ needCnt + "，成功构建数量：" + successCnt + " ]");
            }
        },"rebuildImage").start();

    }

}
