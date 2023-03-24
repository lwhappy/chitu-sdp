package com.chitu.bigdata.sdp.api.enums;

/**
 * @author 587694
 * @description: TODO
 * @date 2021/12/23 11:30
 */
public enum FileType {
    /**
     * 作业类型:SQL
     */
    SQL_STREAM("SQL"),
    /**
     * 作业类型:DS
     */
    DATA_STREAM("DS");
    private String type;
    FileType(String type){this.type = type;};

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
