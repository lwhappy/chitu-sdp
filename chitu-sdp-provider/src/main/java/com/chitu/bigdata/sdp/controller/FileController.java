

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.controller;

import com.chitu.bigdata.sdp.api.bo.SdpFileBO;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.SdpFile;
import com.chitu.bigdata.sdp.api.model.SdpJob;
import com.chitu.bigdata.sdp.api.vo.SdpFileResp;
import com.chitu.bigdata.sdp.mapper.SdpUserMapper;
import com.chitu.bigdata.sdp.service.FileService;
import com.chitu.bigdata.sdp.service.SdpSysConfigService;
import com.chitu.bigdata.sdp.service.validate.domain.SqlExplainResult;
import com.chitu.cloud.model.ResponseData;
import com.chitu.cloud.utils.Context;
import com.chitu.cloud.utils.ContextUtils;
import com.chitu.cloud.utils.StringUtils;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.concurrent.Executor;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/17 14:06
 */
@RestController
@Api(tags="文件管理")
@RequestMapping(value = "/file")
@Slf4j
public class FileController {

    @Autowired
    private FileService fileService;
    @Autowired
    private SdpUserMapper userMapper;
    @Autowired
    private SdpSysConfigService sdpSysConfigService;



    @Autowired
    private Executor monitorExecutor;

    @ApiOperation(value="添加作业")
    @RequestMapping(value="/addFile",method= RequestMethod.POST)
    public ResponseData addFile(@RequestBody @Validated(SdpFileBO.ADD.class) SdpFileBO fileBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(fileService.addFile(fileBO));
        return data;
    }

    @ApiOperation(value="查询作业")
    @RequestMapping(value="/queryFile",method= RequestMethod.POST)
    public ResponseData queryFile(@RequestBody SdpFileBO fileBO,@RequestHeader(value="projectId") Long projectId){
        ResponseData data = new ResponseData<>();
        data.ok();
        fileBO.setProjectId(projectId);
        data.setData(fileService.queryFile(fileBO));
        return data;
    }
     //进行文件编辑锁定
    @ApiOperation(value="作业详情[编辑]")
    @RequestMapping(value="/detailFile",method= RequestMethod.POST)
    public ResponseData detailFile(@RequestBody SdpFileBO fileBO){
        SdpFileResp detailFile = fileService.getDetailFile(fileBO);
        if(Objects.isNull(detailFile)){
            ResponseData data = new ResponseData(ResponseCode.FILE_NOT_EXISTS);
            return data;
        }else {
            ResponseData data = new ResponseData<>();
            data.ok();
            data.setData(detailFile);
            return data;
        }
    }


    @ApiOperation(value="修改作业")
    @RequestMapping(value="/updateFile",method= RequestMethod.POST)
    public ResponseData updateFile(@RequestBody SdpFileBO fileBO,@RequestHeader(value="projectId") Long projectId){
        ResponseData data = new ResponseData<>();
        data.ok();
        fileBO.setProjectId(projectId);
        data.setData(fileService.updateFile(fileBO));
        return data;
    }

    @ApiOperation(value="移动作业")
    @RequestMapping(value="/removeFile",method= RequestMethod.POST)
    public ResponseData removeFile(@RequestBody SdpFileBO fileBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(fileService.removeFile(fileBO));
        return data;
    }

    @ApiOperation(value="文件复制")
    @RequestMapping(value="/copyFile",method= RequestMethod.POST)
    public ResponseData copyFile(@RequestBody SdpFileBO fileBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(fileService.copyFile(fileBO));
        return data;
    }

    @ApiOperation(value="删除作业")
    @RequestMapping(value="/deleteFile",method= RequestMethod.POST)
    public ResponseData deleteFile(@RequestBody SdpFileBO fileBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(fileService.deleteFile(fileBO));
        return data;
    }

    @ApiOperation(value="作业校验[升级版]")
    @RequestMapping(value="/validate",method= RequestMethod.POST)
    public ResponseData validate(@RequestBody SdpFileBO fileBO,@RequestHeader(value="env") String env){
        log.info("进入validate...");
        ResponseData data = new ResponseData<>();
        List<SqlExplainResult> datas = fileService.explainSql(fileBO);
        if(datas.isEmpty()){
            data.setCode(ResponseCode.SQL_WARN_PASS.getCode());
            data.setMsg(ResponseCode.SQL_WARN_PASS.getMessage());
            //告警校验
            String warnMsg = fileService.hintMsg(fileBO);
            if(StringUtils.isNotEmpty(warnMsg)){
                String[] warns = warnMsg.split(";");
                Arrays.stream(warns).forEach(x->{
                    SqlExplainResult explainResult = new SqlExplainResult();
                    explainResult.setError(x);
                    explainResult.setType("warn");
                    datas.add(explainResult);
                });
            }else{
                data.setCode(ResponseCode.SQL_OK_PASS.getCode());
                data.setMsg(ResponseCode.SQL_OK_PASS.getMessage());
            }
        }else{
            data.setCode(ResponseCode.SQL_NOT_PASS.getCode());
            data.setMsg(ResponseCode.SQL_NOT_PASS.getMessage());
            //告警校验
            String warnMsg = fileService.hintMsg(fileBO);
            if(StringUtils.isNotEmpty(warnMsg)){
                String[] warns = warnMsg.split(";");
                Arrays.stream(warns).forEach(x->{
                    SqlExplainResult explainResult = new SqlExplainResult();
                    explainResult.setError(x);
                    explainResult.setType("warn");
                    datas.add(explainResult);
                });
            }
        }
        data.setData(datas);
        log.info("validate完成...");
        return data;
    }

    @ApiOperation(value="作业校验[白名单验证完成立即上线]")
    @RequestMapping(value="/validateOnline4WhiteList",method= RequestMethod.POST)
    public ResponseData validateOnline(@RequestBody SdpFileBO fileBO){
        ResponseData data = new ResponseData<>();
        List<SqlExplainResult> datas = fileService.explainSql(fileBO);
        if(datas.isEmpty()){
            data.setCode(ResponseCode.SQL_WARN_PASS.getCode());
            data.setMsg(ResponseCode.SQL_WARN_PASS.getMessage());
            //告警校验
            String warnMsg = fileService.hintMsg(fileBO);
            if(StringUtils.isNotEmpty(warnMsg)){
                String[] warns = warnMsg.split(";");
                Arrays.stream(warns).forEach(x->{
                    SqlExplainResult explainResult = new SqlExplainResult();
                    explainResult.setError(x);
                    explainResult.setType("warn");
                    datas.add(explainResult);
                });
            }else{
                data.setCode(ResponseCode.SQL_OK_PASS.getCode());
                data.setMsg(ResponseCode.SQL_OK_PASS.getMessage());
            }
            //如果是白名单，则不需要申请，直接上线
            if (sdpSysConfigService.isWhiteList()){
                Context context = ContextUtils.get();
                //如果当前人员是白名单人员无需审批，直接上线
                SdpFileBO fileBO1 = new SdpFileBO();
                fileBO1.setId(fileBO.getId());
                fileBO1.setRemark(fileBO.getRemark());
                fileBO1.setUserId(Long.valueOf(context.getUserId()));
                SdpJob online = fileService.online(fileBO1);
                log.info("异步生成血缘...");
                // TODO 上线成功(提交完事务)异步生成血缘并更新db, 共用监控线程池
            }
        }else{
            data.setCode(ResponseCode.SQL_NOT_PASS.getCode());
            data.setMsg(ResponseCode.SQL_NOT_PASS.getMessage());
            //告警校验
            String warnMsg = fileService.hintMsg(fileBO);
            if(StringUtils.isNotEmpty(warnMsg)){
                String[] warns = warnMsg.split(";");
                Arrays.stream(warns).forEach(x->{
                    SqlExplainResult explainResult = new SqlExplainResult();
                    explainResult.setError(x);
                    explainResult.setType("warn");
                    datas.add(explainResult);
                });
            }
        }
        data.setData(datas);
        return data;
    }

    @ApiOperation(value="作业上线")
    @RequestMapping(value="/online",method= RequestMethod.POST)
    public ResponseData online(@RequestBody SdpFileBO fileBO) {
        ResponseData data = new ResponseData<>();
        SdpJob online = fileService.online(fileBO);
        // TODO 上线成功(提交完事务)异步生成血缘并更新db, 共用监控线程池
        data.ok();
        data.setData(online);
        return data;
    }

    @ApiOperation(value="解锁作业")
    @RequestMapping(value="/unlockFile",method= RequestMethod.POST)
    public ResponseData unlockFile(@RequestBody SdpFileBO fileBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(fileService.lockFile(fileBO));
        return data;
    }

    @ApiOperation(value="项目获取引擎")
    @RequestMapping(value="/getFileEngines",method= RequestMethod.POST)
    public ResponseData getFileEngines(@RequestBody SdpFileBO fileBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(fileService.getFileEngines(fileBO));
        return data;
    }

    @ApiOperation(value="获取文件状态")
    @RequestMapping(value="/checkState",method= RequestMethod.POST)
    public ResponseData checkState(@RequestBody List<Long> fileBO){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(fileService.checkState(fileBO));
        return data;
    }
    @ApiOperation(value="获取用户历史作业列表")
    @RequestMapping(value="/fileHistory",method= RequestMethod.POST)
    public ResponseData fileHistory(){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(fileService.fileHistory());
        return data;
    }

    @ApiOperation(value="生成DAG")
    @RequestMapping(value="/getJobDAG",method= RequestMethod.POST)
    public ResponseData getJobDAG(@RequestBody SdpFileBO fileBO) {
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(fileService.getJobDAG(fileBO));
        return data;
    }

    @ApiOperation(value="查询基础信息")
    @RequestMapping(value="/queryBaseInfo",method= RequestMethod.POST)
    public ResponseData queryBaseInfo(@RequestBody SdpFileBO fileBO) {
        return fileService.queryBaseInfo(fileBO);
    }

    @ApiOperation(value="修改基础信息")
    @RequestMapping(value="/updateBaseInfo",method= RequestMethod.POST)
    public ResponseData updateBaseInfo(@RequestBody SdpFileBO fileBO) {
        return fileService.updateBaseInfo(fileBO);
    }


    @ApiOperation(value="处理元表关系历史数据")
    @RequestMapping(value="/handleHistoryData4MetaTableRelation",method= RequestMethod.POST)
    public ResponseData handleHistoryData4MetaTableRelation() {
        fileService.handleHistoryData4MetaTableRelation();
        ResponseData result = new ResponseData();
        result.ok();
        return  result;
    }

    @ApiOperation(value="全系统批量执行SQL校验")
    @RequestMapping(value="/sqlValidateFull",method= RequestMethod.GET)
    public ResponseData sqlValidateFull(@RequestParam("enabledFlag") Long enabledFlag) {
        ResponseData data = new ResponseData<>();
        Map result = new HashMap<>();
        List<SdpFile> fileList = fileService.selectAll(new SdpFile(enabledFlag));
        fileList.forEach(x->{
            SdpFileBO fileBO = new SdpFileBO();
            fileBO.setId(x.getId());
            List<SqlExplainResult> datas = fileService.explainSql(fileBO);
            //告警校验
            String warnMsg = fileService.hintMsg(fileBO);
            if(StringUtils.isNotEmpty(warnMsg)){
                String[] warns = warnMsg.split(";");
                Arrays.stream(warns).forEach(z->{
                    SqlExplainResult explainResult = new SqlExplainResult();
                    explainResult.setError(z);
                    explainResult.setType("warn");
                    datas.add(explainResult);
                });
            }
            result.put(x.getFileName(),datas);
        });
        data.setData(result);
        data.setCode(ResponseCode.SUCCESS.getCode());
        data.setMsg(ResponseCode.SUCCESS.getMessage());
        return data;
    }



    @ApiOperation(value="文件是否存在")
    @RequestMapping(value="/fileExist",method= RequestMethod.POST)
    public ResponseData fileExist(@RequestBody SdpFileBO fileBO,@RequestHeader(value="projectId") Long projectId){
        fileBO.setProjectId(projectId);
        Map<String, Boolean> stringBooleanMap = fileService.fileExist(fileBO);
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData(stringBooleanMap);
        return data;

    }

    @ApiOperation(value="是否允许新增作业")
    @RequestMapping(value="/allowAddFile",method= RequestMethod.POST)
    public ResponseData allowAddJob(@RequestHeader(value="projectId") Long projectId){
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData( fileService.allowAddJob(projectId));
        return data;
    }

    @ApiOperation(value="是否允许编辑作业")
    @RequestMapping(value="/allowEditFile",method= RequestMethod.POST)
    public ResponseData allowEditJob(@RequestBody SdpFileBO fileBO,@RequestHeader(value="projectId") Long projectId){
        fileBO.setProjectId(projectId);
        ResponseData data = new ResponseData<>();
        data.ok();
        data.setData( fileService.allowEditJob(fileBO));
        return data;
    }
}