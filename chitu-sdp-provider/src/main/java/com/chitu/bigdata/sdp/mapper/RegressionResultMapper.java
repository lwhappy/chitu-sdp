

/**
  * <pre>
  * 作   者：LIZHENYONG
  * 创建日期：2022-4-24
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.model.RegressionResult;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * <pre>
 * connector数据集成测试结果数据访问接口
 * </pre>
 */
@Mapper
public interface RegressionResultMapper extends GenericMapper<RegressionResult, Long> {
}