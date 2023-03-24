package com.chitu.bigdata.sdp.flink.client;

import com.chitu.bigdata.sdp.flink.client.util.SqlType;
import com.chitu.bigdata.sdp.flink.client.util.SqlUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.configuration.PipelineOptions;
import org.apache.flink.contrib.streaming.state.EmbeddedRocksDBStateBackend;
import org.apache.flink.runtime.state.storage.FileSystemCheckpointStorage;
import org.apache.flink.streaming.api.CheckpointingMode;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.table.api.bridge.java.StreamStatementSet;
import org.apache.flink.table.api.bridge.java.StreamTableEnvironment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * 启动类配置  Program 参数添加上，绝对路径改成自己的
 * --sql D:\idea_work\bigdata-sdp\bigdata-sdp-flink\bigdata-sdp-flink-sqlclient-1.14\sql\xxx.sql
 */
public class LocalSqlClient {

    private static final Logger LOG = LoggerFactory.getLogger(LocalSqlClient.class);


    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        StreamTableEnvironment tableEnv = StreamTableEnvironment.create(env);

        initEnvParam(env, tableEnv);

        String sql = String.join("\n", Files.readAllLines(Paths.get(ParameterTool.fromArgs(args).getRequired("sql")))) + "\n";

        // 只做sql切分
        String[] statements = SqlUtil.getStatements(sql, ";\\s*\n");

        StreamStatementSet statementSet = tableEnv.createStatementSet();
        for (String statement : statements) {
            LOG.info("\n" + statement);
            // 去除注释后再获取操作类型
            String removeNoteStatement = SqlUtil.removeNotes(statement);
            SqlType operationType = SqlUtil.getOperationType(removeNoteStatement);

            if (StringUtils.isEmpty(removeNoteStatement) || SqlType.UNKNOWN.equals(operationType)) {
                LOG.warn("跳过无效sql段: \n{}", statement);
                continue;
            }

            // 实际执行的sql保留注释
            if (operationType.equals(SqlType.INSERT)) {
                statementSet.addInsertSql(statement);
            } else {
                tableEnv.executeSql(statement);
            }
        }

        statementSet.execute();


    }

    /**
     * 初始化环境参数
     *
     * @param env
     * @param tableEnv
     */
    private static void initEnvParam(StreamExecutionEnvironment env, StreamTableEnvironment tableEnv) {

        // 操作hdfs需要设置
        System.setProperty("HADOOP_USER_NAME", "admin");

        // TODO 当写入hive表是非当前环境变量对应的hadoop时，则需要设置，本地需要有对应的配置目录
        System.setProperty("hive.catalog.hadoopconf", "/opt/apache/hadoop-one-ticket/etc/hadoop");

        // 全局算子并行度
        env.setParallelism(1);

        // 禁用chain
        env.disableOperatorChaining();

        // table相关参数
        Configuration config = tableEnv.getConfig().getConfiguration();

        // 设置sql任务名
        config.setString(PipelineOptions.NAME, LocalSqlClient.class.getSimpleName());
        config.setString("table.exec.mini-batch.size", "1000");
        config.setString("table.exec.mini-batch.allow-latency", "10s");
        config.setString("table.exec.mini-batch.enabled", "true");
        config.setString("table.optimizer.distinct-agg.split.enabled", "true");
        config.setString("table.exec.source.idle-timeout", "60000");

        // hive相关参数
        config.setString("table.exec.hive.fallback-mapred-writer", "false");
        config.setString("table.exec.hive.infer-source-parallelism.max", "1");

        // 重启策略
        // 固定间隔重启，重启3次，每次间隔10s，达到重启次数后还出现异常则任务会停止
        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, Time.of(10, TimeUnit.SECONDS)));

        // 检查点相关参数
        env.enableCheckpointing(30 * 1000);
        env.getCheckpointConfig().setCheckpointingMode(CheckpointingMode.EXACTLY_ONCE);
        env.setStateBackend(new EmbeddedRocksDBStateBackend());
        env.getCheckpointConfig().setCheckpointStorage(new FileSystemCheckpointStorage("hdfs://bigbigworld:8020/sdp/tmp"));
        env.getCheckpointConfig().setMaxConcurrentCheckpoints(1);
        env.getCheckpointConfig().setCheckpointTimeout(10 * 60 * 1000);
        env.getCheckpointConfig().setExternalizedCheckpointCleanup(CheckpointConfig.ExternalizedCheckpointCleanup.DELETE_ON_CANCELLATION);

    }


}
