

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.model;

import com.baomidou.mybatisplus.annotation.TableField;
import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <pre>
 * 版本实体类
 * 数据库表名称：sdp_version
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpVersion extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    private String fileName;
    /**
     * 字段名称：版本号
     * 
     * 数据库字段信息:version_number VARCHAR(255)
     */
    private String fileVersion;

    /**
     * 字段名称：文件内容
     * 
     * 数据库字段信息:file_content TEXT(65535)
     */
    private String fileContent;
    /**
     * 字段名称：元表sql
     *
     * 数据库字段信息:meta_table_content TEXT(65535)
     */
    private String metaTableContent;
    /**
     * 字段名称：转换语句sql
     *
     * 数据库字段信息:etl_content TEXT(65535)
     */
    private String etlContent;

    /**
     * 字段名称：元表json
     *
     * 数据库字段信息:meta_table_json TEXT(65535)
     */
    private String metaTableJson;
    /**
     * 字段名称：配置内容
     * 
     * 数据库字段信息:config_content TEXT(65535)
     */
    private String configContent;

    /**
     * 字段名称：资源内容
     * 
     * 数据库字段信息:source_content TEXT(65535)
     */
    private String sourceContent;

    /**
     * 字段名称：dataStream配置
     *
     * 数据库字段信息:data_stream_config TEXT(65535)
     */
    private String dataStreamConfig;

    /**
     * 字段名称：备注
     * 
     * 数据库字段信息:remark VARCHAR(255)
     */
    private String remark;

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
    /**
     * 字段名称:是否可删
     * 请求返回类的字段
     */
    @TableField(exist = false)
    private Integer enabledDel;

    public SdpVersion() {
    }
    public SdpVersion(Long fileId) {
        this.fileId= fileId;
    }

    public SdpVersion(Long fileId,String fileVersion) {
        this.fileId= fileId;
        this.fileVersion = fileVersion;
    }

}