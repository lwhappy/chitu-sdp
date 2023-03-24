

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.model.SdpProject;
import com.chitu.cloud.model.GenericBO;
import lombok.Data;
import lombok.EqualsAndHashCode;


/**
 * <pre>
 * 项目业务实体类
 * </pre>
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SdpProjectBO extends GenericBO<SdpProject> {
    public SdpProjectBO() {
        setVo(new SdpProject());
    }

    public SdpProject getSdpProject() {
        return (SdpProject) getVo();
    }

    public void setSdpProject(SdpProject vo) {
        setVo(vo);
    }
    private Long userId;
}