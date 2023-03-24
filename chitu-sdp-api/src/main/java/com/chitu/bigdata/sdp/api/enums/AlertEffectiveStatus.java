package com.chitu.bigdata.sdp.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sutao
 * @create 2021-11-08 15:04
 * 监控/告警生效状态
 */
@Getter
@AllArgsConstructor
public enum AlertEffectiveStatus {
    /**
     * 停止监控告警
     */
    STOP("停止"),
    /**
     * 启动监控告警
     */
    START("启动");

    private String desc;

}
