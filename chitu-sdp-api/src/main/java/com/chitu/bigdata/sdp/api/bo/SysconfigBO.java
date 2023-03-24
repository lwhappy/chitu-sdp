package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.domain.DataSourceWriteContrl;
import com.chitu.bigdata.sdp.api.domain.NoticeUser;
import com.chitu.bigdata.sdp.api.domain.OperMaintenance;
import com.chitu.bigdata.sdp.api.domain.WhitelistUser;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * @author zouchangzhen
 * @date 2022/3/29
 */
@Data
@Valid
public class SysconfigBO {
    @NotNull(message = "作业运维信息不能为空")
    OperMaintenance operMaintenance;

    List<WhitelistUser> whitelists;

    List<NoticeUser> commonNoticeUsers;

    List<NoticeUser> alarmNoticeUsers;

    //资源校验开关
    Boolean resourceValidate;

    /**
     * 前端根据datasourceType来做下来列表，选中
     */
    List<DataSourceWriteContrl> dataSourceWriteContrls;

    /**
     * 规则告警定时任务增加开关
     */
    Boolean alertGlobalSwitch;

    Boolean needTwoApprove;
}
