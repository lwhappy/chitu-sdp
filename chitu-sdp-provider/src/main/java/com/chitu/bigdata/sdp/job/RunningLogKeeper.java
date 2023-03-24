package com.chitu.bigdata.sdp.job;

import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.interceptor.EnvHolder;
import com.chitu.bigdata.sdp.mapper.SdpRuntimeLogMapper;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.chitu.cloud.config.scheduler.RedisLeaderSchedule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * @author 587694
 * @description: TODO 进行
 * @date 2021/11/29 10:06
 */

@Slf4j
@Component
public class RunningLogKeeper {
    @Autowired
    private SdpRuntimeLogMapper runtimeLogMapper;
    @Autowired
    SdpConfig sdpConfig;

    @RedisLeaderSchedule
    @Scheduled(initialDelay = 1000, fixedRate = 7 * 24 * 60 * 60 * 1000)
//   @Scheduled(cron="0 0 1 ? * L")
    public void syncExecute() {
        log.info("======定期删除运行记录数据运行开始=====");
        for (String env : sdpConfig.getEnvList()) {
            EnvHolder.addEnv(env);
            try {
                runtimeLogMapper.cycleDel();
            } catch (Exception e) {
                String errMsg = StrUtils.parse1("【job环境env】: {}", env);
                log.warn(errMsg,e);
            }finally {
                EnvHolder.clearEnv();
            }
        }
        log.info("======定期删除运行记录数据完成=====");
    }

}
