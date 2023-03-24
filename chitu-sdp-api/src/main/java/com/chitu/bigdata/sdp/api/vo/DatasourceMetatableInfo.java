package com.chitu.bigdata.sdp.api.vo;

import com.chitu.bigdata.sdp.api.model.SdpMetaTableConfig;
import lombok.Data;

/**
 * 数据源管理 列表 点引用元表数弹出元表信息
 * @author 599718
 * @date 2022-06-14 17:36
 */
@Data
public class DatasourceMetatableInfo  extends SdpMetaTableConfig {
    /**
     * 数据源类型
     */
    private String datasourceType;
    /**
     * 数据源类型
     */
    private String jobName;
    /**
     * 文件夹id
     */
    private String folderId;
    /**
     * 物理表名
     */
    private String tableName;
}
