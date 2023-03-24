package com.chitu.bigdata.sdp.api.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

/**
 * @author sutao
 * @create 2022-03-07 11:16
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FlinkFailMsgVo {

    private String sourceType;
    private String tableName;
    private String data;
    private String desc;
    @JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "GMT+8"
    )
    private Timestamp failTime;

    private String batchCount;


}
