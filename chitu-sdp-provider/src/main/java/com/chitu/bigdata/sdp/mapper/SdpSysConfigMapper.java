

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-3-29
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.model.SdpSysConfig;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <pre>
 * 系统配置表数据访问接口
 * </pre>
 */
@Mapper
public interface SdpSysConfigMapper extends GenericMapper<SdpSysConfig, Long> {


}