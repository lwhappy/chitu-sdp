package com.chitu.bigdata.sdp.service.datasource.format;

import cn.hutool.crypto.SecureUtil;
import com.chitu.bigdata.sdp.api.domain.FlinkTableGenerate;
import com.chitu.bigdata.sdp.api.enums.CertifyType;
import com.chitu.bigdata.sdp.api.enums.MetaTableType;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.xiaoleilu.hutool.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author zouchangzhen
 * @date 2022/3/28
 */
@Component
@Slf4j
public class JsonKafkaDataFormat extends AbstractKafkaDataFormat{

    @Override
    public String generateDdl(FlinkTableGenerate flinkTableGenerate) {
        StringBuilder sb = new StringBuilder();
        flinkTableGenerate.getMetadataTableColumnList().forEach(item -> {
            sb.append("  ").append(StrUtils.strWithBackticks(item.getColumnName())).append(" ").append(item.getColumnType()).append(",").append("\n");
        });
        String cloumnStr = StrUtil.removeSuffix(sb.toString(), ",\n");
        String tableDdl = null;
        String saslParam = "";
        if (CertifyType.SASL.getType().equals(flinkTableGenerate.getCertifyType())) {
            saslParam = String.format(
                    "  'properties.security.protocol' = 'SASL_PLAINTEXT',\n" +
                            "  'properties.sasl.mechanism' = 'SCRAM-SHA-256',\n" +
                            "  'properties.sasl.jaas.config' = 'org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\"',\n",
                    flinkTableGenerate.getUserName(),
                    SecureUtil.md5(flinkTableGenerate.getPwd())
            );
        }
        if (MetaTableType.DIM.getType().equals(flinkTableGenerate.getMetaTableType()) ||
                MetaTableType.SOURCE.getType().equals(flinkTableGenerate.getMetaTableType())) {
            //sdp-{projectName-jobName-随机4位数字}
            //String groupId = String.format("sdp-%s-%s-%d", flinkTableGenerate.getProjectCode(), flinkTableGenerate.getJobName(), (int) (Math.random() * 9000 + 1000));
            String groupId = String.format("sdp-%s-%s", flinkTableGenerate.getProjectCode(), flinkTableGenerate.getJobName());
            tableDdl = String.format(
                    "CREATE TABLE %s (\n" +
                            "%s\n" +
                            ") WITH (\n" +
                            "  'connector' = 'kafka',\n" +
                            "  'properties.bootstrap.servers' = '%s',\n" +
                            "%s" +
                            "  'topic' = '%s',\n" +
                            "  'properties.group.id' = '%s',\n" +
                            "  'scan.startup.mode' = 'latest-offset',\n" +
                            "  'scan.topic-partition-discovery.interval' = '10000',\n" +
                            "  'format' = 'json',\n" +
                            "  'json.fail-on-missing-field' = 'false',\n" +
                            "  'json.ignore-parse-errors' = 'false' -- 不可更改，false表示平台开启解析失败监控\n" +
                            ");",
                    StrUtils.strWithBackticks(flinkTableGenerate.getFlinkTableName()),
                    cloumnStr,
                    SecureUtil.md5(flinkTableGenerate.getAddress()),
                    saslParam,
                    flinkTableGenerate.getSourceTableName(),
                    groupId
            );
        } else {
            tableDdl = String.format(
                    "CREATE TABLE %s (\n" +
                            "%s\n" +
                            ") WITH (\n" +
                            "  'connector' = 'kafka',\n" +
                            "  'properties.bootstrap.servers' = '%s',\n" +
                            "%s" +
                            "  'topic' = '%s',\n" +
                            "  'sink.partitioner' = 'round-robin',\n" +
                            "  'format' = 'json',\n" +
                            "  'properties.acks' = 'all',\n" +
                            "  'properties.batch.size' = '16384',\n" +
                            "  'properties.linger.ms' = '50',\n" +
                            "  'properties.buffer.memory' = '33554432'\n" +
                            ");",
                    StrUtils.strWithBackticks(flinkTableGenerate.getFlinkTableName()),
                    cloumnStr,
                    SecureUtil.md5(flinkTableGenerate.getAddress()),
                    saslParam,
                    flinkTableGenerate.getSourceTableName()
            );
        }
        return tableDdl;
    }
}
