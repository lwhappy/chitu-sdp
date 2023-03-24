

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-11-8
  * </pre>
  */

package com.chitu.bigdata.sdp.service;

import com.chitu.bigdata.sdp.api.bo.SdpJobAlertRecordBO;
import com.chitu.bigdata.sdp.api.model.SdpJobAlertRecord;
import com.chitu.bigdata.sdp.api.vo.SdpJobAlertRecordVo;
import com.chitu.bigdata.sdp.mapper.SdpJobAlertRecordMapper;
import com.chitu.cloud.model.ResponseData;
import com.chitu.cloud.service.GenericService;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <pre>
 * job告警记录业务类
 * </pre>
 */
@Service
public class SdpJobAlertRecordService extends GenericService<SdpJobAlertRecord, Long> {
    public SdpJobAlertRecordService(@Autowired SdpJobAlertRecordMapper sdpJobAlertRecordMapper) {
        super(sdpJobAlertRecordMapper);
    }
    
    public SdpJobAlertRecordMapper getMapper() {
        return (SdpJobAlertRecordMapper) super.genericMapper;
    }

    public ResponseData queryList(SdpJobAlertRecordBO sdpJobAlertRecordBO) {
        PageHelper.startPage(sdpJobAlertRecordBO.getPage(), sdpJobAlertRecordBO.getPageSize());
        List<SdpJobAlertRecordVo> jobAlertRecordList = this.getMapper().queryList(sdpJobAlertRecordBO);
        PageInfo<SdpJobAlertRecordVo> pageInfo = new PageInfo<>(jobAlertRecordList);
        ResponseData responseData = new ResponseData<>();
        responseData.setData(pageInfo).ok();
        return responseData;
    }
}