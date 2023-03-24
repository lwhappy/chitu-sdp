

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.model.SdpVersion;
import com.chitu.cloud.mapper.GenericMapper;
import com.chitu.cloud.model.Pagination;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <pre>
 * 版本数据访问接口
 * </pre>
 */
@Mapper
public interface SdpVersionMapper extends GenericMapper<SdpVersion, Long> {
    SdpVersion getVersionByFileId(SdpVersion version);

    Integer cleanJobVersion();

    List<SdpVersion> getVersions(Pagination<SdpVersion> pagination);

    SdpVersion selectLastVersion(@Param("fileId") Long fileId);
}