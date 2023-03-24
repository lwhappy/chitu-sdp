

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-12-9
  * </pre>
  */

package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.annotation.ValidateProject;
import com.chitu.bigdata.sdp.api.bo.SdpDataSourceBO;
import com.chitu.bigdata.sdp.api.model.SdpDataSource;
import com.chitu.bigdata.sdp.config.DataSourceConfigProperties;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.service.DataSourceService;
import com.chitu.cloud.model.ResponseData;
import io.swagger.annotations.ApiOperation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

/**
 * <pre>
 * 表现层控制类
 * </pre>
 */
//@RefreshScope
@RestController
@RequestMapping(value = "/dataSource")
public class DataSourceController {
    private static final Logger logger = LoggerFactory.getLogger(DataSourceController.class);
    
    @Autowired
    private DataSourceService sdpDataSourceService;

    @Autowired
    DataSourceConfigProperties dataSourceConfigProperties;
    @Autowired
    SdpConfig sdpConfig;

    @ApiOperation(value="数据源列表查询")
    @RequestMapping(value="/list",method= RequestMethod.POST)
    public ResponseData dataSourceList(@RequestBody SdpDataSourceBO bo,@RequestHeader(value="projectId") Long projectId) {
        ResponseData data = new ResponseData<>();
        data.ok();
        if (Objects.nonNull(projectId)) {
            bo.setProjectId(projectId);
        }
        data.setData(sdpDataSourceService.dataSourceList(bo));
        return data;
    }
    @ApiOperation(value="数据源新增")
    @ValidateProject
    @RequestMapping(value="/add",method= RequestMethod.POST)
    public ResponseData addDataSource(@RequestBody SdpDataSource sds,@RequestHeader(value="projectId") Long projectId) {
        ResponseData data = new ResponseData<>();
        data.ok();
        if (Objects.nonNull(projectId)) {
            sds.setProjectId(projectId);
        }
        data.setData(sdpDataSourceService.add(sds));
        return data;
    }

    @ApiOperation(value="数据源更新")
    @RequestMapping(value="/update",method= RequestMethod.POST)
    public ResponseData updateInstance(@RequestBody SdpDataSource sds,@RequestHeader(value="projectId") Long projectId) {
        ResponseData data = new ResponseData<>();
        data.ok();
        if (Objects.nonNull(projectId)) {
            sds.setProjectId(projectId);
        }
        data.setData(sdpDataSourceService.updateInstance(sds));
        return data;
    }
    @ApiOperation(value="数据源删除")
    @RequestMapping(value="/delete",method= RequestMethod.POST)
    public ResponseData deleteInstance(@RequestBody SdpDataSource sds) {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(sdpDataSourceService.deleteInstance(sds));
        return data;
    }

    @ApiOperation(value="数据源连通测试")
    @RequestMapping(value="/checkConnect",method= RequestMethod.POST)
    public ResponseData checkConnect(@RequestBody SdpDataSource sds,@RequestHeader(value="projectId") Long projectId) {
        sds.setProjectId(projectId);
        return sdpDataSourceService.checkConnect(sds);
    }
    @ApiOperation(value="模糊搜索数据源")
    @RequestMapping(value="/getDataSources",method= RequestMethod.POST)
    public ResponseData getDataSources(@RequestBody SdpDataSource sds,@RequestHeader(value="projectId") Long projectId) {
        if (Objects.nonNull(projectId)) {
            sds.setProjectId(projectId);
        }
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(sdpDataSourceService.getDataSources(sds));
        return data;
    }




    @ApiOperation(value="hive集群列表查询")
    @RequestMapping(value="/hiveClusterList",method= RequestMethod.POST)
    public ResponseData hiveClusterList(@RequestBody(required = false) SdpDataSourceBO bo) {
        ResponseData data = new ResponseData<>();
        String env = sdpConfig.getEnvFromEnvHolder(logger);
        DataSourceConfigProperties.HiveConfig hiveConfig = dataSourceConfigProperties.getEnvMap().get(env);
        List<DataSourceConfigProperties.HiveClusterInfo> datas = hiveConfig.getHive();
        //hive
        data.setData(datas).ok();
        return data;
    }





}