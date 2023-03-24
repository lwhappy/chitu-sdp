package com.chitu.bigdata.sdp.test;

import com.alibaba.fastjson.JSON;
import com.chitu.bigdata.sdp.api.bo.SdpApproveBO;
import com.chitu.bigdata.sdp.api.model.SdpApprove;
import com.chitu.cloud.web.test.BaseTest;
import org.assertj.core.util.Lists;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * @author chenyun
 * @description: TODO
 * @date 2022/2/17 21:39
 */
@AutoConfigureMockMvc
public class ApproveTest extends BaseTest {
    @Autowired
    private MockMvc mvc;

    private final Long projectId = 60L;
    private final Long uid = 123L;
    private final String token = "0b3b4735-8e0e-4a96-b36e-b5031940fca2";

    @Test
    public void testSubmitApply() throws Exception{
        SdpApproveBO approveBO = new SdpApproveBO();
        approveBO.setId(81+"");
        approveBO.setProjectName("测试薛");
        approveBO.setRemark("备注");
        String mvcResult = this.mvc
                .perform(post("/approve/submitApply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(approveBO))
                        .header("projectId",projectId).header("token",token).header("X-uid",uid)
                ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    @Test
    public void queryApply() throws Exception{
        SdpApproveBO approveBO = new SdpApproveBO();
        SdpApprove vo = new SdpApprove();
        vo.setCurrentUser(123+"");
        vo.setType("approve");
        vo.setStatuss(Lists.newArrayList("APPROVING","AGGRE"));
        vo.setStartTime("2021-10-24");
        vo.setEndTime("2022-02-15");
        String mvcResult = this.mvc
                .perform(post("/approve/queryApply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(approveBO))
                        .header("projectId",projectId).header("token",token).header("X-uid",uid)
                ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    @Test
    public void cancelApply() throws Exception{
        SdpApproveBO approveBO = new SdpApproveBO();
        approveBO.setId(81+"");
        approveBO.setProjectName("测试薛");
        approveBO.setRemark("备注");
        String mvcResult = this.mvc
                .perform(post("/approve/cancelApply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(approveBO))
                        .header("projectId",projectId).header("token",token).header("X-uid",uid)
                ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    @Test
    public void detailApply() throws Exception{
        SdpApproveBO approveBO = new SdpApproveBO();
        approveBO.setId(81+"");
        approveBO.setProjectName("测试薛");
        approveBO.setRemark("备注");
        String mvcResult = this.mvc
                .perform(post("/approve/detailApply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(approveBO))
                        .header("projectId",projectId).header("token",token).header("X-uid",uid)
                ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }

    @Test
    public void executeApprove() throws Exception{
        SdpApproveBO approveBO = new SdpApproveBO();
        approveBO.setId(81+"");
        approveBO.setProjectName("测试薛");
        approveBO.setRemark("备注");
        String mvcResult = this.mvc
                .perform(post("/approve/executeApprove")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(JSON.toJSONString(approveBO))
                        .header("projectId",projectId).header("token",token).header("X-uid",uid)
                ).andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
    }
}
