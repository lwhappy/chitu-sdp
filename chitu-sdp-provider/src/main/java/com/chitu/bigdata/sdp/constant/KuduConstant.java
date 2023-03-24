package com.chitu.bigdata.sdp.constant;

/**
 * @author zouchangzhen
 * @date 2022/4/7
 */
public class KuduConstant {
    /**
     * kudu表名格式
     */
    public final static String TABLE_NAME = "impala::%s.%s";

    public static String getKuduTableName(String dbName,String tableName){
        return String.format(TABLE_NAME,dbName,tableName);
    }
}
