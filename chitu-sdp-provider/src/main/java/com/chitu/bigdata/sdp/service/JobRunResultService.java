package com.chitu.bigdata.sdp.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chitu.bigdata.sdp.api.bo.JobRunResultFailDataBO;
import com.chitu.bigdata.sdp.api.domain.MetaTableConfigInfo;
import com.chitu.bigdata.sdp.api.domain.MetricTableValueInfo;
import com.chitu.bigdata.sdp.api.enums.DataSourceType;
import com.chitu.bigdata.sdp.api.enums.MetaTableType;
import com.chitu.bigdata.sdp.api.enums.RunResultType;
import com.chitu.bigdata.sdp.api.model.*;
import com.chitu.bigdata.sdp.api.vo.DiRunningResultVo;
import com.chitu.bigdata.sdp.api.vo.RunningResultVo;
import com.chitu.bigdata.sdp.constant.FlinkConfigKeyConstant;
import com.chitu.bigdata.sdp.constant.PromConstant;
import com.chitu.bigdata.sdp.service.monitor.PrometheusQueryService;
import com.chitu.bigdata.sdp.service.validate.constant.FlinkSQLConstant;
import com.chitu.bigdata.sdp.service.validate.custom.CustomTableEnvironmentImpl;
import com.chitu.bigdata.sdp.service.validate.domain.JobConfigs;
import com.chitu.bigdata.sdp.service.validate.job.JobManager;
import com.chitu.bigdata.sdp.service.validate.util.SqlUtil;
import com.chitu.bigdata.sdp.utils.CommonSqlParser;
import com.chitu.bigdata.sdp.utils.DateUtils;
import com.chitu.bigdata.sdp.utils.SqlParserUtil;
import com.chitu.cloud.model.ResponseData;
import com.google.common.collect.Maps;
import com.xiaoleilu.hutool.date.DateUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.sql.parser.ddl.SqlCreateTable;
import org.apache.flink.table.operations.Operation;
import org.apache.flink.table.operations.ddl.CreateTableOperation;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author sutao
 * @create 2022-03-03 14:27
 */
@Service
@Slf4j
@RefreshScope
public class JobRunResultService {
    private static final String prefixStr4Sink = "_database_";
    private static final String suffixStr4Sink = "___fields";


    @Autowired
    private MetaTableConfigService metaTableConfigService;

    @Autowired
    private DataSourceService dataSourceService;

    @Autowired
    private JobService jobService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private JobInstanceService jobInstanceService;

    @Autowired
    private PrometheusQueryService prometheusQueryService;

    @Autowired
    RestHighLevelClient restHighLevelClient;


    @Autowired
    SdpDwsRunningResultService sdpDwsRunningResultService;

    @Autowired
    SdpKafkaConsumerRecordService sdpKafkaConsumerRecordService;

    private static String INSERT_REGEX = "^((insert)|(INSERT)){1}( )+((into)|(INTO)){1}( )+((\\w)+(\\.){1}){2}(\\w)*";


    public ResponseData queryJobRunningResult(Long jobId) {
        ResponseData responseData = new ResponseData<>();
        List<RunningResultVo> runningResultList = new ArrayList<>();
        // 获取job详情
        SdpJob sdpJob = jobService.get(jobId);
        // 获取最新实例
        SdpJobInstance latestJobInstance = jobInstanceService.getLatestJobInstance(jobId);
        List<SdpMetaTableConfig> metaTableConfigList = metaTableConfigService.selectAll(new SdpMetaTableConfig(sdpJob.getFileId()));
        // 异步并行查询
        MetricTableValueInfo metricTableValueInfo = asyncQueryMetricTableValue(latestJobInstance.getFlinkJobId());
        if (CollectionUtils.isEmpty(metaTableConfigList)) {
            return queryDiRunningResult(metricTableValueInfo, responseData, sdpJob);
        }
        SdpKafkaConsumerRecord record = new SdpKafkaConsumerRecord();
        record.setJobId(jobId);
        List<SdpKafkaConsumerRecord> records = sdpKafkaConsumerRecordService.selectAll(record);
        Map<String, Timestamp> timestampMap = Optional.ofNullable(records).orElse(new ArrayList<>()).stream().collect(Collectors.toMap(SdpKafkaConsumerRecord::getTopicName, SdpKafkaConsumerRecord::getTs, (k1, k2) -> k2));

        CommonSqlParser commonSqlParser = null;
        Map<String, SqlCreateTable> flinkTableMap = Maps.newHashMap();
        try {
            commonSqlParser = new CommonSqlParser(sdpJob.getJobContent());
            Map<String, SqlCreateTable> mFlinkTableMap = commonSqlParser.getCreateTableList().stream().collect(Collectors.toMap(k -> k.getTableName().toString(), v -> v, (k1, k2) -> k2));
            if(!CollectionUtils.isEmpty(mFlinkTableMap)){
                flinkTableMap.putAll(mFlinkTableMap);
            }
        } catch (Exception e) {
            log.error("运行结果解析sql异常", e);
        }

        for (SdpMetaTableConfig item : metaTableConfigList) {
            SdpDataSource dataSource = dataSourceService.get(item.getDataSourceId());
            RunningResultVo runningResultVo = new RunningResultVo();
            runningResultVo.setDataSourceType(dataSource.getDataSourceType());
            runningResultVo.setDataSourceName(dataSource.getDataSourceName());
            runningResultVo.setDatabaseName(dataSource.getDatabaseName());
            runningResultVo.setMetaTableType(item.getMetaTableType());
            runningResultVo.setFlinkTableName(item.getFlinkTableName());
            runningResultVo.setMetatableName(item.getMetaTableName());
            //TODO 这里目前只通过数据源类型判断是来源表，实际要根据sql解析获取对应表的元表类型来判断
            if (DataSourceType.KAFKA.getType().equals(dataSource.getDataSourceType())) {
                SqlCreateTable sqlCreateTable = flinkTableMap.get(item.getFlinkTableName());

                Timestamp timestamp = timestampMap.get(item.getMetaTableName());
                if(Objects.nonNull(timestamp)){
                    runningResultVo.setConsumedTime(DateUtil.format(timestamp, DateUtils.YYYY_MM_DD_HH_MM_SS));
                }

                if (Objects.nonNull(sqlCreateTable)) {
                    Map<String, String> sqlTableOptionMap = SqlParserUtil.getSqlTableOptionMap(sqlCreateTable);
                    String topicPattern = sqlTableOptionMap.getOrDefault(FlinkConfigKeyConstant.TOPIC_PATTERN, "");
                    if (StrUtil.isNotBlank(topicPattern)) {
                        runningResultVo.setMetatableName(topicPattern);
                        runningResultVo.setConsumedTime("暂不支持查看");
                        //兼容topic-pattern的情况
                        /*runningResultVo.setMetatableName(topicPattern);
                        Map<String, Timestamp> timestampMap4TopicPattern = new HashMap<>();
                        for (Map.Entry<String, Timestamp> topicTs : timestampMap.entrySet()) {
                            if (!ReUtil.isMatch(topicPattern, topicTs.getKey())) {
                                continue;
                            }
                            timestampMap4TopicPattern.put(topicTs.getKey(), topicTs.getValue());
                        }
                        Optional<Timestamp> min = timestampMap4TopicPattern.values().stream().min(Timestamp::compareTo);
                        if (min.isPresent()) {
                            runningResultVo.setConsumedTime(min.get());
                        } else {
                            runningResultVo.setConsumedTime(null);
                        }*/
                    }
                }
                runningResultVo.setConsumedTotal(metricTableValueInfo.getConsumedTotalMap().get(item.getFlinkTableName()));
//                runningResultVo.setDeserializeFailNum(metricTableValueInfo.getDeserializeFailNumMap().get(item.getFlinkTableName()));
                runningResultVo.setPendingRecords(metricTableValueInfo.getPendingRecordsMap().get(item.getFlinkTableName()));
                runningResultVo.setSinkSuccessRecords(metricTableValueInfo.getSinkKafkaSuccessRecordMap().get(item.getFlinkTableName()));
            } else {
            }
            runningResultList.add(runningResultVo);
        }

        // 分组返回
        Map<String, List<RunningResultVo>> metaTableTypeGroupMap = runningResultList.stream().filter(item -> StrUtil.isNotEmpty(item.getMetaTableType())).collect(Collectors.groupingBy(RunningResultVo::getMetaTableType));
        responseData.setData(metaTableTypeGroupMap).ok();
        log.info("queryJobRunningResult: {}", JSONObject.toJSONString(responseData));
        return responseData;
    }


    private MetricTableValueInfo asyncQueryMetricTableValue(String flinkJobId) {
        MetricTableValueInfo metricTableValueInfo = new MetricTableValueInfo();
        CompletableFuture<Void> consumedTotal = CompletableFuture.runAsync(() -> {
            String promQL = String.format("%s{job_id=\"%s\"}", PromConstant.RECORDS_CONSUMED_TOTAL, flinkJobId);
            metricTableValueInfo.setConsumedTotalMap(prometheusQueryService.getMetricTableValueSum(promQL));
        });
//        CompletableFuture<Void> deserializeFailNum = CompletableFuture.runAsync(() -> {
//            String promQL = String.format("%s{job_id=\"%s\"}", PromConstant.DESERIALIZEFAILNUM, flinkJobId);
//            metricTableValueInfo.setDeserializeFailNumMap(prometheusQueryService.getMetricTableValueSum(promQL));
//        });
        CompletableFuture<Void> pendingRecords = CompletableFuture.runAsync(() -> {
            String promQL = String.format("%s{job_id=\"%s\"}", PromConstant.PENDINGRECORDS, flinkJobId);
            metricTableValueInfo.setPendingRecordsMap(prometheusQueryService.getMetricTableValueSum(promQL));
        });

        // TODO 如果数据源类型是kafka且是sink表才去查询
        CompletableFuture<Void> sinkKafkaSuccessRecord = CompletableFuture.runAsync(() -> {
            String promQL = String.format("%s{job_id=\"%s\"}", PromConstant.KAFKAPRODUCER_RECORD_SEND_TOTAL, flinkJobId);
            metricTableValueInfo.setSinkKafkaSuccessRecordMap(prometheusQueryService.getMetricTableValueSum(promQL, MetaTableType.SINK.getType()));
        });
        // TODO 如果数据源类型是hive且是sink表才去查询
       /* CompletableFuture<Void> sinkHiveSuccessRecords = CompletableFuture.runAsync(() -> {
            String promQL = String.format("%s{job_id=\"%s\",operator_name=\"StreamingFileWriter\"}", PromConstant.SINK_HIVE_NUMRECORDSIN, flinkJobId);
            metricTableValueInfo.setSinkHiveRecordsMap(prometheusQueryService.getMetricTableValueSum(promQL));
        });*/
        try {
            CompletableFuture.allOf(consumedTotal, pendingRecords, sinkKafkaSuccessRecord).get();
        } catch (InterruptedException e) {
            log.error("CompletableFuture.allOf 线程中断异常", e);
        } catch (ExecutionException e) {
            log.error("CompletableFuture.allOf 线程执行异常", e);
        }
        log.info("metricTableValueInfo: {}", JSONObject.toJSONString(metricTableValueInfo));
        return metricTableValueInfo;
    }


    public ResponseData queryFailData(JobRunResultFailDataBO jobRunResultFailData) {
        ResponseData responseData = new ResponseData<>();
        log.info("queryFailData: {}", JSONObject.toJSONString(responseData));
        return responseData;
    }


    public ResponseData queryDiRunningResult(MetricTableValueInfo metricTableValueInfo, ResponseData responseData, SdpJob sdpJob) {
        //di数据集成作业没有解析对应的元表信息，按照返回的key进行获取数据分source和sink
        ArrayList<MetaTableConfigInfo> sdpMetaTableConfigs = getMetaTableConfigInfos(sdpJob);
        List<RunningResultVo> runningResultList = new ArrayList<>();
        for (MetaTableConfigInfo sdpMetaTableConfig : sdpMetaTableConfigs) {
            if (sdpMetaTableConfig.getDataSourceType().equalsIgnoreCase(DataSourceType.KAFKA.getType())) {
                RunningResultVo runningResultVo = new RunningResultVo();
                runningResultVo.setMetatableName(sdpMetaTableConfig.getMetaTableName());
                runningResultVo.setDataSourceType(sdpMetaTableConfig.getDataSourceType());
                runningResultVo.setConsumedTotal(metricTableValueInfo.getConsumedTotalMap().get(sdpMetaTableConfig.getFlinkTableName()));
                Long failNum = metricTableValueInfo.getDeserializeFailNumMap().get(sdpMetaTableConfig.getFlinkTableName());
                runningResultVo.setDeserializeFailNum(failNum == null ? 0L : failNum);
                runningResultVo.setPendingRecords(metricTableValueInfo.getPendingRecordsMap().get(sdpMetaTableConfig.getFlinkTableName()));
                runningResultList.add(runningResultVo);
            }
        }


        HashMap<String, List<RunningResultVo>> stringListHashMap = new HashMap<>();
        stringListHashMap.put("source", runningResultList);
        List<RunningResultVo> sinkList = new ArrayList<>();
        RunningResultVo runningResultVo = new RunningResultVo();
        sinkList.add(runningResultVo);
        stringListHashMap.put("sink", sinkList);
        responseData.setData(stringListHashMap).ok();
        return responseData;
    }

    private ArrayList<MetaTableConfigInfo> getMetaTableConfigInfos(SdpJob sdpJob) {
        String jobContent = sdpJob.getJobContent();
        JobConfigs jobConfigs = new JobConfigs();
        String[] statements = SqlUtil.getStatements(jobContent, FlinkSQLConstant.SEPARATOR);
        JobManager jobManager = JobManager.build(jobConfigs);
        CustomTableEnvironmentImpl managerEnv = jobManager.getEnv();
        ArrayList<MetaTableConfigInfo> sdpMetaTableConfigs = new ArrayList<>();
        for (String statement : statements) {
            if (StrUtil.isNotBlank(statement)) {
                statement = SqlUtil.removeNote(statement);
                if (statement.startsWith("insert") || statement.startsWith("INSERT")) {
                    Pattern compile = Pattern.compile(INSERT_REGEX);
                    Matcher matcher = compile.matcher(statement);
                    while (matcher.find()) {
                        String tableTrim = matcher.group(0).trim();
                        String tableStr = StrUtil.subAfter(tableTrim, " ", true);
                        String[] split = tableStr.split("\\.");
                        if (split.length == 3) {
                            MetaTableConfigInfo metaTableConfigInfo = new MetaTableConfigInfo();
                            metaTableConfigInfo.setDataSourceType(DataSourceType.HIVE.getType());
                            metaTableConfigInfo.setDataBaseName(split[1]);
                            metaTableConfigInfo.setMetaTableName(split[2]);
                            metaTableConfigInfo.setMetaTableType(MetaTableType.SINK.getType());
                            sdpMetaTableConfigs.add(metaTableConfigInfo);
                        }
                    }
                    continue;
                }
                List<Operation> parse = managerEnv.getParser().parse(statement);
                Operation operation = parse.get(0);
                Map<String, String> options = null;
                MetaTableConfigInfo sdpMetaTableConfig = new MetaTableConfigInfo();
                String tableName = null;
                if (operation instanceof CreateTableOperation) {
                    CreateTableOperation createTableOperation = (CreateTableOperation) operation;
                    options = createTableOperation.getCatalogTable().getOptions();
                    tableName = createTableOperation.getTableIdentifier().getObjectName();
                    sdpMetaTableConfig.setFlinkTableName(tableName);
                    String connectorType = options.get("connector");
                    sdpMetaTableConfig.setDataSourceType(connectorType);
                    Set<String> typeSet = options.keySet();
                    for (String type : typeSet) {
                        switch (type) {
                            case "topic":
                                sdpMetaTableConfig.setMetaTableName(options.get("topic"));
                                break;
                            case "kudu.table":
                                sdpMetaTableConfig.setMetaTableName(options.get("kudu.table"));
                                break;
                            case "index":
                                sdpMetaTableConfig.setMetaTableName(options.get("index"));
                                break;
                            default:
                                break;
                        }
                    }
                    sdpMetaTableConfigs.add(sdpMetaTableConfig);
                }
            }
        }
        return sdpMetaTableConfigs;
    }

    public ResponseData queryDiAlertJobsResult(List<Long> ids) {
        ResponseData responseData = new ResponseData<>();
        responseData.ok();
        List<DiRunningResultVo> runningResultList = new ArrayList<>();
        Map<String, MetricTableValueInfo> valueInfoMap = asyncQueryJobMetric();
        // 获取job详情
        ids.forEach(id -> {
            //获取job进行对元表数据替换
            SdpJob sdpJob = jobService.get(id);
            // 获取最新实例
            SdpJobInstance latestJobInstance = jobInstanceService.getLatestJobInstance(id);
            DiRunningResultVo diRunningResultVo = new DiRunningResultVo();
            diRunningResultVo.setJobId(id);
            ArrayList<MetaTableConfigInfo> metaTableConfigInfos = getMetaTableConfigInfos(sdpJob);
            log.info("DI的sql解析元表实例:{}", JSON.toJSONString(metaTableConfigInfos));
            if (Objects.nonNull(latestJobInstance.getFlinkJobId())) {
                MetricTableValueInfo info = valueInfoMap.get(latestJobInstance.getFlinkJobId());
                if (Objects.nonNull(info)) {
                    Map<String, Long> consumedTotalMap = info.getConsumedTotalMap();
                    Map<String, Long> deserializeFailNumMap = info.getDeserializeFailNumMap();
                    Map<String, Long> pendingRecordsMap = info.getPendingRecordsMap();
                    fillResultVo(diRunningResultVo, metaTableConfigInfos, consumedTotalMap, RunResultType.CONSUMED_TOTAL.getName());
                    fillResultVo(diRunningResultVo, metaTableConfigInfos, deserializeFailNumMap, RunResultType.DESERIALIZE_FAIL.getName());
                    fillResultVo(diRunningResultVo, metaTableConfigInfos, pendingRecordsMap, RunResultType.PENDING_RECORDS.getName());
                    runningResultList.add(diRunningResultVo);
                }
            }
        });

        return responseData.setData(runningResultList);
    }

    private void fillResultVo(DiRunningResultVo diRunningResultVo, ArrayList<MetaTableConfigInfo> metaTableConfigInfos, Map<String, Long> consumedTotalMap, String type) {
        ArrayList<RunningResultVo> runningResultVos = new ArrayList<>();
        if (Objects.nonNull(consumedTotalMap) && consumedTotalMap.size() > 0) {
            for (Map.Entry<String, Long> stringLongEntry : consumedTotalMap.entrySet()) {
                for (MetaTableConfigInfo metaTableConfigInfo : metaTableConfigInfos) {
                    if (DataSourceType.KAFKA.getType().equalsIgnoreCase(metaTableConfigInfo.getDataSourceType()) &&
                            metaTableConfigInfo.getFlinkTableName().equalsIgnoreCase(stringLongEntry.getKey())) {
                        RunningResultVo runningResultVo = new RunningResultVo();
                        runningResultVo.setMetatableName(metaTableConfigInfo.getMetaTableName());
                        runningResultVo.setDataSourceType(DataSourceType.KAFKA.getType());
                        if (RunResultType.CONSUMED_TOTAL.getName().equalsIgnoreCase(type)) {
                            runningResultVo.setConsumedTotal(stringLongEntry.getValue());
                        } else if (RunResultType.DESERIALIZE_FAIL.getName().equalsIgnoreCase(type)) {
                            runningResultVo.setDeserializeFailNum(stringLongEntry.getValue());
                        } else if (RunResultType.PENDING_RECORDS.getName().equalsIgnoreCase(type)) {
                            runningResultVo.setPendingRecords(stringLongEntry.getValue());
                        }
                        runningResultVos.add(runningResultVo);
                    }
                }
            }
        } else {
            RunningResultVo runningResultVo = new RunningResultVo();
            runningResultVos.add(runningResultVo);
        }
        if (RunResultType.CONSUMED_TOTAL.getName().equalsIgnoreCase(type)) {
            diRunningResultVo.setConsumedTotal(runningResultVos);
        } else if (RunResultType.DESERIALIZE_FAIL.getName().equalsIgnoreCase(type)) {
            diRunningResultVo.setDeserializeFailNum(runningResultVos);
        } else if (RunResultType.PENDING_RECORDS.getName().equalsIgnoreCase(type)) {
            diRunningResultVo.setPendingRecords(runningResultVos);
        }
    }

    private Map<String, MetricTableValueInfo> asyncQueryJobMetric() {
        HashMap<String, MetricTableValueInfo> metricTableValueInfos = new HashMap<String, MetricTableValueInfo>(100);
        ArrayList<MetricTableValueInfo> resultList = new ArrayList<>();
        CompletableFuture<Void> consumedTotal = CompletableFuture.runAsync(() -> {
            String promQL = PromConstant.RECORDS_CONSUMED_TOTAL;
            resultList.addAll(prometheusQueryService.getJobsMetricValueSum(promQL));
        });
        CompletableFuture<Void> deserializeFailNum = CompletableFuture.runAsync(() -> {
            String promQL = PromConstant.DESERIALIZEFAILNUM;
            resultList.addAll(prometheusQueryService.getJobsMetricValueSum(promQL));
        });
        CompletableFuture<Void> pendingRecords = CompletableFuture.runAsync(() -> {
            String promQL = PromConstant.PENDINGRECORDS;
            resultList.addAll(prometheusQueryService.getJobsMetricValueSum(promQL));
        });
        try {
            CompletableFuture.allOf(consumedTotal, deserializeFailNum, pendingRecords).get();
        } catch (InterruptedException e) {
            log.error("CompletableFuture.allOf 线程中断异常", e);
        } catch (ExecutionException e) {
            log.error("CompletableFuture.allOf 线程执行异常", e);
        }
        if (CollectionUtils.isEmpty(resultList)) {
            return new HashMap<String, MetricTableValueInfo>(1);
        }
        Map<String, List<MetricTableValueInfo>> listMap = resultList.stream().collect(Collectors.groupingBy(MetricTableValueInfo::getFlinkAppId));
        Set<String> keySet = listMap.keySet();
        for (String key : keySet) {
            MetricTableValueInfo metricTableValueInfo = new MetricTableValueInfo();
            List<MetricTableValueInfo> infos = listMap.get(key);
            metricTableValueInfo.setFlinkAppId(key);
            for (MetricTableValueInfo info : infos) {
                if (Objects.nonNull(info.getConsumedTotalMap())) {
                    metricTableValueInfo.setConsumedTotalMap(info.getConsumedTotalMap());
                }
                if (Objects.nonNull(info.getDeserializeFailNumMap())) {
                    metricTableValueInfo.setDeserializeFailNumMap(info.getConsumedTotalMap());
                }
                if (Objects.nonNull(info.getPendingRecordsMap())) {
                    metricTableValueInfo.setPendingRecordsMap(info.getPendingRecordsMap());
                }
            }
            metricTableValueInfos.put(key, metricTableValueInfo);
        }
        log.info("metricTableValueInfos: {}", JSONObject.toJSONString(metricTableValueInfos));
        return metricTableValueInfos;
    }
}
