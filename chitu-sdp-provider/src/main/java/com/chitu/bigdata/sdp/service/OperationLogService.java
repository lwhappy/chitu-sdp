

/**
  * <pre>
  * 作   者：CHENYUN
  * 创建日期：2021-11-8
  * </pre>
  */

package com.chitu.bigdata.sdp.service;

import com.chitu.bigdata.sdp.api.bo.SdpOperationLogBO;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.SdpOperationLog;
import com.chitu.bigdata.sdp.mapper.SdpOperationLogMapper;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.model.Pagination;
import com.chitu.cloud.service.GenericService;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <pre>
 * 操作日志业务类
 * </pre>
 */
@Service
public class OperationLogService extends GenericService<SdpOperationLog, Long> {
    public OperationLogService(@Autowired SdpOperationLogMapper sdpOperationLogMapper) {
        super(sdpOperationLogMapper);
    }
    
    public SdpOperationLogMapper getMapper() {
        return (SdpOperationLogMapper) super.genericMapper;
    }

    public Pagination<SdpOperationLog> searchByJobId(SdpOperationLogBO operationLogBO) {
        if (Objects.isNull(operationLogBO.getJobId())){
            throw new ApplicationException(ResponseCode.JOB_ID_NULL);
        }
        PageHelper.startPage(operationLogBO.getPage(),operationLogBO.getPageSize());
        Page<SdpOperationLog> sdpOperationLogs = (Page)this.getMapper().searchByJobId(operationLogBO);
        Pagination<SdpOperationLog> paginationData = Pagination.getInstance(operationLogBO.getPage(),operationLogBO.getPageSize());
        paginationData.setRows(sdpOperationLogs);
        paginationData.setRowTotal((int) sdpOperationLogs.getTotal());
        paginationData.setPageTotal(sdpOperationLogs.getPages());

        return paginationData;
    }

}