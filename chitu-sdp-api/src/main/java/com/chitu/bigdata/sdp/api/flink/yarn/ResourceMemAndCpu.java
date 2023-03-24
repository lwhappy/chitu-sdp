package com.chitu.bigdata.sdp.api.flink.yarn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 * @author zouchangzhen
 * @date 2022/4/12
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceMemAndCpu {
    /**
     * 目前看到的单位都是MB ,如果有需要的话，需要到resourceInformations中获取单位计算
     */
    private Integer memory;
    @JsonProperty("vCores")
    private Integer vCores;
    private ResourceInformations resourceInformations;

    public ResourceMemAndCpu() {
    }

    public ResourceMemAndCpu(Integer vCores,Integer memory) {
        this.vCores = vCores;
        this.memory = memory;
    }
}
