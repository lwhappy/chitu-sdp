

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.api.bo.SdpFolderBO;
import com.chitu.bigdata.sdp.api.vo.FolderVo;
import com.chitu.bigdata.sdp.service.FolderService;
import com.chitu.cloud.model.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/15 14:06
 */
@RestController
@Api(tags="目录管理")
@RequestMapping(value = "/folder")
public class FolderController {

    @Autowired
    private FolderService folderService;

    @ApiOperation(value="添加目录")
    @RequestMapping(value="/addFolder",method= RequestMethod.POST)
    public ResponseData addFolder(@RequestBody @Valid SdpFolderBO folderBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(folderService.addFolder(folderBO));
        return data;
    }

    @ApiOperation(value="查询目录(树)")
    @RequestMapping(value="/queryFolder",method= RequestMethod.POST)
    public ResponseData queryFolder(@RequestBody SdpFolderBO folderBO,@RequestHeader(value="projectId") Long projectId){
        ResponseData data = new ResponseData<>();
        data.ok();
        folderBO.setParentId(0L);
        FolderVo folderVo = new FolderVo();
        BeanUtils.copyProperties(folderBO,folderVo);
        folderVo.setProjectId(projectId);
        data.setData(folderService.queryFolder(folderVo));
        return data;
    }

    @ApiOperation(value="修改目录")
    @RequestMapping(value="/updateFolder",method= RequestMethod.POST)
    public ResponseData updateFolder(@RequestBody SdpFolderBO folderBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(folderService.updateFolder(folderBO));
        return data;
    }

    @ApiOperation(value="删除目录")
    @RequestMapping(value="/deleteFolder",method= RequestMethod.POST)
    public ResponseData deleteFolder(@RequestBody SdpFolderBO folderBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(folderService.deleteFolder(folderBO));
        return data;
    }

    @ApiOperation(value="文件/目录搜索")
    @RequestMapping(value="/searchFolder",method= RequestMethod.POST)
    public ResponseData searchFolder(@RequestBody SdpFolderBO folderBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(folderService.searchFolder(folderBO));
        return data;
    }

    @ApiOperation(value="目录搜索")
    @RequestMapping(value="/searchFolders",method= RequestMethod.POST)
    public ResponseData searchFoldesr(@RequestBody SdpFolderBO folderBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(folderService.searchFolders(folderBO));
        return data;
    }

}