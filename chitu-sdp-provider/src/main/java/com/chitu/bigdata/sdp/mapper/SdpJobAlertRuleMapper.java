

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-11-8
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.bo.SdpJobAlertRuleBO;
import com.chitu.bigdata.sdp.api.model.SdpJobAlertRule;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <pre>
 * job告警规则数据访问接口
 * </pre>
 */
@Mapper
public interface SdpJobAlertRuleMapper extends GenericMapper<SdpJobAlertRule, Long> {
    List<SdpJobAlertRule> queryList(SdpJobAlertRuleBO sdpJobAlertRuleBO);

    List<SdpJobAlertRule> getRuleByIndexName(@Param("indexName") String indexName);

    void updateByIndexName(List<SdpJobAlertRule> rules);
}