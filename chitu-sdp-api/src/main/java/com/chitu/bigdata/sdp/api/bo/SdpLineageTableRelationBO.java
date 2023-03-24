

/**
  * <pre>
  * 作   者：chenyun
  * 创建日期：2022-8-17
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.model.SdpLineageTableRelation;
import com.chitu.cloud.model.GenericBO;

/**
 * <pre>
 * 表关系业务实体类
 * </pre>
 */
public class SdpLineageTableRelationBO extends GenericBO<SdpLineageTableRelation> {
    public SdpLineageTableRelationBO() {
        setVo(new SdpLineageTableRelation());
    }
    
    public SdpLineageTableRelation getSdpLineageTableRelation() {
        return (SdpLineageTableRelation) getVo();
    }
    
    public void setSdpLineageTableRelation(SdpLineageTableRelation vo) {
    	setVo(vo);
    }
}