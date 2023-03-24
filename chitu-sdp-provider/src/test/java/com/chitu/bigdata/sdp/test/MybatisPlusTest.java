package com.chitu.bigdata.sdp.test;

import com.chitu.bigdata.sdp.api.model.SdpUser;
import com.chitu.bigdata.sdp.mapper.SdpUserMapper;
import com.chitu.bigdata.sdp.service.UserService;
import com.chitu.cloud.web.test.BaseTest;
import groovy.util.logging.Slf4j;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author zouchangzhen
 * @date 2022/12/2
 */
@Slf4j
public class MybatisPlusTest   extends BaseTest {
    @Autowired
    SdpUserMapper sdpTestMapper;

    @Autowired
    UserService userService;

    @Test
    public void testInsert(){
        SdpUser sdpUser4Add = new SdpUser();
        String userName = "test123321";
        sdpUser4Add.setUserName(userName);
        sdpUser4Add.setEmployeeNumber(userName);
        userService.insert(sdpUser4Add);

        sdpUser4Add.setUserName("updateUserName");
        userService.update(sdpUser4Add);

        sdpUser4Add.setUserName("batchUpdateSelective");
        userService.batchUpdateSelective(Lists.newArrayList(sdpUser4Add));

        sdpUser4Add.setUserName("updateSelectiveUserName");
        userService.updateSelective(sdpUser4Add);

        userService.disable(SdpUser.class,sdpUser4Add.getId());

        userService.enable(SdpUser.class,sdpUser4Add.getId());

        SdpUser sdpUser = userService.get(sdpUser4Add.getId());

        List<SdpUser> byIds = userService.getByIds(sdpUser4Add.getId());

        sdpUser4Add.setUpdationDate(null);
        sdpUser4Add.setCreationDate(null);
        List<SdpUser> sdpUsers = userService.selectAll(sdpUser4Add);


        int delete = userService.delete(sdpUser4Add.getId());


    }
}
