package com.chitu.bigdata.sdp.api.domain;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 常规通知人员
 * @author zouchangzhen
 * @date 2022/3/29
 */
@Data
public class NoticeUser {
    @NotNull(message = "userName不能为空")
    private String userName;

    @NotNull(message = "employeeNumber不能为空")
    private String employeeNumber;

}
