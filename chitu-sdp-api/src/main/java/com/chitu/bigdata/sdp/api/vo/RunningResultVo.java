package com.chitu.bigdata.sdp.api.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 * @author sutao
 * @create 2022-03-02 11:19
 */
@Data
@JsonInclude
public class RunningResultVo {
    private Long jobId;
    private String dataSourceType;
    private String dataSourceName;
    private String databaseName;
    private String metaTableType;
    private String flinkTableName;
    private String metatableName;
    private Long consumedTotal;
    private Long deserializeFailNum;
    private Long pendingRecords;
    private Long sinkSuccessRecords;
    private Long sinkErrorRecords;
    //@JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private String consumedTime;

}
