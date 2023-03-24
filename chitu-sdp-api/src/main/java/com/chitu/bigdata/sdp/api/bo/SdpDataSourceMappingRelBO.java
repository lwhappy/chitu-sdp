

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-10-29
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.model.SdpDataSourceMappingRel;
import com.chitu.cloud.model.GenericBO;

/**
 * <pre>
 * 转环境数据源映射关系表业务实体类
 * </pre>
 */
public class SdpDataSourceMappingRelBO extends GenericBO<SdpDataSourceMappingRel> {
    public SdpDataSourceMappingRelBO() {
        setVo(new SdpDataSourceMappingRel());
    }
    
    public SdpDataSourceMappingRel getSdpDataSourceMappingRel() {
        return (SdpDataSourceMappingRel) getVo();
    }
    
    public void setSdpDataSourceMappingRel(SdpDataSourceMappingRel vo) {
    	setVo(vo);
    }
}