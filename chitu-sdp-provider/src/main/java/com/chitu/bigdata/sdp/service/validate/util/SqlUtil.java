package com.chitu.bigdata.sdp.service.validate.util;


public class SqlUtil {

    public static String[] getRemoveNotesStatements(String sql,String sqlSeparator){
        if(Asserts.isNullString(sql)){
            return new String[0];
        }
        return (removeNotes(sql) + "\n").split(sqlSeparator);
    }

    public static String[] getStatements(String sql,String sqlSeparator){
        if(Asserts.isNullString(sql)){
            return new String[0];
        }
        return sql.split(sqlSeparator);
    }

    public static String removeNote(String sql){
        if(Asserts.isNotNullString(sql)) {
            sql = sql.replaceAll("--([^'\r\n]{0,}('[^'\r\n]{0,}'){0,1}[^'\r\n]{0,}){0,}", "").trim();
        }
        return sql;
    }

    public static String clearNotes(String sql) {
        if(Asserts.isNotNullString(sql)) {
            sql = sql.replaceAll("/\\*(.|[\\r\\n])*?\\*/", "").trim();
        }
        return sql;
    }

    public static String removeNotes(String sql){
        sql = removeNote(sql);
        return clearNotes(sql);
    }

}
