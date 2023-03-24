

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-11-8
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.bo.SdpJarBO;
import com.chitu.bigdata.sdp.api.model.SdpJar;
import com.chitu.cloud.mapper.GenericMapper;
import com.chitu.cloud.model.Pagination;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <pre>
 * jar管理数据访问接口
 * </pre>
 */
@Mapper
public interface SdpJarMapper extends GenericMapper<SdpJar, Long> {
    List<SdpJar> queryJar(Pagination<SdpJar> jarBO);

    List<SdpJar> history(Pagination<SdpJar> x);

    SdpJar queryLatestJar(SdpJarBO jarBO);

    List<SdpJar> searchJar(SdpJarBO jarBO);

    Integer disableByName(@Param("name") String name,@Param("projectId") Long projectId);

    SdpJar myGet(@Param("id")Long id);
}