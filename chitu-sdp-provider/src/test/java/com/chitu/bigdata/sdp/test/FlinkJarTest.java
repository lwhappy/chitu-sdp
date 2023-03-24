package com.chitu.bigdata.sdp.test;

import com.chitu.bigdata.sdp.service.JarService;
import com.chitu.cloud.web.test.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

public class FlinkJarTest extends BaseTest {

    @Autowired
    private JarService jarService;


    @Test
    public void getJarFlinkVersion() throws Exception {

        String jarFlinkVersion = jarService.getJarFlinkVersion("/sdp/workspace/product_test/udf/bigdata-flink-dstream/v1/bigdata-flink-dstream-1.0-SNAPSHOT.jar", "bigdata-flink-dstream-1.0-SNAPSHOT.jar");

        System.out.println(jarFlinkVersion);
       // System.out.println(jarFlinkVersion);

        //jarService.syncJarFlinkVersion();



//        String jarFlinkVersion = jarService.getJarFlinkVersion("/sdp/workspace/rewtrye/udf/commons-lang/v1/commons-lang-2.5.jar", "commons-lang-2.5.jar");
//        System.out.println(jarFlinkVersion);
//        jarFlinkVersion = jarService.getJarFlinkVersion("/sdp/workspace/rewtrye/udf/commons-lang/1.1/commons-lang-2.5.jar", "commons-lang-2.5.jar");
//        System.out.println(jarFlinkVersion);


    }


}
