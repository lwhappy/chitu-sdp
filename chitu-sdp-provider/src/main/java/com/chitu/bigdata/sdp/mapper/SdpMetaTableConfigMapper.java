

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-12-10
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.model.SdpMetaTableConfig;
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
public interface SdpMetaTableConfigMapper extends GenericMapper<SdpMetaTableConfig, Long> {
    List<SdpMetaTableConfig> getUsingSource(@Param("dataSourceId") Long id);

    List<SdpMetaTableConfig> queryByFileId(@Param("fileId") Long fileId);

    String queryHudiHadoopConfDirByFileId(@Param("fileId") Long fileId);

    int disableByFileId(@Param("fileId") Long fileId);
}