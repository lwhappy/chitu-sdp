package com.chitu.bigdata.sdp.api.bo;

import com.chitu.cloud.model.OrderByClause;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author sutao
 * @create 2022-03-07 15:41
 */
@Data
public class JobRunResultFailDataBO {

    @NotNull(message = "jobId不能为空")
    private Long jobId;
    @NotEmpty(message = "数据源类型不能为空")
    private String sourceType;
    @NotEmpty(message = "表名不能为空")
    private String tableName;
    @NotNull(message = "page不能为空")
    private Integer page;
    @NotNull(message = "pageSize不能为空")
    private Integer pageSize;
    @NotNull(message = "orderByClauses不能为空")
    private List<OrderByClause> orderByClauses;

    private String databaseName;
}
