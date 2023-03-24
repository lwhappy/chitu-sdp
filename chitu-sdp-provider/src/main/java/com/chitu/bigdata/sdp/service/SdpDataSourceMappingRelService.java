

/**
 * <pre>
 * 作   者：WANGHAI
 * 创建日期：2022-10-29
 * </pre>
 */

package com.chitu.bigdata.sdp.service;

import com.alibaba.fastjson.JSON;
import com.chitu.bigdata.sdp.api.bo.ChangeEnvBO;
import com.chitu.bigdata.sdp.api.bo.SdpFileBO;
import com.chitu.bigdata.sdp.api.bo.SdpMetaTableConfigBO;
import com.chitu.bigdata.sdp.api.enums.*;
import com.chitu.bigdata.sdp.api.model.*;
import com.chitu.bigdata.sdp.api.vo.ChangeEnvVO;
import com.chitu.bigdata.sdp.api.vo.DataSourceResp;
import com.chitu.bigdata.sdp.constant.BusinessFlag;
import com.chitu.bigdata.sdp.interceptor.EnvHolder;
import com.chitu.bigdata.sdp.mapper.SdpDataSourceMappingRelMapper;
import com.chitu.bigdata.sdp.service.datasource.DataSourceFactory;
import com.chitu.bigdata.sdp.service.validate.util.SqlUtil;
import com.chitu.bigdata.sdp.utils.CommonSqlParser;
import com.chitu.bigdata.sdp.utils.SqlParserUtil;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.model.ResponseData;
import com.chitu.cloud.service.GenericService;
import com.chitu.cloud.utils.ContextUtils;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.xiaoleilu.hutool.util.ReUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.sql.parser.ddl.SqlCreateCatalog;
import org.apache.flink.sql.parser.ddl.SqlCreateTable;
import org.apache.flink.sql.parser.dml.RichSqlInsert;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * <pre>
 * 转环境数据源映射关系表业务类
 * </pre>
 */
@Service("sdpDataSourceMappingRelService")
@Slf4j
public class SdpDataSourceMappingRelService extends GenericService<SdpDataSourceMappingRel, Long> {

    @Autowired
    FileService fileService;
    @Autowired
    SdpMetaTableRelationService sdpMetaTableRelationService;
    @Autowired
    DataSourceService dataSourceService;
    @Autowired
    FileVersionService fileVersionService;
    @Autowired
    FolderService folderService;
    @Autowired
    MetaTableConfigService metaTableConfigService;
    private static final String SQL_CONTENT = "%s\r\n%s";

    @Autowired
    DataSourceFactory dataSourceFactory;

    public SdpDataSourceMappingRelService(@Autowired SdpDataSourceMappingRelMapper sdpDataSourceMappingRelMapper) {
        super(sdpDataSourceMappingRelMapper);
    }

    public SdpDataSourceMappingRelMapper getMapper() {
        return (SdpDataSourceMappingRelMapper) super.genericMapper;
    }

    /**
     * 获取转环境比较数据
     */
    public ChangeEnvVO compare(SdpFileBO fileBO) {
        SdpFile uatFile = fileService.get(fileBO.getId());
        if(BusinessFlag.DI.name().equals(uatFile.getBusinessFlag())){
            throw new ApplicationException(ResponseCode.COMMON_ERR,"数据集成作业不支持转环境");
        }
        ChangeEnvVO vo = new ChangeEnvVO();
        //转环境数据源映射关系
        List<SdpDataSourceMappingRel> mappingRels = Lists.newArrayList();
        //转换环境uat和prod比较数据
        List<SdpVersion> compareVersions = Lists.newArrayList();

        //hive 写在etl_content的情况
        Map<String, String> flinkTableDataSourceTypeMap = new HashMap<>();
        Map<String, String> flinkTableDataSourceUrlMap = new HashMap<>();
        try {
            String etlContent = uatFile.getEtlContent();
            etlContent = SqlUtil.removeNotes(etlContent);
            if (StrUtil.isNotBlank(etlContent) && StrUtil.isNotBlank(etlContent.trim())) {
                CommonSqlParser commonSqlParser = new CommonSqlParser(etlContent);
                List<SqlCreateCatalog> createCatalogList = commonSqlParser.getCreateCatalogList();
                for (SqlCreateCatalog sqlCreateCatalog : createCatalogList) {
                    Map<String, String> sqlTableOptionMap = SqlParserUtil.getSqlTableOptionMap(sqlCreateCatalog);
                    if (!DataSourceType.HIVE.getType().equals(sqlTableOptionMap.get("type"))) {
                        continue;
                    }
                    flinkTableDataSourceTypeMap.put(sqlCreateCatalog.getCatalogName().toString(), DataSourceType.HIVE.getType());
                    flinkTableDataSourceUrlMap.put(sqlCreateCatalog.getCatalogName().toString(), sqlTableOptionMap.getOrDefault("hive-conf-dir",""));
                }
            }
        } catch (Exception e) {
            logger.warn("hive定义在etl_content里面解析异常", e);
        }

        List<SdpDataSourceMappingRel> sdpDataSourceMappingRels = getMapper().getByFileIdAndEnv(EnvironmentEnum.UAT.getCode(), fileBO.getId());
        if (CollectionUtils.isNotEmpty(sdpDataSourceMappingRels)) {
            //1.之前转过环境
            Map<String, SdpDataSourceMappingRel> mappingRelMap = sdpDataSourceMappingRels.stream().collect(Collectors.toMap(SdpDataSourceMappingRel::getUatPriKey, m -> m, (k1, k2) -> k1));

            //1.1 数据源关系
            SdpMetaTableRelation param = new SdpMetaTableRelation();
            param.setFileId(fileBO.getId());
            param.setEnabledFlag(EnableType.ENABLE.getCode());
            List<SdpMetaTableRelation> metaTableRelations = sdpMetaTableRelationService.selectAll(param);
            if (CollectionUtils.isNotEmpty(metaTableRelations)) {
                //获取数据源信息
                List<Long> dataSourceIds = metaTableRelations.stream().filter(f -> Objects.nonNull(f.getDataSourceId())).map(m -> m.getDataSourceId()).distinct().collect(Collectors.toList());
                List<SdpDataSource> dataSources =null;
                if (CollectionUtils.isNotEmpty(dataSourceIds)) {
                    dataSources =  dataSourceService.getByIds(dataSourceIds.toArray(new Long[dataSourceIds.size()]));
                }
                Map<Long, SdpDataSource> dataSourceMap = Optional.ofNullable(dataSources).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(SdpDataSource::getId, m -> m, (k1, k2) -> k1));

                List<Long> metaDataIds = metaTableRelations.stream().filter(f -> Objects.nonNull(f.getMetaTableId())).map(m -> m.getMetaTableId()).distinct().collect(Collectors.toList());
                List<SdpMetaTableConfig> sdpMetaTableConfigs = null;
                if(CollectionUtils.isNotEmpty(metaDataIds)){
                    sdpMetaTableConfigs = metaTableConfigService.getByIds(metaDataIds.toArray(new Long[metaDataIds.size()]));
                }
                Map<Long, SdpMetaTableConfig> sdpMetaTableConfigMap = Optional.ofNullable(sdpMetaTableConfigs).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(SdpMetaTableConfig::getId, m -> m, (k1, k2) -> k1));


                for (SdpMetaTableRelation relation : metaTableRelations) {

                    SdpDataSource sdpDataSource = dataSourceMap.get(relation.getDataSourceId());
                    Long dataSourceId = Objects.nonNull(sdpDataSource) ? sdpDataSource.getId() : null;
                    SdpDataSourceMappingRel sdpDataSourceMappingRel = mappingRelMap.get(Joiner.on("_").useForNull("").join(relation.getFileId(), relation.getMetaTableName(), Objects.nonNull(dataSourceId)?dataSourceId:""));

                    String kafkaMetaTableName = relation.getMetaTableName();
                    if(Objects.nonNull( relation.getMetaTableId())){
                        SdpMetaTableConfig sdpMetaTableConfig = sdpMetaTableConfigMap.get(relation.getMetaTableId());
                        if(Objects.nonNull(sdpMetaTableConfig)){
                            if(Objects.nonNull(sdpDataSource) && DataSourceType.KAFKA.getType().equals(sdpDataSource.getDataSourceType())){
                                kafkaMetaTableName = sdpMetaTableConfig.getMetaTableName();
                                sdpDataSourceMappingRel = mappingRelMap.get(Joiner.on("_").useForNull("").join(relation.getFileId(), kafkaMetaTableName, Objects.nonNull(dataSourceId)?dataSourceId:""));
                            }
                        }
                    }

                    if (Objects.nonNull(sdpDataSourceMappingRel)) {
                        // 已转过环境
                        if(Objects.nonNull(sdpDataSource) && StrUtil.isNotBlank(sdpDataSource.getDataSourceName())){
                            sdpDataSourceMappingRel.setUatDataSourceName(sdpDataSource.getDataSourceName());
                        }

                        //使用生产数据源信息直接替换之前保存下来的冗余数据
                        if(Objects.nonNull(sdpDataSourceMappingRel.getProdDataSourceId())){
                            try {
                                EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.PROD.getCode());
                                queryProdDataSource(uatFile.getProjectId(), sdpDataSourceMappingRel);
                            } finally {
                                EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.UAT.getCode());
                            }
                        }

                        if(DataSourceType.KAFKA.getType().equals(sdpDataSourceMappingRel.getUatDataSourceType())){
                            //kafka的拿元表里面配置的topic，解决通配符的问题
                            sdpDataSourceMappingRel.setUatMetaTableName(kafkaMetaTableName);
                            sdpDataSourceMappingRel.setProdMetaTableName(kafkaMetaTableName);
                        }else {
                            sdpDataSourceMappingRel.setProdMetaTableName(relation.getMetaTableName());
                        }

                        mappingRels.add(sdpDataSourceMappingRel);
                    } else {
                        //新增的元表关系
                        SdpDataSourceMappingRel mSdpDataSourceMappingRel = new SdpDataSourceMappingRel();
                        if (Objects.nonNull(sdpDataSource)) {
                            mSdpDataSourceMappingRel.setUatDataSourceName(sdpDataSource.getDataSourceName());
                            mSdpDataSourceMappingRel.setUatDataSourceUrl(sdpDataSource.getDataSourceUrl());
                            mSdpDataSourceMappingRel.setUatDataSourceType(sdpDataSource.getDataSourceType());
                        } else {
                            String dataSourceType = flinkTableDataSourceTypeMap.get(relation.getFlinkTableName());
                            if (StrUtil.isNotBlank(dataSourceType)) {
                                mSdpDataSourceMappingRel.setUatDataSourceType(dataSourceType);
                            }
                            String dataSourceUrl = flinkTableDataSourceUrlMap.get(relation.getFlinkTableName());
                            if (StrUtil.isNotBlank(dataSourceUrl)) {
                                mSdpDataSourceMappingRel.setUatDataSourceUrl(dataSourceUrl);
                            }
                        }

                        mSdpDataSourceMappingRel.setUatDataSourceId(relation.getDataSourceId());
                        mSdpDataSourceMappingRel.setUatMetaTableName(relation.getMetaTableName());

                        String metaTableType = relation.getMetaTableType();
                        if(Objects.nonNull( relation.getMetaTableId())){
                            SdpMetaTableConfig sdpMetaTableConfig = sdpMetaTableConfigMap.get(relation.getMetaTableId());
                            if(Objects.nonNull(sdpMetaTableConfig)){
                                if(StrUtil.isBlank(metaTableType)){
                                    metaTableType = sdpMetaTableConfig.getMetaTableType();
                                }

                                if(DataSourceType.KAFKA.getType().equals(mSdpDataSourceMappingRel.getUatDataSourceType())){
                                    kafkaMetaTableName = sdpMetaTableConfig.getMetaTableName();
                                }
                            }
                        }

                        mSdpDataSourceMappingRel.setUatMetaTableType(metaTableType);
                        mSdpDataSourceMappingRel.setUatFileId(relation.getFileId());

                        SdpDataSource prodDataSource = queryProdDataSourceByUatDataSourceName(uatFile.getProjectId(), mSdpDataSourceMappingRel.getUatDataSourceName(), mSdpDataSourceMappingRel.getUatDataSourceType());
                        if(Objects.nonNull(prodDataSource)){
                            mSdpDataSourceMappingRel.setProdDataSourceType(prodDataSource.getDataSourceType());
                            mSdpDataSourceMappingRel.setProdDataSourceId(prodDataSource.getId());
                            mSdpDataSourceMappingRel.setProdDataSourceName(prodDataSource.getDataSourceName());
                            mSdpDataSourceMappingRel.setProdDataSourceUrl(prodDataSource.getDataSourceUrl());
                        }else {
                            mSdpDataSourceMappingRel.setProdDataSourceType(mSdpDataSourceMappingRel.getUatDataSourceType());
                            mSdpDataSourceMappingRel.setProdDataSourceId(null);
                        }

                        if(DataSourceType.KAFKA.getType().equals(mSdpDataSourceMappingRel.getUatDataSourceType())){
                            //kafka的拿元表里面配置的topic，解决通配符的问题
                            mSdpDataSourceMappingRel.setUatMetaTableName(kafkaMetaTableName);
                            mSdpDataSourceMappingRel.setProdMetaTableName(kafkaMetaTableName);
                        }else {
                            mSdpDataSourceMappingRel.setProdMetaTableName(relation.getMetaTableName());
                        }

                        mSdpDataSourceMappingRel.setProdMetaTableType(metaTableType);
                        mSdpDataSourceMappingRel.setProdFileId(null);

                        mappingRels.add(mSdpDataSourceMappingRel);
                    }
                }
            }

            //1.2 比较版本数据
            SdpVersion uatVersion = new SdpVersion();
            fileVersionService.getRunningVersion(uatFile, uatVersion);
            compareVersions.add(uatVersion);


            Long prodFileId = sdpDataSourceMappingRels.get(0).getProdFileId();
            try {
                if(Objects.nonNull(prodFileId)){
                    EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.PROD.getCode());
                    SdpFile paramSdpFile = new SdpFile();
                    paramSdpFile.setId(prodFileId);
                    paramSdpFile.setEnabledFlag(EnableType.ENABLE.getCode());
                    List<SdpFile> sdpFiles = fileService.selectAll(paramSdpFile);

                    if(CollectionUtils.isNotEmpty(sdpFiles)){
                        SdpVersion prodVersion = new SdpVersion();
                        fileVersionService.getRunningVersion(sdpFiles.get(0), prodVersion);
                        compareVersions.add(prodVersion);
                    }
                }
            } finally {
                EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.UAT.getCode());
            }
        } else {
            //2. 之前没有转过环境

            //2.1 数据源关系
            SdpMetaTableRelation param = new SdpMetaTableRelation();
            param.setFileId(fileBO.getId());
            param.setEnabledFlag(EnableType.ENABLE.getCode());
            List<SdpMetaTableRelation> metaTableRelations = sdpMetaTableRelationService.selectAll(param);
            if (CollectionUtils.isNotEmpty(metaTableRelations)) {
                SdpDataSourceMappingRel sdpDataSourceMappingRel = null;

                //获取数据源信息
                List<Long> dataSourceIds = metaTableRelations.stream().filter(f -> Objects.nonNull(f.getDataSourceId())).map(m -> m.getDataSourceId()).distinct().collect(Collectors.toList());
                List<SdpDataSource> dataSources = null;
                if (CollectionUtils.isNotEmpty(dataSourceIds)) {
                    dataSources = dataSourceService.getByIds(dataSourceIds.toArray(new Long[dataSourceIds.size()]));
                }
                Map<Long, SdpDataSource> dataSourceMap = Optional.ofNullable(dataSources).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(SdpDataSource::getId, m -> m, (k1, k2) -> k1));


                List<Long> metaDataIds = metaTableRelations.stream().filter(f -> Objects.nonNull(f.getMetaTableId())).map(m -> m.getMetaTableId()).distinct().collect(Collectors.toList());
                List<SdpMetaTableConfig> sdpMetaTableConfigs = null;
                if(CollectionUtils.isNotEmpty(metaDataIds)){
                    sdpMetaTableConfigs = metaTableConfigService.getByIds(metaDataIds.toArray(new Long[metaDataIds.size()]));
                }
                Map<Long, SdpMetaTableConfig> sdpMetaTableConfigMap = Optional.ofNullable(sdpMetaTableConfigs).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(SdpMetaTableConfig::getId, m -> m, (k1, k2) -> k1));


                for (SdpMetaTableRelation relation : metaTableRelations) {
                    sdpDataSourceMappingRel = new SdpDataSourceMappingRel();
                    SdpDataSource sdpDataSource = dataSourceMap.get(relation.getDataSourceId());
                    if (Objects.nonNull(sdpDataSource)) {
                        sdpDataSourceMappingRel.setUatDataSourceName(sdpDataSource.getDataSourceName());
                        sdpDataSourceMappingRel.setUatDataSourceUrl(sdpDataSource.getDataSourceUrl());
                        sdpDataSourceMappingRel.setUatDataSourceType(sdpDataSource.getDataSourceType());
                    } else {
                        String dataSourceType = flinkTableDataSourceTypeMap.get(relation.getFlinkTableName());
                        if (StrUtil.isNotBlank(dataSourceType)) {
                            sdpDataSourceMappingRel.setUatDataSourceType(dataSourceType);
                        }
                        String dataSourceUrl = flinkTableDataSourceUrlMap.get(relation.getFlinkTableName());
                        if (StrUtil.isNotBlank(dataSourceUrl)) {
                            sdpDataSourceMappingRel.setUatDataSourceUrl(dataSourceUrl);
                        }
                    }

                    sdpDataSourceMappingRel.setUatDataSourceId(relation.getDataSourceId());
                    sdpDataSourceMappingRel.setUatMetaTableName(relation.getMetaTableName());

                    String metaTableType = relation.getMetaTableType();
                    String kafkaMetaTableName = relation.getMetaTableName();
                    if(Objects.nonNull( relation.getMetaTableId())){
                        SdpMetaTableConfig sdpMetaTableConfig = sdpMetaTableConfigMap.get(relation.getMetaTableId());
                        if(Objects.nonNull(sdpMetaTableConfig)){
                            if(StrUtil.isBlank(metaTableType)){
                                metaTableType = sdpMetaTableConfig.getMetaTableType();
                            }

                            if(DataSourceType.KAFKA.getType().equals(sdpDataSourceMappingRel.getUatDataSourceType())){
                                kafkaMetaTableName = sdpMetaTableConfig.getMetaTableName();
                            }
                        }
                    }
                    sdpDataSourceMappingRel.setUatMetaTableType(metaTableType);
                    sdpDataSourceMappingRel.setUatFileId(relation.getFileId());


                    SdpDataSource prodDataSource = queryProdDataSourceByUatDataSourceName(uatFile.getProjectId(), sdpDataSourceMappingRel.getUatDataSourceName(), sdpDataSourceMappingRel.getUatDataSourceType());
                    if(Objects.nonNull(prodDataSource)){
                        sdpDataSourceMappingRel.setProdDataSourceType(prodDataSource.getDataSourceType());
                        sdpDataSourceMappingRel.setProdDataSourceId(prodDataSource.getId());
                        sdpDataSourceMappingRel.setProdDataSourceName(prodDataSource.getDataSourceName());
                        sdpDataSourceMappingRel.setProdDataSourceUrl(prodDataSource.getDataSourceUrl());
                    }else {
                        sdpDataSourceMappingRel.setProdDataSourceType(sdpDataSourceMappingRel.getUatDataSourceType());
                        sdpDataSourceMappingRel.setProdDataSourceId(null);
                    }

                    if(DataSourceType.KAFKA.getType().equals(sdpDataSourceMappingRel.getUatDataSourceType())){
                        //kafka的拿元表里面配置的topic，解决通配符的问题
                        sdpDataSourceMappingRel.setUatMetaTableName(kafkaMetaTableName);
                        sdpDataSourceMappingRel.setProdMetaTableName(kafkaMetaTableName);
                    }else {
                        sdpDataSourceMappingRel.setProdMetaTableName(relation.getMetaTableName());
                    }

                    sdpDataSourceMappingRel.setProdMetaTableType(metaTableType);
                    sdpDataSourceMappingRel.setProdFileId(null);
                    mappingRels.add(sdpDataSourceMappingRel);
                }
            }

            //2.2 比较版本数据
            SdpVersion uatVersion = new SdpVersion();
            fileVersionService.getRunningVersion(uatFile, uatVersion);
            compareVersions.add(uatVersion);
        }

        Map<String, SdpDataSourceMappingRel> distinctMap = Optional.ofNullable(mappingRels).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(SdpDataSourceMappingRel::getUatPriKey, m -> m, (k1, k2) -> k2));
        vo.setSdpDataSourceMappingRels(new ArrayList<>(distinctMap.values()));
        vo.setCompareVersions(compareVersions);

        return vo;
    }

    /**
     * 转环境
     * @param changeEnvBO
     * @return
     */
    @Transactional(rollbackFor = Exception.class)
    public Boolean changeEnv(ChangeEnvBO changeEnvBO) throws Exception {
        Long fileId = changeEnvBO.getFileId();
        SdpFile uatFile = fileService.get(fileId);
        if(BusinessFlag.DI.name().equals(uatFile.getBusinessFlag())){
            throw new ApplicationException(ResponseCode.COMMON_ERR,"数据集成作业不支持转环境");
        }
        //同步到生产之后，需要在生产创建同样的目录
        Long lastFolderId = mkFolders4Prod(changeEnvBO.getFileId());
        String userId = Optional.ofNullable(ContextUtils.get()).map(m -> m.getUserId()).orElse(null);
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());

        List<SdpDataSourceMappingRel> sdpDataSourceMappingRels = getMapper().getByFileIdAndEnv(EnvironmentEnum.UAT.getCode(), changeEnvBO.getFileId());
        //锁定生产文件
        if(CollectionUtils.isNotEmpty(sdpDataSourceMappingRels)){
            List<Long> prodFileIds = sdpDataSourceMappingRels.stream().filter(f -> Objects.nonNull(f) && Objects.nonNull(f.getProdFileId())).map(m -> m.getProdFileId()).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(prodFileIds)) {
                try {
                    EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.PROD.getCode());
                    SdpFileBO sdpFileBO = new SdpFileBO();
                    sdpFileBO.setId(prodFileIds.get(0));
                    fileService.lockFile(sdpFileBO);
                }finally {
                    EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.UAT.getCode());
                }
            }
        }
        if (FileType.SQL_STREAM.getType().equals(uatFile.getFileType())) {
            //1.SQL任务
            List<SdpDataSourceMappingRel> sdpDataSourceMappingRels4Page = changeEnvBO.getSdpDataSourceMappingRels();
            if(CollectionUtils.isNotEmpty(sdpDataSourceMappingRels4Page)){
                for (SdpDataSourceMappingRel sdpDataSourceMappingRel : sdpDataSourceMappingRels4Page) {
                    try {
                        EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.PROD.getCode());
                        if(
                                DataSourceType.KAFKA.getType().equals(sdpDataSourceMappingRel.getProdDataSourceType())
                                ||DataSourceType.MYSQL.getType().equals(sdpDataSourceMappingRel.getProdDataSourceType())
                                ||DataSourceType.ES.getType().equals(sdpDataSourceMappingRel.getProdDataSourceType())
                                ||DataSourceType.HBASE.getType().equals(sdpDataSourceMappingRel.getProdDataSourceType())
                        ){
                            SdpMetaTableConfigBO paramSmtcb = new SdpMetaTableConfigBO();
                            paramSmtcb.setDataSourceId(sdpDataSourceMappingRel.getProdDataSourceId());
                            paramSmtcb.setMetaTableName(sdpDataSourceMappingRel.getProdMetaTableName());
                            ResponseData responseData = metaTableConfigService.tableExist(paramSmtcb);
                            if(Objects.nonNull(responseData) && 0 ==  responseData.getCode() && !((Boolean) responseData.getData())){
                                  String msg = "生产环境 [" + sdpDataSourceMappingRel.getProdDataSourceName() +"] 数据源 [" + sdpDataSourceMappingRel.getProdMetaTableName() + "] 表不存在";
                                  throw new ApplicationException(ResponseCode.COMMON_ERR,msg);
                            }
                        }
                    }finally {
                        EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.UAT.getCode());
                    }
                }
            }

            if (CollectionUtils.isNotEmpty(sdpDataSourceMappingRels)) {
                //1.1 SQL之前已经同步过

                //1.1.1 删除之前数据源映射的关系
                List<Long> relIds = sdpDataSourceMappingRels.stream().filter(f -> Objects.nonNull(f) && Objects.nonNull(f.getId())).map(f -> f.getId()).collect(Collectors.toList());
                if (CollectionUtils.isNotEmpty(relIds)) {
                    delete(relIds.toArray(new Long[relIds.size()]));
                }

                //1.1.2 文件处理
                Long prodFileId = sdpDataSourceMappingRels.get(0).getProdFileId();
                Long mProdFileId = prodFileId;
                List<SdpFile> prodFiles = null;
                try {
                    EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.PROD.getCode());

                    SdpFile prodSdpFileParam = new SdpFile();
                    prodSdpFileParam.setId(prodFileId);
                    prodSdpFileParam.setEnabledFlag(EnableType.ENABLE.getCode());
                    prodFiles = fileService.selectAll(prodSdpFileParam);
                    if (CollectionUtils.isNotEmpty(prodFiles)) {
                        //作业还存在（只更sql信息和目录，不再更新配置）
                        SdpFile mSdpFile = new SdpFile();
                        mSdpFile.setId(prodFileId);
                        mSdpFile.setFolderId(lastFolderId);
                        mSdpFile.setContent(uatFile.getContent());
                        mSdpFile.setEtlContent(uatFile.getEtlContent());
                        mSdpFile.setMetaTableContent(uatFile.getMetaTableContent());
                        if (Objects.nonNull(userId)) {
                            mSdpFile.setUpdatedBy(userId);
                        }
                        mSdpFile.setUpdationDate(timestamp);
                        mSdpFile.setNeed2Approve(null);
                        fileService.updateSelective(mSdpFile);
                    } else {
                        //作业已经被删除
                        uatFile.setId(null);
                        uatFile.setFolderId(lastFolderId);
                        if (Objects.nonNull(userId)) {
                            uatFile.setUpdatedBy(userId);
                        }
                        uatFile.setUpdationDate(timestamp);
                        uatFile.setNeed2Approve(null);
                        fileService.insertSelective(uatFile);
                        mProdFileId = uatFile.getId();
                    }

                    //先清除生产的元表数据，在重新插入
                    metaTableConfigService.getMapper().disableByFileId(mProdFileId);
                } finally {
                    EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.UAT.getCode());
                }

                //兼容 uat消费位置不能覆盖生产的消费位置
                Map<String,String> kafkaFlinkTableAndCreateTableMap = Maps.newHashMap();
                if(CollectionUtils.isNotEmpty(prodFiles)){
                    SdpFile sdpFile = prodFiles.get(0);
                    CommonSqlParser commonSqlParser1 = new CommonSqlParser(sdpFile.getContent());
                    List<SqlCreateTable> createTableList = commonSqlParser1.getCreateTableList();
                    for (SqlCreateTable sqlCreateTable : createTableList) {
                        if(SqlParserUtil.isSourceKafka(sqlCreateTable)){
                            kafkaFlinkTableAndCreateTableMap.put(sqlCreateTable.getTableName().toString(),sqlCreateTable.toString());
                        }
                    }
                }

                //1.1.3 元表处理
                metaTable4ChangeEnv(changeEnvBO, uatFile, userId, sdpDataSourceMappingRels4Page, mProdFileId,kafkaFlinkTableAndCreateTableMap);

                //1.1.4 关系处理
                for (SdpDataSourceMappingRel sdpDataSourceMappingRel : sdpDataSourceMappingRels4Page) {
                    sdpDataSourceMappingRel.setUatFileId(changeEnvBO.getFileId());
                    sdpDataSourceMappingRel.setProdFileId(mProdFileId);
                    insertSelective(sdpDataSourceMappingRel);
                }

            } else {
                //1.2 SQL之前没有同步过

                //1.2.1 文件处理
                Long mProdFileId = null;
                try {
                    EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.PROD.getCode());
                    uatFile.setId(null);
                    uatFile.setFolderId(lastFolderId);
                    if (Objects.nonNull(userId)) {
                        uatFile.setUpdatedBy(userId);
                    }
                    uatFile.setUpdationDate(timestamp);
                    uatFile.setNeed2Approve(null);
                    fileService.insertSelective(uatFile);
                    mProdFileId = uatFile.getId();
                } finally {
                    EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.UAT.getCode());
                }

                //1.2.2 元表数据处理
                metaTable4ChangeEnv(changeEnvBO, uatFile, userId, sdpDataSourceMappingRels4Page, mProdFileId,null);

                //1.2.3 转环境关系
                for (SdpDataSourceMappingRel sdpDataSourceMappingRel : sdpDataSourceMappingRels4Page) {
                    sdpDataSourceMappingRel.setUatFileId(changeEnvBO.getFileId());
                    sdpDataSourceMappingRel.setProdFileId(mProdFileId);
                    insertSelective(sdpDataSourceMappingRel);
                }
            }
        } else {
            //2.DataStream任务
            if (CollectionUtils.isNotEmpty(sdpDataSourceMappingRels)) {
                //2.1 DS之前已经同步过

                //2.1.1 文件处理
                Long prodFileId = sdpDataSourceMappingRels.get(0).getProdFileId();
                Long mProdFileId = prodFileId;
                try {
                    EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.PROD.getCode());

                    SdpFile prodSdpFileParam = new SdpFile();
                    prodSdpFileParam.setId(prodFileId);
                    prodSdpFileParam.setEnabledFlag(EnableType.ENABLE.getCode());
                    List<SdpFile> prodFiles = fileService.selectAll(prodSdpFileParam);
                    if (CollectionUtils.isNotEmpty(prodFiles)) {
                        //作业还存在（只更新包信息和目录，不再更新配置）
                        SdpFile mSdpFile = new SdpFile();
                        mSdpFile.setId(prodFileId);
                        mSdpFile.setDataStreamConfig(uatFile.getDataStreamConfig());
                        mSdpFile.setFolderId(lastFolderId);
                        if (Objects.nonNull(userId)) {
                            mSdpFile.setUpdatedBy(userId);
                        }
                        mSdpFile.setUpdationDate(timestamp);
                        mSdpFile.setNeed2Approve(null);
                        fileService.updateSelective(mSdpFile);
                    } else {
                        //作业已经被删除
                        uatFile.setId(null);
                        uatFile.setFolderId(lastFolderId);
                        if (Objects.nonNull(userId)) {
                            uatFile.setUpdatedBy(userId);
                        }
                        uatFile.setUpdationDate(timestamp);
                        uatFile.setNeed2Approve(null);
                        fileService.insertSelective(uatFile);
                        mProdFileId = uatFile.getId();
                    }
                } finally {
                    EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.UAT.getCode());
                }

                //2.1.2 更新转环境关系
                for (SdpDataSourceMappingRel sdpDataSourceMappingRel : sdpDataSourceMappingRels) {
                    sdpDataSourceMappingRel.setProdFileId(mProdFileId);
                    updateSelective(sdpDataSourceMappingRel);
                }

            } else {
                //2.2 DS之前没有同步过

                //2.2.1 文件处理
                Long mProdFileId = null;
                try {
                    EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.PROD.getCode());
                    uatFile.setId(null);
                    uatFile.setFolderId(lastFolderId);
                    if (Objects.nonNull(userId)) {
                        uatFile.setUpdatedBy(userId);
                    }
                    uatFile.setUpdationDate(timestamp);
                    uatFile.setNeed2Approve(null);
                    fileService.insertSelective(uatFile);
                    mProdFileId = uatFile.getId();
                } finally {
                    EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.UAT.getCode());
                }

                //2.2.2 转环境关系
                SdpDataSourceMappingRel sdpDataSourceMappingRel = new SdpDataSourceMappingRel();
                sdpDataSourceMappingRel.setUatFileId(changeEnvBO.getFileId());
                sdpDataSourceMappingRel.setProdFileId(mProdFileId);
                insertSelective(sdpDataSourceMappingRel);
            }
        }

        return true;
    }

    /**
     * 转环境元表处理
     * @param changeEnvBO
     * @param userId
     * @param sdpDataSourceMappingRels4Page
     * @param mProdFileId
     * @throws Exception
     */
    private void metaTable4ChangeEnv(ChangeEnvBO changeEnvBO, SdpFile sdpFile, String userId, List<SdpDataSourceMappingRel> sdpDataSourceMappingRels4Page, Long mProdFileId, Map<String,String> kafkaFlinkTableAndCreateTableMap) throws Exception {
        //3. 元表处理
        Map<String, SdpDataSourceMappingRel> newUatRelMap = Optional.ofNullable(sdpDataSourceMappingRels4Page).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(SdpDataSourceMappingRel::getUatPriKey, m -> m, (k1, k2) -> k1));

        //获取uat元表数据
        SdpMetaTableConfig sdpMetaTableConfig = new SdpMetaTableConfig();
        sdpMetaTableConfig.setFileId(changeEnvBO.getFileId());
        sdpMetaTableConfig.setEnabledFlag(EnableType.ENABLE.getCode());
        List<SdpMetaTableConfig> uatSdpMetaTableConfigs = metaTableConfigService.selectAll(sdpMetaTableConfig);
        Map<String, SdpMetaTableConfig> uatSdpMetaTableMap = Optional.ofNullable(uatSdpMetaTableConfigs).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(SdpMetaTableConfig::getFlinkTableName, m -> m, (k1, k2) -> k2));

        List<Long> uatDataSourceIds = Optional.ofNullable(uatSdpMetaTableConfigs).orElse(new ArrayList<>()).stream().filter(f -> Objects.nonNull(f) && Objects.nonNull(f.getDataSourceId())).map(m -> m.getDataSourceId()).collect(Collectors.toList());
        List<SdpDataSource> uatDataSources = null;
        if (CollectionUtils.isNotEmpty(uatDataSourceIds)) {
            uatDataSources = dataSourceService.getByIds(uatDataSourceIds.toArray(new Long[uatDataSourceIds.size()]));
        }
        Map<Long, SdpDataSource> uatDataSourceMap = Optional.ofNullable(uatDataSources).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(SdpDataSource::getId, m -> m, (k1, k2) -> k2));

        //已经处理的关系
        Set<String> processedRel = new HashSet<>();
        //1.正常建元表
        if(CollectionUtils.isNotEmpty(uatSdpMetaTableConfigs)){
            for (SdpMetaTableConfig uatMetaTableConfig : uatSdpMetaTableConfigs) {
                SdpDataSource uatSdpDataSource = uatDataSourceMap.get(uatMetaTableConfig.getDataSourceId());
                if(
                        DataSourceType.HIVE.getType().equalsIgnoreCase(uatSdpDataSource.getDataSourceType())

                ){
                    // create catalog 元表和物理表关系是1对0..*。元表没有指定特定的物理表
                    SdpDataSourceMappingRel sdpDataSourceMappingRel = null;
                    for (SdpDataSourceMappingRel xSdpDataSourceMappingRel : sdpDataSourceMappingRels4Page) {
                          if(
                                  Objects.nonNull(uatSdpDataSource)
                              && Objects.nonNull(uatSdpDataSource.getId())
                              &&  uatSdpDataSource.getId().equals(xSdpDataSourceMappingRel.getUatDataSourceId())
                          ){
                              sdpDataSourceMappingRel =  xSdpDataSourceMappingRel;
                              break;
                          }
                    }
                    if (Objects.nonNull(sdpDataSourceMappingRel)) {
                        String uatPriKey = Joiner.on("_").useForNull("").join(changeEnvBO.getFileId(), sdpDataSourceMappingRel.getUatMetaTableName(), Objects.nonNull(sdpDataSourceMappingRel.getUatDataSourceId())?sdpDataSourceMappingRel.getUatDataSourceId():"");
                        processedRel.add(uatPriKey);
                        //获取生产的配置信息,创建生产的元表
                        try {
                            EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.PROD.getCode());

                            SdpMetaTableConfig existParam = new SdpMetaTableConfig();
                            existParam.setFileId(changeEnvBO.getFileId());
                            existParam.setFlinkTableName(uatMetaTableConfig.getFlinkTableName());
                            existParam.setEnabledFlag(EnableType.ENABLE.getCode());
                            List<SdpMetaTableConfig> existMetaTableConfigs = metaTableConfigService.selectAll(existParam);
                            if(CollectionUtils.isEmpty(existMetaTableConfigs)){
                                //新增生产元表
                                SdpMetaTableConfig insertSdpMetaTableConfig = new SdpMetaTableConfig();
                                BeanUtils.copyProperties(uatMetaTableConfig, insertSdpMetaTableConfig);
                                insertSdpMetaTableConfig.setFileId(mProdFileId);
                                insertSdpMetaTableConfig.setId(null);

                                //查询生产的数据源
                                SdpDataSource sdpDataSource = queryProdDataSource(sdpFile.getProjectId(), sdpDataSourceMappingRel);

                                insertSdpMetaTableConfig.setDataSourceId(sdpDataSource.getId());

                                insertSdpMetaTableConfig.setMetaTableName(sdpDataSourceMappingRel.getProdMetaTableName());
                                insertSdpMetaTableConfig.setMetaTableType(sdpDataSourceMappingRel.getProdMetaTableType());
                                if (Objects.nonNull(userId)) {
                                    insertSdpMetaTableConfig.setCreatedBy(userId);
                                    insertSdpMetaTableConfig.setUpdatedBy(userId);
                                }

                                String ddl = dataSourceFactory.getDataSource(sdpDataSource.getDataSourceType()).modifyOption4ChangeEnv(insertSdpMetaTableConfig.getFlinkDdl(), sdpDataSource);
                                insertSdpMetaTableConfig.setFlinkDdl(ddl);

                                metaTableConfigService.insertSelective(insertSdpMetaTableConfig);
                            }else {
                                logger.info("[{}]生产环境已经存在该元表:{}", changeEnvBO.getFileId(),uatMetaTableConfig.getFlinkTableName());
                            }
                        } finally {
                            EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.UAT.getCode());
                        }
                    } else {
                        logger.info("catalog: {} -> 不存在关系，应该是数据有误", JSON.toJSONString(uatMetaTableConfig));
                    }
                }else {
                    // create table
                    String uatPriKey = Joiner.on("_").useForNull("").join(changeEnvBO.getFileId(), uatMetaTableConfig.getMetaTableName(), Objects.nonNull(uatSdpDataSource.getId())?uatSdpDataSource.getId():"");
                    SdpDataSourceMappingRel sdpDataSourceMappingRel = newUatRelMap.get(uatPriKey);
                    if (Objects.nonNull(sdpDataSourceMappingRel)) {
                        processedRel.add(uatPriKey);
                        //获取生产的配置信息,创建生产的元表
                        try {
                            EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.PROD.getCode());
                            //新增生产元表
                            SdpMetaTableConfig insertSdpMetaTableConfig = new SdpMetaTableConfig();
                            BeanUtils.copyProperties(uatMetaTableConfig, insertSdpMetaTableConfig);
                            insertSdpMetaTableConfig.setFileId(mProdFileId);
                            insertSdpMetaTableConfig.setId(null);

                            //查询生产的数据源
                            SdpDataSource sdpDataSource = queryProdDataSource(sdpFile.getProjectId(), sdpDataSourceMappingRel);
                            insertSdpMetaTableConfig.setDataSourceId(sdpDataSource.getId());

                            insertSdpMetaTableConfig.setMetaTableName(sdpDataSourceMappingRel.getProdMetaTableName());
                            insertSdpMetaTableConfig.setMetaTableType(sdpDataSourceMappingRel.getProdMetaTableType());
                            if (Objects.nonNull(userId)) {
                                insertSdpMetaTableConfig.setCreatedBy(userId);
                                insertSdpMetaTableConfig.setUpdatedBy(userId);
                            }

                            sdpDataSource.setKafkaFlinkTableAndCreateTableMap(kafkaFlinkTableAndCreateTableMap);
                            String ddl = dataSourceFactory.getDataSource(sdpDataSource.getDataSourceType()).modifyOption4ChangeEnv(insertSdpMetaTableConfig.getFlinkDdl(), sdpDataSource);
                            insertSdpMetaTableConfig.setFlinkDdl(ddl);

                            metaTableConfigService.insertSelective(insertSdpMetaTableConfig);
                        } finally {
                            EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.UAT.getCode());
                        }
                    } else {
                        logger.info("table: {} -> 不存在关系，应该是数据有误", JSON.toJSONString(uatMetaTableConfig));
                    }
                }
            }
        }

        //2.把ddl信息合并到文件中
        try {
            EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.PROD.getCode());
            SdpFile mProdFile = fileService.get(mProdFileId);


            SdpMetaTableConfig param4Stc = new SdpMetaTableConfig();
            param4Stc.setFileId(mProdFileId);
            param4Stc.setEnabledFlag(EnableType.ENABLE.getCode());
            List<SdpMetaTableConfig> prodSdpMetaTableConfigs = metaTableConfigService.selectAll(param4Stc);
            
            StringBuilder mtcBuilder = new StringBuilder();
            for (int i = 0,len =  prodSdpMetaTableConfigs.size(); i < len; i++) {
                SdpMetaTableConfig smtc = prodSdpMetaTableConfigs.get(i);
                String flinkDdl = smtc.getFlinkDdl();
                if(i == len -1){
                    mtcBuilder.append(flinkDdl);
                }else {
                    mtcBuilder.append(flinkDdl).append(StrUtils.CRLF);
                }
            }
            log.info("[{}]转换环境合并元表内容： {}",changeEnvBO.getFileId(),mtcBuilder.toString());
            mProdFile.setMetaTableContent(mtcBuilder.toString());
            mProdFile.setContent(String.format(SQL_CONTENT, mProdFile.getMetaTableContent(), mProdFile.getEtlContent()));

            fileService.updateSelective(mProdFile);
        }finally {
            EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.UAT.getCode());
        }

        //3. 处理hive catalog 在etl_content里面的情况
        try {
            String etlContent = sdpFile.getEtlContent();
            etlContent = SqlUtil.removeNotes(etlContent);
            if (StrUtil.isBlank(etlContent) || StrUtil.isBlank(etlContent.trim())) {
                return;
            }
            CommonSqlParser commonSqlParser = new CommonSqlParser(etlContent);
            List<SqlCreateCatalog> createCatalogList = commonSqlParser.getCreateCatalogList();
            if(CollectionUtils.isEmpty(createCatalogList)){
                return;
            }

            //未处理的关系
            List<SdpDataSourceMappingRel> unProcessedRels = Lists.newArrayList();
            for (SdpDataSourceMappingRel sdpDataSourceMappingRel : sdpDataSourceMappingRels4Page) {
                if (processedRel.contains(sdpDataSourceMappingRel.getUatPriKey())) {
                    continue;
                }
                unProcessedRels.add(sdpDataSourceMappingRel);
            }
            Map<String, SdpDataSourceMappingRel> unProcessedRelMap = unProcessedRels.stream().collect(Collectors.toMap(SdpDataSourceMappingRel::getUatPriKey, m -> m, (k1, k2) -> k2));
            logger.info("未处理的映射关系",JSON.toJSONString(unProcessedRelMap));

            Map<String,String> flinkTableAndMetaTableMap = Maps.newHashMap();
            for (RichSqlInsert richSqlInsert : commonSqlParser.getInsertSqlList()) {
                String  targetTable = richSqlInsert.getTargetTable().toString();
                flinkTableAndMetaTableMap.put(SqlParserUtil.getFlinkTableName(targetTable),SqlParserUtil.getTableName(targetTable));
            }
            logger.info("元表和物理表映射关系：{}",JSON.toJSONString(flinkTableAndMetaTableMap));

            for (SqlCreateCatalog sqlCreateCatalog : createCatalogList) {
                Map<String, String> sqlTableOptionMap = SqlParserUtil.getSqlTableOptionMap(sqlCreateCatalog);
                if (!DataSourceType.HIVE.getType().equals(sqlTableOptionMap.get("type"))) {
                    continue;
                }

                String hivePathPattern = null;
                String hivePath = sqlTableOptionMap.get("hive-conf-dir");
                if(StrUtil.isNotBlank(hivePath)){
                    hivePathPattern = "\\s*'hive-conf-dir'\\s*=\\s*'"+hivePath+"'";
                }

                String hadoopPathPattern  = null;
                String hadoopPath = sqlTableOptionMap.get("hadoop-conf-dir");
                if(StrUtil.isNotBlank(hadoopPath)){
                    hadoopPathPattern = "\\s*'hadoop-conf-dir'\\s*=\\s*'"+hadoopPath+"'";
                }

                String flinkTableName = sqlCreateCatalog.getCatalogName().toString();
                //存在的元表,上面已经处理
                SdpMetaTableConfig metaTableConfig = uatSdpMetaTableMap.get(flinkTableName);
                if (Objects.nonNull(metaTableConfig)) {
                    continue;
                }
                if (Objects.isNull(unProcessedRelMap) || CollectionUtils.isEmpty(unProcessedRelMap.values())) {
                    continue;
                }
                for (SdpDataSourceMappingRel xSdpDataSourceMappingRel : unProcessedRelMap.values()) {
                    if (!DataSourceType.HIVE.getType().equals(xSdpDataSourceMappingRel.getProdDataSourceType())) {
                        continue;
                    }

                    //hive在etl_content，所以是没有dataSourceName
                    String uatPriKey = Joiner.on("_").useForNull("").join(changeEnvBO.getFileId(), flinkTableAndMetaTableMap.get(flinkTableName), "");
                    SdpDataSourceMappingRel sdpDataSourceMappingRel = newUatRelMap.get(uatPriKey);
                    logger.info("etl_content获取的hive关系: {} -> {}",uatPriKey,Objects.nonNull(sdpDataSourceMappingRel)?JSON.toJSONString(sdpDataSourceMappingRel):"null");
                    if (Objects.nonNull(sdpDataSourceMappingRel)) {
                        //获取生产的配置信息,创建生产的元表
                        try {
                            EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.PROD.getCode());


                            SdpDataSource sdpDataSource = queryProdDataSource(sdpFile.getProjectId(), sdpDataSourceMappingRel);
                            if(Objects.nonNull(sdpDataSource)){
                                SdpFile mProdFile = fileService.get(mProdFileId);
                                String mEtlContent = mProdFile.getEtlContent();
                                if(StrUtil.isNotBlank(mEtlContent)){
                                    String mHivePath = StrUtils.CRLF + "  'hive-conf-dir' = '" + sdpDataSource.getDataSourceUrl() + "'";
                                    if (StrUtil.isNotBlank(hivePathPattern)) {
                                        mEtlContent = ReUtil.replaceAll(mEtlContent, hivePathPattern, mHivePath);
                                    }

                                    String mHadoopPath = StrUtils.CRLF +  "  'hadoop-conf-dir' = '" + sdpDataSource.getHadoopConfDir() + "'";
                                    if (StrUtil.isNotBlank(hadoopPathPattern)) {
                                        mEtlContent =  ReUtil.replaceAll(mEtlContent,hadoopPathPattern,mHadoopPath);
                                    }

                                    mProdFile.setEtlContent(mEtlContent);
                                    mProdFile.setContent(String.format(SQL_CONTENT, mProdFile.getMetaTableContent(), mEtlContent));
                                    fileService.updateSelective(mProdFile);
                                }
                            }
                        } finally {
                            EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.UAT.getCode());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.info("处理hive建表语句写在etl_content异常", e);
            throw e;
        }


    }

    /**
     * 生产环境创建和uat同等的目录
     * @param fileId
     * @return
     */
    private Long mkFolders4Prod(Long fileId) {
        //挂作业文件夹id
        Long lastFolderId = 0L;
        String queryFullPath = fileService.queryFullPath(fileId);
        if (StrUtil.isNotBlank(queryFullPath)) {
            SdpFile sdpFile = fileService.get(fileId);
            List<String> folders = Splitter.on("/").trimResults().omitEmptyStrings().splitToList(queryFullPath);
            Long parentId = 0L;
            for (int i = 0; i < folders.size(); i++) {
                if (0 == i && "作业开发".equals(folders.get(0))) {
                    continue;
                }

                try {
                    //切生产处理
                    EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.PROD.getCode());
                    List<SdpFolder> sdpFolders = folderService.getMapper().getFolderByName(sdpFile.getProjectId(), folders.get(i), parentId);
                    if (CollectionUtils.isNotEmpty(sdpFolders)) {
                        //存在
                        parentId = sdpFolders.get(0).getId();
                        lastFolderId = sdpFolders.get(0).getId();
                    } else {
                        SdpFolder sdpFolder = new SdpFolder();
                        sdpFolder.setParentId(parentId);
                        sdpFolder.setFolderName(folders.get(i));
                        sdpFolder.setProjectId(sdpFile.getProjectId());
                        folderService.insertSelective(sdpFolder);
                        parentId = sdpFolder.getId();
                        lastFolderId = sdpFolder.getId();
                    }
                } finally {
                    //再切回来
                    EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.UAT.getCode());
                }
            }
        }
        return lastFolderId;
    }

    public SdpDataSource queryProdDataSource(Long projectId,SdpDataSourceMappingRel rel){
        if(Objects.nonNull(rel.getProdDataSourceId())){
            SdpDataSource sdpDataSource = dataSourceService.get(rel.getProdDataSourceId());
            if(Objects.nonNull(sdpDataSource)){
                rel.setProdDataSourceId(sdpDataSource.getId());
                rel.setProdDataSourceName(sdpDataSource.getDataSourceName());
                rel.setProdDataSourceUrl(sdpDataSource.getDataSourceUrl());
                rel.setProdDataSourceType(sdpDataSource.getDataSourceType());
            }
            return  sdpDataSource;
        }

        SdpDataSource mSdpDataSource = new DataSourceResp();
        mSdpDataSource.setProjectId(projectId);
        mSdpDataSource.setDataSourceName(rel.getProdDataSourceName());
        mSdpDataSource.setEnabledFlag(EnableType.ENABLE.getCode());
        List<SdpDataSource> dataSourceList = dataSourceService.selectAll(mSdpDataSource);
        logger.info("查询生产数据源信息: {} -> {}",JSON.toJSONString(rel),CollectionUtils.isNotEmpty(dataSourceList)?JSON.toJSONString(dataSourceList):"null");
        if(CollectionUtils.isNotEmpty(dataSourceList)){
            SdpDataSource sdpDataSource = dataSourceList.get(0);
            rel.setProdDataSourceId(sdpDataSource.getId());
            rel.setProdDataSourceName(sdpDataSource.getDataSourceName());
            rel.setProdDataSourceUrl(sdpDataSource.getDataSourceUrl());
            rel.setProdDataSourceType(sdpDataSource.getDataSourceType());
            return sdpDataSource;
        }else {
            return null;
        }
    }

    /**
     * 根据uat名称去查生产数据源实例信息
     * @param projectId
     * @param dataSourceName
     * @param dataSourceType
     * @return
     */
    private SdpDataSource queryProdDataSourceByUatDataSourceName(Long projectId,String dataSourceName, String dataSourceType) {
        SdpDataSource prodDataSource = null;
        if(Objects.nonNull(projectId) && StrUtil.isNotBlank(dataSourceName) && StrUtil.isNotBlank(dataSourceType)){
            try {
                EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.PROD.getCode());
                //根据uat名称去查询生产的同名实例
                SdpDataSource paramSds = new SdpDataSource();
                paramSds.setProjectId(projectId);
                paramSds.setDataSourceName(dataSourceName);
                paramSds.setDataSourceType(dataSourceType);
                paramSds.setEnabledFlag(EnableType.ENABLE.getCode());
                List<SdpDataSource> dataSourceList = dataSourceService.selectAll(paramSds);
                prodDataSource = CollectionUtils.isNotEmpty(dataSourceList)?dataSourceList.get(0):null;
            }finally {
                EnvHolder.clearEnvAndAddEnv(EnvironmentEnum.UAT.getCode());
            }
        }
        return prodDataSource;
    }
}