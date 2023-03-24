
package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.model.SdpVersion;
import com.chitu.cloud.model.GenericBO;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * <pre>
 * 作业版本实体类
 * </pre>
 * @author 587694
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpVersion4BO extends GenericBO<SdpVersion> {

    public SdpVersion4BO() {
        setVo(new SdpVersion());
    }
    public SdpVersion getSdpVersion(){
        return (SdpVersion) getVo();
    }
    public void setSdpVersion(SdpVersion vo){
        setVo(vo);
    }
}