package com.chitu.bigdata.sdp.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum DataSourceType {
    MYSQL("mysql"),
    ES("elasticsearch"),
    KAFKA("kafka"),
    HIVE("hive"),
    HBASE("hbase"),
    DATAGEN("datagen"),
    PRINT("print");

    private String type;

    public static DataSourceType ofType(String type){
        for (DataSourceType value : values()) {
            if (value.getType().equalsIgnoreCase(type)){
                return value;
            }
        }
        return null;
    }

//    public static String replaceType(String type){
//        switch (type){
//            case "mysql":
//            case "doris":
//                return MYSQL.type;
//            case "elasticsearch":
//                return ES.type;
//            case "kafka":
//                return KAFKA.type;
//            case "hive":
//                return HIVE.type;
//            default:
//                return type;
//        }
//    }
}
