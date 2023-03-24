

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-12
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * <pre>
 * 用户Token实体类
 * 数据库表名称：sdp_token
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpToken extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 
     * 
     * 数据库字段信息:user_id BIGINT(19)
     */
    private Long userId;

    /**
     * 字段名称：token
     * 
     * 数据库字段信息:token VARCHAR(100)
     */
    private String token;

    /**
     * 字段名称：过期时间
     * 
     * 数据库字段信息:expire_time DATETIME(19)
     */
    private Timestamp expireTime;

    public SdpToken() {
    }	
}