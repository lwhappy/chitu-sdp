package com.chitu.bigdata.sdp.api.domain;

/**
 * 傀儡 不做任何具体实现，hive数据源验证使用，只是证明文件夹存在
 * @author zouchangzhen
 * @date 2022/4/15
 */
public class HiveConnection implements AutoCloseable{
    @Override
    public void close() throws Exception {
        // do nothing
    }
}
