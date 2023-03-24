package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.domain.ClusterInfo;
import lombok.Data;

/**
 * @author zouchangzhen
 * @date 2022/10/27
 */
@Data
public class ClusterInfoBO extends ClusterInfo {
    private String env;
    private String engineType;
}
