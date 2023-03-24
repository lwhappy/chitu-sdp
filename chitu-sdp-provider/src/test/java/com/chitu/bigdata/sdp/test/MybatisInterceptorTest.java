package com.chitu.bigdata.sdp.test;

import com.chitu.bigdata.sdp.mapper.SdpKafkaConsumerRecordMapper;
import com.chitu.bigdata.sdp.mapper.SdpUserMapper;
import com.chitu.cloud.web.test.BaseTest;
import groovy.util.logging.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author zouchangzhen
 * @date 2022/10/9
 */
@Slf4j
public class MybatisInterceptorTest   extends BaseTest {
    @Autowired
    SdpUserMapper sdpUserMapper;
    @Autowired
    SdpKafkaConsumerRecordMapper sdpKafkaConsumerRecordMapper;

    @Test
    public void testSelect() {

    }


}
