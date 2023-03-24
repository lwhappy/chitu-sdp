

/**
 * <pre>
 * 作   者：CHENYUN
 * 创建日期：2021-10-15
 * </pre>
 */

package com.chitu.bigdata.sdp.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chitu.bigdata.sdp.api.bo.SdpFileBO;
import com.chitu.bigdata.sdp.api.domain.*;
import com.chitu.bigdata.sdp.api.enums.*;
import com.chitu.bigdata.sdp.api.model.*;
import com.chitu.bigdata.sdp.api.vo.*;
import com.chitu.bigdata.sdp.config.*;
import com.chitu.bigdata.sdp.constant.*;
import com.chitu.bigdata.sdp.flink.common.conf.ConfigConst;
import com.chitu.bigdata.sdp.flink.common.util.DeflaterUtils;
import com.chitu.bigdata.sdp.flink.common.util.HadoopUtils;
import com.chitu.bigdata.sdp.flink.common.util.PropertiesUtils;
import com.chitu.bigdata.sdp.interceptor.EnvHolder;
import com.chitu.bigdata.sdp.mapper.*;
import com.chitu.bigdata.sdp.service.datasource.DataSourceFactory;
import com.chitu.bigdata.sdp.service.datasource.KafkaDataSource;
import com.chitu.bigdata.sdp.service.notify.EmailNotifyService;
import com.chitu.bigdata.sdp.service.validate.ClusterServiceImpl;
import com.chitu.bigdata.sdp.service.validate.constant.FlinkSQLConstant;
import com.chitu.bigdata.sdp.service.validate.custom.CustomTableEnvironmentImpl;
import com.chitu.bigdata.sdp.service.validate.domain.JobConfigs;
import com.chitu.bigdata.sdp.service.validate.domain.SqlExplainResult;
import com.chitu.bigdata.sdp.service.validate.job.JobManager;
import com.chitu.bigdata.sdp.service.validate.util.SqlUtil;
import com.chitu.bigdata.sdp.utils.*;
import com.chitu.bigdata.sdp.utils.flink.sql.MetaTableTypeFetcher;
import com.chitu.bigdata.sdp.utils.flink.sql.ViewTableMapFetcher;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.model.ResponseData;
import com.chitu.cloud.service.GenericService;
import com.chitu.cloud.utils.Context;
import com.chitu.cloud.utils.ContextUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.xiaoleilu.hutool.date.DateUtil;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.PageUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.SqlIdentifier;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlWith;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.calcite.shaded.com.google.common.collect.ImmutableList;
import org.apache.flink.sql.parser.ddl.SqlCreateCatalog;
import org.apache.flink.sql.parser.ddl.SqlCreateTable;
import org.apache.flink.sql.parser.ddl.SqlCreateView;
import org.apache.flink.sql.parser.ddl.SqlTableOption;
import org.apache.flink.sql.parser.dml.RichSqlInsert;
import org.apache.flink.table.operations.Operation;
import org.apache.flink.table.operations.ddl.CreateCatalogOperation;
import org.apache.flink.table.operations.ddl.CreateTableOperation;
import org.apache.hadoop.fs.FileSystem;
import org.assertj.core.util.Lists;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import scala.collection.JavaConversions;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.chitu.bigdata.sdp.utils.JobOperationUtils.REDIS_KEY_PREFIX;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/17 14:06
 */
@Service
@Slf4j
@RefreshScope
public class FileService extends GenericService<SdpFile, Long> {
    public FileService(@Autowired SdpFileMapper sdpFileMapper) {
        super(sdpFileMapper);
    }

    public SdpFileMapper getMapper() {
        return (SdpFileMapper) super.genericMapper;
    }

    private static final String NAME_REGULAR = "([A-Za-z0-9_\\-])+";
    private static final String SQL_CONTENT = "%s\r\n%s";
    private static final String SQL_PATTEN = "\\$\\{%s\\}";
    private static final String YAML_PRE = "sql.param.%s";
    private static final String PARSE_RES = "'password' =(.*),";
    private static final String PARSE_RES_2 = "\" password=\"(.*)'";
    private static final String HIVE_OPTION = "\\/\\*\\+( )*OPTIONS\\(.*\\*\\/";
    private static String LINE_REGEX = "line \\d{1,}";


    @Autowired
    private EmailNotifyService emailNotifyService;

    /**
     * 指定存量文件的创建时间. ，格式：yyyyMMddHHmm
     */
    @Value("${file.edit.checkCreateTime:202211182130}")
    private Long checkCreateTime;

    @Autowired
    private SdpFileMapper sdpFileMapper;
    @Autowired
    private SdpJobMapper jobMapper;
    @Autowired
    private SdpJarMapper jarMapper;
    @Autowired
    private SdpVersionMapper versionMapper;
    @Autowired
    private SdpJobInstanceMapper instanceMapper;
    @Autowired
    private SdpProjectEngineMapper sdpProjectEngineMapper;
    @Autowired
    private SdpUserMapper sdpUserMapper;
    @Autowired
    private SdpOperationLogMapper logMapper;
    @Autowired
    private SdpJobAlertRuleService sdpJobAlertRuleService;
    @Autowired
    private FlinkConfigProperties flinkConfigProperties;
    @Autowired
    private SdpMetaTableConfigMapper mtaTableConfigMapper;
    @Autowired
    private DataSourceFactory dataSourceFactory;
    @Autowired
    private SdpDataSourceMapper sdpDataSourceMapper;
    @Autowired
    DataSourceService dataSourceService;
    @Autowired
    private SdpApproveMapper approveMapper;
    @Autowired
    private SdpFileExtraMapper fileExtraMapper;
    @Autowired
    private SdpFolderMapper folderMapper;
    @Autowired
    private MetaTableConfigService configService;
    @Autowired
    private SdpMetaTableRelationMapper tableRelationMapper;
    @Autowired
    SdpMetaTableRelationService sdpMetaTableRelationService;
    @Autowired
    SdpSysConfigService sdpSysConfigService;
    @Autowired
    UserService userService;
    @Autowired
    RedisLocker redisLocker;


    @Autowired
    EngineService engineService;

    @Autowired
    private ClusterInfoConfig clusterInfoConfig;

    @Autowired
    DataSourceConfigProperties dataSourceConfigProperties;
    @Autowired
    ProjectService projectService;
    @Autowired
    SdpConfig sdpConfig;
    @Autowired
    CheckConfigProperties checkConfigProperties;
    @Autowired
    FileService fileService;
    @Autowired
    JobService jobService;


    private final static String ONLINE = "online";


    /**
     * 保存点路径
     */
    @Value("${flink.common.savepointDir}")
    private String savepointDir;

    @Value("${clusterSyncLocalDir}")
    private String clusterSyncLocalDir;


    @Autowired
    SdpProjectMapper sdpProjectMapper;
    @Autowired
    RedisTemplateService redisTemplateService;
    @Autowired
    KafkaDataSource kafkaDataSource;
    @Autowired
    JarService jarService;


    /**
     * 检查点间隔时间
     */
    private final static Long CHECKPOINTING_INTERVAL = 5 * 60 * 1000L;

    private static String COMMENT_REGEX = "(^\\s{0,}--|\\n\\s{0,}--|\\r\\n\\s{0,}--).{0,}";

    private static String HANDLE_META_TABLE_RELATION_MSG = "===handleRelation4Metatable[{}]: {} -> {}";

    @Transactional(rollbackFor = Exception.class)
    public SdpFile addFile(SdpFileBO fileBO) {
        Context context = ContextUtils.get();
        SdpFile file = new SdpFile();
        BeanUtils.copyProperties(fileBO, file);
        //移除空行
        String newEtlContent = fileBO.getEtlContent();
//        if(!StringUtils.isEmpty(fileBO.getEtlContent())){
//            newEtlContent = fileBO.getEtlContent().replaceAll("(?m)^\\s*$"+System.lineSeparator(), "").replaceAll("(?m)^\\s*$"+ StrUtils.LF, "").trim();
//        }
        file.setEtlContent(newEtlContent);
        if (fileBO.getUserId() != null) {
            file.setCreatedBy(fileBO.getUserId() + "");
            file.setUpdatedBy(fileBO.getUserId() + "");
        }
        //检验名称规则
        if (!fileBO.getFileName().matches(NAME_REGULAR)) {
            throw new ApplicationException(ResponseCode.FILE_CONFORM_RULE, fileBO.getFileName());
        }
        //设置flink配置默认值
        if (FileType.SQL_STREAM.getType().equals(fileBO.getFileType())) {
            String flag = file.getBusinessFlag();
            if (!StringUtils.isEmpty(flag) && flag.equals(BusinessFlag.SDP.toString())) {
                fileBO.getJobConfig().setFlinkYaml(flinkConfigProperties.getSqlTemplate());
            }
        } else {
            fileBO.getJobConfig().setFlinkYaml(flinkConfigProperties.getDStreamTemplate());
        }
        file.setConfigContent(JSON.toJSONString(fileBO.getJobConfig()));
        file.setSourceContent(JSON.toJSONString(fileBO.getSourceConfig()));
        packageFileTypeParams(fileBO, file);
        file.setFileStatus(FileStatus.CREATED.toString());
        if (context != null && org.apache.commons.lang.StringUtils.isNotBlank(context.getUserId())) {
            file.setLockedBy(context.getUserId());
        }

        String requetEnv = sdpConfig.getEnvFromEnvHolder(null);
        for (String env : sdpConfig.getEnvList()) {
            try {
                EnvHolder.clearEnvAndAddEnv(env);
                SdpFile file1 = this.getMapper().getFile(file);
                if (file1 != null) {
                    String msg = "该作业在" + (EnvironmentEnum.UAT.getCode().equals(env) ? "UAT环境" : (EnvironmentEnum.PROD.getCode().equals(env) ? "生产环境" : "")) + "已存在,不能重名";
                    throw new ApplicationException(ResponseCode.FILE_IS_EXISTS.getCode(), msg);
                }
            } finally {
                EnvHolder.clearEnvAndAddEnv(requetEnv);
            }
        }

        //作业等级设置
        SdpProject sdpProject = projectService.get(file.getProjectId());
        Integer priority = Objects.nonNull(file.getPriority()) ? file.getPriority() : (Objects.nonNull(sdpProject) && Objects.nonNull(sdpProject.getPriority()) ? sdpProject.getPriority() : PriorityLevel.P2.getType());
        file.setPriority(priority);

        insertSelective(file);
        String flag = fileBO.getBusinessFlag();
        if (!StringUtils.isEmpty(flag) && flag.equals(BusinessFlag.SDP.toString())) {
            //插入元表关系表【当sink hive时】
            fileBO.setId(file.getId());
            MetaTableRelationResult relationResult = handleMetaTableRelation(fileBO);
            try {
                handleRelation4Metatable(relationResult);
            } catch (Exception e) {
                String errMsg = StrUtils.parse1(HANDLE_META_TABLE_RELATION_MSG, fileBO.getId(), "元表关系处理异常");
                log.warn(errMsg, e);
            }
        }
        return file;
    }

    private String reBuildEtlContent(String etlContent) {
        if (StrUtil.isBlank(etlContent) || StrUtil.isBlank(etlContent.trim())) {
            return "";
        }
        Pattern compile = Pattern.compile(HIVE_OPTION);
        Matcher errorMa = compile.matcher(etlContent);
        LinkedHashSet<String> keySet = new LinkedHashSet<>();
        while (errorMa.find()) {
            keySet.add(errorMa.group(0));
        }
        if (0 != keySet.size()) {
            Iterator<String> iterator = keySet.iterator();
            while (iterator.hasNext()) {
                String next = iterator.next();
                if (!StrUtil.isBlank(next)) {
                    etlContent = etlContent.replaceAll(HIVE_OPTION, "");
                }
            }
        }
        return etlContent;
    }

    private boolean isSinkHive(List<SdpMetaTableConfig> previous, String node) {
        String table = node;
        for (SdpMetaTableConfig metaTableConfig : previous) {
            if (table.equalsIgnoreCase(metaTableConfig.getFlinkTableName())) {
                return false;
            }
        }
        return true;
    }

    private void packageFileTypeParams(SdpFileBO fileBO, SdpFile file) {
        if (FileType.SQL_STREAM.getType().equals(fileBO.getFileType())) {
            String newEtlContent = StrUtils.SPACE;
            String newMetaContent = StrUtils.SPACE;
            //移除空行
            if (!StringUtils.isEmpty(fileBO.getMetaTableContent())) {
                newMetaContent = fileBO.getMetaTableContent();
            }
            if (!StringUtils.isEmpty(fileBO.getEtlContent())) {
                newEtlContent = fileBO.getEtlContent();
            }
            file.setContent(String.format(SQL_CONTENT, newMetaContent, newEtlContent));
        } else {
            if (Objects.nonNull(fileBO.getDataStreamConfig())) {
                file.setDataStreamConfig(JSON.toJSONString(fileBO.getDataStreamConfig()));
            }
        }
    }

    private List<SqlExplainResult> checkYaml(SdpFile file, List<SqlExplainResult> data) {
        String configuredFormat = "%s页面已单独配置，不能配置";
        String defaultConfigFormat = "%s后端默认配置，不能重新配置";
        List<SqlExplainResult> wrongKey = new ArrayList<>();
        JSONObject jsonObject = new JSONObject();
        if (Objects.nonNull(file.getConfigContent())) {
            String configContent = file.getConfigContent();
            JobConfig jobConfig = JSONObject.parseObject(configContent, JobConfig.class);
            String flag = file.getBusinessFlag();
            YamlUtils.parseYaml(jsonObject, jobConfig, flag, data);
            Set<String> keySet = jsonObject.keySet();
            for (String key : keySet) {
                if (FlinkConfigKeyConstant.isConfigured(key)) {
                    SqlExplainResult result = new SqlExplainResult();
                    String error = String.format(configuredFormat, key);
                    result.setError(error);
                    result.setType("error");
                    wrongKey.add(result);
                }
                if (FlinkConfigKeyConstant.isDefaultConfig(key)) {
                    SqlExplainResult result = new SqlExplainResult();
                    String error = String.format(defaultConfigFormat, key);
                    result.setError(error);
                    result.setType("error");
                    wrongKey.add(result);
                }
            }
        }
        return wrongKey;
    }

    public List<SdpFileResp> queryFile(SdpFileBO fileBO) {
        String userId = ContextUtils.get().getUserId();
        SdpFile file = new SdpFile();
        BeanUtils.copyProperties(fileBO, file);
        List<SdpFile> list = sdpFileMapper.selectAll(file);
        ArrayList<SdpFileResp> sdpFileResps = new ArrayList<>();
        list.forEach(x -> {
            SdpFileResp sdpFileResp = new SdpFileResp();
            BeanUtils.copyProperties(x, sdpFileResp);
            String lockedBy = x.getLockedBy();
            sdpFileResp.setLockSign(EnableType.UNABLE.getCode());
            if (Objects.nonNull(lockedBy)) {
                if (userId.equals(lockedBy)) {
                    sdpFileResp.setLockSign(EnableType.ENABLE.getCode());
                }
            } else {
                sdpFileResp.setLockSign(EnableType.ENABLE.getCode());
            }
            SdpUser sdpUser = sdpUserMapper.selectById(Long.valueOf(lockedBy == null ? "0" : lockedBy));
            if (Objects.nonNull(sdpUser)) {
                sdpFileResp.setLockedBy(sdpUser.getUserName());
            }
            sdpFileResp.setJobConfig(JSONObject.parseObject(x.getConfigContent(), JobConfig.class));
            sdpFileResp.setSourceConfig(JSONObject.parseObject(x.getSourceContent(), SourceConfig.class));
            sdpFileResp.setConfigContent(null);
            sdpFileResp.setSourceContent(null);
            sdpFileResps.add(sdpFileResp);
        });
        return sdpFileResps;
    }

    public Integer removeFile(SdpFileBO fileBO) {
        SdpFile file = new SdpFile();
        BeanUtils.copyProperties(fileBO, file);
        file.setBusinessFlag(null);
        return updateSelective(file);
    }

    @Transactional(rollbackFor = Exception.class)
    public SdpFile copyFile(SdpFileBO fileBO) {
        Context context = ContextUtils.get();
        String newName = fileBO.getFileName();
        //检验名称规则
        if (StringUtils.isEmpty(newName)) {
            throw new ApplicationException(ResponseCode.FILE_NOT_NULL);
        }
        if (!newName.matches(NAME_REGULAR)) {
            throw new ApplicationException(ResponseCode.FILE_CONFORM_RULE, newName);
        }
        SdpFile file = get(fileBO.getId());
        file.setId(null);
        file.setFileName(newName);
        file.setFileStatus(FileStatus.CREATED.toString());
        file.setLockedBy(context.getUserId());
        file.setCreatedBy(context.getUserId());
        file.setUpdatedBy(context.getUserId());
        file.setCreationDate(new Timestamp(System.currentTimeMillis()));
        file.setUpdationDate(new Timestamp(System.currentTimeMillis()));
        SdpFile file1 = this.getMapper().getFile(file);
        if (file1 != null) {
            throw new ApplicationException(ResponseCode.FILE_IS_EXISTS);
        }
        insert(file);

        SdpProject sdpProject = projectService.get(file.getProjectId());

        //复制元表配置
        List<SdpMetaTableConfig> metaTables = mtaTableConfigMapper.queryByFileId(fileBO.getId());
        if (CollectionUtils.isNotEmpty(metaTables)) {
            metaTables.forEach(x -> {
                x.setId(null);
                x.setFileId(file.getId());
                x.setCreatedBy(context.getUserId());
                x.setUpdatedBy(context.getUserId());
                x.setCreationDate(new Timestamp(System.currentTimeMillis()));
                x.setUpdationDate(new Timestamp(System.currentTimeMillis()));

                String flinkDdl = modifyGroupId(x.getFlinkDdl(), sdpProject.getProjectCode(), newName);
                x.setFlinkDdl(flinkDdl);

            });
            configService.insertBatch(metaTables);
        }

        // 更新文件中的元表信息
        List<SdpMetaTableConfig> newMetaTables = mtaTableConfigMapper.queryByFileId(file.getId());
        if (CollectionUtil.isNotEmpty(newMetaTables)) {
            StringBuilder mtcBuilder = new StringBuilder();
            for (int i = 0, len = newMetaTables.size(); i < len; i++) {
                SdpMetaTableConfig smtc = newMetaTables.get(i);
                String flinkDdl = smtc.getFlinkDdl();
                if (StrUtil.isBlank(flinkDdl)) {
                    log.warn("该元表【{}】的flinkDdl为空", smtc.getId());
                    continue;
                }
                if (i == len - 1) {
                    mtcBuilder.append(flinkDdl);
                } else {
                    mtcBuilder.append(flinkDdl);
                    if (!flinkDdl.trim().endsWith(StrUtils.CRLF) && !flinkDdl.trim().endsWith(StrUtils.LF)) {
                        mtcBuilder.append(StrUtils.CRLF);
                    }
                }
            }
            log.info("复制文件-合并元表内容： {}", mtcBuilder.toString());
            file.setMetaTableContent(mtcBuilder.toString());
            file.setContent(String.format(SQL_CONTENT, file.getMetaTableContent(), file.getEtlContent()));
            fileService.updateSelective(file);
        } else {
            log.info("{}-该文件的元表信息为空", file.getId());
        }
        return file;
    }

    public String modifyGroupId(String ddl, String projectCode, String fileName) {
        String newDdl = ddl;
        try {
            CommonSqlParser commonSqlParser = new CommonSqlParser(ddl);
            List<SqlCreateTable> createTableList = commonSqlParser.getCreateTableList();
            if (CollectionUtil.isNotEmpty(createTableList)) {
                if (SqlParserUtil.isSourceKafka(createTableList.get(0))) {
                    Map<String, String> optionsMap = new HashMap<>();
                    optionsMap.put(FlinkConfigKeyConstant.PROPERTIES_GROUP_ID, String.format(CommonConstant.GROUP_ID_FORMAT, projectCode, fileName));
                    Map<String, SqlTableOption> replaceOptionsMap = SqlParserUtil.getTableOptionMap(optionsMap);

                    List<SqlNode> options = createTableList.get(0).getPropertyList().getList();
                    int offset1 = SqlParserUtil.removeOption(options, FlinkConfigKeyConstant.PROPERTIES_GROUP_ID);
                    if (-1 == offset1) {
                        SqlParserUtil.insertOption(options, options.size(), replaceOptionsMap.get(FlinkConfigKeyConstant.PROPERTIES_GROUP_ID));
                    } else {
                        SqlParserUtil.insertOption(options, offset1, replaceOptionsMap.get(FlinkConfigKeyConstant.PROPERTIES_GROUP_ID));
                    }

                    StringBuilder modifySql = SqlParserUtil.flinkSqlBuilder(ddl, options);
                    newDdl = modifySql.toString();
                }
            }
        } catch (Exception e) {
            log.warn("修改groupId异常", e);
        }
        return newDdl;
    }

    /**
     * 文件是否锁定校验
     *
     * @param fileId
     */
    public void fileLockCheck(Long fileId) {
        Context context = ContextUtils.get();
        if (Objects.nonNull(context)) {
            SdpFile sdpFile = this.get(fileId);
            if (!context.getUserId().equals(sdpFile.getLockedBy())) {
                throw new ApplicationException(ResponseCode.FILE_IS_LOCKED);
            }
        }
    }

    @Transactional(rollbackFor = Exception.class)
    public SdpFileResp updateFile(SdpFileBO fileBO) {
        SdpFile file = new SdpFile();
        BeanUtils.copyProperties(fileBO, file);
        //移除空行
        String newEtlContent = fileBO.getEtlContent();
//        if(!StringUtils.isEmpty(fileBO.getEtlContent())){
//            newEtlContent = fileBO.getEtlContent().replaceAll("(?m)^\\s*$"+System.lineSeparator(), "").replaceAll("(?m)^\\s*$"+ StrUtils.LF, "").trim();
//        }
        file.setEtlContent(newEtlContent);
        //首先判断锁定者是否为本人
        SdpFile file1 = get(fileBO.getId());
        if (file.getBusinessFlag().equals(BusinessFlag.SDP.toString())) {
            Context context = ContextUtils.get();
            String locked = file1.getLockedBy();
            if (Objects.nonNull(context)) {
                if (Objects.nonNull(locked)) {
                    if (!context.getUserId().equals(locked)) {
                        throw new ApplicationException(ResponseCode.FILE_IS_LOCKED);
                    }
                } else {
                    fileBO.setLockedBy(context.getUserId());
                }
            }
        }
        //校验名称不可以更改
        if (Objects.nonNull(file1) || !StringUtils.isEmpty(file1.getFileName())) {
            if (!file1.getFileName().equals(fileBO.getFileName())) {
                throw new ApplicationException(ResponseCode.FILE_CANNOT_CHANGE);
            }
        }
        if (Objects.isNull(fileBO.getJobConfig().getFlinkYaml())) {
            throw new ApplicationException(ResponseCode.YAML_CONF_EMPTY);
        }
        if (file.getBusinessFlag().equals(BusinessFlag.SDP.toString())) {
            fileBO.getJobConfig().setFlinkYaml(DeflaterUtils.zipString(fileBO.getJobConfig().getFlinkYaml()));
        }
        file.setConfigContent(JSON.toJSONString(fileBO.getJobConfig()));
        file.setSourceContent(JSON.toJSONString(fileBO.getSourceConfig()));
        packageFileTypeParams(fileBO, file);
        file.setBusinessFlag(null);
        log.info("更新后jar信息===" + file.getDataStreamConfig());
        FileEditVO fileEditVO = allowEditJob(fileBO);
        if (!fileEditVO.getAllowEdit()) {
            log.info("文件【{}】不允许编辑,内容置空不更新", fileBO.getId());
            //sql
            file.setContent(null);
            file.setEtlContent(null);
            file.setMetaTableContent(null);

            //ds
            file.setDataStreamConfig(null);
        }
        updateSelective(file);

        //插入元表关系表【当sink hive时】
        MetaTableRelationResult relationResult = handleMetaTableRelation(fileBO);
        try {
            handleRelation4Metatable(relationResult);
        } catch (Exception e) {
            String errMsg = StrUtils.parse1(HANDLE_META_TABLE_RELATION_MSG, fileBO.getId(), "元表关系处理异常");
            log.error(errMsg, e);
        }

        //返回最新版本信息，为上线准备
        SdpFileResp sdpFileResp = new SdpFileResp();
        SdpVersion version = new SdpVersion();
        version.setFileId(file.getId());
        SdpVersion sdpVersion = getVersion(version);
        String currVersion = null;
        if (sdpVersion != null) {
            currVersion = sdpVersion.getFileVersion();
        }
        String latestVersion = VersionUtil.increaseVersion(currVersion);
        file = get(file.getId());
        BeanUtils.copyProperties(file, sdpFileResp);
        sdpFileResp.setVersion(latestVersion);
        return sdpFileResp;
    }

    @Transactional(rollbackFor = Exception.class)
    public Integer deleteFile(SdpFileBO fileBO) {
        //先判断该文件是否在作业表存在
        SdpJob job = jobMapper.queryJobByFileId(fileBO.getId());
        if (job != null) {
            throw new ApplicationException(ResponseCode.JOB_IS_EXISTS);
        }
        List<SdpApprove> approves = approveMapper.queryByFileId(fileBO.getId());
        if (CollectionUtils.isNotEmpty(approves)) {
            throw new ApplicationException(ResponseCode.FILE_IS_APPROVING);
        }

        String headerEnv = EnvHolder.getEnv();
        if (EnvironmentEnum.UAT.getCode().equals(headerEnv)) {
            SdpFile sdpFile = fileService.get(fileBO.getId());
            if (Objects.nonNull(sdpFile) && BusinessFlag.SDP.name().equals(sdpFile.getBusinessFlag())) {
                try {
                    EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.PROD.getCode());

                    SdpFile paramSdpFile = new SdpFile();
                    paramSdpFile.setEnabledFlag(EnableType.ENABLE.getCode());
                    paramSdpFile.setFileName(sdpFile.getFileName());
                    paramSdpFile.setProjectId(sdpFile.getProjectId());
                    List<SdpFile> sdpFiles = fileService.selectAll(paramSdpFile);
                    if (CollectionUtils.isNotEmpty(sdpFiles)) {
                        throw new ApplicationException(ResponseCode.COMMON_ERR, "生产环境已存在该作业，不允许删除");
                    }

                } finally {
                    EnvHolder.clearEnvAndAddEnv(headerEnv);
                }
            }
        }

        //删除元表数据
        SdpMetaTableConfig sdpMetaTableConfig = new SdpMetaTableConfig();
        sdpMetaTableConfig.setFileId(fileBO.getId());
        sdpMetaTableConfig.setEnabledFlag(1L);
        List<SdpMetaTableConfig> metaTableConfigs = configService.selectAll(sdpMetaTableConfig);
        if (CollectionUtils.isNotEmpty(metaTableConfigs)) {
            Long[] ids = metaTableConfigs.stream().map(x -> x.getId()).toArray(Long[]::new);
            configService.disable(SdpMetaTableConfig.class, ids);
        }
        return disable(SdpFile.class, fileBO.getId());
    }


    public List<SqlExplainResult> explainSql(SdpFileBO fileBO) {
        SdpFile file = get(fileBO.getId());
        if (file == null) {
            throw new ApplicationException(ResponseCode.FILE_NOT_EXISTS);
        }
        List<SqlExplainResult> data = new ArrayList();

        checkHiveCataLog(file, data);
        checkFileParams(file, data);
        if (file.getFileType().equals(FileType.SQL_STREAM.getType())) {
            assembleContent(file, "validate", data);
        }
        replacePlaceholder(file, data);
        String content = file.getContent().replaceAll(StrUtils.CRLF, StrUtils.LF);
        //校验DDL部分
        if (ddlValidate(content, file, data)) {
            content = file.getMetaTableContent();
        }

        if (log.isDebugEnabled()) {
            log.info("executeValidateSQL: {}", content);
        }
        List<SqlExplainResult> result = executeValidate(content);
        log.info("校验作业{}，返回的校验结果为{}", file.getFileName(), Objects.nonNull(result) ? JSON.toJSONString(result) : "null");
        //对敏感信息进行脱敏处理
        handleSensitive(result);

        if (!CollectionUtils.isEmpty(result)) {
            data.addAll(result);
        }
        //经过assembleContent方法后，content的内容变为明文了，此时更新文件的校验状态，不需要把content字段更新入库
        file.setContent(null);
        if (CollectionUtils.isNotEmpty(data)) {
            file.setFileStatus(FileStatus.FAILED.toString());
        } else {
            file.setFileStatus(FileStatus.PASS.toString());
        }
        updateSelective(file);
        return data;
    }

    private void checkHiveCataLog(SdpFile file, List<SqlExplainResult> data) {
        List<String> hiveConfList = new ArrayList<>();
        String env = sdpConfig.getEnvFromEnvHolder(log);
        DataSourceConfigProperties.HiveConfig hiveConfig = dataSourceConfigProperties.getEnvMap().get(env);
        for (DataSourceConfigProperties.HiveClusterInfo hiveClusterInfo : hiveConfig.getHive()) {
            String dataSourceUrl = hiveClusterInfo.getDataSourceUrl();
            // 去除url最后的/
            if (!StringUtils.isEmpty(dataSourceUrl) && dataSourceUrl.charAt(dataSourceUrl.length() - 1) == '/') {
                dataSourceUrl = dataSourceUrl.substring(0, dataSourceUrl.length() - 1);
            }
            hiveConfList.add(dataSourceUrl);
        }
        try {
            List<SqlCreateCatalog> createCatalogList = new CommonSqlParser(file.getContent()).getCreateCatalogList();
            if (CollectionUtil.isNotEmpty(createCatalogList)) {
                createCatalogList.stream().forEach(item -> {
                    for (SqlNode sqlNode : item.getPropertyList().getList()) {
                        SqlTableOption tableOption = (SqlTableOption) sqlNode;
                        if (tableOption.getKeyString().equals("hive-conf-dir")) {
                            String hiveConfDir = tableOption.getValueString();
                            // 去除dir最后的/
                            if (!StringUtils.isEmpty(hiveConfDir) && hiveConfDir.charAt(hiveConfDir.length() - 1) == '/') {
                                hiveConfDir = hiveConfDir.substring(0, hiveConfDir.length() - 1);
                            }
                            if (!hiveConfList.contains(hiveConfDir)) {
                                SqlExplainResult explainResult = new SqlExplainResult();
                                String errorMsg = String.format("hive catalog：[%s]的hive-conf-dir配置值已过期或不正确，请重新生成元表或者编辑！具体配置值可以到 数据源管理->选择hive数据源->选择hive集群，查看对应配置目录", item.getCatalogName());
                                explainResult.setError(errorMsg);
                                data.add(explainResult);
                            }
                            break;
                        }
                    }
                });
            }
        } catch (Exception e) {
            // 避免影响主流程
            log.warn("sql解析失败！", e);
        }

    }

    private void handleSensitive(List<SqlExplainResult> result) {
        if (!CollectionUtils.isEmpty(result)) {
            result.stream().map(x -> {
                String error = x.getError();
                takeOffSecret(error, PARSE_RES);
                takeOffSecret(error, PARSE_RES_2);
                x.setError(error);
                String sql = x.getSql();
                takeOffSecret(sql, PARSE_RES);
                takeOffSecret(sql, PARSE_RES_2);
                x.setSql(sql);
                return x;
            }).collect(Collectors.toList());
        }
    }

    public boolean ddlValidate(String content, SdpFile file, List<SqlExplainResult> data) {
        boolean sqlCreateFunction = false;
        try {
            sqlCreateFunction = SqlParserUtil.isSqlCreateFunction(content);
        } catch (Exception e) {
            log.error("sql前置校验异常", e);
            //包装提示
            boolean isTip = false;
            SqlExplainResult x = new SqlExplainResult();
            String error = e.getMessage();
            if (StrUtil.isNotBlank(error)) {
                takeOffSecret(error, PARSE_RES);
                takeOffSecret(error, PARSE_RES_2);
                x.setError(error);
                x.setLine(getErrorLine(error));
                isTip = true;
            }

            String sql = content;
            if (StrUtil.isNotBlank(sql)) {
                takeOffSecret(sql, PARSE_RES);
                takeOffSecret(sql, PARSE_RES_2);
                //这个地方会返回整个sql content，且里面的密码没有处理掉，先不返回了
//                x.setSql(sql.replaceAll(COMMENT_REGEX,""));
                isTip = true;
            }
            if (isTip) {
                data.add(x);
            }
        }
        if (sqlCreateFunction) {
            //如果是带有UDF函数的SQL，则只对DDL语句做校验
            JobConfig jobConfig = JSONObject.parseObject(file.getConfigContent(), JobConfig.class);
            if (null == jobConfig.getJarId()) {
                SqlExplainResult explainResult = new SqlExplainResult();
                explainResult.setError(ResponseCode.UDF_FUNCTION_EXCEPTION.getMessage());
                data.add(explainResult);
            }
        }
        return sqlCreateFunction;
    }

    private Integer getErrorLine(String error) {
        Integer line = 0;
        Pattern pattern = Pattern.compile(LINE_REGEX);
        Matcher matcher = pattern.matcher(error);
        if (matcher.find()) {
            String line2 = matcher.group();
            String line3 = line2.replace("line ", "");
            line = Integer.valueOf(line3);
        }
        return line;
    }

    private static final String CHECKSOURCE_LOGMSG = "check source table[{}] -> {}";


    private String checkHiveCheckpointInterval(String configContent) {
        String env = sdpConfig.getEnvFromEnvHolder(log);
        CheckConfigProperties.CheckConfig configs = checkConfigProperties.getEnvMap().get(env);
        String tip = "[hive检查点间隔时间]建议设置为[%s],设置路径：高级配置-execution.checkpointing.interval: %s";
        if (StrUtil.isBlank(configContent)) {
            return String.format(tip, configs.getHive().getExecutionCheckpointingInterval(), configs.getHive().getExecutionCheckpointingInterval());
        }

        JobConfig jobConfig = JSONObject.parseObject(configContent, JobConfig.class);
        Map<String, String> yamlMap = YamlUtils.parseYaml(jobConfig.getFlinkYaml());
        Integer interval = Integer.valueOf(yamlMap.getOrDefault(FlinkConstant.EXECUTION_CHECKPOINTING_INTERVAL, "0"));
        if (interval < configs.getHive().getExecutionCheckpointingInterval()) {
            return String.format(tip, configs.getHive().getExecutionCheckpointingInterval(), configs.getHive().getExecutionCheckpointingInterval());
        }

        return "";
    }

    private void checkWriteHive(RichSqlInsert insertSql, List<SqlExplainResult> data) {
        //禁止往非白名单的hive库写入数据
        //获取数据库名称(sdp_test.sdp_test.hive_table06)
        SqlIdentifier targetTableID = (SqlIdentifier) insertSql.getTargetTableID();
        ImmutableList<String> names = targetTableID.names;
        String needCheckDbName = Objects.nonNull(names) && names.size() >= 2 ? names.get(1) : null;

        List<DataSourceWriteContrl> dataSourceWriteContrls = sdpSysConfigService.getValListByKey(SdpSysConfigConstant.DATASOURCE_WRITE_CONTRLS, DataSourceWriteContrl.class);
        Map<String, String> datasourceCtrlMap = Optional.ofNullable(dataSourceWriteContrls).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(DataSourceWriteContrl::getDataSourceType, DataSourceWriteContrl::getContent, (k1, k2) -> k2));
        String hiveDbName = datasourceCtrlMap.getOrDefault(DataSourceType.HIVE.getType(), "");
        List<String> hiveDbNameWhitelist = Splitter.on(",").omitEmptyStrings().trimResults().splitToList(hiveDbName.replace("，", ","));

        //非白名单
        if (Objects.nonNull(needCheckDbName) && !hiveDbNameWhitelist.contains(needCheckDbName)) {
            SqlExplainResult explainResult = new SqlExplainResult();
            explainResult.setError("[" + needCheckDbName + "]是非白名单hive库，详情请联系系统管理员");
            data.add(explainResult);
        }
    }


    /**
     * 是否是允许使用的ip
     *
     * @param allowIps
     * @param ips
     * @return
     */
    public boolean isAllowIps(List<String> allowIps, String[] ips) {
        boolean allow = false;
        for (String ipStr : ips) {
            String ip = ipStr.substring(0, ipStr.indexOf(":"));
            String realIp = null;
            try {
                //域名地址返回的格式为：
                realIp = InetAddress.getByName(ip).getHostAddress();
            } catch (UnknownHostException e) {
                e.printStackTrace();
                log.info("pase host error.source={}", ipStr);
            }
            log.info("check ip: {}", Optional.ofNullable(realIp).orElse("null"));
            if (realIp != null && allowIps.contains(realIp)) {
                allow = true;
                break;
            }
        }
        return allow;
    }

    private void takeOffSecret(String error, String pt) {
        Pattern compile = Pattern.compile(pt);
        Matcher errorMa = compile.matcher(error);
        LinkedHashSet<String> keySet = new LinkedHashSet<>();
        while (errorMa.find()) {
            keySet.add(errorMa.group(1));
        }
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            if (StrUtil.isBlank(next)) {
                error = error.replaceAll(next, "*******");
            }
        }
    }

    private boolean checkFileParams(SdpFile file, List<SqlExplainResult> data) {
        if (FileType.DATA_STREAM.getType().equals(file.getFileType())) {
            return true;
        }
        engineClusterCheck(file, data);
        //校验元表和转换sql
        String flag = file.getBusinessFlag();
        if (flag != null && flag.equalsIgnoreCase(BusinessFlag.SDP.toString())) {
            //etl部分不能包含ddl逻辑
            String etlContent = file.getEtlContent();
            if (!StringUtils.isEmpty(etlContent)) {
                String regex1 = "(CREATE|create)\\s+(TABLE|table)";
                Pattern pattern1 = Pattern.compile(regex1);
                Matcher matcher1 = pattern1.matcher(etlContent);
                if (matcher1.find()) {
                    SqlExplainResult explainResult = new SqlExplainResult();
                    explainResult.setError(ResponseCode.FILE_DDL_EXCEPTION.getMessage());
                    data.add(explainResult);
                }
            }
            //限制双斜杆的注释使用
            String content = file.getMetaTableContent() + etlContent;
            //将注释行替换为空行
            content = content.replaceAll(COMMENT_REGEX, StrUtils.LF);
            if (content.contains("//")) {
                SqlExplainResult explainResult = new SqlExplainResult();
                explainResult.setError(ResponseCode.UN_SUPPORT_NOTES.getMessage());
                data.add(explainResult);
            }
        }
        //校验yaml是否存在异常
        List<SqlExplainResult> wrongKeys = checkYaml(file, data);
        if (!CollectionUtils.isEmpty(wrongKeys)) {
            data.addAll(wrongKeys);
        }
        return false;
    }

    private void engineClusterCheck(SdpFile file, List<SqlExplainResult> data) {
        try {
            SdpVersion sdpVersion = getVersion(new SdpVersion(file.getId()));
            // 校验是否有切换引擎集群，非首次上线才校验
            if (sdpVersion != null) {
                JobConfig latestJobConfig = JSONObject.parseObject(file.getConfigContent(), JobConfig.class);
                JobConfig currVersionJobConfig = JSONObject.parseObject(sdpVersion.getConfigContent(), JobConfig.class);
                SdpEngine latestEngine = engineService.get(latestJobConfig.getEngineId());
                SdpEngine currVersionEngine = null;
                if (currVersionJobConfig.getEngineId() == null) {
                    currVersionEngine = engineService.selectAll(new SdpEngine(currVersionJobConfig.getEngine())).get(0);
                } else {
                    currVersionEngine = engineService.get(currVersionJobConfig.getEngineId());
                }
                if (!latestEngine.getEngineCluster().equals(currVersionEngine.getEngineCluster())) {
                    // 查询任务运行状态
                    SdpJob sdpJob = jobMapper.queryJobByFileId(file.getId());
                    if (sdpJob != null) {
                        SdpJobInstance originInstance = instanceMapper.queryByJobId(sdpJob.getId());
                        if (originInstance != null) {
                            if (JobStatus.RUNNING.toString().equals(originInstance.getJobStatus())) {
                                SqlExplainResult explainResult = new SqlExplainResult();
                                String errorMsg = String.format("当前任务已在原引擎[%s]集群中运行，请先停止后验证上线！", currVersionEngine.getEngineName());
                                explainResult.setError(errorMsg);
                                data.add(explainResult);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            // 避免影响主流程
            log.error("切换引擎校验逻辑异常", e);
        }
    }

    public List<SqlExplainResult> executeValidate(String content) {
        JobConfigs config = new JobConfigs();
        config.setUseStatementSet(true);
        if (!config.isUseSession()) {
            ClusterServiceImpl clusterService = new ClusterServiceImpl();
            config.setAddress(clusterService.buildEnvironmentAddress());
        }
        JobManager jobManager = JobManager.build(config);
        List<SqlExplainResult> list = jobManager.explainSql(content).getSqlExplainResults();
        return list.stream().filter(x -> x.getError() != null).collect(Collectors.toList());
    }

    @Transactional(rollbackFor = Exception.class)
    public SdpJob online(SdpFileBO fileBO) {
        String onlineKey = String.format("%s:%s:%s", REDIS_KEY_PREFIX, "job_online", fileBO.getId());
        //为了避免上线操作被并发调用, 这里获取锁等待5秒, 锁有效期30秒
        boolean success = redisLocker.tryLock(onlineKey, 5000L, 30000L);
        if (!success) {
            throw new ApplicationException(ResponseCode.ACQUIRE_JOB_LOCK_FAIL.getCode(),
                    ResponseCode.ACQUIRE_JOB_LOCK_FAIL.getMessage() + ", fileId: " + fileBO.getId());
        }

        SdpJob job = new SdpJob();
        try {
            SdpFile param = new SdpFile();
            param.setId(fileBO.getId());
            SdpFile file = this.getMapper().getFile(param);
            if (file == null) {
                throw new ApplicationException(ResponseCode.FILE_NOT_EXISTS.getCode(), ResponseCode.FILE_NOT_EXISTS.getMessage());
            }
            if (!FileStatus.PASS.toString().equals(file.getFileStatus())) {
                throw new ApplicationException(ResponseCode.SQL_NOT_PASS.getCode(), ResponseCode.SQL_NOT_PASS.getMessage());
            }
            if (file.getFileType().equals(FileType.SQL_STREAM.getType())) {
                //对content里面的数据进行敏感值替换处理
                assembleContent(file, "online", null);
                //进行sql中赋值替换
                replacePlaceholder(file, null);
            }
            job.setJobName(file.getFileName());
            job.setJobContent(file.getContent());
            job.setConfigContent(file.getConfigContent());
            job.setSourceContent(file.getSourceContent());
            job.setDataStreamConfig(file.getDataStreamConfig());
            job.setBusinessFlag(file.getBusinessFlag());
            job.setProjectId(file.getProjectId());
            job.setFileId(file.getId());
            //设置版本
            SdpVersion version = new SdpVersion();
            version.setFileId(file.getId());
            SdpVersion sdpVersion = getVersion(version);
            String currVersion = null;
            if (sdpVersion != null) {
                currVersion = sdpVersion.getFileVersion();
            }
            String latestVersion = VersionUtil.increaseVersion(currVersion);
            job.setLatestVersion(latestVersion);
            job.setEnabledFlag(1L);
            Long applyBy = fileBO.getUserId();
            Context context = ContextUtils.get();
            SdpJob sdpJob = jobMapper.queryJobByFileId(file.getId());
            if (sdpJob != null) {
                job.setId(sdpJob.getId());
                job.setEnabledFlag(EnableType.ENABLE.getCode());
                job.setCreatedBy(sdpJob.getCreatedBy());

                SdpProject sdpProject = sdpProjectMapper.selectById(file.getProjectId());
                Integer priority = Objects.nonNull(sdpJob.getPriority()) ? sdpJob.getPriority() : null;
                priority = Objects.nonNull(priority) ? priority : file.getPriority();
                priority = Objects.nonNull(priority) ? priority : sdpProject.getPriority();
                priority = Objects.nonNull(priority) ? priority : PriorityLevel.P2.getType();

                job.setPriority(priority);
                if (context != null) {
                    job.setUpdatedBy(applyBy == null ? context.getUserId() : String.valueOf(applyBy));
                }
                job.setUpdationDate(new Timestamp(System.currentTimeMillis()));
                log.info("更新作业-jar信息===" + job.getDataStreamConfig());
                jobService.updateSelective(job);
            } else {
                if (context != null && org.apache.commons.lang.StringUtils.isNotBlank(context.getUserId())) {
                    job.setCreatedBy(applyBy == null ? context.getUserId() : String.valueOf(applyBy));
                    job.setUpdatedBy(applyBy == null ? context.getUserId() : String.valueOf(applyBy));
                } else {
                    if (fileBO.getUserId() != null) {
                        job.setCreatedBy(fileBO.getUserId() + "");
                        job.setUpdatedBy(fileBO.getUserId() + "");
                    }
                }
                SdpProject sdpProject = sdpProjectMapper.selectById(file.getProjectId());
                Integer priority = Objects.nonNull(file.getPriority()) ? file.getPriority() : (Objects.nonNull(sdpProject.getPriority()) ? sdpProject.getPriority() : PriorityLevel.P2.getType());
                job.setPriority(priority);
                job.setCreationDate(new Timestamp(System.currentTimeMillis()));
                job.setUpdationDate(new Timestamp(System.currentTimeMillis()));
                log.info("插入作业-jar信息===" + job.getDataStreamConfig());
                jobMapper.insert(job);
            }
            //上线成功，则插入一个版本和实例
            BeanUtils.copyProperties(file, version);
            version.setId(null);
            version.setRemark(fileBO.getRemark());
            version.setFileVersion(latestVersion);
            version.setFileContent(file.getContent());
            version.setEnabledFlag(1L);
            //applyBy不为null，则表示是审批同意之后自动发起的上线操作，此时createdBy为发起申请人
            if (context != null && org.apache.commons.lang.StringUtils.isNotBlank(context.getUserId())) {
                version.setCreatedBy(applyBy == null ? context.getUserId() : String.valueOf(applyBy));
                version.setUpdatedBy(applyBy == null ? context.getUserId() : String.valueOf(applyBy));
            } else {
                if (fileBO.getUserId() != null) {
                    version.setCreatedBy(fileBO.getUserId() + "");
                    version.setUpdatedBy(fileBO.getUserId() + "");
                }
            }
            version.setCreationDate(new Timestamp(System.currentTimeMillis()));
            version.setUpdationDate(new Timestamp(System.currentTimeMillis()));
            //拼接元表json封装到字段中
            SdpMetaTableConfig sdpMetaTableConfig = new SdpMetaTableConfig();
            sdpMetaTableConfig.setFileId(file.getId());
            List<SdpMetaTableConfig> smcs = configService.selectAll(sdpMetaTableConfig);
            if (!CollectionUtils.isEmpty(smcs)) {
                version.setMetaTableJson(JSON.toJSONString(smcs));
            }
            version.setMetaTableContent(file.getMetaTableContent());
            version.setEtlContent(file.getEtlContent());
            insertVersion(version);
            //初始化一个实例
            SdpJobInstance originInstance = instanceMapper.queryByJobId(job.getId());
            SdpJobInstance instance = insertInstance(originInstance, job, context);

            //添加默认告警规则
            if (VersionUtil.INIT_VERSION_PLUS.equals(job.getLatestVersion()) || CollectionUtil.isEmpty(sdpJobAlertRuleService.selectAll(new SdpJobAlertRule(job.getId())))) {
                sdpJobAlertRuleService.addDefaultRule(job);
            }
            Assert.notNull(instance, "file online get the instance must be not null");
            //插入日志
            insertLog(instance, context);


            // 校验是否有切换引擎集群，非首次上线才校验
            if (sdpVersion != null && originInstance != null) {
                JobConfig latestJobConfig = JSONObject.parseObject(file.getConfigContent(), JobConfig.class);
                JobConfig currVersionJobConfig = JSONObject.parseObject(sdpVersion.getConfigContent(), JobConfig.class);
                if (Objects.isNull(latestJobConfig) || Objects.isNull(latestJobConfig.getEngineId())) {
                    log.info("作业引擎不存在，请配置!");
                    throw new ApplicationException(ResponseCode.ENGINE_NOT_EXIST.getCode(), "作业引擎不存在，请配置");
                }
                SdpEngine latestEngine = engineService.get(latestJobConfig.getEngineId());
                if (Objects.isNull(latestEngine)) {
                    log.info("作业引擎不存在，请配置!!!");
                    throw new ApplicationException(ResponseCode.ENGINE_NOT_EXIST.getCode(), "作业引擎不存在，请配置");
                }
                SdpEngine currVersionEngine = null;
                if (currVersionJobConfig.getEngineId() == null) {
                    currVersionEngine = engineService.selectAll(new SdpEngine(currVersionJobConfig.getEngine())).get(0);
                } else {
                    currVersionEngine = engineService.get(currVersionJobConfig.getEngineId());
                }
                // 当引擎id不同就同步
                if (!Objects.equals(latestEngine.getId(), currVersionEngine.getId())) {
                    log.info("文件[{}], 当前文件版本[{}], 当前引擎id[{}], 上线引擎id[{}]", file.getFileName(), currVersion, currVersionEngine.getId(), latestEngine.getId());
                    syncClusterCheckPointAndSavePoint(currVersionEngine, latestEngine, originInstance.getFlinkJobId());
                }
            }

            //删除集群缓存重新获取
            boolean isDel = redisTemplateService.delete(RedisTemplateService.HADOOP_CONF_DIR, job.getId());
            log.info("[{}]作业集群缓存删除：{}", job.getId(), isDel);

            //上线成功，发送跨声通知
            //项目管理员+系统配置名单
            List<SdpUser> sdpUsers = userService.queryReceiveUserList(file.getProjectId(), NotifiUserType.COMMON_NOTICE_USERS);
            //普通成员
            List<SdpUser> users3 = sdpUserMapper.queryCommonUserByProject(file.getProjectId());
            if (CollectionUtils.isNotEmpty(users3)) {
                sdpUsers.addAll(users3);
            }
            //去重
            Set<String> receiveUers = sdpUsers.stream().map(x -> x.getEmail()).collect(Collectors.toSet());

            JobResp sdpJob1 = jobMapper.queryJob4Notify(job);
            if (null != sdpJob1) {
                //申请人自己也要发通知
                //admin.add(sdpJob1.getEmployeeNumber());
                receiveUers.add(sdpJob1.getEmail());
                //log.info("上线成功发送通知===" + JSON.toJSONString(receiveUers));
                //emailNotifyService.sendMsg(Lists.newArrayList(receiveUers), "上线", JSON.toJSONString(assembleMsgContent(sdpJob1)));
            }
            //置空content防止信息泄漏
            job.setJobContent(null);

            return job;
        } finally {
            redisLocker.unlock(onlineKey);
        }
    }

    /**
     * 不同集群同步检查点和保存点数据
     *
     * @param currentEngine
     * @param targetEngine
     * @param flinkJobId
     */
    private void syncClusterCheckPointAndSavePoint(SdpEngine currentEngine, SdpEngine targetEngine, String flinkJobId) {
        String env = sdpConfig.getEnvFromEnvHolder(log);
        String currentClusterCode;
        String targetClusterCode;
        if (EnvironmentEnum.UAT.getCode().equals(env)) {
            currentClusterCode = currentEngine.getUatEngineCluster();
            targetClusterCode = targetEngine.getUatEngineCluster();
        } else {
            currentClusterCode = currentEngine.getEngineCluster();
            targetClusterCode = targetEngine.getEngineCluster();
        }

        log.info("env: {}, currentClusterCode: {}, targetClusterCode: {}", env, currentClusterCode, targetClusterCode);

        List<ClusterInfo> clusterInfos = engineService.getClusterInfos(env);
        log.info("clusterInfos: {}", JSONObject.toJSONString(clusterInfos));

        // 根据集群code获取hadoop配置目录
        Map<String, List<ClusterInfo>> clusterMap = clusterInfos.stream().collect(Collectors.groupingBy(ClusterInfo::getClusterCode));
        String currentHadoopConf = clusterMap.get(currentClusterCode).get(0).getHadoopConfDir();
        String targetHadoopConf = clusterMap.get(targetClusterCode).get(0).getHadoopConfDir();

        log.info("currentHadoopConf: {}, targetHadoopConf: {}", currentHadoopConf, targetHadoopConf);

        try {
            String srcHdfsFile = flinkConfigProperties.getDefaultMap().get(FlinkConfigKeyConstant.CHECKPOINT_DIR) + flinkJobId;

            String localFilePath = clusterSyncLocalDir + "ck/" + flinkJobId;

            log.info("同步ck srcHdfsFile: {}, localFilePath: {}", srcHdfsFile, localFilePath);

            // 先下载到本地
            HdfsUtils.copyToLocalFile(currentHadoopConf, srcHdfsFile, localFilePath);

            // 删除目标集群文件目录
            HdfsUtils.delete(targetHadoopConf, srcHdfsFile);

            // 上传
            HdfsUtils.uploadFile(targetHadoopConf, localFilePath, srcHdfsFile);

            srcHdfsFile = savepointDir + flinkJobId;
            localFilePath = clusterSyncLocalDir + "savepoints/" + flinkJobId;
            log.info("同步savepoint srcHdfsFile: {}, localFilePath: {}", srcHdfsFile, localFilePath);
            HdfsUtils.copyToLocalFile(currentHadoopConf, srcHdfsFile, localFilePath);
            HdfsUtils.delete(targetHadoopConf, srcHdfsFile);
            HdfsUtils.uploadFile(targetHadoopConf, localFilePath, srcHdfsFile);


        } catch (Exception e) {
            log.error("同步集群ck异常", e);
            throw new RuntimeException("同步集群ck异常", e);
        }

    }

    private Map<String, String> assembleMsgContent(SdpJob sdpJob) {
        Map<String, String> msgContent = new HashMap<>();
        msgContent.put("projectName", sdpJob.getProjectName());
        msgContent.put("jobName", sdpJob.getJobName());
        msgContent.put("approveStatus", "上线成功");
        msgContent.put("approveApplicator", sdpJob.getUpdatedBy());
        msgContent.put("triggerTime", DateUtil.formatDateTime(sdpJob.getUpdationDate()));
        return msgContent;
    }

    /**
     * 此方法用于将yaml配置的变量替换到sql中；yaml中形式为sql.param.xxx: value，在sql的格式为${xxx}
     *
     * @param file
     */
    private void replacePlaceholder(SdpFile file, List<SqlExplainResult> data) {
        LinkedHashSet<String> keySet = new LinkedHashSet<>();
        String configContent = file.getConfigContent();
        String content = file.getContent();
        if (StringUtils.isEmpty(content)) {
            return;
        }
        Pattern compile = Pattern.compile(StrUtils.SQL_PLACEHOLDER);
        Matcher matcher = compile.matcher(content);
        JobConfig jobConfig = JSON.parseObject(configContent, JobConfig.class);
        JSONObject jsonObject = new JSONObject();
        String flag = file.getBusinessFlag();
        YamlUtils.parseYaml(jsonObject, jobConfig, flag, data);
        while (matcher.find()) {
            keySet.add(matcher.group(1));
        }
        Iterator<String> iterator = keySet.iterator();
        while (iterator.hasNext()) {
            String next = iterator.next();
            String key = String.format(YAML_PRE, next);
            String value = jsonObject.getString(key);
            if (Objects.nonNull(value)) {
                String format = String.format(SQL_PATTEN, next);
                content = content.replaceAll(format, value);
            }
        }
        file.setContent(content);
    }

    /**
     * 此方法是用于将敏感字段替换进行校验
     *
     * @param file
     * @param type
     */
    public void assembleContent(SdpFile file, String type, List<SqlExplainResult> data) {
        String flag = file.getBusinessFlag();
        if (flag != null && flag.equals(BusinessFlag.SDP.toString())) {
            String metaTableContent = file.getMetaTableContent();
            if (StringUtils.isEmpty(metaTableContent)) {
                if (null != data) {
                    SqlExplainResult explainResult = new SqlExplainResult();
                    explainResult.setError(ResponseCode.META_TABLE_EMPTY.getMessage());
                    data.add(explainResult);
                    return;
                } else {
                    throw new ApplicationException(ResponseCode.META_TABLE_EMPTY);
                }
            }
            String[] statements = SqlUtil.getStatements(metaTableContent.replaceAll(StrUtils.CRLF, StrUtils.LF), FlinkSQLConstant.SEPARATOR + StrUtils.LF);
            StringBuffer metaTableSql = new StringBuffer();
            for (String x : statements) {
                if (org.apache.commons.lang3.StringUtils.isNotBlank(x)) {
                    try {
                        List<SqlNode> sqlNodes = SqlParserUtil.getSqlNodes(x);
                        for (SqlNode sqlNode : sqlNodes) {
                            if (sqlNode instanceof SqlCreateTable || sqlNode instanceof SqlCreateCatalog) {
                                Map<String, String> options = new LinkedHashMap<>();
                                String tableName = null;
                                List<SqlNode> nodes = null;
                                if (sqlNode instanceof SqlCreateTable) {
                                    tableName = ((SqlCreateTable) sqlNode).getTableName().toString();
                                    nodes = ((SqlCreateTable) sqlNode).getPropertyList().getList();
                                } else {
                                    tableName = ((SqlCreateCatalog) sqlNode).getCatalogName().toString();
                                    nodes = ((SqlCreateCatalog) sqlNode).getPropertyList().getList();
                                }
                                for (SqlNode z : nodes) {
                                    SqlTableOption option = (SqlTableOption) z;
                                    options.put(option.getKeyString(), option.getValueString());
                                }
                                SdpMetaTableConfig sdpMetaTableConfig = new SdpMetaTableConfig();
                                sdpMetaTableConfig.setFlinkTableName(tableName);
                                sdpMetaTableConfig.setFileId(file.getId());
                                List<SdpMetaTableConfig> metaTableConfigs = configService.selectAll(sdpMetaTableConfig);
                                if (CollectionUtils.isEmpty(metaTableConfigs)) {
                                    if (CollectionUtils.isNotEmpty(data)) {
                                        SqlExplainResult explainResult = new SqlExplainResult();
                                        explainResult.setError(ResponseCode.META_TABLE_NOT_EXISTS.getMessage());
                                        data.add(explainResult);
                                    } else {
                                        throw new ApplicationException(ResponseCode.META_TABLE_NOT_EXISTS);
                                    }
                                }
                                SdpMetaTableConfig metaTableConfig = metaTableConfigs.get(0);
                                SdpDataSource sdpDataSource = dataSourceService.getByIdWithPwdPlaintext(metaTableConfig.getDataSourceId());

                                options = dataSourceFactory.getDataSource(sdpDataSource.getDataSourceType()).replaceConnParam(options, sdpDataSource);
                                ArrayList<String> list = new ArrayList<>();
                                fillOptions(type, options, sdpDataSource, list);
                                String param = org.apache.commons.lang3.StringUtils.join(list, StrUtils.DLF);
                                StringBuffer sql = new StringBuffer();
                                sql.append(org.apache.commons.lang3.StringUtils.substringBeforeLast(x, StrUtils.LP)).append(StrUtils.LP).append(StrUtils.LF).append(param).append(StrUtils.CR).append(StrUtils.RPS);
                                metaTableSql.append(sql).append(StrUtils.LF);
                            } else {
                                metaTableSql.append(x).append(FlinkSQLConstant.SEPARATOR).append(StrUtils.LF);
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            file.setContent(metaTableSql.append(file.getEtlContent()).toString());
        } else {
            file.setContent(file.getEtlContent());
        }

    }

    private void fillOptions(String type, Map<String, String> options, SdpDataSource sdpDataSource, ArrayList<String> list) {
        for (Map.Entry<String, String> stringStringEntry : options.entrySet()) {
            String value = null;
            String key = stringStringEntry.getKey();
            if ((DataSourceOption.EsOption.HOSTS.getOption().equals(key) && "validate".equals(type) && (DataSourceType.ES.getType().equals(sdpDataSource.getDataSourceType())))
            ) {
                value = StrUtil.subBefore(stringStringEntry.getValue(), StrUtils.SEMICOLON, false);
            } else if (DataSourceOption.KafkaOption.SASL_CONFIG.getOption().equals(key) && "validate".equals(type) && DataSourceType.KAFKA.getType().equals(sdpDataSource.getDataSourceType())
            ) {
                value = StrUtil.subBefore(stringStringEntry.getValue(), StrUtils.SEMICOLON, false);
            } else {
                value = stringStringEntry.getValue();
            }
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append(StrUtils.APOSTROPHE).append(key).append(StrUtils.APOSTROPHE).append(StrUtils.EQUAL_SIGN).append(StrUtils.APOSTROPHE).append(value).append(StrUtils.APOSTROPHE);
            list.add(stringBuffer.toString());
        }
    }

    public String metaAssembleContent(SdpMetaTableConfig metaTableConfig) {
        JobConfigs jobConfigs = new JobConfigs();
        //限制双斜杆的注释使用
        if (metaTableConfig.getFlinkDdl().contains("//")) {
            throw new ApplicationException(ResponseCode.UN_SUPPORT_NOTES);
        }
        String[] statements = SqlUtil.getStatements(metaTableConfig.getFlinkDdl(), FlinkSQLConstant.SEPARATOR);
        JobManager jobManager = JobManager.build(jobConfigs);
        CustomTableEnvironmentImpl managerEnv = jobManager.getEnv();
        StringBuffer metaTableSql = new StringBuffer();
        for (String x : statements) {
            x = SqlUtil.removeNote(x);
            if (StrUtil.isBlank(x)) {
                continue;
            }
            List<Operation> parse = managerEnv.getParser().parse(x);
            Operation operation = parse.get(0);
            if (operation instanceof CreateTableOperation || operation instanceof CreateCatalogOperation) {
                Map<String, String> options = null;
                String tableName = null;
                if (operation instanceof CreateTableOperation) {
                    CreateTableOperation createTableOperation = (CreateTableOperation) operation;
                    options = createTableOperation.getCatalogTable().getOptions();
                    tableName = createTableOperation.getTableIdentifier().getObjectName();
                } else {
                    CreateCatalogOperation createTableOperation = (CreateCatalogOperation) operation;
                    Map<String, String> mOptions = createTableOperation.getProperties();
                    tableName = createTableOperation.getCatalogName();
                    options = Maps.newHashMap();
                    if (Objects.nonNull(mOptions)) {
                        options.putAll(mOptions);
                    }
                }
                SdpDataSource sdpDataSource = dataSourceService.getByIdWithPwdPlaintext(metaTableConfig.getDataSourceId());
                options = dataSourceFactory.getDataSource(sdpDataSource.getDataSourceType()).replaceConnParam(options, sdpDataSource);
                ArrayList<String> list = new ArrayList<>();
                fillOptions("validate", options, sdpDataSource, list);
                String param = org.apache.commons.lang3.StringUtils.join(list, StrUtils.DLF);
                StringBuffer sql = new StringBuffer();
                sql.append(org.apache.commons.lang3.StringUtils.substringBeforeLast(x, StrUtils.LP)).append(StrUtils.LP).append(StrUtils.LF).append(param).append(StrUtils.CR).append(StrUtils.RPS);
                metaTableSql.append(sql).append(StrUtils.LF);
            } else {
                metaTableSql.append(x).append(FlinkSQLConstant.SEPARATOR).append(StrUtils.LF);
            }
        }
        return metaTableSql.toString();
    }

    @Transactional(rollbackFor = Exception.class)
    public void insertLog(SdpJobInstance instance, Context context) {
        SdpOperationLog sdpLog = new SdpOperationLog();
        sdpLog.setAction(JobAction.ONLINE.toString());
        sdpLog.setStatus(LogStatus.SUCCESS.toString());
        sdpLog.setInstanceId(instance.getId());
        if (context != null && org.apache.commons.lang.StringUtils.isNotBlank(context.getUserId())) {
            sdpLog.setCreatedBy(context.getUserId());
        } else {
            sdpLog.setCreatedBy(instance.getCreatedBy());
        }
        sdpLog.setCreationDate(new Timestamp(System.currentTimeMillis()));
        sdpLog.setFromExpectStatus(FileStatus.PASS.toString());
        sdpLog.setToExpectStatus(ExpectStatus.INITIALIZE.toString());
        logMapper.insert(sdpLog);
    }

    @Transactional(rollbackFor = Exception.class)
    public SdpJobInstance insertInstance(SdpJobInstance instance, SdpJob job, Context context) {
        SdpJobInstance jobInstance = new SdpJobInstance();
        BeanUtils.copyProperties(job, jobInstance);
        jobInstance.setId(null);
        if (instance == null) {
            //首次发布上线
            jobInstance.setIsLatest(true);
            jobInstance.setEnabledFlag(1L);
        } else {
            jobInstance.setIsLatest(false);
            jobInstance.setEnabledFlag(1L);
        }
        jobInstance.setJobId(job.getId());
        jobInstance.setJobVersion(job.getLatestVersion());
        jobInstance.setJobStatus(JobStatus.INITIALIZE.toString());
        jobInstance.setRawStatus(RawStatus.INITIALIZE.toString());
        jobInstance.setExpectStatus(ExpectStatus.INITIALIZE.toString());
        if (context != null && org.apache.commons.lang.StringUtils.isNotBlank(context.getUserId())) {
            jobInstance.setCreatedBy(context.getUserId());
            jobInstance.setUpdatedBy(context.getUserId());
        } else {
            jobInstance.setCreatedBy(job.getCreatedBy() + "");
            jobInstance.setUpdatedBy(job.getUpdatedBy() + "");
        }
        jobInstance.setCreationDate(new Timestamp(System.currentTimeMillis()));
        jobInstance.setUpdationDate(new Timestamp(System.currentTimeMillis()));
        instanceMapper.insert(jobInstance);
        return jobInstance;
    }

    public Integer lockFile(SdpFileBO fileBO) {
        SdpFile file = new SdpFile();
        Context context = ContextUtils.get();
        file.setId(fileBO.getId());
        file.setLockedBy(context.getUserId());
        return updateSelective(file);
    }

    //文件版本相关操作
    public Integer insertVersion(SdpVersion version) {
        return versionMapper.insert(version);
    }

    public SdpVersion getVersion(SdpVersion version) {
        return versionMapper.getVersionByFileId(version);
    }

    /**
     * 进行文档读取需要核对锁，同时需要进行线程锁限制
     *
     * @param fileBO
     * @return
     */
    public SdpFileResp getDetailFile(SdpFileBO fileBO) {
        String userId = "0";
        Context context = ContextUtils.get();
        if (context != null && org.apache.commons.lang.StringUtils.isNotBlank(context.getUserId())) {
            userId = context.getUserId();
        }
        fileBO.setUserId(Long.valueOf(userId));
        SdpFileResp detailFile = this.getMapper().getDetailFile(fileBO);
        if (Objects.isNull(detailFile)) {
            return null;
        }
        JobConfig jobConfig = JSONObject.parseObject(detailFile.getConfigContent(), JobConfig.class);
        // 增加历史数据为空兼容处理
        if (StrUtil.isEmpty(jobConfig.getFlinkVersion())) {
            jobConfig.setFlinkVersion(FlinkVersion.VERSION_114.getVersion());
        }
        if (isBase64(jobConfig.getFlinkYaml())) {
            jobConfig.setFlinkYaml(DeflaterUtils.unzipString(jobConfig.getFlinkYaml()));
        }
        detailFile.setConfigContent(JSONObject.toJSONString(jobConfig));
        return detailFile;
    }

    private boolean isBase64(String str) {
        if (Objects.isNull(str)) {
            return false;
        }
        String base64Pattern = "^([A-Za-z0-9+/]{4})*([A-Za-z0-9+/]{4}|[A-Za-z0-9+/]{3}=|[A-Za-z0-9+/]{2}==)$";
        return Pattern.matches(base64Pattern, str);
    }

    public List<EngineResp> getFileEngines(SdpFileBO fileBO) {

        Context context = ContextUtils.get();
        String userId = context.getUserId();
        ArrayList<EngineResp> engineResps = new ArrayList<>();
        List<SdpEngine> engines = sdpProjectEngineMapper.getEngines(fileBO.getProjectId());
        for (SdpEngine engine : engines) {
            EngineResp engineResp = new EngineResp();
            BeanUtils.copyProperties(engine, engineResp);
            if (Objects.nonNull(userId)) {
                List<ProjectUser> mangerUser4Project = sdpUserMapper.getMangerUser4Project(Long.valueOf(userId), fileBO.getProjectId());
                engineResp.setProjectUserRole(mangerUser4Project);
            }
            engineResps.add(engineResp);
        }
        return engineResps;
    }

    public Object checkState(List<Long> fileBO) {
        if (CollectionUtils.isEmpty(fileBO)) {
            return null;
        }
        return this.getMapper().checkState(fileBO);
    }

    public Object fileHistory() {
        String userId = ContextUtils.get().getUserId();
        List<SdpFileResp> fhList = null;
        SdpUser sdpUser = sdpUserMapper.selectById(Long.valueOf(userId));
        if (UserRole.ManagerEnum.COMMON_USER.getCode().equals(sdpUser.getIsAdmin())) {
            fhList = this.getMapper().getFileHistory4Common(userId);
        } else {
            fhList = this.getMapper().getFileHistory(userId);
        }
        return fhList;
    }

    public String getJobDAG(SdpFileBO fileBO) {
        String result = "";
        DataStreamConfig ds = fileBO.getDataStreamConfig();
        if (ds != null) {
            //先查询是否存在DAG
            if (StringUtils.isEmpty(ds.getJarVersion())) {
                throw new ApplicationException(ResponseCode.DAG_PARAMS_MISS, ds.getJarVersion());
            }
            if (StringUtils.isEmpty(ds.getMainClass())) {
                throw new ApplicationException(ResponseCode.DAG_PARAMS_MISS, ds.getMainClass());
            }
            SdpFile sdpFile = this.getMapper().selectById(fileBO.getId());
            String dag = sdpFile.getDag();
            if (!StringUtils.isEmpty(dag)) {
                String version = dag.split("---")[0];
                String mainClass = dag.split("---")[1];
                if (version.equals(ds.getJarVersion()) && mainClass.equals(ds.getMainClass())) {
                    return dag.split("---")[2];
                }
            }
            String mainClass = ds.getMainClass();
            SdpJar sdpJar = jarMapper.selectById(ds.getJarId());
            if (sdpJar != null) {
                String tmpDir = "/tmp/";
                String fullPath = ConfigConst.SDP_HDFS_WORKSPACE_DEFAULT() + "/workspace" + sdpJar.getUrl();
                //兼容历史数据，需要带上name
                if (!fullPath.endsWith(".jar")) {
                    fullPath += "/" + sdpJar.getName();
                }
                String jarPath;
                try {
                    long start = System.currentTimeMillis();
                    jarPath = HadoopUtils.downloadJar(fullPath, tmpDir);
                    log.info("getJobDAG下载jar包完成，耗时===" + (System.currentTimeMillis() - start));
                } catch (IOException e) {
                    log.error("getJobDAG下载jar包异常===" + e);
                    throw new ApplicationException(ResponseCode.JAR_DOWNLOAD_ERROR.getCode(), e.getMessage());
                }
                if (jarPath != null) {
                    String configContent = sdpFile.getConfigContent();
                    JobConfig jobConfig = JSONObject.parseObject(configContent, JobConfig.class);
                    String flinkVersion = null;
                    if (Objects.isNull(jobConfig) || StrUtil.isBlank(jobConfig.getFlinkVersion()) || FlinkVersion.VERSION_114.getVersion().equals(jobConfig.getFlinkVersion())) {
                        flinkVersion = FlinkVersion.VERSION_114.getVersion();
                    } else {
                        flinkVersion = FlinkVersion.VERSION_115.getVersion();
                    }

                    String command = "/opt/apache/flink-" + flinkVersion + "/bin/flink info -c " + mainClass + " " + jarPath;
                    //String command = "/opt/apache/flink-1.14.0/bin/flink info -c " + mainClass + " " + jarPath;
                    command = handleMainParams(configContent, command);
                    log.info("生产DAG图执行命令:\n {}", command);
                    String fileExistsCli = "ls -l " + jarPath;
                    //判断文件是否下载完成，下载完成后才能生成执行计划
                    fileDownloadSuccess(fileExistsCli);
                    StringBuffer buffer = runCommand(command);
                    log.info("====getJobDAG生成DAG结果====\n" + buffer.toString());
                    if (StringUtils.isEmpty(buffer)) {
                        throw new ApplicationException(ResponseCode.DAG_GENERATOR_ERROR);
                    } else {
                        //DAG结果保存入库
                        result = buffer.toString();
                        if (StringUtils.isEmpty(result) && result.contains("nodes")) {
                            SdpFile file = new SdpFile();
                            file.setId(fileBO.getId());
                            file.setDag(ds.getJarVersion() + "---" + ds.getMainClass() + "---" + result);
                            updateSelective(file);
                        }
                    }
                    //删除下载的jar包
                    command = "rm -rf " + jarPath;
                    runCommand(command);
                }
            } else {
                throw new ApplicationException(ResponseCode.DAG_GENERATOR_ERROR.getCode(), "jar文件不存在");
            }
        }
        return result;
    }

    private void fileDownloadSuccess(String fileExistsCli) {
        int times = 60 * 1000;
        String fileExistsResult;
        try {
            long startTime = System.currentTimeMillis();
            do {
                StringBuffer fileExists = runCommand(fileExistsCli);
                fileExistsResult = fileExists.toString();
                Thread.sleep(2000);
            } while (!fileExistsResult.contains("-rw-r--r--") && (System.currentTimeMillis() < startTime + times));
        } catch (Exception e) {
            e.printStackTrace();
            log.error("查询jar包下载状态异常===" + e);
        }
    }

    private String handleMainParams(String configContent, String command) {
        JobConfig jobConfig = JSONObject.parseObject(configContent, JobConfig.class);
        String flinkYaml = jobConfig.getFlinkYaml();
        if (!StringUtils.isEmpty(flinkYaml)) {
            flinkYaml = DeflaterUtils.unzipString(flinkYaml);
            Map<String, String> yamlMap;
            try {
                scala.collection.immutable.Map<String, String> yamlText = PropertiesUtils.fromYamlText(flinkYaml);
                yamlMap = JavaConversions.mapAsJavaMap(yamlText);
            } catch (Exception e) {
                log.error("Flink Yaml解析异常==={}", e);
                throw new ApplicationException(ResponseCode.YAML_CONFIG_ERROR);
            }
            Set entrySet = yamlMap.entrySet().stream().filter(x -> x.getKey().startsWith("main.param")).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(entrySet)) {
                String mainParam = "";
                Iterator<Map.Entry<String, String>> iterator = entrySet.iterator();
                while (iterator.hasNext()) {
                    Map.Entry<String, String> next = iterator.next();
                    String key = next.getKey();
                    String value = next.getValue();
//                    mainParam += "-" + key + " " + JSON.toJSONString(value) + " ";
                    mainParam += "-" + key + " '" + value + "' ";
                }
                command += " " + mainParam;
            }
        }
        return command;
    }

    public StringBuffer runCommand(String command) {
        log.info("====FileService#runCommand CLI命令====\n" + command);
        String[] cmd = new String[]{"sh", "-c", command};
        StringBuffer result = new StringBuffer();
        Process process = null;
        Scanner scanner1 = null;
        Scanner scanner2 = null;
        InputStream inputStream = null;
        InputStream errorStream = null;
        try {
            process = Runtime.getRuntime().exec(cmd);
            inputStream = process.getInputStream();
            errorStream = process.getErrorStream();
            scanner1 = new Scanner(inputStream);
            scanner2 = new Scanner(errorStream);
            while (scanner1.hasNextLine()) {
                String line = scanner1.nextLine();
                if (!line.contains("---") && !line.contains("No description provided.")) {
                    result.append(line).append("\n");
                }
            }
            //此时执行失败，没有获取到执行计划，则返回执行错误信息
            while (scanner2.hasNextLine() && !result.toString().contains("nodes")) {
                String line = scanner2.nextLine();
                result.append(line).append(StrUtils.CRLF);
            }
        } catch (Throwable e) {
            log.error("提交linux command异常===" + e);
            e.printStackTrace();
            throw new ApplicationException(ResponseCode.DAG_GENERATOR_ERROR.getCode(), e.getMessage());
        } finally {
            try {
                if (scanner1 != null) {
                    scanner1.close();
                }
            } catch (Throwable e) {
                log.warn("关闭正常scanner输入流异常", e);
            }
            try {
                if (scanner2 != null) {
                    scanner2.close();
                }
            } catch (Throwable e) {
                log.warn("关闭异常scanner输入流异常", e);
            }
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (Throwable e) {
                log.warn("关闭正常输入流异常", e);
            }
            try {
                if (errorStream != null) {
                    errorStream.close();
                }
            } catch (Throwable e) {
                log.warn("关闭异常输入流异常", e);
            }
            try {
                if (process != null) {
                    process.destroy();
                }
            } catch (Throwable e) {
                log.warn("停止进程异常", e);
                throw new ApplicationException(ResponseCode.DAG_GENERATOR_ERROR.getCode(), e.getMessage());
            }
        }
        return result;
    }

    public ResponseData queryBaseInfo(SdpFileBO fileBO) {
        ResponseData result = new ResponseData();
        SdpFile sdpFile = this.getMapper().queryBaseInfo(fileBO);
        Long folderId = sdpFile.getFolderId();
        //组装作业保存路径
        if (folderId != 0) {
            assembleFilePath(folderId, sdpFile);
            if (!StringUtils.isEmpty(sdpFile.getFilePath())) {
                sdpFile.setFilePath("/作业开发/" + sdpFile.getFilePath());
            } else {
                sdpFile.setFilePath("/作业开发");
            }
        } else {
            //说明在根目录下
            sdpFile.setFilePath("/作业开发");
        }

        SdpJobInstance param = new SdpJobInstance();
        param.setFileId(fileBO.getId());
        List<SdpJobInstance> list = instanceMapper.queryInstance4Sync(param);
        if (CollectionUtils.isNotEmpty(list)) {
            SdpJobInstance instance = list.get(0);
            sdpFile.setJobStatus(instance.getJobStatus());
        }
        result.setData(sdpFile);
        result.ok();
        return result;
    }

    private void assembleFilePath(Long folderId, SdpFile sdpFile) {
        FolderVo folderBO = new FolderVo();
        folderBO.setId(folderId);
        List<FolderVo> list = folderMapper.queryFolder(folderBO);
        if (CollectionUtils.isNotEmpty(list)) {
            FolderVo leaves = list.get(0);
            Long parentId = leaves.getParentId();
            String filePath = sdpFile.getFilePath();
            if (StringUtils.isEmpty(filePath)) {
                sdpFile.setFilePath(leaves.getFolderName());
            } else {
                sdpFile.setFilePath(leaves.getFolderName() + "/" + filePath);
            }
            assembleFilePath(parentId, sdpFile);
        }
    }

    public ResponseData updateBaseInfo(SdpFileBO fileBO) {
        ResponseData result = new ResponseData();
        Context context = ContextUtils.get();
        SdpFileExtra fileExtra = new SdpFileExtra();
        BeanUtils.copyProperties(fileBO, fileExtra);
        if (StringUtils.isEmpty(fileBO.getFileId()) && StringUtils.isEmpty(fileBO.getId())) {
            //两个ID都为空，一定是第一次新增文件，则新增扩展信息
            if (context != null) {
                fileExtra.setCreatedBy(context.getUserId());
                fileExtra.setUpdatedBy(context.getUserId());
            }
            fileExtra.setCreationDate(new Timestamp(System.currentTimeMillis()));
            fileExtra.setUpdationDate(new Timestamp(System.currentTimeMillis()));
            fileExtraMapper.insert(fileExtra);
        } else {
            SdpFileExtra extra = null;
            if (!StringUtils.isEmpty(fileBO.getFileId())) {
                //保存过文件，但没有保存过扩展信息
                extra = fileExtraMapper.queryByFileId(fileBO.getFileId());
            } else if (!StringUtils.isEmpty(fileBO.getId())) {
                //保存过文件，且保存过扩展信息
                extra = fileExtraMapper.selectById(fileBO.getId());
            }
            if (null != extra) {
                if (context != null) {
                    fileExtra.setUpdatedBy(context.getUserId());
                }
                fileExtra.setUpdationDate(new Timestamp(System.currentTimeMillis()));
                fileExtraMapper.updateByFileId(fileExtra);
            } else {
                if (context != null) {
                    fileExtra.setCreatedBy(context.getUserId());
                    fileExtra.setUpdatedBy(context.getUserId());
                }
                fileExtra.setCreationDate(new Timestamp(System.currentTimeMillis()));
                fileExtra.setUpdationDate(new Timestamp(System.currentTimeMillis()));
                fileExtraMapper.insert(fileExtra);
            }
        }
        result.ok();
        return result;
    }

    /**
     * 根据文件ID获取上级目录全路径
     *
     * @param fileId
     * @return
     */
    public String queryFullPath(Long fileId) {
        SdpFileBO fileBO = new SdpFileBO();
        fileBO.setId(fileId);

        SdpFile sdpFile = this.getMapper().queryBaseInfo(fileBO);
        Long folderId = sdpFile.getFolderId();
        //组装作业保存路径
        if (folderId != 0) {
            assembleFilePath(folderId, sdpFile);
            if (!StringUtils.isEmpty(sdpFile.getFilePath())) {
                sdpFile.setFilePath("/作业开发/" + sdpFile.getFilePath());
            } else {
                sdpFile.setFilePath("/作业开发");
            }
        } else {
            //说明在根目录下
            sdpFile.setFilePath("/作业开发");
        }

        return sdpFile.getFilePath();
    }

    public void updateApproveFlag(SdpFile param) {
        this.getMapper().updateApproveFlag(param);
    }

    public String hintMsg(SdpFileBO fileBO) {
        log.info("hintMsg={}", fileBO);
        String hintMsg = "";
        SdpFile detailFile = get(fileBO.getId());
        //只有SDP的数据才需要校验
        if (!detailFile.getBusinessFlag().equals(BusinessFlag.SDP.toString())) {
            return hintMsg;
        }

        // 增加flink版本校验
        hintMsg = jarFlinkVersionCheck(hintMsg, detailFile);

        if (StringUtils.isEmpty(detailFile.getEtlContent())) {
            return hintMsg;
        }
        List<SqlNode> sqlNodes = new ArrayList<>();
        CommonSqlParser commonSqlParser = null;
        try {
            sqlNodes = SqlParserUtil.getSqlNodes(detailFile.getEtlContent());
            commonSqlParser = new CommonSqlParser(detailFile.getMetaTableContent());
        } catch (Exception e) {
            log.warn("parse sql error ,msg=" + e.getMessage());
            return hintMsg;
        }
        if (sqlNodes == null || sqlNodes.size() < 1) {
            return hintMsg;
        }
        //这个要兼容之前hive的catalog写在etl_content字段里面的情况
        boolean etlIncludeCatalog = false;
        for (SqlNode sqlNode : sqlNodes) {
            if (sqlNode instanceof SqlCreateCatalog) {
                SqlCreateCatalog sqlCreateCatalog = (SqlCreateCatalog) sqlNode;
                List<SqlNode> options = sqlCreateCatalog.getPropertyList().getList();
                for (SqlNode option : options) {
                    if (option instanceof SqlTableOption) {
                        if (DataSourceType.HIVE.getType().equalsIgnoreCase(((SqlTableOption) option).getValueString())) {
                            etlIncludeCatalog = true;
                            break;
                        }
                    }
                }
            }

            //这里sqlNode还有可能是catlog和view
            if (sqlNode instanceof RichSqlInsert) {
                RichSqlInsert insertSql = (RichSqlInsert) sqlNode;
                if (StringUtils.isEmpty(insertSql.getTargetTableID().toString())) {
                    continue;
                }
                System.out.println(insertSql.getTargetTableID().toString());
                String env = sdpConfig.getEnvFromEnvHolder(log);
                CheckConfigProperties.CheckConfig configs = checkConfigProperties.getEnvMap().get(env);
                if (configs.getHive().isMSwitch() && etlIncludeCatalog) {
                    //1.etlContent包含hive catalog的情况（非元表配置的方式）
                    hintMsg = checkHiveCheckpointInterval(detailFile.getConfigContent());
                }
                //兼容hive的
                String mFlinkTableName = insertSql.getTargetTableID().toString().split("\\.")[0].replace("`", "");
                List<SdpMetaTableConfig> confogMetaList = configService.selectAll(new SdpMetaTableConfig(fileBO.getId(), mFlinkTableName));
                if (CollectionUtils.isEmpty(confogMetaList)) {
                    continue;
                }
                SdpMetaTableConfig confogMeta = confogMetaList.get(0);
                if (confogMeta == null || confogMeta.getDataSourceId() == null) {
                    continue;
                }
                SdpDataSource sdpDataSource = sdpDataSourceMapper.selectById(confogMeta.getDataSourceId());
                if (sdpDataSource == null || StringUtils.isEmpty(sdpDataSource.getDataSourceUrl())) {
                    continue;
                }
                if (configs.getHive().isMSwitch() && DataSourceType.HIVE.getType().equals(sdpDataSource.getDataSourceType())) {
                    //2.元表配置hive的情况
                    String hiveMsg = checkHiveCheckpointInterval(detailFile.getConfigContent());
                    if (!hintMsg.contains(FlinkConfigKeyConstant.EXECUTION_CHECKPOINTING_INTERVAL) && StrUtil.isNotBlank(hiveMsg)) {
                        hintMsg = StrUtil.isNotBlank(hintMsg) ? hintMsg + ";" + hiveMsg : hiveMsg;
                    }
                }
                if (DataSourceType.HBASE.getType().equals(sdpDataSource.getDataSourceType())) {
                    if (Objects.isNull(commonSqlParser)) {
                        try {
                            //解析会比较慢，所以单独放到使用的地方解析
                            commonSqlParser = new CommonSqlParser(detailFile.getMetaTableContent());
                        } catch (Exception e) {
                            log.warn("解析建表语句异常", e);
                        }
                    }
                    String insertSqlStr = sqlNode.toString();
                    hintMsg = checkSinkHbaseSourceKafkaAndCanalConfig(hintMsg, detailFile, commonSqlParser, insertSqlStr);
                }
            }
        }
        hintMsg = checkSourceKafkaNoPrimaryKey(hintMsg, commonSqlParser);

        hintMsg = checkTopicPartition(hintMsg, commonSqlParser);

        return hintMsg;
    }

    /**
     * 检测是否加上了分区自动发现
     *
     * @param hintMsg
     * @param commonSqlParser
     * @return
     */
    private String checkTopicPartition(String hintMsg, CommonSqlParser commonSqlParser) {
        try {
            for (SqlCreateTable sqlCreateTable : commonSqlParser.getCreateTableList()) {
                if (SqlParserUtil.isSourceKafka(sqlCreateTable)) {
                    Map<String, String> sqlTableOptionMap = SqlParserUtil.getSqlTableOptionMap(sqlCreateTable);
                    String disInt = sqlTableOptionMap.get("scan.topic-partition-discovery.interval");
                    if (StrUtil.isBlank(disInt)) {
                        String warnMsg = "[" + sqlCreateTable.getTableName().toString() + "]元表动态分区发现没有开启,如果新增分区,则不会消费该分区的数据。建议加上该配置: 'scan.topic-partition-discovery.interval' = '10000' ";
                        hintMsg = StrUtil.isNotBlank(hintMsg) ? hintMsg + ";" + warnMsg : warnMsg;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析sql异常1", e);
        }
        return hintMsg;
    }

    private String jarFlinkVersionCheck(String hintMsg, SdpFile detailFile) {

        JobConfig jobConfig = JSONObject.parseObject(detailFile.getConfigContent(), JobConfig.class);
        if (StrUtil.isEmpty(jobConfig.getFlinkVersion())) {
            jobConfig.setFlinkVersion(FlinkVersion.VERSION_114.getVersion());
        }
        String msg = "高级配置选择的flink版本和jar包中pom指定的flink版本不一致,可能会导致任务运行失败,请检查并处理!";
        if (FileType.SQL_STREAM.getType().equals(detailFile.getFileType())) {
            if (jobConfig.getJarId() != null) {
                SdpJar sdpJar = jarService.get(jobConfig.getJarId());
                if (StrUtil.isEmpty(sdpJar.getFlinkVersion())) {
                    log.warn("jarId[{}], flink版本为空", sdpJar.getId());
                    return hintMsg;
                }
                if (sdpJar != null && !sdpJar.getFlinkVersion().equals(jobConfig.getFlinkVersion())) {
                    hintMsg = StrUtil.isNotBlank(hintMsg) ? hintMsg + ";" + msg : msg;
                }
            }
        } else if (FileType.DATA_STREAM.getType().equals(detailFile.getFileType())) {
            DataStreamConfig dsConfig = JSON.parseObject(detailFile.getDataStreamConfig(), DataStreamConfig.class);
            if (dsConfig.getJarId() != null) {
                SdpJar sdpJar = jarService.get(dsConfig.getJarId());
                if (StrUtil.isEmpty(sdpJar.getFlinkVersion())) {
                    log.warn("jarId[{}], flink版本为空", sdpJar.getId());
                    return hintMsg;
                }
                if (sdpJar != null && !sdpJar.getFlinkVersion().equals(jobConfig.getFlinkVersion())) {
                    hintMsg = StrUtil.isNotBlank(hintMsg) ? hintMsg + ";" + msg : msg;
                }
            }
        }

        return hintMsg;
    }

    private String checkSinkHbaseSourceKafkaAndCanalConfig(String hintMsg, SdpFile detailFile, CommonSqlParser commonSqlParser, String insertSqlStr) {
        //满足条件：1.sink hbase，2.source kafka并且格式是canal-json 3.没有配置table.exec.sink.upsert-materialize 或者配置了值不为NONE的
        try {
            boolean isSourceKafkaAndCanal = false;
            for (SqlCreateTable sqlCreateTable : commonSqlParser.getCreateTableList()) {
                String selectFlinkTableName = StrUtils.strWithBackticks(sqlCreateTable.getTableName().toString());
                if (insertSqlStr.contains(selectFlinkTableName) && SqlParserUtil.isSourceKafkaAndCanal(sqlCreateTable)) {
                    isSourceKafkaAndCanal = true;
                    break;
                }
            }

            if (isSourceKafkaAndCanal) {
                JobConfig jobConfig = JSONObject.parseObject(detailFile.getConfigContent(), JobConfig.class);
                Map<String, String> configs = YamlUtils.parseYaml(jobConfig.getFlinkYaml());
                if (!configs.keySet().contains(FlinkConfigKeyConstant.TABLE_EXEC_SINK_UPSERT_MATERIALIZE) || !configs.getOrDefault(FlinkConfigKeyConstant.TABLE_EXEC_SINK_UPSERT_MATERIALIZE, "").trim().equalsIgnoreCase("NONE")) {
                    if (!hintMsg.contains(FlinkConfigKeyConstant.TABLE_EXEC_SINK_UPSERT_MATERIALIZE)) {
                        String msg = "消费CanalJson格式数据写入HBase, 建议高级配置里使用table.exec.sink.upsert-materialize: NONE";
                        hintMsg = StrUtil.isNotBlank(hintMsg) ? hintMsg + ";" + msg : msg;
                    }
                }
            }
        } catch (Exception e) {
            log.error("hbase配置校验异常", e);
        }
        return hintMsg;
    }


    private String checkSourceKafkaNoPrimaryKey(String hintMsg, CommonSqlParser commonSqlParser) {
        try {
            StringBuilder sb = new StringBuilder();
            for (SqlCreateTable sqlCreateTable : commonSqlParser.getCreateTableList()) {
                if (SqlParserUtil.isSourceKafkaAndCanal(sqlCreateTable) && CollectionUtil.isEmpty(sqlCreateTable.getFullConstraints())) {
                    String metaTableName = sqlCreateTable.getTableName().toString();
                    String topic = "";
                    Map<String, String> sqlTableOptionMap = SqlParserUtil.getSqlTableOptionMap(sqlCreateTable);
                    if (sqlTableOptionMap.containsKey(FlinkConfigKeyConstant.TOPIC)) {
                        topic = sqlTableOptionMap.get(FlinkConfigKeyConstant.TOPIC);
                    } else {
                        topic = sqlTableOptionMap.get(FlinkConfigKeyConstant.TOPIC_PATTERN);
                    }
                    sb.append("元表[引用名称: " + metaTableName + ", topic名称: " + topic + "]没有定义primary key\n");
                }
            }
            if (StrUtil.isNotEmpty(sb)) {
                sb.append("平台建议对消费了canal json的kafka元表定义primary key, 避免可能出现delete语句丢失的问题, 并且能提升join性能.");
                hintMsg = StrUtil.isNotBlank(hintMsg) ? hintMsg + ";" + sb : sb.toString();
            }
        } catch (Exception e) {
            log.error("checkSourceKafkaNoPrimaryKey异常", e);
        }
        return hintMsg;
    }


    /**
     * 这个主要是否需要二级审批使用
     * 判断hive catalog是否定义在etl_content里面的情况
     *
     * @param fileBO
     */
    @Transactional(rollbackFor = Exception.class)
    public MetaTableRelationResult handleMetaTableRelation(SdpFileBO fileBO) {
        if (log.isTraceEnabled()) {
            log.trace("handleMetaTableRelation={}", fileBO);
        }

        String etlContent = fileBO.getEtlContent();
        if (FileType.DATA_STREAM.getType().equals(fileBO.getFileType())) {
            return null;
        }

        boolean hiveInEtlContent = false;

        List<SqlNode> sqlNodes = new ArrayList<>();
        CommonSqlParser commonSqlParser = null;
        try {
            etlContent = SqlUtil.removeNotes(etlContent);
            etlContent = reBuildEtlContent(etlContent);
            if (StrUtil.isNotBlank(etlContent) && StrUtil.isNotBlank(etlContent.trim())) {
                sqlNodes = SqlParserUtil.getSqlNodes(etlContent);
            }
            commonSqlParser = new CommonSqlParser(fileBO.getMetaTableContent());
        } catch (Exception e) {
            String xFileId = "null";
            if (Objects.nonNull(fileBO) && Objects.nonNull(fileBO.getId())) {
                xFileId = fileBO.getId() + "";
            }
            log.warn("元表配置->SQL解析异常===" + xFileId, e);
            // throw new ApplicationException(ResponseCode.METATABLE_SQL_PARSE_ERROR);
        }

        if (Objects.isNull(commonSqlParser)) {
            return null;
        }

        MetaTableRelationResult relationResult = new MetaTableRelationResult();
        //elt_content获取元表类型关系
        Map<String, MetaTableType> flinkTableTypeMap = new HashMap<>();

        if (CollectionUtils.isNotEmpty(sqlNodes)) {
            for (SqlNode sqlNode : sqlNodes) {
                if (sqlNode instanceof SqlCreateCatalog) {
                    //这个要兼容之前hive ddl写在etl_content字段里面的情况
                    SqlCreateCatalog sqlCreateCatalog = (SqlCreateCatalog) sqlNode;
                    commonSqlParser.getCreateCatalogList().add(sqlCreateCatalog);

                    List<SqlNode> options = sqlCreateCatalog.getPropertyList().getList();
                    for (SqlNode option : options) {
                        if (option instanceof SqlTableOption) {
                            if (DataSourceType.HIVE.getType().equalsIgnoreCase(((SqlTableOption) option).getValueString())) {
                                hiveInEtlContent = true;
                                break;
                            }
                        }
                    }
                } else if (sqlNode instanceof RichSqlInsert) {
                    RichSqlInsert richSqlInsert = (RichSqlInsert) sqlNode;
                    try {
                        MetaTableTypeFetcher metaTableTypeFetcher = new MetaTableTypeFetcher(richSqlInsert);
                        Map<String, MetaTableType> tableTypeMap = metaTableTypeFetcher.getTableTypeMap();
                        if (!org.springframework.util.CollectionUtils.isEmpty(tableTypeMap)) {
                            flinkTableTypeMap.putAll(tableTypeMap);
                        }
                    } catch (Exception e) {
                        String xFileId = "null";
                        if (Objects.nonNull(fileBO) && Objects.nonNull(fileBO.getId())) {
                            xFileId = fileBO.getId() + "";
                        }
                        log.warn("元表配置->SQL解析异常2===" + xFileId, e);
                    }
                } else if (sqlNode instanceof SqlCreateView) {
                    commonSqlParser.getCreateViewList().add((SqlCreateView) sqlNode);
                } else if (sqlNode instanceof SqlWith) {
                    commonSqlParser.getSqlWithList().add((SqlWith) sqlNode);
                }
            }
        }

        log.info(HANDLE_META_TABLE_RELATION_MSG, fileBO.getId(), "flinkTableTypeMap", JSON.toJSONString(flinkTableTypeMap));

        Context context = ContextUtils.get();
        if (hiveInEtlContent) {
            List<SdpMetaTableRelation> relations = new ArrayList<>();
            //先查出之前元表个数
            List<SdpMetaTableRelation> previous = tableRelationMapper.queryHiveTable(fileBO.getId());
            List<SdpMetaTableConfig> metaTableConfig = mtaTableConfigMapper.queryByFileId(fileBO.getId());
            //先删再插入
            tableRelationMapper.deleteHiveTable(fileBO.getId());
            sqlNodes.forEach(sql -> {
                if (sql instanceof RichSqlInsert) {
                    SdpMetaTableRelation relation = new SdpMetaTableRelation();
                    String sqlNode = ((RichSqlInsert) sql).getTargetTable().toString();
                    //修复不是写hive表报错问题
                    if (isSinkHive(metaTableConfig, sqlNode)) {
                        String[] nodes = sqlNode.split("\\.");
                        if (nodes.length != 3) {
                            throw new ApplicationException(ResponseCode.SQL_GRAMMAR_ERROR, sqlNode);
                        }
                        relation.setMetaTableType(MetaTableType.SINK.getType());
                        relation.setDatabaseName(nodes[1]);
                        relation.setMetaTableName(nodes[2]);
                        relation.setFileId(fileBO.getId());
                        relation.setEnabledFlag(1L);
                        relation.setCreatedBy(context.getUserId());
                        relation.setUpdatedBy(context.getUserId());
                        relation.setCreationDate(new Timestamp(System.currentTimeMillis()));
                        relation.setUpdationDate(new Timestamp(System.currentTimeMillis()));
                        relations.add(relation);
                    }
                }
            });
            if (CollectionUtils.isNotEmpty(relations)) {
                sdpMetaTableRelationService.insertBatch(relations);
                SdpFile sdpFile = get(fileBO.getId());
                if (CollectionUtils.isNotEmpty(previous)) {
                    relations.forEach(x -> x.setMetaTableId(null));
                    boolean need2Approve = true;
                    Set<String> previousSet = previous.stream().map(m ->
                            //哪个数据源哪个数据库哪个表定位是否新增表，有可能存在不同库，表名一样的情况
                            Joiner.on("-").useForNull("").join(m.getDataSourceId(), m.getDatabaseName(), m.getMetaTableName())
                    ).collect(Collectors.toSet());
                    Set<String> relationsSet = relations.stream().map(m ->
                            Joiner.on("-").useForNull("").join(m.getDataSourceId(), m.getDatabaseName(), m.getMetaTableName())
                    ).collect(Collectors.toSet());
                    //Sets.difference(relationsSet,previousSet) 存在relationsSet中，不存在previousSet集合中
                    boolean notEqualCollection = CollectionUtils.isNotEmpty(Sets.difference(relationsSet, previousSet));
                    if (log.isTraceEnabled()) {
                        log.trace("===need2Approve[{}]-FileService:{} - {} --->>> {} - {}", fileBO.getId(), notEqualCollection, sdpFile.getNeed2Approve(), JSON.toJSONString(previousSet), JSON.toJSONString(relationsSet));
                    }
                    if (notEqualCollection) {
                        need2Approve = true;
                    } else {
                        if (sdpFile.getNeed2Approve()) {
                            //元表配置发生了变化，则不管hive是否发生变化，都需要审批
                            need2Approve = true;
                        } else {
                            need2Approve = false;
                        }
                    }
                    //更新标识
                    SdpFile param = new SdpFile();
                    param.setId(fileBO.getId());
                    param.setNeed2Approve(need2Approve);
                    updateApproveFlag(param);
                }
            }
        }

        relationResult.setFileBO(fileBO);
        relationResult.setCommonSqlParser(commonSqlParser);
        relationResult.setFlinkTableTypeMap(flinkTableTypeMap);

        return relationResult;
    }

    /**
     * 元表关系处理（解释sql获取血缘关系）
     *
     * @param relationResult
     */
    @Transactional(rollbackFor = Exception.class)
    public void handleRelation4Metatable(MetaTableRelationResult relationResult) {
        if (Objects.isNull(relationResult) || Objects.isNull(relationResult.getFileBO()) || Objects.isNull(relationResult.getCommonSqlParser()) || Objects.isNull(relationResult.getFlinkTableTypeMap())) {
            log.info(HANDLE_META_TABLE_RELATION_MSG, "参数为空", Objects.nonNull(relationResult) ? JSON.toJSONString(relationResult) : "null", "");
            return;
        }

        SdpFileBO fileBO = relationResult.getFileBO();
        CommonSqlParser commonSqlParser = relationResult.getCommonSqlParser();
        Map<String, MetaTableType> flinkTableTypeMap = relationResult.getFlinkTableTypeMap();

        String flag = fileBO.getBusinessFlag();
        boolean isSdp = StrUtil.isNotBlank(flag) && flag.equals(BusinessFlag.SDP.toString());
        String env = sdpConfig.getEnvFromEnvHolder(log);
        CheckConfigProperties.CheckConfig configs = checkConfigProperties.getEnvMap().get(env);
        log.info(HANDLE_META_TABLE_RELATION_MSG, fileBO.getId(), "getBusinessFlag", Objects.isNull(flag) ? "null" : flag);
        log.info(HANDLE_META_TABLE_RELATION_MSG, fileBO.getId(), "metatablerelationSwitch", configs.getMetatablerelation().isMSwitch());
        log.info(HANDLE_META_TABLE_RELATION_MSG, fileBO.getId(), "isSdp", isSdp);
        if (!configs.getMetatablerelation().isMSwitch() || !isSdp) {
            return;
        }
        Context context = ContextUtils.get();
        //元表关系处理
        SdpMetaTableRelation copyRelation = new SdpMetaTableRelation();
        copyRelation.setFileId(fileBO.getId());
        copyRelation.setEnabledFlag(1L);
        copyRelation.setCreatedBy(context.getUserId());
        copyRelation.setUpdatedBy(context.getUserId());
        copyRelation.setCreationDate(new Timestamp(System.currentTimeMillis()));
        copyRelation.setUpdationDate(new Timestamp(System.currentTimeMillis()));

        tableRelationMapper.delByFileId(fileBO.getId());
        List<SdpMetaTableRelation> relations = new ArrayList<>();
        SdpMetaTableRelation relation = null;
        Map<String, Object> metaTableMap = commonSqlParser.getMetaTable();
        log.info(HANDLE_META_TABLE_RELATION_MSG, fileBO.getId(), "元表集合[" + metaTableMap.size() + "]", org.springframework.util.CollectionUtils.isEmpty(metaTableMap) ? "" : JSON.toJSONString(metaTableMap.keySet()));
        //1.表和catalog
        for (Map.Entry<String, Object> mt : metaTableMap.entrySet()) {
            List<SdpMetaTableConfig> metaTableConfigs = configService.selectAll(new SdpMetaTableConfig(fileBO.getId(), SqlParserUtil.getFlinkTableName(mt.getKey())));
            //存在元表的情况
            if (CollectionUtils.isNotEmpty(metaTableConfigs)) {
                SdpMetaTableConfig sdpMetaTableConfig = metaTableConfigs.get(0);
                SdpDataSource sdpDataSource = dataSourceService.get(sdpMetaTableConfig.getDataSourceId());
                if (mt.getValue() instanceof SqlCreateCatalog) {
                    boolean exist = false;
                    //可能存在多个物理表
                    for (Map.Entry<String, MetaTableType> entry : flinkTableTypeMap.entrySet()) {
                        if (!SqlParserUtil.getFlinkTableName(entry.getKey()).equals(mt.getKey())) {
                            continue;
                        }
                        exist = true;
                        relation = new SdpMetaTableRelation();
                        BeanUtils.copyProperties(copyRelation, relation);
                        relation.setFlinkTableName(mt.getKey());
                        //存在元表信息
                        relation.setMetaTableName(SqlParserUtil.getTableName(entry.getKey()));
                        relation.setMetaTableId(sdpMetaTableConfig.getId());

                        if (Objects.nonNull(sdpDataSource)) {
                            //存在数据源
                            relation.setDataSourceId(sdpDataSource.getId());
                        }
                        relation.setDatabaseName(SqlParserUtil.getDbName(entry.getKey()));
                        relation.setMetaTableType(entry.getValue().getType());

                        relations.add(relation);
                    }
                    if (!exist) {
                        //定义了元表信息，没有写elt_content insert into语句中引用的情况
                        relation = new SdpMetaTableRelation();
                        BeanUtils.copyProperties(copyRelation, relation);
                        relation.setFlinkTableName(mt.getKey());
                        //存在元表信息
                        relation.setMetaTableName(null);
                        relation.setMetaTableId(sdpMetaTableConfig.getId());

                        if (Objects.nonNull(sdpDataSource)) {
                            //存在数据源
                            relation.setDataSourceId(sdpDataSource.getId());
                        }
                        relation.setDatabaseName(null);
                        relation.setMetaTableType(null);

                        relations.add(relation);
                    }
                } else {
                    Map<String, String> sqlTableOptionMap = SqlParserUtil.getSqlTableOptionMap((SqlCreateTable) mt.getValue());

                    relation = new SdpMetaTableRelation();
                    BeanUtils.copyProperties(copyRelation, relation);
                    relation.setFlinkTableName(mt.getKey());
                    //存在元表信息
                    relation.setMetaTableName(sdpMetaTableConfig.getMetaTableName());

                    String topicPattern = sqlTableOptionMap.getOrDefault(FlinkConfigKeyConstant.TOPIC_PATTERN, "");
                    if (sqlTableOptionMap.getOrDefault(FlinkConfigKeyConstant.CONNECTOR, "").equals(DataSourceType.KAFKA.getType()) && StrUtil.isNotBlank(topicPattern)) {
                        relation.setMetaTableName(topicPattern);
                    }
                    relation.setMetaTableId(sdpMetaTableConfig.getId());

                    if (Objects.nonNull(sdpDataSource)) {
                        //存在数据源
                        relation.setDatabaseName(sdpDataSource.getDatabaseName());
                        relation.setDataSourceId(sdpDataSource.getId());
                    }
                    String type = Optional.ofNullable(flinkTableTypeMap).map(m -> m.get(mt.getKey())).map(m -> m.getType()).orElse(null);
                    relation.setMetaTableType(type);

                    relations.add(relation);
                }

            } else {
                //不存在元表的情况：目前就是hive定义语句在etl_content的情况
                if (mt.getValue() instanceof SqlCreateCatalog) {
                    boolean exist = false;
                    //可能存在多个物理表
                    for (Map.Entry<String, MetaTableType> entry : flinkTableTypeMap.entrySet()) {
                        if (!SqlParserUtil.getFlinkTableName(entry.getKey()).equals(mt.getKey())) {
                            continue;
                        }
                        exist = true;
                        relation = new SdpMetaTableRelation();
                        BeanUtils.copyProperties(copyRelation, relation);
                        relation.setFlinkTableName(mt.getKey());
                        //存在元表信息
                        relation.setMetaTableName(SqlParserUtil.getTableName(entry.getKey()));
                        relation.setMetaTableId(null);
                        relation.setDataSourceId(null);
                        relation.setDatabaseName(SqlParserUtil.getDbName(entry.getKey()));
                        relation.setMetaTableType(entry.getValue().getType());

                        relations.add(relation);
                    }
                    if (!exist) {
                        //定义了元表信息，没有写elt_content insert into语句中引用的情况
                        relation = new SdpMetaTableRelation();
                        BeanUtils.copyProperties(copyRelation, relation);
                        relation.setFlinkTableName(mt.getKey());
                        //存在元表信息
                        relation.setMetaTableName(null);
                        relation.setMetaTableId(null);
                        relation.setDataSourceId(null);

                        relation.setDatabaseName(null);
                        relation.setMetaTableType(null);

                        relations.add(relation);
                    }
                } else {
                    log.info(HANDLE_META_TABLE_RELATION_MSG, fileBO.getId(), "不明定义语句", mt.getValue().toString());
                }
            }
        }

        Map<String, SdpMetaTableRelation> existMap = relations.stream().collect(Collectors.toMap(SdpMetaTableRelation::getPriKey, m -> m, (k1, k2) -> k2));
        //2.处理视图存在的物理表
        List<SqlCreateView> createViewList = commonSqlParser.getCreateViewList();
        List<SqlWith> sqlWithList = commonSqlParser.getSqlWithList();
        log.info(HANDLE_META_TABLE_RELATION_MSG, fileBO.getId(), "VIEW视图集合[" + createViewList.size() + "]", "WITH视图集合[" + sqlWithList.size() + "]");
        if (CollectionUtil.isNotEmpty(createViewList) || CollectionUtil.isNotEmpty(sqlWithList)) {
            ViewTableMapFetcher viewTableMapFetcher = new ViewTableMapFetcher(createViewList, sqlWithList);
            List<String> views = viewTableMapFetcher.getViewNames();
            for (String view : views) {
                MetaTableType metaTableType = flinkTableTypeMap.get(view);
                String type = Optional.ofNullable(metaTableType).map(m -> m.getType()).orElse(null);
                List<String> flinkTableList = viewTableMapFetcher.getFlinkTableList(view, new ArrayList<>());
                log.info(HANDLE_META_TABLE_RELATION_MSG, fileBO.getId(), "获取" + view + "视图的物理表", JSON.toJSONString(flinkTableList));
                for (String flinkTable : flinkTableList) {
                    List<SdpMetaTableConfig> metaTableConfigs = configService.selectAll(new SdpMetaTableConfig(fileBO.getId(), SqlParserUtil.getFlinkTableName(flinkTable)));
                    //存在元表的情况
                    if (CollectionUtils.isNotEmpty(metaTableConfigs)) {
                        SdpMetaTableConfig sdpMetaTableConfig = metaTableConfigs.get(0);
                        SdpDataSource sdpDataSource = dataSourceService.get(sdpMetaTableConfig.getDataSourceId());
                        relation = new SdpMetaTableRelation();
                        BeanUtils.copyProperties(copyRelation, relation);
                        relation.setFlinkTableName(SqlParserUtil.getFlinkTableName(flinkTable));
                        //存在元表信息
                        relation.setMetaTableName(SqlParserUtil.getTableName(flinkTable));
                        if (StrUtil.isNotBlank(sdpMetaTableConfig.getMetaTableName())) {
                            relation.setMetaTableName(sdpMetaTableConfig.getMetaTableName());
                        }

                        relation.setMetaTableId(sdpMetaTableConfig.getId());

                        relation.setDatabaseName(SqlParserUtil.getDbName(flinkTable));
                        if (Objects.nonNull(sdpDataSource)) {
                            //存在数据源
                            relation.setDataSourceId(sdpDataSource.getId());
                            if (StrUtil.isNotBlank(sdpDataSource.getDatabaseName())) {
                                relation.setDatabaseName(sdpDataSource.getDatabaseName());
                            }
                        }

                        relation.setMetaTableType(type);

                        //以table和catalog的为准，如果已经存在更新一下metaTableType，不存在则添加进来
                        if (existMap.keySet().contains(relation.getPriKey())) {
                            SdpMetaTableRelation sdpMetaTableRelation = existMap.get(relation.getPriKey());
                            if (Objects.nonNull(sdpMetaTableRelation) && StrUtil.isNotBlank(type)) {
                                sdpMetaTableRelation.setMetaTableType(type);
                            }
                        } else {
                            relations.add(relation);
                            existMap.put(relation.getPriKey(), relation);
                        }
                    } else {
                        relation = new SdpMetaTableRelation();
                        BeanUtils.copyProperties(copyRelation, relation);
                        relation.setFlinkTableName(SqlParserUtil.getFlinkTableName(flinkTable));
                        //存在元表信息
                        relation.setMetaTableName(SqlParserUtil.getTableName(flinkTable));
                        relation.setMetaTableId(null);
                        relation.setDataSourceId(null);
                        relation.setDatabaseName(SqlParserUtil.getDbName(flinkTable));
                        relation.setMetaTableType(type);

                        //以table和catalog的为准，如果已经存在更新一下metaTableType，不存在则添加进来
                        if (existMap.keySet().contains(relation.getPriKey())) {
                            SdpMetaTableRelation sdpMetaTableRelation = existMap.get(relation.getPriKey());
                            if (Objects.nonNull(sdpMetaTableRelation) && StrUtil.isNotBlank(type)) {
                                sdpMetaTableRelation.setMetaTableType(type);
                            }
                        } else {
                            relations.add(relation);
                            existMap.put(relation.getPriKey(), relation);
                        }
                    }
                }
            }
        }

        if (CollectionUtils.isNotEmpty(relations)) {
            log.info(HANDLE_META_TABLE_RELATION_MSG, fileBO.getId(), "元表集合[" + relations.size() + "]", JSON.toJSONString(relations));
            sdpMetaTableRelationService.insertBatch(relations);
        }
    }

    /**
     * 处理元表关系历史数据
     */
    @Deprecated
    public void handleHistoryData4MetaTableRelation() {
        Integer pageSize = 100;
        Integer count = getMapper().queryCountByMinId(Long.MAX_VALUE);
        int totalPage = PageUtil.totalPage(count, pageSize);
        Long minId = Long.MAX_VALUE;
        for (int i = 0; i < totalPage; i++) {
            List<SdpFile> sdpFiles = getMapper().queryByMinId(minId, pageSize);
            if (CollectionUtils.isEmpty(sdpFiles)) {
                break;
            }
            for (SdpFile sdpFile : sdpFiles) {
                try {
                    SdpFileBO sdpFileBO = new SdpFileBO();
                    BeanUtils.copyProperties(sdpFile, sdpFileBO);
                    sdpFileBO.setId(sdpFile.getId());
                    MetaTableRelationResult relationResult = handleMetaTableRelation(sdpFileBO);
                    try {
                        handleRelation4Metatable(relationResult);
                    } catch (Exception e) {
                        String errMsg = StrUtils.parse1(HANDLE_META_TABLE_RELATION_MSG, sdpFile.getId(), "元表关系处理异常");
                        log.error(errMsg, e);
                    }

                } catch (Exception e) {
                    String errMsg = StrUtils.parse1(HANDLE_META_TABLE_RELATION_MSG, sdpFile.getId(), "处理元表关系历史数据异常");
                    log.error(errMsg, e);
                }
            }
            minId = sdpFiles.get(sdpFiles.size() - 1).getId();
        }


    }

    /**
     * @author zouchangzhen
     * @date 2022/6/24
     */
    @Data
    public static class MetaTableRelationResult {
        SdpFileBO fileBO;
        CommonSqlParser commonSqlParser;
        Map<String, MetaTableType> flinkTableTypeMap;
    }


    public Map<String, Boolean> fileExist(SdpFileBO fileBO) {
        if (Objects.isNull(fileBO) || CollectionUtil.isEmpty(fileBO.getFileNames())) {
            return Collections.EMPTY_MAP;
        }

        List<SdpFile> sdpFiles = getMapper().selectByFileNames(fileBO.getProjectId(), fileBO.getFileNames());
        Map<String, SdpFile> sdpFileMap = Optional.ofNullable(sdpFiles).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(SdpFile::getFileName, m -> m, (k1, k2) -> k1));

        Map<String, Boolean> fileExistMap = new LinkedHashMap<>();
        for (String fileName : fileBO.getFileNames()) {
            fileExistMap.put(fileName, Objects.nonNull(sdpFileMap.get(fileName)));
        }
        return fileExistMap;
    }

    /**
     * 是否允许新增作业
     *
     * @param projectId
     * @return
     */
    public Boolean allowAddJob(Long projectId) {
        String env = sdpConfig.getEnvFromEnvHolder(log);
        if (EnvironmentEnum.PROD.getCode().equalsIgnoreCase(env)) {
            //生产环境
            SdpProject sdpProject = projectService.get(projectId);
            Integer allowJobAddEdit = sdpProject.getAllowJobAddEdit();
            return CommonConstant.ONE_FOR_INT.equals(allowJobAddEdit);
        } else {
            return true;
        }
    }

    /**
     * 是否允许编辑作业
     *
     * @param
     * @return
     */
    public FileEditVO allowEditJob(SdpFileBO fileBO) {
        FileEditVO fileEditVO = new FileEditVO();
        String env = sdpConfig.getEnvFromEnvHolder(log);
        if (EnvironmentEnum.PROD.getCode().equalsIgnoreCase(env)) {
            SdpFile sdpFile = fileService.get(fileBO.getId());
            if (Objects.isNull(sdpFile)) {
                log.info("生产环境不存在该文件【{}】", fileBO.getId());
                throw new ApplicationException(ResponseCode.COMMON_ERR, "文件不存在");
            }
            String dateStr = DateUtil.format(sdpFile.getCreationDate(), DateUtils.YYYYMMDDHHMM);
            Long dateInt = Long.valueOf(dateStr);

            if (dateInt > checkCreateTime) {
                //新增任务
                SdpProject sdpProject = projectService.get(fileBO.getProjectId());
                Integer allowJobAddEdit = sdpProject.getAllowJobAddEdit();
                fileEditVO.setAllowEdit(CommonConstant.ONE_FOR_INT.equals(allowJobAddEdit));
                fileEditVO.setHistoryFile(false);
                return fileEditVO;
            } else {
                //存量任务
                fileEditVO.setAllowEdit(true);
                fileEditVO.setHistoryFile(true);
                return fileEditVO;
            }
        } else {
            fileEditVO.setAllowEdit(true);
            fileEditVO.setHistoryFile(false);
            return fileEditVO;
        }
    }
}
