

/**
 * <pre>
 * 作   者：CHENYUN
 * 创建日期：2021-11-16
 * </pre>
 */

package com.chitu.bigdata.sdp.service;


import com.alibaba.fastjson.JSONObject;
import com.chitu.bigdata.sdp.api.bo.SdpRuntimeLogBO;
import com.chitu.bigdata.sdp.api.domain.ClusterInfo;
import com.chitu.bigdata.sdp.api.domain.JobConfig;
import com.chitu.bigdata.sdp.api.enums.*;
import com.chitu.bigdata.sdp.api.flink.AppInfo;
import com.chitu.bigdata.sdp.api.model.SdpEngine;
import com.chitu.bigdata.sdp.api.model.SdpJob;
import com.chitu.bigdata.sdp.api.model.SdpJobInstance;
import com.chitu.bigdata.sdp.api.model.SdpRuntimeLog;
import com.chitu.bigdata.sdp.api.vo.RuntimeLogResp;
import com.chitu.bigdata.sdp.config.KubernetesClusterConfig;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.flink.common.util.HadoopUtils;
import com.chitu.bigdata.sdp.mapper.SdpEngineMapper;
import com.chitu.bigdata.sdp.mapper.SdpRuntimeLogMapper;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.model.ResponseData;
import com.chitu.cloud.service.GenericService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/11/17 14:06
 */
@RefreshScope
@Service
@Slf4j
public class SdpRuntimeLogService extends GenericService<SdpRuntimeLog, Long> {

    public SdpRuntimeLogService(@Autowired SdpRuntimeLogMapper sdpRuntimeLogMapper) {
        super(sdpRuntimeLogMapper);
    }

    public SdpRuntimeLogMapper getMapper() {
        return (SdpRuntimeLogMapper) super.genericMapper;
    }

    @Autowired
    private JobService jobService;
    //    @Value("${flink.historyServer.url}")
//    private String flinkHistoryUrl;
//    @Value("${flink.proxy.urlPrefix}")
//    private String prodFlinkProxy;
    @Autowired
    SdpConfig sdpConfig;

    @Autowired
    private SdpEngineMapper engineMapper;
    @Autowired
    EngineService engineService;
    @Autowired
    KubernetesClusterConfig kubernetesClusterConfig;
    @Autowired
    JobInstanceService jobInstanceService;

    public static final int MAX_CHECK = 25;

    public ResponseData queryRuntimeLog(SdpRuntimeLogBO job) {
        ResponseData data = new ResponseData<>();
        data.ok();
        StringBuffer stringBuffer = new StringBuffer();
        if (Objects.isNull(job.getJobId())) {
            throw new ApplicationException(ResponseCode.LOG_LACK_ARGUMENT);
        }
        ArrayList<SdpRuntimeLog> logs = this.getMapper().getByJobId(job);
        //与请求的latestTime对比，判断是否是最新logs，不是不需要返回信息
        if (CollectionUtils.isEmpty(logs)){
            data.setCode(9003);
            data.setData("运行日志无更新");
            return data;
        }
        Timestamp operationTime = logs.get(logs.size() - 1).getOperationTime();
        String latestTime = job.getLatestTime();
        Timestamp lastStamp = Timestamp.valueOf(latestTime == null ? "1970-01-01 08:00:00.0" : latestTime);
        if (!operationTime.after(lastStamp)) {
            data.setCode(9003);
            data.setData("运行日志无更新");
            return data;
        }
        for (SdpRuntimeLog xlog : logs) {
            String env = sdpConfig.getEnvFromEnvHolder(log);
            SdpJob sdpJob = jobService.get(xlog.getJobId());

            JobConfig jobConfig = JSONObject.parseObject(sdpJob.getConfigContent(), JobConfig.class);
            SdpEngine engine = engineService.get(jobConfig.getEngineId());
            ClusterInfo clusterInfo = engineService.getClusterInfo(env, engine);

            String flinkProxyUrl = clusterInfo.getFlinkProxyUrl();
            stringBuffer.append(xlog.toString(flinkProxyUrl,env)).append("\r\n");
        }
        //对已完成的任务进行更新historyUrl
        SdpJobInstance sdpJobInstance = new SdpJobInstance();
        sdpJobInstance.setJobId(job.getJobId());
        sdpJobInstance.setIsLatest(true);
        List<SdpJobInstance> sdpJobInstances = jobInstanceService.selectAll(sdpJobInstance);
        if (!CollectionUtils.isEmpty(sdpJobInstances)) {
            if (Objects.nonNull(sdpJobInstances.get(0).getFlinkJobId())) {
                String rawStatus = sdpJobInstances.get(0).getRawStatus();
                String jobStatus = sdpJobInstances.get(0).getJobStatus();
                if (RawStatus.FINISHED.name().equals(rawStatus)){
                    String flinkHistoryUrl = getFlinkHistoryUrl(sdpJobInstances.get(0));
                    String format = "《查看Flink历史日志》【%s】\r\n";
                    String format1 = String.format(format, flinkHistoryUrl);
                    stringBuffer.append(format1);
                }
                //当任务由于数据问题失败时，需要增加对应的flink历史日志信息
                if(JobStatus.SFAILED.name().equals(jobStatus)){
                    String flinkHistoryUrl = getFlinkHistoryUrl(sdpJobInstances.get(0));
                    if(Objects.nonNull(flinkHistoryUrl)) {
                        String format = "运行失败 《查看Flink历史日志》【%s】\r\n";
                        String format1 = String.format(format, flinkHistoryUrl);
                        stringBuffer.append(format1);
                    }
                }
            }
        }
        RuntimeLogResp runtimeLogResp = new RuntimeLogResp();
        runtimeLogResp.setLogs(stringBuffer.toString());
        runtimeLogResp.setLatestTime(operationTime);
        data.setData(runtimeLogResp);
        return data;
    }


    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void recordLog(Long jobId, String type, JobRunStep validate, LogStatus logStatus,String exception) {
        try {
            SdpRuntimeLog sdpRuntimeLog = new SdpRuntimeLog();
            sdpRuntimeLog.setJobId(jobId);
            sdpRuntimeLog.setOperation(type);
            sdpRuntimeLog.setStepAction(validate.name());
            sdpRuntimeLog.setOperationTime(new Timestamp(System.currentTimeMillis()));
            if (Objects.nonNull(logStatus)){
                sdpRuntimeLog.setOperationStage(logStatus.name());
            }
            if (Objects.nonNull(exception)){
                sdpRuntimeLog.setStackTrace(exception);
            }
            this.insert(sdpRuntimeLog);
        } catch (Exception e) {
            logger.error(jobId + "===运行日志写入作业{}失败==={}",type, e);
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void recordStartLog(Long jobId, String type, JobRunStep commit, LogStatus logStatus, String appId,String exception)  {

        //组装yarnUrl和flinkUrl
        try {
            String hadoopConfDir = jobService.getHadoopConfDir(jobId);
            AppInfo appInfo = com.chitu.bigdata.sdp.utils.HadoopUtils.httpYarnAppInfo(appId,null,hadoopConfDir);
            logger.info("======获取到appInfo："+ JSONObject.toJSONString(appInfo));
            String yarnUrl = appInfo.getApp().getAmContainerLogs();

            SdpRuntimeLog sdpRuntimeLog = new SdpRuntimeLog();
            sdpRuntimeLog.setJobId(jobId);
            sdpRuntimeLog.setOperation(type);
            sdpRuntimeLog.setStepAction(commit.name());
            sdpRuntimeLog.setOperationTime(new Timestamp(System.currentTimeMillis()));
            if (Objects.nonNull(logStatus)){
                sdpRuntimeLog.setOperationStage(logStatus.name());
            }

            if (Objects.nonNull(yarnUrl)){
                sdpRuntimeLog.setYarnLogUrl(yarnUrl);
            }
            if (Objects.nonNull(exception)){
                sdpRuntimeLog.setStackTrace(exception);
            }
            this.insert(sdpRuntimeLog);
            logger.info("=====开始监测作业运行状态=====");
            //监测作业运行状态
            checkJobStatus(jobId, type, logStatus, appId,hadoopConfDir);
        } catch (Exception e) {
            logger.error(jobId + "===运行日志写入作业{}失败==={}",type, e);
        }

    }



    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void recordStartLogK8s(Long jobId, String type, JobRunStep commit, LogStatus logStatus, String appId, String exception)  {

        //组装yarnUrl和flinkUrl
        try {
            SdpRuntimeLog sdpRuntimeLog = new SdpRuntimeLog();
            sdpRuntimeLog.setJobId(jobId);
            sdpRuntimeLog.setOperation(type);
            sdpRuntimeLog.setStepAction(commit.name());
            sdpRuntimeLog.setOperationTime(new Timestamp(System.currentTimeMillis()));
            if (Objects.nonNull(logStatus)){
                sdpRuntimeLog.setOperationStage(logStatus.name());
            }
            if (Objects.nonNull(exception)){
                sdpRuntimeLog.setStackTrace(exception);
            }
            this.insert(sdpRuntimeLog);
            logger.info( "===jobId[{}]k8s作业开始运行写入日志检测中..==={}",jobId);
            //监测作业运行状态
            checkJobStatusK8s(jobId, type, logStatus, appId);
        } catch (Exception e) {
            logger.error(jobId + "===运行日志写入作业{}失败==={}",type, e);
        }
    }

    private void checkJobStatusK8s(Long jobId, String type, LogStatus logStatus,String appId) {
        if (LogStatus.SUCCESS.equals(logStatus)) {
            //记录检测状态开始
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                logger.info("k8s作业启动检测状态开始,jobId："+jobId);
                recordExamine(jobId, type,JobRunStep.EXAMINE,null,null);
                int count = 0;
                while (true){
                    //按照jobId和appId进行获取instance表数据，如果没有数据等待3s，再次发起申请，如果有flinkId写入即为成功
                    SdpJobInstance instance = new SdpJobInstance();
                    instance.setIsLatest(true);
                    instance.setJobId(jobId);
                    instance.setApplicationId(appId);
                    List<SdpJobInstance> list = jobInstanceService.selectAll(instance);
                    if (!CollectionUtils.isEmpty(list)){
                        SdpJobInstance jobInstance = list.get(0);
                        // 如果raw_status状态为异常，状态同步线程会添加日志
                        if (Objects.nonNull(jobInstance.getFlinkUrl()) && RawStatus.RUNNING.name().equals(jobInstance.getRawStatus())){
                            // 检测到正常运行于flink，返回写入数据
                            String format = "%s/#/job-manager/logs";
                            String flinkLogUrl = String.format(format, jobInstance.getJobmanagerAddress());
                            recordExamine(jobId, type,JobRunStep.RUNNING,LogStatus.SUCCESS,flinkLogUrl);
                            return;
                        }
                    }
                    ++count;
                    if (count >= kubernetesClusterConfig.getStartMaxCheckNumber()){
                        logger.error("k8s检测作业状态超出检测次数限制,jobId："+jobId);
//                        recordExamine(jobId, type,JobRunStep.RUNNING,LogStatus.SUCCESS,null);
                        return;
                    }
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        logger.error("k8s检测作业状态异常，休眠异常{}",e);
                    }
                }
            });
            future.exceptionally((t) -> {
                logger.error("k8s检测作业状态异常,{}", t);
                return null;
            });
        }
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void insertFailLog(Long jobId,String applicationId, String type){
        try {
            SdpRuntimeLog sdpRuntimeLog = new SdpRuntimeLog();
            sdpRuntimeLog.setJobId(jobId);
            sdpRuntimeLog.setOperation(type);
            sdpRuntimeLog.setStepAction(JobRunStep.RUNNING.name());
            sdpRuntimeLog.setOperationTime(new Timestamp(System.currentTimeMillis()));
            sdpRuntimeLog.setOperationStage(LogStatus.FAILED.name());
//            sdpRuntimeLog.setK8sLogUrl(getK8sUrl(applicationId));
            this.insert(sdpRuntimeLog);
        } catch (Exception e) {
            logger.error(jobId + "===运行日志写入作业{}失败==={}",type, e);
        }
    }



    private void checkJobStatus(Long jobId, String type, LogStatus logStatus, String appId,String hadoopConfDir) {
        if (LogStatus.SUCCESS.equals(logStatus)) {
            //记录检测状态开始
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                recordExamine(jobId, type,JobRunStep.EXAMINE,null,null);
                int count = 0;
                while (true){
                    //按照jobId和appId进行获取instance表数据，如果没有数据等待3s，再次发起申请，如果有flinkId写入即为成功
                    SdpJobInstance instance = new SdpJobInstance();
                    instance.setIsLatest(true);
                    instance.setJobId(jobId);
                    instance.setApplicationId(appId);
                    List<SdpJobInstance> list = jobInstanceService.selectAll(instance);
                    if (!CollectionUtils.isEmpty(list)){
                        SdpJobInstance jobInstance = list.get(0);
                        if (Objects.nonNull(jobInstance.getFlinkUrl())){
                            //检测到正常运行于flink，返回写入数据
                            String format = "%s/proxy/%s/#/job-manager/logs";
                            String flinkHost = HadoopUtils.getFlinkWebAppURL(hadoopConfDir);
                            String flinkUrl = String.format(format, flinkHost, appId);
                            recordExamine(jobId, type,JobRunStep.RUNNING,LogStatus.SUCCESS,flinkUrl);
                            return;
                        }
                        //如果raw_status状态为异常，返回失败,返回异常的栈信息
                        if (Objects.nonNull(jobInstance.getRawStatus()) && Objects.nonNull(jobInstance.getJobStatus())) {
                            if (RawStatus.fromStatus(jobInstance.getRawStatus()).isFailed() ||
                                    JobStatus.RFAILED.name().equals(jobInstance.getJobStatus()) ||
                                    JobStatus.SFAILED.name().equals(jobInstance.getJobStatus())) {
                                recordExamine(jobId, type, JobRunStep.RUNNING, LogStatus.FAILED,null);
                                return;
                            }
                        }
                    }
                    ++count;
                    if (count >= MAX_CHECK){
                        logger.error("检测作业状态异常,超出检测次数限制");
                        //组装flinkUrl,可以让一直启动中的任务查看flink日志信息
                        String format = "%s/proxy/%s/#/job-manager/logs";
                        String flinkHost = HadoopUtils.getFlinkWebAppURL(hadoopConfDir);
                        String flinkUrl = String.format(format, flinkHost, appId);
                        recordExamine(jobId, type,JobRunStep.RUNNING,LogStatus.SUCCESS,flinkUrl);
                        return;
                    }
                    try {
                        Thread.sleep(3000);
                    } catch (InterruptedException e) {
                        logger.error("检测作业状态异常，休眠异常{}",e);
                    }
                }
            });
            future.exceptionally((t) -> {
                logger.error("检测作业状态异常,{}", t);
                return null;
            });

        }
    }

    private void recordExamine(Long jobId, String type, JobRunStep examine, LogStatus logStatus,String flinkUrl) {
        SdpRuntimeLog sdpRuntimeLog = new SdpRuntimeLog();
        sdpRuntimeLog.setJobId(jobId);
        sdpRuntimeLog.setOperation(type);
        sdpRuntimeLog.setStepAction(examine.name());
        sdpRuntimeLog.setOperationTime(new Timestamp(System.currentTimeMillis()));
        if (Objects.nonNull(flinkUrl)) {
            sdpRuntimeLog.setFlinkLogUrl(flinkUrl);
        }
        if (Objects.nonNull(logStatus)) {
            sdpRuntimeLog.setOperationStage(logStatus.name());
        }
        this.insert(sdpRuntimeLog);
    }

    //    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void recordStopLog(Long jobId, String type, JobRunStep stepValue, LogStatus failed, String stringifyException) {
        try {
            SdpRuntimeLog srl =  this.getMapper().getInfo4Url(jobId);
            String yarnUrl = null;
            if (Objects.nonNull(srl)){
                yarnUrl = srl.getYarnLogUrl();
            }
            SdpJobInstance sdpJobInstance = new SdpJobInstance();
            sdpJobInstance.setJobId(jobId);
            sdpJobInstance.setIsLatest(true);
            List<SdpJobInstance> sdpJobInstances = jobInstanceService.selectAll(sdpJobInstance);
            String flinkUrl = null;
            if (!CollectionUtils.isEmpty(sdpJobInstances)) {
                flinkUrl = getFlinkHistoryUrl(sdpJobInstances.get(0));
            }
            SdpRuntimeLog sdpRuntimeLog = new SdpRuntimeLog();
            sdpRuntimeLog.setJobId(jobId);
            sdpRuntimeLog.setOperation(type);
            sdpRuntimeLog.setStepAction(stepValue.name());
            sdpRuntimeLog.setOperationTime(new Timestamp(System.currentTimeMillis()));
            if (Objects.nonNull(failed)){
                sdpRuntimeLog.setOperationStage(failed.name());
            }
            if (Objects.nonNull(flinkUrl)){
                sdpRuntimeLog.setFlinkLogUrl(flinkUrl);
            }
            if (Objects.nonNull(yarnUrl)){
                sdpRuntimeLog.setYarnLogUrl(yarnUrl);
            }
            if (Objects.nonNull(stringifyException)){
                sdpRuntimeLog.setStackTrace(stringifyException);
            }
            this.insert(sdpRuntimeLog);
        } catch (Exception e) {
            logger.error(jobId + "===运行日志写入作业{}失败==={}",type, e);
        }
    }

    public String getFlinkHistoryUrl(SdpJobInstance sdpJobInstance) {
        //获取flink的historyServer的地址
        String  flinkUrl = null;
        String flinkJobId = sdpJobInstance.getFlinkJobId();
        if (!StringUtils.isEmpty(flinkJobId)) {
            String format = "http://%s/#/job/%s/exceptions";
            String cluster = engineMapper.queryClusterByJobId(sdpJobInstance.getJobId());
            String env = sdpConfig.getEnvFromEnvHolder(log);

            SdpJob sdpJob = jobService.get(sdpJobInstance.getJobId());

            JobConfig jobConfig = JSONObject.parseObject(sdpJob.getConfigContent(), JobConfig.class);
            SdpEngine engine = engineService.get(jobConfig.getEngineId());
            ClusterInfo clusterInfo = engineService.getClusterInfo(env, engine);

            String flinkHistoryUrl =clusterInfo.getFlinkHistoryUrl();
            if (!StringUtils.isEmpty(flinkHistoryUrl)) {
                flinkUrl = String.format(format, flinkHistoryUrl, flinkJobId);
            }
        }
        return flinkUrl;
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED)
    public void deleteRuntimeLogs(Long jobId) {
        try {
            this.getMapper().deleteByJobId(jobId);
        } catch (Exception e) {
            logger.error("删除历史运行日志异常{}",e);
        }
    }
}