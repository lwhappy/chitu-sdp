package com.chitu.bigdata.sdp.api.vo;

import lombok.Data;

/**
 * sql校验返回信息扩展
 * @author zouchangzhen
 * @date 2022/9/19
 */
@Data
public class ExplainExt {
    /**
     * 类型，{@Link com.chitu.bigdata.sdp.api.enums.ExplainInfoEnum}
     */
    private Integer type;
    private Object obj;
}
