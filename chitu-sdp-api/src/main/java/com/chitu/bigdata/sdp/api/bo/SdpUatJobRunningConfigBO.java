

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-5-16
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.model.SdpUatJobRunningConfig;
import com.chitu.cloud.model.GenericBO;

/**
 * <pre>
 * UAT环境作业运行配置业务实体类
 * </pre>
 */
public class SdpUatJobRunningConfigBO extends GenericBO<SdpUatJobRunningConfig> {
    public SdpUatJobRunningConfigBO() {
        setVo(new SdpUatJobRunningConfig());
    }
    
    public SdpUatJobRunningConfig getSdpUatJobRunningConfig() {
        return (SdpUatJobRunningConfig) getVo();
    }
    
    public void setSdpUatJobRunningConfig(SdpUatJobRunningConfig vo) {
    	setVo(vo);
    }
}