

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-10-15
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.domain.SourceKafkaInfo;
import com.chitu.bigdata.sdp.api.model.SdpJob;
import com.chitu.cloud.model.GenericBO;
import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

/**
 * <pre>
 * 作业业务实体类
 * </pre>
 * @author chenyun
 */
@EqualsAndHashCode(callSuper = true)
@Data
@ApiModel(value = "作业入参实体")
public class SdpJobBO extends GenericBO<SdpJob> {
    public SdpJobBO() {
        setVo(new SdpJob());
    }

    public SdpJob getSdpJob() {
        return (SdpJob) getVo();
    }

    public void setSdpJob(SdpJob vo) {
        setVo(vo);
    }

    /**
     * 是否使用最新版本恢复
     */
    public Boolean useLatest;
    /**
     * 配置信息需求种类信息
     */
    public String confType;

    public String savepointName;

    /**
     * 传递保存点路径或者检查点路径
     */
    public String savepointPath;

    /**
     * kafka源表修改消费位置参数
     */
    List<SourceKafkaInfo> sourceKafkaList;

    /**
     * uat任务运行有效期
     */
    public Integer uatJobRunningValidDays;

    /**
     * 是否是自动拉起的作业
     */
    public boolean isAutoPull = false;


}