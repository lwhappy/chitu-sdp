package com.chitu.bigdata.sdp.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @author sutao
 * @create 2021-11-09 17:13
 */
@Data
public class SdpJobAlertRecordVo {

    private Long id;

    private Long jobId;

    private Long ruleId;

    @ApiModelProperty(value = "告警时间")
    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "GMT+8"
    )
    private Timestamp alertTime;

    @ApiModelProperty(value = "告警事件")
    private String alertContent;

    private String ruleGenerateType;

    @ApiModelProperty(value = "规则名称")
    private String ruleName;

    private String ruleDesc;

    private String indexName;

    @ApiModelProperty(value = "告警规则")
    private String ruleContent;

    private String effectiveTime;

    private Integer alertRate;

    private String notifiType;

    private String notifyUsers;

    private String effectiveState;

    private String createdBy;
    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "GMT+8"
    )
    private Timestamp creationDate;
    private String updatedBy;
    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "GMT+8"
    )
    private Timestamp updationDate;

}
