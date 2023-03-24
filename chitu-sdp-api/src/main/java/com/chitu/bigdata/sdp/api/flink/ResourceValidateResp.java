

package com.chitu.bigdata.sdp.api.flink;

import com.chitu.bigdata.sdp.api.flink.yarn.ResourceMemAndCpu;
import lombok.Data;

/**
 * TODO 类功能描述
 *
 * @author chenyun
 * @version 1.0
 * @date 2022/7/5 20:28
 */
@Data
public class ResourceValidateResp {
    private Boolean success = true;
    private String notice;
    private ResourceMemAndCpu task;
    private ResourceMemAndCpu queue;
    private ResourceMemAndCpu cluster;
}
