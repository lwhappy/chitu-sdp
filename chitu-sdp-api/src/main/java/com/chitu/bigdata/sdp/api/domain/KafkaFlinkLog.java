package com.chitu.bigdata.sdp.api.domain;

import lombok.Data;

/**
 * @author sutao
 * @create 2022-05-24 15:25
 */
@Data
public class KafkaFlinkLog {

    private String level;
    private String message;

}
