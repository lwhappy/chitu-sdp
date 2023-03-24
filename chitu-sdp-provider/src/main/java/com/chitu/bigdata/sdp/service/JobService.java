package com.chitu.bigdata.sdp.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ReUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.chitu.bigdata.sdp.api.bo.SdpDataSourceBO;
import com.chitu.bigdata.sdp.api.bo.SdpFileBO;
import com.chitu.bigdata.sdp.api.bo.SdpJarBO;
import com.chitu.bigdata.sdp.api.bo.SdpJobBO;
import com.chitu.bigdata.sdp.api.domain.*;
import com.chitu.bigdata.sdp.api.enums.*;
import com.chitu.bigdata.sdp.api.flink.AppInfoList;
import com.chitu.bigdata.sdp.api.flink.Application;
import com.chitu.bigdata.sdp.api.flink.Checkpoint;
import com.chitu.bigdata.sdp.api.model.*;
import com.chitu.bigdata.sdp.api.vo.DatasourceJobInf;
import com.chitu.bigdata.sdp.api.vo.DirectStartDataVO;
import com.chitu.bigdata.sdp.api.vo.UatJobRunningConfigVO;
import com.chitu.bigdata.sdp.config.*;
import com.chitu.bigdata.sdp.constant.BusinessFlag;
import com.chitu.bigdata.sdp.constant.CommonConstant;
import com.chitu.bigdata.sdp.constant.FlinkConfigKeyConstant;
import com.chitu.bigdata.sdp.flink.common.conf.ConfigConst;
import com.chitu.bigdata.sdp.flink.common.enums.ApplicationType;
import com.chitu.bigdata.sdp.flink.common.enums.DevelopmentMode;
import com.chitu.bigdata.sdp.flink.common.enums.ExecutionMode;
import com.chitu.bigdata.sdp.flink.common.enums.ResolveOrder;
import com.chitu.bigdata.sdp.flink.common.util.HadoopUtils;
import com.chitu.bigdata.sdp.flink.common.util.HdfsUtils;
import com.chitu.bigdata.sdp.flink.common.util.*;
import com.chitu.bigdata.sdp.mapper.*;
import com.chitu.bigdata.sdp.service.datasource.KafkaDataSource;
import com.chitu.bigdata.sdp.service.monitor.JobStatusNotifyService;
import com.chitu.bigdata.sdp.service.validate.constant.FlinkSQLConstant;
import com.chitu.bigdata.sdp.service.validate.util.SqlUtil;
import com.chitu.bigdata.sdp.utils.DateUtils;
import com.chitu.bigdata.sdp.utils.JsonUtils;
import com.chitu.bigdata.sdp.utils.*;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.model.Pagination;
import com.chitu.cloud.model.ResponseData;
import com.chitu.cloud.service.GenericService;
import com.chitu.cloud.utils.Context;
import com.chitu.cloud.utils.ContextUtils;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.google.common.collect.Lists;
import com.xiaoleilu.hutool.date.DateUtil;
import com.xiaoleilu.hutool.util.CollectionUtil;
import io.fabric8.kubernetes.api.model.ConfigMap;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.dsl.Resource;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.sql.parser.ddl.SqlCreateCatalog;
import org.apache.flink.sql.parser.ddl.SqlCreateTable;
import org.apache.flink.sql.parser.ddl.SqlTableOption;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.config.SaslConfigs;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import scala.collection.JavaConversions;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/13 14:11
 */
@RefreshScope
@Service
@Slf4j
public class JobService extends GenericService<SdpJob, Long> {

    public JobService(@Autowired SdpJobMapper sdpJobMapper) {
        super(sdpJobMapper);
    }

    public SdpJobMapper getMapper() {
        return (SdpJobMapper) super.genericMapper;
    }

    @Autowired
    private JobInstanceService instanceService;
    @Autowired
    private SdpJobInstanceMapper instanceMapper;
    @Autowired
    private SdpOperationLogMapper logMapper;
    @Autowired
    private SdpVersionMapper versionMapper;
    @Autowired
    private SdpJarMapper jarMapper;
    @Autowired
    private SdpJobMapper jobMapper;
    @Autowired
    private SdpRuntimeLogService sdpRuntimeLogService;
    @Autowired
    private SdpProjectMapper sdpProjectMapper;
    @Autowired
    private SdpEngineMapper engineMapper;
    @Autowired
    private FlinkConfigProperties flinkConfigProperties;
    @Autowired
    private SdpRuntimeLogService runtimeLogService;
    @Autowired
    EngineService engineService;
    @Autowired
    FileVersionService fileVersionService;


    @Autowired
    PrometheusConfigProperties prometheusConfigProperties;

    @Autowired
    private JobStatusNotifyService jobStatusNotifyService;
    @Autowired
    private Executor handleStartOrStopExecutor;
    @Autowired
    FileService fileService;
    @Autowired
    SdpSavepointService sdpSavepointService;
    @Autowired
    SdpUatJobRunningConfigService sdpUatJobRunningConfigService;
    @Autowired
    private MetaTableConfigService metaTableConfigService;
    @Autowired
    CheckConfigProperties checkConfigProperties;
    @Autowired
    UatJobAutoOfflineJobConfig uatJobAutoOfflineJobConfig;
    @Autowired
    UserService userService;
    @Autowired
    KafkaDataSource kafkaDataSource;
    @Autowired
    DataSourceService dataSourceService;
    @Autowired
    private SdpJobAlertRuleService jobAlertRuleService;
    @Autowired
    private DefaultAlertRuleConfigProperties defaultAlertRuleConfigProperties;
    @Autowired
    @Qualifier("restfulTemplate")
    private RestTemplate restTemplate;
    @Autowired
    private ClusterInfoConfig clusterInfoConfig;


    @Autowired
    private Executor monitorExecutor;
    @Autowired
    RedisTemplateService redisTemplateService;
    @Autowired
    private RedisTemplate<String, String> redisTmplate;
    @Value("${kafka.consumer.sasl.groupId}")
    private String consumerSaslKafkaMetaGroupId;
    @Autowired
    FolderService folderService;
    @Autowired
    SdpConfig sdpConfig;
    @Autowired
    KubernetesClusterConfig kubernetesClusterConfig;
    @Autowired
    KubernetesClientConfig kubernetesClientConfig;
    @Autowired
    JobImageService dockerImageService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    JarService jarService;

    /**
     * 保存点路径
     */
    @Value("${flink.common.savepointDir}")
    private String savepointDir;

    private static final String[] applicationType = {"NEW", "NEW_SAVING", "SUBMITTED", "ACCEPTED", "RUNNING"};

    private static final String SQL_CONTENT = "%s\r\n%s";

    private static final String SQL_PARAM = "^(sql\\.param\\.[\\w]+)$";

    private static final String JOB_NAME_FORMAT = "%s_%s";


    public List<String> searchJob(SdpJobBO jobBO) {
        return this.getMapper().searchJob(jobBO);
    }

    public Pagination<SdpJob> queryJob(SdpJobBO jobBO) {
        jobBO.getVo().setJobName(StrUtils.changeWildcard(jobBO.getVo().getJobName()));
        Long folderId = Optional.ofNullable(jobBO).map(m -> m.getVo()).map(m -> m.getFolderId()).orElse(null);
        if (Objects.nonNull(folderId)) {
            Set<Long> folderIds = new HashSet<>();
            folderService.queryFolderIdAndChildFolderId(folderId, folderIds);
            if (CollectionUtils.isNotEmpty(folderIds)) {
                jobBO.getVo().setFolderIds(Lists.newArrayList(folderIds));
            }
        }
        Pagination<SdpJob> pagination = Pagination.getInstance4BO(jobBO);
        this.executePagination(p -> getMapper().queryJob(p), pagination);
        //获取prometheus的跳转网址
        List<SdpJob> rows = pagination.getRows();
        rows = rows.stream().map(x -> combineGrafanaUrl(x)).collect(Collectors.toList());
        pagination.setRows(rows);
        return pagination;
    }

    private SdpJob combineGrafanaUrl(SdpJob x) {
        if (Objects.nonNull(x)) {
            String env = sdpConfig.getEnvFromEnvHolder(log);
            if (JobStatus.RUNNING.name().equals(x.getJobStatus())) {
                String jobName = x.getJobName();
                Long projectId = x.getProjectId();
                SdpProject sdpProject = sdpProjectMapper.selectById(projectId);
                String projectCode = sdpProject.getProjectCode();
                jobName = String.format(JOB_NAME_FORMAT, projectCode, jobName);

                PrometheusConfigProperties.PrometheusConfig prometheusConfig = prometheusConfigProperties.getEnvMap().get(env);
                String prometheusUrl = String.format(prometheusConfig.getUrlPrefix(), jobName);
                x.setGrafanaUrl(prometheusUrl);
            }
            //将生产环境的flink_url替换为代理地址
            if (env.equals("prod")) {
                JobConfig jobConfig = JSONObject.parseObject(x.getConfigContent(), JobConfig.class);
                SdpEngine engine = engineService.get(jobConfig.getEngineId());
                ClusterInfo clusterInfo = engineService.getClusterInfo(EnvironmentEnum.PROD.getCode(), engine);
                String flinkProxyUrl = clusterInfo.getFlinkProxyUrl();
                String flinkUrl = x.getFlinkUrl();
                if (StrUtil.isNotEmpty(flinkUrl) && StrUtil.isNotEmpty(flinkProxyUrl)) {
                    String[] proxySplit = flinkUrl.split("proxy");
                    if (proxySplit.length >= 2) {
                        String jobApplicationId = proxySplit[1];
                        x.setFlinkUrl(flinkProxyUrl + jobApplicationId);
                    }
                }
            }
            //返回全路径（前端有需要截取最后一个文件夹名称作为目录列）
            if (Objects.nonNull(x.getFileId())) {
                String fullPath = fileService.queryFullPath(x.getFileId());
                if (StrUtil.isNotBlank(fullPath)) {
                    x.setFullPath(fullPath);
                }
            }
            x.setSlots(getSlotNum(x.getConfigContent()));
        }
        return x;
    }

    public String getSlotNum(String configContent) {
        String slots = "";
        //1.配置文件中的默认值
        String defaultVal = flinkConfigProperties.getDefaultMap().getOrDefault(FlinkConfigKeyConstant.TASKMANAGER_NUMBEROFTASKSLOTS, "1");

        if (StrUtil.isNotBlank(configContent)) {
            try {
                //2.从数据库中的配置信息获取slot数
                JSONObject jsonObject = new JSONObject();
                JobConfig jobConfig = JSONObject.parseObject(configContent, JobConfig.class);
                //是否加密
                if (isBase64(jobConfig.getFlinkYaml())) {
                    YamlUtils.parseYaml(jsonObject, jobConfig, BusinessFlag.SDP.toString(), null);
                    slots = StrUtil.isNotBlank(jsonObject.getString(FlinkConfigKeyConstant.TASKMANAGER_NUMBEROFTASKSLOTS)) ? jsonObject.getString(FlinkConfigKeyConstant.TASKMANAGER_NUMBEROFTASKSLOTS) : defaultVal;
                } else {
                    if (StrUtil.isNotBlank(jobConfig.getFlinkYaml())) {
                        Map<String, String> yamlMap = null;
                        try {
                            scala.collection.immutable.Map<String, String> yamlText = PropertiesUtils.fromYamlText(jobConfig.getFlinkYaml());
                            yamlMap = JavaConversions.mapAsJavaMap(yamlText);
                        } catch (Exception e) {
                            throw new ApplicationException(ResponseCode.YAML_CONFIG_ERROR);
                        }
                        slots = StrUtil.isNotBlank(yamlMap.get(FlinkConfigKeyConstant.TASKMANAGER_NUMBEROFTASKSLOTS)) ? yamlMap.get(FlinkConfigKeyConstant.TASKMANAGER_NUMBEROFTASKSLOTS) : defaultVal;
                    }
                }
            } catch (Exception e) {
                log.error("获取slots出现异常", e);
            }
        }
        return StrUtil.isNotBlank(slots) ? slots : defaultVal;
    }

    private boolean isBase64(String str) {
        if (Objects.isNull(str)) {
            return false;
        }
        String base64Pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
        return Pattern.matches(base64Pattern, str);
    }

    @Transactional(rollbackFor = Exception.class)
    public List<Long> deleteJob(List<SdpJob> jobBO) {
        List<Long> ids = jobBO.stream().map(x -> x.getId()).collect(Collectors.toList());
        List<SdpJob> list = this.getMapper().queryJob4Delete(ids);
        if (CollectionUtils.isNotEmpty(list)) {
            throw new ApplicationException(ResponseCode.JOB_IS_RUNNING);
        }
        ids.forEach(x -> {
            runtimeLogService.deleteRuntimeLogs(x);
        });
        instanceMapper.disables(ids);
        this.getMapper().disables(ids);
        return ids;
    }

    @Transactional(rollbackFor = Exception.class)
    public void validateStart(SdpJobBO job, String type) {
        Long jobId = job.getVo().getId();
        redisTemplateService.delete(RedisTemplateService.SDP_JOB_INSTANCE_LIST_CACHE, jobId);
        if (type.equals(JobAction.START.toString())) {
            //当时启动的时候需要进行删除前面的logs
            sdpRuntimeLogService.deleteRuntimeLogs(jobId);
        }
        //往运行日志进行写入检验状态
        sdpRuntimeLogService.recordLog(jobId, type, JobRunStep.VALIDATE, null, null);
        SdpJob sdpJob;
        try {
            sdpJob = get(jobId);
            //通过作业ID查询实例状态【是否在运行中】
            SdpJobInstance instance = instanceMapper.queryByJobId(jobId);
            String currentStatus = instance.getRawStatus();
            String jobStatus = instance.getJobStatus();
            RawStatus rawStatus = RawStatus.fromStatus(currentStatus);
            if (type.equals(JobAction.START.toString())) {
                if (!rawStatus.isInitialize() && !rawStatus.isFinished() && !rawStatus.isFailed() && !rawStatus.isTerminated()) {
                    throw new ApplicationException(ResponseCode.JOB_IS_RUNNING);
                }
            } else {
                if (!job.isAutoPull()) {
                    if (BusinessFlag.SDP.name().equals(sdpJob.getBusinessFlag()) && !jobStatus.equals(JobStatus.PAUSED.toString()) && !jobStatus.equals(JobStatus.RFAILED.toString())) {
                        throw new ApplicationException(ResponseCode.JOB_STATUS_EXCEPTION);
                    }
                }
            }
            //判断是否有新的版本【如果有,自动选择使用最新版本启动】
            String latestVersion = sdpJob.getLatestVersion();
            SdpJobInstance instance1 = new SdpJobInstance();
            instance1.setJobId(jobId);
            instance1.setJobVersion(latestVersion);
            SdpJobInstance instance2 = instanceMapper.queryLatestVersion(instance1);
            if (type.equals(JobAction.START.toString())) {
                if (instance2 != null && instance2.getId() > instance.getId()) {
                    instance = instance2;
                    instance.setEnabledFlag(1L);
                    instance.setIsLatest(true);
                }
            } else {
                //用户选择是否使用最新版本恢复
                if (job.useLatest) { //使用最新版本恢复
                    if (instance2 != null && instance2.getId() > instance.getId()) {
                        String flinkJobId = instance.getFlinkJobId();
                        instance = instance2;
                        instance.setIsLatest(true);
                        instance.setEnabledFlag(1L);
                        //启动时，要根据flinkJobId获取前一次实例的检查点
                        instance.setFlinkJobId(flinkJobId);
                    }
                }
            }
            //更新当前作业状态
            instance.setExpectStatus(ExpectStatus.RUNNING.toString());
            //修改之前实例的latest状态，产生一个新的实例
            Integer count = instanceMapper.changeLatest(instance);
            if (count > 0) {
                //确保旧的实例停掉之后再插入新的实例
                instance.setId(null);
                instance.setApplicationId(null);
                instance.setConfiguration(null);
                instance.setInstanceInfo(null);
                instance.setStartTime(null);
                instance.setCreationDate(new Timestamp(System.currentTimeMillis()));
                instance.setUpdationDate(new Timestamp(System.currentTimeMillis()));
                instanceMapper.insert(instance);
            }
        } catch (ApplicationException e) {
            sdpRuntimeLogService.recordLog(jobId, type, JobRunStep.VALIDATE, LogStatus.FAILED, ExceptionUtils.stringifyException(e));
            throw e;
        }
        //往运行日志进行写入检验状态
        sdpRuntimeLogService.recordLog(jobId, type, JobRunStep.VALIDATE, LogStatus.SUCCESS, null);

    }

    /**
     * 构建推送镜像并记录运行日志
     *
     * @param jobId
     * @param type
     * @param useLatest
     */
    private void buildImageSaveRuntimeLog(Long jobId, String type, boolean useLatest) {

        try {

            if (!dockerImageService.needBuildPushImage(jobId, type, useLatest)) {
                return;
            }

            // 记录构建镜像运行日志
            sdpRuntimeLogService.recordLog(jobId, type, JobRunStep.BUILD_PUSH_IMAGE, null, null);

            SdpJob sdpJob = get(jobId);
            String flinkJobName = projectService.get(sdpJob.getProjectId()).getProjectCode() + "_" + sdpJob.getJobName();
            JobConfig jobConfig = JSONObject.parseObject(sdpJob.getConfigContent(), JobConfig.class);
            if (StrUtil.isEmpty(jobConfig.getFlinkVersion())) {
                jobConfig.setFlinkVersion(FlinkVersion.VERSION_114.getVersion());
            }
            SdpEngine engine = engineService.get(jobConfig.getEngineId());
            SdpFile file = fileService.get(sdpJob.getFileId());

            Long jarId;
            if (FileType.DATA_STREAM.getType().equals(file.getFileType())) {
                DataStreamConfig dsConfig = JSON.parseObject(sdpJob.getDataStreamConfig(), DataStreamConfig.class);
                jarId = dsConfig.getJarId();
            } else {
                jarId = jobConfig.getJarId();
            }
            SdpJar sdpJar = jarService.get(jarId);

            dockerImageService.buildPushImage(flinkJobName, jobConfig.getFlinkVersion(), engine, sdpJar, file.getContent());
            sdpRuntimeLogService.recordLog(sdpJob.getId(), type, JobRunStep.BUILD_PUSH_IMAGE, LogStatus.SUCCESS, null);
        } catch (Exception e) {
            // 记录运行日志
            sdpRuntimeLogService.recordLog(jobId, type, JobRunStep.BUILD_PUSH_IMAGE, LogStatus.FAILED, ExceptionUtils.stringifyException(e));
            // 将实例状态修改为启动失败
            SdpJobInstance oldInstance = instanceMapper.queryByJobId(jobId);
            SdpJobInstance newJobInstance = new SdpJobInstance();
            newJobInstance.setId(oldInstance.getId());
            if (type.equals(JobAction.RECOVER.toString())) {
                newJobInstance.setJobStatus(JobStatus.RFAILED.toString());
                newJobInstance.setExpectStatus(JobStatus.RFAILED.toString());
            } else {
                newJobInstance.setJobStatus(JobStatus.SFAILED.toString());
                newJobInstance.setExpectStatus(JobStatus.SFAILED.toString());
            }
            instanceService.updateSelective(newJobInstance);
            throw new RuntimeException(e);
        }

    }


    @Transactional(rollbackFor = Exception.class)
    public void startJob(SdpJobBO jobBO, String type) {
        Context context = ContextUtils.get();
        Long jobId = jobBO.getVo().getId();
        JobRunStep js;
        if (type.equals(JobRunStep.RECOVER.name())) {
            js = JobRunStep.RECOVER;
        } else {
            js = JobRunStep.COMMIT;
        }
        sdpRuntimeLogService.recordLog(jobId, type, js, null, null);
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                //等待100ms再运行, 以免之前的线程还没完成, 锁还没释放.
                Thread.sleep(100L);
            } catch (InterruptedException ignore) {
            }
            try {
                // TODO 该方法会依赖 linux 环境执行命令，windows本地环境执行会报错，如果要调试可以将该方法注释
                buildImageSaveRuntimeLog(jobId, type, jobBO.useLatest);
                JobOperationUtils.runInJobLock(jobId, JobOperationUtils.OperationType.HANDLE_START, () -> {
                    handleStart(type, js, jobBO, context);
                    return null;
                });
            } catch (Exception e) {
                String errorMessage = String.format("handleStart方法执行出现异常, jobId: %d, message: %s", jobId, e.getMessage());
                DataMonitorUtils.monitorError(errorMessage);
                log.error(errorMessage, e);
            }
        }, handleStartOrStopExecutor);

        if (uatJobAutoOfflineJobConfig.isUatOrDev()) {
            //作业自动下线配置
            SdpUatJobRunningConfig config = new SdpUatJobRunningConfig();
            config.setJobId(jobBO.getVo().getId());
            config.setDays(jobBO.getUatJobRunningValidDays());
            sdpUatJobRunningConfigService.upsert(config);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleStart(String type, JobRunStep js, SdpJobBO jobBO, Context context) {
        Long jobId = jobBO.getVo().getId();
        SdpJob sdpJob = this.getMapper().selectById(jobId);
        sdpJob.setUseLatest(jobBO.getUseLatest());
        instanceService.renewJobVersion(sdpJob);
        SdpJobInstance instance = instanceMapper.queryByJobId(jobId);
        if (context != null) {
            instance.setUpdatedBy(context.getUserId());
            instance.setCreatedBy(context.getUserId());
        }
        SdpOperationLog sdpLog = new SdpOperationLog();
        try {
            sdpLog.setStatus(LogStatus.SUCCESS.toString());
            sdpLog.setToExpectStatus(ExpectStatus.RUNNING.toString());
            if (context != null) {
                sdpLog.setCreatedBy(context.getUserId());
            }
            sdpLog.setCreationDate(new Timestamp(System.currentTimeMillis()));
            if (type.equals(JobAction.START.toString())) {
                sdpLog.setFromExpectStatus(instance.getJobStatus());
                sdpLog.setAction(JobAction.START.toString());
            } else {
                sdpLog.setFromExpectStatus(ExpectStatus.PAUSED.toString());
                sdpLog.setAction(JobAction.RECOVER.toString());
            }
            SdpProject sdpProject = sdpProjectMapper.selectById(sdpJob.getProjectId());
            String projectCode = sdpProject.getProjectCode();
            // k8s 暂时不考虑校验是否存在
            JobConfig jobConfig = JSONObject.parseObject(sdpJob.getConfigContent(), JobConfig.class);
            SdpEngine engine = engineService.get(jobConfig.getEngineId());
            String jobName = String.format(JOB_NAME_FORMAT, projectCode, sdpJob.getJobName());
//            if (EngineTypeEnum.YARN.getType().equals(engine.getEngineType())) {
//                jobName = validateJobName(sdpJob, sdpProject);
//            }

            Application params = assembleParams(sdpJob, type, jobName, projectCode, sdpLog, engine);
            //当assembleParams出异常，插入日志表，下面的逻辑又出现异常，再次插入日志表，会出现主键重复问题
            sdpLog.setId(null);
            if (type.equals(JobAction.START.toString()) && StrUtil.isNotBlank(jobBO.getSavepointPath())) {
                params.setSavePoint(jobBO.getSavepointPath());
            }

            ResponseData responseData;
            LinkedHashMap response = null;
            try {
                String flinkClientUrl = getFlinkClientUrl(jobConfig);
                responseData = restTemplate.postForObject(flinkClientUrl + "/flink/startJob", params, ResponseData.class);
                log.info("启动任务返回结果===" + JSON.toJSONString(responseData));
                if (responseData != null && responseData.getCode() == 0) {
                    response = (LinkedHashMap) responseData.getData();
                } else {
                    if (EngineTypeEnum.KUBERNETES.getType().equals(engine.getEngineType())) {
                        sdpRuntimeLogService.insertFailLog(jobId, instance.getApplicationId(), type);
                    }
                    throw new ApplicationException(ResponseCode.JOB_START_ERROR.getCode(), responseData.getMsg());
                }
                log.info(jobId + "===作业{}完成==={}", type, response);
                //获取实际的yarnUrl和flinkUrl，需要判断返回applicationId是否为空，为空是提交yarn/k8s执行失败
                if (Objects.isNull(response.get("applicationId"))) {
                    sdpRuntimeLogService.recordLog(jobId, type, js, LogStatus.FAILED, EngineTypeEnum.KUBERNETES.getType().equals(engine.getEngineType()) ? ResponseCode.COMMIT_K8S_FAILED.getMessage() : ResponseCode.COMMIT_YARN_FAILED.getMessage());
                } else {
                    log.info("===作业JobId:{} 引擎类型 : {}===", jobId, engine.getEngineType());
                    if (EngineTypeEnum.KUBERNETES.getType().equals(engine.getEngineType())) {
                        log.info(jobId + "===进入KUBERNETES添加日志jobId:{}===", jobId);
                        // 修改log4j日志设置
                        updateConfigMap(engine, (String) response.get("applicationId"), jobId, jobConfig.getFlinkVersion());
                        // 添加运行结果日志
                        sdpRuntimeLogService.recordStartLogK8s(jobId, type, js, LogStatus.SUCCESS, String.valueOf(response.get("applicationId")), null);
                    } else {
                        log.info(jobId + "===进入Yarn添加日志jobId:{}===", jobId);
                        sdpRuntimeLogService.recordStartLog(jobId, type, js, LogStatus.SUCCESS, String.valueOf(response.get("applicationId")), null);
                    }
                }
            } catch (Exception e) {
                String exception = ExceptionUtils.stringifyException(e);
                log.error(jobId + "===作业{}失败1==={}", type, exception);
                sdpRuntimeLogService.recordLog(jobId, type, js, LogStatus.FAILED, exception);
                if (type.equals(JobAction.RECOVER.toString())) {
                    instance.setJobStatus(JobStatus.RFAILED.toString());
                    instance.setExpectStatus(JobStatus.RFAILED.toString());
                } else {
                    instance.setJobStatus(JobStatus.SFAILED.toString());
                    instance.setExpectStatus(JobStatus.SFAILED.toString());
                }
                sdpLog.setStatus(LogStatus.FAILED.toString());
                sdpLog.setMessage(exception);
            }
            //作业启动后的后续处理
            instanceService.afterStartJob(instance, sdpJob, response);
            sdpLog.setInstanceId(instance.getId());
            logMapper.insert(sdpLog);
        } catch (Exception e) {
            String exception = ExceptionUtils.stringifyException(e);
            log.error(jobId + "===作业{}失败2==={}", type, exception);
            if (type.equals(JobAction.RECOVER.toString())) {
                instance.setJobStatus(JobStatus.RFAILED.toString());
                instance.setExpectStatus(JobStatus.RFAILED.toString());
            } else {
                instance.setJobStatus(JobStatus.SFAILED.toString());
                instance.setExpectStatus(JobStatus.SFAILED.toString());
            }
            instanceService.afterStartJob(instance, sdpJob, null);
            sdpLog.setStatus(LogStatus.FAILED.toString());
            sdpLog.setMessage(exception);
            logMapper.insert(sdpLog);
        }
    }

    private String getFlinkClientUrl(JobConfig jobConfig) {
        String env = sdpConfig.getEnvFromEnvHolder(log);
        SdpEngine engine = engineService.get(jobConfig.getEngineId());

        ClusterInfo clusterInfo = engineService.getClusterInfo(env, engine);

        if (FlinkVersion.VERSION_115.getVersion().equals(jobConfig.getFlinkVersion())) {
            return clusterInfo.getFlinkClient115Url();
        } else if (FlinkVersion.VERSION_114.getVersion().equals(jobConfig.getFlinkVersion()) || StrUtil.isEmpty(jobConfig.getFlinkVersion())) {
            return clusterInfo.getFlinkClient114Url();
        } else {
            return null;
        }
    }


    public String validateJobName(SdpJob sdpJob, SdpProject sdpProject) {
        List<String> appTypes = Arrays.asList(applicationType);
        String name = sdpJob.getJobName();
        String projectCode = sdpProject.getProjectCode();
        String jobName = String.format(JOB_NAME_FORMAT, projectCode, name);
        String format = "%s/ws/v1/cluster/apps";
        String hadoopConfDir = getHadoopConfDir(sdpJob.getId());
        String postUrl = String.format(format, HadoopUtils.getRMWebAppURL(true, hadoopConfDir));
        AppInfoList appInfoList;
        try {
            appInfoList = com.chitu.bigdata.sdp.utils.HadoopUtils.httpGetDoResult(postUrl, AppInfoList.class);
        } catch (Exception e) {
            log.error("获取yarn上面状态列表异常:{}", e);
            throw new ApplicationException(ResponseCode.VALIDATE_JOB_NAME_FAIL, e.getMessage());
        }
        if (Objects.nonNull(appInfoList) && Objects.nonNull(appInfoList.getApps())) {
            List<AppInfoList.AppInfos.Apps> infoList = appInfoList.getApps().getApp();
            infoList.forEach(y -> {
                if (appTypes.contains(y.getState())) {
                    if (jobName.equals(y.getName())) {
                        throw new ApplicationException(ResponseCode.YARN_NAME_EXIST);
                    }
                }
            });
        }
        return jobName;
    }

    private Application assembleParams(SdpJob sdpJob, String type, String jobName, String projectCode, SdpOperationLog sdpLog, SdpEngine engine) {
        String dsConfig = sdpJob.getDataStreamConfig();
        Application params = new Application();
        params.setId(sdpJob.getId());
        params.setJobName(jobName);
        params.setJobType(DevelopmentMode.FLINKSQL.getValue());
        String exception = "";
        if (StrUtil.isNotEmpty(dsConfig)) {
            DataStreamConfig dsConfig1 = JSONObject.parseObject(dsConfig, DataStreamConfig.class);
            SdpJar sdpJar = jarMapper.selectById(dsConfig1.getJarId());
            params.setJobType(DevelopmentMode.CUSTOMCODE.getValue());
            params.setMainClass(dsConfig1.getMainClass());
            String hdfsPath = HdfsUtils.getDefaultFS() + ConfigConst.SDP_HDFS_WORKSPACE_DEFAULT() + "/workspace" + sdpJar.getUrl();
            FileSystem fs = null;
            try {
                String hadoopConfDir = getHadoopConfDir(sdpJob.getId());
                fs = com.chitu.bigdata.sdp.utils.HdfsUtils.getFileSystem(hadoopConfDir);
                FileStatus[] files = fs.listStatus(new Path(hdfsPath));
                if (files.length != 0) {
                    String fullJar = files[0].getPath().toString();
                    params.setFlinkUserJar(fullJar);
                }
            } catch (Exception e) {
                log.error("===启动作业组装入参,获取DS JAR包异常===", e);
                exception = ExceptionUtils.stringifyException(e);
            } finally {
                try {
                    if (fs != null) {
                        fs.close();
                    }
                } catch (Exception e1) {
                    log.error("===获取DS JAR包,关闭流异常===", e1);
                    exception = ExceptionUtils.stringifyException(e1);
                }
            }
        }
        params.setAppType(ApplicationType.APACHE_FLINK.getType());
        params.setResolveOrder(ResolveOrder.PARENT_FIRST.getValue());

        params.setFlameGraph(false);
        params.setFlinkSql(DeflaterUtils.zipString(sdpJob.getJobContent()));
        if (type.equals(JobAction.RECOVER.toString())) {
            List<Checkpoint> checkpoints = getCheckpoints(sdpJob.getId(), 1);
            if (!CollectionUtils.isEmpty(checkpoints) && StrUtil.isNotBlank(checkpoints.get(0).getFilePath())) {
                params.setSavePoint(checkpoints.get(0).getFilePath());
            }
        }
        JobConfig jobConfig = JSONObject.parseObject(sdpJob.getConfigContent(), JobConfig.class);
        if (type.equals(JobAction.START.toString()) || type.equals(JobAction.RECOVER.toString())) {
            if (null != jobConfig.getJarId()) {
                SdpJarBO jarBO = new SdpJarBO();
                jarBO.setProjectId(sdpJob.getProjectId());
                jarBO.setName(jobConfig.getJarName());
                jarBO.setVersion(jobConfig.getJarVersion());
                List<SdpJar> jars = jarMapper.searchJar(jarBO);
                if (CollectionUtils.isNotEmpty(jars)) {
                    params.setUdfPath(jars.get(0).getUrl());
                }
            }
        }
        JSONObject options = new JSONObject();
        SourceConfig sourceConfig = JSON.parseObject(sdpJob.getSourceContent(), SourceConfig.class);
        options.put(FlinkConfigKeyConstant.JOBMANAGER_MEMORY, sourceConfig.getJobManagerMem() + "G");
        options.put(FlinkConfigKeyConstant.TASKMANAGER_MEMORY, sourceConfig.getTaskManagerMem() + "G");
        options.put(FlinkConfigKeyConstant.PARALLELISM, sourceConfig.getParallelism());

        //解析flink yaml文件
        try {
            String flag = sdpJob.getBusinessFlag();
            YamlUtils.parseYaml(options, jobConfig, flag, null);
            //将sql占位符参数剔除
            Set<String> keySet = options.keySet();
            Iterator<String> ite = keySet.iterator();
            while (ite.hasNext()) {
                String k = ite.next();
                if (Pattern.matches(SQL_PARAM, k)) {
                    ite.remove();
                }
            }

            if (EngineTypeEnum.KUBERNETES.getType().equals(engine.getEngineType())) {
                // 转小写
                params.setJobName(params.getJobName().toLowerCase());
                params.setExecutionMode(ExecutionMode.KUBERNETES_NATIVE_APPLICATION.getMode());
                String env = sdpConfig.getEnvFromEnvHolder(log);
                params.setNamespace(EnvironmentEnum.UAT.getCode().equals(env) ? engine.getUatNamespace() : engine.getNamespace());
                params.setHadoopConfigMapName(kubernetesClusterConfig.getHadoopConfigMapName());
                params.setJobManagerServiceAccount(kubernetesClusterConfig.getJobManagerServiceAccount());
                if (StrUtil.isEmpty(jobConfig.getFlinkVersion())) {
                    jobConfig.setFlinkVersion(FlinkVersion.VERSION_114.getVersion());
                }
                Long jarId;
                if (StrUtil.isNotEmpty(sdpJob.getDataStreamConfig())) {
                    DataStreamConfig dsCfg = JSONObject.parseObject(dsConfig, DataStreamConfig.class);
                    jarId = dsCfg.getJarId();
                } else {
                    jarId = jobConfig.getJarId();
                }
                setContainerImage(params, jobConfig.getFlinkVersion(), params.getJobName(), jarId, sdpJob.getJobContent(), engine);
                setflinkUserJar(params, jobConfig.getFlinkVersion());
            } else {
                //进行yarn队列设定
                filterQueue(jobConfig, options);
                params.setExecutionMode(ExecutionMode.APPLICATION.getMode());
            }

            //设置默认值
            flinkConfigProperties.getDefaultMap().forEach((k, v) -> {
                options.put(k, v);
            });

            options.put(FlinkConfigKeyConstant.CHECKPOINT_DIR, HdfsUtils.getDefaultFS() + flinkConfigProperties.getDefaultMap().get(FlinkConfigKeyConstant.CHECKPOINT_DIR));

            //log4j日志配置输出kafka需要获取该配置参数 -> env.java.opts: -Dflink_job_name=sink_doris -DyarnContainerId=$CONTAINER_ID
            options.put(FlinkConfigKeyConstant.ENV_JAVA_OPTS, String.format("-Dproject_code=%s -Dflink_job_name=%s -Dyarn_container_id=$CONTAINER_ID", projectCode, jobName));
            String customHadoopConf = getCataLogHadoopConf(sdpJob.getJobContent());
            if (StrUtil.isNotEmpty(customHadoopConf)) {
                options.put(FlinkConfigKeyConstant.ENV_JAVA_OPTS_TASKMANAGER, String.format("-Dhive.catalog.hadoopconf=%s", customHadoopConf));
            }
            if (Objects.nonNull(sdpJob) && Objects.nonNull(sdpJob.getFileId())) {
                String hudiHadoopConf = metaTableConfigService.getMapper().queryHudiHadoopConfDirByFileId(sdpJob.getFileId());
                if (StrUtil.isNotBlank(hudiHadoopConf)) {
                    String hudiHadoopConfEnv = String.format("-Dhudi.catalog.hadoopconf=%s", hudiHadoopConf);
                    Object o = options.get(FlinkConfigKeyConstant.ENV_JAVA_OPTS);
                    if (Objects.nonNull(o) && StrUtil.isNotBlank(o.toString())) {
                        hudiHadoopConfEnv = o.toString() + " " + hudiHadoopConfEnv;
                    }
                    options.put(FlinkConfigKeyConstant.ENV_JAVA_OPTS, hudiHadoopConfEnv);
                }
            }

            params.setOptions(JSON.toJSONString(options));
//            log.info("===启动作业入参options==={}",JSON.toJSONString(options));
        } catch (Exception e) {
            log.error("===解析yaml文件异常===" + e);
            exception = ExceptionUtils.stringifyException(e);
            throw new ApplicationException(ResponseCode.YAML_CONFIG_ERROR);
        }
        if (StrUtil.isNotEmpty(exception)) {
            sdpLog.setStatus(LogStatus.FAILED.toString());
            sdpLog.setMessage(exception);
            logMapper.insert(sdpLog);
        }
        return params;
    }


    public void setContainerImage(Application params, String flinkVersion, String flinkJobName, Long jarId, String sqlContent, SdpEngine engine) {
        String esVersion = "";
        if (FlinkVersion.VERSION_115.getVersion().equals(flinkVersion)) {
            if (StrUtil.isNotBlank(sqlContent)) {
                if (sqlContent.contains(ESVersion.VERSION_6.getVersion())) {
                    esVersion = ".es6";
                } else if (sqlContent.contains(ESVersion.VERSION_7.getVersion())) {
                    esVersion = ".es7";
                }
            }
        }
        String env = sdpConfig.getEnvFromEnvHolder(log);
//        ClusterInfo clusterInfo = kubernetesClusterConfig.getEnvMap().get(env).getClusters().get(0);
        ClusterInfo clusterInfo = engineService.getClusterInfo(env, engine);
        if (jarId == null) {
            params.setContainerImage(clusterInfo.getDockerHubAddress() + kubernetesClusterConfig.getSqlBaseImage() + ":" + flinkVersion + esVersion);
        } else {
            params.setContainerImage(clusterInfo.getDockerHubAddress() + kubernetesClusterConfig.getImagePrefix() + flinkJobName + ":" + flinkVersion);
        }
    }


    public void setflinkUserJar(Application params, String flinkVersion) {
        // ds
        if (params.getFlinkUserJar() != null) {
            String[] hdfsPath = params.getFlinkUserJar().split("/");
            params.setFlinkUserJar(kubernetesClusterConfig.getFlinkUserJarPrefix() + hdfsPath[hdfsPath.length - 1]);
            return;
        }

        // sql
        if (FlinkVersion.VERSION_114.getVersion().equals(flinkVersion)) {
            params.setFlinkUserJar(kubernetesClusterConfig.getFlinkUserJarPrefix() + kubernetesClusterConfig.getSql114Jar());
        } else if (FlinkVersion.VERSION_115.getVersion().equals(flinkVersion)) {
            params.setFlinkUserJar(kubernetesClusterConfig.getFlinkUserJarPrefix() + kubernetesClusterConfig.getSql115Jar());
        }

    }


    /**
     * 如果sql存在多个create catalog，只返回其中一个配置的hadoopconf
     *
     * @param sql
     * @return
     */
    private String getCataLogHadoopConf(String sql) {
        AtomicReference<String> hadoopConf = new AtomicReference<>("");
        try {
            List<SqlNode> sqlNodes = SqlParserUtil.getSqlNodes(sql);
            List<SqlNode> sqlCreateCatalogNode = sqlNodes.stream().filter(sqlNode -> sqlNode instanceof SqlCreateCatalog).collect(Collectors.toList());
            sqlCreateCatalogNode.forEach(item -> {
                SqlCreateCatalog sqlCreateCatalog = (SqlCreateCatalog) item;
                if (sqlCreateCatalog.getPropertyList() != null) {
                    sqlCreateCatalog.getPropertyList().getList().forEach(optionNode -> {
                        SqlTableOption sqlTableOption = (SqlTableOption) optionNode;
                        if ("hadoop-conf-dir".equals(sqlTableOption.getKeyString())) {
                            hadoopConf.set(sqlTableOption.getValueString());
                        }
                    });
                }
            });
        } catch (SqlParseException e) {
            log.warn("sql解析异常", e);
        }
        return hadoopConf.get();
    }


    public void filterQueue(JobConfig jobConfig, JSONObject options) {
        SdpEngine engine = null;
        SdpEngine sdpEngine = new SdpEngine();
        if (Objects.nonNull(jobConfig.getEngineId())) {
            Long engineId = jobConfig.getEngineId();
            SdpEngine se = engineMapper.selectById(engineId);
            if (Objects.nonNull(se)) {
                engine = se;
            }
        } else {
            if (Objects.nonNull(jobConfig.getEngine())) {
                sdpEngine.setEngineName(jobConfig.getEngine());
                List<SdpEngine> sdpEngines = engineService.selectAll(sdpEngine);
                if (!CollectionUtils.isEmpty(sdpEngines)) {
                    engine = sdpEngines.get(0);
                }
            }
        }

        String env = sdpConfig.getEnvFromEnvHolder(log);
        ClusterInfoConfig.YarnConfig yarnConfig = clusterInfoConfig.getEnvMap().get(env);

        if (Objects.nonNull(engine)) {
            if (EnvironmentEnum.PROD.getCode().equalsIgnoreCase(env)) {
                //prod
                if (Objects.nonNull(engine.getEngineQueue())) {
                    options.put(FlinkConfigKeyConstant.YARN_QUEUE, engine.getEngineQueue());
                } else {
                    options.put(FlinkConfigKeyConstant.YARN_QUEUE, yarnConfig.getDefaultConfig().getQueue());
                }
            } else {
                //uat
                if (Objects.nonNull(engine.getUatEngineQueue())) {
                    options.put(FlinkConfigKeyConstant.YARN_QUEUE, engine.getUatEngineQueue());
                } else {
                    options.put(FlinkConfigKeyConstant.YARN_QUEUE, yarnConfig.getDefaultConfig().getQueue());
                }
            }
        } else {
            options.put(FlinkConfigKeyConstant.YARN_QUEUE, yarnConfig.getDefaultConfig().getQueue());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public void validateStop(SdpJobBO job, String type) {
        Long jobId = job.getVo().getId();
        redisTemplateService.delete(RedisTemplateService.SDP_JOB_INSTANCE_LIST_CACHE, jobId);
        //往运行日志进行写入检验状态
        sdpRuntimeLogService.recordLog(jobId, type, JobRunStep.VALIDATE, null, null);
        SdpJobInstance instance = instanceMapper.queryByJobId(jobId);
        try {
            String currentStatus = instance.getRawStatus();
            RawStatus rawStatus = RawStatus.fromStatus(currentStatus);
            if (!rawStatus.isStarting() && !rawStatus.isRunning() && !instance.getJobStatus().equals(JobStatus.PAUSED.toString()) && !instance.getJobStatus().equals(JobStatus.RFAILED.toString())) {
                throw new ApplicationException(ResponseCode.JOB_NOT_RUNNING);
            }
            //修改instance表状态
            if (type.equals(JobAction.STOP.toString())) {
                instance.setExpectStatus(ExpectStatus.TERMINATED.toString());
                instance.setSignStatus(ExpectStatus.TERMINATED.toString());
            } else {
                instance.setExpectStatus(ExpectStatus.PAUSED.toString());
                instance.setSignStatus(ExpectStatus.PAUSED.toString());
            }
            instance.setIsLatest(null);
            instance.setUpdationDate(new Timestamp(System.currentTimeMillis()));
            instanceService.updateSelective(instance);
        } catch (ApplicationException e) {
            sdpRuntimeLogService.recordLog(jobId, type, JobRunStep.VALIDATE, LogStatus.FAILED, ExceptionUtils.stringifyException(e));
            throw e;
        }
        //往运行日志进行写入检验状态
        sdpRuntimeLogService.recordLog(jobId, type, JobRunStep.VALIDATE, LogStatus.SUCCESS, null);
    }

    @Transactional(rollbackFor = Exception.class)
    public void stopJob(SdpJobBO jobBO, String type) {
        Context context = ContextUtils.get();
        Long jobId = jobBO.getVo().getId();
        sdpRuntimeLogService.recordLog(jobId, type, JobRunStep.getStepValue(type), null, null);
        CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
            try {
                //等待100ms再运行, 以免之前的线程还没完成, 锁还没释放.
                Thread.sleep(100L);
            } catch (InterruptedException ignore) {
            }
            try {
                JobOperationUtils.runInJobLock(jobId, JobOperationUtils.OperationType.HANDLE_STOP, () -> {
                    handleStop(type, jobId, context);
                    return null;
                });
            } catch (Exception e) {
                String errorMessage = String.format("handleStop方法执行出现异常, jobId: %d, message: %s", jobId, e.getMessage());
                DataMonitorUtils.monitorError(errorMessage);
                log.error(errorMessage, e);
            }
        }, handleStartOrStopExecutor);
    }

    @Transactional(rollbackFor = Exception.class)
    public void handleStop(String type, Long jobId, Context context) {
        SdpJobInstance instance = instanceMapper.queryByJobId(jobId);
        SdpOperationLog sdpLog = new SdpOperationLog();
        try {
            Application params = new Application();
            params.setJobId(instance.getFlinkJobId());
            params.setAppId(instance.getApplicationId());
            params.setExecutionMode(ExecutionMode.APPLICATION.getMode());
            sdpLog.setStatus(LogStatus.SUCCESS.toString());
            sdpLog.setInstanceId(instance.getId());
            sdpLog.setFromExpectStatus(ExpectStatus.RUNNING.toString());
            if (context != null) {
                sdpLog.setCreatedBy(context.getUserId());
            }
            sdpLog.setCreationDate(new Timestamp(System.currentTimeMillis()));
            //修改instance表状态
            if (type.equals(JobAction.STOP.toString())) {
                sdpLog.setToExpectStatus(ExpectStatus.TERMINATED.toString());
                sdpLog.setAction(JobAction.STOP.toString());
            } else {
                sdpLog.setToExpectStatus(ExpectStatus.PAUSED.toString());
                sdpLog.setAction(JobAction.PAUSE.toString());
            }
            SdpJob sdpJob = get(jobId);
            SdpJob sj = new SdpJob();
            BeanUtils.copyProperties(sdpJob, sj);
            sj.setJobStatus(instance.getJobStatus());
            try {
                JobConfig jobConfig = JSONObject.parseObject(sdpJob.getConfigContent(), JobConfig.class);
                SdpEngine engine = engineService.get(jobConfig.getEngineId());
                if (EngineTypeEnum.KUBERNETES.getType().equals(engine.getEngineType())) {
//                    String flinkJobName = projectService.get(sdpJob.getProjectId()).getProjectCode() + "_" + sdpJob.getJobName();
//                    params.setJobName(flinkJobName.toLowerCase());
                    params.setExecutionMode(ExecutionMode.KUBERNETES_NATIVE_APPLICATION.getMode());
                    String env = sdpConfig.getEnvFromEnvHolder(log);
                    params.setNamespace(EnvironmentEnum.UAT.getCode().equals(env) ? engine.getUatNamespace() : engine.getNamespace());
                }
                String flinkClientUrl = getFlinkClientUrl(jobConfig);
                restTemplate.postForObject(flinkClientUrl + "/flink/stopJob", params, ResponseData.class);
                log.info(jobId + "===作业{}完成===", type);
                sdpRuntimeLogService.recordStopLog(jobId, type, JobRunStep.getStepValue(type), LogStatus.SUCCESS, null);
                try {
                    if (type.equals(JobAction.STOP.toString())) {
                       /* String checkPointPrefix = flinkConfigProperties.getDefaultMap().get(FlinkConfigKeyConstant.CHECKPOINT_DIR)+ instance.getFlinkJobId();
                        log.info("作业停止，开始删除checkPointPath:{}",checkPointPrefix);
                        HdfsUtils.delete(checkPointPrefix);*/
                    } else {
                        instance.setSignStatus(JobStatus.PAUSED.name());
                    }
                } catch (Exception e1) {
                    log.error("删除检查点异常===" + e1);
                    String exception = ExceptionUtils.stringifyException(e1);
                    sdpLog.setStatus(LogStatus.FAILED.toString());
                    sdpLog.setMessage(exception);
                }
            } catch (Exception e) {
                boolean logStatus = false;
                log.error(jobId + "===作业{}失败==={}", type, e);
                sdpLog.setStatus(LogStatus.FAILED.toString());
                String exceptions = ExceptionUtils.stringifyException(e);
                if (exceptions.contains("doesn't run anymore")) {
                    //此时，该任务在yarn上非运行中
                    sdpLog.setStatus(LogStatus.SUCCESS.toString());
                    try {
                        if (type.equals(JobAction.STOP.toString())) {
                            /*String checkPointPrefix = flinkConfigProperties.getDefaultMap().get(FlinkConfigKeyConstant.CHECKPOINT_DIR) + instance.getFlinkJobId();
                            log.info("作业停止，开始删除checkPointPath:{}",checkPointPrefix);
                            HdfsUtils.delete(checkPointPrefix);*/
                        } else {
                            instance.setSignStatus(JobStatus.PAUSED.name());
                        }
                        //如果yarn上面的任务已停止，则返回停止成功
                        logStatus = true;
                    } catch (Exception e2) {
                        log.error("删除检查点异常===" + e2);
                        String exception = ExceptionUtils.stringifyException(e2);
                        sdpLog.setStatus(LogStatus.FAILED.toString());
                        sdpLog.setMessage(exception);
                    }
                }
                try {
                    if (logStatus) {
                        sdpRuntimeLogService.recordStopLog(jobId, type, JobRunStep.getStepValue(type), LogStatus.SUCCESS, null);
                    } else {
                        sdpRuntimeLogService.recordStopLog(jobId, type, JobRunStep.getStepValue(type), LogStatus.FAILED, exceptions);
                    }
                } catch (Exception e3) {
                    log.error(jobId + "===运行日志记录作业{}失败==={}", type, e3);
                    String exception = ExceptionUtils.stringifyException(e3);
                    sdpLog.setStatus(LogStatus.FAILED.toString());
                    sdpLog.setMessage(exception);
                }
                if (instance.getJobStatus().equals(JobStatus.PAUSED.toString()) || instance.getJobStatus().equals(JobStatus.RFAILED.toString())) {
                    instance.setJobStatus(JobStatus.TERMINATED.toString());
                    instance.setExpectStatus(ExpectStatus.TERMINATED.toString());
                    try {
                        jobStatusNotifyService.buildStatusParam(instance, sj);
                    } catch (Exception exception) {
                        log.error(jobId + "===作业状态发跨声{}失败==={}", type, e);
                    }
                    instance.setSignStatus(JobStatus.TERMINATED.name());
                } else if (instance.getJobStatus().equals(JobStatus.RUNNING.toString())) {
                    instance.setExpectStatus(ExpectStatus.RUNNING.toString());
                }

                sdpLog.setMessage(exceptions);
            }
            instance.setIsLatest(null);
            log.info(jobId + "===作业更新==={}", JSON.toJSONString(instance));
            instanceService.updateSelective(instance);
            logMapper.insert(sdpLog);
        } catch (Exception e4) {
            log.error(jobId + "===作业{}失败==={}", type, e4);
            String exception = ExceptionUtils.stringifyException(e4);
            sdpLog.setStatus(LogStatus.FAILED.toString());
            sdpLog.setMessage(exception);
            logMapper.insert(sdpLog);
        }
    }

    public Object getJobConf(SdpJobBO jobBO) {
        if (Objects.isNull(jobBO.getConfType())) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, jobBO.getConfType());
        }
        if (Objects.isNull(jobBO.getVo().getId())) {
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, jobBO.getId());
        }
        String confType = jobBO.getConfType();
        Long id = jobBO.getVo().getId();
        SdpJob sdpJob = this.getMapper().selectById(id);
        String flag = sdpJob.getBusinessFlag();
        SdpVersion sdpVersion = new SdpVersion();
        sdpVersion.setFileId(sdpJob.getFileId());
        if (Objects.nonNull(sdpJob.getRunningVersion())) {
            sdpVersion.setFileVersion(sdpJob.getRunningVersion());
        } else {
            sdpVersion.setFileVersion(sdpJob.getLatestVersion());
        }
        List<SdpVersion> sdpVersions = fileVersionService.selectAll(sdpVersion);
        Object result = null;
        switch (JobConfType.getJobConfType(confType)) {
            case BASIC_CONF:
                result = getConfContent(sdpVersions);
                break;
            case SQL_CONF:
                result = getSqlConf(sdpVersions);
                break;
            case SOURCE_CONF:
                result = getSourceContent(sdpVersions);
                break;
            case YML_CONF:
                result = getYmlConf(sdpVersions, flag);
                break;
            case JAR_CONF:
                result = getJarConf(sdpVersions);
                break;
            case DS_CONF:
                result = getDsConf(sdpVersions);
                break;
            default:
                result = null;
                break;
        }
        return result;
    }

    private void updateConfigMap(SdpEngine engine, String appId, Long jobId, String flinkVersion) {
        try {
            String env = sdpConfig.getEnvFromEnvHolder(log);
            String nameSpace = EnvironmentEnum.UAT.getCode().equals(env) ? engine.getUatNamespace() : engine.getNamespace();
            KubernetesClient kubernetesClient = kubernetesClientConfig.getKubernetesClient(env);
            Resource<ConfigMap> configMapResource = kubernetesClient.configMaps().inNamespace(nameSpace).withName("flink-config-" + appId);
            ConfigMap configMap = configMapResource.get();
            if (configMap != null) {
                Map<String, String> data = configMap.getData();
                if (data != null) {
                    String logKey = "log4j-console.properties";
                    String logFileName = "log4j-console-" + env + ".properties";
                    String logDir = flinkConfigProperties.getFlinkVersion(flinkVersion).get("confDir") + logFileName;
                    String newLogStr = FileUtil.readString(new File(logDir), StandardCharsets.UTF_8);
                    if (StrUtil.isNotEmpty(newLogStr)) {
                        data.put(logKey, newLogStr);
                        log.info("设置log4j日志jobId:{} logKey:{} 新文件路径:{}", jobId, logKey, logDir);
                        kubernetesClient.configMaps().resource(configMap).createOrReplace();
                        log.info("===设置log4j日志成功 jobId:{}===", jobId);
                    }
                }
            }
        } catch (Exception e) {
            log.error("===设置log4j日志失败 jobId:{}===", jobId, e);
        }
    }

    private Object getDsConf(List<SdpVersion> sdpVersions) {
        DataStreamConfig dsc = null;
        if (CollectionUtils.isNotEmpty(sdpVersions)) {
            String dataStreamConfig = sdpVersions.get(0).getDataStreamConfig();
            if (Objects.nonNull(dataStreamConfig)) {
                dsc = JSON.parseObject(dataStreamConfig, DataStreamConfig.class);
                SdpJar sdpJar = jarMapper.selectById(dsc.getJarId());
                dsc.setDescription(sdpJar.getDescription() == null ? "" : sdpJar.getDescription());
                dsc.setGit(sdpJar.getGit());
                dsc.setUrl(sdpJar.getUrl());
            }
        }
        return dsc;
    }

    private Object getJarConf(List<SdpVersion> sdpVersions) {
        SdpJar sdpJar = null;
        if (!CollectionUtils.isEmpty(sdpVersions)) {
            String configContent = sdpVersions.get(0).getConfigContent();
            JobConfig jobConfig = JSON.parseObject(configContent, JobConfig.class);
            String jarVersion = jobConfig.getJarVersion();
            String jarName = jobConfig.getJarName();
            if (Objects.nonNull(jarVersion) && Objects.nonNull(jarName)) {
                SdpJarBO jarBO = new SdpJarBO();
                jarBO.setProjectId(sdpVersions.get(0).getProjectId());
                jarBO.setName(jobConfig.getJarName());
                jarBO.setVersion(jobConfig.getJarVersion());
                List<SdpJar> jars = jarMapper.searchJar(jarBO);
                if (!CollectionUtils.isEmpty(jars)) {
                    SdpJar jar = jars.get(0);
                    sdpJar = new SdpJar();
                    sdpJar.setDescription(jar.getDescription() == null ? "" : jar.getDescription());
                    sdpJar.setName(jar.getName());
                    sdpJar.setGit(jar.getGit());
                    sdpJar.setUrl(jar.getUrl());
                    sdpJar.setVersion(jar.getVersion());
                    sdpJar.setEnabledFlag(null);
                    sdpJar.setJobs(null);
                }
            }
        }
        return sdpJar;
    }

    private Object getYmlConf(List<SdpVersion> sdpVersions, String flag) {
        JSONObject options = new JSONObject();
        if (!CollectionUtils.isEmpty(sdpVersions)) {
            String configContent = sdpVersions.get(0).getConfigContent();
            JobConfig jobConfig = JSON.parseObject(configContent, JobConfig.class);
            YamlUtils.parseYaml(options, jobConfig, flag, null);
        }
        return options;
    }

    private Object getSourceContent(List<SdpVersion> sdpVersions) {
        SourceConfig sourceConfig = new SourceConfig();
        if (!CollectionUtils.isEmpty(sdpVersions)) {
            String sourceContent = sdpVersions.get(0).getSourceContent();
            sourceConfig = JSON.parseObject(sourceContent, SourceConfig.class);
        }
        return sourceConfig;
    }

    private Object getSqlConf(List<SdpVersion> sdpVersions) {
        JSONObject object = null;
        if (!CollectionUtils.isEmpty(sdpVersions)) {
//            String fileContent = sdpVersions.get(0).getFileContent();
            String metaTableContent = sdpVersions.get(0).getMetaTableContent();
            String etlContent = sdpVersions.get(0).getEtlContent();
            String fileContent = String.format(SQL_CONTENT, metaTableContent == null ? StrUtils.SPACE : metaTableContent,
                    etlContent == null ? StrUtils.SPACE : etlContent);
            object = new JSONObject();
            object.put("cont", fileContent);
        }
        return object;
    }

    private Object getConfContent(List<SdpVersion> sdpVersions) {
        JobConfig jobConfig = null;
        if (!CollectionUtils.isEmpty(sdpVersions)) {
            String content = sdpVersions.get(0).getConfigContent();
            jobConfig = JSON.parseObject(content, JobConfig.class);
            // 增加历史数据为空兼容处理
            if (StrUtil.isEmpty(jobConfig.getFlinkVersion())) {
                jobConfig.setFlinkVersion(FlinkVersion.VERSION_114.getVersion());
            }
            if (Objects.nonNull(jobConfig.getEngineId())) {
                SdpEngine engine = engineService.get(jobConfig.getEngineId());
                if (Objects.nonNull(engine)) {
                    jobConfig.setEngineType(engine.getEngineType());
                    String env = sdpConfig.getEnvFromEnvHolder(log);
                    if (EngineTypeEnum.KUBERNETES.getType().equals(engine.getEngineType())) {
                        // namespace
                        String nameSpace = EnvironmentEnum.UAT.getCode().equals(env) ? engine.getUatNamespace() : engine.getNamespace();
                        jobConfig.setNamespace(nameSpace);
                    } else {
                        // 队列
                        String engineQueue = EnvironmentEnum.UAT.getCode().equals(env) ? engine.getUatEngineQueue() : engine.getEngineQueue();
                        jobConfig.setEngineQueue(engineQueue);
                    }
                    // 集群
                    ClusterInfo clusterInfo = engineService.getClusterInfo(env, engine);
                    jobConfig.setEngineCluster(clusterInfo.getClusterName());
                }
            }
            //处理不需要数据，置为null
            jobConfig.setJarId(null);
            jobConfig.setJarName(null);
            jobConfig.setJarVersion(null);
            jobConfig.setFlinkYaml(null);
        }
        return jobConfig;
    }

    public void updatePriority(List<SdpJob> jobs) {
        if (CollectionUtils.isNotEmpty(jobs)) {
            List<SdpJobAlertRule> rules = new ArrayList();
            List<Map<String, String>> defaultRules = defaultAlertRuleConfigProperties.getRules();
            for (SdpJob jobBO : jobs) {
                if (Objects.isNull(jobBO.getId())) {
                    throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, jobBO.getId());
                }
                Integer priority = jobBO.getPriority();
                if (Objects.isNull(priority)) {
                    throw new ApplicationException(ResponseCode.INVALID_ARGUMENT, priority);
                }
                if (jobBO.getUpdateRule()) {
                    defaultRules.forEach(z -> {
                        SdpJobAlertRule alertRule = new SdpJobAlertRule();
                        alertRule.setJobId(jobBO.getId());
                        Set<Map.Entry<String, String>> entries = z.entrySet();
                        Iterator<Map.Entry<String, String>> iterator = entries.iterator();
                        while (iterator.hasNext()) {
                            Map.Entry<String, String> next = iterator.next();
                            if (next.getKey().equals("indexName")) {
                                alertRule.setIndexName(next.getValue());
                            }
                            if (next.getKey().endsWith("P" + priority)) {
                                alertRule.setNotifiType(next.getValue().equals("") ? null : next.getValue());
                            }
                        }
                        rules.add(alertRule);
                    });
                }
            }
            batchUpdateSelective(jobs);
            //更新对应作业的告警规则配置信息
            if (CollectionUtils.isNotEmpty(rules)) {
                jobAlertRuleService.getMapper().updateByIndexName(rules);
            }
        }
    }

    public Object selectUpdatedBy(Long projectId) {
        return this.getMapper().selectUpdatedBy(projectId.toString());
    }

    @Transactional(rollbackFor = Exception.class)
    public void triggerSavepoint4Job(SdpJobBO jobBO, String type) {
        Context context = ContextUtils.get();
        Long jobId = jobBO.getVo().getId();

        //LogStatus
        SdpSavepoint sdpSavepoint = new SdpSavepoint();
        sdpSavepoint.setJobId(jobId);
        sdpSavepoint.setSavepointName(jobBO.getSavepointName());
        sdpSavepoint.setTriggerTime(new Timestamp(System.currentTimeMillis()));
        //sdpSavepoint.setFilePath();
        //sdpSavepoint.setOperateStatus();
        //sdpSavepoint.setOperateErrMsg();
        sdpSavepoint.setEnabledFlag(1L);
        sdpSavepoint.setCreatedBy(context.getUserId());
        sdpSavepoint.setUpdatedBy(context.getUserId());
        sdpSavepointService.insertSelective(sdpSavepoint);

        SdpJobInstance instance = instanceMapper.queryByJobId(jobId);
        try {
            Application params = new Application();
            params.setJobId(instance.getFlinkJobId());
            params.setAppId(instance.getApplicationId());
            params.setExecutionMode(ExecutionMode.APPLICATION.getMode());
            params.setSavePoint(HdfsUtils.getDefaultFS() + savepointDir);
            // String cluster = engineMapper.queryClusterByJobId(jobId);
            SdpJob sdpJob = this.get(jobId);
            JobConfig jobConfig = JSONObject.parseObject(sdpJob.getConfigContent(), JobConfig.class);
            SdpEngine engine = engineService.get(jobConfig.getEngineId());
            if (EngineTypeEnum.KUBERNETES.getType().equals(engine.getEngineType())) {
                params.setExecutionMode(ExecutionMode.KUBERNETES_NATIVE_APPLICATION.getMode());
                String env = sdpConfig.getEnvFromEnvHolder(log);
                params.setNamespace(EnvironmentEnum.UAT.getCode().equals(env) ? engine.getUatNamespace() : engine.getNamespace());
            }
            String flinkClientUrl = getFlinkClientUrl(jobConfig);
            ResponseData<String> responseData = restTemplate.postForObject(flinkClientUrl + "/flink/triggerSavepoint", params, ResponseData.class);
            if (responseData == null || responseData.getCode() != 0) {
                throw new ApplicationException(ResponseCode.COMMON_ERR, "触发保存点接口异常");
            }
            String filePath = responseData.getData();
            //String filePath = applicationService.triggerSavepoint(params);
            log.info(jobId + "===作业{}完成===", type);
            sdpSavepoint.setFilePath(filePath);
            if (StrUtil.isBlank(filePath)) {
                sdpSavepoint.setOperateStatus(LogStatus.FAILED.name());
                sdpSavepoint.setOperateErrMsg("添加保存点返回路径为空");
            } else {
                sdpSavepoint.setOperateStatus(LogStatus.SUCCESS.name());
            }
            sdpSavepointService.updateSelective(sdpSavepoint);
        } catch (Exception e4) {
            log.error(jobId + "===作业{}失败===", type, e4);
            String exception = ExceptionUtils.stringifyException(e4);
            sdpSavepoint.setOperateStatus(LogStatus.FAILED.name());
            sdpSavepoint.setOperateErrMsg(exception);
            sdpSavepointService.updateSelective(sdpSavepoint);
        }
    }

    /**
     * 获取检查点数据
     *
     * @param jobId
     * @param size  获取多少条
     * @return
     */
    public List<Checkpoint> getCheckpoints(Long jobId, int size) {
        List<SdpJobInstance> sdpJobInstances = instanceService.getMapper().queryLatestRecords(jobId, 10);
        List<Checkpoint> checkpoints = Lists.newArrayList();

        if (!CollectionUtils.isEmpty(sdpJobInstances)) {
            for (SdpJobInstance sdpJobInstance : sdpJobInstances) {
                String checkPointPrefix = flinkConfigProperties.getDefaultMap().get(FlinkConfigKeyConstant.CHECKPOINT_DIR) + sdpJobInstance.getFlinkJobId();
                List<FileStatus> checkPointPaths = com.chitu.bigdata.sdp.utils.HdfsUtils.getCheckPointPaths(getHadoopConfDir(jobId), checkPointPrefix);
                if (CollectionUtils.isEmpty(checkPointPaths)) {
                    //不存在，则寻找下一个实例的检查点
                    log.info("jobName[{}]，flinkJobId[{}]，检查点路径不存在:{}", sdpJobInstance.getJobName(), sdpJobInstance.getFlinkJobId(), checkPointPrefix);
                    continue;
                }

                for (int i = 0, len = checkPointPaths.size(); i < len; i++) {
                    //最多获取多少个检查点数据
                    int orderNum = i + 1;
                    if (orderNum > size) {
                        continue;
                    }
                    FileStatus fileStatus = checkPointPaths.get(i);
                    Checkpoint checkpoint = new Checkpoint();
                    checkpoint.setFilePath(checkPointPrefix + "/" + fileStatus.getPath().getName());
                    checkpoint.setOrderNum(orderNum);
                    checkpoint.setTriggerTime(new Timestamp(fileStatus.getModificationTime()));
                    checkpoints.add(checkpoint);
                }
                //如果存在检查点数据，直接跳出循环
                if (!CollectionUtils.isEmpty(checkpoints)) {
                    break;
                }
            }
        }
        log.info("获取检查点信息：{}", JSON.toJSONString(checkpoints));
        return checkpoints;
    }

    /**
     * 是否存在检查点数据
     *
     * @param jobId
     * @return
     */
    public boolean isExistCheckpoint(Long jobId) {
        return CollectionUtils.isNotEmpty(getCheckpoints(jobId, 1));
    }

    public DirectStartDataVO queryDirectStartData(Long jobId) {
        DirectStartDataVO directStartDataVO = new DirectStartDataVO();

        if (uatJobAutoOfflineJobConfig.isUatOrDev()) {
            SdpUatJobRunningConfig sdpUatJobRunningConfig = new SdpUatJobRunningConfig();
            sdpUatJobRunningConfig.setJobId(jobId);
            sdpUatJobRunningConfig.setEnabledFlag(1L);
            List<SdpUatJobRunningConfig> sdpUatJobRunningConfigs = sdpUatJobRunningConfigService.selectAll(sdpUatJobRunningConfig);
            directStartDataVO.setUatJobRunningValidDays(Optional.ofNullable(sdpUatJobRunningConfigs).filter(f -> !CollectionUtils.isEmpty(f)).map(m -> m.get(0)).map(m -> m.getDays()).orElse(CommonConstant.THREE));
        }

        SdpJob sdpJob = this.getMapper().selectById(jobId);
        List<SourceKafkaInfo> sourceKafkaInfos = getCreateTableList4SourceKafkaInfo(sdpJob.getJobContent());
        directStartDataVO.setSourceKafkaList(sourceKafkaInfos);

        return directStartDataVO;
    }


    public UatJobRunningConfigVO queryRunningConfig(Long jobId) {
        UatJobRunningConfigVO uatJobRunningConfigVO = new UatJobRunningConfigVO();
        uatJobRunningConfigVO.setEnv(sdpConfig.getEnvFromEnvHolder(log) + "环境");

        SdpUatJobRunningConfig sdpUatJobRunningConfig = new SdpUatJobRunningConfig();
        sdpUatJobRunningConfig.setJobId(jobId);
        sdpUatJobRunningConfig.setEnabledFlag(1L);
        List<SdpUatJobRunningConfig> sdpUatJobRunningConfigs = sdpUatJobRunningConfigService.selectAll(sdpUatJobRunningConfig);
        if (!CollectionUtils.isEmpty(sdpUatJobRunningConfigs)) {
            if (CommonConstant.MINUS_ONE.equals(sdpUatJobRunningConfigs.get(0).getDays())) {
                uatJobRunningConfigVO.setRunTimeSetting("作业运行不自动下线");
            } else {
                uatJobRunningConfigVO.setRunTimeSetting(String.format("作业运行%s天后自动下线", sdpUatJobRunningConfigs.get(0).getDays()));
            }
        } else {
            uatJobRunningConfigVO.setRunTimeSetting("-");
        }

        SdpJobInstance latestJobInstance = instanceService.getLatestJobInstance(jobId);
        if (Objects.nonNull(latestJobInstance) && Objects.nonNull(latestJobInstance.getExecuteDuration())) {
            uatJobRunningConfigVO.setExecuteDuration(com.chitu.bigdata.sdp.utils.DateUtils.dhms(latestJobInstance.getExecuteDuration() / 1000L));
        } else {
            uatJobRunningConfigVO.setExecuteDuration(com.chitu.bigdata.sdp.utils.DateUtils.dhms(0L));
        }

        return uatJobRunningConfigVO;
    }

    /**
     * 获取kafka源表建表语句信息
     *
     * @return
     */
    public List<SourceKafkaInfo> getCreateTableList4SourceKafkaInfo(String sql) {
        if (StrUtil.isBlank(sql)) {
            return Collections.emptyList();
        }
        try {
            CommonSqlParser commonSqlParser = new CommonSqlParser(sql);
            return commonSqlParser.getCreateTableList().stream().filter(f -> SqlParserUtil.isSourceKafka(f)).map(m -> {
                Map<String, String> sqlTableOptionMap = SqlParserUtil.getSqlTableOptionMap(m);
                SourceKafkaInfo sourceKafkaInfo = new SourceKafkaInfo();
                sourceKafkaInfo.setFlinkTableName(m.getTableName().toString());
                sourceKafkaInfo.setTopic(sqlTableOptionMap.getOrDefault(FlinkConfigKeyConstant.TOPIC, sqlTableOptionMap.getOrDefault(FlinkConfigKeyConstant.TOPIC_PATTERN, "")));
                sourceKafkaInfo.setStartupMode(sqlTableOptionMap.get(FlinkConfigKeyConstant.STARTUP_MODE));
                String timestampMillis = sqlTableOptionMap.get(FlinkConfigKeyConstant.STARTUP_TIMESTAMP);
                if (StrUtil.isNotBlank(timestampMillis)) {
                    Date date = new Date(Long.valueOf(timestampMillis));
                    sourceKafkaInfo.setDateStr(DateUtil.format(date, DateUtils.YYYY_MM_DD_HH_MM_SS));
                }
                String offsets = sqlTableOptionMap.get(FlinkConfigKeyConstant.STARTUP_OFFSETS);
                if (StrUtil.isNotBlank(offsets)) {
                    sourceKafkaInfo.setOffsets(offsets);
                }

                /*if(startjobReturnWNum){
                    //这个是单条单条获取版本，还有一个批量获取的，都太慢了，暂时不返还
                    sourceKafkaInfo.setJobId(jobId);
                    String wNum = waitConsumedNum(sourceKafkaInfo);
                    sourceKafkaInfo.setWaitConsumedNum(wNum);
                }*/
                return sourceKafkaInfo;
            }).collect(Collectors.toList());
        } catch (Exception e) {
            //这里异常捕获是为了不影响正常启动
            log.error("获取kafka source解析sql异常", e);
            return Collections.emptyList();
        }
    }

    /**
     * kafka源表修改消费模式处理
     *
     * @param jobBO
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleParam(SdpJobBO jobBO) throws Exception {
        String env = sdpConfig.getEnvFromEnvHolder(log);
        CheckConfigProperties.CheckConfig configs = checkConfigProperties.getEnvMap().get(env);
        if (!configs.getStartjob().isMSwitch() || Objects.isNull(jobBO) || CollectionUtils.isEmpty(jobBO.getSourceKafkaList())) {
            return;
        }

        Context context = ContextUtils.get();
        String userId = Optional.ofNullable(context).map(m -> m.getUserId()).orElse("");

        Map<String, SourceKafkaInfo> tableMap = jobBO.getSourceKafkaList().stream().collect(Collectors.toMap(SourceKafkaInfo::getFlinkTableName, m -> m, (k1, k2) -> k2));

        Long jobId = jobBO.getVo().getId();
        SdpJob sdpJob = getMapper().selectById(jobId);
        SdpFile sdpFile = fileService.get(sdpJob.getFileId());

        //锁文件
        SdpFileBO sdpFileBO = new SdpFileBO();
        sdpFileBO.setId(sdpFile.getId());
        fileService.lockFile(sdpFileBO);
        //防止更新文件的时候把原来的设置回去了
        sdpFile.setLockedBy(userId);

        boolean isNewVersion = false;

        StringBuilder ddl = new StringBuilder();
        //===>>>赤兔<<<===
        if (BusinessFlag.SDP.name().equals(sdpJob.getBusinessFlag())) {
            StringBuilder dml = new StringBuilder();

            dml.append(sdpFile.getEtlContent());
            FlinkSqlBuilder flinkSqlBuilder = modifyOption(tableMap, sdpFile.getMetaTableContent());
            ddl.append(flinkSqlBuilder.toSqlString());

            //锁文件(再次锁文件)
            fileService.lockFile(sdpFileBO);

            //更新元表信息
            for (Map<String, String> map : flinkSqlBuilder.modifiedSql.values()) {
                for (Map.Entry<String, String> sqlMap : map.entrySet()) {
                    List<SdpMetaTableConfig> confogMetaList = metaTableConfigService.selectAll(new SdpMetaTableConfig(sdpFile.getId(), sqlMap.getKey()));
                    if (CollectionUtils.isEmpty(confogMetaList)) {
                        continue;
                    }
                    SdpMetaTableConfig confogMeta = confogMetaList.get(0);
                    String sql = sqlMap.getValue() + FlinkSQLConstant.SEPARATOR; // + StrUtils.LF;
                    confogMeta.setFlinkDdl(sql);
                    if (StrUtil.isNotBlank(userId)) {
                        confogMeta.setUpdatedBy(userId);
                    }
                    metaTableConfigService.updateSelective(confogMeta);
                }
            }
            //更新文件信息
            sdpFile.setContent(ddl.toString() + StrUtils.LF + dml.toString());
            sdpFile.setMetaTableContent(ddl.toString());
            sdpFile.setEtlContent(dml.toString());
            if (StrUtil.isNotBlank(userId)) {
                sdpFile.setUpdatedBy(userId);
            }
            fileService.updateSelective(sdpFile);
            //更新任务表信息
            fileService.assembleContent(sdpFile, "online", null);
            sdpJob.setJobContent(sdpFile.getContent());
            if (StrUtil.isNotBlank(userId)) {
                sdpJob.setUpdatedBy(userId);
            }
            updateSelective(sdpJob);
            isNewVersion = true;
        }
        //===>>>数据集成<<<===
        else if (BusinessFlag.DI.name().equals(sdpJob.getBusinessFlag())) {
            //数据集成的sql都放在EtlContent这里面，并且没有元表信息
            String allContent = sdpFile.getEtlContent();
            FlinkSqlBuilder flinkSqlBuilder = modifyOption(tableMap, allContent);
            ddl.append(flinkSqlBuilder.toSqlString());
            //锁文件(再次锁文件)
            fileService.lockFile(sdpFileBO);
            //更新文件信息
            sdpFile.setContent(ddl.toString());
            sdpFile.setEtlContent(ddl.toString());
            if (StrUtil.isNotBlank(userId)) {
                sdpFile.setUpdatedBy(userId);
            }
            fileService.updateSelective(sdpFile);
            //更新任务表信息
            sdpJob.setJobContent(ddl.toString());
            if (StrUtil.isNotBlank(userId)) {
                sdpJob.setUpdatedBy(userId);
            }
            updateSelective(sdpJob);
            isNewVersion = true;
        } else {
            log.info("unknown BusinessFlag");
        }

        //新增一个版本
        if (isNewVersion) {
            SdpVersion version = new SdpVersion();
            version.setFileId(sdpFile.getId());
            SdpVersion sdpVersion = fileService.getVersion(version);
            String currVersion = null;
            if (sdpVersion != null) {
                currVersion = sdpVersion.getFileVersion();
            }
            String latestVersion = VersionUtil.increaseVersion(currVersion);
            sdpJob.setLatestVersion(latestVersion);
            sdpJob.setEnabledFlag(1L);
            //更新job的最新版本号
            updateSelective(sdpJob);

            //上线成功，则插入一个版本和实例
            BeanUtils.copyProperties(sdpFile, version);
            version.setId(null);
            String userName = "";
            if (StrUtil.isNotBlank(userId)) {
                SdpUser sdpUser = userService.get(Long.valueOf(userId));
                userName = Optional.ofNullable(sdpUser).map(m -> m.getUserName()).orElse("");
            }

            version.setRemark("[" + userName + "]修改kafka消费位置");
            version.setFileVersion(latestVersion);
            version.setFileContent(sdpFile.getContent());
            version.setEnabledFlag(1L);

            if (StrUtil.isNotBlank(userId)) {
                version.setCreatedBy(userId);
                version.setUpdatedBy(userId);
            }
            version.setCreationDate(new Timestamp(System.currentTimeMillis()));
            version.setUpdationDate(new Timestamp(System.currentTimeMillis()));
            //拼接元表json封装到字段中
            SdpMetaTableConfig sdpMetaTableConfig = new SdpMetaTableConfig();
            sdpMetaTableConfig.setFileId(sdpFile.getId());
            List<SdpMetaTableConfig> smcs = metaTableConfigService.selectAll(sdpMetaTableConfig);
            if (!CollectionUtils.isEmpty(smcs)) {
                version.setMetaTableJson(JSON.toJSONString(smcs));
            }
            version.setMetaTableContent(sdpFile.getMetaTableContent());
            version.setEtlContent(sdpFile.getEtlContent());
            fileService.insertVersion(version);
        }
    }

    public FlinkSqlBuilder modifyOption(Map<String, SourceKafkaInfo> tableMap, String sql) throws Exception {
        FlinkSqlBuilder flinkSqlBuilder = new FlinkSqlBuilder();
        if (StrUtil.isBlank(sql)) {
            return flinkSqlBuilder;
        }

        String[] flinkSqlList = sql.split(FlinkSQLConstant.SEPARATOR);
        for (int i = 0, len = flinkSqlList.length; i < len; i++) {
            String flinkSql = flinkSqlList[i];

            if (StrUtil.isBlank(flinkSql) || StrUtil.isBlank(flinkSql.trim())) {
                continue;
            }
            String cloneSql = new String(flinkSql);
            cloneSql = SqlUtil.removeNote(cloneSql);
            cloneSql = SqlUtil.clearNotes(cloneSql);
            if (StrUtil.isBlank(cloneSql) || StrUtil.isBlank(cloneSql.trim())) {
                continue;
            }

            CommonSqlParser commonSqlParser = null;
            try {
                commonSqlParser = new CommonSqlParser(flinkSql);
            } catch (Exception e) {
                log.error("修改消费位置-解析sql语句出错", e);
            }
            //是否是建表语句且是kafka并且是source表
            if (Objects.nonNull(commonSqlParser) && CollectionUtils.isNotEmpty(commonSqlParser.getCreateTableList()) && SqlParserUtil.isSourceKafka(commonSqlParser.getCreateTableList().get(0))) {
                //可能需要修改消费位置的建表语句
                SqlCreateTable sqlCreateTable = commonSqlParser.getCreateTableList().get(0);
                String flinkTableName = sqlCreateTable.getTableName().toString();
                List<SqlNode> options = sqlCreateTable.getPropertyList().getList();

                SourceKafkaInfo sourceKafkaInfo = tableMap.get(flinkTableName);
                //判断是否有修改
                if (Objects.nonNull(sourceKafkaInfo)) {
                    //创建新选项
                    String timestampStr = StrUtil.isNotBlank(sourceKafkaInfo.getDateStr()) && StrUtil.isNotBlank(sourceKafkaInfo.getDateStr().trim()) ? String.valueOf(DateUtil.parse(sourceKafkaInfo.getDateStr(), DateUtils.YYYY_MM_DD_HH_MM_SS).getTime()) : "";
                    Map<String, SqlTableOption> replaceTableOptionMap = SqlParserUtil.getTableOptionMap(sourceKafkaInfo.getStartupMode(), timestampStr, sourceKafkaInfo.getOffsets());

                    int startUpModeLocation = removeOption(options, FlinkConfigKeyConstant.STARTUP_MODE);
                    insertOption(options, startUpModeLocation, replaceTableOptionMap.get(FlinkConfigKeyConstant.STARTUP_MODE));

                    int startUpModeLocation4Ts = removeOption(options, FlinkConfigKeyConstant.STARTUP_TIMESTAMP);
                    if (sourceKafkaInfo.getStartupMode().equals(FlinkConfigKeyConstant.TIMESTAMP)) {
                        if (-1 == startUpModeLocation4Ts) {
                            insertOption(options, options.size(), replaceTableOptionMap.get(FlinkConfigKeyConstant.STARTUP_TIMESTAMP));
                        } else {
                            insertOption(options, startUpModeLocation4Ts, replaceTableOptionMap.get(FlinkConfigKeyConstant.STARTUP_TIMESTAMP));
                        }
                    }

                    int startUpModeLocation4Offset = removeOption(options, FlinkConfigKeyConstant.STARTUP_OFFSETS);
                    if (sourceKafkaInfo.getStartupMode().equals(FlinkConfigKeyConstant.SPECIFIC_OFFSETS)) {
                        if (-1 == startUpModeLocation4Offset) {
                            insertOption(options, options.size(), replaceTableOptionMap.get(FlinkConfigKeyConstant.STARTUP_OFFSETS));
                        } else {
                            insertOption(options, startUpModeLocation4Offset, replaceTableOptionMap.get(FlinkConfigKeyConstant.STARTUP_OFFSETS));
                        }
                    }

                    String keyword = ReUtil.getGroup1(".*(\\)\\s*WITH\\s*\\().*", flinkSql);
                    int index = flinkSql.lastIndexOf(keyword);
                    if (index > 0) {
                        String prefix = flinkSql.substring(0, index + keyword.length());
                        StringBuilder modifySql = new StringBuilder(prefix);

                        modifySql.append(StrUtils.LF);
                        String kv = "'%s' = '%s'";
                        for (int j = 0, xlen = options.size(); j < xlen; j++) {
                            SqlTableOption sqlTableOption = (SqlTableOption) options.get(j);
                            if (Objects.isNull(sqlTableOption)) {
                                continue;
                            }
                            if (j == xlen - 1) {
                                modifySql.append(String.format(kv, sqlTableOption.getKeyString(), sqlTableOption.getValueString())).append(StrUtils.LF);
                            } else {
                                modifySql.append(String.format(kv, sqlTableOption.getKeyString(), sqlTableOption.getValueString())).append(StrUtils.DLF);
                            }
                        }
                        modifySql.append(")");

                        Map<String, String> flinkSqlMap = new HashMap<>();
                        flinkSqlMap.put(flinkTableName, modifySql.toString());
                        flinkSqlBuilder.modifiedSql.put(Integer.valueOf(i), flinkSqlMap);
                    } else {
                        flinkSqlBuilder.unModifiedSql.put(Integer.valueOf(i), flinkSql);
                    }
                } else {
                    //没有修改
                    flinkSqlBuilder.unModifiedSql.put(Integer.valueOf(i), flinkSql);
                }
            } else {
                //function|view
                flinkSqlBuilder.unModifiedSql.put(Integer.valueOf(i), flinkSql);
            }
        }
        return flinkSqlBuilder;
    }

    /**
     * 删除选项，返回下标位置
     *
     * @param options
     * @param optionKey
     * @return
     */
    private int removeOption(List<SqlNode> options, String optionKey) {
        int location = -1;
        if (CollectionUtils.isEmpty(options)) {
            return location;
        }
        int index = -1;
        final Iterator<SqlNode> each = options.iterator();
        while (each.hasNext()) {
            ++index;
            SqlTableOption next = (SqlTableOption) each.next();
            if (Objects.nonNull(next) && next.getKeyString().equals(optionKey)) {
                each.remove();
                location = index;
            }
        }
        return location;
    }

    /**
     * 按照下标写回去
     *
     * @param options
     * @param location
     * @param sqlTableOption
     * @return
     */
    private List<SqlNode> insertOption(List<SqlNode> options, int location, SqlTableOption sqlTableOption) {
        if (CollectionUtils.isEmpty(options)) {
            return options;
        }
        if (location >= 0) {
            options.add(location, sqlTableOption);
        }
        return options;
    }

    public Object JobListByDataSource(SdpDataSourceBO datasourceBo) {
        try {
            PageHelper.startPage(datasourceBo.getPage(), datasourceBo.getPageSize());
            Page<DatasourceJobInf> page = (Page) this.getMapper().JobListByDataSource(datasourceBo);
            Pagination pagination = Pagination.getInstance(datasourceBo.getPage(), datasourceBo.getPageSize());
            pagination.setRows(page);
            pagination.setRowTotal((int) page.getTotal());
            pagination.setPageTotal(page.getPages());
            return pagination;
        } catch (Exception e) {
            log.error("JobListByDataSource select error=", e);
            return null;
        }
    }

    @Data
    public static class FlinkSqlBuilder {
        //排序号 -> flinkTableName -> sql ： 1比1比1关系
        Map<Integer, Map<String, String>> modifiedSql = new HashMap<>();
        //排序号 ->  sql
        Map<Integer, String> unModifiedSql = new HashMap<>();

        /**
         * 保证sql的顺序，版本比较的时候不至于错位太多
         *
         * @return
         */
        public String toSqlString() {
            StringBuilder sqlStr = new StringBuilder();
            int size = modifiedSql.size() + unModifiedSql.size();
            for (int i = 0; i < size; i++) {
                Map<String, String> modifiedSqlMap = modifiedSql.get(Integer.valueOf(i));
                if (!org.springframework.util.CollectionUtils.isEmpty(modifiedSqlMap)) {
                    modifiedSqlMap.forEach((k, v) -> {
                        sqlStr.append(v).append(FlinkSQLConstant.SEPARATOR); //.append(StrUtils.LF);
                    });
                }

                String sql = unModifiedSql.get((Integer.valueOf(i)));
                if (StrUtil.isNotBlank(sql)) {
                    sqlStr.append(sql).append(FlinkSQLConstant.SEPARATOR); //.append(StrUtils.LF);
                }
            }
            return sqlStr.toString();
        }
    }

    public String waitConsumedNum(SourceKafkaInfo sourceKafkaInfo) {
        Long jobId = sourceKafkaInfo.getJobId();
        SdpJob sdpJob = getMapper().selectById(jobId);
        if (Objects.isNull(sdpJob)) {
            log.info("===jobId:{}==={}", jobId, "作业为空");
            return StrUtils.numToW(null);
        }

        SdpFile sdpFile = fileService.get(sdpJob.getFileId());
        if (Objects.isNull(sdpFile)) {
            log.info("===jobId:{}==={}", jobId, "文件为空");
            return StrUtils.numToW(null);
        }

        List<SdpMetaTableConfig> confogMetaList = metaTableConfigService.selectAll(new SdpMetaTableConfig(sdpFile.getId(), sourceKafkaInfo.getFlinkTableName()));
        if (CollectionUtils.isEmpty(confogMetaList)) {
            log.info("===jobId:{}==={}", jobId, "元表配置为空");
            return StrUtils.numToW(null);
        }
        SdpMetaTableConfig confogMeta = confogMetaList.get(0);
        SdpDataSource sdpDataSource = dataSourceService.getByIdWithPwdPlaintext(confogMeta.getDataSourceId());
        if (Objects.isNull(sdpDataSource)) {
            log.info("===jobId:{}==={}", jobId, "数据源为空");
            return StrUtils.numToW(null);
        }

        ConnectInfo connectInfo = new ConnectInfo();
        connectInfo.setAddress(sdpDataSource.getDataSourceUrl());
        connectInfo.setUsername(sdpDataSource.getUserName());
        connectInfo.setPwd(sdpDataSource.getPassword());
        connectInfo.setCertifyType(sdpDataSource.getCertifyType());
        log.info("获取消费数 _ConnectInfo_: {}", JSON.toJSONString(connectInfo));
        String groupId = "SDP_" + sourceKafkaInfo.getFlinkTableName() + "_" + IdUtil.simpleUUID();
        if (FlinkConfigKeyConstant.GROUP_OFFSETS.equals(sourceKafkaInfo.getStartupMode())) {
            //group-offsets启动模式时需要传递真实groupId
            try {
                CommonSqlParser sqlParser = new CommonSqlParser(sdpJob.getJobContent());
                for (SqlCreateTable sqlCreateTable : sqlParser.getCreateTableList()) {
                    if (SqlParserUtil.isSourceKafka(sqlCreateTable)) {
                        List<SqlNode> nodes = sqlCreateTable.getPropertyList().getList();
                        Map<String, String> options = new HashMap<>();
                        for (SqlNode z : nodes) {
                            SqlTableOption option = (SqlTableOption) z;
                            options.put(option.getKeyString(), option.getValueString());
                        }
                        if (options.get("topic").equals(sourceKafkaInfo.getTopic())) {
                            groupId = options.get("properties.group.id");
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        Long timestamp = null;
        if (StrUtil.isNotBlank(sourceKafkaInfo.getDateStr()) && StrUtil.isNotBlank(sourceKafkaInfo.getDateStr().trim())) {
            timestamp = DateUtil.parse(sourceKafkaInfo.getDateStr(), DateUtils.YYYY_MM_DD_HH_MM_SS).getTime();
        }
        Long num = null;
        try {
            num = kafkaDataSource.waitConsumedNum(connectInfo, sourceKafkaInfo.getStartupMode(), groupId, sourceKafkaInfo.getTopic(), timestamp);
        } catch (Exception e) {
            log.info("===jobId:{}==={}", jobId, "获取待消费数异常");
            log.error("获取待消费数失败", e);
            return StrUtils.numToW(null);
        }
        return StrUtils.numToW(num);
    }

    public String getHadoopConfDir(Long jobId) {
        String env = sdpConfig.getEnvFromEnvHolder(log);
        SdpJob sdpJob = get(jobId);
        JobConfig jobConfig = JSONObject.parseObject(sdpJob.getConfigContent(), JobConfig.class);
        SdpEngine engine = engineService.get(jobConfig.getEngineId());
        ClusterInfo clusterInfo = engineService.getClusterInfo(env, engine);
        return clusterInfo.getHadoopConfDir();
    }

    @Deprecated
    public boolean tempCheckCanal(Long projectId) {
        String redisKey = "bigdata:sdp:checkcanal:" + projectId;
        ValueOperations<String, String> valueOperations = redisTmplate.opsForValue();
        String existStr = valueOperations.get(redisKey);
        if (StrUtil.isNotBlank(existStr)) {
            throw new ApplicationException(ResponseCode.COMMON_ERR, "正在处理，请稍等...");
        }

        valueOperations.set(redisKey, "处理中", 20, TimeUnit.MINUTES);
        CompletableFuture.runAsync(() -> {
            try {
                List<SdpDataSource> dataSourceList = dataSourceService.getMapper().selectForCanalTemp(projectId);
                if (CollectionUtils.isNotEmpty(dataSourceList)) {
                    for (SdpDataSource sdpDataSource : dataSourceList) {
                        ConnectInfo connectInfo = new ConnectInfo();
                        connectInfo.setAddress(sdpDataSource.getDataSourceUrl());
                        connectInfo.setDatabaseName(sdpDataSource.getDatabaseName());
                        connectInfo.setUsername(sdpDataSource.getUserName());
                        connectInfo.setPwd(sdpDataSource.getPassword());
                        connectInfo.setCertifyType(sdpDataSource.getCertifyType());

                        Properties props = new Properties();
                        props.put("bootstrap.servers", connectInfo.getAddress());
                        props.put("group.id", "getTopicColumnGroup");
                        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
                        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
                        props.put("enable.auto.commit", "false");
                        if (Objects.nonNull(connectInfo.getCertifyType()) && CertifyType.SASL.getType().equals(connectInfo.getCertifyType())) {
                            props.put("group.id", consumerSaslKafkaMetaGroupId);
                            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
                            props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256");
                            props.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"" + connectInfo.getUsername() + "\" password=\"" + connectInfo.getPwd() + "\";");
                        }
                        //latest  earliest
                        props.put("auto.offset.reset", "earliest");
                        props.put("max.poll.records", "100");
                        props.put("fetch.max.wait.ms", "1000");
                        props.put("session.timeout.ms", "30000");
                        KafkaConsumer<String, String> consumer = null;
                        try {
                            consumer = new KafkaConsumer<>(props);
                            String topic = sdpDataSource.getHbaseZnode();
                            consumer.subscribe(Collections.singletonList(topic));
                            ConsumerRecords<String, String> records = consumer.poll(6000);
                            for (ConsumerRecord<String, String> record : records) {
                                if (JsonUtils.isCanalJson(record.value())) {
                                    //canal-json
                                    JSONObject jsonObject = JSON.parseObject(record.value(), Feature.OrderedField);
                                    if (Objects.isNull(jsonObject)) {
                                        continue;
                                    }
                                    JSONArray data = jsonObject.getJSONArray("data");
                                    if (CollectionUtil.isEmpty(data)) {
                                        continue;
                                    }
                                    int size = data.size();
                                    if (size > 1) {
                                        log.info("canal同步-data字段存在多条数据: 数据源名称[{}],topic[{}] -> {}条", sdpDataSource.getDataSourceName(), topic, size);
                                        break;
                                    }
                                }
                            }
                        } catch (Exception e) {
                            log.warn("排查canal数据异常", e);
                        } finally {
                            if (consumer != null) {
                                consumer.close();
                            }
                        }

                        try {
                            Thread.sleep(2000);
                        } catch (Exception e) {

                        }
                    }
                }
            } finally {
                redisTmplate.delete(redisKey);
            }
        });
        return true;
    }
}
