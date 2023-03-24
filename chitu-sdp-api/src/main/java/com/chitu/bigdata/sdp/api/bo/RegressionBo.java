package com.chitu.bigdata.sdp.api.bo;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.ArrayList;

/**
 * @author 587694
 */
@Data
public class RegressionBo {
    @JsonProperty(value = "nSqlId")
    private Long nSqlId;
    @JsonProperty(value = "cSqlId")
    private Long cSqlId;
    @JsonProperty(value = "jSqlId")
    private Long jSqlId;
    @JsonProperty(value = "dSqlId")
    private Long dSqlId;
    @JsonProperty(value = "dropDimTable")
    private Boolean dropDimTable;
    @JsonProperty(value = "nDorisId")
    private Long nDorisId;
    private ArrayList<Long> ids;
}
