package com.chitu.bigdata.sdp.service.datasource.format;

import com.chitu.bigdata.sdp.api.domain.FlinkTableGenerate;

/**
 *
 * kafka数据格式（用于生成DDL）
 *
 * @author zouchangzhen
 * @date 2022/3/28
 */
public abstract class AbstractKafkaDataFormat {

    /**
     * 生成DDL(格式不一样，生成的模板可能不一样)
      * @return
     */
    public abstract String generateDdl(FlinkTableGenerate flinkTableGenerate);
}
