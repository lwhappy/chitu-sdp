package com.chitu.bigdata.sdp.utils;

import cn.hutool.core.io.FileUtil;
import com.xiaoleilu.hutool.util.CollectionUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/13 15:58
 */
public class HdfsUtils {

    private static final Logger logger = LoggerFactory.getLogger(HdfsUtils.class);

    public static Map<String, Configuration> configurationMap = new ConcurrentHashMap<>();


    public static Configuration getConfiguration(String hadoopConfDir) {
        if (configurationMap.containsKey(hadoopConfDir)) {
            return configurationMap.get(hadoopConfDir);
        }
        Configuration conf = new Configuration();
        File hadoopConfFile = new File(hadoopConfDir);
        List<String> confNameList = Arrays.asList("core-site.xml", "hdfs-site.xml", "yarn-site.xml", "mapred-site.xml");
        List<File> files = Stream.of(hadoopConfFile.listFiles()).filter(item -> item.isFile() && confNameList.contains(item.getName())).collect(Collectors.toList());
        if (CollectionUtil.isNotEmpty(files)) {
            for (File f : files) {
                conf.addResource(new Path(f.getAbsolutePath()));
            }
            configurationMap.put(hadoopConfDir, conf);
        }
        return configurationMap.get(hadoopConfDir);
    }


    public static FileSystem getFileSystem(String hadoopConfDir) throws IOException {
        if (configurationMap.containsKey(hadoopConfDir)) {
            return FileSystem.get(configurationMap.get(hadoopConfDir));
        }
        FileSystem fileSystem = null;
        File hadoopConfFile = new File(hadoopConfDir);
        List<String> confNameList = Arrays.asList("core-site.xml", "hdfs-site.xml", "yarn-site.xml", "mapred-site.xml");
        List<File> files = Stream.of(hadoopConfFile.listFiles()).filter(item -> item.isFile() && confNameList.contains(item.getName())).collect(Collectors.toList());
        Configuration conf = new Configuration();
        if (CollectionUtil.isNotEmpty(files)) {
            for (File f : files) {
                conf.addResource(new Path(f.getAbsolutePath()));
            }

            configurationMap.put(hadoopConfDir, conf);

            //ClassLoaderUtils.loadResource(hadoopConfDir);

            if (StringUtils.isBlank(conf.get("hadoop.tmp.dir"))) {
                conf.set("hadoop.tmp.dir", "/tmp");
            }
            if (StringUtils.isBlank(conf.get("hbase.fs.tmp.dir"))) {
                conf.set("hbase.fs.tmp.dir", "/tmp");
            }
            // disable timeline service as we only query yarn app here.
            // Otherwise we may hit this kind of ERROR:
            // java.lang.ClassNotFoundException: com.sun.jersey.api.client.config.ClientConfig
            conf.set("yarn.timeline-service.enabled", "false");

            // 必须设置，否则会有缓存多线程同时操作获取的是同一个fileSystem实例
            conf.set("fs.hdfs.impl.disable.cache", "true");
            fileSystem = FileSystem.get(conf);

            logger.info("获取fileSystem实例：" + fileSystem);
        }
        return fileSystem;
    }


    public static List<FileStatus> getCheckPointPaths(String hadoopConfDir, String fileNamePrefix) {
        List<FileStatus> filterSortFiles = new ArrayList<>();
        FileSystem fileSystem = null;
        try {
            fileSystem = getFileSystem(hadoopConfDir);
            if (!fileSystem.exists(new Path(fileNamePrefix))) {
                logger.error("检查点路径前缀不存在：" + fileNamePrefix);
                return new ArrayList<>();
            }
            FileStatus[] fileStatuses = fileSystem.listStatus(new Path(fileNamePrefix));

            if (Objects.nonNull(fileStatuses) && fileStatuses.length > 0) {
                FileStatus[] itemFileStatuses = null;
                for (FileStatus fileStatus : fileStatuses) {

                    if (Objects.isNull(fileStatus) || !fileStatus.isDirectory()) {
                        continue;
                    }

                    if (!fileStatus.getPath().getName().contains("chk-")) {
                        continue;
                    }

                    itemFileStatuses = fileSystem.listStatus(fileStatus.getPath());
                    if (Objects.isNull(itemFileStatuses) || itemFileStatuses.length <= 0) {
                        continue;
                    }

                    List<FileStatus> metadata = Stream.of(itemFileStatuses).filter(f -> f.getPath().getName().contains("_metadata")).collect(Collectors.toList());
                    if (CollectionUtil.isEmpty(metadata)) {
                        continue;
                    }

                    filterSortFiles.add(fileStatus);
                }
            }
           /* filterSortFiles = Stream.of(fileStatuses)
                    .filter(item -> item.getPath().getName().contains("chk-"))
                    //.sorted(Comparator.comparing(FileStatus::getModificationTime).reversed())
                    .collect(Collectors.toList());*/
            Comparator<FileStatus> ageComparator = (o1, o2) -> Long.valueOf(o1.getPath().getName().split("chk-")[1]).compareTo(Long.valueOf(o2.getPath().getName().split("chk-")[1]));
            filterSortFiles.sort(ageComparator.reversed());
        } catch (IOException e) {
            logger.error("hdfs操作异常", e);
        } finally {
            if (fileSystem != null) {
                try {
                    fileSystem.close();
                } catch (IOException e) {
                    logger.error("hdfs关闭连接异常", e);
                }
            }
        }
        return filterSortFiles;
    }


    public static boolean delete(String hadoopConfDir, String path) throws Exception {
        FileSystem fileSystem = null;
        try {
            fileSystem = getFileSystem(hadoopConfDir);
            if (fileSystem.exists(new Path(path))) {
                return fileSystem.delete(new Path(path), true);
            }
        } finally {
            if (fileSystem != null) {
                try {
                    fileSystem.close();
                } catch (IOException e) {
                    logger.error("hdfs关闭连接异常", e);
                }
            }
        }
        return false;
    }


    /**
     * 下载hdfs文件/目录到本地
     *
     * @param hadoopConfDir
     * @param src
     * @param dst
     * @return
     * @throws Exception
     */
    public static void copyToLocalFile(String hadoopConfDir, String src, String dst) throws Exception {
        FileSystem fileSystem = null;
        // 删除本地文件或目录
        boolean delFlag = FileUtil.del(dst);
        // 删除失败
        if (!delFlag) {
            throw new Exception("删除本地文件失败");
        }
        try {
            fileSystem = getFileSystem(hadoopConfDir);
            boolean exists = fileSystem.exists(new Path(src));
            if (exists) {
                fileSystem.copyToLocalFile(false, new Path(src), new Path(dst), true);
            }
        } finally {
            if (fileSystem != null) {
                try {
                    fileSystem.close();
                } catch (IOException e) {
                    logger.error("hdfs关闭连接异常", e);
                }
            }
        }
    }


    /**
     * 将本地目录或文件上传的hdfs
     *
     * @param localSrc
     * @param dst
     * @throws Exception
     */
    public static void uploadFile(String hadoopConfDir, String localSrc, String dst) throws Exception {
        FileSystem fileSystem = null;
        try {
            File srcFile = new File(localSrc);
            if (srcFile.exists()) {
                fileSystem = getFileSystem(hadoopConfDir);
                if (srcFile.isDirectory()) {
                    copyDirectory(localSrc, dst, fileSystem);
                } else {
                    copyFile(localSrc, dst, fileSystem);
                }
            }
        } finally {
            if (fileSystem != null) {
                try {
                    fileSystem.close();
                } catch (IOException e) {
                    logger.error("hdfs关闭连接异常", e);
                }
            }
        }
    }


    /**
     * 拷贝本地目录到hdfs
     *
     * @param src
     * @param dst
     * @param fs
     * @return
     * @throws Exception
     */
    private static boolean copyDirectory(String src, String dst, FileSystem fs) throws Exception {
        Path path = new Path(dst);
        if (!fs.exists(path)) {
            fs.mkdirs(path);
        }
        File file = new File(src);
        File[] files = file.listFiles();
        for (int i = 0; i < files.length; i++) {
            File f = files[i];
            if (f.isDirectory()) {
                String fname = f.getName();
                if (dst.endsWith("/")) {
                    copyDirectory(f.getPath(), dst + fname + "/", fs);
                } else {
                    copyDirectory(f.getPath(), dst + "/" + fname + "/", fs);
                }
            } else {
                copyFile(f.getPath(), dst, fs);
            }
        }
        return true;
    }


    /**
     * 拷贝本地文件到hdfs目录下
     *
     * @param localSrc
     * @param dst
     * @param fs
     * @return
     * @throws Exception
     */
    private static boolean copyFile(String localSrc, String dst, FileSystem fs) throws Exception {
        File file = new File(localSrc);
        dst = dst + file.getName();
        //Path path = new Path(dst);
        //boolean exists = fs.exists(path);
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        OutputStream out = fs.create(new Path(dst));
        IOUtils.copyBytes(in, out, 4096, true);
        return true;
    }


}
