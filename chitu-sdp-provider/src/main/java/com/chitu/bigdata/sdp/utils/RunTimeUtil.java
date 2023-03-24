package com.chitu.bigdata.sdp.utils;

public class RunTimeUtil {
    public static void recovery(Object obj) {
        obj = null;
        System.gc();
    }
}
