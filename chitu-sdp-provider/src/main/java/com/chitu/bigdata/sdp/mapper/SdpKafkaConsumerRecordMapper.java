

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-6-7
  * </pre>
  */

package com.chitu.bigdata.sdp.mapper;

import com.chitu.bigdata.sdp.api.model.SdpKafkaConsumerRecord;
import com.chitu.cloud.mapper.GenericMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <pre>
 * kafka 消费位置记录表数据访问接口
 * </pre>
 */
@Mapper
public interface SdpKafkaConsumerRecordMapper extends GenericMapper<SdpKafkaConsumerRecord, Long> {
     int upsert(@Param("infos")List<SdpKafkaConsumerRecord> infos);
}