

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import lombok.Data;

/**
 * <pre>
 * 项目用户关系表业务实体类
 * </pre>
 */
@Data
public class SdpProjectUserBO {
    private Long id;
    private Long projectId;

    /**
     * 字段名称：用户ID
     *
     * 数据库字段信息:user_id BIGINT(19)
     */
    private Long userId;
}