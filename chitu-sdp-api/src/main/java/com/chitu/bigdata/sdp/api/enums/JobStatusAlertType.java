package com.chitu.bigdata.sdp.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 587694
 * @create 2021-1-20 15:28
 */
@Getter
@AllArgsConstructor
public enum JobStatusAlertType {
    JOB_STATUS_CHANGE("作业状态变更"),
    JOB_PULL_UP("作业被自动拉起");
    private String desc;
}
