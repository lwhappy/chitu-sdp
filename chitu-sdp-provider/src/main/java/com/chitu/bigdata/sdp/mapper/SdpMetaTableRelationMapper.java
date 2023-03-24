

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2022-3-31
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.bo.SdpDataSourceBO;
import com.chitu.bigdata.sdp.api.model.SdpMetaTableRelation;
import com.chitu.bigdata.sdp.api.vo.DatasourceMetatableInfo;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <pre>
 * 数据访问接口
 * </pre>
 */
@Mapper
public interface SdpMetaTableRelationMapper extends GenericMapper<SdpMetaTableRelation, Long> {
    void deleteByFileId(@Param("fileId") Long fileId);

    void deleteHiveTable(@Param("fileId") Long fileId);

    List<SdpMetaTableRelation> queryByFileId(@Param("fileId") Long fileId);

    List<SdpMetaTableRelation> queryHiveTable(@Param("fileId") Long fileId);

    /**
     * @param fileId
     */
    void delByFileId(@Param("fileId") Long fileId);

    Integer countMetaTable(SdpMetaTableRelation sdpMetaTableRelation);

    List<DatasourceMetatableInfo> metaTableListByDataSource(SdpDataSourceBO datasourceBo);

    List<SdpMetaTableRelation> getSourceTableByFileId(@Param("fileId") Long fileId,@Param("dataSourceType")String dataSourceType,@Param("metaTableType")String metaTableType);
}