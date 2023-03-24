package com.chitu.bigdata.sdp.flink.client.util;



import java.util.Collection;
import java.util.Map;


public class Asserts {

    public static boolean isNotNull(Object object){
        return object!=null;
    }

    public static boolean isNull(Object object){
        return object==null;
    }

    public static boolean isNullString(String str){
        return isNull(str)||"".equals(str);
    }

    public static boolean isNotNullString(String str){
        return !isNullString(str);
    }

    public static boolean isEquals(String str1,String str2){
        if(isNull(str1)&&isNull(str2)){
            return true;
        }else if(isNull(str1)||isNull(str2)){
            return false;
        }else{
            return str1.equals(str2);
        }
    }

    public static boolean isEqualsIgnoreCase(String str1,String str2){
        if(isNull(str1)&&isNull(str2)){
            return true;
        }else if(isNull(str1)||isNull(str2)){
            return false;
        }else{
            return str1.equalsIgnoreCase(str2);
        }
    }

    public static boolean isNullCollection(Collection collection) {
        if (isNull(collection)||collection.size()==0) {
            return true;
        }
        return false;
    }

    public static boolean isNotNullCollection(Collection collection) {
        return !isNullCollection(collection);
    }

    public static boolean isNullMap(Map map) {
        if (isNull(map)||map.size()==0) {
            return true;
        }
        return false;
    }

    public static boolean isNotNullMap(Map map) {
        return !isNullMap(map);
    }
}
