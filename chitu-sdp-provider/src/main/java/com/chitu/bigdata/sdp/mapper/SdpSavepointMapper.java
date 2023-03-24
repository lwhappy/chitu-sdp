

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-4-18
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.model.SdpSavepoint;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <pre>
 * 保存点记录信息数据访问接口
 * </pre>
 */
@Mapper
public interface SdpSavepointMapper extends GenericMapper<SdpSavepoint, Long> {
    List<SdpSavepoint> queryByJobId(@Param("jobId") Long jobId, @Param("operateStatus")String operateStatus,@Param("size") Integer size);

    /**
     * 查询待清除的所有job
     * @param
     * @return
     */
    List<Long> queryWaitClear();

    /**
     * 跳过保留数据查询非保留的数据
     * @param jobId
     * @param operateStatus
     * @return
     */
    List<SdpSavepoint> queryNonRetainByJobId(@Param("jobId") Long jobId, @Param("operateStatus")String operateStatus,@Param("retainSize") Integer retainSize,@Param("pageSize") Integer pageSize);

    int updateEnabledFlag(@Param("ids") List<Long> ids);
}