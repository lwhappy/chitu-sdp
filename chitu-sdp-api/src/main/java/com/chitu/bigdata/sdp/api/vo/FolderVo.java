package com.chitu.bigdata.sdp.api.vo;

import com.chitu.cloud.model.GenericModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/22 15:48
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class FolderVo extends GenericModel<Long> {
    private String folderName;
    private Long parentId;
    private Long projectId;
    private Boolean isLeaf = true;
    private List<FolderVo> children;

    //文件和目录一起联合查询
    private String type;
    private String name;
    private Long folderId;
    private String fileType;
    private String isOnLine;
    private Integer priority;
}
