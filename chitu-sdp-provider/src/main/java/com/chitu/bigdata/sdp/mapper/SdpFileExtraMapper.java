

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2022-3-21
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.model.SdpFileExtra;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <pre>
 * 数据访问接口
 * </pre>
 */
@Mapper
public interface SdpFileExtraMapper extends GenericMapper<SdpFileExtra, Long> {
    SdpFileExtra queryByFileId(Long id);

    void updateByFileId(SdpFileExtra fileExtra);
}