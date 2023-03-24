

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.model.SdpEngine;
import com.chitu.cloud.model.GenericBO;

/**
 * <pre>
 * 引擎业务实体类
 * </pre>
 */

public class SdpEngineBO extends GenericBO<SdpEngine> {

    public SdpEngineBO() {
        setVo(new SdpEngine());
    }

    public SdpEngine getSdpEngine() {
        return (SdpEngine) getVo();
    }

    public void setSdpEngine(SdpEngine vo) {
        setVo(vo);
    }
}