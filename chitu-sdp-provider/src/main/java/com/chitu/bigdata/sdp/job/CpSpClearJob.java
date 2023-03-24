package com.chitu.bigdata.sdp.job;

import cn.hutool.core.util.StrUtil;
import com.chitu.bigdata.sdp.api.enums.LogStatus;
import com.chitu.bigdata.sdp.api.model.SdpJobInstance;
import com.chitu.bigdata.sdp.api.model.SdpSavepoint;
import com.chitu.bigdata.sdp.config.CustomMonitorConfigProperties;
import com.chitu.bigdata.sdp.config.FlinkConfigProperties;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.constant.FlinkConfigKeyConstant;
import com.chitu.bigdata.sdp.interceptor.EnvHolder;
import com.chitu.bigdata.sdp.mapper.SdpJobInstanceMapper;
import com.chitu.bigdata.sdp.mapper.SdpSavepointMapper;
import com.chitu.bigdata.sdp.service.JobService;
import com.chitu.bigdata.sdp.utils.HdfsUtils;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.chitu.cloud.config.scheduler.RedisLeaderSchedule;
import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.fs.FileStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Checkpoint&Savepoint定期清除任务
 * @author zouchangzhen
 * @date 2022/4/20
 */
@Slf4j
@Component
public class CpSpClearJob {
    @Autowired
    SdpJobInstanceMapper sdpJobInstanceMapper;
    @Autowired
    FlinkConfigProperties flinkConfigProperties;
    @Autowired
    SdpSavepointMapper sdpSavepointMapper;

    @Autowired
    JobService jobService;
    @Autowired
    SdpConfig sdpConfig;

    @Autowired
    CustomMonitorConfigProperties customMonitorConfigProperties;

    @RedisLeaderSchedule
    @Scheduled(cron = "${custom.cpSpClearJob.cron}")
    public void clearCpSp(){
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
        String param = customMonitorConfigProperties.getCustomConfigAndEnv().getCustomConfig().getCpSpClearJob().getParam();
        log.info("CpSpClearJob param: {}" , param);
        Map<String, String> paramMap = StrUtils.getParamMap(param);
        Integer dateSubDay = Integer.valueOf(paramMap.getOrDefault("dateSubDay","1"));

        this.clearCheckPoints(dateSubDay);
        //讨论之后暂时不清除保存点数据
        //this.clearSavePoints();
    }

    /**
     * 定期清除检查点数据
     *
     * 如果job存在多个实例(flinkJobId)，且最新实例(存在检查点)，则删除最新实例之前的所有实例ck数据
     * /sdp/ck/a78fb25ff10386db966ff86cf9a06bd9
     * @param dateSubDay
     */
    private void clearCheckPoints(Integer dateSubDay) {
        try {
            //1.获取所有job的最近实例
            List<SdpJobInstance> latestInstance4Jobs = sdpJobInstanceMapper.queryLatestInstance4Job(dateSubDay);
            if(!CollectionUtils.isEmpty(latestInstance4Jobs)){
                String checkPointPrefix = null;
                for (SdpJobInstance latest : latestInstance4Jobs) {
                    checkPointPrefix = flinkConfigProperties.getDefaultMap().get(FlinkConfigKeyConstant.CHECKPOINT_DIR) + latest.getFlinkJobId();
                    String hadoopConfDir = jobService.getHadoopConfDir(latest.getJobId());
                    List<FileStatus> checkPointPaths = HdfsUtils.getCheckPointPaths(hadoopConfDir,checkPointPrefix);
                    if (org.apache.commons.collections.CollectionUtils.isEmpty(checkPointPaths)) {
                        //检查最近实例不存在检查点，循环下一个
                        continue;
                    }
                    log.info("jobName: {} , flinkJobId:{} -> 最近实例存在检查点",latest.getJobName(),latest.getFlinkJobId());
                    //2.查询最近实例之前的所有实例
                    List<SdpJobInstance> sdpJobInstances = sdpJobInstanceMapper.queryBeforeInstanceBaseOnLatest(latest.getJobId(), latest.getId(), dateSubDay);
                    if(CollectionUtils.isEmpty(sdpJobInstances)){
                       continue;
                    }
                    //3.循环删除之前检查点数据
                    for (SdpJobInstance before : sdpJobInstances) {
                        try {
                            checkPointPrefix = flinkConfigProperties.getDefaultMap().get(FlinkConfigKeyConstant.CHECKPOINT_DIR) + before.getFlinkJobId();
                            HdfsUtils.delete(hadoopConfDir,checkPointPrefix);
                            log.info("jobName: {} , flinkJobId:{} -> 删除最近实例之前的检查点数据：{}",before.getJobName(),before.getFlinkJobId(),checkPointPrefix);
                        } catch (Exception e) {
                            log.error("删除检查点["+checkPointPrefix+"]数据失败",e);
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("定期清除检查点异常",e);
        }
    }

    /**
     * 定期清除保存点数据，每个作业只保存最近5个保存点，其他的定期清除
     */
    private void clearSavePoints() {
        try {
            List<Long> jobIds = sdpSavepointMapper.queryWaitClear();
            if(!CollectionUtils.isEmpty(jobIds)){
                List<SdpSavepoint> sdpSavepoints = null;
                for (Long jobId : jobIds) {
                    sdpSavepoints = sdpSavepointMapper.queryNonRetainByJobId(jobId, null, 5,Integer.MAX_VALUE);
                    if(CollectionUtils.isEmpty(sdpSavepoints)){
                        continue;
                    }

                    List<Long> logicDelIds= Lists.newArrayList();
                    for (SdpSavepoint sdpSavepoint : sdpSavepoints) {
                        try {
                            if(Objects.isNull(sdpSavepoint)){
                                continue;
                            }
                            if(StrUtil.isBlank(sdpSavepoint.getOperateStatus()) || LogStatus.FAILED.name().equals(sdpSavepoint.getOperateStatus())){
                                logicDelIds.add(sdpSavepoint.getId());
                                continue;
                            }
                            if (LogStatus.SUCCESS.name().equals(sdpSavepoint.getOperateStatus())) {
                                //删除文件
                                String hadoopConfDir = jobService.getHadoopConfDir(jobId);
                                HdfsUtils.delete(hadoopConfDir,sdpSavepoint.getFilePath());
                                logicDelIds.add(sdpSavepoint.getId());
                            }
                        } catch (Exception e) {
                            log.error("删除保存点["+sdpSavepoint.getFilePath()+"]数据失败",e);
                        }
                    }

                    //逻辑删除保存点数据
                    if(!CollectionUtils.isEmpty(logicDelIds)){
                        sdpSavepointMapper.updateEnabledFlag(logicDelIds);
                    }
                }
            }
        } catch (Exception e) {
            log.error("定期清除保存点异常",e);
        }
    }
}
