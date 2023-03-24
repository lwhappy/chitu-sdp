package com.chitu.bigdata.sdp.api.bo;

import lombok.Data;

/**
 * @author sutao
 * @create 2021-11-10 22:43
 */

@Data
public class PromResponceInfo {

    /**
     * 状态
     * 成功-- success
     */
    private String status;

    /**
     * prometheus指标属性和值
     */
    private PromDataInfo data;





}
