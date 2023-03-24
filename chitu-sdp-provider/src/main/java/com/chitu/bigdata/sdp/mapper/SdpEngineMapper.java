

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.domain.QueueAndSourceConfig;
import com.chitu.bigdata.sdp.api.model.SdpEngine;
import com.chitu.cloud.mapper.GenericMapper;
import com.chitu.cloud.model.Pagination;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * <pre>
 * 引擎数据访问接口
 * </pre>
 */
@Mapper
public interface SdpEngineMapper extends GenericMapper<SdpEngine, Long> {
    List<SdpEngine> searchPage(Pagination<SdpEngine> p);

    List<SdpEngine> getProjectEngines(@Param("projectId") Long projectId);

    SdpEngine getInfo(SdpEngine instance);

    List<SdpEngine> getByName(@Param("engineName") String engineName);

    List<SdpEngine> getEngineByName(@Param("engineName")String engineName,@Param("userId")Long userId);

   QueueAndSourceConfig getQueueAndSourceConfig(@Param("jobId")Long jobId);

    String queryClusterByJobId(@Param("jobId")Long jobId);

    List<SdpEngine> queryEngineTypeByIds(@Param("ids") Set<Long> ids);
}