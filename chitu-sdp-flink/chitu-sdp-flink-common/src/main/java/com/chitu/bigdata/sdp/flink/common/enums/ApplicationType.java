package com.chitu.bigdata.sdp.flink.common.enums;


public enum ApplicationType {
    /**
     * SDP Flink
     */
    SDP_FLINK(1, "SDP Flink"),
    /**
     * Apache Flink
     */
    APACHE_FLINK(2, "Apache Flink"),
    /**
     * SDP Spark
     */
    SDP_SPARK(3, "SDP Spark"),
    /**
     * Apache Spark
     */
    APACHE_SPARK(4, "Apache Spark");
    int type;
    String name;

    ApplicationType(int type, String name) {
        this.type = type;
        this.name = name;
    }

    public int getType() {
        return type;
    }

    public String getName() {
        return name;
    }

    public static ApplicationType of(int type) {
        for (ApplicationType appType : ApplicationType.values()) {
            if (appType.getType() == type) {
                return appType;
            }
        }
        return null;
    }
}
