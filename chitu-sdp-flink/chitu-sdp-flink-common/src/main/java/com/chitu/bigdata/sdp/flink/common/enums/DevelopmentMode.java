package com.chitu.bigdata.sdp.flink.common.enums;


public enum DevelopmentMode {
    CUSTOMCODE("Custom Code", 1),
    FLINKSQL("Flink SQL", 2);

    String mode;
    Integer value;

    DevelopmentMode(String mode, Integer value) {
        this.mode = mode;
        this.value = value;
    }

    public static DevelopmentMode of(Integer value) {
        for (DevelopmentMode mode : values()) {
            if (mode.value.equals(value)) {
                return mode;
            }
        }
        return null;
    }

    public Integer getValue() {
        return value;
    }

}
