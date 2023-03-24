package com.chitu.bigdata.sdp.api.vo;

import com.alibaba.fastjson.JSONObject;
import lombok.Data;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/12/13 22:04
 */
@Data
public class SdpApproveVO {
    private String content;
    private String description;
    private String metaTable;
    private String fileType;
    private JSONObject approveFlow;
    private String projectName;
    private String projectCode;
    private String jobName;

}
