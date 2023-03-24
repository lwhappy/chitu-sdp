

/**
  * <pre>
  * 作   者：WANGHAI
  * 创建日期：2022-6-7
  * </pre>
  */

package com.chitu.bigdata.sdp.api.bo;

import com.chitu.bigdata.sdp.api.model.SdpKafkaConsumerRecord;
import com.chitu.cloud.model.GenericBO;

/**
 * <pre>
 * kafka 消费位置记录表业务实体类
 * </pre>
 */
public class SdpKafkaConsumerRecordBO extends GenericBO<SdpKafkaConsumerRecord> {
    public SdpKafkaConsumerRecordBO() {
        setVo(new SdpKafkaConsumerRecord());
    }
    
    public SdpKafkaConsumerRecord getSdpKafkaConsumerRecord() {
        return (SdpKafkaConsumerRecord) getVo();
    }
    
    public void setSdpKafkaConsumerRecord(SdpKafkaConsumerRecord vo) {
    	setVo(vo);
    }
}