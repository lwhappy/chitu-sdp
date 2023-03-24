package com.chitu.bigdata.sdp.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sutao
 * @create 2021-08-18 20:18
 * 告警生成规则类型
 */
@Getter
@AllArgsConstructor
public enum AlertGenerateRuleType {
    /**
     * 系统自动生成
     */
    SYSTEM_AUTOMATIC("系统自动"),
    /**
     * 用户自定义
     */
    CUSTOMIZE("自定义");

    private String desc;

}
