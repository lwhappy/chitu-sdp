package com.chitu.bigdata.sdp.api.domain;

import lombok.Builder;
import lombok.Data;


@Data
@Builder
public class TableColLineageRelation {

    private String sourceCatalog;

    private String sourceDatabase;

    private String sourceTable;

    private String sourceColumn;

    private String targetCatalog;

    private String targetDatabase;

    private String targetTable;

    private String targetColumn;

}
