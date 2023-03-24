

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.controller;


import com.chitu.bigdata.sdp.annotation.ValidateProjectLeader;
import com.chitu.bigdata.sdp.api.bo.SdpProjectBO;
import com.chitu.bigdata.sdp.api.bo.SdpProjectBTO;
import com.chitu.bigdata.sdp.api.domain.ProjectUser;
import com.chitu.bigdata.sdp.api.domain.SdpProjectInfo;
import com.chitu.bigdata.sdp.api.model.SdpEngine;
import com.chitu.bigdata.sdp.api.model.SdpProject;
import com.chitu.bigdata.sdp.api.vo.SdpProjectResp;
import com.chitu.bigdata.sdp.config.BusinessLineConfigProperties;
import com.chitu.bigdata.sdp.service.ProjectService;
import com.chitu.bigdata.sdp.utils.ResponseUtils;
import com.chitu.cloud.model.Pagination;
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
//@RefreshScope
@Api(tags = "项目管理")
@RestController
@RequestMapping(value = "/project/projectManagement")
public class ProjectController {
    private static final Logger logger = LoggerFactory.getLogger(ProjectController.class);

    @Autowired
    private ProjectService projectService;

    @Autowired
    BusinessLineConfigProperties businessLineConfigProperties;


    @ApiOperation(value = "项目管理-项目列表")
    @RequestMapping(value = "/projectInfo",method= RequestMethod.POST)
    public ResponseData getProjectInfo(@RequestBody SdpProjectBO sdpProjectBO) {
        ResponseData data = new ResponseData<>();
        data.ok();
        Pagination<SdpProjectResp> pagination = this.projectService.getProjectInfo(sdpProjectBO);
        data.setData(pagination);
        return data;
    }

    @ApiOperation(value = "项目管理-项目列表")
    @RequestMapping(value = "/projectList",method= RequestMethod.POST)
    public ResponseData projectList(@RequestBody SdpProjectBTO sdpProjectBTO) {
        ResponseData data = new ResponseData<>();
        data.ok();
        Pagination<SdpProjectResp> pagination = this.projectService.projectList(sdpProjectBTO);
        data.setData(pagination);
        return data;
    }

    @ApiOperation(value = "项目管理-添加项目")
    @RequestMapping(value = "/add",method= RequestMethod.POST)
    public ResponseData addProject(@RequestBody SdpProjectInfo sdpProjectInfo,@RequestHeader(value="env") String env) {
        ResponseData data = new ResponseData<>();
        data.ok();
        sdpProjectInfo.setEnv(env);
        data.setData(this.projectService.add(sdpProjectInfo));
        return data;
    }

    @ApiOperation(value = "项目管理-添加项目user")
    @RequestMapping(value = "/deleteProjectUser",method= RequestMethod.POST)
    public ResponseData deleteProjectUser(@RequestBody ProjectUser projectUser) {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.projectService.deleteProjectUser(projectUser));
        return data;
    }

    @ApiOperation(value = "项目管理-项目成员角色转变")
    @RequestMapping(value = "/changeRoles",method= RequestMethod.POST)
    public ResponseData changeRoles(@RequestBody List<ProjectUser> projectUser) {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.projectService.changeRoles(projectUser));
        return data;
    }

    @ApiOperation(value = "项目管理-修改项目明细信息")
    @RequestMapping(value = "/update",method= RequestMethod.POST)
    public ResponseData updateProject(@RequestBody SdpProjectInfo sdpProjectInfo) {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.projectService.update(sdpProjectInfo));
        return data;
    }

    @ApiOperation(value = "项目管理-删除项目")
    @RequestMapping(value = "/delete",method= RequestMethod.POST)
    public ResponseData deleteProject(@RequestBody SdpProjectInfo sdpProjectInfo) {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.projectService.delete(sdpProjectInfo));
        return data;
    }

    @ApiOperation(value = "项目管理-项目名称模糊搜索")
    @RequestMapping(value = "/search",method= RequestMethod.POST)
    public ResponseData searchProject(@RequestBody SdpProjectInfo sdpProjectInfo) {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.projectService.search(sdpProjectInfo));
        return data;
    }

    @ApiOperation(value = "项目管理-项目获取用户")
    @RequestMapping(value = "/projectUserInfo",method= RequestMethod.POST)
    public ResponseData projectUserInfo(@RequestBody SdpProject sdpProject) {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.projectService.projectUserInfo(sdpProject));
        return data;
    }

    @ValidateProjectLeader
    @ApiOperation(value = "项目管理-项目查询当前用户的引擎")
    @RequestMapping(value = "/getEngineByName",method= RequestMethod.POST)
    public ResponseData getEngineByName(@RequestBody SdpEngine sdpEngine) {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.projectService.getEngineByName(sdpEngine));
        return data;
    }


    @ApiOperation(value = "项目管理-项目名称查询")
    @RequestMapping(value = "/getProjects",method= RequestMethod.POST)
    public ResponseData getProjects(@RequestBody(required = false) SdpProject sdpProject) {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.projectService.getProjects(sdpProject));
        return data;
    }

    @ApiOperation(value = "项目管理-历史使用projectId查询，前端使用跳转文件编辑页面")
    @RequestMapping(value = "/getProjectHistory",method= RequestMethod.POST)
    public ResponseData getProjectHistory() {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.projectService.getProjectHistory());
        return data;
    }

    @ApiOperation(value = "项目管理-项目用户角色获取")
    @RequestMapping(value = "/getUserRole",method= RequestMethod.POST)
    public ResponseData getUserRole(@RequestHeader(value="projectId") Long projectId) {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.projectService.getUserRole(projectId));
        return data;
    }

    @ApiOperation(value = "项目管理-项目获取用户和系统管理员")
    @RequestMapping(value = "/getProjectUser",method= RequestMethod.POST)
    public ResponseData getProjectUser(@RequestBody SdpProject sdpProject) {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(this.projectService.getProjectUser(sdpProject));
        return data;
    }



    @ApiOperation(value = "查询项目等级")
    @RequestMapping(value = "/detail",method= RequestMethod.POST)
    public ResponseData detail(@RequestBody SdpProject sdpProject) {
        return ResponseUtils.success(projectService.get(sdpProject.getId()));
    }

    @ValidateProjectLeader
    @ApiOperation(value = "项目管理-查询业务线")
    @RequestMapping(value = "/queryBusinessLine",method= RequestMethod.GET)
    public ResponseData queryBusinessLine(@RequestHeader(value="env") String env ) {
        ResponseData data = new ResponseData<>();
        data.setData(businessLineConfigProperties.getBls());
        data.ok();
        return data;
    }
}

