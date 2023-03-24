package com.chitu.bigdata.sdp.test;

import com.alibaba.fastjson.JSON;
import com.chitu.bigdata.sdp.api.bo.SdpFileBO;
import com.chitu.bigdata.sdp.api.domain.DataStreamConfig;
import com.chitu.bigdata.sdp.api.model.SdpDataSource;
import com.chitu.bigdata.sdp.service.FileService;
import com.chitu.bigdata.sdp.service.datasource.DataSourceFactory;
import com.chitu.bigdata.sdp.service.validate.constant.FlinkSQLConstant;
import com.chitu.bigdata.sdp.service.validate.custom.CustomTableEnvironmentImpl;
import com.chitu.bigdata.sdp.service.validate.domain.JobConfigs;
import com.chitu.bigdata.sdp.service.validate.job.JobManager;
import com.chitu.bigdata.sdp.service.validate.util.SqlUtil;
import com.chitu.cloud.web.test.BaseTest;
import org.apache.commons.lang3.StringUtils;
import org.apache.flink.table.operations.Operation;
import org.apache.flink.table.operations.ddl.CreateTableOperation;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.*;
import java.time.temporal.ChronoField;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.time.temporal.ChronoField.*;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/10/18 14:53
 */
public class FileTest extends BaseTest {
    @Autowired
    private FileService fileService;
    @Autowired
    private DataSourceFactory dataSourceFactory;

    @Test
    public void testDevGetDag(){
        SdpFileBO fileBO = new SdpFileBO();
        DataStreamConfig dataStreamConfig = new DataStreamConfig();
        fileBO.setId(1392L);
        dataStreamConfig.setJarId(400L);
        dataStreamConfig.setMainClass("com.chitu.bigdata.sdp.dstream.app.ChangeLogKafkaCDC");
        dataStreamConfig.setJarName("bigdata-sdp-dstream-1.0-SNAPSHOT.jar");
        dataStreamConfig.setJarVersion("v1");
        fileBO.setDataStreamConfig(dataStreamConfig);
        fileService.getJobDAG(fileBO);
    }

    @Test
    public void testUatGetDag(){
        SdpFileBO fileBO = new SdpFileBO();
        DataStreamConfig dataStreamConfig = new DataStreamConfig();
        fileBO.setId(2679L);
        dataStreamConfig.setJarId(540L);
        dataStreamConfig.setMainClass("com.chitu.bigdata.flink.app.IntegralSceneBusinessData");
        dataStreamConfig.setJarName("bigdata-flink-data-process-1.0-SNAPSHOT.jar");
        dataStreamConfig.setJarVersion("v72");
        fileBO.setDataStreamConfig(dataStreamConfig);
        fileService.getJobDAG(fileBO);
    }

    private static final DateTimeFormatter TIMESTAMP_FORMATTER =
            new DateTimeFormatterBuilder()
                    .appendValue(YEAR, 1, 10, SignStyle.NORMAL)
                    .appendLiteral('-')
                    .appendValue(MONTH_OF_YEAR, 1, 2, SignStyle.NORMAL)
                    .appendLiteral('-')
                    .appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NORMAL)
                    .optionalStart()
                    .appendLiteral(" ")
                    .appendValue(HOUR_OF_DAY, 1, 2, SignStyle.NORMAL)
                    .appendLiteral(':')
                    .appendValue(MINUTE_OF_HOUR, 1, 2, SignStyle.NORMAL)
                    .appendLiteral(':')
                    .appendValue(SECOND_OF_MINUTE, 1, 2, SignStyle.NORMAL)
                    .optionalStart()
                    .appendFraction(ChronoField.NANO_OF_SECOND, 1, 9, true)
                    .optionalEnd()
                    .optionalEnd()
                    .toFormatter()
                    .withResolverStyle(ResolverStyle.LENIENT);

    private static final DateTimeFormatter DATE_FORMATTER =
            new DateTimeFormatterBuilder()
                    .appendValue(YEAR, 1, 10, SignStyle.NORMAL)
                    .appendLiteral('-')
                    .appendValue(MONTH_OF_YEAR, 1, 2, SignStyle.NORMAL)
                    .appendLiteral('-')
                    .appendValue(DAY_OF_MONTH, 1, 2, SignStyle.NORMAL)
                    .toFormatter()
                    .withResolverStyle(ResolverStyle.LENIENT);

    @Test
    public void testA(){
        LocalDateTime result;
        String timestampString = "2022-02-12";
        try {
            result =  LocalDateTime.parse(timestampString, TIMESTAMP_FORMATTER);
        } catch (DateTimeParseException e) {
            result = LocalDateTime.of(
                    LocalDate.parse(timestampString, DATE_FORMATTER), LocalTime.MIDNIGHT);
        }
        System.out.println(result);
    }

    @Test
    public void testSplit(){
        String result = "----------------------- Execution Plan -----------------------\\n{\\n  \\\"nodes\\\" : [ {\\n    \\\"id\\\" : 1,\\n    \\\"type\\\" : \\\"Source: KafkaSource-default_catalog.default_database.order_info\\\",\\n    \\\"pact\\\" : \\\"Data Source\\\",\\n    \\\"contents\\\" : \\\"Source: KafkaSource-default_catalog.default_database.order_info\\\",\\n    \\\"parallelism\\\" : 1\\n  }, {\\n    \\\"id\\\" : 2,\\n    \\\"type\\\" : \\\"Calc(select=[DATE_FORMAT(create_time, _UTF-16LE'yyyy-MM-dd HH:00') AS dt, area_code, order_id])\\\",\\n    \\\"pact\\\" : \\\"Operator\\\",\\n    \\\"contents\\\" : \\\"Calc(select=[DATE_FORMAT(create_time, _UTF-16LE'yyyy-MM-dd HH:00') AS dt, area_code, order_id])\\\",\\n    \\\"parallelism\\\" : 1,\\n    \\\"predecessors\\\" : [ {\\n      \\\"id\\\" : 1,\\n      \\\"ship_strategy\\\" : \\\"FORWARD\\\",\\n      \\\"side\\\" : \\\"second\\\"\\n    } ]\\n  }, {\\n    \\\"id\\\" : 4,\\n    \\\"type\\\" : \\\"GroupAggregate(groupBy=[dt, area_code], select=[dt, area_code, COUNT(order_id) AS order_num])\\\",\\n    \\\"pact\\\" : \\\"Operator\\\",\\n    \\\"contents\\\" : \\\"GroupAggregate(groupBy=[dt, area_code], select=[dt, area_code, COUNT(order_id) AS order_num])\\\",\\n    \\\"parallelism\\\" : 1,\\n    \\\"predecessors\\\" : [ {\\n      \\\"id\\\" : 2,\\n      \\\"ship_strategy\\\" : \\\"HASH\\\",\\n      \\\"side\\\" : \\\"second\\\"\\n    } ]\\n  }, {\\n    \\\"id\\\" : 5,\\n    \\\"type\\\" : \\\"NotNullEnforcer(fields=[dt, area_code])\\\",\\n    \\\"pact\\\" : \\\"Operator\\\",\\n    \\\"contents\\\" : \\\"NotNullEnforcer(fields=[dt, area_code])\\\",\\n    \\\"parallelism\\\" : 1,\\n    \\\"predecessors\\\" : [ {\\n      \\\"id\\\" : 4,\\n      \\\"ship_strategy\\\" : \\\"FORWARD\\\",\\n      \\\"side\\\" : \\\"second\\\"\\n    } ]\\n  }, {\\n    \\\"id\\\" : 6,\\n    \\\"type\\\" : \\\"Sink: Sink(table=[default_catalog.default_database.order_statistic], fields=[dt, area_code, order_num])\\\",\\n    \\\"pact\\\" : \\\"Data Sink\\\",\\n    \\\"contents\\\" : \\\"Sink: Sink(table=[default_catalog.default_database.order_statistic], fields=[dt, area_code, order_num])\\\",\\n    \\\"parallelism\\\" : 1,\\n    \\\"predecessors\\\" : [ {\\n      \\\"id\\\" : 5,\\n      \\\"ship_strategy\\\" : \\\"FORWARD\\\",\\n      \\\"side\\\" : \\\"second\\\"\\n    } ]\\n  } ]\\n}\\n--------------------------------------------------------------\\n\\nNo description provided.\\n";
        String regex = "(---).{1,}(---)";
        String[] result1 = result.split(regex);
        String aa = result1[1];
        System.out.println(aa);
    }

    @Test
    public void testExplainSql(){
        SdpFileBO fileBO = new SdpFileBO();
        fileBO.setId(170L);
        List result = fileService.explainSql(fileBO);
        System.out.println("result===" + JSON.toJSONString(result));
    }
    @Test
    public void testChangeParams(){

        String a = "  CREATE TABLE map_sink (\n" +
                "  dt VARCHAR,\n" +
                "  code VARCHAR,\n" +
                "  order_num BIGINT,\n" +
                "  PRIMARY KEY (dt,code) NOT ENFORCED\n" +
                "  ) WITH (\n" +
                "  'connector.type' = 'jdbc', -- 使用 jdbc connector\n" +
                "  'url' = 'jdbc:mysql://******',\n" +
                "  'table' = 'map',\n" +
                "  'username' = '******',\n" +
                "  'password' = '******',\n" +
                "  'connector.write.flush.max-rows' = '1' -- 默认 5000 条，为了演示改为 1 条\n" +
                "  );";
//        String a = "CREATE TABLE order_log (\n" +
//                "  code VARCHAR,\n" +
//                "  order_num BIGINT,\n" +
//                "  ts TIMESTAMP(3)\n" +
//                "  ) WITH (\n" +
//                "  'connector.type' = 'kafka',\n" +
//                "  'connector.version' = 'universal',\n" +
//                "  'connector.topic' = 'order_log',\n" +
//                "  'connector.properties.bootstrap.servers' = 'szzb-bg-uat-etl-10:9092,szzb-bg-uat-etl-11:9092,szzb-bg-uat-etl-12:9092',\n" +
//                "  'connector.properties.group.id' = 'sink_mysql_group_testx2',\n" +
//                "  'connector.startup-mode' = 'earliest-offset',\n" +
//                "  'update-mode' = 'append',\n" +
//                "  'format.type' = 'json',\n" +
//                "  'format.derive-schema' = 'true'\n" +
//                "  );\n" +
//                "\n" +
//                "  CREATE TABLE map_sink (\n" +
//                "  dt VARCHAR,\n" +
//                "  code VARCHAR,\n" +
//                "  order_num BIGINT,\n" +
//                "  PRIMARY KEY (dt,code) NOT ENFORCED\n" +
//                "  ) WITH (\n" +
//                "  'connector.type' = 'jdbc', -- 使用 jdbc connector\n" +
//                "  'connector.url' = 'jdbc:mysql://******',\n" +
//                "  'connector.table' = 'map',\n" +
//                "  'connector.username' = '******',\n" +
//                "  'connector.password' = '******', \n" +
//                "  'connector.write.flush.max-rows' = '1' -- 默认 5000 条，为了演示改为 1 条\n" +
//                "  );";

//        String[] strs = a.split(";");
//        for(int i = 0; i <strs.length-1;i++) {
//            String substringAfter = org.apache.commons.lang3.StringUtils.substringAfter(strs[i], "CREATE TABLE ");
//            String table = org.apache.commons.lang3.StringUtils.substringBefore(substringAfter, " ");
//
////            String parmsBef = org.apache.commons.lang3.StringUtils.substringAfter(strs[i], ") WITH (");
////            String parm = org.apache.commons.lang3.StringUtils.substringBefore(parmsBef, ")");
//            System.out.println(parm);
        SdpFileBO sdpFileBO = new SdpFileBO();
        JobConfigs jobConfigs = new JobConfigs();
        SdpDataSource sdpDataSource = new SdpDataSource();
        sdpDataSource.setDatabaseName("test");
        sdpDataSource.setDataSourceUrl("jdbc://test");
        sdpDataSource.setUserName("root");
        sdpDataSource.setPassword("123");
        String[] sqls = SqlUtil.getStatements(a, FlinkSQLConstant.SEPARATOR);
        JobManager jobManager = JobManager.build(jobConfigs);
        CustomTableEnvironmentImpl managerEnv = jobManager.getEnv();
        StringBuffer metaTableSql = new StringBuffer();
        for (String item : sqls) {
            item = SqlUtil.removeNote(item);
            List<Operation> parse = managerEnv.getParser().parse(item);
            CreateTableOperation createTableOperation = (CreateTableOperation) parse.get(0);
            String databaseName = createTableOperation.getTableIdentifier().getObjectName();
            System.out.println(databaseName);
            Map<String, String> options = createTableOperation.getCatalogTable().getOptions();
            Map<String, String> replaceConnParam = dataSourceFactory.getDataSource("mysql").replaceConnParam(options,sdpDataSource);
            ArrayList<String> list = new ArrayList<>();
            for (Map.Entry<String, String> stringStringEntry : replaceConnParam.entrySet()) {
                String key = stringStringEntry.getKey();
                String value = stringStringEntry.getValue();
                StringBuffer stringBuffer = new StringBuffer();
                stringBuffer.append("'").append(key).append("'").append("=").append("'").append(value).append("'");
                list.add(stringBuffer.toString());
            }
            String join = StringUtils.join(list, ",\n");
            StringBuffer sql = new StringBuffer();
            sql.append(StringUtils.substringBeforeLast(item,"(")).append("(").append(join).append(");");
            metaTableSql.append(sql).append("\n");
        }
        System.out.println(metaTableSql);




    }

}
