

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-12
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import lombok.Data;

import java.sql.Timestamp;

/**
 * <pre>
 * 用户Token业务实体类
 * </pre>
 * @author 587694
 */
@Data
public class SdpTokenBO {
    private Long id;
    private Long userId;
    private String token;
    private Timestamp expireTime;
}