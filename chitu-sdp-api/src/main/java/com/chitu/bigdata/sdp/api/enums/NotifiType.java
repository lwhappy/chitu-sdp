package com.chitu.bigdata.sdp.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sutao
 * @create 2021-11-08 14:59
 * 告警通知方式
 */
@Getter
@AllArgsConstructor
public enum NotifiType {

//    CROSS_SOUND("跨声"),
//    SHORT_MESSAGE("短信"),
//    TELEPHONE("电话"),
    EMAIL("邮箱");

    private String desc;

}
