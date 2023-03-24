

/**
  * <pre>
  * 作   者：chenyun
  * 创建日期：2022-8-17
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.model.SdpProJobTableDetail;
import com.chitu.cloud.model.GenericBO;
import lombok.Data;

/**
 * <pre>
 * 项目作业表明细业务实体类
 * </pre>
 */
@Data
public class SdpProJobTableDetailBO extends GenericBO<SdpProJobTableDetail> {
    public SdpProJobTableDetailBO() {
        setVo(new SdpProJobTableDetail());
    }
    
    public SdpProJobTableDetail getSdpProJobTableDetail() {
        return (SdpProJobTableDetail) getVo();
    }
    
    public void setSdpProJobTableDetail(SdpProJobTableDetail vo) {
    	setVo(vo);
    }

}