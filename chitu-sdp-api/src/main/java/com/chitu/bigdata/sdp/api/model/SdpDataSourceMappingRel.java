

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-10-29
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.chitu.cloud.model.GenericModel;
import com.google.common.base.Joiner;
import lombok.Data;

import java.io.Serializable;
import java.util.Objects;

/**
 * <pre>
 * 转环境数据源映射关系表实体类
 * 数据库表名称：sdp_data_source_mapping_rel
 * </pre>
 */
@Data
public class SdpDataSourceMappingRel extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 字段名称：uat文件id
     * 
     * 数据库字段信息:uat_file_id BIGINT(19)
     */
    private Long uatFileId;

    /**
     * 字段名称：prod文件id
     * 
     * 数据库字段信息:prod_file_id BIGINT(19)
     */
    private Long prodFileId;

    /**
     * 字段名称：uat元表id
     * 
     * 数据库字段信息:uat_meta_table_id BIGINT(19)
     */
    private Long uatDataSourceId;

    /**
     * 字段名称：生产元表id
     * 
     * 数据库字段信息:prod_meta_table_id BIGINT(19)
     */
    private Long prodDataSourceId;

    /**
     * 字段名称：uat 数据源名称
     * 
     * 数据库字段信息:uat_data_source_name VARCHAR(255)
     */
    private String uatDataSourceName;

    /**
     * 字段名称：uat数据源地址
     * 
     * 数据库字段信息:uat_data_source_url VARCHAR(4000)
     */
    private String uatDataSourceUrl;

    /**
     * 字段名称：uat数据源类型
     * 
     * 数据库字段信息:uat_data_source_type VARCHAR(255)
     */
    private String uatDataSourceType;

    /**
     * 字段名称：uat元表类型
     * 
     * 数据库字段信息:uat_meta_table_type VARCHAR(50)
     */
    private String uatMetaTableType;

    /**
     * 字段名称：uat物理表名称
     * 
     * 数据库字段信息:uat_meta_table_name VARCHAR(255)
     */
    private String uatMetaTableName;

    /**
     * 字段名称：prod 数据源名称
     * 
     * 数据库字段信息:prod_data_source_name VARCHAR(255)
     */
    private String prodDataSourceName;

    /**
     * 字段名称：prod数据源地址
     * 
     * 数据库字段信息:prod_data_source_url VARCHAR(4000)
     */
    private String prodDataSourceUrl;

    /**
     * 字段名称：prod数据源类型
     * 
     * 数据库字段信息:prod_data_source_type VARCHAR(255)
     */
    private String prodDataSourceType;

    /**
     * 字段名称：prod元表类型
     * 
     * 数据库字段信息:prod_meta_table_type VARCHAR(50)
     */
    private String prodMetaTableType;

    /**
     * 字段名称：prod物理表名称
     * 
     * 数据库字段信息:prod_meta_table_name VARCHAR(255)
     */
    private String prodMetaTableName;

    /**
     * 字段名称：日志追踪ID
     * 
     * 数据库字段信息:trace_id VARCHAR(128)
     */
    private String traceId;

    public String getUatPriKey(){
        return Joiner.on("_").useForNull("").join(uatFileId,uatMetaTableName, Objects.nonNull(uatDataSourceId)?uatDataSourceId:"");
    }

//    public String getProdPriKey(){
//        return Joiner.on("_").useForNull("").join(prodFileId,prodMetaTableName,StrUtil.isNotBlank(prodDataSourceName)?prodDataSourceName:"");
//    }

    public SdpDataSourceMappingRel() {
    }	
    public Long getUatFileId() {
        return this.uatFileId;
    }

    public void setUatFileId(Long uatFileId) {
        this.uatFileId = uatFileId;
    }
	
    public Long getProdFileId() {
        return this.prodFileId;
    }

    public void setProdFileId(Long prodFileId) {
        this.prodFileId = prodFileId;
    }
	

	
    public String getUatDataSourceName() {
        return this.uatDataSourceName;
    }

    public void setUatDataSourceName(String uatDataSourceName) {
        this.uatDataSourceName = uatDataSourceName;
    }
	
    public String getUatDataSourceUrl() {
        return this.uatDataSourceUrl;
    }

    public void setUatDataSourceUrl(String uatDataSourceUrl) {
        this.uatDataSourceUrl = uatDataSourceUrl;
    }
	
    public String getUatDataSourceType() {
        return this.uatDataSourceType;
    }

    public void setUatDataSourceType(String uatDataSourceType) {
        this.uatDataSourceType = uatDataSourceType;
    }
	
    public String getUatMetaTableType() {
        return this.uatMetaTableType;
    }

    public void setUatMetaTableType(String uatMetaTableType) {
        this.uatMetaTableType = uatMetaTableType;
    }
	
    public String getUatMetaTableName() {
        return this.uatMetaTableName;
    }

    public void setUatMetaTableName(String uatMetaTableName) {
        this.uatMetaTableName = uatMetaTableName;
    }
	
    public String getProdDataSourceName() {
        return this.prodDataSourceName;
    }

    public void setProdDataSourceName(String prodDataSourceName) {
        this.prodDataSourceName = prodDataSourceName;
    }
	
    public String getProdDataSourceUrl() {
        return this.prodDataSourceUrl;
    }

    public void setProdDataSourceUrl(String prodDataSourceUrl) {
        this.prodDataSourceUrl = prodDataSourceUrl;
    }
	
    public String getProdDataSourceType() {
        return this.prodDataSourceType;
    }

    public void setProdDataSourceType(String prodDataSourceType) {
        this.prodDataSourceType = prodDataSourceType;
    }
	
    public String getProdMetaTableType() {
        return this.prodMetaTableType;
    }

    public void setProdMetaTableType(String prodMetaTableType) {
        this.prodMetaTableType = prodMetaTableType;
    }
	
    public String getProdMetaTableName() {
        return this.prodMetaTableName;
    }

    public void setProdMetaTableName(String prodMetaTableName) {
        this.prodMetaTableName = prodMetaTableName;
    }
	
    public String getTraceId() {
        return this.traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}