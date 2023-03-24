package com.chitu.bigdata.sdp.api.domain;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author zouchangzhen
 * @date 2022/4/12
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class QueueAndSourceConfig extends SourceConfig{
    private String engineQueue;
    private String uatEngineQueue;
    private String jobName;
    private String configContent;
}
