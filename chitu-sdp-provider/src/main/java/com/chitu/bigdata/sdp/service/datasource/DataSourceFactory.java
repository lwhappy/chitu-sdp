package com.chitu.bigdata.sdp.service.datasource;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author sutao
 * @create 2021-07-28 9:30
 */
@Component
public class DataSourceFactory {

    @Autowired
    Map<String, AbstractDataSource> abstractDataSourceMap = new ConcurrentHashMap<>(10);

    public AbstractDataSource getDataSource(String dataSourceType) {
        AbstractDataSource dataSource = abstractDataSourceMap.get(dataSourceType);
        if (dataSource == null) {
            throw new RuntimeException("数据源类型不存在!");
        }
        return dataSource;
    }

}
