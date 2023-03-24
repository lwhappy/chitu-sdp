

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-11-16
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.bo.SdpRuntimeLogBO;
import com.chitu.bigdata.sdp.api.model.SdpRuntimeLog;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.ArrayList;

/**
 * <pre>
 * 运行日志数据访问接口
 * </pre>
 */
@Mapper
public interface SdpRuntimeLogMapper extends GenericMapper<SdpRuntimeLog, Long> {
    ArrayList<SdpRuntimeLog> getByJobId(SdpRuntimeLogBO job);

    SdpRuntimeLog getInfo4Url(@Param("jobId")Long jobId);

    void cycleDel();

    void deleteByJobId(@Param("jobId")Long jobId);

    ArrayList<SdpRuntimeLog> getDiLog(SdpRuntimeLogBO sdpRuntimeLogBO);
}