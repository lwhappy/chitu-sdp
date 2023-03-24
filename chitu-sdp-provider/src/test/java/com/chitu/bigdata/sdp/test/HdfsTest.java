package com.chitu.bigdata.sdp.test;


import com.chitu.bigdata.sdp.flink.common.util.HadoopUtils;
import com.chitu.bigdata.sdp.utils.HdfsUtils;
import com.chitu.bigdata.sdp.utils.TripleDesCipher;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.IOUtils;

import java.io.*;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class HdfsTest {

    public static void main(String[] args) throws Exception {


        HdfsUtils.copyToLocalFile("D:\\opt\\apache\\hadoop-3.1.1\\etc\\hadoop", "/sdp/ck/ff887a149ecfec74b307cc2d418a05b7", "D:\\app");

        List<FileStatus> checkPointPaths = HdfsUtils.getCheckPointPaths("D:\\opt\\apache\\hadoop-3.1.1\\etc\\hadoop","/sdp/ck/ff887a149ecfec74b307cc2d418a05b7");

        Configuration conf = HdfsUtils.getConfiguration("D:\\opt\\apache\\hadoop-3.1.1\\etc\\hadoop");
        String s1 = conf.get(FileSystem.FS_DEFAULT_NAME_KEY);
        Map map = new HashMap();


        System.setProperty("HADOOP_USER_NAME", "admin");


        String host = new URI("http://bigdata-sdp-flink-submit-114").getHost();

        //List<FileStatus> checkPointPaths = HdfsUtils.getCheckPointPaths("D:\\module\\hadoop-3.1.1\\etc\\hadoop", "/sdp/ck/8bec99d0ef7325a68e3ace90fbfd835f");

        System.out.println(checkPointPaths);


        String s = "\n" +
                "\n" +
                "\n" +
                "insert into sink_mysql(f_sequence,f_random,f_random_str,ts) select * from datagen_source";

        LineNumberReader lineNumberReader = new LineNumberReader(new StringReader(s));
        lineNumberReader.skip(Long.MAX_VALUE);
        int line1 = lineNumberReader.getLineNumber();

        System.out.println("");



        // 先下载到本地
        //HdfsUtils.copyToLocalFile("D:\\module\\hadoop-3.1.1\\etc\\hadoop", "hdfs://bigbigworld/sdp/ck/ff84934d79fc59407b536055f27e5ea8", "/sdp/ck/ff84934d79fc59407b536055f27e5ea8");

        // 删除目标集群目录
        //HdfsUtils.delete("D:\\module\\hadoop01\\hadoop-3.1.1\\etc\\hadoop", "hdfs://bigbigworld/sdp/ck/ff84934d79fc59407b536055f27e5ea8");

        // 上传
        HdfsUtils.uploadFile("D:\\module\\hadoop01\\hadoop-3.1.1\\etc\\hadoop","/sdp/ck/ff84934d79fc59407b536055f27e5ea9","hdfs://bigbigworld/sdp/ck/ff84934d79fc59407b536055f27e5ea9");





        System.out.println(new TripleDesCipher("11111111111111111111111111111111111111").decrypt("nFQSVgyN5XguRhGutbO2ga1qiLT6N4M9"));


        System.out.println(HadoopUtils.getRMWebAppURL(true, "D:\\module\\hadoop01\\hadoop-3.1.1\\etc\\hadoop"));
        System.out.println(HadoopUtils.getFlinkWebAppURL("D:\\module\\hadoop01\\hadoop-3.1.1\\etc\\hadoop"));
        System.out.println(HadoopUtils.getFlinkWebAppURL("D:\\module\\hadoop-3.1.1\\etc\\hadoop"));

//        System.setProperty("HADOOP_USER_NAME", "admin");
//        List<FileStatus> checkPointPaths = HdfsUtils.getCheckPointPaths("D:\\module\\hadoop01\\hadoop-3.1.1\\etc\\hadoop","hdfs://bigbigworld/sdp/ck/255e57b599944f7c5b249526c561cdf33");
//        System.out.println(checkPointPaths);
//        System.out.println(HdfsUtils.delete("D:\\module\\hadoop01\\hadoop-3.1.1\\etc\\hadoop","hdfs://bigbigworld/sdp/ck/255e57b599944f7c5b249526c561cdf3"));


    }


    /**
     * 将本地目录或文件上传的hdfs
     *
     * @param localSrc
     * @param dst
     * @throws Exception
     */
    public static void uploadFile(String localSrc, String dst) throws Exception {
        FileSystem fileSystem = HdfsUtils.getFileSystem("D:\\module\\hadoop-3.1.1\\etc\\hadoop");
        File srcFile = new File(localSrc);
        if (srcFile.isDirectory()) {
            copyDirectory(localSrc, dst, fileSystem);
        } else {
            copyFile(localSrc, dst, fileSystem);
        }
    }


    /**
     * 拷贝本地目录到hdfs
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
     * 拷贝本地文件hdfs目录下
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
        Path path = new Path(dst);
        fs.exists(path);
        InputStream in = new BufferedInputStream(new FileInputStream(file));
        OutputStream out = fs.create(new Path(dst));
        IOUtils.copyBytes(in, out, 4096, true);
        in.close();
        return true;
    }



}
