

/**
 * <pre>
 * 作   者：CHENYUN
 * 创建日期：2021-12-10
 * </pre>
 */

package com.chitu.bigdata.sdp.api.model;

import com.chitu.cloud.model.GenericModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * <pre>
 * 实体类
 * 数据库表名称：sdp_meta_table_config
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpMetaTableConfig extends GenericModel<Long> implements Serializable {
    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "文件id", required = true)
    @NotNull(message = "文件id不能为空")
    private Long fileId;

    @ApiModelProperty(value = "引用名称", required = true)
    @NotEmpty(message = "引用名称不能为空")
    private String flinkTableName;

    @ApiModelProperty(value = "数据源id", required = true)
    @NotNull(message = "数据源id不能为空")
    private Long dataSourceId;

    @ApiModelProperty(value = "元表名称", required = false)
    // @NotEmpty(message = "元表名称不能为空")
    private String metaTableName;

    @ApiModelProperty(value = "元表类型", required = false)
    //@NotEmpty(message = "元表类型不能为空")
    private String metaTableType;

    @ApiModelProperty(value = "ddl", required = true)
    @NotEmpty(message = "ddl不能为空")
    private String flinkDdl;

    public SdpMetaTableConfig() {

    }

    public SdpMetaTableConfig(Long fileId) {
        this.fileId = fileId;
    }

    public SdpMetaTableConfig(Long fileId, String flinkTableName) {
        this.fileId = fileId;
        this.flinkTableName = flinkTableName;
    }
}