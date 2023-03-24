package com.chitu.bigdata.sdp.api.vo;

import com.chitu.bigdata.sdp.api.model.SdpDataSource;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

@EqualsAndHashCode(callSuper = true)
@Data
public class DataSourceResp  extends SdpDataSource implements Serializable {

    private String ownerName;
    private Integer isUsed;
    /**
     * 统计 元表
     */
    private Integer countMetaTable;
    /**
     * 统计 作业
     */
    private Integer countJob;

    private String projectName;


}
