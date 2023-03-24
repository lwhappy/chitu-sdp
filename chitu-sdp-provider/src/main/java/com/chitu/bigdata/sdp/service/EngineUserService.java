package com.chitu.bigdata.sdp.service;

import com.chitu.bigdata.sdp.api.model.SdpEngineUser;
import com.chitu.bigdata.sdp.mapper.SdpEngineUserMapper;
import com.chitu.cloud.service.GenericService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <pre>
 * 引擎业务类
 * </pre>
 */
@Service
@Slf4j
public class EngineUserService extends GenericService<SdpEngineUser, Long> {
    public EngineUserService(@Autowired SdpEngineUserMapper sdpEngineUserMapper) {
        super(sdpEngineUserMapper);
    }

    public SdpEngineUserMapper getMapper() {
        return (SdpEngineUserMapper) super.genericMapper;
    }

    public SdpEngineUser selectIsExist(SdpEngineUser sdpEngineUser) {

        return this.getMapper().selectIsExist(sdpEngineUser);
    }
}
