package com.chitu.bigdata.sdp.api.domain;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author zouchangzhen
 * @date 2022/6/22
 */
@Data
public class MetaTableConfigBatchDomain {
    @NotNull(message = "fileId不能为空")
    private Long fileId;
    @NotNull(message = "fileId不能为空")
    private Long dataSourceId;
    @NotEmpty(message = "metaTableType不能为空")
    private String metaTableType;
    /**
     * 多个使用逗号隔开
     */
    @NotEmpty(message = "topics不能为空")
    private String topics;
}
