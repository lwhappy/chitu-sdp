

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.model.SdpEngine;
import com.chitu.bigdata.sdp.api.model.SdpProjectEngine;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <pre>
 * 项目引擎关系表数据访问接口
 * </pre>
 */
@Mapper
public interface SdpProjectEngineMapper extends GenericMapper<SdpProjectEngine, Long> {

    List<SdpProjectEngine> getProjectEngines(@Param("projectId") Long projectInfoId);

    List<SdpEngine> getEngines(@Param("projectId")  Long projectId);

    List<SdpProjectEngine> selectIsExist(SdpProjectEngine sdpProjectEngine);

    void updateDisable(@Param("projectId")Long projectInfoId, @Param("engineId")Long id);
}