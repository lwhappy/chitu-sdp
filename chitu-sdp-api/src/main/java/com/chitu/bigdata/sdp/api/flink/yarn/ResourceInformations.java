package com.chitu.bigdata.sdp.api.flink.yarn;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

/**
 * @author zouchangzhen
 * @date 2022/4/12
 */
@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResourceInformations {
    List<ResourceInformation> resourceInformation;
}
