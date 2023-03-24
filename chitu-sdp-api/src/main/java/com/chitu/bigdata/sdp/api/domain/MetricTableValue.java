package com.chitu.bigdata.sdp.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author sutao
 * @create 2022-03-02 11:28
 */
@Data
@AllArgsConstructor
public class MetricTableValue {

    private String tableName;
    private Long value;

}
