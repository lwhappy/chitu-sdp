package com.chitu.bigdata.sdp.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 表格式類型
 *
 * @author zouchangzhen
 * @date 2022/3/28
 */
@Getter
@AllArgsConstructor
public enum FormatType {
    JSON("json"),
    CANAL_JSON("canalJson"),
    RAW("raw");

    /**
     * type需要遵循驼峰命名的格式，后面根据类型获取bean的时候要用到
     */
    private String type;
}
