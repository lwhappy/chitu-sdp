

/**
 * <pre>
 * 作   者：CHENYUN
 * 创建日期：2021-11-8
 * </pre>
 */

package com.chitu.bigdata.sdp.service;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.core.util.StrUtil;
import com.chitu.bigdata.sdp.api.bo.SdpJarBO;
import com.chitu.bigdata.sdp.api.domain.ClusterInfo;
import com.chitu.bigdata.sdp.api.enums.EngineTypeEnum;
import com.chitu.bigdata.sdp.api.enums.JarAction;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.SdpJar;
import com.chitu.bigdata.sdp.api.model.SdpJob;
import com.chitu.bigdata.sdp.api.model.SdpProject;
import com.chitu.bigdata.sdp.config.ClusterInfoConfig;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.flink.common.conf.ConfigConst;
import com.chitu.bigdata.sdp.flink.common.util.HadoopUtils;
import com.chitu.bigdata.sdp.interceptor.EnvHolder;
import com.chitu.bigdata.sdp.mapper.SdpJarMapper;
import com.chitu.bigdata.sdp.mapper.SdpJobMapper;
import com.chitu.bigdata.sdp.mapper.SdpProjectMapper;
import com.chitu.bigdata.sdp.utils.HdfsUtils;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.chitu.bigdata.sdp.utils.VersionUtil;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.model.Pagination;
import com.chitu.cloud.service.GenericService;
import com.chitu.cloud.utils.StringUtils;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FSDataOutputStream;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.fs.permission.FsAction;
import org.apache.hadoop.fs.permission.FsPermission;
import org.apache.maven.model.Dependency;
import org.apache.maven.model.Model;
import org.apache.maven.model.io.xpp3.MavenXpp3Reader;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.CompletableFuture;
import java.util.jar.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;

/**
 * <pre>
 * jar管理业务类
 * </pre>
 */
@Service
@RefreshScope
@Slf4j
public class JarService extends GenericService<SdpJar, Long> {
    public JarService(@Autowired SdpJarMapper sdpJarMapper) {
        super(sdpJarMapper);
    }

    public SdpJarMapper getMapper() {
        return (SdpJarMapper) super.genericMapper;
    }

    private static final String JAR_NAME = "([A-Za-z0-9_\\-.)(])+";
    @Autowired
    private SdpJarMapper jarMapper;
    @Autowired
    private SdpJobMapper jobMapper;
    @Autowired
    private SdpProjectMapper projectMapper;
    @Autowired
    private FileService fileService;
    @Autowired
    private ClusterInfoConfig clusterInfoConfig;
    @Autowired
    SdpConfig sdpConfig;
    @Autowired
    EngineService engineService;

    private String validateJar(MultipartFile file, String name) {
        Pattern pattern = Pattern.compile(JAR_NAME);
        String fileName = file.getOriginalFilename();
        Matcher matcher = pattern.matcher(fileName);
        if (!matcher.matches()) {
            throw new ApplicationException(ResponseCode.JAR_NAME_ERROR);
        }
        if (!fileName.endsWith(".jar")) {
            throw new ApplicationException(ResponseCode.JAR_TYPE_ERROR);
        }
        if (StringUtils.isEmpty(name)) {
            throw new ApplicationException(ResponseCode.JAR_NAME_NULL);
        }
        Matcher matcher1 = pattern.matcher(name);
        if (!matcher1.matches()) {
            throw new ApplicationException(ResponseCode.JAR_NAME_ERROR);
        }
        return fileName;
    }

    private String assemblePath(Long projectId, String version, String name) {
        SdpProject project = projectMapper.selectById(projectId);
        String projectCode = project.getProjectCode();
        if (JarAction.ADD.name().equals(version)) {
            //首页新增jar
            version = VersionUtil.INIT_VERSION_PLUS;
        } else if (JarAction.UPDATE.name().equals(version)) {
            //首页对指定jar上传新版本，查询当前jar最新版本
            SdpJarBO jarBO = new SdpJarBO();
            jarBO.setName(name);
            jarBO.setProjectId(projectId);
            SdpJar jar = jarMapper.queryLatestJar(jarBO);
            String currVersion = "";
            if (null != jar) {
                currVersion = jar.getVersion();
            }
            version = VersionUtil.increaseVersion(currVersion);
        }//否则，是对明细页，历史jar进行更新

        String filePath = name;
        if (name.contains(".")) {
            int index = name.indexOf(".");
            String[] fileNames = name.split("\\.");
            if (fileNames.length > 2) {
                //说明此时jar文件名中包含.
                filePath = name.substring(0, index - 2);
            } else {
                filePath = name.substring(0, index);
            }
        }
        String udfPath = "/" + projectCode + "/udf/" + filePath + "/" + version;
        return udfPath;
    }

    public Boolean executeUpload(String uploadPath, MultipartFile file, String confDir) {
        Boolean result = true;
        FileSystem fs = null;
        FSDataOutputStream os = null;
        try {
            fs = HdfsUtils.getFileSystem(confDir);
            Path targetFile = new Path(uploadPath);
            os = fs.create(targetFile);
            os.write(file.getBytes());
            FsPermission permission = new FsPermission(FsAction.ALL, FsAction.ALL, FsAction.ALL);
            fs.setPermission(targetFile, permission);
        } catch (Exception e) {
            result = false;
            logger.error("上传jar包失败===" + e);
            e.printStackTrace();
        } finally {
            try {
                if (os != null) {
                    os.close();
                }
                if (fs != null) {
                    fs.close();
                }
            } catch (Exception e) {
                logger.error("上传jar文件关闭流异常==={}", e);
            }
        }
        return result;
    }

    public List<Boolean> uploadJarASync(List<String> hadoopConfDirs, String uploadPath, MultipartFile file) {
        return hadoopConfDirs
                .stream()
                .distinct()
                .map(confDir -> CompletableFuture.supplyAsync(() -> {
                    return executeUpload(uploadPath, file, confDir);
                }))
                .collect(Collectors.toList())
                .stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    public List<Boolean> uploadJarASync1(List<String> hadoopConfDirs, String uploadPath, MultipartFile file) {
        List<Boolean> result = new ArrayList<>();
        hadoopConfDirs.forEach(confDir -> {
            Boolean success = executeUpload(uploadPath, file, confDir);
            result.add(success);
        });
        return result;
    }

    public String uploadJar(MultipartFile file, Long projectId, String version, String name) {
        long start = System.currentTimeMillis();
        String udfPath;
        String fileName = validateJar(file, name);
        udfPath = assemblePath(projectId, version, name);
        String uploadPath = ConfigConst.SDP_HDFS_WORKSPACE_DEFAULT() + "/workspace" + udfPath + "/" + fileName;
        logger.info("上传jar hdfs路径===" + uploadPath);
        //上传HDFS,同时上传到多个环境中
        List<String> envList = sdpConfig.getEnvList();
        List<ClusterInfo> clusterInfos = Lists.newArrayList();
        for (String environment : envList) {
            List<ClusterInfo> mClusterInfos = engineService.getClusterInfos(environment, EngineTypeEnum.YARN.getType());
            if(!CollectionUtils.isEmpty(mClusterInfos)){
                clusterInfos.addAll(mClusterInfos);
            }
        }
        List<String> hadoopConfDirs = clusterInfos.stream().map(z -> z.getHadoopConfDir()).collect(Collectors.toList());
        List<Boolean> result = uploadJarASync(hadoopConfDirs, uploadPath, file);
        List<Boolean> result1 = result.stream().filter(x -> x == false).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(result1)) {
            throw new ApplicationException(ResponseCode.JAR_UPLOAD_FAILED);
        }
        logger.info("上传jar包耗时===" + (System.currentTimeMillis() - start));
        return udfPath;
    }

    public SdpJar addJar(MultipartFile file, Long projectId, String version, String name, String git, String description, String type) {
        synchronized (JarService.class) {
            SdpJar result = null;
            String name1 = name;
            String git1 = git;
            String description1 = description;
            try {
                name1 = URLDecoder.decode(name, "UTF-8");
                git1 = URLDecoder.decode(git, "UTF-8");
                description1 = URLDecoder.decode(description, "UTF-8");
            } catch (Exception e) {
                logger.error("字段解码异常");
            }
            if (JarAction.ADD.name().equals(type)) {
                SdpJarBO sdpJarBO = new SdpJarBO();
                sdpJarBO.setName(name1);
                sdpJarBO.setProjectId(projectId);
                List<SdpJar> sdpJars = jarMapper.searchJar(sdpJarBO);
                if (!CollectionUtils.isEmpty(sdpJars)) {
                    throw new ApplicationException(ResponseCode.JAR_ADD_FAILED.getCode(), ResponseCode.JAR_ADD_FAILED.getMessage());
                }
            }
            String udfPath = uploadJar(file, projectId, version, name1);
            if (StringUtils.isEmpty(udfPath)) {
                throw new ApplicationException(ResponseCode.JAR_UPLOAD_FAILED.getCode(), ResponseCode.JAR_UPLOAD_FAILED.getMessage());
            }

            String flinkVersion = null;
            try {
                // jar上传成功则获取jar flink版本
                String uploadPath = ConfigConst.SDP_HDFS_WORKSPACE_DEFAULT() + "/workspace" + udfPath + "/" + name1;
                log.info("jar path: [{}], jarName: [{}]", uploadPath, name1);
                flinkVersion = getJarFlinkVersion(uploadPath, name1);
                log.info("jarFlinkVersion: [{}]", flinkVersion);
            } catch (Exception e) {
                log.warn("获取jar flink版本异常", e);
            }


            SdpJar jar = new SdpJar();
            jar.setName(name1);
            jar.setGit(git1);
            jar.setUrl(udfPath);
            jar.setDescription(description1);
            jar.setProjectId(projectId);
            jar.setFlinkVersion(flinkVersion);
            if (JarAction.ADD.name().equals(type)) {
                //首页新增jar
                jar.setVersion(VersionUtil.INIT_VERSION_PLUS);
                insert(jar);
                result = jar;
            } else if (JarAction.UPDATE.name().equals(type)) {
                //首页更新jar
                int index = udfPath.lastIndexOf("/");
                String version1 = udfPath.substring(index + 1);
//                String[] paths = udfPath.split("/");
//                String version1 = paths[paths.length-2];
                jar.setVersion(version1);
                insert(jar);
                result = jar;
            } else {//明细页，对指定版本历史jar更新
                SdpJarBO jarBO = new SdpJarBO();
                jarBO.setName(name1);
                jarBO.setProjectId(projectId);
                jarBO.setVersion(version);
                SdpJar sdpJar = jarMapper.queryLatestJar(jarBO);
                if (null != sdpJar) {
                    sdpJar.setName(name1);
                    sdpJar.setGit(git1);
                    sdpJar.setUrl(udfPath);
                    sdpJar.setDescription(description1);
                    sdpJar.setProjectId(projectId);
                    sdpJar.setFlinkVersion(flinkVersion);
                    updateSelective(sdpJar);
                    result = sdpJar;
                }
            }
            return result;
        }
    }

    public String getJarFlinkVersion(String udfPath, String jarName) throws Exception {
        String flinkVersion = null;

        String env = sdpConfig.getEnvFromEnvHolder(log);

        List<ClusterInfo> clusterInfos = engineService.getClusterInfos(env, EngineTypeEnum.YARN.getType());
        List<String> hadoopConfDirs = clusterInfos.stream().map(z -> z.getHadoopConfDir()).collect(Collectors.toList());

        ClusterInfoConfig.YarnConfig yarnConfig = clusterInfoConfig.getEnvMap().get(env);
        String localFilePath = yarnConfig.getClusterSyncLocalDir() + "tmp/jar/" + jarName;
        // 1.将hdfs jar文件下载到本地
        HdfsUtils.copyToLocalFile(hadoopConfDirs.get(0), udfPath, localFilePath);

        if (!FileUtil.exist(localFilePath)) {
            return null;
        }

        // 2.获取符合规则的文件名
        JarFile jarFile = null;
        try {
            jarFile = new JarFile(localFilePath);
            List<String> fileNamelist = new ArrayList<>();
            Enumeration<JarEntry> en = jarFile.entries();
            while (en.hasMoreElements()) {
                JarEntry entry = en.nextElement();
                String fileName = entry.getName();
                if (fileName.startsWith("META-INF/maven") && fileName.endsWith("pom.xml")) {
                    fileNamelist.add(fileName);
                }
            }

            log.info("获取符合规则的文件名数量: {}", fileNamelist.size());
            long starTime = System.currentTimeMillis();
            // 3.遍历获取
            for (String fileName : fileNamelist) {
                JarEntry jarEntry = jarFile.getJarEntry(fileName);
                InputStream inputStream = null;
                try {
                    inputStream = jarFile.getInputStream(jarEntry);
                    MavenXpp3Reader reader = new MavenXpp3Reader();
                    Model model = reader.read(inputStream);
                    Properties properties = model.getProperties();
                    for (Dependency dependency : model.getDependencies()) {
                        if ("org.apache.flink".equals(dependency.getGroupId())) {
                            // 获取版本
                            Matcher m = Pattern.compile("\\$\\{(.*)}").matcher(dependency.getVersion());
                            if (m.find()) {
                                String propertyKey = m.group(1);
                                flinkVersion = properties.getProperty(propertyKey);
                            } else {
                                flinkVersion = dependency.getVersion();
                            }
                            long endTime = System.currentTimeMillis();
                            log.info("解析pom获取flink版本耗时: [{}ms]", (endTime - starTime));
                            break;
                        }
                    }
                } finally {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
                if (StrUtil.isNotEmpty(flinkVersion)) {
                    break;
                }
            }
        } finally {
            if (jarFile != null) {
                jarFile.close();
            }
            try {
                FileUtil.del(localFilePath);
            } catch (IORuntimeException e) {
                e.printStackTrace();
            }
        }


        return flinkVersion;
    }

    public Pagination<SdpJar> queryJar(SdpJarBO jarBO) {
        Pagination<SdpJar> pagination = Pagination.getInstance4BO(jarBO);
        this.executePagination(x -> jarMapper.queryJar(x), pagination);
        List<SdpJar> rows = pagination.getRows();
        if (!CollectionUtils.isEmpty(rows)) {
            Long projectId = rows.get(0).getProjectId();
            SdpProject sdpProject = projectMapper.selectById(projectId);
            rows.forEach(x -> {
                SdpJarBO jar = new SdpJarBO();
                BeanUtils.copyProperties(x, jar);
                List<SdpJob> jobs = referenceJobs(jar);
                if (!CollectionUtils.isEmpty(jobs)) {
                    x.setJobs(jobs.size());
                }
                String newVersion = VersionUtil.increaseVersion(x.getVersion());
                x.setNewVersion(newVersion);
                x.setForbidUdxUpdation(sdpProject.getForbidUdxUpdation());
            });
        }
        return pagination;
    }


    public List<SdpJar> searchJar(SdpJarBO jarBO) {
        jarBO.setName(StrUtils.changeWildcard(jarBO.getName()));
        return this.getMapper().searchJar(jarBO);
    }

    public Pagination<SdpJar> history(SdpJarBO jarBO) {
        Pagination<SdpJar> pagination = Pagination.getInstance4BO(jarBO);
        this.executePagination(x -> jarMapper.history(x), pagination);
        List<SdpJar> rows = pagination.getRows();
        if (!CollectionUtils.isEmpty(rows)) {
            rows.forEach(x -> {
                SdpJarBO jar = new SdpJarBO();
                BeanUtils.copyProperties(x, jar);
                List<SdpJob> jobs = referenceJobs(jar);
                if (!CollectionUtils.isEmpty(jobs)) {
                    x.setJobs(jobs.size());
                }
            });
        }
        return pagination;
    }

    public Integer deleteJar(SdpJarBO jarBO) {
        Integer result;
        //先检查是否有作业在引用
        String name = jarBO.getName();
        SdpJar jar = get(Long.valueOf(jarBO.getId()));
        BeanUtils.copyProperties(jar, jarBO);
        if (StringUtils.isNotEmpty(name)) {
            //首页删除
            jarBO.setVersion(null);
        }
        List<SdpJob> jobs = referenceJobs(jarBO);
        if (!CollectionUtils.isEmpty(jobs)) {
            throw new ApplicationException(ResponseCode.JAR_IS_USING.getCode(), ResponseCode.JAR_IS_USING.getMessage());
        }
        if (StringUtils.isNotEmpty(name)) {
            //首页删除
            result = this.getMapper().disableByName(name, jarBO.getProjectId());
        } else {
            result = disable(SdpJar.class,jar.getId());
        }
        return result;
    }

    public List<SdpJob> referenceJobs(SdpJarBO jarBO) {
        String headerEnv = sdpConfig.getEnvFromEnvHolder(null);

        List<SdpJob> allEnvJob = Lists.newArrayList();
        for (String env : sdpConfig.getEnvList()) {
            try {
                EnvHolder.clearEnvAndAddEnv(env);
                List<SdpJob> sdpJobs = jobMapper.queryReferenceJobs(jarBO);
                if(org.apache.commons.collections.CollectionUtils.isNotEmpty(sdpJobs)){
                    allEnvJob.addAll(sdpJobs);
                }
            }finally {
                EnvHolder.clearEnvAndAddEnv(headerEnv);
            }
        }
        return allEnvJob;
    }

    public PageInfo referenceJobs4Page(SdpJarBO jarBO) {
        PageHelper.startPage(jarBO.getPage(), jarBO.getPageSize());
        List<SdpJob> list = referenceJobs(jarBO);
        return new PageInfo<>(list);
    }

    public ResponseEntity download(SdpJarBO jarBO) throws Exception {
        ResponseEntity result = null;
        SdpJar sdpJar;
        String jarId = jarBO.getId();
        if (StringUtils.isNotEmpty(jarId)) {
            //详情页下载
            sdpJar = get(Long.valueOf(jarId));
        } else {
            //首页下载
            sdpJar = this.getMapper().queryLatestJar(jarBO);
        }
        if (sdpJar != null) {
            String tmpDir = "/tmp/";
            String fullPath = ConfigConst.SDP_HDFS_WORKSPACE_DEFAULT() + "/workspace" + sdpJar.getUrl();
            //兼容历史数据，需要带上name
            if (!fullPath.endsWith(".jar")) {
                fullPath += "/" + sdpJar.getName();
            }
            String jarPath;
            try {
                jarPath = HadoopUtils.downloadJar(fullPath, tmpDir);
            } catch (IOException e) {
                logger.error("从hdfs下载jar包到linux异常===" + e);
                throw new ApplicationException(ResponseCode.JAR_DOWNLOAD_ERROR.getCode(), e.getMessage());
            }
            if (jarPath != null) {
                FileSystemResource file = new FileSystemResource(jarPath);
                HttpHeaders headers = new HttpHeaders();
                headers.add("Cache-Control", "no-cache, no-store, must-revalidate");
                headers.add("Content-Disposition", String.format("attachment; filename=\"%s\"", file.getFilename()));
                headers.add("Pragma", "no-cache");
                headers.add("Expires", "0");
                result = ResponseEntity
                        .ok()
                        .headers(headers)
                        .contentLength(file.contentLength())
                        .contentType(MediaType.parseMediaType("application/java-archive"))
                        .body(new InputStreamResource(file.getInputStream()));
            }
        }
        return result;
    }

    public String download2(SdpJarBO jarBO, HttpServletResponse response) {
        String desPath = null;
        SdpJar sdpJar;
        String jarId = jarBO.getId();
        if (StringUtils.isNotEmpty(jarId)) {
            //详情页下载
            sdpJar = get(Long.valueOf(jarId));
        } else {
            //首页下载
            sdpJar = this.getMapper().queryLatestJar(jarBO);
        }
        if (sdpJar != null) {
            String tmpDir = "/tmp/";
            String fullPath = ConfigConst.SDP_HDFS_WORKSPACE_DEFAULT() + "/workspace" + sdpJar.getUrl();
            //兼容历史数据，需要带上name
            if (!fullPath.endsWith(".jar")) {
                fullPath += "/" + sdpJar.getName();
            }
            String jarPath;
            try {
                jarPath = HadoopUtils.downloadJar(fullPath, tmpDir);
            } catch (IOException e) {
                logger.error("从hdfs下载jar包到linux异常===" + e);
                throw new ApplicationException(ResponseCode.JAR_DOWNLOAD_ERROR.getCode(), e.getMessage());
            }
            if (jarPath != null) {
                byte[] buffer = new byte[1024];
                FileInputStream fis = null;
                BufferedInputStream bis = null;
                try {
                    response.reset();
                    String fileName = jarPath.substring(jarPath.lastIndexOf(File.separator) + 1);
                    String type = new MimetypesFileTypeMap().getContentType(fileName);
                    response.setContentType(type);
                    response.setCharacterEncoding("UTF-8");
                    response.setHeader("content-Type", type);
                    response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, "utf-8"));
                    //方案1
//                    desPath = "D:/" + fileName;
//                    File src = new File(jarPath);
//                    File des = new File(desPath);
//                    copyJar(src,des);

                    //方案2
                    File file = new File(jarPath);
                    fis = new FileInputStream(file);
                    bis = new BufferedInputStream(fis);
                    OutputStream os = response.getOutputStream();
                    int i = bis.read(buffer);
                    while (i != -1) {
                        os.write(buffer, 0, i);
                        i = bis.read(buffer);
                    }

                } catch (Exception e) {
                    logger.error("从linux下载jar文件到本地失败===" + e);
                    throw new ApplicationException(ResponseCode.JAR_DOWNLOAD_ERROR.getCode(), e.getMessage());
                } finally {
                    if (bis != null) {
                        try {
                            bis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    if (fis != null) {
                        try {
                            fis.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    //删除linux上的jar文件
                    String command = "rm -rf " + jarPath;
                    fileService.runCommand(command);
                }
            }
        }
        return desPath;
    }

    public void copyJar(File src, File des) {
        JarInputStream jarIn = null;
        JarOutputStream jarOut = null;
        try {
            jarIn = new JarInputStream(new BufferedInputStream(new FileInputStream(src)));
            Manifest manifest = jarIn.getManifest();
            if (manifest == null) {
                jarOut = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(des)));
            } else {
                jarOut = new JarOutputStream(new BufferedOutputStream(new FileOutputStream(des)), manifest);
            }

            byte[] bytes = new byte[1024];
            while (true) {
                ZipEntry entry = jarIn.getNextJarEntry();
                if (entry == null) break;
                jarOut.putNextEntry(entry);
                int len = jarIn.read(bytes, 0, bytes.length);
                while (len != -1) {
                    jarOut.write(bytes, 0, len);
                    len = jarIn.read(bytes, 0, bytes.length);
                }
            }
            jarOut.finish();
        } catch (Exception e) {
            logger.error("复制jar内容异常=====" + e);
            e.printStackTrace();
            throw new ApplicationException(ResponseCode.JAR_DOWNLOAD_ERROR.getCode(), e.getMessage());
        } finally {
            if (jarOut != null) {
                try {
                    jarOut.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (jarIn != null) {
                try {
                    jarIn.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public void syncJarFlinkVersion() {
        try {
            List<SdpJar> sdpJars = this.selectAll(new SdpJar());
            for (SdpJar sdpJar : sdpJars) {
                String uploadPath = ConfigConst.SDP_HDFS_WORKSPACE_DEFAULT() + "/workspace" + sdpJar.getUrl() + "/" + sdpJar.getName();
                try {
                    log.info("jar path: [{}], jarName: [{}]", uploadPath, sdpJar.getName());
                    String jarFlinkVersion = getJarFlinkVersion(uploadPath, sdpJar.getName());
                    log.info("jarId[{}], flink版本: [{}]", sdpJar.getId(), jarFlinkVersion);
                    sdpJar.setFlinkVersion(jarFlinkVersion);
                } catch (Exception e) {
                    log.warn("获取jar flink版本异常", e);
                }
            }
            int size = this.updateSelective(sdpJars.stream().toArray(SdpJar[]::new));
            log.info("updateSelective size: {}", size);
        } catch (Exception e) {
            log.warn("更新数据库异常", e);
        }

    }
}