

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-25
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.model.SdpRawStatusHistory;
import com.chitu.cloud.model.GenericBO;

/**
 * <pre>
 * 作业状态历史业务实体类
 * </pre>
 * @author chenyun
 */
public class SdpRawStatusHistoryBO extends GenericBO<SdpRawStatusHistory> {
    public SdpRawStatusHistoryBO() {
        setVo(new SdpRawStatusHistory());
    }
    
    public SdpRawStatusHistory getSdpRawStatusHistory() {
        return (SdpRawStatusHistory) getVo();
    }
    
    public void setSdpRawStatusHistory(SdpRawStatusHistory vo) {
    	setVo(vo);
    }
}