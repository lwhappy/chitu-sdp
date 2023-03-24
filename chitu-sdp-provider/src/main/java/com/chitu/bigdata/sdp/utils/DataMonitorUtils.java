package com.chitu.bigdata.sdp.utils;

import lombok.extern.slf4j.Slf4j;

/**
 * @Author liubin
 * @Date 2021/12/14 22:32
 **/
@Slf4j
public class DataMonitorUtils {

    public static void monitorError(String message) {
        log.error("SDP_WARN " + message);
    }

}
