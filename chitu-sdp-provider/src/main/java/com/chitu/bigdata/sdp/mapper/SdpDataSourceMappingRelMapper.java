

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-10-29
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.model.SdpDataSourceMappingRel;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <pre>
 * 转环境数据源映射关系表数据访问接口
 * </pre>
 */
@Mapper
public interface SdpDataSourceMappingRelMapper extends GenericMapper<SdpDataSourceMappingRel, Long> {
    public List<SdpDataSourceMappingRel>  getByFileIdAndEnv(@Param("env")String env,@Param("fileId")Long fileId);
}