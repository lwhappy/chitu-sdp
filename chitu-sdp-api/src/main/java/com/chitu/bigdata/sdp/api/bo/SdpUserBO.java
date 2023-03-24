

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.model.SdpUser;
import com.chitu.cloud.model.GenericBO;

/**
 * <pre>
 * 用户业务实体类
 * </pre>
 * @author 587694
 */

public class SdpUserBO extends GenericBO<SdpUser> {

    public SdpUserBO() {
        setVo(new SdpUser());
    }

    public SdpUser getSdpUser() {
        return (SdpUser) getVo();
    }

    public void setSdpUser(SdpUser vo) {
        setVo(vo);
    }
}