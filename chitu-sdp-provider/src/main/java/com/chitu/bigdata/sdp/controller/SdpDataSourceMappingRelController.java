

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-10-29
  * </pre>
  */

package com.chitu.bigdata.sdp.controller;

import com.alibaba.fastjson.JSON;
import com.chitu.bigdata.sdp.api.bo.ChangeEnvBO;
import com.chitu.bigdata.sdp.api.bo.SdpFileBO;
import com.chitu.bigdata.sdp.service.SdpDataSourceMappingRelService;
import com.chitu.cloud.model.ResponseData;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping(value = "/dataSourceMappingRel")
public class SdpDataSourceMappingRelController {
    private static final Logger logger = LoggerFactory.getLogger(SdpDataSourceMappingRelController.class);
    
    @Autowired
    private SdpDataSourceMappingRelService sdpDataSourceMappingRelService;


    @ApiOperation(value="获取比较数据")
    @RequestMapping(value="/compare",method= RequestMethod.POST)
    public ResponseData compare(@RequestBody SdpFileBO fileBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(sdpDataSourceMappingRelService.compare(fileBO));
        return data;
    }

    @ApiOperation(value="转环境数据保存")
    @RequestMapping(value="/changeEnv",method= RequestMethod.POST)
    public ResponseData changeEnv(@RequestBody ChangeEnvBO changeEnvBO) throws Exception{
        logger.info("[{}]转环境: {}",changeEnvBO.getFileId(), JSON.toJSONString(changeEnvBO));
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(sdpDataSourceMappingRelService.changeEnv(changeEnvBO));
        return data;
    }

}