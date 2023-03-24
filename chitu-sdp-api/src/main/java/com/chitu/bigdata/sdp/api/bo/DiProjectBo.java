package com.chitu.bigdata.sdp.api.bo;

import com.alibaba.fastjson.annotation.JSONField;
import com.chitu.cloud.model.OrderByClause;
import lombok.Data;

import java.util.LinkedList;

/**
 * @author 587694
 */
@Data
public class DiProjectBo {
    @JSONField(ordinal = 0)
    private Integer[] projectIds;
    @JSONField(ordinal = 1)
    private String projectName;
    @JSONField(ordinal = 2)
    private String projectCode;
    @JSONField(ordinal = 3)
    private String creationDateStart;
    @JSONField(ordinal = 4)
    private String creationDateEnd;
    @JSONField(ordinal = 5)
    private String projectMemberUserId;
    @JSONField(ordinal = 6)
    private Integer page;
    @JSONField(ordinal = 7)
    private Integer pageSize;
    @JSONField(ordinal = 8)
    private LinkedList<OrderByClause> orderByClauses;
}
