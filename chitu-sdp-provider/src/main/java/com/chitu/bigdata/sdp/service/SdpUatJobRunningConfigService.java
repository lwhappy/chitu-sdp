

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-5-16
  * </pre>
  */

package com.chitu.bigdata.sdp.service;


import com.chitu.bigdata.sdp.api.model.SdpUatJobRunningConfig;
import com.chitu.bigdata.sdp.mapper.SdpUatJobRunningConfigMapper;
import com.chitu.cloud.service.GenericService;
import com.chitu.cloud.utils.Context;
import com.chitu.cloud.utils.ContextUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;

/**
 * <pre>
 * UAT环境作业运行配置业务类
 * </pre>
 */
@Service("sdpUatJobRunningConfigService")
public class SdpUatJobRunningConfigService extends GenericService<SdpUatJobRunningConfig, Long> {
    public SdpUatJobRunningConfigService(@Autowired SdpUatJobRunningConfigMapper sdpUatJobRunningConfigMapper) {
        super(sdpUatJobRunningConfigMapper);
    }
    
    public SdpUatJobRunningConfigMapper getMapper() {
        return (SdpUatJobRunningConfigMapper) super.genericMapper;
    }

    public int upsert(SdpUatJobRunningConfig updateConfig){

        SdpUatJobRunningConfig param = new SdpUatJobRunningConfig();
        param.setJobId(updateConfig.getJobId());
        param.setEnabledFlag(1L);
        List<SdpUatJobRunningConfig> sdpUatJobRunningConfigs = selectAll(param);

        Context context = ContextUtils.get();
        //给默认值3天，兼容DI
        updateConfig.setDays(Objects.nonNull(updateConfig.getDays())?updateConfig.getDays():3);
        if(Objects.nonNull(context)){
            updateConfig.setUpdatedBy(context.getUserId());
        }

        if(CollectionUtils.isEmpty(sdpUatJobRunningConfigs)){
            if(Objects.nonNull(context)){
                updateConfig.setCreatedBy(context.getUserId());
            }
           return getMapper().insert(updateConfig);
        }else {
            updateConfig.setId(sdpUatJobRunningConfigs.get(0).getId());
            return updateSelective(updateConfig);
        }
    }
}