package com.chitu.bigdata.sdp.utils;

import com.chitu.cloud.web.test.BaseTest;
import org.junit.Test;

public class VersionUtilTest extends BaseTest {


    @Test
    public void increaseVersionTest(){
        String version = "V99";
        String newVersion = VersionUtil.increaseVersion(version);
        System.out.println(newVersion);
    }

}