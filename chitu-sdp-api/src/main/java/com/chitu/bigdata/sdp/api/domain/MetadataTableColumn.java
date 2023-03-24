package com.chitu.bigdata.sdp.api.domain;

import com.chitu.bigdata.sdp.api.enums.FormatType;
import lombok.Data;

/**
 * @author sutao
 * @create 2021-12-09 17:27
 */
@Data
public class MetadataTableColumn {

    private String columnName;
    private String columnType;

    /**
     * 是否主键
     */
    private boolean primaryKey;

    /**
     * es特有属性,es7不存在该属性了
     */
    private String indexType;

    /**
     * 标识数据格式类型  目前只有kafka data在用
     */
    private FormatType formatType;

}
