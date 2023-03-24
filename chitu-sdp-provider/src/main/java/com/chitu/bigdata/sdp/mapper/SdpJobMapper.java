

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.bo.SdpDataSourceBO;
import com.chitu.bigdata.sdp.api.bo.SdpJarBO;
import com.chitu.bigdata.sdp.api.bo.SdpJobBO;
import com.chitu.bigdata.sdp.api.model.SdpJob;
import com.chitu.bigdata.sdp.api.model.SdpProject;
import com.chitu.bigdata.sdp.api.vo.DatasourceJobInf;
import com.chitu.bigdata.sdp.api.vo.JobResp;
import com.chitu.cloud.mapper.GenericMapper;
import com.chitu.cloud.model.Pagination;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <pre>
 * 作业数据访问接口
 * </pre>
 */
@Mapper
public interface SdpJobMapper extends GenericMapper<SdpJob, Long> {
    List<SdpJob> queryJob4Delete(List<Long> ids);

    Integer disables(List<Long> ids);

    List<String> searchJob(SdpJobBO jobBO);

    List<SdpJob> selectLists(SdpJobBO jobBO);

    List<SdpJob> queryJob(Pagination<SdpJob> p);

    Integer insertOrUpdate(SdpJob job);

    SdpJob queryJob4Compare(@Param("fileId") Long fileId);

    Integer rollback(SdpJob job);

    SdpJob queryJobByFileId(@Param("fileId") Long fileId);

    List<SdpJob> queryReferenceJobs(SdpJarBO jarBO);

    List<SdpJob> getRunningStatusJob();

    List<SdpJob> selectUpdatedBy(@Param("projectId")String projectId);

    JobResp queryJob4Notify(SdpJob job);

    Integer countJob(@Param("datasourceId") Long datasourceId,@Param("jobName") String jobName);

    List<DatasourceJobInf> JobListByDataSource(SdpDataSourceBO datasourceBo);

    List<SdpJob> queryByPriority(SdpProject sdpProject);

    JobResp queryJobById(@Param("jobId")Long jobId);

    List<SdpJob> getJobByIds(List<Long> ids);
}