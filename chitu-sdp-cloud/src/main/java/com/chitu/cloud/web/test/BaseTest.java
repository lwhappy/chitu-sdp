package com.chitu.cloud.web.test;

import com.chitu.cloud.web.config.SdpFrameworkConfiguration;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes = {SdpFrameworkConfiguration.class})
public abstract class BaseTest {
}
