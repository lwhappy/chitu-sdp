package com.chitu.bigdata.sdp.service;


import com.chitu.bigdata.sdp.api.model.SdpProjectUser;
import com.chitu.bigdata.sdp.mapper.SdpProjectUserMapper;
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
public class ProjectUserService extends GenericService<SdpProjectUser, Long> {
    public ProjectUserService(@Autowired SdpProjectUserMapper sdpProjectUserMapper) {
        super(sdpProjectUserMapper);
    }

    public SdpProjectUserMapper getMapper() {
        return (SdpProjectUserMapper) super.genericMapper;
    }

}
