package com.chitu.bigdata.sdp.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ESVersion {

    VERSION_6("elasticsearch-6"),
    VERSION_7("elasticsearch-7");

    private String version;

}
