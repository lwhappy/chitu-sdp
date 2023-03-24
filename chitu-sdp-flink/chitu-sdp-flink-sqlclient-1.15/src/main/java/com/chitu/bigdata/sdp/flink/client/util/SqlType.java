package com.chitu.bigdata.sdp.flink.client.util;


public enum SqlType {

    CREATE("CREATE"),
    INSERT("INSERT"),
    SELECT("SELECT"),
    USE("USE"),
    SHOW("SHOW"),
    DESCRIBE("DESCRIBE"),
    DROP("DROP"),
    ALTER("ALTER"),
    LOAD("LOAD"),
    EXPLAIN("EXPLAIN"),
    UNLOAD("UNLOAD"),
    SET("SET"),
    RESET("RESET"),
    UNKNOWN("UNKNOWN"),
    ;

    private String type;

    SqlType(String type) {
        this.type = type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public boolean equalsValue(String value){
        return type.equalsIgnoreCase(value);
    }
}
