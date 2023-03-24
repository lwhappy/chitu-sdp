package com.chitu.bigdata.sdp.api.domain;


import com.alibaba.fastjson.annotation.JSONField;
import lombok.Data;

import java.io.Serializable;

/**
 * @author sutao
 * @create 2022-05-20 11:41
 */
@Data
public class HbaseCdcCheckResult implements Serializable {

    @JSONField(ordinal = 1)
    private String checkType;
    private String topic;
    private String key;
    private String rowkey;
    private String cdcHbaseTable;
    private String cdcTopic;
    private long timestamp;
    private long processTime;
    private long onTimer;
    private long checkStartTime;
    private long queryHbaseTime;
    private long checkEndTime;

}
