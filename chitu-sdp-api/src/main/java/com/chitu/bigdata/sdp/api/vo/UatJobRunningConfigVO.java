package com.chitu.bigdata.sdp.api.vo;

import lombok.Data;

/**
 * @author zouchangzhen
 * @date 2022/5/17
 */
@Data
public class UatJobRunningConfigVO {
    /**
     * 运行环境
     */
    private String env;

    /**
     * 运行时长设置
     */
    private String runTimeSetting;

    /**
     * 已运行时长
     */
    private String executeDuration;
}
