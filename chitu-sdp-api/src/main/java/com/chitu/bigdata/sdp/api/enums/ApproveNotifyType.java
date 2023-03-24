package com.chitu.bigdata.sdp.api.enums;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * @author 587694
 */

@Getter
@AllArgsConstructor
public enum ApproveNotifyType {
    /**
     * 提交申请
     */
    SUBMIT("submit"),
    /**
     * 审批申请
     */
    EXECUTE("execute");
    private String type;
}
