

package com.chitu.bigdata.sdp.api.domain;

import lombok.Data;

import java.util.List;

/**
 * TODO 类功能描述
 *
 * @author chenyun
 * @version 1.0
 * @date 2022/7/18 11:12
 */
@Data
public class ClusterInfo {
    private String clusterName;
    private String clusterCode;
    private String nameServices;
    private String flinkClient114Url;
    private String flinkClient115Url;
    private String hadoopConfDir;
    private String flinkProxyUrl;
    private String flinkHistoryUrl;
    private List<String> namespaces;

    //k8s
    private String dockerFileWorkspace;
    private String dockerHubAddress;
    private String caCertData;
    private String clientCertData;
    private String clientKeyData;
    private String masterUrl;
}
