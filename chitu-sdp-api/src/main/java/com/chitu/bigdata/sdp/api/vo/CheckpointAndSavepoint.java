package com.chitu.bigdata.sdp.api.vo;

import com.chitu.bigdata.sdp.api.flink.Checkpoint;
import com.chitu.bigdata.sdp.api.model.SdpSavepoint;
import lombok.Data;

import java.util.List;

/**
 * @author zouchangzhen
 * @date 2022/4/19
 */
@Data
public class CheckpointAndSavepoint {
    List<SdpSavepoint> savepoints;
    List<Checkpoint> checkpoints;
}
