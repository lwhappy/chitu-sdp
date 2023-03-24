package com.chitu.bigdata.sdp.api.flink.yarn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * "maximumAllocation": 9223372036854775807,
* 	"minimumAllocation": 0,
* 	"name": "memory-mb",
* 	"resourceType": "COUNTABLE",
* 	"units": "Mi",
* 	"value": 20480
 * @author zouchangzhen
 * @date 2022/4/12
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceInformation {
    /**
     * 内存还是cpu
     */
    private String name;
    /**
     * 单位
     */
    private String units;
    /**
     * 值
     */
    private Long value;

}
