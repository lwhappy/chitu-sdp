

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-5-9
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.model.SdpDwsRunningResult;
import com.chitu.cloud.model.GenericBO;

/**
 * <pre>
 * 运行结果统计表业务实体类
 * </pre>
 */
public class SdpDwsRunningResultBO extends GenericBO<SdpDwsRunningResult> {
    public SdpDwsRunningResultBO() {
        setVo(new SdpDwsRunningResult());
    }
    
    public SdpDwsRunningResult getSdpDwsRunningResult() {
        return (SdpDwsRunningResult) getVo();
    }
    
    public void setSdpDwsRunningResult(SdpDwsRunningResult vo) {
    	setVo(vo);
    }
}