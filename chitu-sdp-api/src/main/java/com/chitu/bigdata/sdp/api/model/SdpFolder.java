

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
 * 目录实体类
 * 数据库表名称：sdp_folder
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpFolder extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：目录名称
     * 
     * 数据库字段信息:folder_name VARCHAR(255)
     */
    private String folderName;

    /**
     * 字段名称：父目录ID
     * 
     * 数据库字段信息:parent_id BIGINT(19)
     */
    private Long parentId;

    /**
     * 字段名称：项目ID
     * 
     * 数据库字段信息:project_id BIGINT(19)
     */
    private Long projectId;

    private String businessFlag;

    public SdpFolder() {
    }	
}