
package com.chitu.bigdata.sdp.flink.common.enums;

public enum Semantic {

    /**
     *
     */
    EXACTLY_ONCE,

    /**
     *
     */
    AT_LEAST_ONCE,

    /**
     *
     */
    NONE;

    public static Semantic of(String name) {
        for (Semantic semantic : Semantic.values()) {
            if (name.equals(semantic.name())) {
                return semantic;
            }
        }
        return null;
    }

}
