

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-11-16
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.model.SdpRuntimeLog;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <pre>
 * 运行日志实体类
 * 数据库表名称：sdp_runtime_log
 * </pre>
 * @author 587694
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpRuntimeLogBO extends SdpRuntimeLog implements Serializable {

    private static final long serialVersionUID = 1L;
    /**
     * 最后更新时间
     */
    private String latestTime;
}