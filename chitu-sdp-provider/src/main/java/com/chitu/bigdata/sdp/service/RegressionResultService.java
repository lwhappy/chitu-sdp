

/**
  * <pre>
  * 作   者：LIZHENYONG
  * 创建日期：2022-4-24
  * </pre>
  */

package com.chitu.bigdata.sdp.service;

import com.chitu.bigdata.sdp.api.model.RegressionResult;
import com.chitu.bigdata.sdp.mapper.RegressionResultMapper;
import com.chitu.cloud.service.GenericService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * <pre>
 * connector数据集成测试结果业务类
 * </pre>
 * @author 587694
 */
@Slf4j
@Service("regressionResultService")
public class RegressionResultService extends GenericService<RegressionResult, Long> {
    public RegressionResultService(@Autowired RegressionResultMapper regressionResultMapper) {
        super(regressionResultMapper);
    }
    
    public RegressionResultMapper getMapper() {
        return (RegressionResultMapper) super.genericMapper;
    }

    public void resultRecord(String table, String data, String sourceType, String jobType, String regressionType, String result) {
        try {
            RegressionResult regressionResult = new RegressionResult();
            regressionResult.setDataSourceType(sourceType);
            regressionResult.setJobType(jobType);
            regressionResult.setRegressionType(regressionType);
            regressionResult.setResult(result);
            regressionResult.setTableName(table);
            if(Objects.nonNull(data)) {
                regressionResult.setFailDetail(data);
            }
            this.insertSelective(regressionResult);
        } catch (Exception e) {
            log.error("插入结果数据出错",e.getMessage());
        }
    }
}