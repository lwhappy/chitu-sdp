

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <pre>
 * 项目引擎关系表实体类
 * 数据库表名称：sdp_project_engine
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpProjectEngine extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：项目ID
     * 
     * 数据库字段信息:project_id BIGINT(19)
     */
    private Long projectId;

    /**
     * 字段名称：引擎ID
     * 
     * 数据库字段信息:engine_id BIGINT(19)
     */
    private Long engineId;

    public SdpProjectEngine() {
    }	
}