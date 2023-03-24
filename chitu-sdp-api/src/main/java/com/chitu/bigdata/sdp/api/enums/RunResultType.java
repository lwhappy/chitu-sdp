package com.chitu.bigdata.sdp.api.enums;


/**
 * @author 587694
 */

public enum RunResultType {
    /**
     * 消费的kafka的offset位置
     */
    CONSUMED_TOTAL("consumedTotal"),
    /**
     * 序列化失败数据量
     */
    DESERIALIZE_FAIL("deserializeFail"),
    /**
     * 背压的数据量
     */
    PENDING_RECORDS("pendingRecords");

    private String name;

    RunResultType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    public static RunResultType getCompareName (String name){
        RunResultType value1 = null;
        for (RunResultType value : values()) {
            if (value.getName().equals(name)){
                value1 = value;
            }
        }
        return value1;
    }
}
