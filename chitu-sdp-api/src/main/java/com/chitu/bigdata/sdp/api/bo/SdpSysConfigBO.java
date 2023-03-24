

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-3-29
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.model.SdpSysConfig;
import com.chitu.cloud.model.GenericBO;

/**
 * <pre>
 * 系统配置表业务实体类
 * </pre>
 * @author 587694
 */
public class SdpSysConfigBO extends GenericBO<SdpSysConfig> {
    public SdpSysConfigBO() {
        setVo(new SdpSysConfig());
    }
    
    public SdpSysConfig getSdpSysConfig() {
        return (SdpSysConfig) getVo();
    }
    
    public void setSdpSysConfig(SdpSysConfig vo) {
    	setVo(vo);
    }
}