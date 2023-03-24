package com.chitu.bigdata.sdp.constant;

import lombok.Getter;

/**
 * @author chenyun
 * @description: TODO
 * @date 2022/2/24 16:45
 */
@Getter
public enum BusinessFlag {
    DI("数据集成"),
    SDP("赤兔平台");

    private String flag;

    BusinessFlag(String flag) {
        this.flag = flag;
    }
}
