

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.bo.SdpFolderBO;
import com.chitu.bigdata.sdp.api.model.SdpFolder;
import com.chitu.bigdata.sdp.api.vo.FolderVo;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <pre>
 * 目录数据访问接口
 * </pre>
 */
@Mapper
public interface SdpFolderMapper extends GenericMapper<SdpFolder, Long> {
    List<FolderVo> queryFolder(FolderVo folderBO);

    SdpFolder getFolder(SdpFolder folder);

    List<FolderVo> searchFolderAndFile(SdpFolderBO folderBO);

    /**
     * 模糊查询目录
     * @param folderBO
     * @return
     */
    List<FolderVo> searchFolder(SdpFolderBO folderBO);

    /**
     * 查询项目相应的文件夹是否存在
     * @param projectId
     * @param folderName
     * @param parentId
     * @return
     */
    public List<SdpFolder>  getFolderByName(@Param("projectId") Long projectId,@Param("folderName") String folderName,@Param("parentId") Long parentId);


}