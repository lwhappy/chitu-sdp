

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-4-18
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.model.SdpSavepoint;
import com.chitu.cloud.model.GenericBO;

/**
 * <pre>
 * 保存点记录信息业务实体类
 * </pre>
 */
public class SdpSavepointBO extends GenericBO<SdpSavepoint> {
    public SdpSavepointBO() {
        setVo(new SdpSavepoint());
    }
    
    public SdpSavepoint getSdpSavepoint() {
        return (SdpSavepoint) getVo();
    }
    
    public void setSdpSavepoint(SdpSavepoint vo) {
    	setVo(vo);
    }
}