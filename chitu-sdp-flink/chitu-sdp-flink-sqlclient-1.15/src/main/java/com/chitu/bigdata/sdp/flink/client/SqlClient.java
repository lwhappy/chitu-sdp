package com.chitu.bigdata.sdp.flink.client;


import com.alibaba.fastjson.JSON;
import com.chitu.bigdata.sdp.flink.client.util.DeflaterUtils;
import com.chitu.bigdata.sdp.flink.client.util.SqlType;
import com.chitu.bigdata.sdp.flink.client.util.SqlUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamStatementSet;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;


/**
 * @author sutao
 * @create 2022-09-14 09:30
 */
public class SqlClient {

    private static final Logger LOG = LoggerFactory.getLogger(SqlClient.class);

    public static void main(String[] args) {

        // 获取参数
        ParameterTool parameterTool = ParameterTool.fromArgs(args);
        parameterTool.toMap().forEach((k, v) -> {
            LOG.info("{}:{}", k, v);
        });

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        String yamlStr = parameterTool.getRequired("yaml.conf");
        Map<String, Object> yamlMap = JSON.parseObject(DeflaterUtils.unzipString(yamlStr), Map.class);
        yamlMap.forEach((k, v) -> {
            if (k.startsWith("table.")) {
                LOG.info("{}:{}", k, v);
                tableEnv.getConfig().getConfiguration().setString(k, v.toString());
            }
        });

        String appName = parameterTool.get("app.name");
        if (StringUtils.isNotEmpty(appName)) {
            tableEnv.getConfig().getConfiguration().setString(PipelineOptions.NAME, appName);
        }

        String sql = DeflaterUtils.unzipString(parameterTool.getRequired("sql")) + "\n";

        // 只做sql切分
        String[] statements = SqlUtil.getStatements(sql, ";\\s*\n");

        StreamStatementSet statementSet = tableEnv.createStatementSet();
        for (String statement : statements) {
            // 去除注释后再获取操作类型
            String removeNoteStatement = SqlUtil.removeNotes(statement);
            SqlType operationType = SqlUtil.getOperationType(removeNoteStatement);

            if (StringUtils.isEmpty(removeNoteStatement) || SqlType.UNKNOWN.equals(operationType)) {
                LOG.warn("跳过无效sql段: \n{}", statement);
                continue;
            }

            // 实际执行的sql保留注释
            if (operationType.equals(SqlType.INSERT)) {
                LOG.info("\n" + statement);
                statementSet.addInsertSql(statement);
            } else {
                tableEnv.executeSql(statement);
            }
        }

        statementSet.execute();


    }

}
