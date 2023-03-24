package com.chitu.bigdata.sdp.job;

import cn.hutool.core.util.ReUtil;
import com.alibaba.fastjson.JSON;
import com.chitu.bigdata.sdp.api.bo.PromResultInfo;
import com.chitu.bigdata.sdp.api.domain.ConnectInfo;
import com.chitu.bigdata.sdp.api.enums.CertifyType;
import com.chitu.bigdata.sdp.api.model.*;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.constant.BusinessFlag;
import com.chitu.bigdata.sdp.constant.FlinkConfigKeyConstant;
import com.chitu.bigdata.sdp.constant.PromConstant;
import com.chitu.bigdata.sdp.interceptor.EnvHolder;
import com.chitu.bigdata.sdp.mapper.SdpJobInstanceMapper;
import com.chitu.bigdata.sdp.service.DataSourceService;
import com.chitu.bigdata.sdp.service.FileService;
import com.chitu.bigdata.sdp.service.MetaTableConfigService;
import com.chitu.bigdata.sdp.service.SdpKafkaConsumerRecordService;
import com.chitu.bigdata.sdp.service.datasource.KafkaDataSource;
import com.chitu.bigdata.sdp.service.monitor.PrometheusQueryService;
import com.chitu.bigdata.sdp.utils.CommonSqlParser;
import com.chitu.bigdata.sdp.utils.SqlParserUtil;
import com.chitu.bigdata.sdp.utils.StrUtils;
import com.chitu.cloud.config.scheduler.RedisLeaderSchedule;
import com.google.common.base.Splitter;
import com.xiaoleilu.hutool.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;
import org.apache.flink.sql.parser.ddl.SqlCreateTable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executor;
import java.util.stream.Collectors;

/**
 * kafka消费位置获取任务
 *
 * @author zouchangzhen
 * @date 2022/6/7
 */
@Slf4j
@Component
public class KafkaConsumerOffsetFetchJob {
    @Autowired
    SdpJobInstanceMapper sdpJobInstanceMapper;
    @Autowired
    PrometheusQueryService prometheusQueryService;
    @Autowired
    KafkaDataSource kafkaDataSource;
    @Autowired
    FileService fileService;
    @Autowired
    MetaTableConfigService metaTableConfigService;
    @Autowired
    DataSourceService dataSourceService;
    @Autowired
    SdpKafkaConsumerRecordService sdpKafkaConsumerRecordService;
    @Autowired
    Executor kafkaOffsetFetchExecutor;
    @Autowired
    SdpConfig sdpConfig;

    private final static  String MSG = "===ConsumerOffset[%s]: %s";

    //调度时间默认是10分钟一次
    //@Scheduled(fixedDelay = 2 * 60 * 1000)
    @RedisLeaderSchedule
    @Scheduled(fixedDelayString = "${custom.kafkaConsumerOffsetFetchJob.fixedDelay:600000}")
    public void kafkaConsumerOffsetFetch() {
        String osName = System.getProperties().getProperty("os.name");
        if (osName.equals("Windows 10")) {
            return;
        }
        List<String> envList = sdpConfig.getEnvList();
        for (String env : envList) {
            EnvHolder.addEnv(env);
            try {
                handleSingleEnv();
            } catch (Exception e) {
                String errMsg = StrUtils.parse1("【job环境env】: {}", env);
                log.warn(errMsg,e);
            }finally {
                EnvHolder.clearEnv();
            }
        }
    }

    private void handleSingleEnv() {
        List<SdpJobInstance> sdpJobInstances = sdpJobInstanceMapper.queryLatestJobs();
        if (CollectionUtils.isEmpty(sdpJobInstances)) {
            return;
        }

        long start = System.currentTimeMillis();
        CountDownLatch countDownLatch = new CountDownLatch(sdpJobInstances.size());
        for (SdpJobInstance sdpJobInstance : sdpJobInstances) {
            CompletableFuture.runAsync(() -> {
                handleSingle(sdpJobInstance);
            },kafkaOffsetFetchExecutor).whenComplete((v, t) -> {
                countDownLatch.countDown();
            }).exceptionally((e) -> {
                log.warn("kafkaConsumerOffsetFetch["+ sdpJobInstance.getId()+"] 异常", e);
                return null;
            });
        }
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        if(log.isTraceEnabled()){
            log.trace(String.format(MSG, "总耗时", (System.currentTimeMillis() - start) + ", 数据大小: " +sdpJobInstances.size() ));
        }
    }

    private void handleSingle(SdpJobInstance sdpJobInstance) {
        if(log.isTraceEnabled()){
            log.trace(String.format(MSG, sdpJobInstance.getId(), "==>> 开始获取消费位置"));
        }
        long start1 = System.currentTimeMillis();
        try {
            long start2 = System.currentTimeMillis();
            if (Objects.isNull(sdpJobInstance) || StrUtil.isBlank(sdpJobInstance.getJobContent())) {
                log.info(String.format(MSG, sdpJobInstance.getId(), "jobContent为空了"));
                return;
            }

            Map<String,Set<String>> kafkaServerTopicMap = new HashMap<>();
            List<ConnectInfo> connectInfos = new ArrayList<>();

            Map<String, Map<Integer, Long>> topicPartitionOffsetMap =null;

            start2 = System.currentTimeMillis();
            //获取kafka topic
            CommonSqlParser sqlParser = new CommonSqlParser(sdpJobInstance.getJobContent());
            if(log.isTraceEnabled()){
                log.trace(String.format(MSG, sdpJobInstance.getId(), "解析sql耗时："+ (System.currentTimeMillis() - start2)));
            }


            for (SqlCreateTable sqlCreateTable : sqlParser.getCreateTableList()) {
                //是否是kafka source判断
                if (!SqlParserUtil.isSourceKafka(sqlCreateTable)) {
                    continue;
                }

                List<String> mTopics = new ArrayList<>();

                Map<String, String> sqlTableOptionMap = SqlParserUtil.getSqlTableOptionMap(sqlCreateTable);
                String topic = sqlTableOptionMap.getOrDefault(FlinkConfigKeyConstant.TOPIC, "");
                List<String> xTopics = Splitter.on(";").splitToList(topic);
                if (CollectionUtils.isNotEmpty(xTopics)) {
                    mTopics.addAll(xTopics);
                }
                String topic_pattern = sqlTableOptionMap.getOrDefault(FlinkConfigKeyConstant.TOPIC_PATTERN, "");
                //主题为空或者topic-pattern不为空的跳过处理
                if(StrUtil.isNotBlank(topic_pattern) && StrUtil.isNotBlank(topic_pattern.trim())){
                    if(log.isTraceEnabled()){
                        log.trace(String.format(MSG, sdpJobInstance.getId(), "topic-pattern的不处理"));
                    }
                    continue;
                }
                if (CollectionUtils.isEmpty(mTopics)) {
                    continue;
                }

                ConnectInfo connectInfo = null;
                if(BusinessFlag.DI.name().equals(sdpJobInstance.getBusinessFlag())){
                    connectInfo = new ConnectInfo();
                    String servers = sqlTableOptionMap.get("properties.bootstrap.servers");
                    connectInfo.setAddress(servers);
                    connectInfo.setCertifyType("default");
                    String certifyType = sqlTableOptionMap.get("properties.sasl.mechanism");
                    String up = sqlTableOptionMap.get("properties.sasl.jaas.config");
                    if(StrUtil.isNotBlank(up)) {
                        String username = ReUtil.getGroup1(".*username\\s*=\\s*\"(.*)\"\\s+", up);
                        String password = ReUtil.getGroup1(".*password\\s*=\\s*\"(.*)\"\\s*", up);
                        if (StrUtil.isNotBlank(username) && StrUtil.isNotBlank(password)) {
                            connectInfo.setUsername(username);
                            connectInfo.setPwd(password);
                            connectInfo.setCertifyType(CertifyType.SASL.getType());
                        }
                    }
                }else {
                    SdpFile sdpFile = fileService.get(sdpJobInstance.getFileId());
                    if (Objects.isNull(sdpFile)) {
                        if(log.isTraceEnabled()) {
                            log.trace(String.format(MSG, sdpJobInstance.getId(), "文件为空【" + sdpJobInstance.getFileId() + "】"));
                        }
                        continue;
                    }
                    List<SdpMetaTableConfig> confogMetaList = metaTableConfigService.selectAll(new SdpMetaTableConfig(sdpFile.getId(), sqlCreateTable.getTableName().toString()));
                    if (CollectionUtils.isEmpty(confogMetaList)) {
                        if(log.isTraceEnabled()) {
                            log.trace(String.format(MSG, sdpJobInstance.getId(), "元表配置为空【" + sqlCreateTable.getTableName().toString() + "】"));
                        }
                        continue;
                    }
                    SdpMetaTableConfig confogMeta = confogMetaList.get(0);
                    SdpDataSource sdpDataSource = dataSourceService.getByIdWithPwdPlaintext(confogMeta.getDataSourceId());
                    if (Objects.isNull(sdpDataSource)) {
                        if(log.isTraceEnabled()) {
                            log.trace(String.format(MSG, sdpJobInstance.getId(), "数据源为空【" + confogMeta.getDataSourceId() + "】"));
                        }
                        continue;
                    }

                    connectInfo = new ConnectInfo();
                    connectInfo.setAddress(sdpDataSource.getDataSourceUrl());
                    connectInfo.setUsername(sdpDataSource.getUserName());
                    connectInfo.setPwd(sdpDataSource.getPassword());
                    connectInfo.setCertifyType(sdpDataSource.getCertifyType());
                }

                //兼容topic-pattern的情况
               /* if (StrUtil.isNotBlank(topic_pattern)) {
                    start2 = System.currentTimeMillis();
                    List<String> allTopic = kafkaDataSource.getTables(connectInfo);
                    log.info(String.format(MSG, sdpJobInstance.getId(), "topic pattern 获取topics 耗时："+ (System.currentTimeMillis() - start2)));


                    for (String tp : allTopic) {
                        if (ReUtil.isMatch(Pattern.compile(topic_pattern), tp)) {
                            mTopics.add(tp);
                        }
                    }
                }*/
                if (CollectionUtils.isEmpty(mTopics)) {
                    continue;
                }

                //收集起来分组处理
                Set<String> collectTopics = kafkaServerTopicMap.getOrDefault(connectInfo.getAddress(), new HashSet<>());
                collectTopics.addAll(mTopics);
                kafkaServerTopicMap.put(connectInfo.getAddress(),collectTopics);
                connectInfos.add(connectInfo);
            }

            if(CollectionUtils.isNotEmpty(connectInfos)){
                //获取kafka offset指标
                String promQL = String.format("%s{job_id=\"%s\"}", PromConstant.TOPIC_PARTITION_COMMITTEDOFFSET, sdpJobInstance.getFlinkJobId());

                start2 = System.currentTimeMillis();
                List<PromResultInfo> metrics = prometheusQueryService.getMetrics(promQL);
                if(log.isTraceEnabled()) {
                    log.trace(String.format(MSG, sdpJobInstance.getId(), "请求prometheus耗时："+ (System.currentTimeMillis() - start2)));
                }


                if (CollectionUtils.isEmpty(metrics)) {
                    if(log.isTraceEnabled()) {
                        log.trace(String.format(MSG, sdpJobInstance.getId(), "请求prometheus返回值为空【" + sdpJobInstance.getFlinkJobId() + "】"));
                    }
                    return;
                }
                //topic -> partition -> offset
                topicPartitionOffsetMap = new HashMap<>();
                for (PromResultInfo metric : metrics) {
                    if (Objects.isNull(metric)
                            || Objects.isNull(metric.getMetric())
                            || StrUtil.isBlank(metric.getMetric().getTopic())
                            || StrUtil.isBlank(metric.getMetric().getPartition())
                            || Objects.isNull(metric.getValue())
                            || metric.getValue().length <= 1
                    ) {
                        continue;
                    }
                    Map<Integer, Long> poMap = topicPartitionOffsetMap.getOrDefault(metric.getMetric().getTopic(), new HashMap<>());
                    poMap.put(Integer.valueOf(metric.getMetric().getPartition()), Long.valueOf(metric.getValue()[1]));
                    topicPartitionOffsetMap.put(metric.getMetric().getTopic(), poMap);
                }
                if(log.isTraceEnabled()) {
                    log.trace(String.format(MSG, sdpJobInstance.getId(), "topicPartitionOffsetMap: " + JSON.toJSONString(topicPartitionOffsetMap)));
                }
                if (org.springframework.util.CollectionUtils.isEmpty(topicPartitionOffsetMap)) {
                    return;
                }

                Map<String, ConnectInfo> addrConnMap = connectInfos.stream().collect(Collectors.toMap(ConnectInfo::getAddress, c -> c, (k1, k2) -> k2));
                for (Map.Entry<String, ConnectInfo> entry : addrConnMap.entrySet()) {
                    Set<String> topicSet = kafkaServerTopicMap.get(entry.getKey());
                    List<String> topicList = Optional.ofNullable(topicSet).map(m -> new ArrayList<>(m)).orElse(new ArrayList<>()).stream().filter(f -> StrUtil.isNotBlank(f)).collect(Collectors.toList());

                    start2 = System.currentTimeMillis();
                    Map<String, Long> topicTimestampMap = kafkaDataSource.partitionTimestampMap(entry.getValue(), null,topicList, topicPartitionOffsetMap,String.format(MSG,sdpJobInstance.getId() ,""));
                    String json = JSON.toJSONString(Optional.ofNullable(topicTimestampMap).orElse(new HashMap<>()));
                    if(log.isTraceEnabled()) {
                        log.trace(String.format(MSG, sdpJobInstance.getId(), "根据offset获取时间戳耗时："+ (System.currentTimeMillis() - start2) + ", topicTimestampMap: " +json));
                    }

                    if (!org.springframework.util.CollectionUtils.isEmpty(topicTimestampMap)) {
                        List<SdpKafkaConsumerRecord> records = new ArrayList<>();
                        topicTimestampMap.forEach((k, v) -> {
                            SdpKafkaConsumerRecord record = new SdpKafkaConsumerRecord();
                            record.setJobId(sdpJobInstance.getJobId());
                            record.setTopicName(k);
                            record.setTs(new Timestamp(v));
                            record.setEnabledFlag(1L);
                            records.add(record);
                        });

                        if (CollectionUtils.isNotEmpty(records)) {
                            sdpKafkaConsumerRecordService.getMapper().upsert(records);
                        }
                    }
                }
            }

        } catch (Exception e) {
            String errMsg = String.format(MSG,sdpJobInstance.getId(),"异常");
            log.warn(errMsg, e);
        }finally {
            if(log.isTraceEnabled()) {
                log.trace(String.format(MSG, sdpJobInstance.getId(), "<<== 耗时："+ (System.currentTimeMillis() - start1)));
            }
        }
    }
}
