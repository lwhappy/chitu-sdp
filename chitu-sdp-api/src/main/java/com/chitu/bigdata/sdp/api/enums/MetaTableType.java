package com.chitu.bigdata.sdp.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author sutao
 * @create 2021-12-10 12:20
 */
@Getter
@AllArgsConstructor
public enum MetaTableType {

    SOURCE("source"),
    SINK("sink"),
    DIM("dim");

    private String type;

}
