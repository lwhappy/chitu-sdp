

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-11-8
  * </pre>
  */

package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.api.bo.SdpJarBO;
import com.chitu.bigdata.sdp.service.JarService;
import com.chitu.cloud.model.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;

/**
 * <pre>
 * 表现层控制类
 * </pre>
 */
@RestController
@Api(tags="JAR管理")
@RequestMapping(value = "/jar")
public class JarController {

    @Autowired
    private JarService jarService;

    @ApiOperation(value="新增jar")
    @RequestMapping(value="/addJar",method= RequestMethod.POST)
    public ResponseData addJar(@RequestParam("file") MultipartFile file,
                               @RequestHeader(value="projectId") Long projectId,
                               @RequestHeader(value="version") String version,
                               @RequestHeader(value="name") String name,
                               @RequestHeader(value="git") String git,
                               @RequestHeader(value="description") String description,
                               @RequestHeader(value="type") String type){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(jarService.addJar(file,projectId,version,name,git,description,type));
        return data;
    }

    @ApiOperation(value="查询jar")
    @RequestMapping(value="/queryJar",method= RequestMethod.POST)
    public ResponseData queryJar(@RequestBody SdpJarBO jarBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(jarService.queryJar(jarBO));
        return data;
    }

    @ApiOperation(value="搜索jar")
    @RequestMapping(value="/searchJar",method= RequestMethod.POST)
    public ResponseData searchJar(@RequestBody SdpJarBO jarBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(jarService.searchJar(jarBO));
        return data;
    }

    @ApiOperation(value="历史版本")
    @RequestMapping(value="/history",method= RequestMethod.POST)
    public ResponseData history(@RequestBody SdpJarBO jarBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(jarService.history(jarBO));
        return data;
    }

    @ApiOperation(value="删除jar")
    @RequestMapping(value="/deleteJar",method= RequestMethod.POST)
    public ResponseData deleteJar(@RequestBody SdpJarBO jarBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(jarService.deleteJar(jarBO));
        return data;
    }

    @ApiOperation(value="引用作业数")
    @RequestMapping(value="/referenceJobs",method= RequestMethod.POST)
    public ResponseData referenceJobs(@RequestBody SdpJarBO jarBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(jarService.referenceJobs4Page(jarBO));
        return data;
    }

    @ApiOperation(value="下载jar")
    @RequestMapping(value = "/download", method = RequestMethod.POST)
    public ResponseEntity download(@RequestBody SdpJarBO jarBO) throws Exception{
        return jarService.download(jarBO);
    }

    @ApiOperation(value="下载jar")
    @RequestMapping(value="/download2",method= RequestMethod.POST)
    public void download2(@RequestBody SdpJarBO jarBO, HttpServletResponse response){
        jarService.download2(jarBO,response);
    }

}