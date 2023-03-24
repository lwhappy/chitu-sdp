

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.annotation.ValidateAdmin;
import com.chitu.bigdata.sdp.api.bo.ClusterInfoBO;
import com.chitu.bigdata.sdp.api.bo.SdpEngineBO;
import com.chitu.bigdata.sdp.api.bo.SdpJobBO;
import com.chitu.bigdata.sdp.api.domain.EngineUserInfo;
import com.chitu.bigdata.sdp.api.domain.SdpEngineInfo;
import com.chitu.bigdata.sdp.api.model.SdpEngine;
import com.chitu.bigdata.sdp.service.EngineService;
import com.chitu.bigdata.sdp.utils.ResponseUtils;
import com.chitu.cloud.model.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <pre>
 * 表现层控制类
 * </pre>
 */

@RestController
@Api(tags = "引擎管理")
@RequestMapping(value = "/setting/engineSetting")
public class EngineController {
    private static final Logger logger = LoggerFactory.getLogger(EngineController.class);
    
    @Autowired
    private EngineService engineService;

    @ApiOperation(value = "引擎管理-引擎列表")
    @RequestMapping(value ="/engineInfo",method= RequestMethod.POST)
    public ResponseData getEngineInfo(@RequestBody SdpEngineBO sdpEngineBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.engineService.getEngineInfo(sdpEngineBO));
        return data;
    }

    @ApiOperation(value = "引擎管理-删除引擎")
    @RequestMapping(value ="/delete",method= RequestMethod.POST)
    public ResponseData deleteEngine(@RequestBody SdpEngine sdpEngine){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.engineService.delete(sdpEngine));
        return data;
    }

    @ApiOperation(value = "引擎管理-添加引擎用户")
    @RequestMapping(value ="/addUser",method= RequestMethod.POST)
    public ResponseData addUser(@RequestBody List<EngineUserInfo> engineUserInfos){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.engineService.addUser(engineUserInfos));
        return data;
    }

    @ApiOperation(value = "引擎管理-添加引擎")
    @RequestMapping(value ="/add",method= RequestMethod.POST)
    public ResponseData addEngine(@RequestBody SdpEngineInfo sdpEngine){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.engineService.add(sdpEngine));
        return data;
    }


    @ApiOperation(value = "引擎管理-搜索引擎用户")
    @RequestMapping(value ="/getEngineUser",method= RequestMethod.POST)
    public ResponseData getEngineUser(@RequestBody EngineUserInfo engineUserInfo){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.engineService.getEngineUser(engineUserInfo));
        return data;
    }

    @ApiOperation(value = "引擎管理-模糊搜索")
    @RequestMapping(value ="/getByName",method= RequestMethod.POST)
    public ResponseData getByName(@RequestBody SdpEngine sdpEngine){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.engineService.getByName(sdpEngine));
        return data;
    }


    @ApiOperation(value = "引擎管理-删除引擎用户")
    @RequestMapping(value ="/deleteUser",method= RequestMethod.POST)
    public ResponseData deleteUser(@RequestBody EngineUserInfo engineUserInfo){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.engineService.deleteUser(engineUserInfo));
        return data;
    }

    @ApiOperation(value = "引擎管理-引擎引用项目列表")
    @RequestMapping(value ="/engineProjects",method= RequestMethod.POST)
    public ResponseData engineProjects(@RequestBody SdpEngine sdpEngine){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.engineService.getProject4Engine(sdpEngine));
        return data;
    }

    @ApiOperation(value = "引擎管理-引擎队列列表")
    @ValidateAdmin
    @RequestMapping(value ="/engineQueues",method= RequestMethod.POST)
    public ResponseData engineQueues(@RequestBody ClusterInfoBO clusterInfo){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.engineService.engineQueues(clusterInfo));
        return data;
    }

    @ApiOperation(value = "集群资源校验")
    @RequestMapping(value ="/resourceValidate",method= RequestMethod.POST)
    public ResponseData resourceValidate(@RequestBody SdpJobBO jobBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(engineService.checkResource(Long.valueOf(jobBO.getId())));
        return data;
    }

    @ApiOperation(value = "查询引擎集群")
    @ValidateAdmin
    @RequestMapping(value ="/queryCluster",method= RequestMethod.GET)
    public ResponseData queryCluster(@RequestParam String env,@RequestParam String engineType){
        return ResponseUtils.success(engineService.getClusterInfos(env, engineType));
    }
}