package com.chitu.bigdata.sdp.api.bo;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class GetClusterByAddressBO implements Serializable {
    /**
     * kafka集群地址集合
     */
    private List<String> kafkaAddrs;
}
