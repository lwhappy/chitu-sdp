package com.chitu.bigdata.sdp.api.enums;

/**
 * @author 587694
 * @description: TODO
 * @date 2021/10/19 10:50
 */
public enum JobConfType {
    BASIC_CONF("基础配置","jobConfig"),
    SQL_CONF("sql配置","sqlConfig"),
    YML_CONF("yml配置","flinkConfig"),
    SOURCE_CONF("资源配置","sourceConfig"),
    DS_CONF("ds配置","dsConfig"),
    JAR_CONF("jar配置","jarConfig");
    private String name;
    private String type;

    JobConfType(String name,String type) {
        this.name = name;
        this.type = type;
    }
    public String getName(){return name;}
    public void setName(String name){ this.name = name; }
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public static   JobConfType getJobConfType(String type){
        JobConfType value1 = null;
        for (JobConfType value : values()) {
            if (value.getType().equals(type)){
                value1 = value;
            }
        }
        return value1;
    }
}
