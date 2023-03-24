

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import lombok.Data;

import java.sql.Timestamp;

/**
 * <pre>
 * 作业实例业务实体类
 * </pre>
 * @author chenyun
 */
@Data
public class SdpJobInstanceBO {
    private Long id;
    private Long projectId;
    private Long jobId;
    private Long flinkJobId;
    private String instanceInfo;
    private String applicationId;
    private String configuration;
    private String jobStatus;
    private String rawStatus;
    private String expectStatus;
    private String jobVersion;
    private Boolean isLatest;
    private String jobContent;
    private String configContent;
    private String sourceContent;
    private String dataStreamConfig;
    private Long executeDuration;
    private Timestamp startTime;
    private Timestamp endTime;
    private String savePoint;
}