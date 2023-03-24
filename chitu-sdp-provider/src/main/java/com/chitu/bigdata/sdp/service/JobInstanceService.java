package com.chitu.bigdata.sdp.service;

import com.chitu.bigdata.sdp.api.enums.RawStatus;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.enums.TriggerType;
import com.chitu.bigdata.sdp.api.model.SdpJob;
import com.chitu.bigdata.sdp.api.model.SdpJobInstance;
import com.chitu.bigdata.sdp.api.model.SdpRawStatusHistory;
import com.chitu.bigdata.sdp.mapper.SdpJobInstanceMapper;
import com.chitu.bigdata.sdp.mapper.SdpJobMapper;
import com.chitu.bigdata.sdp.mapper.SdpRawStatusHistoryMapper;
import com.chitu.bigdata.sdp.utils.Assert;
import com.chitu.cloud.service.GenericService;
import com.chitu.cloud.utils.Context;
import com.chitu.cloud.utils.ContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/11/2 17:10
 */
@Service
public class JobInstanceService extends GenericService<SdpJobInstance, Long> {
    public JobInstanceService(@Autowired SdpJobInstanceMapper jobInstanceMapper) {
        super(jobInstanceMapper);
    }

    public SdpJobInstanceMapper getMapper() {
        return (SdpJobInstanceMapper) super.genericMapper;
    }

    @Autowired
    private SdpJobMapper jobMapper;
    @Autowired
    private SdpRawStatusHistoryMapper historyMapper;

    @Autowired
    private JobService jobService;


    @Transactional(rollbackFor = Exception.class)
    public void afterStartJob(SdpJobInstance instance, SdpJob sdpJob, LinkedHashMap response) {
        //启动任务后,更新最新实例
        if (null != sdpJob.getUseLatest() && sdpJob.getUseLatest() == true) {
            instance.setJobVersion(sdpJob.getLatestVersion());
        }
        updateInstance(response, instance, sdpJob);
        //初始化一条作业状态历史
        insertStatusHistory(instance);
        //立马手动同步状态
        /*if(response != null){
            if(response.applicationId() != null){
                try{
                    redissonLocker.lock("sync_job_id_"+instance.getJobId());
                    JobInfoThread jobInfoThread = new JobInfoThread(instance,null,instanceMapper,historyMapper,flinkConfigProperties, jobStatusNotifyService);
                    jobInfoThread.execute(instance);
                }catch (Exception e){
                    logger.error("同步任务状态异常==="+e);
                }finally {
                    redissonLocker.unlock("sync_job_id_"+instance.getJobId());
                }
            }
        }*/
    }

    public void updateInstance(LinkedHashMap response, SdpJobInstance jobInstance, SdpJob sdpJob) {
        Context context = ContextUtils.get();
        if (response != null) {
            jobInstance.setApplicationId(String.valueOf(response.get("applicationId")));
            jobInstance.setConfiguration(response.get("configuration").toString());
            if(response.get("jobManagerAddress")!=null){
                jobInstance.setJobmanagerAddress(response.get("jobManagerAddress").toString());
            }
        }
        jobInstance.setJobContent(sdpJob.getJobContent());
        jobInstance.setConfigContent(sdpJob.getConfigContent());
        jobInstance.setSourceContent(sdpJob.getSourceContent());
        if (null != sdpJob.getUseLatest() && sdpJob.getUseLatest() == true) {
            jobInstance.setJobVersion(sdpJob.getLatestVersion());
        } else {
            jobInstance.setJobVersion(sdpJob.getRunningVersion());
        }
        if (context != null) {
            jobInstance.setCreatedBy(context.getUserId());
            jobInstance.setUpdatedBy(context.getUserId());
        }
        update(jobInstance);
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void renewJobVersion(SdpJob sdpJob) {
        if (null != sdpJob.getUseLatest() && sdpJob.getUseLatest() == true) {
            sdpJob.setRunningVersion(sdpJob.getLatestVersion());
            jobService.updateSelective(sdpJob);
        }
    }

    public Integer insertStatusHistory(SdpJobInstance instance) {
        SdpRawStatusHistory history = new SdpRawStatusHistory();
        history.setJobId(instance.getJobId());
        history.setInstanceId(instance.getId());
        history.setTrigger(TriggerType.SDP.toString());
        history.setFromStatus(RawStatus.INITIALIZE.toString());
        history.setToStatus(RawStatus.STARTING.toString());
        return historyMapper.insert(history);
    }

    public SdpJobInstance getLatestJobInstance(Long jobId) {
        List<SdpJobInstance> sdpJobInstances = this.selectAll(new SdpJobInstance(jobId));
        Assert.notEmpty(sdpJobInstances,ResponseCode.JOB_NOT_EXISTS);
        // 获取最新实例
        List<SdpJobInstance> latestJobInstanceList = sdpJobInstances.stream().filter(item -> item.getIsLatest() && item.getFlinkJobId() != null).collect(Collectors.toList());
        Assert.notEmpty(latestJobInstanceList,ResponseCode.JOB_NOT_EXISTS);
        return latestJobInstanceList.get(0);
    }


}
