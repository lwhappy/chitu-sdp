

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-11-8
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.bo.SdpOperationLogBO;
import com.chitu.bigdata.sdp.api.model.SdpOperationLog;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;
import java.util.List;

/**
 * <pre>
 * 操作日志数据访问接口
 * </pre>
 */
@Mapper
public interface SdpOperationLogMapper extends GenericMapper<SdpOperationLog, Long> {
    List<SdpOperationLog> searchByJobId(SdpOperationLogBO operationLogBO);

    SdpOperationLog getLogByInstanceId(@Param("instanceId") Long id);

    SdpOperationLog getByInstanceId4JobStatus(@Param("instanceId") Long id);

    ArrayList<SdpOperationLog> selectByInstanceId(@Param("instanceId") Long id);
}