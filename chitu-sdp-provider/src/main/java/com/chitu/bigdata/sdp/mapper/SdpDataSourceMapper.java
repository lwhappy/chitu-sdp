

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-12-9
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.bo.SdpDataSourceBO;
import com.chitu.bigdata.sdp.api.model.SdpDataSource;
import com.chitu.bigdata.sdp.api.vo.DataSourceResp;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <pre>
 * 数据访问接口
 * </pre>
 * @author 587694
 */
@Mapper
public interface SdpDataSourceMapper extends GenericMapper<SdpDataSource, Long> {
    List<DataSourceResp> dataSourceList(SdpDataSourceBO bo);

    List<SdpDataSource> selectByName(SdpDataSource sds);

    List<SdpDataSource> getDataSources(SdpDataSource sds);

    List<SdpDataSource> selectType4exPlain(@Param("fileId") Long id);


    @Deprecated
    List<SdpDataSource> selectForCanalTemp(@Param("projectId") Long projectId);
}