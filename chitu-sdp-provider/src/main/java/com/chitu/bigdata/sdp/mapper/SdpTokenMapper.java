

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-12
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.model.SdpToken;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <pre>
 * 用户Token数据访问接口
 * </pre>
 */
@Mapper
public interface SdpTokenMapper extends GenericMapper<SdpToken, Long> {
    void deleteByToken(@Param("token") String token);

    SdpToken queryToken(@Param("userId") Long userId);
}