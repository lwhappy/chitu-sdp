package com.chitu.bigdata.sdp.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class TableLineageRelation {

    private String sourceCatalog;

    private String sourceDatabase;

    private String sourceTable;

    private String targetCatalog;

    private String targetDatabase;

    private String targetTable;


}
