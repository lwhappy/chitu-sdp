

/**
 * <pre>
 * 作   者：CHENYUN
 * 创建日期：2021-10-15
 * </pre>
 */

package com.chitu.bigdata.sdp.service;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.chitu.bigdata.sdp.api.bo.SdpVersion4BO;
import com.chitu.bigdata.sdp.api.bo.SdpVersionBO;
import com.chitu.bigdata.sdp.api.domain.DataStreamConfig;
import com.chitu.bigdata.sdp.api.domain.JobConfig;
import com.chitu.bigdata.sdp.api.enums.CompareSource;
import com.chitu.bigdata.sdp.api.enums.FileStatus;
import com.chitu.bigdata.sdp.api.enums.FileType;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.*;
import com.chitu.bigdata.sdp.flink.common.util.DeflaterUtils;
import com.chitu.bigdata.sdp.flink.common.util.PropertiesUtils;
import com.chitu.bigdata.sdp.mapper.*;
import com.chitu.bigdata.sdp.utils.PaginationUtils;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.chitu.bigdata.sdp.utils.VersionUtil;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.model.Pagination;
import com.chitu.cloud.service.GenericService;
import com.chitu.cloud.utils.Context;
import com.chitu.cloud.utils.ContextUtils;
import com.chitu.cloud.utils.StringUtils;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import scala.collection.JavaConversions;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 587694
 * @description: TODO
 * @date 2021/10/17 14:06
 */
@Service
public class FileVersionService extends GenericService<SdpVersion, Long> {
    public FileVersionService(@Autowired SdpVersionMapper sdpVersionMapper) {
        super(sdpVersionMapper);
    }

    public SdpVersionMapper getMapper() {
        return (SdpVersionMapper) super.genericMapper;
    }


    @Autowired
    private SdpFileMapper sdpFileMapper;
    @Autowired
    FileService fileService;
    @Autowired
    private SdpJobMapper jobMapper;

    @Autowired
    private MetaTableConfigService metaTableConfigService;

    @Autowired
    private SdpApproveMapper sdpApproveMapper;

    @Autowired
    SdpVersionMapper sdpVersionMapper;

    @Autowired
    private SdpJarMapper jarMapper;

    private static final String SQL_CONTENT = "%s\r\n%s";


    public List<SdpVersion> compareVersion(SdpVersionBO versionBO) {
        List<SdpVersion> result = new ArrayList();
        String compareSource = versionBO.getCompareSource();
        if(StringUtils.isEmpty(compareSource)){
            throw new ApplicationException(ResponseCode.PARAM_IS_MISSING,"compareSource");
        }
        switch (CompareSource.getCompareName(compareSource)){
            case FILE_COMPARE:
                compareFile(versionBO, result);
                break;
            case JOB_COMPARE:
                compareJob(versionBO, result);
                break;
            case APPROVE_COMPARE:
                compareApproveJob(versionBO, result);
                break;
            default:
                throw new ApplicationException(ResponseCode.INVALID_ARGUMENT,versionBO.getCompareSource());
        }
        return result;
    }

    private void compareApproveJob(SdpVersionBO versionBO, List<SdpVersion> result) {
//        Long id = versionBO.getId();
//        SdpApprove sdpApprove = sdpApproveMapper.get(id);
//        Long fileId = sdpApprove.getFileId();
//        SdpFile sdpFile = sdpFileMapper.get(fileId);
        Long fileId = versionBO.getId();
        SdpFile sdpFile = sdpFileMapper.selectById(fileId);
        SdpVersion runningVersion = new SdpVersion();
        getRunningVersion(sdpFile, runningVersion);
        SdpVersion sv = sdpVersionMapper.selectLastVersion(fileId);
        result.add(runningVersion);
        if (Objects.nonNull(sv)) {
            getLastVersion(sv);
            //重新封装sqlContent，防止敏感信息泄漏
            packageFileContent(sv);
            result.add(sv);
        }
    }

    private void getLastVersion(SdpVersion sv) {
        if (Objects.nonNull(sv.getConfigContent())) {
            String configContent = sv.getConfigContent();
            configContent = handleYaml(configContent);
            String pretty = formatJson(configContent);
            sv.setConfigContent(pretty);
        }
        if (Objects.nonNull(sv.getSourceContent())) {
            String sourceContent = sv.getSourceContent();
            String sc = formatJson(sourceContent);
            sv.setSourceContent(sc);
        }
        if (Objects.nonNull(sv.getDataStreamConfig())) {
            String dataStreamConfig = sv.getDataStreamConfig();
            DataStreamConfig streamConfig = JSON.parseObject(dataStreamConfig, DataStreamConfig.class);
            SdpJar sdpJar = jarMapper.selectById(streamConfig.getJarId());
            streamConfig.setDescription(sdpJar.getDescription() == null ? "" : sdpJar.getDescription());
            streamConfig.setGit(sdpJar.getGit());
            streamConfig.setUrl(sdpJar.getUrl());
            streamConfig.setJarId(null);
            dataStreamConfig = JSON.toJSONString(streamConfig);
            String json = formatJson(dataStreamConfig);
            sv.setDataStreamConfig(json);
        }
    }

    public void getRunningVersion(SdpFile sdpFile, SdpVersion runningVersion) {
        if (sdpFile != null) {
            if (Objects.nonNull(sdpFile.getConfigContent())) {
                String configContent1 = sdpFile.getConfigContent();
                configContent1 = handleYaml(configContent1);
                String cc = formatJson(configContent1);
                sdpFile.setConfigContent(cc);
            }
            if (Objects.nonNull(sdpFile.getSourceContent())) {
                String sourceContent1 = sdpFile.getSourceContent();
                String scc = formatJson(sourceContent1);
                sdpFile.setSourceContent(scc);
            }
            if (Objects.nonNull(sdpFile.getDataStreamConfig())) {
                String dataStreamConfig = sdpFile.getDataStreamConfig();
                DataStreamConfig streamConfig = JSON.parseObject(dataStreamConfig, DataStreamConfig.class);
                SdpJar sdpJar = jarMapper.selectById(streamConfig.getJarId());
                if(sdpJar != null){
                    streamConfig.setDescription(sdpJar.getDescription() == null ? "" : sdpJar.getDescription());
                    streamConfig.setGit(sdpJar.getGit());
                    streamConfig.setUrl(sdpJar.getUrl());
                    streamConfig.setJarId(null);
                    String streamStr = JSON.toJSONString(streamConfig);
                    String formatJson = formatJson(streamStr);
                    sdpFile.setDataStreamConfig(formatJson);
                }
            }
            BeanUtils.copyProperties(sdpFile, runningVersion);
            runningVersion.setFileName(sdpFile.getFileName());
            runningVersion.setConfigContent(sdpFile.getConfigContent());
            runningVersion.setFileContent(sdpFile.getContent());
            runningVersion.setSourceContent(sdpFile.getSourceContent());
        }
    }


    private void compareJob(SdpVersionBO versionBO, List result) {
        Long id = versionBO.getId();
        SdpJob sdpJob = jobMapper.selectById(id);
        String runningVersion = sdpJob.getRunningVersion();
        String latestVersion = sdpJob.getLatestVersion();
        Long fileId = sdpJob.getFileId();
        if (Objects.isNull(latestVersion) || StringUtils.isEmpty(latestVersion)) {
            throw new ApplicationException(ResponseCode.VERSION_IS_NULL);
        }
        if (Objects.isNull(runningVersion) || StringUtils.isEmpty(runningVersion)){
            runningVersion = VersionUtil.fallbackVersion(latestVersion);
        }
        SdpVersion sdpVersion = new SdpVersion();
        sdpVersion.setFileId(fileId);
        sdpVersion.setFileVersion(runningVersion);
        getVersionDetail(sdpVersion,result);
        sdpVersion.setFileVersion(latestVersion);
        getVersionDetail(sdpVersion,result);
    }

    private void getVersionDetail(SdpVersion sdpVersion,List result) {
        List<SdpVersion> currentVersionJobs = this.selectAll(sdpVersion);
        if (CollectionUtils.isEmpty(currentVersionJobs)) {
            throw new ApplicationException(ResponseCode.VERSION_NOT_EXIST);
        }
        SdpVersion currentVersionJob = currentVersionJobs.get(0);
        //格式化json数据
        getLastVersion(currentVersionJob);
        //重新封装sqlContent，防止敏感信息泄漏
        packageFileContent(currentVersionJob);
        result.add(currentVersionJob);
    }

    private void packageFileContent(SdpVersion currentVersionJob) {
        String metaTableContent = currentVersionJob.getMetaTableContent();
        String etlContent = currentVersionJob.getEtlContent();
        currentVersionJob.setFileContent(String.format(SQL_CONTENT, metaTableContent == null ? StrUtils.SPACE : metaTableContent,
                etlContent == null ? StrUtils.SPACE : etlContent));
    }

    /**
     * 格式化json数据
     * @param configContent
     * @return
     */
    private String formatJson(String configContent) {
        JSONObject object = JSONObject.parseObject(configContent);
        String pretty = JSON.toJSONString(object, SerializerFeature.PrettyFormat, SerializerFeature.WriteMapNullValue,
                SerializerFeature.WriteDateUseDateFormat,SerializerFeature.SortField,SerializerFeature.MapSortField);
        return pretty;
    }

    private void compareFile(SdpVersionBO versionBO, List result) {
        SdpVersion currentVersion = this.get(versionBO.getId());
        getLastVersion(currentVersion);
        //对fileContent重新构建，防止敏感信息出现
        packageFileContent(currentVersion);
        SdpFile sdpFile = sdpFileMapper.selectById(currentVersion.getFileId());
        SdpVersion runningVersion = new SdpVersion();
        getRunningVersion(sdpFile, runningVersion);
        result.add(runningVersion);
        result.add(currentVersion);

    }


    private String handleYaml(String configContent1) {
        LinkedHashMap<String, String> options = new LinkedHashMap<>();
        JobConfig jobConfig = JSON.parseObject(configContent1, JobConfig.class);
        if(Objects.nonNull(jobConfig.getFlinkYaml()) && StringUtils.isNotEmpty(jobConfig.getFlinkYaml())) {
            try {
                parseYaml(options, jobConfig);
            } catch (Exception e) {
                options.put("error","yaml is empty");
            }
            jobConfig.setFlinkYamlMap(options);
            jobConfig.setFlinkYaml(null);
            configContent1 = JSON.toJSONString(jobConfig);
        }
        return configContent1;
    }

    private void parseYaml( LinkedHashMap<String, String> options, JobConfig jobConfig) {
        scala.collection.immutable.Map<String, String> yamlText = PropertiesUtils.fromYamlText(DeflaterUtils.unzipString(jobConfig.getFlinkYaml()));
        Map<String,String> yamlMap = JavaConversions.mapAsJavaMap(yamlText);
        options.putAll(yamlMap);
    }
    public Integer rollbackVersion(SdpVersionBO versionBO) {
        Context context = ContextUtils.get();
        SdpVersion currentVersion = this.get(versionBO.getId());
//        SdpJob job = new SdpJob();
        Long fileId = currentVersion.getFileId();
        SdpFile sdpFile = sdpFileMapper.selectById(fileId);
        if(sdpFile != null){
            sdpFile.setSourceContent(currentVersion.getSourceContent());
            sdpFile.setConfigContent(currentVersion.getConfigContent());
            sdpFile.setDataStreamConfig(currentVersion.getDataStreamConfig());
            //重新封装sqlContent，防止敏感信息泄漏
            packageContent(currentVersion, sdpFile);
            sdpFile.setEtlContent(currentVersion.getEtlContent());
            sdpFile.setMetaTableContent(currentVersion.getMetaTableContent());
            sdpFile.setFileStatus(FileStatus.PASS.name());
            sdpFile.setLockedBy(context.getUserId());
            sdpFile.setUpdatedBy(context.getUserId());
        }
        //进行元表还原
        if (Objects.nonNull(currentVersion.getMetaTableJson())) {
            String metaTableJson = currentVersion.getMetaTableJson();
            List<SdpMetaTableConfig> sdpMetaTableConfigs = JSON.parseObject(metaTableJson, new TypeReference<List<SdpMetaTableConfig>>() {
            });
            List<SdpMetaTableConfig> metaTableConfigs = sdpMetaTableConfigs.stream().map(x -> {
                x.setId(null);
                return x;
            }).collect(Collectors.toList());
            metaTableConfigService.addMetaTableConfig(metaTableConfigs);
        }
//        job.setFileId(currentVersion.getFileId());
//        job.setJobContent(currentVersion.getFileContent());
//        job.setConfigContent(currentVersion.getConfigContent());
//        job.setSourceContent(currentVersion.getSourceContent());
//        job.setRunningVersion(currentVersion.getFileVersion());
//        if(context != null){
//            job.setUpdatedBy(context.getUserId());
//        }
//        return jobMapper.rollback(job);
        return fileService.update(sdpFile);
    }

    private void packageContent(SdpVersion currentVersion, SdpFile sdpFile) {
        String metaTableContent = currentVersion.getMetaTableContent();
        String etlContent = currentVersion.getEtlContent();
        if(FileType.DATA_STREAM.getType().equals(sdpFile.getFileType())){
            sdpFile.setContent("");
        }else {
            sdpFile.setContent(String.format(SQL_CONTENT, metaTableContent == null ? StrUtils.SPACE : metaTableContent, etlContent == null ? StrUtils.SPACE : etlContent));
        }
    }


    public Pagination<SdpVersion> getVersions(SdpVersion4BO sdpVersion4BO) {
        if (Objects.isNull(sdpVersion4BO.getVo().getFileId()) || 0L == sdpVersion4BO.getVo().getFileId()){
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT,sdpVersion4BO.getVo().getFileId());
        }
        Pagination<SdpVersion> pagination = PaginationUtils.getInstance4BO(sdpVersion4BO);
        this.executePagination(p ->getMapper().getVersions(p),pagination);
        return pagination;
    }

    public Object deleteVersion(SdpVersionBO versionBO) {
        if (Objects.isNull(versionBO.getId()) || 0L == versionBO.getId()){
            throw new ApplicationException(ResponseCode.INVALID_ARGUMENT,versionBO.getId());
        }
      //需要判断是否是当前job存在的版本，是：不可以删除
//        SdpVersion sdpVersion = versionMapper.get(versionBO.getId());
//        String fileVersion = sdpVersion.getFileVersion();
//        Long fileId = sdpVersion.getFileId();
//        SdpJob sdpJob = jobMapper.queryJobByFileId(fileId);
//        //在job中的版本不可以删除
//        if (fileVersion.equals(sdpJob.getLatestVersion())){
//            throw new ApplicationException(ResponseCode.VERSION_WAIT_RUN);
//        }
//        if (fileVersion.equals(sdpJob.getRunningVersion())){
//            throw new ApplicationException(ResponseCode.JOB_VERSION_RUNNING);
//        }
        return this.disable(SdpVersion.class,versionBO.getId());
    }
}
