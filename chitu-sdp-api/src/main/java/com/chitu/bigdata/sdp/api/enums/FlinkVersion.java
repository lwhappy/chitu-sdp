package com.chitu.bigdata.sdp.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum FlinkVersion {

    VERSION_114("1.14.3"),
    VERSION_115("1.15.2");

    private String version;

}
