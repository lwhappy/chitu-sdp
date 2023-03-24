

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-5-9
  * </pre>
  */

package com.chitu.bigdata.sdp.service;

import com.chitu.bigdata.sdp.api.model.SdpDwsRunningResult;
import com.chitu.bigdata.sdp.mapper.SdpDwsRunningResultMapper;
import com.chitu.cloud.service.GenericService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <pre>
 * 运行结果统计表业务类
 * </pre>
 */
@Service("sdpDwsRunningResultService")
public class SdpDwsRunningResultService extends GenericService<SdpDwsRunningResult, Long> {
    public SdpDwsRunningResultService(@Autowired SdpDwsRunningResultMapper sdpDwsRunningResultMapper) {
        super(sdpDwsRunningResultMapper);
    }
    
    public SdpDwsRunningResultMapper getMapper() {
        return (SdpDwsRunningResultMapper) super.genericMapper;
    }
}