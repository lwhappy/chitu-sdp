package com.chitu.bigdata.sdp.test;

import com.chitu.bigdata.sdp.flink.common.util.DeflaterUtils;
import com.chitu.cloud.web.test.BaseTest;
import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.io.IOException;

/**
 * @author sutao
 * @create 2021-12-16 10:13
 */
public class FlinkConfigTest extends BaseTest {

    @Test
    public void getflinkTemplate(){
//        URL url = Thread.currentThread().getContextClassLoader().getResource("flink-sql-template.yml");
////        URL url = Thread.currentThread().getContextClassLoader().getResource("flink-dstream-template.yml");
//        assert url != null;
//        String path = url.getPath();
        File file = new File("D:\\work\\bigdata-sdp\\bigdata-sdp-provider\\src\\main\\resources\\flink-sql-template.yml");
        try {
            String conf = FileUtils.readFileToString(file);
            String s = DeflaterUtils.zipString(conf);
            System.out.println(s);
            String s1 =  DeflaterUtils.unzipString(s);
            System.out.println(s1);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
