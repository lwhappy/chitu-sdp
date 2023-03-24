package com.chitu.bigdata.sdp.api.domain;

import lombok.Builder;
import lombok.Data;

/**
 * @author sutao
 * @create 2022-03-10 9:24
 */
@Data
@Builder
public class RuleCehckResp {

    /**
     * 命中状态
     */
    private boolean hisFalg;

    /**
     * 描述
     */
    private String desc;

}
