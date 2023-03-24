package com.chitu.bigdata.sdp.service;


import com.chitu.bigdata.sdp.api.model.SdpProjectEngine;
import com.chitu.bigdata.sdp.mapper.SdpProjectEngineMapper;
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
public class ProjectEngineService extends GenericService<SdpProjectEngine, Long> {
    public ProjectEngineService(@Autowired SdpProjectEngineMapper sdpProjectEngineMapper) {
        super(sdpProjectEngineMapper);
    }

    public SdpProjectEngineMapper getMapper() {
        return (SdpProjectEngineMapper) super.genericMapper;
    }

}
