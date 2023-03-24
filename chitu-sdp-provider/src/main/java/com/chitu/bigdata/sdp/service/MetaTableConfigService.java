

/**
 * <pre>
 * 作   者：CHENYUN
 * 创建日期：2021-12-10
 * </pre>
 */

package com.chitu.bigdata.sdp.service;

import com.alibaba.fastjson.JSON;
import com.chitu.bigdata.sdp.api.bo.SdpMetaTableConfigBO;
import com.chitu.bigdata.sdp.api.domain.ConnectInfo;
import com.chitu.bigdata.sdp.api.domain.FlinkTableGenerate;
import com.chitu.bigdata.sdp.api.domain.MetaTableConfigBatchDomain;
import com.chitu.bigdata.sdp.api.domain.MetadataTableColumn;
import com.chitu.bigdata.sdp.api.enums.CertifyType;
import com.chitu.bigdata.sdp.api.enums.DataSourceType;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.*;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.interceptor.EnvHolder;
import com.chitu.bigdata.sdp.mapper.SdpDataSourceMapper;
import com.chitu.bigdata.sdp.mapper.SdpMetaTableConfigMapper;
import com.chitu.bigdata.sdp.mapper.SdpMetaTableRelationMapper;
import com.chitu.bigdata.sdp.service.datasource.DataSourceFactory;
import com.chitu.bigdata.sdp.service.datasource.KafkaDataSource;
import com.chitu.bigdata.sdp.service.validate.constant.FlinkSQLConstant;
import com.chitu.bigdata.sdp.service.validate.custom.CustomTableEnvironmentImpl;
import com.chitu.bigdata.sdp.service.validate.ddl.MetaTableConfigCheckFactory;
import com.chitu.bigdata.sdp.service.validate.domain.JobConfigs;
import com.chitu.bigdata.sdp.service.validate.domain.SqlExplainResult;
import com.chitu.bigdata.sdp.service.validate.job.JobManager;
import com.chitu.bigdata.sdp.service.validate.util.SqlUtil;
import com.chitu.bigdata.sdp.utils.Assert;
import com.chitu.bigdata.sdp.utils.SqlParserUtil;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.model.ResponseData;
import com.chitu.cloud.service.GenericService;
import com.chitu.cloud.utils.SpringUtils;
import com.google.common.base.Joiner;
import com.google.common.collect.Sets;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.SqlNode;
import org.apache.calcite.sql.SqlNodeList;
import org.apache.calcite.sql.parser.SqlParseException;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.sql.parser.ddl.SqlCreateTable;
import org.apache.flink.sql.parser.ddl.SqlTableOption;
import org.apache.flink.table.operations.Operation;
import org.apache.flink.table.operations.ddl.CreateTableOperation;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

/**
 * <pre>
 * 业务类
 * </pre>
 */
@Service
@Slf4j
public class MetaTableConfigService extends GenericService<SdpMetaTableConfig, Long> {
    public MetaTableConfigService(@Autowired SdpMetaTableConfigMapper sdpMetaTableConfigMapper) {
        super(sdpMetaTableConfigMapper);
    }

    public SdpMetaTableConfigMapper getMapper() {
        return (SdpMetaTableConfigMapper) super.genericMapper;
    }


    @Autowired
    DataSourceFactory dataSourceFactory;

    @Autowired
    DataSourceService dataSourceService;

    @Autowired
    FileService fileService;

    @Autowired
    ProjectService projectService;

    @Autowired
    KafkaDataSource kafkaDataSource;
    @Autowired
    private SdpMetaTableRelationMapper tableRelationMapper;
    @Autowired
    MetaTableConfigCheckFactory metaTableConfigCheckFactory;
    @Autowired
    SdpDataSourceMapper sdpDataSourceMapper;

    @Autowired
    SdpConfig sdpConfig;
    @Autowired
    SdpMetaTableRelationService sdpMetaTableRelationService;

    private static final String GROUP_ID = "properties.group.id";

    @Transactional(rollbackFor = Exception.class)
    public ResponseData addMetaTableConfig(List<SdpMetaTableConfig> metaTableConfigList) {
        ResponseData responseData = new ResponseData<>();

        // 1.文件锁定校验
        fileService.fileLockCheck(metaTableConfigList.get(0).getFileId());

        // 2.校验元表名不能相同
        List<String> distinctTableList = metaTableConfigList.stream().map(SdpMetaTableConfig::getFlinkTableName).distinct().collect(Collectors.toList());
        Assert.isTrue(distinctTableList.size() == metaTableConfigList.size(), ResponseCode.METATABLE_NOT_REPETITION);

        //3. 不同的数据源，可以有不一样的校验规则
        ResponseData checkResp = metaTableConfigCheckFactory.checkEachItem(metaTableConfigList);
        if(0 != checkResp.getCode()){
            return checkResp;
        }

        //获取数据源信息
        List<Long> dataSourceIds = metaTableConfigList.stream().map(m -> m.getDataSourceId()).collect(Collectors.toList());
        List<SdpDataSource> dataSources = dataSourceService.getByIds(dataSourceIds.toArray(new Long[dataSourceIds.size()]));
        Map<Long, SdpDataSource> dataSourceMap = Optional.ofNullable(dataSources).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(SdpDataSource::getId, m -> m, (k1, k2) -> k2));

        // 4.校验sql语法
        AtomicBoolean sqlValidate = new AtomicBoolean(true);
        ArrayList<Map> validateErrorList = new ArrayList<>();
        Map<String, String> validateErrorMap = new HashMap<>(3);
        metaTableConfigList.forEach(item -> {
            SdpDataSource sdpDataSource = dataSourceMap.get(item.getDataSourceId());
            String content;
//            if (!DataSourceType.DORIS.getType().equals(sdpDataSource.getDataSourceType())) {
                content = fileService.metaAssembleContent(item);
//            } else {
//                content = item.getFlinkDdl();
//            }
            List<SqlExplainResult> sqlExplainResults = fileService.executeValidate(content);
            if (CollectionUtils.isNotEmpty(sqlExplainResults)) {
                validateErrorMap.put("flinkTableName", item.getFlinkTableName());
                validateErrorMap.put("errorMsg", sqlExplainResults.get(0).getError());
                validateErrorList.add(validateErrorMap);
                sqlValidate.set(false);
            }
        });

        if (!sqlValidate.get()) {
            responseData.setCode(ResponseCode.SQL_NOT_PASS.getCode());
            responseData.setMsg(ResponseCode.SQL_NOT_PASS.getMessage());
            responseData.setData(validateErrorList);
            return responseData;
        }
        // 4.检验需要ASAL认证的kafka权限
        checkKafkaAsal(metaTableConfigList, responseData, validateErrorList, validateErrorMap);

        // 5.更新数据(先删除再新增)
        List<SdpMetaTableConfig> queryMetaTableConfigList = this.selectAll(new SdpMetaTableConfig(metaTableConfigList.get(0).getFileId()));
        if (CollectionUtil.isNotEmpty(queryMetaTableConfigList)) {
            Long[] ids = queryMetaTableConfigList.stream().map(SdpMetaTableConfig::getId).toArray(Long[]::new);
            this.disable(SdpMetaTableConfig.class,ids);
        }

        metaTableConfigList.forEach(item -> {
            item.setId(null);
            //移除空行
            String newDdl = item.getFlinkDdl();
//            if(StringUtils.isNotEmpty(item.getFlinkDdl())){
//                newDdl = item.getFlinkDdl().replaceAll("(?m)^\\s*$"+System.lineSeparator(), "").replaceAll("(?m)^\\s*$"+ StrUtils.LF, "").trim();
//            }
            item.setFlinkDdl(replaceOptionValue(newDdl));
        });
        this.insertBatch(metaTableConfigList);

        //插入元表关系表
        insertTableRelations(metaTableConfigList);

        // 返回data为拼接后完整flink ddl
        String[] flinkDdlArray = metaTableConfigList.stream().map(SdpMetaTableConfig::getFlinkDdl).toArray(String[]::new);
        String joinFlinkDdl = StrUtil.join(StrUtil.LF, flinkDdlArray);
        log.info("保存元表，最新flink dll：\n{}", joinFlinkDdl);
        if (ResponseCode.KAFKA_PERMISSION_FILED.getCode() != responseData.getCode()) {
            responseData.setData(joinFlinkDdl).ok();
        } else {
            responseData.setData(joinFlinkDdl);
        }
        return responseData;
    }

    @Transactional(rollbackFor = Exception.class)
    public void insertTableRelations(List<SdpMetaTableConfig> metaTableList) {
        Long fileId = metaTableList.get(0).getFileId();
        //先查出之前元表个数
        List<SdpMetaTableRelation> previous = tableRelationMapper.queryByFileId(fileId);
        //先删除再插入
        tableRelationMapper.deleteByFileId(fileId);

        List<SdpMetaTableRelation> relations = new ArrayList<>();
        metaTableList.forEach(meta->{
            SdpMetaTableRelation relation = new SdpMetaTableRelation();
            BeanUtils.copyProperties(meta,relation);
            SdpDataSource dataSource = dataSourceService.get(meta.getDataSourceId());
            relation.setDatabaseName(dataSource.getDatabaseName());
            relation.setMetaTableId(meta.getId());
            relation.setEnabledFlag(1L);
            relation.setId(null);
            relations.add(relation);
        });
        if(CollectionUtils.isNotEmpty(relations)){
            sdpMetaTableRelationService.insertBatch(relations);
            SdpFile sdpFile = fileService.get(fileId);
            if(CollectionUtils.isNotEmpty(previous)){
                relations.forEach(x->x.setMetaTableId(null));
                boolean need2Approve = true;
                Set<String> previousSet =  previous.stream().map(m->
                        //哪个数据源哪个数据库哪个表定位是否新增表，有可能存在不同库，表名一样的情况
                        Joiner.on("-").useForNull("").join(m.getDataSourceId(),m.getDatabaseName(),m.getMetaTableName())
                ).collect(Collectors.toSet());
                Set<String> relationsSet = relations.stream().map(m->
                        Joiner.on("-").useForNull("").join(m.getDataSourceId(),m.getDatabaseName(),m.getMetaTableName())
                ).collect(Collectors.toSet());
                //Sets.difference(relationsSet,previousSet) 存在relationsSet中，不存在previousSet集合中
                boolean notEqualCollection = CollectionUtils.isNotEmpty(Sets.difference(relationsSet,previousSet));
                if(log.isTraceEnabled()){
                    log.trace("===need2Approve[{}]-MetaTableConfigService:{} - {} --->>> {} - {}",fileId,notEqualCollection,sdpFile.getNeed2Approve(), JSON.toJSONString(previousSet),JSON.toJSONString(relationsSet));
                }
                if(notEqualCollection){
                    need2Approve = true;
                }else{
                    if(sdpFile.getNeed2Approve()){
                        //hive表发生了变化，则不管元表配置是否发生变化，都需要审批
                        need2Approve = true;
                    }else{
                        need2Approve = false;
                    }
                }
                //更新标识
                SdpFile param = new SdpFile();
                param.setId(fileId);
                param.setNeed2Approve(need2Approve);
                fileService.updateApproveFlag(param);
            }
        }
    }

    private String replaceOptionValue(String sql) {
        List<SqlNode> sqlNodes = null;
        try {
            sqlNodes = SqlParserUtil.getSqlNodes(sql);
        } catch (SqlParseException e) {
            log.error("sql解析失败", e);
            return sql;
        }
        for (SqlNode sqlNode : sqlNodes) {
            if (sqlNode instanceof SqlCreateTable) {
                SqlCreateTable sqlCreateTable = (SqlCreateTable) sqlNode;
                SqlNodeList propertyList = sqlCreateTable.getPropertyList();
                List<SqlNode> sqlTableOptionList = propertyList.getList();
                for (SqlNode item : sqlTableOptionList) {
                    SqlTableOption tableOption = (SqlTableOption) item;
                    if ("format".equals(tableOption.getKeyString())) {
                        if ("json".equals(tableOption.getValueString())) {
                            sql = sql.replace("'json.ignore-parse-errors' = 'true'", "'json.ignore-parse-errors' = 'false'");
                        } else if ("canal-json".equals(tableOption.getValueString())) {
                            sql = sql.replace("'canal-json.ignore-parse-errors' = 'true'", "'canal-json.ignore-parse-errors' = 'false'");
                        }
                        break;
                    }
                }
            }
        }
        return sql;
    }


    private void checkKafkaAsal(List<SdpMetaTableConfig> metaTableConfigList, ResponseData responseData, ArrayList<Map> validateErrorList, Map<String, String> validateErrorMap) {
        metaTableConfigList.forEach(x -> {
            SdpDataSource sdpDataSource = dataSourceService.getByIdWithPwdPlaintext(x.getDataSourceId());
            if (CertifyType.SASL.getType().equals(sdpDataSource.getCertifyType()) && DataSourceType.KAFKA.getType().equals(sdpDataSource.getDataSourceType())) {
                if (StrUtil.isNotBlank(x.getFlinkDdl())) {
                    String flinkDdl = x.getFlinkDdl();
                    JobConfigs jobConfigs = new JobConfigs();
                    JobManager jobManager = JobManager.build(jobConfigs);
                    CustomTableEnvironmentImpl managerEnv = jobManager.getEnv();
                    flinkDdl = SqlUtil.removeNote(flinkDdl);
                    String[] statements = SqlUtil.getStatements(flinkDdl, FlinkSQLConstant.SEPARATOR);
                    List<Operation> parse = managerEnv.getParser().parse(statements[0]);
                    CreateTableOperation createTableOperation = (CreateTableOperation) parse.get(0);
                    Map<String, String> options = createTableOperation.getCatalogTable().getOptions();
                    options = kafkaDataSource.replaceConnParam(options, sdpDataSource);
                    if (options.containsKey(GROUP_ID)) {
                        String groupId = options.get(GROUP_ID);
                        ConnectInfo connectInfo = new ConnectInfo();
                        connectInfo.setAddress(sdpDataSource.getDataSourceUrl());
                        connectInfo.setUsername(sdpDataSource.getUserName());
                        connectInfo.setPwd(sdpDataSource.getPassword());
                        connectInfo.setCertifyType(sdpDataSource.getCertifyType());
                        String errorMsg = kafkaDataSource.checkAsalConnect(connectInfo, x.getMetaTableName(), groupId);
                        if (StrUtil.isNotBlank(errorMsg)) {
                            validateErrorMap.put("checkConnect", errorMsg);
                            validateErrorList.add(validateErrorMap);
                        }
                    }
                }
            }
            if (CollectionUtils.isNotEmpty(validateErrorList)) {
                responseData.setCode(ResponseCode.KAFKA_PERMISSION_FILED.getCode());
                responseData.setMsg(ResponseCode.KAFKA_PERMISSION_FILED.getMessage());
            }
        });
    }

    public ResponseData queryMetaTableConfig(SdpMetaTableConfigBO sdpMetaTableConfigBO) {
        //根据文件id查询
        List<SdpMetaTableConfig> queryMetaTableConfigList = this.selectAll(new SdpMetaTableConfig(sdpMetaTableConfigBO.getFileId()));
        ResponseData responseData = new ResponseData<>();
        responseData.setData(queryMetaTableConfigList).ok();
        return responseData;
    }

    public ResponseData tableExistVerify(SdpMetaTableConfigBO sdpMetaTableConfigBO) {
        //指定环境请求
        String env = sdpMetaTableConfigBO.getEnv();
        if(StrUtil.isNotBlank(env)){
            String headEnv = sdpConfig.getEnvFromEnvHolder(null);
            try {
                 EnvHolder.clearEnvAndAddEnv(env);
                 return tableExist(sdpMetaTableConfigBO);
            }finally {
                //重置回去
                EnvHolder.clearEnvAndAddEnv(headEnv);
            }
        }else {
            return tableExist(sdpMetaTableConfigBO);
        }
    }

    public ResponseData tableExist(SdpMetaTableConfigBO sdpMetaTableConfigBO) {
        ResponseData responseData = new ResponseData<>();
        boolean tableExist = false;
        SdpDataSource sdpDataSource = dataSourceService.getByIdWithPwdPlaintext(sdpMetaTableConfigBO.getDataSourceId());
        ConnectInfo connectInfo = new ConnectInfo();
        connectInfo.setAddress(sdpDataSource.getDataSourceUrl());
        connectInfo.setCertifyType(sdpDataSource.getCertifyType());
        connectInfo.setDatabaseName(sdpDataSource.getDatabaseName());
        connectInfo.setUsername(sdpDataSource.getUserName());
        connectInfo.setPwd(sdpDataSource.getPassword());
        connectInfo.setCertifyType(sdpDataSource.getCertifyType());
        connectInfo.setHbaseZnode(sdpDataSource.getHbaseZnode());
        log.info("验证元表是否存在 _ConnectInfo_: {}",JSON.toJSONString(connectInfo));
        try {
            tableExist = dataSourceFactory.getDataSource(sdpDataSource.getDataSourceType()).tableExists(connectInfo, sdpMetaTableConfigBO.getMetaTableName());
        } catch (Exception e) {
            log.error("获取数据源表是否存在，出现异常", e);
            throw new ApplicationException(ResponseCode.ERROR.getCode(), "获取数据源表是否存在，出现异常！");
        }
        responseData.setData(tableExist).ok();
        return responseData;
    }

    public ResponseData generateDdl(SdpMetaTableConfigBO sdpMetaTableConfigBO) {
        //获取数据源信息
        SdpDataSource sdpDataSource = dataSourceService.getByIdWithPwdPlaintext(sdpMetaTableConfigBO.getDataSourceId());
        //非hive和非tidb需要校验元表名称和元表类型是否为空
        if(!DataSourceType.HIVE.getType().equals(sdpDataSource.getDataSourceType())
                &&  !DataSourceType.DATAGEN.getType().equals(sdpDataSource.getDataSourceType())
                &&  !DataSourceType.PRINT.getType().equals(sdpDataSource.getDataSourceType())
        ){
            if(StrUtil.isBlank(sdpMetaTableConfigBO.getMetaTableName())){
               throw new ApplicationException(ResponseCode.COMMON_ERR,"元表名称不能为空");
            }
            if(StrUtil.isBlank(sdpMetaTableConfigBO.getMetaTableType())){
                throw new ApplicationException(ResponseCode.COMMON_ERR,"元表类型不能为空");
            }
        }

        //获取项目信息
        SdpFile sdpFile = fileService.get(sdpMetaTableConfigBO.getFileId());
        SdpProject sdpProject = projectService.get(sdpFile.getProjectId());

        ConnectInfo connectInfo = new ConnectInfo();
        connectInfo.setAddress(sdpDataSource.getDataSourceUrl());
        connectInfo.setDatabaseName(sdpDataSource.getDatabaseName());
        connectInfo.setUsername(sdpDataSource.getUserName());
        connectInfo.setPwd(sdpDataSource.getPassword());
        connectInfo.setCertifyType(sdpDataSource.getCertifyType());
        connectInfo.setHbaseZnode(sdpDataSource.getHbaseZnode());
        log.info("获取字段 _ConnectInfo_: {}", JSON.toJSONString(connectInfo));
        List<MetadataTableColumn> metadataTableColumnList = new ArrayList<>();
        try {
            //获取物理表字段信息
            metadataTableColumnList = dataSourceFactory.getDataSource(sdpDataSource.getDataSourceType()).getTableColumns(connectInfo, sdpMetaTableConfigBO.getMetaTableName());
        } catch (Exception e) {
            log.error("获取数据源表字段异常", e);
            //不抛出去，避免无法生成ddl
            //throw new ApplicationException(ResponseCode.ERROR.getCode(), "获取数据源表字段异常！");
        }
        FlinkTableGenerate flinkTableGenerate = new FlinkTableGenerate();
        flinkTableGenerate.setProjectCode(sdpProject.getProjectCode());
        flinkTableGenerate.setJobName(sdpFile.getFileName());
        flinkTableGenerate.setFlinkTableName(sdpMetaTableConfigBO.getFlinkTableName());
        flinkTableGenerate.setSourceTableName(sdpMetaTableConfigBO.getMetaTableName());
        flinkTableGenerate.setAddress(sdpDataSource.getDataSourceUrl());
        flinkTableGenerate.setStreamLoadUrl(sdpDataSource.getStreamLoadUrl());
        flinkTableGenerate.setDatabaseName(sdpDataSource.getDatabaseName());
        flinkTableGenerate.setUserName(sdpDataSource.getUserName());
        flinkTableGenerate.setPwd(sdpDataSource.getPassword());
        flinkTableGenerate.setMetadataTableColumnList(metadataTableColumnList);
        flinkTableGenerate.setMetaTableType(sdpMetaTableConfigBO.getMetaTableType());
        flinkTableGenerate.setCertifyType(sdpDataSource.getCertifyType());
        flinkTableGenerate.setHbaseZnode(sdpDataSource.getHbaseZnode());
        flinkTableGenerate.setHadoopConfDir(sdpDataSource.getHadoopConfDir());
        flinkTableGenerate.setHudiCatalogPath(sdpDataSource.getHudiCatalogPath());
        log.info("生成元表 _ConnectInfo_: {}", JSON.toJSONString(flinkTableGenerate));
        String flinkTableDdl = dataSourceFactory.getDataSource(sdpDataSource.getDataSourceType()).generateDdl(flinkTableGenerate);
        ResponseData responseData = new ResponseData<>();
        responseData.setData(flinkTableDdl).ok();
        return responseData;
    }

    @Transactional(rollbackFor = Exception.class)
    public ResponseData deleteMetaTableConfig(SdpMetaTableConfigBO sdpMetaTableConfigBO) {
        Assert.notNull(sdpMetaTableConfigBO.getId(), ResponseCode.METATABLE_ID_CANNOT_BE_EMPTY);
        //查询文件id
        SdpMetaTableConfig metaTableConfig = this.get(sdpMetaTableConfigBO.getId());

        //文件锁定校验
        fileService.fileLockCheck(metaTableConfig.getFileId());

        //删除
        this.disable(SdpMetaTableConfig.class,sdpMetaTableConfigBO.getId());
        //获取最新元表
        List<SdpMetaTableConfig> metaTableConfigList = this.selectAll(new SdpMetaTableConfig(metaTableConfig.getFileId()));
        String[] flinkDdlArray = metaTableConfigList.stream().map(SdpMetaTableConfig::getFlinkDdl).toArray(String[]::new);
        String joinFlinkDdl = StrUtil.join(StrUtil.LF, flinkDdlArray);
        log.info("删除元表[{}]后，最新flink dll：\n{}", metaTableConfig.getFlinkTableName(), joinFlinkDdl);
        ResponseData responseData = new ResponseData<>();
        responseData.setData(joinFlinkDdl).ok();
        return responseData;
    }

    /**
     * sink 和 source分开请求
     * postman批量生成元表
     * @return
     */
    public boolean addMetaTableConfigs(List<MetaTableConfigBatchDomain> domains) {
        if (CollectionUtil.isEmpty(domains)){
             return false;
        }
        List<SdpMetaTableConfig> metaTableConfigList = new ArrayList<>();
        for (MetaTableConfigBatchDomain domain : domains) {
            SdpFile sdpFile = fileService.get(domain.getFileId());
            SdpProject sdpProject = projectService.get(sdpFile.getProjectId());

            String groupId = sdpProject.getProjectCode()+"-"+sdpFile.getFileName();
            String[] topics = domain.getTopics().replace("，", ",").split(",");
            for (String topic : topics) {
                SdpMetaTableConfig sdpMetaTableConfig =  new SdpMetaTableConfig();
                sdpMetaTableConfig.setFileId(domain.getFileId());
                if (domain.getMetaTableType().equals("sink")) {
                    String flinkTableName = "sink_" + topic.replace("-","_");
                    sdpMetaTableConfig.setFlinkTableName(flinkTableName);
                    sdpMetaTableConfig.setMetaTableType("sink");

                    String ddl = "CREATE TABLE `%s` (\n" +
                            "  `rawData` VARCHAR\n" +
                            ") WITH (\n" +
                            "  'connector' = 'kafka',\n" +
                            "  'properties.bootstrap.servers' = 'b00a738fc69bffe491dfa50a1692c33a',\n" +
                            "  'topic' = '%s',\n" +
                            "  'sink.partitioner' = 'sdp-canal',\n" +
                            "  'properties.sdp.kafka.key.property' = 'id',\n" +
                            "  'format' = 'raw',\n" +
                            "  'properties.acks' ='all',\n" +
                            "  'properties.batch.size' = '16384',\n" +
                            "  'properties.linger.ms' = '50',\n" +
                            "  'properties.buffer.memory' ='33554432'\n" +
                            ");";
                    sdpMetaTableConfig.setFlinkDdl(String.format(ddl,flinkTableName,topic));
                }else {
                    String flinkTableName =  "source_" + topic.replace("-","_");
                    sdpMetaTableConfig.setFlinkTableName(flinkTableName);
                    sdpMetaTableConfig.setMetaTableType("source");

                    String ddl = "CREATE TABLE `%s` (\n" +
                            "  `rawData` VARCHAR\n" +
                            ") WITH (\n" +
                            "  'connector' = 'kafka',\n" +
                            "  'properties.bootstrap.servers' = '4ba4c6bdebfb91d3dc6064e176e9565a',\n" +
                            "  'topic' = '%s',\n" +
                            "  'properties.group.id' = '%s',\n" +
                            "  'scan.startup.mode' = 'latest-offset',\n" +
                            "  'scan.topic-partition-discovery.interval' = '10000',\n" +
                            "  'format' = 'raw'\n" +
                            ");";
                    sdpMetaTableConfig.setFlinkDdl(String.format(ddl,flinkTableName,topic,groupId));
                }
                sdpMetaTableConfig.setDataSourceId(domain.getDataSourceId());
                sdpMetaTableConfig.setMetaTableName(topic);

                metaTableConfigList.add(sdpMetaTableConfig);
            }
        }


        if(CollectionUtil.isNotEmpty(metaTableConfigList)){
            MetaTableConfigService me = SpringUtils.getBean(MetaTableConfigService.class);
            me.addMetaTableConfig(metaTableConfigList);
        }
        return true;
    }
}