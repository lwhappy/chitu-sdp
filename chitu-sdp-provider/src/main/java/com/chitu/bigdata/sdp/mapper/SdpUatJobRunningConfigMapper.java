

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-5-16
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.model.SdpUatJobRunningConfig;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <pre>
 * UAT环境作业运行配置数据访问接口
 * </pre>
 */
@Mapper
public interface SdpUatJobRunningConfigMapper extends GenericMapper<SdpUatJobRunningConfig, Long> {

   List<SdpUatJobRunningConfig> queryByJobIds(@Param("jobIds") List<Long> jobIds);

}