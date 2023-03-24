package com.chitu.bigdata.sdp.api.domain;

import lombok.Data;

/**
 * 数据源写入控制
 * @author zouchangzhen
 * @date 2022/4/22
 */
@Data
public class DataSourceWriteContrl {
    /**
     * 数据源类型
     */
    private String dataSourceType;

    /**
     * 需要控制的内容
     */
    private String content;

}
