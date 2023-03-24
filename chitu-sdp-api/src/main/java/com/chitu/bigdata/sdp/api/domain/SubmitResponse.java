

package com.chitu.bigdata.sdp.api.domain;

import lombok.Data;

import java.util.Map;

/**
 * TODO 类功能描述
 *
 * @author chenyun
 * @version 1.0
 * @date 2022/7/19 11:41
 */
@Data
public class SubmitResponse {
    private String applicationId;
    private Map<String, String> configuration;
    private String jobManagerAddress;
}
