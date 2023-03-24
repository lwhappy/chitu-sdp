package com.chitu.bigdata.sdp.api.domain;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 作业运维
 * @author zouchangzhen
 * @date 2022/3/29
 */
@Data
public class OperMaintenance {
    /**
     * 启动-恢复开关：0关 1开
     */
    @NotNull(message = "启动/恢复开关不能为空")
    private Integer start  = 1 ;
    /**
     * 停止-暂停开关：0关 1开
     */
    @NotNull(message = "停止/暂停开关不能为空")
    private Integer stop = 1;
}
