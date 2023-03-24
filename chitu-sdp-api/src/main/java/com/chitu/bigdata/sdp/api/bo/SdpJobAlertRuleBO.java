

/**
 * <pre>
 * 作   者：CHENYUN
 * 创建日期：2021-11-8
 * </pre>
 */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.model.RuleContent;
import com.chitu.cloud.model.OrderByClause;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * <pre>
 * job告警规则业务实体类
 * @author sutao10
 * </pre>
 */
@Data
public class SdpJobAlertRuleBO {

    @ApiModelProperty(value = "用于分页查询")
    private Integer page;
    @ApiModelProperty(value = "用于分页查询")
    private Integer pageSize = 10;
    @ApiModelProperty(value = "排序字段数组")
    private List<OrderByClause> orderByClauses;


    @ApiModelProperty(value = "主键id，修改/删除时必传")
    @NotNull(message = "主键不能为空", groups = {Update.class})
    private Long id;

    @ApiModelProperty(value = "jobId", required = true)
    @NotNull(message = "jobId不能为空", groups = {Save.class, Update.class})
    private Long jobId;

    @ApiModelProperty(value = "规则生成类型:SYSTEM_AUTOMATIC/CUSTOMIZE", required = true)
    @NotNull(message = "规则类型不能为空", groups = {Save.class, Update.class})
    private String ruleGenerateType;

    @ApiModelProperty(value = "规则名称", required = true)
    @NotEmpty(message = "规则名称不能为空", groups = {Save.class, Update.class})
    private String ruleName;

    @ApiModelProperty(value = "规则描述", required = true)
    @NotEmpty(message = "规则描述不能为空", groups = {Save.class, Update.class})
    private String ruleDesc;

    @ApiModelProperty(value = "指标:NUMBER_RESTARTS/NUMBER_CHECKPOINT/DELAY/INTERRUPT_OPERATION", required = true)
    private String indexName;

    @ApiModelProperty(value = "规则内容", required = true)
    @Valid
    @NotNull(message = "规则内容不能为空", groups = {Save.class, Update.class})
    private RuleContent ruleContent;

    @ApiModelProperty(value = "生效时间，例如00:00-10:00", required = true)
    @NotEmpty(message = "生效时间不能为空", groups = {Save.class, Update.class})
    private String effectiveTime;

    @ApiModelProperty(value = "告警频率(分钟)", required = true)
    @NotNull(message = "告警频率不能为空", groups = {Save.class, Update.class})
    private Integer alertRate;

    @ApiModelProperty(value = "通知方式:CROSS_SOUND/SHORT_MESSAGE/TELEPHONE", required = true)
//    @NotNull(message = "通知方式不能为空", groups = {Save.class, Update.class})
    private String notifiType;

    @ApiModelProperty(value = "通知用户，例如[{\"employee_number\":493026,\"employee_name\":\"苏涛\"},{\"employee_number\":123456,\"employee_name\":\"张三\"}]", required = true)
    @NotEmpty(message = "通知用户不能为空", groups = {Save.class, Update.class})
    private String notifyUsers;

    @ApiModelProperty(value = "生效状态:STOP/START", required = true)
    @NotNull(message = "生效状态不能为空", groups = {Save.class, Update.class})
    private String effectiveState;


    /**
     * 保存的时候校验分组
     */
    public interface Save {
    }

    /**
     * 更新的时候校验分组
     */
    public interface Update {
    }


}