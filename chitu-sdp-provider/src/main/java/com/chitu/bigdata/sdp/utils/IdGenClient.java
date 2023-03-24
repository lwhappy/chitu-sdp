package com.chitu.bigdata.sdp.utils;

import java.util.Random;

/**
 * @author chenyun
 * @create 2021-10-12 11:01
 */
public class IdGenClient {

    public static Long getId() {
        return new Random().nextLong();
    }

}
