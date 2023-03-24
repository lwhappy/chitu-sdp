package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.dto.TopicInfoDTO;
import lombok.Data;

import java.util.List;

/**
 * @author zouchangzhen
 * @date 2022/9/21
 */
@Data
public class TopicInfoReqBO {
    private List<TopicInfoDTO> list;

}
