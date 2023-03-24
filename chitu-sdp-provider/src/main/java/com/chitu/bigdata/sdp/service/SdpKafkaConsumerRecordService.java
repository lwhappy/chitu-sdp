

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-6-7
  * </pre>
  */

package com.chitu.bigdata.sdp.service;


import com.chitu.bigdata.sdp.api.model.SdpKafkaConsumerRecord;
import com.chitu.bigdata.sdp.mapper.SdpKafkaConsumerRecordMapper;
import com.chitu.cloud.service.GenericService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <pre>
 * kafka 消费位置记录表业务类
 * </pre>
 */
@Service("sdpKafkaConsumerRecordService")
public class SdpKafkaConsumerRecordService extends GenericService<SdpKafkaConsumerRecord, Long> {
    public SdpKafkaConsumerRecordService(@Autowired SdpKafkaConsumerRecordMapper sdpKafkaConsumerRecordMapper) {
        super(sdpKafkaConsumerRecordMapper);
    }
    
    public SdpKafkaConsumerRecordMapper getMapper() {
        return (SdpKafkaConsumerRecordMapper) super.genericMapper;
    }
}