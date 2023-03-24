

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-12-10
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.model.SdpApprove;
import com.chitu.cloud.mapper.GenericMapper;
import com.chitu.cloud.model.Pagination;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <pre>
 * 数据访问接口
 * </pre>
 */
@Mapper
public interface SdpApproveMapper extends GenericMapper<SdpApprove, Long> {
    List<SdpApprove> queryApply(Pagination<SdpApprove> p);

    Integer queryPending1(@Param("userName") String userName,@Param("status") String status);

    Integer queryPending2(@Param("userName") String userName,@Param("status") String status);

    SdpApprove getApply(SdpApprove approve);

    SdpApprove myGet(@Param("id")Long id);

    void myUpdate(SdpApprove approve);

    Integer cancelApply(SdpApprove approve);

    List<SdpApprove> queryByFileId(@Param("fileId") Long id);

}