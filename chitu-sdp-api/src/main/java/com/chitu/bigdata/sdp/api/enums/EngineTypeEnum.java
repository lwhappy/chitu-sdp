package com.chitu.bigdata.sdp.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author zouchangzhen
 * @date 2022/10/27
 */
@Getter
@AllArgsConstructor
public enum EngineTypeEnum {
    YARN("yarn"),
    KUBERNETES("kubernetes");


    private String type;
}
