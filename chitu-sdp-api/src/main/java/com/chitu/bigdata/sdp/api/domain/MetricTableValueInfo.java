package com.chitu.bigdata.sdp.api.domain;

import lombok.Data;

import java.util.Map;

/**
 * @author sutao
 * @create 2022-03-02 16:19
 */
@Data
public class MetricTableValueInfo {
    Long jobId;
    String flinkAppId;
    Map<String,Long> consumedTotalMap;
    Map<String,Long> deserializeFailNumMap;
    Map<String,Long> pendingRecordsMap;
    Map<String,Long> sinkKafkaSuccessRecordMap;

    //sink
    //Map<String,Long> totalFlushRowsMap;
    //Map<String,Long> totalFlushFailedTimesMap;
    Map<String,Long> sinkHiveRecordsMap;

}
