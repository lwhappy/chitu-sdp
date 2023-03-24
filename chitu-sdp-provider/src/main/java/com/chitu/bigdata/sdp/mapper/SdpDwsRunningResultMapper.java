

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-5-9
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.model.SdpDwsRunningResult;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * <pre>
 * 运行结果统计表数据访问接口
 * </pre>
 */
@Mapper
public interface SdpDwsRunningResultMapper extends GenericMapper<SdpDwsRunningResult, Long> {
    SdpDwsRunningResult selectSum(@Param("flinkJobId") String flinkJobId,@Param("sourceType") String sourceType,@Param("databaseTableName") String databaseTableName);
}