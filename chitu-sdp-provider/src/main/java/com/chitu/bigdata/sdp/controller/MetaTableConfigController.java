

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-12-10
  * </pre>
  */

package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.api.bo.SdpDataSourceBO;
import com.chitu.bigdata.sdp.api.bo.SdpMetaTableConfigBO;
import com.chitu.bigdata.sdp.api.domain.MetaTableConfigBatchDomain;
import com.chitu.bigdata.sdp.api.model.SdpMetaTableConfig;
import com.chitu.bigdata.sdp.api.utils.QueryGroup;
import com.chitu.bigdata.sdp.service.MetaTableConfigService;
import com.chitu.bigdata.sdp.service.SdpMetaTableRelationService;
import com.chitu.cloud.model.ResponseData;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

/**
 * <pre>
 * 表现层控制类
 * </pre>
 */
@RestController
@Api(tags="元表配置")
@RequestMapping(value = "/meta/table")
@Validated
public class MetaTableConfigController {

    @Autowired
    private MetaTableConfigService metaTableConfigService;
    @Autowired
    private SdpMetaTableRelationService relationService;

    @ApiOperation(value="保存元表配置")
    @RequestMapping(value="/add",method= RequestMethod.POST)
    public ResponseData add(@RequestBody @Valid List<SdpMetaTableConfig> metaTableConfigList) {
        return metaTableConfigService.addMetaTableConfig(metaTableConfigList);
    }

    @ApiOperation(value="删除元表")
    @RequestMapping(value="/delete",method= RequestMethod.POST)
    public ResponseData delete(@RequestBody SdpMetaTableConfigBO sdpMetaTableConfigBO) {
        return metaTableConfigService.deleteMetaTableConfig(sdpMetaTableConfigBO);
    }

    @ApiOperation(value="查询元表配置")
    @RequestMapping(value="/query",method= RequestMethod.POST)
    public ResponseData query(@RequestBody @Validated(QueryGroup.class) SdpMetaTableConfigBO sdpMetaTableConfigBO) {
        return metaTableConfigService.queryMetaTableConfig(sdpMetaTableConfigBO);
    }

    @ApiOperation(value="元表名验证")
    @RequestMapping(value="/existVerify",method= RequestMethod.POST)
    public ResponseData existVerify(@RequestBody @Validated(SdpMetaTableConfigBO.Verify.class) SdpMetaTableConfigBO sdpMetaTableConfigBO) {
        return metaTableConfigService.tableExistVerify(sdpMetaTableConfigBO);
    }

    @ApiOperation(value="生成ddl")
    @RequestMapping(value="/generateDdl",method= RequestMethod.POST)
    public ResponseData generateDdl(@RequestBody @Validated(SdpMetaTableConfigBO.Generate.class) SdpMetaTableConfigBO sdpMetaTableConfigBO) {
        return metaTableConfigService.generateDdl(sdpMetaTableConfigBO);
    }

    @ApiOperation(value="数据源管理,查询元表列表")
    @RequestMapping(value="/select",method= RequestMethod.POST)
    public ResponseData JobListByDataSource(@RequestBody @Validated SdpDataSourceBO datasourceBo, @RequestHeader(value="projectId")  Long projectId){
        ResponseData data = new ResponseData<>();
        data.ok();
        datasourceBo.setProjectId(projectId);
        return  data.setData(relationService.metaTableListByDataSource(datasourceBo));
    }

    /**
     * 使用注意事项：只是刚开始初始化的时候使用，因为里面的逻辑是删除才新增回去，只能初始化使用
     * @param domain
     * @param projectId
     * @return
     */
    @ApiOperation(value="postman批量生成元表")
    @RequestMapping(value="/batchAddMetaTableConfig",method= RequestMethod.POST)
    public ResponseData batchAddMetaTableConfig(@RequestBody  List<MetaTableConfigBatchDomain> domains){
        ResponseData data = new ResponseData<>();
        data.ok();
        return  data.setData(metaTableConfigService.addMetaTableConfigs(domains));
    }


}