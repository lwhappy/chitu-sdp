package com.chitu.bigdata.sdp.constant;


import java.util.HashMap;
import java.util.Map;

/**
 * @author sutao
 * @create 2021-12-13 15:52
 */
public class FlinkDataTypeMapping {

    public static final Map<String, String> MYSQL_TYPE_MAP = new HashMap<>();
    public static final Map<String, String> ES_TYPE_MAP = new HashMap<>();
    public static final Map<String, String> KUDU_TYPE_MAP = new HashMap<>();

    /**
     * 未匹配上默认类型
     */
    public static final String NOT_EXIST_DEFAULT_TYPE = "VARCHAR";

    static {
        MYSQL_TYPE_MAP.put("TINYINT", "TINYINT");
        MYSQL_TYPE_MAP.put("SMALLINT", "SMALLINT");
        MYSQL_TYPE_MAP.put("INT", "INT");
        MYSQL_TYPE_MAP.put("MEDIUMINT", "INT");
        MYSQL_TYPE_MAP.put("BIGINT", "BIGINT");
        MYSQL_TYPE_MAP.put("FLOAT", "FLOAT");
        MYSQL_TYPE_MAP.put("DOUBLE", "DOUBLE");
        MYSQL_TYPE_MAP.put("NUMERIC", "DECIMAL(p, s)");
        MYSQL_TYPE_MAP.put("DECIMAL", "DECIMAL(p, s)");
        MYSQL_TYPE_MAP.put("BOOLEAN", "BOOLEAN");
        MYSQL_TYPE_MAP.put("DATE", "DATE");
        MYSQL_TYPE_MAP.put("TIME", "TIME");
        MYSQL_TYPE_MAP.put("DATETIME", "TIMESTAMP");
        MYSQL_TYPE_MAP.put("TIMESTAMP", "TIMESTAMP");
        MYSQL_TYPE_MAP.put("CHAR", "CHAR");
        MYSQL_TYPE_MAP.put("VARCHAR", "VARCHAR");
        MYSQL_TYPE_MAP.put("TEXT", "STRING");
        MYSQL_TYPE_MAP.put("BINARY", "BYTES");
        MYSQL_TYPE_MAP.put("VARBINARY", "BYTES");
        MYSQL_TYPE_MAP.put("BLOB", "BYTES");
        MYSQL_TYPE_MAP.put("ARRAY", "ARRAY");


        ES_TYPE_MAP.put("text", "VARCHAR");
        ES_TYPE_MAP.put("keyword", "VARCHAR");
        ES_TYPE_MAP.put("date", "DATE");
        ES_TYPE_MAP.put("boolean", "BOOLEAN");
        ES_TYPE_MAP.put("object", "MAP");
        ES_TYPE_MAP.put("byte", "INT");
        ES_TYPE_MAP.put("short", "INT");
        ES_TYPE_MAP.put("integer", "INT");
        ES_TYPE_MAP.put("long", "BIGINT");
        ES_TYPE_MAP.put("float", "FLOAT");
        ES_TYPE_MAP.put("half_float", "FLOAT");
        ES_TYPE_MAP.put("scaled_float", "FLOAT");
        ES_TYPE_MAP.put("double", "DOUBLE");
    }

}
