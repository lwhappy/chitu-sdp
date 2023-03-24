

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2022-3-21
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <pre>
 * 实体类
 * 数据库表名称：sdp_file_extra
 * </pre>
 * @author 587694
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpFileExtra extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：业务价值
     * 
     * 数据库字段信息:business_value LONGTEXT(2147483647)
     */
    private String businessValue;

    /**
     * 字段名称：技术说明
     * 
     * 数据库字段信息:tech_specifications LONGTEXT(2147483647)
     */
    private String techSpecifications;

    /**
     * 字段名称：项目ID
     * 
     * 数据库字段信息:project_id BIGINT(19)
     */
    private Long projectId;

    /**
     * 字段名称：文件ID
     * 
     * 数据库字段信息:file_id BIGINT(19)
     */
    private Long fileId;

    public SdpFileExtra() {
    }	
}