package com.chitu.bigdata.sdp.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 通知用户类型
 * @author zouchangzhen
 * @date 2022/04/22
 */
@Getter
@AllArgsConstructor
public enum NotifiUserType {
    COMMON_NOTICE_USERS("常规通知名单"),
    ALARM_NOTICE_USERS("告警通知名单");
    private String desc;
}
