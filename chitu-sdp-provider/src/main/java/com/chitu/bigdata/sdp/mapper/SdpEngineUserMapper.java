

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.domain.EngineUserInfo;
import com.chitu.bigdata.sdp.api.model.SdpEngineUser;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <pre>
 * 引擎用户关系表数据访问接口
 * </pre>
 */
@Mapper
public interface SdpEngineUserMapper extends GenericMapper<SdpEngineUser, Long> {

    SdpEngineUser selectIsExist(SdpEngineUser sdpEngineUser);


    void updateDisable(SdpEngineUser sdpEngineUser);

    List<EngineUserInfo> getEngineUsers(@Param("engineId") Long id);


}