package com.chitu.bigdata.sdp.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.RuntimeUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chitu.bigdata.sdp.api.domain.ClusterInfo;
import com.chitu.bigdata.sdp.api.domain.DataStreamConfig;
import com.chitu.bigdata.sdp.api.domain.JobConfig;
import com.chitu.bigdata.sdp.api.enums.*;
import com.chitu.bigdata.sdp.api.model.*;
import com.chitu.bigdata.sdp.config.KubernetesClusterConfig;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.flink.common.conf.ConfigConst;
import com.chitu.bigdata.sdp.utils.HdfsUtils;
import com.xiaoleilu.hutool.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class JobImageService {

    @Autowired
    private KubernetesClusterConfig kubernetesConfig;

    @Autowired
    private FileService fileService;

    @Autowired
    private SdpConfig sdpConfig;

    @Autowired
    private EngineService engineService;

    @Autowired
    private JarService jarService;

    @Autowired
    private JobService jobService;

    @Autowired
    private FileVersionService fileVersionService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private RestTemplate restTemplate;


    /**
     * 校验是否需要构建镜像
     *
     * @param jobId
     * @param jobActionType
     * @param useLatest
     * @return
     */
    public boolean needBuildPushImage(Long jobId, String jobActionType, boolean useLatest) {
        SdpJob job = jobService.get(jobId);
        SdpFile file = fileService.get(job.getFileId());
        JobConfig jobConfig = JSONObject.parseObject(job.getConfigContent(), JobConfig.class);
        if (StrUtil.isEmpty(jobConfig.getFlinkVersion())) {
            jobConfig.setFlinkVersion(FlinkVersion.VERSION_114.getVersion());
        }
        Long jarId;
        if (FileType.DATA_STREAM.getType().equals(file.getFileType())) {
            DataStreamConfig dsConfig = JSON.parseObject(job.getDataStreamConfig(), DataStreamConfig.class);
            jarId = dsConfig.getJarId();
        } else {
            jarId = jobConfig.getJarId();
        }

        if (jarId == null) {
            log.info("sql任务不依赖udf，无需构建镜像...");
            return false;
        }

        SdpEngine engine = engineService.get(jobConfig.getEngineId());
        if (EngineTypeEnum.YARN.getType().equals(engine.getEngineType())) {
            log.info("部署集群类型是yarn，无需构建镜像...");
            return false;
        }

        if (JobAction.RECOVER.toString().equals(jobActionType) && !useLatest) {
            log.info("作业恢复的老版本, 无需构建镜像...");
            return false;
        }

        String flinkJobName = projectService.get(job.getProjectId()).getProjectCode() + "_" + job.getJobName();
        flinkJobName = flinkJobName.toLowerCase();
        String env = sdpConfig.getEnvFromEnvHolder(log);
        ClusterInfo clusterInfo = engineService.getClusterInfo(env, engine);
        String[] str = clusterInfo.getDockerHubAddress().split("/");
        String imageExistsUrl = "https://" + str[0] + "/api/v2.0/projects/" + str[1] + "/repositories/" + kubernetesConfig.getImagePrefix() + flinkJobName + "/artifacts/" + jobConfig.getFlinkVersion();
        log.info("校验镜像是否存在url: [{}]", imageExistsUrl);
        Map<String, Object> map = null;
        try {
            map = restTemplate.getForObject(imageExistsUrl, Map.class);
        } catch (HttpClientErrorException.NotFound e) {
            // 404表示仓库不存在该镜像
            log.info("jobId:{},仓库不存在该镜像:{}", jarId, imageExistsUrl);
            return true;
        }
        log.info("仓库存在该镜像，镜像更新时间返回值:{}", map.get("push_time"));
        // 存在进行判断
        DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        Date repDate = null;
        try {
            repDate = sdf.parse((String) map.get("push_time"));
        } catch (Exception e) {
            log.info("解析时间报错", e);
        }
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(repDate);
        // 由于时区问题 +8小时
        calendar.set(Calendar.HOUR, calendar.get(Calendar.HOUR) + 8);
        repDate = calendar.getTime();
        List<SdpVersion> sdpVersions = fileVersionService.selectAll(new SdpVersion(job.getFileId(), job.getRunningVersion()));
        if (CollectionUtil.isNotEmpty(sdpVersions)) {
            SdpJar oldVersionJar = null;
            if (FileType.DATA_STREAM.getType().equals(file.getFileType())) {
                DataStreamConfig dsConfig = JSON.parseObject(sdpVersions.get(0).getDataStreamConfig(), DataStreamConfig.class);
                oldVersionJar = jarService.get(dsConfig.getJarId());
            } else {
                JobConfig oldVersionJobConfig = JSONObject.parseObject(sdpVersions.get(0).getConfigContent(), JobConfig.class);
                if (oldVersionJobConfig.getJarId() != null) {
                    oldVersionJar = jarService.get(oldVersionJobConfig.getJarId());
                }
            }
            SdpJar latestJar = jarService.get(jarId);
            log.info("运行版本jarId: [{}], 最新版本jarId: [{}]", Objects.nonNull(oldVersionJar) ? oldVersionJar.getId() : null, latestJar.getId());
            if (oldVersionJar != null && Objects.equals(oldVersionJar.getId(), latestJar.getId())) {
                log.info("imageCreateTime [{}], jarUpdationDate [{}]", repDate.toString(), latestJar.getUpdationDate());
                if (repDate.compareTo(latestJar.getUpdationDate()) > 0) {
                    log.info("job运行和最新版本的jar名称和版本一样 且 镜像更新时间大于jar更新时间, 无需构建镜像...");
                    return false;
                }
            }
        }
        return true;
    }


    /**
     * 构建推送镜像
     *
     * @param flinkJobName
     * @param flinkVersion
     * @param engine
     * @param sdpJar
     * @throws Exception
     */
    public void buildPushImage(String flinkJobName, String flinkVersion, SdpEngine engine, SdpJar sdpJar, String sqlContent) throws Exception {
        log.info("满足构建镜像条件, 开始构建镜像...");
        // 1.先下载jar到本地
        ClusterInfo clusterInfo = engineService.getClusterInfo(sdpConfig.getEnvFromEnvHolder(log), engine);
        String hadoopConf = clusterInfo.getHadoopConfDir();
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
        String sqlbaseImage = clusterInfo.getDockerHubAddress() + kubernetesConfig.getSqlBaseImage() + ":" + flinkVersion + esVersion;
        String jarHdfsPath = ConfigConst.SDP_HDFS_WORKSPACE_DEFAULT() + "/workspace" + sdpJar.getUrl() + "/" + sdpJar.getName();
        String jarLocalPath = clusterInfo.getDockerFileWorkspace() + jarHdfsPath;
        log.info("hadoopConf [{}], jarHdfsPath [{}], jarLocalPath [{}]", hadoopConf, jarHdfsPath, jarLocalPath);
        HdfsUtils.copyToLocalFile(hadoopConf, jarHdfsPath, jarLocalPath);

        // 2.生成 DockerFile文件
        String dockerFileName = kubernetesConfig.getDockerFilePrefix() + flinkJobName;
        String dockerFilePath = clusterInfo.getDockerFileWorkspace() + "/" + dockerFileName;
        File outputFile = new File(dockerFilePath);
        String dockerFileContent = String.format(
                "FROM %s\nCOPY %s $FLINK_HOME/usrlib/",
                sqlbaseImage,
                StrUtil.removePrefix(jarHdfsPath, "/"));
        log.info("dockerFileContent [{}]", dockerFileContent);
        log.info("dockerFilePath [{}]", dockerFilePath);
        FileUtils.write(outputFile, dockerFileContent, "UTF-8");


        // 3.构建推送镜像
        BufferedReader reader = null;
        InputStreamReader inputStreamReader = null;
        Process exec = null;
        try {
            String imageTag = clusterInfo.getDockerHubAddress() + kubernetesConfig.getImagePrefix() + flinkJobName.toLowerCase() + ":" + flinkVersion;
            String dockerBuildCmd = String.format(
                    "cd %s;docker build -t %s -f %s . --no-cache;docker push %s",
                    clusterInfo.getDockerFileWorkspace(),
                    imageTag,
                    dockerFileName,
                    imageTag);
            log.info("dockerBuildCmd[{}]", dockerBuildCmd);
            long starTime = System.currentTimeMillis();
            exec = RuntimeUtil.exec("/bin/sh", "-c", dockerBuildCmd);
            inputStreamReader = new InputStreamReader(exec.getInputStream());
            reader = new BufferedReader(inputStreamReader);
            StringBuilder cmdLogInfo = new StringBuilder();
            String readLine = "";
            while ((readLine = reader.readLine()) != null) {
                cmdLogInfo.append(readLine+"\n");
            }
            log.info("镜像构建推送日志[{}]",cmdLogInfo);
            boolean execFlag = exec.waitFor(kubernetesConfig.getBuildImageTimeout(), TimeUnit.SECONDS);
            if (!execFlag) {
                throw new RuntimeException("执行构建推送镜像命令失败");
            }
            log.info("镜像构建推送成功, 耗时[{}]", (System.currentTimeMillis() - starTime));
        } finally {
            // 4.删除本地文件，避免占用磁盘空间
            try {
                if(reader != null){
                    reader.close();
                }
                if(inputStreamReader != null){
                    inputStreamReader.close();
                }
                if (exec != null) {
                    exec.destroy();
                }
                FileUtil.del(dockerFilePath);
                FileUtil.del(jarLocalPath);
            } catch (Exception e) {
                log.error("删除文件失败", e);
            }
        }

    }

    /**
     * 删除镜像
     *
     * @param list
     */
    public void deleteImage(List<SdpJob> list) {
        for (SdpJob job : list) {
            try {
                JobConfig jobConfig = JSONObject.parseObject(job.getConfigContent(), JobConfig.class);
                if (StrUtil.isEmpty(jobConfig.getFlinkVersion())) {
                    jobConfig.setFlinkVersion(FlinkVersion.VERSION_114.getVersion());
                }
                String flinkJobName = projectService.get(job.getProjectId()).getProjectCode() + "_" + job.getJobName();
                flinkJobName = flinkJobName.toLowerCase();
                SdpEngine engine = engineService.get(jobConfig.getEngineId());
                ClusterInfo clusterInfo = engineService.getClusterInfo(sdpConfig.getEnvFromEnvHolder(log), engine);
//                ClusterInfo clusterInfo = kubernetesConfig.getEnvMap().get(sdpConfig.getEnvFromEnvHolder(log)).getClusters().get(0);
                String imageTag = clusterInfo.getDockerHubAddress() + kubernetesConfig.getImagePrefix() + flinkJobName + ":" + jobConfig.getFlinkVersion();
                String imageExistsCmd = "docker image ls " + imageTag;
                log.info("校验镜像是否存在命令: [{}]", imageExistsCmd);
                String imageExistsResult = RuntimeUtil.execForStr("/bin/sh", "-c", imageExistsCmd);
                log.info("镜像是否存在返回值: [{}]", imageExistsResult);
                // 镜像存在则删除
                if (imageExistsResult.contains(flinkJobName)) {
                    String dockerBuildCmd = String.format("docker rmi %s", imageTag);
                    log.info("dockerBuildCmd[{}]", dockerBuildCmd);
                    String imageDeleteResult = RuntimeUtil.execForStr("/bin/sh", "-c", dockerBuildCmd);
                    log.info("删除镜像返回值: [{}]", imageDeleteResult);
                    if (imageDeleteResult.contains("Error")) {
                        log.error("删除镜像失败:{}", imageDeleteResult);
                    } else {
                        log.info("镜像删除成功");
                    }
                }
            } catch (Exception e) {
                log.error("===删除镜像异常 jobId:{}===", job.getId(), e);
            }
        }
    }

    /**
     * 判断是否需要重新构建镜像(基础镜像有变动的情况)
     *
     * @param jobId
     * @return
     */
    public boolean needRebuildPushImage(Long jobId) {
        SdpJob job = jobService.get(jobId);
        SdpFile file = fileService.get(job.getFileId());
        JobConfig jobConfig = JSONObject.parseObject(job.getConfigContent(), JobConfig.class);
        if (StrUtil.isEmpty(jobConfig.getFlinkVersion())) {
            jobConfig.setFlinkVersion(FlinkVersion.VERSION_114.getVersion());
        }
        Long jarId;
        if (FileType.DATA_STREAM.getType().equals(file.getFileType())) {
            DataStreamConfig dsConfig = JSON.parseObject(job.getDataStreamConfig(), DataStreamConfig.class);
            jarId = dsConfig.getJarId();
        } else {
            jarId = jobConfig.getJarId();
        }

        if (jarId == null) {
            log.info("sql任务不依赖udf，无需构建镜像...");
            return false;
        }

        SdpEngine engine = engineService.get(jobConfig.getEngineId());
        if (EngineTypeEnum.YARN.getType().equals(engine.getEngineType())) {
            log.info("部署集群类型是yarn，无需构建镜像...");
            return false;
        }
        return true;
    }

    /**
     * 重新构建镜像(基础镜像有变动的情况)
     *
     * @param jobId
     */
    public void rebuildPushImage(Long jobId) {
        SdpJob sdpJob = jobService.get(jobId);
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

        try {
            buildPushImage(flinkJobName, jobConfig.getFlinkVersion(), engine, sdpJar, file.getContent());
        } catch (Exception e) {
            log.error("=== rebuildPushImage构建镜像失败 ===", e);
            throw new RuntimeException(e);
        }
    }

}
