package com.chitu.bigdata.sdp.api.domain;

import lombok.Data;

import javax.validation.constraints.NotNull;

/**
 * 系统白名单用户
 * @author zouchangzhen
 * @date 2022/3/29
 */
@Data
public class WhitelistUser {
    @NotNull(message = "userName不能为空")
    private String userName;

    @NotNull(message = "employeeNumber不能为空")
    private String employeeNumber;

    /*@JsonFormat(
            pattern = "yyyy-MM-dd HH:mm:ss",
            timezone = "GMT+8"
    )
    private Timestamp creationDate;*/
}
