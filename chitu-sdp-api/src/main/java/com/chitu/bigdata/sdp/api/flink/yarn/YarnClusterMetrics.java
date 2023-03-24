package com.chitu.bigdata.sdp.api.flink.yarn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

/**
 * @author zouchangzhen
 * @date 2022/4/21
 */
@Data
public class YarnClusterMetrics {
    private ClusterMetrics clusterMetrics;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClusterMetrics{
      private ResourceMemAndCpu totalUsedResourcesAcrossPartition;
      private ResourceMemAndCpu totalClusterResourcesAcrossPartition;
    }
}
