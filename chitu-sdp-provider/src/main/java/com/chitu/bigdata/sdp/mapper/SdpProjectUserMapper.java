

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.model.SdpProjectUser;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <pre>
 * 项目用户关系表数据访问接口
 * </pre>
 */
@Mapper
public interface SdpProjectUserMapper extends GenericMapper<SdpProjectUser, Long> {


    List<SdpProjectUser> getProjectLeaders(@Param("projectId") Long projectInfoId);

    List<SdpProjectUser> getProjectUsers(@Param("projectId") Long projectInfoId);

    List<SdpProjectUser> queryProject4User(@Param("userId") String userId);

    Integer queryProjectUserIsLeader(@Param("userId") String userId);

    List<SdpProjectUser> selectIsExist(SdpProjectUser sdpProjectUser);

    List<SdpProjectUser> getProjectResponsibles(@Param("projectId")Long projectInfoId);

    List<SdpProjectUser> getByInstance(SdpProjectUser sdpProjectUser);

    List<SdpProjectUser> getUserBypid(@Param("projectId")Long projectInfoId);


    List<SdpProjectUser> selectProjectManger(@Param("userId") Long userId,@Param("projectId") Long projectId);


    int disableByInstance(SdpProjectUser sdpProjectUser);
}