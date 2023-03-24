

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.model.SdpJobInstance;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <pre>
 * 作业实例数据访问接口
 * </pre>
 */
@Mapper
public interface SdpJobInstanceMapper extends GenericMapper<SdpJobInstance, Long> {
    List<SdpJobInstance> queryInstance4Sync(SdpJobInstance instance);

    List<SdpJobInstance> queryById(SdpJobInstance instance);

    Integer repairExpectStatus(SdpJobInstance jobInstance );

    Integer changeLatest(SdpJobInstance jobInstance);

    SdpJobInstance queryByJobId(@Param("jobId")Long jobId);

    Integer updateByJobId(SdpJobInstance jobInstance);

    SdpJobInstance queryLatestVersion(SdpJobInstance instance);

    Integer disables(List<Long> ids);

    List<SdpJobInstance> queryByJobIds(List<Long> ids);

    List<SdpJobInstance> queryExceptionInstance();

    List<SdpJobInstance> queryLatestRecords(@Param("jobId")Long jobId,@Param("size") Integer size);

    /**
     * 查询每个job的最近实例
     * @param dateSubDay
     * @return
     */
    List<SdpJobInstance> queryLatestInstance4Job(@Param("dateSubDay")Integer dateSubDay);

    /**
     * 查询某个job最近实例之前的所有实例
     * @param dateSubDay
     * @return
     */
    List<SdpJobInstance> queryBeforeInstanceBaseOnLatest(@Param("jobId")Long jobId,@Param("latestId")Long latestId,@Param("dateSubDay")Integer dateSubDay);

    /**
     * 查询所有正在运行的job
     * @return
     */
    List<SdpJobInstance> queryRunningJob();

    List<SdpJobInstance> queryLatestJobs();

    List<Long> queryFailedJobs(@Param("executeDuration") Long executeDuration);

    SdpJobInstance queryJobById(@Param("jobId")Long jobId);
}