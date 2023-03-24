package com.chitu.bigdata.sdp.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sutao
 * @create 2021-11-11 9:47
 * 运算符
 */
@Getter
@AllArgsConstructor
public enum Operators {
    GREATER_THAN_EQUAL(">="),
    LESS_THAN_EQUAL("<=");

    private String desc;
}
