

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-25
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.model.SdpRawStatusHistory;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <pre>
 * 作业状态历史数据访问接口
 * </pre>
 */
@Mapper
public interface SdpRawStatusHistoryMapper extends GenericMapper<SdpRawStatusHistory, Long> {

    @Override
    int insert(SdpRawStatusHistory data);
}