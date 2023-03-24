package com.chitu.bigdata.sdp.api.domain;

import com.chitu.cloud.model.Pagination;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * 统计项目，用户，job类
 * @author 587694
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ProjectCountInfo  extends Pagination {
    private static final long serialVersionUID = 1L;
    /**
     * 项目总数
     */
    private Long projectTotal;
    /**
     * job总数
     */
    private Long jobTotal;
    /**
     * 用户总数
     */
    private Long employeeTotal;

}
