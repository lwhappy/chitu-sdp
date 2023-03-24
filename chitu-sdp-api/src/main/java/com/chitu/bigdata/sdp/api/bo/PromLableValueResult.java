package com.chitu.bigdata.sdp.api.bo;

import lombok.Data;

import java.util.List;

/**
 * @author sutao
 * @create 2022-06-10 16:22
 */
@Data
public class PromLableValueResult {


    /**
     * 状态
     * 成功-- success
     */
    private String status;

    private List<String> data;

}
