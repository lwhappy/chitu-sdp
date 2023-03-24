

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.api.bo.SdpVersion4BO;
import com.chitu.bigdata.sdp.api.bo.SdpVersionBO;
import com.chitu.bigdata.sdp.service.FileVersionService;
import com.chitu.cloud.model.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author 587694
 * @description: TODO
 * @date 2021/10/17 14:06
 */
@RestController
@Api(tags="文件版本管理")
@RequestMapping(value = "/file/version")
public class FileVersionController {
    @Autowired
    FileVersionService fileVersionService;

    @ApiOperation(value="获取版本信息")
    @RequestMapping(value="/getVersions",method= RequestMethod.POST)
    public ResponseData getVersions(@RequestBody SdpVersion4BO sdpVersion4BO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(fileVersionService.getVersions(sdpVersion4BO));
        return data;
    }

    //文件版本相关操作
    @ApiOperation(value="版本比对")
    @RequestMapping(value="/compare",method= RequestMethod.POST)
    public ResponseData compareVersion(@RequestBody SdpVersionBO versionBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(fileVersionService.compareVersion(versionBO));
        return data;
    }

    @ApiOperation(value="版本删除")
    @RequestMapping(value="/deleteVersion",method= RequestMethod.POST)
    public ResponseData deleteVersion(@RequestBody SdpVersionBO versionBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(fileVersionService.deleteVersion(versionBO));
        return data;
    }

    @ApiOperation(value="版本回滚")
    @RequestMapping(value="/rollback",method= RequestMethod.POST)
    public ResponseData rollbackVersion(@RequestBody SdpVersionBO versionBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(fileVersionService.rollbackVersion(versionBO));
        return data;
    }


//    @ApiOperation(value="离开file对锁定进行更改")
//    @RequestMapping(value="/releaseLockFile",method= RequestMethod.POST)
//    public ResponseData releaseLockFile(@RequestBody SdpFileBO fileBO){
//        ResponseData data = new ResponseData<>();
//        data.ok();
//        data.setData(fileService.releaseLockFile(fileBO));
//        return data;
//    }
}