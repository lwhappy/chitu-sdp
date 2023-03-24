
/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.domain.SourceConfig;
import com.chitu.cloud.model.OrderByClause;
import io.swagger.annotations.ApiModel;
import lombok.Data;

import java.util.List;


/**
 * @author chenyun
 */
@Data
@ApiModel(value = "对外接口-任务入参")
public class ApiJobBO {
    /**
     * 保存任务入参
     */
    private String businessFlag;

//    private Long businessJobId;

    /**
     * 任务ID
     */
    private Long id;
    /**
     * 文件ID
     */
    private Long fileId;

    private String flinkYaml;

    private String flinkSQL;

    private String flinkVersion;

    private SourceConfig sourceConfig;

    private String jobName;

    private String projectCode;

    private String folderName;
    /**
     * 启动任务入参
     */
    private String startMode;

    private String startOffsets;

    private String startTimestamp;

    private String userId;
    /**
     * 查询任务状态入参
     */
    private List<Long> ids;

    private String sourceType;

    private String latestTime;

    private Integer page;

    private Integer pageSize;

    private List<OrderByClause> orderByClauses;

    private String tableName;
}