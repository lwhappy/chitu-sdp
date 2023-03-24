package com.chitu.bigdata.sdp.api.domain;

import lombok.Data;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author zouchangzhen
 * @date 2022/5/17
 */
@Data
public class SourceKafkaInfo {

    @NotEmpty(message = "flinkTableName不能为空",groups = {QUERY.class})
    private String flinkTableName;

    @NotEmpty(message = "topic不能为空",groups = {QUERY.class})
    private String topic;

    @NotEmpty(message = "startupMode不能为空",groups = {QUERY.class})
    private String startupMode;

    private String dateStr;

    @NotNull(message = "作业id不能为空",groups = {QUERY.class})
    private Long jobId;

    private String waitConsumedNum;

    private String offsets;

    public interface QUERY{}
}
