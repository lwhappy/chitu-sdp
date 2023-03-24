package com.chitu.bigdata.sdp.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class TableTypeDetail {

    private String catalog;

    private String database;

    private String table;

    private String type;

}
