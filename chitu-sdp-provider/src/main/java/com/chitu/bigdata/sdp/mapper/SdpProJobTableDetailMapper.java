

/**
  * <pre>
  * 作   者：chenyun
  * 创建日期：2022-8-17
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.bo.SdpProJobTableDetailBO;
import com.chitu.bigdata.sdp.api.model.SdpProJobTableDetail;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <pre>
 * 项目作业表明细数据访问接口
 * </pre>
 */
@Mapper
public interface SdpProJobTableDetailMapper extends GenericMapper<SdpProJobTableDetail, Long> {

    List<SdpProJobTableDetail> getJobInfo(SdpProJobTableDetailBO lineageBO);
}