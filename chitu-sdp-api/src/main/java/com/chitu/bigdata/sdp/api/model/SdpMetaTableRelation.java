

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2022-3-31
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.chitu.cloud.model.GenericModel;
import com.google.common.base.Joiner;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <pre>
 * 实体类
 * 数据库表名称：sdp_meta_table_relation
 * </pre>
 * @author 587694
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpMetaTableRelation extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：文件ID
     * 
     * 数据库字段信息:file_id BIGINT(19)
     */
    private Long fileId;

    /**
     * 字段名称：数据库名
     * 
     * 数据库字段信息:database_name VARCHAR(255)
     */
    private String databaseName;

    /**
     * 字段名称: 物理表名称
     * 
     * 数据库字段信息:meta_table_name VARCHAR(255)
     */
    private String metaTableName;

    /**
     * 字段名称：元表类型
     * 
     * 数据库字段信息:meta_table_type VARCHAR(50)
     */
    private String metaTableType;

    /**
     * 字段名称：数据源ID
     * 
     * 数据库字段信息:data_source_id BIGINT(19)
     */
    private Long dataSourceId;

    /**
     * 字段名称：元表ID
     * 
     * 数据库字段信息:meta_table_id BIGINT(19)
     */
    private Long metaTableId;

    //元表名称
    private String flinkTableName;


    @TableField(exist = false)
    private String dataSourceUrl;

    public String getPriKey(){
        //哪个文件哪个引用名称可以兼容大部分的唯一性了，但是有catalog的情况，只能加上数据源、数据库、物理表确定唯一性。
        return Joiner.on("/").useForNull("").join(fileId,dataSourceId,flinkTableName,databaseName,metaTableName);
    }

    public SdpMetaTableRelation() {
    }	
}