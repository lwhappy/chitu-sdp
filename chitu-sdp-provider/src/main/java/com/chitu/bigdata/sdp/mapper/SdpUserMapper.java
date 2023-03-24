

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.domain.EngineUserInfo;
import com.chitu.bigdata.sdp.api.domain.ProjectUser;
import com.chitu.bigdata.sdp.api.model.SdpProject;
import com.chitu.bigdata.sdp.api.model.SdpUser;
import com.chitu.cloud.mapper.GenericMapper;
import com.chitu.cloud.model.Pagination;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <pre>
 * 用户数据访问接口
 * </pre>
 */
@Mapper
public interface SdpUserMapper extends GenericMapper<SdpUser, Long> {
    List<SdpUser> searchPage(Pagination<SdpUser> p);

    List<SdpUser> getCondition(@Param("nameOrNumber") String nameOrNumber);

    SdpUser selectIsExist(SdpUser sdpuser);

    List<ProjectUser> getProjectUser(@Param("projectId") Long projectId);


    SdpUser queryUser(@Param("employeeNumber") String employeeNumber);

    List<SdpUser> getEngineUser(EngineUserInfo engineUserInfo);


    int  upsertAdmin(SdpUser user);

    SdpUser selectExist(SdpUser sdpuser);

    List<ProjectUser> getMangerUser4Project(@Param("userId") Long permissionsId,@Param("projectId")Long id);

    ProjectUser getProjectHistory(@Param("userId")Long userId);

    ProjectUser getUser4Project(@Param("userId")Long valueOf, @Param("projectId")Long projectId);

    List<ProjectUser> getUserInfos(SdpProject sdpProject);

    List<SdpUser> queryListByNumbers(@Param("list")List<String> list);

    List<ProjectUser> getProUserAndAdmin(SdpProject sdpProject);

    List<SdpUser> getProjectLeaders(@Param("projectId") Long projectInfoId);

    List<SdpUser> queryUser4Phone();

    void updateByEmployeeNumber(SdpUser user);

    List<SdpUser> queryCommonUserByProject(@Param("projectId")Long projectId);

    ProjectUser queryProjects4User(@Param("userId")Long userId);
}