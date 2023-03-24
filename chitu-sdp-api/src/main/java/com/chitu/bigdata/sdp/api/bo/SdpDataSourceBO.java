

/**
 * <pre>
 * 作   者：CHENYUN
 * 创建日期：2021-10-15
 * </pre>
 */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.cloud.model.OrderByClause;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;

/**
 * <pre>
 * 数据源管理入参类
 * </pre>
 * @author 587694
 */
@Data
@ApiModel(value = "数据源管理入参")
public class SdpDataSourceBO {

    private String dataSourceType;
    private String dataSourceName;
    private Long projectId;
    private Integer page;
    private Integer pageSize;
    /**
     * 排序对象
     */
    private List<OrderByClause> orderByClauses;
    /**
     * 作业名称
     */
    private String jobName;
    /**
     * 物理表名称
     */
    private String tableName;
    /**
     * 元表名称
     */
    private String metaTableName;
    /**
     * 数据源id
     */
    private Long id;

    private String projectName;


    private String env;


}