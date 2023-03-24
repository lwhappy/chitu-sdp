package com.chitu.bigdata.sdp.api.flink;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;

import java.sql.Timestamp;

/**
 * @author zouchangzhen
 * @date 2022/4/19
 */
@Data
public class Checkpoint {
    private Integer orderNum;

    private String filePath;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    Timestamp triggerTime;
}
