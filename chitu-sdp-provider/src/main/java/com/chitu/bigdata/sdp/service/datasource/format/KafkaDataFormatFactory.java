package com.chitu.bigdata.sdp.service.datasource.format;


import com.chitu.bigdata.sdp.api.enums.FormatType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author zouchangzhen
 * @date 2022/3/28
 */
@Component
public class KafkaDataFormatFactory {

    /**
     * 数据格式的beanName
     * 例如 json格式的对应的是 {@link JsonKafkaDataFormat}
     */
    public final static String BEAN_NAME = "%sKafkaDataFormat";

    @Autowired
    Map<String, AbstractKafkaDataFormat> kafkaDataFormatMap = new ConcurrentHashMap<>(10);

    public AbstractKafkaDataFormat getKafkaDataFormat(FormatType formatType) {
        AbstractKafkaDataFormat kafkaDataFormat = kafkaDataFormatMap.get(String.format(BEAN_NAME,formatType.getType()));
        if (kafkaDataFormat == null) {
            throw new RuntimeException("数据类型不存在!");
        }
        return kafkaDataFormat;
    }

}
