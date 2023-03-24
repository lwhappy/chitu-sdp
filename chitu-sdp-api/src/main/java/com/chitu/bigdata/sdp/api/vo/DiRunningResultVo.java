package com.chitu.bigdata.sdp.api.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 * @author 587694
 */
@Data
@JsonInclude
public class DiRunningResultVo {
    private Long jobId;
    private List<RunningResultVo> consumedTotal;
    private List<RunningResultVo> deserializeFailNum;
    private List<RunningResultVo> pendingRecords;

}
