

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.bo.SdpFileBO;
import com.chitu.bigdata.sdp.api.model.SdpFile;
import com.chitu.bigdata.sdp.api.vo.SdpFileResp;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <pre>
 * 文件数据访问接口
 * </pre>
 */
@Mapper
public interface SdpFileMapper extends GenericMapper<SdpFile, Long> {

    List<SdpFile> selectAll(SdpFile data);

    SdpFile getFile(SdpFile file);


    SdpFileResp getDetailFile(SdpFileBO fileBO);

    SdpFile queryFileByJobId(@Param("jobId") Long jobId);

    List<SdpFile> selectByInstance(SdpFile sdpFile);

    List<SdpFile> getEngine4File(@Param("projectId") Long projectInfoId, @Param("engineId") Long id);

    List<SdpFile> checkState(List<Long> list);

    List<SdpFileResp> getFileHistory(@Param("updatedBy") String userId);

    List<SdpFileResp> getFileHistory4Common(@Param("updatedBy") String userId);


    SdpFile queryBaseInfo(SdpFileBO fileBO);

    void updateApproveFlag(SdpFile param);

    Integer queryCountByMinId(@Param("minId") Long minId);
    List<SdpFile> queryByMinId(@Param("minId") Long minId,@Param("pageSize") Integer pageSize);

    List<SdpFile> selectByFileNames(@Param("projectId") Long projectId,@Param("fileNames") List<String> fileNames);
}