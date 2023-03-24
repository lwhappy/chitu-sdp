

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;


import com.chitu.cloud.model.OrderByClause;
import lombok.Data;

import java.util.List;


/**
 * <pre>
 * 项目业务实体类
 * </pre>
 */
@Data
public class SdpProjectBTO {

    private String productLineName;
    private String projectName;
    private Integer page;
    private Integer pageSize;
    /**
     * 排序对象
     */
    private List<OrderByClause> orderByClauses;
    private Long userId;
}