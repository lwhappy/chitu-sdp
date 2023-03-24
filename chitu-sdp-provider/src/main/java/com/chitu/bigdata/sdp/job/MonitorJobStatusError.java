package com.chitu.bigdata.sdp.job;

import com.chitu.bigdata.sdp.api.bo.SdpJobBO;
import com.chitu.bigdata.sdp.api.enums.*;
import com.chitu.bigdata.sdp.api.model.SdpJob;
import com.chitu.bigdata.sdp.api.model.SdpJobInstance;
import com.chitu.bigdata.sdp.api.model.SdpOperationLog;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.flink.common.util.HadoopUtils;
import com.chitu.bigdata.sdp.interceptor.EnvHolder;
import com.chitu.bigdata.sdp.mapper.SdpJobInstanceMapper;
import com.chitu.bigdata.sdp.mapper.SdpOperationLogMapper;
import com.chitu.bigdata.sdp.service.FileService;
import com.chitu.bigdata.sdp.service.JobService;
import com.chitu.bigdata.sdp.service.OperationLogService;
import com.chitu.bigdata.sdp.utils.JobOperationUtils;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.chitu.cloud.config.scheduler.RedisLeaderSchedule;
import com.chitu.cloud.exception.ApplicationException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.sql.Timestamp;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chenyun
 * @description: 定时扫描作业实例表的状态，处理状态卡住的情况
 * @date 2022/03/17 16:14
 */

@Slf4j
@Component
public class MonitorJobStatusError {
    private final Integer WAIT_TIME = 300000;
    @Autowired
    private SdpJobInstanceMapper jobInstanceMapper;
    @Autowired
    private SdpOperationLogMapper logMapper;
    @Autowired
    OperationLogService operationLogService;
    @Autowired
    private JobService jobService;
    @Autowired
    private FileService fileService;
    @Autowired
    SdpConfig sdpConfig;
    @Autowired
    private RestTemplate restTemplate;

    @RedisLeaderSchedule
    @Scheduled(fixedRate = 60*1000)
    public void repairExpectStatus(){
        List<String> envList = sdpConfig.getEnvList();
        for (String env : envList) {
            EnvHolder.addEnv(env);
            try {
                handleSingleEnv();
            } catch (Exception e) {
                String errMsg = StrUtils.parse1("【job环境env】: {}", env);
                log.warn(errMsg,e);
            }finally {
                EnvHolder.clearEnv();
            }
        }
    }

    private void handleSingleEnv() {
        List<SdpJobInstance> list = jobInstanceMapper.queryExceptionInstance();
        if(CollectionUtils.isNotEmpty(list)){
            list.forEach(x->{
                try {
                    Timestamp startTime = x.getStartTime();
                    Timestamp now = new Timestamp(System.currentTimeMillis());
                    if(null != startTime && (now.getTime() - startTime.getTime() > WAIT_TIME)){
                        log.info("开始处理异常状态作业{}=====",x.getJobId()+"##"+x.getApplicationId());
                        String jobStatus = x.getJobStatus();
                        String expectStatus = x.getExpectStatus();
                        if(ExpectStatus.RUNNING.toString().equals(expectStatus)){
                            //如果是集群没资源，则停止任务
                            if(RawStatus.ACCEPTED.toString().equals(x.getRawStatus())){
                                // stopJob(x.getJobId());
                                log.info("开始kill作业{}=====",x.getJobId()+"##"+x.getApplicationId());

                                //String killJob = "yarn application -kill "+x.getApplicationId();
                                //fileService.runCommand(killJob);
                                killJob(x.getJobId(),x.getApplicationId());

                                x.setExpectStatus(JobStatus.TERMINATED.toString());
                                SdpOperationLog sdpLog = logMapper.getLogByInstanceId(x.getId());
                                if(sdpLog != null){
                                    sdpLog.setMessage("集群资源不足，启动任务失败");
                                    operationLogService.updateSelective(sdpLog);
                                }
                            }else{
                                log.info("启动->修改作业{}状态为{}=====",x.getJobId(),jobStatus);
                                x.setExpectStatus(jobStatus);
                            }
                        }else{
                            log.info("暂停/停止->修改作业{}状态为{}=====",x.getJobId(),jobStatus);
                            x.setExpectStatus(jobStatus);
                        }
                        jobInstanceMapper.repairExpectStatus(x);
                    }

                }catch (Exception e){
                    String errMsg = "处理异常状态作业{"+x.getJobId()+"##"+x.getApplicationId()+"}=====异常";
                    log.warn(errMsg,e);
                }
            });
        }
    }

    /**
     * kill job
     * @param jobId
     * @param applicationId
     */
    private void killJob(Long jobId,String applicationId){
        String format = "%s/ws/v1/cluster/apps/%s/state";
        String hadoopConfDir = jobService.getHadoopConfDir(jobId);
        String schedulerUrl = String.format(format, HadoopUtils.getRMWebAppURL(true,hadoopConfDir),applicationId);


        Map<String,Object> param = new HashMap<>();
        param.put("state","KILLED");
        restTemplate.put(schedulerUrl, param);
    }

    private void stopJob(Long jobId) {
        SdpJobBO jobBO = new SdpJobBO();
        SdpJob sdpJob = new SdpJob();
        sdpJob.setId(jobId);
        jobBO.setVo(sdpJob);
        try{
            JobOperationUtils.runInJobLock(jobId, JobOperationUtils.OperationType.STOP_JOB, () -> {
                jobService.validateStop(jobBO, JobAction.STOP.toString());
                jobService.stopJob(jobBO, JobAction.STOP.toString());
                return null;
            });
        } catch (Exception e){
            log.error("下线作业失败===jobId: " + jobId, e);
            throw new ApplicationException(ResponseCode.ERROR,String.format("下线作业失败, jobId: %d, message: %s", jobId, e.getMessage()));
        }
    }

}
