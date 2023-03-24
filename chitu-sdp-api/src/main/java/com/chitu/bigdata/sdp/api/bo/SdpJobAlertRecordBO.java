

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-11-8
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.cloud.model.OrderByClause;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.util.List;

/**
 * <pre>
 * job告警记录业务实体类
 * </pre>
 * @author sutao10
 */
@Data
public class SdpJobAlertRecordBO {

    private Long jobId;
    private Integer page;
    private Integer pageSize = 10;
    @ApiModelProperty(value = "排序字段数组")
    private List<OrderByClause> orderByClauses;


}