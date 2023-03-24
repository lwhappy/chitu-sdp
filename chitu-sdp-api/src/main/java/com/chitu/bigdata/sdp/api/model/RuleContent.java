package com.chitu.bigdata.sdp.api.model;

import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

/**
 * @author sutao
 * @create 2021-11-16 15:58
 */
@Data
public class RuleContent {

    @ApiModelProperty(value = "时间差(m)")
    private Long timeDiff;
    @ApiModelProperty(value = "运算符：GREATER_THAN_EQUAL/LESS_THAN_EQUAL", required = true)
    private String operator;
    @ApiModelProperty(value = "阈值", required = true)
    private String threshold;


}
