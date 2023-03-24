package com.chitu.bigdata.sdp.api.domain;

import lombok.Data;

import java.util.List;

/**
 * @author 587694
 */
@Data
public class SourceMeta {
    private List<String> topics;
    private String kafkaServer;
    private Long dataSourceId;
    private List<String> tables;
    private String sourceType;
    private Integer replica;
    private Integer numPartitions;
}
