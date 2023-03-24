package com.chitu.bigdata.sdp.api.vo;

import com.chitu.bigdata.sdp.api.model.SdpDataSourceMappingRel;
import com.chitu.bigdata.sdp.api.model.SdpVersion;
import lombok.Data;

import java.util.List;

/**
 * 转环境VO
 * @author zouchangzhen
 * @date 2022/10/29
 */
@Data
public class ChangeEnvVO {
    /**
     * 转环境数据源映射关系
     */
    private List<SdpDataSourceMappingRel> sdpDataSourceMappingRels;

    /**
     * 转环境版本比较
     */
    private List<SdpVersion> compareVersions;
}
