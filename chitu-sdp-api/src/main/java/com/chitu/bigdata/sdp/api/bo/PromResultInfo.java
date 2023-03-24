package com.chitu.bigdata.sdp.api.bo;

import lombok.Data;

/**
 * @author sutao
 * @create 2021-11-10 22:44
 */
@Data
public class PromResultInfo {

    /**
     * prometheus指标属性
     */
    private PromMetricInfo metric;

    /**
     * prometheus指标值
     */
    private String[] value;

}
