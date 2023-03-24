

/**
 * <pre>
 * 作   者：CHENYUN
 * 创建日期：2021-12-10
 * </pre>
 */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.utils.InsertGroup;
import com.chitu.bigdata.sdp.api.utils.QueryGroup;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * <pre>
 * 业务实体类
 * @author chenyun
 * </pre>
 */
@Data
@ApiModel(value = "元表配置入参")
public class SdpMetaTableConfigBO {

    @ApiModelProperty(value = "元表id", required = true)
    private Long id;

    @ApiModelProperty(value = "文件id", required = true)
    @NotNull(message = "文件id不能为空", groups = {QueryGroup.class, Generate.class})
    private Long fileId;

    @ApiModelProperty(value = "数据源id", required = true)
    @NotNull(message = "数据源id不能为空", groups = {Generate.class, Verify.class, InsertGroup.class})
    private Long dataSourceId;

    @ApiModelProperty(value = "元表名称", required = false)
    /**
     * @NotEmpty(message = "元表名称不能为空", groups = {Generate.class, Verify.class})
     */
    private String metaTableName;

    @ApiModelProperty(value = "引用名称", required = true)
    @NotEmpty(message = "引用名称不能为空", groups = Generate.class)
    private String flinkTableName;

    @ApiModelProperty(value = "元表类型", required = false)
    /**
     * @NotEmpty(message = "元表类型不能为空", groups = Generate.class)
     */
    private String metaTableType;

    private String env;


    public interface Generate {
    }

    public interface Verify {
    }
}