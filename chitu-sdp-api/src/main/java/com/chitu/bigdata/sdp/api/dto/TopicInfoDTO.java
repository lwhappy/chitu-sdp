package com.chitu.bigdata.sdp.api.dto;

import com.google.common.base.Joiner;
import lombok.Data;

import java.util.List;

/**
 * @author zouchangzhen
 * @date 2022/9/21
 */
@Data
public class TopicInfoDTO {
    private String appKey;
    private String clusterToken;
    private String topic;
    /**
     * topic是否是正则表达式，0否，1是
     */
    private Integer isRegex = 0;
    /**
     * 0是无权限，1是有权限
     */
    private  Integer readAuth;

    /**
     * 如果是正则，返回没有权限的 topic集合
     */
    private List<String> noAuthTopicList;

    public String getKey(){
        return Joiner.on("_").useForNull("null").join(appKey,clusterToken,topic);
    }
}