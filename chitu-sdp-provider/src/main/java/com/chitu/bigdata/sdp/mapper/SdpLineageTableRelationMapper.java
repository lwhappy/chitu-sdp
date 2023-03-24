

/**
  * <pre>
  * 作   者：chenyun
  * 创建日期：2022-8-17
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.bo.SdpLineageTableRelationBO;
import com.chitu.bigdata.sdp.api.model.SdpLineageTableRelation;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <pre>
 * 表关系数据访问接口
 * </pre>
 */
@Mapper
public interface SdpLineageTableRelationMapper extends GenericMapper<SdpLineageTableRelation, Long> {

    List<SdpLineageTableRelation> queryByJobId(@Param("jobId") Long jobId);

    List<SdpLineageTableRelation> queryRelations(SdpLineageTableRelationBO lineageBO);
}