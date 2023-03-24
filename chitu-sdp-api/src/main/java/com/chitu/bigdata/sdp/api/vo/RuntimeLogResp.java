package com.chitu.bigdata.sdp.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

@Data
public class RuntimeLogResp {

    private String logs;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss.SSS",timezone = "GMT+8")
    private Timestamp latestTime;
}
