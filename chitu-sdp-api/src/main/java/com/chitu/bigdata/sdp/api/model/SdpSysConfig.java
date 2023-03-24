

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-3-29
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <pre>
 * 系统配置表实体类
 * 数据库表名称：sdp_sys_config
 * </pre>
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class SdpSysConfig extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：配置类型key
     * 
     * 数据库字段信息:config_key VARCHAR(255)
     */
    private String configKey;

    /**
     * 字段名称：配置value
     * 
     * 数据库字段信息:config_value TEXT(65535)
     */
    private String configValue;

    public SdpSysConfig() {
    }	
}