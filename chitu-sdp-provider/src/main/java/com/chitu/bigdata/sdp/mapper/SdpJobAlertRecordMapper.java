

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-11-8
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.bo.SdpJobAlertRecordBO;
import com.chitu.bigdata.sdp.api.model.SdpJobAlertRecord;
import com.chitu.bigdata.sdp.api.vo.SdpJobAlertRecordVo;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * <pre>
 * job告警记录数据访问接口
 * </pre>
 */
@Mapper
public interface SdpJobAlertRecordMapper extends GenericMapper<SdpJobAlertRecord, Long> {
    List<SdpJobAlertRecordVo> queryList(SdpJobAlertRecordBO sdpJobAlertRecordBO);
}