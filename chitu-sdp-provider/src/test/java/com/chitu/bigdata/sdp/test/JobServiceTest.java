package com.chitu.bigdata.sdp.test;

import com.alibaba.fastjson.JSONObject;
import com.chitu.bigdata.sdp.api.domain.JobConfig;
import com.chitu.bigdata.sdp.api.flink.Checkpoint;
import com.chitu.bigdata.sdp.api.model.SdpJob;
import com.chitu.bigdata.sdp.api.model.SdpProject;
import com.chitu.bigdata.sdp.mapper.SdpProjectMapper;
import com.chitu.bigdata.sdp.service.JobService;
import com.chitu.cloud.web.test.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;


public class JobServiceTest extends BaseTest {

     @Autowired
     private JobService jobService;

    @Autowired
    private SdpProjectMapper sdpProjectMapper;

     @Test
   public void filterQueueTest(){
         JobConfig jobConfig = new JobConfig();
//         jobConfig.setEngineId(108L);
         jobConfig.setEngine("flinkX");
         JSONObject jsonObject = new JSONObject();
         jobService.filterQueue(jobConfig,jsonObject);
   }
   @Test
   public void validateJobNameTest() {
       SdpJob sdpJob = new SdpJob();
       sdpJob.setJobName("common");
       sdpJob.setProjectId(72L);
       SdpProject sdpProject = sdpProjectMapper.selectById(sdpJob.getProjectId());
       String jobName = jobService.validateJobName(sdpJob,sdpProject);
       System.out.println(jobName);
   }

   @Test
    public void test(){
       List<Checkpoint> checkpoints = jobService.getCheckpoints(2086L, 1);
       System.out.println(checkpoints);
   }

}