package com.chitu.bigdata.sdp.job;

import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.constant.CommonConstant;
import com.chitu.bigdata.sdp.interceptor.EnvHolder;
import com.chitu.bigdata.sdp.utils.DataMonitorUtils;
import com.chitu.bigdata.sdp.utils.RedisLocker;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.chitu.cloud.config.scheduler.RedisLeaderSchedule;
import com.chitu.cloud.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

import java.util.List;

/**
 * @author chenyun
 * @description: 监控作业状态同步(定时任务)是否正常运行，否则发出告警
 * @date 2022/02/08 16:14
 */

@Slf4j
@Component
public class MonitorJobStatusSync {
    @Autowired
    private RedisLocker redisLocker;
    @Autowired
    SdpConfig sdpConfig;

    @RedisLeaderSchedule
    @Scheduled(fixedRate = 60*1000)
    public void monitorExecute(){
        List<String> envList = sdpConfig.getEnvList();
        for (String env : envList) {
            EnvHolder.addEnv(env);
            try {
                handleSingleEnv( env);
            } catch (Exception e) {
                String errMsg = StrUtils.parse1("【job环境env】: {}", env);
                log.warn(errMsg,e);
            }finally {
                EnvHolder.clearEnv();
            }
        }
    }

    private void handleSingleEnv(String env) {
        Jedis jedis = null;
        try{
            jedis = redisLocker.getJedis();
            String time = jedis.get(CommonConstant.SYNC_JOB_INSTANCE+env);
            if(StringUtils.isNotEmpty(time)){
                Long time1 = Long.valueOf(time);
                Long current = System.currentTimeMillis();
                if(current - time1 > 120000){
                    DataMonitorUtils.monitorError("同步作业状态定时任务未在运行中,请检查程序逻辑");
                }
            }
        }catch (Exception e){
             log.error("监控同步作业状态的定时任务运行时异常==={}",e);
        }finally {
            if(jedis != null){
                jedis.close();
            }
        }
    }

}
