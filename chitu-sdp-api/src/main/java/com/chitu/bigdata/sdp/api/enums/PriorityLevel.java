package com.chitu.bigdata.sdp.api.enums;

/**
 * @author chenyun
 * @description: TODO 作业/项目等级枚举
 * @date 2021/06/22 10:50
 */
public enum PriorityLevel {
    P4("P4",4),
    P3("P3",3),
    P2("P2",2),
    P1("P1",1);

    private String name;
    private Integer type;

    PriorityLevel(String name, Integer type) {
        this.name = name;
        this.type = type;
    }
    public String getName(){return name;}
    public void setName(String name){ this.name = name; }
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public static PriorityLevel getPriority(String name){
        PriorityLevel value1 = null;
        for (PriorityLevel value : values()) {
            if (value.getName().equals(name)){
                value1 = value;
            }
        }
        return value1;
    }
}
