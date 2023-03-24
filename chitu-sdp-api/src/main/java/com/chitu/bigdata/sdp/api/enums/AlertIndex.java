package com.chitu.bigdata.sdp.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sutao
 * @create 2021-11-09 15:28
 */
@Getter
@AllArgsConstructor
public enum AlertIndex {
    /**
     * 告警规则："重启次数"
     */
    NUMBER_RESTARTS("重启次数"),
    /**
     * 告警规则："checkpoint次数"
     */
    NUMBER_CHECKPOINT("checkpoint次数"),
    /**
     * 告警规则："作业状态异常"
     */
    INTERRUPT_OPERATION("作业状态异常"),
    /**
     * 告警规则："延迟"
     */
    DELAY("延迟");

    private String desc;

}
