

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.bo.SdpProjectBO;
import com.chitu.bigdata.sdp.api.bo.SdpProjectBTO;
import com.chitu.bigdata.sdp.api.domain.ProjectCountInfo;
import com.chitu.bigdata.sdp.api.model.SdpProject;
import com.chitu.bigdata.sdp.api.vo.SdpProjectResp;
import com.chitu.cloud.mapper.GenericMapper;
import com.chitu.cloud.model.Pagination;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Set;

/**
 * <pre>
 * 项目数据访问接口
 * </pre>
 */
@Mapper
public interface SdpProjectMapper extends GenericMapper<SdpProject, Long> {

    List<SdpProject> projectList(SdpProjectBTO pagination);

    List<SdpProject> searchByName(@Param("projectName") String projectName);

    List<SdpProject> getByName(@Param("projectName") String projectName);

    List<SdpProject> getProjects(@Param("userId") Long userId,@Param("projectName") String projectName);

    long projectsCount(SdpProjectBO sdpProjectBO);

    long filesCount(SdpProjectBO sdpProjectBO);

    long usersCount(SdpProjectBO sdpProjectBO);

    List<SdpProjectResp> getProject4Engine(@Param("engineId") Long id);

    SdpProject getProjectByCode(@Param("projectCode")String projectCode);

    List<SdpProjectResp> getJobOnlineNumByProjectid(@Param("projectIds") List<Long> projectIds);

    ProjectCountInfo allCount(SdpProjectBTO sdpProjectBTO);

    List<SdpProject> getProjectInfo(Pagination<SdpProject> p);

    List<SdpProject> getProjectCodeByIds(@Param("ids") Set<Long> ids);
}