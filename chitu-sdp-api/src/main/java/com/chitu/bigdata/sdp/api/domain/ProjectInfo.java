

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.domain;

import com.chitu.bigdata.sdp.api.model.SdpProject;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * <pre>
 * 项目引用类
 * 数据库表名称：sdp_project
 * </pre>
 * @author 587694
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class ProjectInfo extends SdpProject implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 用于进项项目列表排序使用
     */
    private Long isLeader;

    /**
     * 在线作业数
     */
    private Integer onlineJobNum;

    private Integer userCount;

    private String ownerName;


    public ProjectInfo() {
    }	
}