package com.chitu.bigdata.sdp.flink.submit.controller;


import com.alibaba.fastjson.JSONObject;
import com.chitu.bigdata.sdp.api.domain.SubmitResponse;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.flink.Application;
import com.chitu.bigdata.sdp.flink.common.enums.ExecutionMode;
import com.chitu.bigdata.sdp.flink.common.util.ExceptionUtils;
import com.chitu.bigdata.sdp.flink.submit.service.FlinkAppServiceFactory;
import com.chitu.bigdata.sdp.flink.submit.service.KubernetesApplicationServiceImpl;
import com.chitu.cloud.model.ResponseData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/flink")
public class FlinkClientController {

    @Autowired
    FlinkAppServiceFactory flinkAppServiceFactory;




    @RequestMapping(value="/startJob",method= RequestMethod.POST)
    public ResponseData start(@RequestBody Application application) {
        log.info("start param: {}", JSONObject.toJSONString(application));
        ResponseData responseData = new ResponseData();
        try{
           com.chitu.bigdata.sdp.flink.submit.service.SubmitResponse submitResponse = flinkAppServiceFactory.getFlinkAppService(ExecutionMode.of(application.getExecutionMode()).getName()).start(application);
            SubmitResponse submitResp = new SubmitResponse();
            submitResp.setApplicationId(submitResponse.clusterId());
            submitResp.setConfiguration(submitResponse.configuration().toMap());
            submitResp.setJobManagerAddress(submitResponse.jobManagerAddress());
            responseData.setData(submitResp).ok();
            log.info("start finish: {}", JSONObject.toJSONString(responseData));
        }catch (Exception e){
            String exception = ExceptionUtils.stringifyException(e);
            responseData.setCode(ResponseCode.JOB_START_ERROR.getCode());
            responseData.setMsg(exception);
        }
        return responseData;
    }


    @RequestMapping(value="/stopJob",method= RequestMethod.POST)
    public ResponseData stop(@RequestBody Application application){
        log.info("stop param: {}", JSONObject.toJSONString(application));
        ResponseData responseData = new ResponseData();
        responseData.setData(flinkAppServiceFactory.getFlinkAppService(ExecutionMode.of(application.getExecutionMode()).getName()).stop(application)).ok();
        log.info("stop finish: {}", JSONObject.toJSONString(responseData));
        return responseData;
    }


    @RequestMapping(value="/triggerSavepoint",method= RequestMethod.POST)
    public ResponseData triggerSavepoint(@RequestBody Application application){
        log.info("triggerSavepoint param: {}", JSONObject.toJSONString(application));
        ResponseData responseData = new ResponseData();
        responseData.setData(flinkAppServiceFactory.getFlinkAppService(ExecutionMode.of(application.getExecutionMode()).getName()).triggerSavepoint(application)).ok();
        log.info("triggerSavepoint finish: {}", JSONObject.toJSONString(responseData));
        return responseData;
    }

    @RequestMapping(value="/killClusterK8s",method= RequestMethod.POST)
    public ResponseData killClusterK8s(@RequestBody Application application){
        ResponseData responseData = new ResponseData();
        KubernetesApplicationServiceImpl kubernetesApplicationService = (KubernetesApplicationServiceImpl)flinkAppServiceFactory.getFlinkAppService(ExecutionMode.of(application.getExecutionMode()).getName());
        kubernetesApplicationService.killCluster(application);
        responseData.setData("ok").ok();
        return responseData;
    }


    @RequestMapping(value="/test",method= RequestMethod.GET)
    public ResponseData start() {
        ResponseData responseData = new ResponseData();
        responseData.setData("success").ok();
        return responseData;
    }




}
