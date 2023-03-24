package com.chitu.bigdata.sdp.api.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

/**
 * @author 587694
 */
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Data
public class TableNameAndAlias {

    private String tableName;

    private String tableAlias;

    private boolean addIsNull;

}
