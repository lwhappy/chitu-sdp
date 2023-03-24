package com.chitu.bigdata.sdp.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.chitu.bigdata.sdp.api.bo.*;
import com.chitu.bigdata.sdp.api.domain.*;
import com.chitu.bigdata.sdp.api.enums.ApproveStatus;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.*;
import com.chitu.bigdata.sdp.mapper.SdpFileMapper;
import com.chitu.bigdata.sdp.mapper.SdpProjectUserMapper;
import com.chitu.bigdata.sdp.mapper.SdpUserMapper;
import com.chitu.cloud.model.ResponseData;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.entity.ContentType;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/12/2 9:52
 */

//@RunWith(SpringRunner.class)
//@SpringBootTest
@AutoConfigureMockMvc
//@RestController
@Slf4j
public class GlobalTestController {

    @Autowired
    private MockMvc mvc;
    @Autowired
    private SdpFileMapper fileMapper;
    @Autowired
    private SdpUserMapper userMapper;
    @Autowired
    private SdpProjectUserMapper projectUserMapper;

    private final Long projectId = 60L;
    private final Long uid = 123L;
    private final String commonField = "CHENYUN_TEST";
    private SdpEngine engine = null;
    private SdpProject project = null;
    private SdpFolder folder = null;
    private SdpJar jar = null;
    private SdpFile file = null;
    private SdpJob sdpJob = null;
    private SdpApprove approve = null;


    @ApiOperation(value="集成测试")
    @RequestMapping(value="/globalTest",method= RequestMethod.POST)
    public ResponseData globalTest(){
        ResponseData data = new ResponseData<>();
        data.ok();
        try{
            testAddEngine();
        }catch (Exception e){
            System.out.println("===集成测试异常===="+e);
            failedExecute();
        }
        return data;
    }

    @Test
    public void globalTest1(){
        try{
            testAddEngine();
        }catch (Exception e){
            System.out.println("===集成测试异常===="+e);
        }
    }

    //添加引擎
    @Test
    public void testAddEngine() throws Exception{
        SdpEngineInfo engineBO = new SdpEngineInfo();
        List<SdpUser> userInfo = new ArrayList<>();
        SdpUser user = new SdpUser();
        user.setUserName("陈赟");
        user.setEmployeeNumber("631716");
        userInfo.add(user);

        engineBO.setUserInfo(userInfo);
        engineBO.setEngineName(commonField);
        engineBO.setEngineVersion("1.14");
        engineBO.setEngineQueue("report_queue");
        engineBO.setEngineCluster("yarn");
        String mvcResult = this.mvc
                .perform(post("/setting/engineSetting/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(engineBO))
                        .header("projectId",projectId).header("X-uid",uid)
                ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JSONObject result = JSON.parseObject(mvcResult);
        if(result.getInteger("code") == 0 && result.get("msg").equals("OK")){
            engine = result.getObject("data", SdpEngine.class);
            if(engine != null){
                testAddProject();
            }
        }else{
            log.error("新建引擎失败");
        }
    }

    //添加项目
    @Test
    public void testAddProject() throws Exception{
        SdpProjectInfo projectBO = new SdpProjectInfo();
        List<SdpEngine> projectEngines = new ArrayList<>();
        projectEngines.add(engine);
        List<SdpUser> projectLeader = new ArrayList<>();
        SdpUser sdpUser = new SdpUser();
        sdpUser.setUserName("陈赟");
        sdpUser.setEmployeeNumber("631716");
        projectLeader.add(sdpUser);
        List<ProjectUser> projectUsers = new ArrayList<>();
        ProjectUser user = new ProjectUser();
        user.setUserName("李振用");
        user.setEmployeeNumber("631723");
        projectUsers.add(user);

        projectBO.setProjectEngines(projectEngines);
        projectBO.setProjectLeader(projectLeader);
        projectBO.setProjectUsers(projectUsers);
        projectBO.setProjectName(commonField);
        projectBO.setProjectCode(commonField);
        String mvcResult = this.mvc
                .perform(post("/project/projectManagement/add")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(projectBO))
                        .header("projectId",projectId).header("X-uid",uid)
                ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JSONObject result = JSON.parseObject(mvcResult);
        if(result.getInteger("code") == 0 && result.get("msg").equals("OK")){
            project = result.getObject("data", SdpProject.class);
            if(project != null){
                testAddFolder();
            }
        }else{
            log.error("新建项目失败");
            failedExecute();
        }
    }

    //添加目录
    @Test
    public void testAddFolder() throws Exception{
        SdpFolderBO folderBO = new SdpFolderBO();
        folderBO.setProjectId(project.getId());
        folderBO.setFolderName(commonField);
        folderBO.setParentId(0L);
        String mvcResult = this.mvc
                .perform(post("/folder/addFolder")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(folderBO))
                        .header("projectId",projectId).header("X-uid",uid)
                ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JSONObject result = JSON.parseObject(mvcResult);
        if(result.getInteger("code") == 0 && result.get("msg").equals("OK")){
            folder = result.getObject("data", SdpFolder.class);
            if(folder != null){
                testAddJar();
            }
        }else{
            log.error("新建目录失败");
            failedExecute();
        }
    }

    //上传JAR
    @Test
    public void testAddJar() throws Exception{
        String jarPath;
        String osName = System.getProperties().getProperty("os.name");
        if(osName.equals("Windows 10")){
            jarPath = "D:\\workspace\\bigdata-flink-udf\\target\\";
        }else{
            jarPath = "/home/dev/";
        }
        File file = new File(jarPath + "bigdata-flink-udf.jar");
        FileInputStream  ins = new FileInputStream(file);
        MockMultipartFile multiFile = new MockMultipartFile("file",commonField+".jar", ContentType.MULTIPART_FORM_DATA.getMimeType(), ins);
        String mvcResult = this.mvc
                .perform(multipart("/jar/addJar")
                        .file(multiFile)
                        .header("projectId",projectId)
                        .header("X-uid",uid)
                        .header("version","ADD")
                        .header("name",commonField)
                        .header("git","*******")
                        .header("description",commonField)
                        .header("type","ADD")
                ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JSONObject result = JSON.parseObject(mvcResult);
        if(result.getInteger("code") == 0 && result.get("msg").equals("OK")){
            jar = result.getObject("data", SdpJar.class);
            if(jar != null){
                testAddFile();
            }
        }else{
            log.error(ResponseCode.JAR_UPLOAD_FAILED.getMessage());
            failedExecute();
        }
    }

    //添加文件
    @Test
    public void testAddFile() throws Exception{
        SdpFileBO fileBO = new SdpFileBO();
        JobConfig jobConfig = new JobConfig();
        jobConfig.setJarId(jar.getId());
        jobConfig.setJarName(jar.getName());
        jobConfig.setJarVersion(jar.getVersion());
        jobConfig.setEngineId(engine.getId());
        jobConfig.setEngine(engine.getEngineName());
        jobConfig.setVersion(engine.getEngineVersion());
//        jobConfig.setFlinkYaml(
//                "restart-strategy: fixed-delay\n" +
//                "restart-strategy.fixed-delay.attempts: 10\n" +
//                "restart-strategy.fixed-delay.delay: 1000\n" +
//                "state.backend: hashmap\n" +
//                "state.checkpoint-storage: filesystem\n" +
//                "execution.checkpointing.interval: 5000\n" +
//                "execution.checkpointing.mode: EXACTLY_ONCE\n" +
//                "execution.checkpointing.unaligned: false\n" +
//                "execution.checkpointing.max-concurrent-checkpoints: 1  \n" +
//                "state.backend.incremental: true\n" +
//                "table.exec.state.ttl: 90000000\n" +
//                "table.exec.mini-batch.enabled: true\n" +
//                "table.exec.mini-batch.allow-latency: 10s\n" +
//                "table.exec.mini-batch.size: 1000\n" +
//                "table.optimizer.distinct-agg.split.enabled: true\n" +
//                "state.checkpoints.dir: hdfs://bigbigworld/sdp/ck/\n" +
//                "execution.checkpointing.externalized-checkpoint-retention: RETAIN_ON_CANCELLATION\n" +
//                "state.checkpoints.num-retained: 5\n" +
//                "taskmanager.numberOfTaskSlots: 1 " );

        SourceConfig sourceConfig = new SourceConfig();
        sourceConfig.setMode("基础模式");
        sourceConfig.setParallelism(1);
        sourceConfig.setJobManagerCpu(1);
        sourceConfig.setJobManagerMem("1");
        sourceConfig.setTaskManagerCpu(1);
        sourceConfig.setTaskManagerMem("1");

        String sql = "CREATE TABLE order_log (\r\n  code VARCHAR,\r\n  order_num BIGINT,\r\n  ts VARCHAR\r\n  ) WITH (\r\n  'connector' = 'kafka',\r\n  'topic' = 'order_log6',\r\n  'properties.bootstrap.servers' = 'szzb-bg-uat-etl-10:9092,szzb-bg-uat-etl-11:9092,szzb-bg-uat-etl-12:9092',\r\n  'properties.group.id' = 'sink_mysql_order_log6',\r\n  'scan.startup.mode' = 'earliest-offset',\r\n  'format' = 'json'\r\n  );\r\n\r\n  CREATE TABLE map_sink (\r\n  dt VARCHAR,\r\n  code VARCHAR,\r\n  order_num BIGINT,\r\n  PRIMARY KEY (dt,code) NOT ENFORCED\r\n  ) WITH (\r\n  'connector' = 'jdbc',\r\n  'url' = 'jdbc:mysql://******',\r\n  'table-name' = 'map',\r\n  'username' = '******',\r\n  'password' = '******',\r\n  'sink.buffer-flush.max-rows' = '1'\r\n  );\r\n\r\n  INSERT INTO map_sink\r\n  SELECT\r\n  DATE_FORMAT(ts, 'yyyy-MM-dd HH:00') dt,\r\n  code,\r\n  sum(order_num) AS order_num\r\n  FROM order_log\r\n  GROUP BY DATE_FORMAT(ts, 'yyyy-MM-dd HH:00'),code;";
        fileBO.setJobConfig(jobConfig);
        fileBO.setSourceConfig(sourceConfig);
        fileBO.setContent(sql);
        fileBO.setFileName(commonField);
        fileBO.setFileType("SQL");
        fileBO.setFolderId(folder.getId());
        fileBO.setProjectId(project.getId());
        String mvcResult = this.mvc
                .perform(post("/file/addFile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(fileBO))
                        .header("projectId",projectId).header("X-uid",uid)
                ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JSONObject result = JSON.parseObject(mvcResult);
        if(result.getInteger("code") == 0 && result.get("msg").equals("OK")){
            file = result.getObject("data", SdpFile.class);
            if(file != null){
                testFileValidate();
            }
        }else{
            log.error("新建文件失败");
            failedExecute();
        }
    }

    //SQL验证
    @Test
    public void testFileValidate() throws Exception{
        SdpFileBO fileBO = new SdpFileBO();
        fileBO.setId(file.getId());
        String mvcResult = this.mvc
                .perform(post("/file/validate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(fileBO))
                        .header("projectId",projectId).header("X-uid",uid)
                ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JSONObject result = JSON.parseObject(mvcResult);
        if(result.getInteger("code") == 0 && result.get("msg").equals("OK")){
            SdpFile file1 = result.getObject("data", SdpFile.class);
            if(file1 != null){
//                testFileOnline();
                testSubmitApply();
            }
        }else{
            log.error(ResponseCode.SQL_NOT_PASS.getMessage());
            failedExecute();
        }
    }

    //提交审批申请
    @Test
    public void testSubmitApply() throws Exception{
        SdpApproveBO approveBO = new SdpApproveBO();
        approveBO.setId(file.getId()+"");
        approveBO.setProjectName(project.getProjectName());
        approveBO.setRemark("集成测试");
        String mvcResult = this.mvc
                .perform(post("/approve/submitApply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(approveBO))
                        .header("projectId",projectId).header("X-uid",uid)
                ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JSONObject result = JSON.parseObject(mvcResult);
        if(result.getInteger("code") == 0 && result.get("msg").equals("OK")){
            SdpApprove approve = result.getObject("data", SdpApprove.class);
            if(approve != null){
                testExecuteApprove();
            }
        }else{
            log.error(ResponseCode.SQL_NOT_PASS.getMessage());
            failedExecute();
        }
    }

    //执行审批[一级审批]
    @Test
    public void testExecuteApprove() throws Exception{
        List<SdpProjectUser> leaders = projectUserMapper.getProjectLeaders(approve.getProjectId());
        Long userId = leaders.get(0).getUserId();
        SdpApproveBO approveBO = new SdpApproveBO();
        approveBO.setId(approve.getId()+"");
        approveBO.setFileId(approve.getFileId());
        approveBO.setStatus(ApproveStatus.AGREE.toString());
        approveBO.setOpinion("同意");
        String mvcResult = this.mvc
                .perform(post("/approve/executeApprove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(approveBO))
                        .header("projectId",projectId).header("X-uid",userId)
                ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JSONObject result = JSON.parseObject(mvcResult);
        if(result.getInteger("code") == 0 && result.get("msg").equals("OK")){
            approve = result.getObject("data", SdpApprove.class);
            if(approve != null){
                testExecuteApprove2();
            }
        }else{
            log.error(ResponseCode.SQL_NOT_PASS.getMessage());
            failedExecute();
        }
    }

    //执行审批[二级审批]
    @Test
    public void testExecuteApprove2() throws Exception{
        List<SdpUser> admins = userMapper.getCondition(null);
        List<Long> users = admins.stream()
                .filter(x->!x.getId().equals(123L))
                .map(x->x.getId()).collect(Collectors.toList());
        Long userId = users.get(0);
        SdpApproveBO approveBO = new SdpApproveBO();
        approveBO.setId(approve.getId()+"");
        approveBO.setFileId(approve.getFileId());
        approveBO.setStatus(ApproveStatus.AGREE.toString());
        approveBO.setOpinion("同意");
        String mvcResult = this.mvc
                .perform(post("/approve/executeApprove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(approveBO))
                        .header("projectId",projectId).header("X-uid",userId)
                ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JSONObject result = JSON.parseObject(mvcResult);
        if(result.getInteger("code") == 0 && result.get("msg").equals("OK")){
            sdpJob = result.getObject("data", SdpJob.class);
            if(sdpJob != null){
                testJobStart();
            }
        }else{
            log.error(ResponseCode.SQL_NOT_PASS.getMessage());
            failedExecute();
        }
    }

    //文件上线【上线接口废弃，审批通过后，自动调用上线接口】
    @Test
    public void testFileOnline() throws Exception{
        SdpFileBO fileBO = new SdpFileBO();
        fileBO.setId(file.getId());
        fileBO.setRemark("作业上线");
        String mvcResult = this.mvc
                .perform(post("/file/online")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(fileBO))
                        .header("projectId",projectId).header("X-uid",uid)
                ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        JSONObject result = JSON.parseObject(mvcResult);
        if(result.getInteger("code") == 0 && result.get("msg").equals("OK")){
            sdpJob = result.getObject("data", SdpJob.class);
            if(sdpJob != null){
                testJobStart();
            }
        }else{
            log.error("作业上线失败");
            failedExecute();
        }
    }

    //启动作业
    @Test
    public void testJobStart() throws Exception{
        if(null != sdpJob){
            List<SdpJobBO> jobBOs = new ArrayList<>();
            SdpJobBO jobBO = new SdpJobBO();
            jobBO.setVo(sdpJob);
            jobBO.setUseLatest(true);
            jobBOs.add(jobBO);
            String mvcResult = this.mvc
                    .perform(post("/job/startJob")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSONArray.toJSONString(jobBOs, SerializerFeature.DisableCircularReferenceDetect))
                            .header("projectId",projectId).header("X-uid",uid)
                    ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            JSONObject result = JSON.parseObject(mvcResult);
            if(result.getInteger("code") == 0 && result.get("msg").equals("OK")){
                Thread.sleep(200000);
                testJobPaused();
            }else{
                log.error("作业启动失败");
                failedExecute();
            }
        }
    }

    //暂停作业
    @Test
    public void testJobPaused() throws Exception{
        if(null != sdpJob){
            SdpJobBO jobBO = new SdpJobBO();
            jobBO.setVo(sdpJob);
            String mvcResult = this.mvc
                    .perform(post("/job/pauseJob")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON.toJSONString(jobBO, SerializerFeature.DisableCircularReferenceDetect))
                            .header("projectId",projectId).header("X-uid",uid)
                    ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            JSONObject result = JSON.parseObject(mvcResult);
            if(result.getInteger("code") == 0 && result.get("msg").equals("OK")){
                Thread.sleep(30000);
                testJobRecover();
            }else{
                log.error("作业暂停失败");
                failedExecute();
            }
        }
    }

    //恢复作业
    @Test
    public void testJobRecover() throws Exception{
        if(null != sdpJob){
            SdpJobBO jobBO = new SdpJobBO();
            jobBO.setVo(sdpJob);
            jobBO.setUseLatest(false);
            String mvcResult = this.mvc
                    .perform(post("/job/recoverJob")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON.toJSONString(jobBO, SerializerFeature.DisableCircularReferenceDetect))
                            .header("projectId",projectId).header("X-uid",uid)
                    ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            JSONObject result = JSON.parseObject(mvcResult);
            if(result.getInteger("code") == 0 && result.get("msg").equals("OK")){
                Thread.sleep(30000);
                testJobStop();
            }else{
                log.error("作业恢复失败");
                failedExecute();
            }
        }
    }

    //停止作业
    @Test
    public void testJobStop() throws Exception{
        if(null != sdpJob){
            List<SdpJobBO> jobBOs = new ArrayList<>();
            SdpJobBO jobBO = new SdpJobBO();
            jobBO.setVo(sdpJob);
            jobBOs.add(jobBO);
            String mvcResult = this.mvc
                    .perform(post("/job/stopJob")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSONArray.toJSONString(jobBOs, SerializerFeature.DisableCircularReferenceDetect))
                            .header("projectId",projectId).header("X-uid",uid)
                    ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            JSONObject result = JSON.parseObject(mvcResult);
            if(result.getInteger("code") == 0 && result.get("msg").equals("OK")){
                testDeleteJob();
            }else{
                log.error("作业停止失败");
                failedExecute();
            }
        }
    }

    //删除作业
    @Test
    public void testDeleteJob() throws Exception{
        if(null != sdpJob){
            List<SdpJob> jobBOs = new ArrayList<>();
            jobBOs.add(sdpJob);
            String mvcResult = this.mvc
                    .perform(post("/job/deleteJob")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSONArray.toJSONString(jobBOs))
                            .header("projectId",projectId).header("X-uid",uid)
                    ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            JSONObject result = JSON.parseObject(mvcResult);
            if(result.getInteger("code") == 0 && result.get("msg").equals("OK")){
                List<Integer> ids = result.getObject("data", ArrayList.class);
                if(!CollectionUtils.isEmpty(ids)){
                        testDeleteFile();
                }else{
                    log.error("作业删除失败");
                }
            }
        }
    }

    //删除文件
    @Test
    public void testDeleteFile() throws Exception{
        if(null != file){
            SdpFileBO fileBO = new SdpFileBO();
            fileBO.setId(file.getId());
            String mvcResult = this.mvc
                    .perform(post("/file/deleteFile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON.toJSONString(fileBO))
                            .header("projectId",projectId).header("X-uid",uid)
                    ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            JSONObject result = JSON.parseObject(mvcResult);
            if(result.getInteger("code") == 0 && result.get("msg").equals("OK")){
                if(result.getInteger("date") != null){
                    testDeleteJar();
                }
            }else{
                log.error("文件删除失败");
            }
        }
    }

    //删除JAR
    @Test
    public void testDeleteJar() throws Exception{
        if(null != jar){
            SdpJarBO jarBO = new SdpJarBO();
            jarBO.setId(String.valueOf(jar.getId()));
            String mvcResult = this.mvc
                    .perform(post("/jar/deleteJar")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON.toJSONString(jarBO))
                            .header("projectId",projectId).header("X-uid",uid)
                    ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            JSONObject result = JSON.parseObject(mvcResult);
            if(result.getInteger("code") == 0 && result.get("msg").equals("OK")){
                if(result.getInteger("date") != null){
                    testDeleteFolder();
                }
            }else{
                log.error("删除JAR失败");
            }
        }
    }

    //删除目录
    @Test
    public void testDeleteFolder() throws Exception{
        if(null != folder){
            SdpFolderBO folderBO = new SdpFolderBO();
            folderBO.setId(folder.getId());
            String mvcResult = this.mvc
                    .perform(post("/folder/deleteFolder")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON.toJSONString(folderBO))
                            .header("projectId",projectId).header("X-uid",uid)
                    ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            JSONObject result = JSON.parseObject(mvcResult);
            if(result.getInteger("code") == 0 && result.get("msg").equals("OK")){
                if(result.getInteger("date") != null){
                    testDeleteProject();
                }
            }else{
                log.error("删除目录失败");
            }
        }
    }

    //删除项目
    @Test
    public void testDeleteProject() throws Exception{
        if(null != project){
            SdpProjectInfo projectBO = new SdpProjectInfo();
            projectBO.setId(project.getId());
            String mvcResult = this.mvc
                    .perform(post("/project/projectManagement/delete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON.toJSONString(projectBO))
                            .header("projectId",projectId).header("X-uid",uid)
                    ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            JSONObject result = JSON.parseObject(mvcResult);
            if(result.getInteger("code") == 0 && result.get("msg").equals("OK")){
                if(result.getInteger("date") != null){
                    testDeleteEngine();
                }
            }else{
                log.error("删除项目失败");
            }
        }
    }

    //删除引擎
    @Test
    public void testDeleteEngine() throws Exception{
        if(engine != null){
            SdpEngine engineBO = new SdpEngine();
            engineBO.setId(engine.getId());
            String mvcResult = this.mvc
                    .perform(post("/setting/engineSetting/delete")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(JSON.toJSONString(engineBO))
                            .header("projectId",projectId).header("X-uid",uid)
                    ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
            JSONObject result = JSON.parseObject(mvcResult);
            if(result.getInteger("code") == 0 && result.get("msg").equals("OK")){
                if(result.getInteger("date") != null){
                    System.out.println("===集成测试完成===");
                }
            }else{
                log.error("删除引擎失败");
            }
        }
    }

    private void failedExecute() {
        try{
            testDeleteJob();
        }catch (Exception e1){
            e1.printStackTrace();
        }
        try{
            testDeleteFile();
        }catch (Exception e1){
            e1.printStackTrace();
        }
        try{
            testDeleteJar();
        }catch (Exception e1){
            e1.printStackTrace();
        }
        try{
            testDeleteFolder();
        }catch (Exception e1){
            e1.printStackTrace();
        }
        try{
            testDeleteProject();
        }catch (Exception e1){
            e1.printStackTrace();
        }
        try{
            testDeleteEngine();
        }catch (Exception e1){
            e1.printStackTrace();
        }
    }

}
