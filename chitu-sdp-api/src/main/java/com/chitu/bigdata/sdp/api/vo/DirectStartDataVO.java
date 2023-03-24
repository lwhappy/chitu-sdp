package com.chitu.bigdata.sdp.api.vo;

import com.chitu.bigdata.sdp.api.domain.SourceKafkaInfo;
import lombok.Data;

import java.util.List;

/**
 * @author zouchangzhen
 * @date 2022/5/16
 */
@Data
public class DirectStartDataVO {

    private Integer uatJobRunningValidDays;

    List<SourceKafkaInfo> sourceKafkaList;

}
