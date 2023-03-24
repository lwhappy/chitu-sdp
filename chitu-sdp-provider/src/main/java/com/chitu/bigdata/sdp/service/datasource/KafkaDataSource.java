package com.chitu.bigdata.sdp.service.datasource;

import cn.hutool.core.util.ReUtil;
import cn.hutool.crypto.SecureUtil;
import cn.hutool.json.JSONUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.chitu.bigdata.sdp.api.domain.ConnectInfo;
import com.chitu.bigdata.sdp.api.domain.FlinkTableGenerate;
import com.chitu.bigdata.sdp.api.domain.MetadataTableColumn;
import com.chitu.bigdata.sdp.api.domain.SourceMeta;
import com.chitu.bigdata.sdp.api.enums.CertifyType;
import com.chitu.bigdata.sdp.api.enums.DataSourceOption;
import com.chitu.bigdata.sdp.api.enums.FormatType;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.api.model.SdpDataSource;
import com.chitu.bigdata.sdp.config.CheckConfigProperties;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.constant.CommonConstant;
import com.chitu.bigdata.sdp.constant.FlinkConfigKeyConstant;
import com.chitu.bigdata.sdp.constant.FlinkDataTypeMapping;
import com.chitu.bigdata.sdp.service.ProjectService;
import com.chitu.bigdata.sdp.service.datasource.format.AbstractKafkaDataFormat;
import com.chitu.bigdata.sdp.service.datasource.format.KafkaDataFormatFactory;
import com.chitu.bigdata.sdp.utils.*;
import com.chitu.cloud.exception.ApplicationException;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.xiaoleilu.hutool.util.CollectionUtil;
import com.xiaoleilu.hutool.util.MapUtil;
import com.xiaoleilu.hutool.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.calcite.sql.SqlNode;
import org.apache.flink.sql.parser.ddl.SqlCreateTable;
import org.apache.flink.sql.parser.ddl.SqlTableOption;
import org.apache.kafka.clients.CommonClientConfigs;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.config.SaslConfigs;
import org.apache.kafka.common.errors.GroupAuthorizationException;
import org.apache.kafka.common.errors.TopicAuthorizationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.function.Function;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * @author sutao
 * @create 2021-12-09 17:19
 */
@Component("kafka")
@Slf4j
@RefreshScope
public class KafkaDataSource extends AbstractDataSource<AdminClient> {

    @Value("${kafka.consumer.sasl.groupId}")
    private String consumerSaslKafkaMetaGroupId;

    @Autowired
    KafkaDataFormatFactory kafkaDataFormatFactory;

    @Autowired
    CheckConfigProperties checkConfigProperties;
    @Autowired
    SdpConfig sdpConfig;


    @Autowired
    ProjectService projectService;

    private static final String TOPIC_RESP = "没有权限读取topic,异常%s";
    private static final String GROUP_RESP = "group没有权限获取数据,异常%s";

    private static final String GROUP_ID = "getTopicColumnGroup";

    @Override
    public AdminClient getConnection(ConnectInfo connectInfo) throws Exception {
        Map<String, Object> props = assembleMap(connectInfo,null);
        AdminClient adminClient = AdminClient.create(props);
        try {
            adminClient.describeCluster().nodes().get();
            return adminClient;
        } catch (Exception e){
           closeConnection(adminClient);
           throw e;
        }
    }

    private Map<String, Object> assembleMap(ConnectInfo connectInfo,Map<String, Object> appendConfMap) {
        Map<String, Object> props = new HashMap<>();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, connectInfo.getAddress());
        props.put(AdminClientConfig.REQUEST_TIMEOUT_MS_CONFIG, 20000);
        props.put(AdminClientConfig.RETRIES_CONFIG, "1");
        if(!CollectionUtils.isEmpty(appendConfMap)){
            props.putAll(appendConfMap);
        }

        if (Objects.nonNull(connectInfo.getCertifyType()) && CertifyType.SASL.getType().equals(connectInfo.getCertifyType())) {
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
            props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256");
            props.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"" + connectInfo.getUsername() + "\" password=\"" + connectInfo.getPwd() + "\";");
        }

        return props;
    }

    @Override
    public void closeConnection(AdminClient adminClient) {
        if (adminClient != null) {
            adminClient.close();
        }
    }

    @Override
    public boolean tableExists(ConnectInfo connectInfo, String tableName) throws Exception {
        if (this.getTables(connectInfo).contains(tableName)) {
            return true;
        }
        return false;
    }

    @Override
    public List<String> getTables(ConnectInfo connectInfo) throws Exception {
        List<String> tables = new ArrayList<>();
        AdminClient adminClient = null;
        try {
            adminClient = getConnection(connectInfo);
            Set<String> topicsName = adminClient.listTopics().names().get();
            for (String name : topicsName) {
                tables.add(name);
            }
        } finally {
            closeConnection(adminClient);
        }
        return tables;
    }

    @Override
    public List<MetadataTableColumn> getTableColumns(ConnectInfo connectInfo, String tableName) {
        List<MetadataTableColumn> metadataTableColumnList = new ArrayList<>();
        Properties props = new Properties();
        props.put("bootstrap.servers", connectInfo.getAddress());
        props.put("group.id", GROUP_ID);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("enable.auto.commit", "false");

        if (Objects.nonNull(connectInfo.getCertifyType()) && CertifyType.SASL.getType().equals(connectInfo.getCertifyType())) {
            //TODO 这个消费组id必须授权才能消费
            props.put("group.id", consumerSaslKafkaMetaGroupId);
            props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
            props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256");
            props.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"" + connectInfo.getUsername() + "\" password=\"" + connectInfo.getPwd() + "\";");
        }
        //latest  earliest
        props.put("auto.offset.reset", "earliest");
        KafkaConsumer<String, String> consumer = null;
        try {
            consumer = new KafkaConsumer<>(props);
            consumer.subscribe(Collections.singletonList(tableName));
            //轮询拉取次数
            int pollSize = 0;
            while (true) {
                ConsumerRecords<String, String> records = consumer.poll(200);
                pollSize++;
                log.info("poll kafka data [{}] 次", pollSize);
                for (ConsumerRecord<String, String> record : records) {
                    log.info("consumer kafka data: {}", record.value());
                    if(JsonUtils.isCanalJson(record.value())){
                        //canal-json
                        JSONObject jsonObject = JSON.parseObject(record.value(), Feature.OrderedField);
                        if (Objects.isNull(jsonObject)) {
                            continue;
                        }
                        JSONArray data = jsonObject.getJSONArray("data");
                        if (CollectionUtil.isEmpty(data)) {
                            continue;
                        }
                        JSONObject dataJson = data.getJSONObject(0);

                        dataJson.forEach((k, v) -> {
                            MetadataTableColumn metadataTableColumn = new MetadataTableColumn();
                            metadataTableColumn.setColumnName(k);
                            metadataTableColumn.setColumnType(FlinkDataTypeMapping.NOT_EXIST_DEFAULT_TYPE);
                            metadataTableColumn.setFormatType(FormatType.CANAL_JSON);
                            metadataTableColumnList.add(metadataTableColumn);
                        });
                    }else if (JSONUtil.isJson(record.value())) {
                        //json
                        Map<String, Object> logMap = JSON.parseObject(record.value(), LinkedHashMap.class, Feature.OrderedField);
                        logMap.forEach((k, v) -> {
                            MetadataTableColumn metadataTableColumn = new MetadataTableColumn();
                            metadataTableColumn.setColumnName(k);
                            metadataTableColumn.setColumnType(FlinkDataTypeMapping.NOT_EXIST_DEFAULT_TYPE);
                            metadataTableColumn.setFormatType(FormatType.JSON);
                            metadataTableColumnList.add(metadataTableColumn);
                        });
                    }else {
                        //raw
                        MetadataTableColumn metadataTableColumn = new MetadataTableColumn();
                        metadataTableColumn.setColumnName("log");
                        metadataTableColumn.setColumnType(FlinkDataTypeMapping.NOT_EXIST_DEFAULT_TYPE);
                        metadataTableColumn.setFormatType(FormatType.RAW);
                        metadataTableColumnList.add(metadataTableColumn);
                    }
                    break;
                }
                String env = sdpConfig.getEnvFromEnvHolder(log);
                CheckConfigProperties.CheckConfig configs = checkConfigProperties.getEnvMap().get(env);
                //如果有数据或者拉取fetchColNumOfTimes次(fetchColNumOfTimes*200/1000秒)都没数据，则退出循环
                if (!records.isEmpty() || CollectionUtil.isNotEmpty(metadataTableColumnList) || pollSize >= configs.getKafka().getFetchColNumOfTimes()) {
                    break;
                }
            }
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }
        return metadataTableColumnList;
    }

    @Override
    public String generateDdl(FlinkTableGenerate flinkTableGenerate) {
        //flinkTableGenerate.getMetadataTableColumnList().get(0).getFormatType() 的 formatType字段是必须色设置的
        FormatType formatType = !CollectionUtil.isEmpty(flinkTableGenerate.getMetadataTableColumnList()) && Objects.nonNull(flinkTableGenerate.getMetadataTableColumnList().get(0).getFormatType())?flinkTableGenerate.getMetadataTableColumnList().get(0).getFormatType():FormatType.JSON;
        AbstractKafkaDataFormat kafkaDataFormat = kafkaDataFormatFactory.getKafkaDataFormat(formatType);
        return  kafkaDataFormat.generateDdl(flinkTableGenerate);
    }

    @Override
    public Map<String, String> replaceConnParam(Map<String, String> options, SdpDataSource sdpDataSource) {
        if(log.isTraceEnabled()){
            log.trace("replaceConnParam 替换前: {}",Objects.nonNull(options)?JSON.toJSONString(options):"null");
        }
        String mechanism = options.get(DataSourceOption.KafkaOption.MECHANISM_CONFIG.getOption());
        for (String key : options.keySet()) {
            switch (DataSourceOption.KafkaOption.ofOption(key)) {
                case BOOTSTRAP:
                    options.put(key, sdpDataSource.getDataSourceUrl());
                    break;
                case SASL_CONFIG:
                    changeSaslPsw(key, options, sdpDataSource);
                default:
                    break;
            }
        }
        if(log.isTraceEnabled()){
            log.trace("replaceConnParam 替换后: {}",Objects.nonNull(options)?JSON.toJSONString(options):"null");
        }
        return options;
    }

    private void changeSaslPsw(String key, Map<String, String> options, SdpDataSource sdpDataSource) {
        String value = options.get(key);
        String prex = StrUtil.subBefore(value, StrUtils.EQUAL_SIGN, true);
        options.put(key,prex+"=\""+sdpDataSource.getPassword()+"\";");
    }



    public  String checkAsalConnect(ConnectInfo connectInfo, String metaTableName, String groupId){
        String result = null;
        Properties props = new Properties();
        props.put("bootstrap.servers", connectInfo.getAddress());
        props.put("group.id", groupId);
        props.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.put("enable.auto.commit", "false");

        props.put(CommonClientConfigs.SECURITY_PROTOCOL_CONFIG, "SASL_PLAINTEXT");
        props.put(SaslConfigs.SASL_MECHANISM, "SCRAM-SHA-256");
        props.put("sasl.jaas.config", "org.apache.kafka.common.security.scram.ScramLoginModule required username=\"" + connectInfo.getUsername() + "\" password=\"" + connectInfo.getPwd() + "\";");

        props.put("auto.offset.reset", "earliest");
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(props);
        consumer.subscribe(Collections.singletonList(metaTableName));
        try {
            ConsumerRecords<String, String> records = consumer.poll(200);
        } catch (Exception e) {
            if(e instanceof TopicAuthorizationException){
               return String.format(TOPIC_RESP,e.getMessage());
            }
            if (e instanceof GroupAuthorizationException){
                return String.format(GROUP_RESP,e.getMessage());
            }
            result = e.getMessage();
        } finally {
            if (consumer != null) {
                consumer.close();
            }
        }
        return result;
    }

    @Override
    public void initTable(SourceMeta meta) {
        AdminClient client = KafkaConnectUtil.getClient(meta.getKafkaServer());
        try {
            for (String topic : meta.getTopics()) {
                //为实现统一的offset，先删除再创建
                client.deleteTopics(Arrays.asList(topic));
                NewTopic newTopic = new NewTopic(topic, meta.getNumPartitions(), meta.getReplica().shortValue());
                client.createTopics(Arrays.asList(newTopic));
            }

        }catch (Exception e){
            log.error("===删除topic或者新建topic失败，异常:{}",e);
        } finally {
            client.close();
        }
    }


    @Override
    public String modifyOption4ChangeEnv(String ddl, SdpDataSource sdpDataSource) throws Exception {
        Map<String, String> kafkaFlinkTableAndCreateTableMap = sdpDataSource.getKafkaFlinkTableAndCreateTableMap();

        CommonSqlParser commonSqlParser = new CommonSqlParser(ddl);
        SqlCreateTable sqlCreateTable = commonSqlParser.getCreateTableList().get(0);

        String prodSql = "";
        if(MapUtil.isNotEmpty(kafkaFlinkTableAndCreateTableMap)){
            prodSql = kafkaFlinkTableAndCreateTableMap.get(sqlCreateTable.getTableName().toString());
        }

        List<SqlNode> options = sqlCreateTable.getPropertyList().getList();
        if(StrUtil.isNotBlank(prodSql)){
            //存在，消费位置信息就要以生产的为主。删除uat的所有消费位置模式选项
            CommonSqlParser commonSqlParser1 = new CommonSqlParser(prodSql);
            SqlCreateTable sqlCreateTable1 = commonSqlParser1.getCreateTableList().get(0);
            Map<String, SqlTableOption> replaceOptionsMap = SqlParserUtil.getTableOptionMap(sqlCreateTable1);

            int offset1 = removeOption(options, FlinkConfigKeyConstant.STARTUP_MODE);
            SqlTableOption sqlTableOption1 = replaceOptionsMap.get(FlinkConfigKeyConstant.STARTUP_MODE);
            if(Objects.nonNull(sqlTableOption1)){
                if (-1 == offset1) {
                    insertOption(options, options.size(),sqlTableOption1);
                } else {
                    insertOption(options, offset1, sqlTableOption1);
                }
            }

            int offset2 = removeOption(options, FlinkConfigKeyConstant.STARTUP_OFFSETS);
            SqlTableOption sqlTableOption2 = replaceOptionsMap.get(FlinkConfigKeyConstant.STARTUP_OFFSETS);
            if(Objects.nonNull(sqlTableOption2)){
                if (-1 == offset2) {
                    insertOption(options, options.size(), sqlTableOption2);
                } else {
                    insertOption(options, offset2, sqlTableOption2);
                }
            }

            int offset3 = removeOption(options, FlinkConfigKeyConstant.STARTUP_TIMESTAMP);
            SqlTableOption sqlTableOption3 = replaceOptionsMap.get(FlinkConfigKeyConstant.STARTUP_TIMESTAMP);
            if(Objects.nonNull(sqlTableOption3)){
                if (-1 == offset3) {
                    insertOption(options, options.size(), sqlTableOption3);
                } else {
                    insertOption(options, offset3, sqlTableOption3);
                }
            }

        }


        if (CertifyType.SASL.getType().equals(sdpDataSource.getCertifyType())) {

            Map<String,String> optionsMap = Maps.newHashMap();
            optionsMap.put(FlinkConfigKeyConstant.PROPERTIES_SECURITY_PROTOCOL,"SASL_PLAINTEXT");
            optionsMap.put(FlinkConfigKeyConstant.PROPERTIES_SASL_MECHANISM,"SCRAM-SHA-256");
            optionsMap.put(FlinkConfigKeyConstant.PROPERTIES_SASL_JAAS_CONFIG,String.format("org.apache.kafka.common.security.scram.ScramLoginModule required username=\"%s\" password=\"%s\"",sdpDataSource.getUserName(), SecureUtil.md5(sdpDataSource.getPassword())));
            Map<String, SqlTableOption> replaceOptionsMap = SqlParserUtil.getTableOptionMap(optionsMap);

            int offset1 = removeOption(options, FlinkConfigKeyConstant.PROPERTIES_SECURITY_PROTOCOL);
            if (-1 == offset1) {
                insertOption(options, options.size(), replaceOptionsMap.get(FlinkConfigKeyConstant.PROPERTIES_SECURITY_PROTOCOL));
            } else {
                insertOption(options, offset1, replaceOptionsMap.get(FlinkConfigKeyConstant.PROPERTIES_SECURITY_PROTOCOL));
            }

            int offset2 = removeOption(options, FlinkConfigKeyConstant.PROPERTIES_SASL_MECHANISM);
            if (-1 == offset2) {
                insertOption(options, options.size(), replaceOptionsMap.get(FlinkConfigKeyConstant.PROPERTIES_SASL_MECHANISM));
            } else {
                insertOption(options, offset2, replaceOptionsMap.get(FlinkConfigKeyConstant.PROPERTIES_SASL_MECHANISM));
            }

            int offset3 = removeOption(options, FlinkConfigKeyConstant.PROPERTIES_SASL_JAAS_CONFIG);
            if (-1 == offset3) {
                insertOption(options, options.size(), replaceOptionsMap.get(FlinkConfigKeyConstant.PROPERTIES_SASL_JAAS_CONFIG));
            } else {
                insertOption(options, offset3, replaceOptionsMap.get(FlinkConfigKeyConstant.PROPERTIES_SASL_JAAS_CONFIG));
            }
        }else {
            //非认证的需要清掉认证信息
            removeOption(options,FlinkConfigKeyConstant.PROPERTIES_SECURITY_PROTOCOL);
            removeOption(options,FlinkConfigKeyConstant.PROPERTIES_SASL_MECHANISM);
            removeOption(options,FlinkConfigKeyConstant.PROPERTIES_SASL_JAAS_CONFIG);
        }

        StringBuilder modifySql = flinkSqlBuilder(ddl, options);

        return modifySql.toString();
    }

    public  KafkaConsumer<String, String> initConsumer(ConnectInfo connectInfo,String groupId){
        return  initConsumer(connectInfo,groupId,3);
    }

    public  KafkaConsumer<String, String> initConsumer(ConnectInfo connectInfo,String groupId,Integer pollSize){
        Map<String, Object> consumerConfig = assembleMap(connectInfo,null);
        if(StrUtil.isBlank(groupId)){
            Object o = consumerConfig.get(SaslConfigs.SASL_MECHANISM);
            if(Objects.isNull(o)){
                 groupId = GROUP_ID;
            }else {
                //SCRAM-SHA-256
                groupId = consumerSaslKafkaMetaGroupId;
            }
        }
        consumerConfig.put("group.id", groupId);
        // 关闭自动commit
        consumerConfig.put("enable.auto.commit", false);
        consumerConfig.put("auto.commit.interval.ms", "1000");
        consumerConfig.put("session.timeout.ms", "30000");
        consumerConfig.put("fetch.min.bytes", "1048576");
        consumerConfig.put("fetch.max.wait.ms", "1000");
        consumerConfig.put("max.partition.fetch.bytes", "8388608");
        consumerConfig.put("max.poll.records", pollSize.toString());
        consumerConfig.put("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerConfig.put("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        consumerConfig.put("client.id", StrUtil.subAfter(groupId,"-",false));
        return  new KafkaConsumer<String, String>(consumerConfig);
    }


    /**
     * 获取待消费数
     * @param connectInfo 连接参数
     * @param startupMode 消费模式：latest -> return 0 ; earliest -> logSize总数 ; timestamp -> logSize总数 - 按时间戳重置后的offset
     * @param groupId
     * @param mTopic 可能是正则表达式
     * @param timestamp
     * @return
     * @throws Exception
     */
    public  Long waitConsumedNum(ConnectInfo connectInfo,String startupMode, String groupId,String mTopic,Long timestamp) throws Exception {
        if (StrUtil.isBlank(startupMode) || startupMode.equals(FlinkConfigKeyConstant.LATEST_OFFSET)) {
            return CommonConstant.ZERO_FOR_LONG;
        }
        if(StrUtil.isBlank(groupId)){
            throw new IllegalAccessException("groupId不能为空");
        }
        if(StrUtil.isBlank(mTopic)){
            throw new IllegalAccessException("topic不能为空");
        }
        if (startupMode.equals(FlinkConfigKeyConstant.TIMESTAMP) && Objects.isNull(timestamp)) {
            throw new IllegalAccessException("没有指定timestamp时间戳参数");
        }

        //兼容topic-pattern的情况
        List<String> allTopic = getTables(connectInfo);
        List<String> topics = Lists.newArrayList();
        //兼容topic=topic1;topic2这种情况
        List<String> mTopics = Splitter.on(";").splitToList(mTopic);

        for (String topic : allTopic) {
            for (String xTopic : mTopics) {
                if (ReUtil.isMatch(Pattern.compile(xTopic),topic)) {
                    topics.add(topic);
                }
            }
        }

        if(CollectionUtil.isEmpty(topics)){
            log.info("没有匹配到topic");
            return CommonConstant.ZERO_FOR_LONG;
        }

        Long waitConsumedSum = CommonConstant.ZERO_FOR_LONG;

        try (final KafkaConsumer<String, String> consumer = initConsumer(connectInfo, null)) {
            for (String topic : topics) {
                Map<Integer, Long> endOffsetsMap = new HashMap<>();
                Map<Integer, Long> resetOffsetsMap = new HashMap<>();

                List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);
                List<TopicPartition> topicPartitions = partitionInfos.stream().map(partitionInfo -> new TopicPartition(topic, partitionInfo.partition())).collect(Collectors.toList());

                //获取logSize
                Map<TopicPartition, Long> endOffsets = consumer.endOffsets(topicPartitions);
                for (Map.Entry<TopicPartition, Long> endOffset : endOffsets.entrySet()) {
                    endOffsetsMap.put(endOffset.getKey().partition(), endOffset.getValue());
                }
                for (Integer partition : endOffsetsMap.keySet()) {
                    Long endOffset = Optional.ofNullable(endOffsetsMap.get(partition)).orElse(CommonConstant.ZERO_FOR_LONG);
                    waitConsumedSum += endOffset;
                }

                if (startupMode.equals(FlinkConfigKeyConstant.TIMESTAMP)) {
                    //指定时间戳 -> offset转换
                    Map<TopicPartition, Long> topicTimetampMap = topicPartitions.stream().collect(Collectors.toMap(Function.identity(), topicPartition -> timestamp));
                    Set<Map.Entry<TopicPartition, OffsetAndTimestamp>> entries = consumer.offsetsForTimes(topicTimetampMap).entrySet();
                    for (Map.Entry<TopicPartition, OffsetAndTimestamp> entry : entries) {
                        TopicPartition key = entry.getKey();
                        OffsetAndTimestamp value = entry.getValue();

                        long offset;
                        if (Objects.nonNull(value)) {
                            offset = value.offset();
                        } else {
                            //当指定时间戳大于分区分区最新数据时间戳时，为null
                            consumer.assign(Collections.singleton(key));
                            consumer.seekToEnd(Collections.singleton(key));
                            offset = consumer.position(key);
                        }
                        resetOffsetsMap.put(key.partition(), offset);
                    }

                    //待消费数 = 总数 - 按时间戳重置之后的offset
                    for (Integer partition : endOffsetsMap.keySet()) {
                        Long resetOffset = Optional.ofNullable(resetOffsetsMap.get(partition)).orElse(CommonConstant.ZERO_FOR_LONG);
                        waitConsumedSum -= resetOffset;
                    }
                }else if(startupMode.equals(FlinkConfigKeyConstant.EARLIEST_OFFSET)){
                    Map<TopicPartition, Long> topicPartitionLongMap = consumer.beginningOffsets(topicPartitions);
                    for (Map.Entry<TopicPartition, Long> entry : topicPartitionLongMap.entrySet()) {
                        TopicPartition key = entry.getKey();
                        Long eOffset = entry.getValue();
                        resetOffsetsMap.put(key.partition(), Objects.isNull(eOffset)?CommonConstant.ZERO_FOR_LONG:eOffset);
                    }

                    //待消费数 = 总数 - 最早的offset
                    for (Integer partition : endOffsetsMap.keySet()) {
                        Long resetOffset = Optional.ofNullable(resetOffsetsMap.get(partition)).orElse(CommonConstant.ZERO_FOR_LONG);
                        waitConsumedSum -= resetOffset;
                    }
                }else if(startupMode.equals(FlinkConfigKeyConstant.GROUP_OFFSETS)){
                    Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetAndMetadataMap = listConsumerGroupOffsets(connectInfo,groupId);
                    if(topicPartitionOffsetAndMetadataMap.size() == 0){
                        Map<TopicPartition, Long> topicPartitionLongMap = consumer.beginningOffsets(topicPartitions);
                        for (Map.Entry<TopicPartition, Long> entry : topicPartitionLongMap.entrySet()) {
                            TopicPartition key = entry.getKey();
                            Long eOffset = entry.getValue();
                            resetOffsetsMap.put(key.partition(), Objects.isNull(eOffset)?CommonConstant.ZERO_FOR_LONG:eOffset);
                        }
                    }else {
                        for (Map.Entry<TopicPartition, OffsetAndMetadata> entry : topicPartitionOffsetAndMetadataMap.entrySet()) {
                            TopicPartition key = entry.getKey();
                            if(key.topic().equals(topic)){
                                Long eOffset = entry.getValue().offset();
                                resetOffsetsMap.put(key.partition(), Objects.isNull(eOffset)?CommonConstant.ZERO_FOR_LONG:eOffset);
                            }
                        }
                        //当partiton数不一致的时候，需要将当前groupId对应的差额部分partiton给默认值0
                        if(topicPartitionOffsetAndMetadataMap.size() != topicPartitions.size()){
                            Set<Integer> allPartitions = topicPartitions.stream().map(x->x.partition()).collect(Collectors.toSet());
                            Set<Integer> currGroupIdParts = topicPartitionOffsetAndMetadataMap.keySet().stream().map(x->x.partition()).collect(Collectors.toSet());
                            Set<Integer> difference = Sets.difference(allPartitions,currGroupIdParts);
                            log.info("分区不同===="+difference.size());
                            difference.forEach(x->resetOffsetsMap.put(x, CommonConstant.ZERO_FOR_LONG));
                        }
                    }
                    //待消费数 = 总数 - 最早的offset
                    for (Integer partition : endOffsetsMap.keySet()) {
                        Long resetOffset = Optional.ofNullable(resetOffsetsMap.get(partition)).orElse(CommonConstant.ZERO_FOR_LONG);
                        waitConsumedSum -= resetOffset;
                    }
                }
            }
        } catch (Exception e) {
            log.error("获取待消费数异常",e);
            throw new ApplicationException(ResponseCode.COMMON_ERR,"获取待消费数异常");
        }
        return waitConsumedSum;
    }

    public Map<TopicPartition, OffsetAndMetadata> listConsumerGroupOffsets(ConnectInfo connectInfo,String groupId) throws Exception {
        Map<TopicPartition, OffsetAndMetadata> topicPartitionOffsetAndMetadataMap;
        AdminClient adminClient = null;
        try {
            adminClient = getConnection(connectInfo);
            topicPartitionOffsetAndMetadataMap = adminClient.listConsumerGroupOffsets(groupId).partitionsToOffsetAndMetadata().get();
        } finally {
            closeConnection(adminClient);
        }
        return topicPartitionOffsetAndMetadataMap;
    }

    /**
     * 获取消费位置时间戳
     * @param connectInfo kafka连接参数
     * @param groupId 消费组
     * @param mTopics topic集合
     * @param topicPartitionOffsetMap topic 分区 offset
     * @return topic->timestamp
     */
    public Map<String,Long> partitionTimestampMap(ConnectInfo connectInfo,String groupId, List<String> mTopics, Map<String,Map<Integer, Long>> topicPartitionOffsetMap,String msgPrefix){
        msgPrefix = StrUtil.isNotBlank(msgPrefix)?msgPrefix:"===partitionTimestampMap: ";
        if(Objects.isNull(connectInfo) || CollectionUtil.isEmpty(mTopics)){
            if(log.isTraceEnabled()){
                log.trace(msgPrefix+" PTM -> connectInfo: {}" + " , mTopics:{}",Objects.isNull(connectInfo),CollectionUtil.isEmpty(mTopics));
            }
            return  Collections.emptyMap();
        }
        if(StrUtil.isBlank(groupId)){
            //和获取字段使用同一个组，避免出现授权异常
            groupId = GROUP_ID;
        }

        Map<String,Long> map = new HashMap<>();
        KafkaConsumer<String, String> consumer = initConsumer(connectInfo, null,1);
        try {
            for (String topic : mTopics) {
                if(StrUtil.isBlank(topic)){
                    if(log.isTraceEnabled()){
                        log.trace(msgPrefix+" PTM -> topic为空");
                    }
                    continue;
                }
                Map<Integer, Long> patitionOffsetMap = topicPartitionOffsetMap.get(topic);
                if(CollectionUtils.isEmpty(patitionOffsetMap)){
                    if(log.isTraceEnabled()){
                        log.trace(msgPrefix+" PTM -> topic: {}, patitionOffsetMap为空",topic);
                    }

                    //有中杠的主题，prometheus上面的是下划线
                    patitionOffsetMap = topicPartitionOffsetMap.get(topic.replace("-","_"));
                    if(CollectionUtils.isEmpty(patitionOffsetMap)) {
                        if(log.isTraceEnabled()){
                            log.trace(msgPrefix+" PTM中杠转下划线 -> topic: {}, patitionOffsetMap为空",topic);
                        }
                        continue;
                    }
                }

                List<PartitionInfo> partitionInfos = consumer.partitionsFor(topic);
                List<TopicPartition> topicPartitions = partitionInfos.stream().map(partitionInfo -> new TopicPartition(topic, partitionInfo.partition())).collect(Collectors.toList());

                label1: for (TopicPartition tp : topicPartitions) {
                    Long offset = patitionOffsetMap.get(Integer.valueOf(tp.partition()));
                    if (Objects.isNull(offset) || offset < 0){
                        if(log.isTraceEnabled()){
                            log.trace(msgPrefix+" PTM -> topic:{},patition:{}, offset: {}" ,tp.topic(),tp.partition(),Objects.isNull(offset)?"null":offset);
                        }
                        continue;
                    }

                    consumer.assign(Arrays.asList(tp));
                    //必须使用这个方法 不能使用 poll(Duration.ofSecond(0))
                    //consumer.poll(0);
                    Set<TopicPartition> assignment = new HashSet<>();
                    int cnt = 0;
                    while (assignment.size() == 0 && cnt < 15) {
                        consumer.poll(100);
                        assignment = consumer.assignment();
                        ++cnt;
                        if(cnt == 14){
                            if(log.isTraceEnabled()){
                                log.trace(msgPrefix+" PTM -> topic:{},patition:{}, assignment: {}次" ,tp.topic(),tp.partition(),cnt);
                            }
                        }
                    }
                    for (TopicPartition topicPartition : assignment) {
                        try {
                            //有可能是最后一条，所以要减一，获取前一条的时间戳
                            long xoffset = offset > 0 ? offset - 1 : offset;
                            consumer.seek(topicPartition,xoffset);
                            consumer.assign(Arrays.asList(topicPartition));
                            //设置太小可能获取不到返回数据，目前设置为2s
                            ConsumerRecords<String, String> poll = consumer.poll(2000);
                            if(log.isTraceEnabled()){
                                log.trace(msgPrefix+" PTM -> topic: {}, patition: {}, seek: {}, poll: {}" ,topicPartition.topic(),topicPartition.partition(),xoffset,CollectionUtil.isNotEmpty(poll));
                            }
                            for (ConsumerRecord<String, String> record : poll) {
                                //获取最小的时间戳(时间戳可能返回-1的情况)
                                long timestamp = record.timestamp();
                                Long ts = map.get(topic);
                                if(log.isTraceEnabled()){
                                    log.trace(msgPrefix+" PTM -> topic: {}, patition: {}, timestamp: {}" ,topicPartition.topic(),topicPartition.partition(),timestamp);
                                }
                                if((Objects.isNull(ts) || timestamp > ts)  && timestamp > 1000){
                                    map.put(topic,timestamp);
                                }
                                break label1;
                            }
                        } catch (Exception e) {
                            String errMsg = StrUtils.parse1(msgPrefix + " PTM -> topic: {}, patition: {} 异常", topicPartition.topic(), topicPartition.partition());
                            log.warn(errMsg,e);
                        }
                    }
                }
            }
        }catch (Exception e){
            throw e;
        }finally {
            if(Objects.nonNull(consumer)){
                consumer.close();
            }
        }
        return map;
    }

}
