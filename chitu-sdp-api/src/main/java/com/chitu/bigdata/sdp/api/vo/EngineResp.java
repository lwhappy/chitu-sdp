

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.vo;

import com.chitu.bigdata.sdp.api.domain.ProjectUser;
import com.chitu.bigdata.sdp.api.model.SdpEngine;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.util.List;

/**
 * <pre>
 * 引擎实体类
 * 数据库表名称：sdp_engine
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class EngineResp extends SdpEngine implements Serializable {
    private static final long serialVersionUID = 1L;


    /**
     * 字段名称：引擎当前用户角色
     *
     * 数据库字段信息:engine_url VARCHAR(255)
     */
     private List<ProjectUser> projectUserRole;


    public EngineResp() {
    }	
}