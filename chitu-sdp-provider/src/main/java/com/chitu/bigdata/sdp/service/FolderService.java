

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.service;

import com.chitu.bigdata.sdp.api.bo.SdpFolderBO;
import com.chitu.bigdata.sdp.api.enums.EnableType;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.SdpFile;
import com.chitu.bigdata.sdp.api.model.SdpFolder;
import com.chitu.bigdata.sdp.api.vo.FolderVo;
import com.chitu.bigdata.sdp.constant.CommonConstant;
import com.chitu.bigdata.sdp.mapper.SdpFileMapper;
import com.chitu.bigdata.sdp.mapper.SdpFolderMapper;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.service.GenericService;
import com.google.common.base.Preconditions;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.Objects;
import java.util.Set;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/15 14:06
 */
@Service
public class FolderService extends GenericService<SdpFolder, Long> {
    public FolderService(@Autowired SdpFolderMapper sdpFolderMapper) {
        super(sdpFolderMapper);
    }
    
    public SdpFolderMapper getMapper() {
        return (SdpFolderMapper) super.genericMapper;
    }

    @Autowired
    private SdpFileMapper fileMapper;
    @Autowired
    FileService fileService;

    public SdpFolder addFolder(SdpFolderBO folderBO) {
        SdpFolder folder = new SdpFolder();
        BeanUtils.copyProperties(folderBO,folder);
        SdpFolder folder1 = this.getMapper().getFolder(folder);
        if(null != folder1){
            throw new ApplicationException(ResponseCode.FOLDER_IS_EXISTS);
        }
        insert(folder);
        return folder;
    }

    public List<FolderVo> queryFolder(FolderVo folderBO) {
        List<FolderVo> parent = this.getMapper().queryFolder(folderBO);
        if(!CollectionUtils.isEmpty(parent)){
            for(FolderVo folder : parent){
                FolderVo folder1 = new FolderVo();
                folder1.setProjectId(folder.getProjectId());
                folder1.setParentId(folder.getId());
                List<FolderVo> child = queryFolder(folder1);
                if(!CollectionUtils.isEmpty(child)){
                    folder.setIsLeaf(false);
                    folder.setChildren(child);
                }
            }
        }
        return parent;
    }

    public Integer updateFolder(SdpFolderBO folderBO) {
        SdpFolder folder = new SdpFolder();
        BeanUtils.copyProperties(folderBO,folder);
        folder.setBusinessFlag(null);
        return updateSelective(folder);
    }

    public Integer deleteFolder(SdpFolderBO folderBO) {
        SdpFile file = new SdpFile();
        file.setFolderId(folderBO.getId());
        List<SdpFile> files = fileService.selectAll(file);
        if(!CollectionUtils.isEmpty(files)){
            throw new ApplicationException(ResponseCode.FOLDER_NOT_NULL);
        }
        return disable(SdpFolder.class,folderBO.getId());
    }

    public List<FolderVo> searchFolder(SdpFolderBO folderBO) {
        folderBO.setFolderName(StrUtils.changeWildcard(folderBO.getFolderName()));
        return this.getMapper().searchFolderAndFile(folderBO);
    }

    public List<FolderVo> searchFolders(SdpFolderBO folderBO) {
        folderBO.setFolderName(StrUtils.changeWildcard(folderBO.getFolderName()));
        return this.getMapper().searchFolder(folderBO);
    }

    public void queryFolderIdAndChildFolderId(Long folderId, Set<Long> folderIds) {
        Preconditions.checkNotNull(folderIds,"文件夹集合对象不可以为空");
        if(Objects.isNull(folderId) || CommonConstant.ZERO_FOR_LONG.equals(folderId)){
            return;
        }
        folderIds.add(folderId);

        SdpFolder param = new SdpFolder();
        param.setParentId(folderId);
        param.setEnabledFlag(EnableType.ENABLE.getCode());
        List<SdpFolder> sdpFolders = selectAll(param);

        if (!CollectionUtils.isEmpty(sdpFolders)) {
            for (SdpFolder folder : sdpFolders) {
                if (Objects.isNull(folder)) {
                    continue;
                }
                queryFolderIdAndChildFolderId(folder.getId(), folderIds);
            }
        }
    }

}