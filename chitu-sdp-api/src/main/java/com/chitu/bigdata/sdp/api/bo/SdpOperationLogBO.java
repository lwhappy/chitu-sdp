

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.enums.JobAction;
import com.chitu.bigdata.sdp.api.enums.LogStatus;
import com.chitu.cloud.model.OrderByClause;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.HashMap;
import java.util.List;

/**
 * <pre>
 * 操作记录入参类
 * </pre>
 * @author 587694
 */
@Data
@ApiModel(value = "操作记录入参")
public class SdpOperationLogBO  {
    @ApiModelProperty("作业ID")
    private Long jobId;

    private Integer page;
    private Integer pageSize;
    /**
     * 排序对象
     */
    private List<OrderByClause> orderByClauses;

    private HashMap<String,String> jobActions = JobAction.getJobActionMap();

    private HashMap<String,String> logStatus = LogStatus.getLogStatusMap();

}