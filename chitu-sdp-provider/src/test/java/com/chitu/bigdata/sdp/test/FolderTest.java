package com.chitu.bigdata.sdp.test;

import com.alibaba.fastjson.JSON;
import com.chitu.bigdata.sdp.api.bo.SdpFolderBO;
import com.chitu.bigdata.sdp.api.model.SdpFolder;
import com.chitu.bigdata.sdp.api.vo.FolderVo;
import com.chitu.bigdata.sdp.service.FolderService;
import com.chitu.cloud.web.test.BaseTest;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/19 10:04
 */
public class FolderTest extends BaseTest {
    @Autowired
    private FolderService folderService;

    @Test
    public void testSearchFolder(){
        SdpFolderBO folderBO = new SdpFolderBO();
        folderBO.setFolderName("销售目标");
        List<FolderVo> result = folderService.searchFolder(folderBO);
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void testQueryFolder(){
        FolderVo folderBO = new FolderVo();
        folderBO.setProjectId(60L);
        List<FolderVo> result = folderService.queryFolder(folderBO);
        System.out.println(JSON.toJSONString(result));
    }

    @Test
    public void testAddFolder(){
        SdpFolderBO folderBO = new SdpFolderBO();
        folderBO.setFolderName("测试");
        folderBO.setParentId(2L);
        folderBO.setProjectId(1L);
        SdpFolder result = folderService.addFolder(folderBO);
        System.out.println(JSON.toJSONString(result));
    }

}
