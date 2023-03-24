package com.chitu.bigdata.sdp.service.monitor;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.chitu.bigdata.sdp.api.bo.PromLableValueResult;
import com.chitu.bigdata.sdp.api.bo.PromResponceInfo;
import com.chitu.bigdata.sdp.api.bo.PromResultInfo;
import com.chitu.bigdata.sdp.api.domain.MetricTableValue;
import com.chitu.bigdata.sdp.api.domain.MetricTableValueInfo;
import com.chitu.bigdata.sdp.api.enums.MetaTableType;
import com.chitu.bigdata.sdp.config.PrometheusConfigProperties;
import com.chitu.bigdata.sdp.config.SdpConfig;
import com.chitu.bigdata.sdp.constant.PromConstant;
import com.chitu.bigdata.sdp.utils.DataMonitorUtils;
import com.xiaoleilu.hutool.util.CollectionUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author sutao
 * @create 2021-11-15 16:14
 * Prometheus查询服务
 */
@Slf4j
@Service
@RefreshScope
public class PrometheusQueryService {

    @Autowired
    private RestTemplate restTemplate;


    @Autowired
    PrometheusConfigProperties prometheusConfigProperties;
    @Autowired
    SdpConfig sdpConfig;

    private static final String PREFIXSTR_114_KAFKA = "database_";
    private static final String SUFFIXSTR_114_KAFKA = "____";

    private static final String SINK_KAFKA_MARK = "Sink_table";
    private static final String SINK_KAFKA_SPLIT_MARK = "___";

    private static final String PREFIXSTR_115_KAFKA = "Source:_";
    private static final String SUFFIXSTR_115_KAFKA = "_____";


    public List<String> getLableValues(String suffixUrl) {
        String env = sdpConfig.getEnvFromEnvHolder(log);
        String promURL = prometheusConfigProperties.getEnvMap().get(env).getBaseUrl() + suffixUrl;
        try {
            PromLableValueResult lableValueResult = restTemplate.getForObject(promURL, PromLableValueResult.class);
            if (PromConstant.SUCCESS.equals(lableValueResult.getStatus())) {
                return lableValueResult.getData();
            } else {
                log.warn(String.format("请求QL：%s 调用Prometheus接口返回异常, 返回结果：%s", promURL, JSONObject.toJSONString(lableValueResult)));
            }
        } catch (RestClientException e) {
            log.error(String.format("请求QL：%s 调用Prometheus接口异常", promURL));
        }
        return new ArrayList<>();
    }


    public PromResponceInfo metricsQuery(String promQL) {
        String env = sdpConfig.getEnvFromEnvHolder(log);
        String promURL = prometheusConfigProperties.getEnvMap().get(env).getQueryUrl() + "?query={promQL}";
        Map<String, String> map = new HashMap<>(3);
        map.put("promQL", promQL);
        PromResponceInfo responceInfo = null;
        try {
            String result = restTemplate.getForObject(promURL, String.class, map);
            responceInfo = JSON.parseObject(result, PromResponceInfo.class);
            if (log.isDebugEnabled()) {
                log.debug("请求QL：{}，返回信息：{}", promQL, JSONObject.toJSONString(responceInfo));
            }
        } catch (RestClientException e) {
            DataMonitorUtils.monitorError("监控告警规则调用Prometheus接口异常");
            log.error(String.format("请求QL：%s 调用Prometheus接口异常", promQL));
        }
        return responceInfo;
    }


    /**
     * 查询获取单个value值
     *
     * @param promQL
     * @return
     */
    public Long getMetricSingleValue(String promQL) {
        PromResponceInfo promResponceInfo = this.metricsQuery(promQL);
        if (promResponceInfo == null || !PromConstant.SUCCESS.equals(promResponceInfo.getStatus())) {
            log.error("prometheus接口查询数据，返回状态异常，[{}]", JSONObject.toJSONString(promResponceInfo));
            return null;
        }
        List<PromResultInfo> resultList = promResponceInfo.getData().getResult();
        if (CollectionUtil.isNotEmpty(resultList)) {
            String[] value = resultList.get(0).getValue();
            if (value != null && value.length > 0) {
                return Double.valueOf(value[1]).longValue();
            }
        }
        return null;
    }


    public Map<String, Long> getMetricTableValueSum(String promQL) {
        Map<String, Long> resultMap = new HashMap<>();
        PromResponceInfo promResponceInfo = this.metricsQuery(promQL);
        if (promResponceInfo == null || !PromConstant.SUCCESS.equals(promResponceInfo.getStatus())) {
            log.error("prometheus接口查询数据，返回状态异常，[{}]", JSONObject.toJSONString(promResponceInfo));
            return resultMap;
        }
        List<PromResultInfo> resultList = promResponceInfo.getData().getResult();
        if (CollectionUtil.isEmpty(resultList)) {
            return resultMap;
        }
        Map<String, List<MetricTableValue>> tableGroup = getTableGroup(resultList);
        tableGroup.forEach((k, v) -> {
            resultMap.put(k, v.stream().mapToLong(MetricTableValue::getValue).sum());
        });
        return resultMap;
    }


    /**
     * 获取表粒度value聚合值 根据指标的类型source/sink去获取
     *
     * @param promQL
     * @param type
     * @return
     */
    public Map<String, Long> getMetricTableValueSum(String promQL, String type) {
        Map<String, Long> resultMap = new HashMap<>();
        PromResponceInfo promResponceInfo = this.metricsQuery(promQL);
        if (promResponceInfo == null || !PromConstant.SUCCESS.equals(promResponceInfo.getStatus())) {
            log.error("prometheus接口查询数据，返回状态异常，[{}]", JSONObject.toJSONString(promResponceInfo));
            return resultMap;
        }
        List<PromResultInfo> resultList = promResponceInfo.getData().getResult();
        if (CollectionUtil.isEmpty(resultList)) {
            return resultMap;
        }
        Map<String, List<MetricTableValue>> tableGroup = getTableGroup(resultList, type);
        tableGroup.forEach((k, v) -> {
            resultMap.put(k, v.stream().mapToLong(MetricTableValue::getValue).sum());
        });
        return resultMap;
    }


    /**
     * 获取表粒度value最大值
     *
     * @param promQL
     * @return
     */
    public Map<String, Long> getMetricTableValueMax(String promQL) {
        Map<String, Long> resultMap = new HashMap<>();
        PromResponceInfo promResponceInfo = this.metricsQuery(promQL);
        if (promResponceInfo == null || !PromConstant.SUCCESS.equals(promResponceInfo.getStatus())) {
            log.error("prometheus接口查询数据，返回状态异常，[{}]", JSONObject.toJSONString(promResponceInfo));
            return resultMap;
        }
        List<PromResultInfo> resultList = promResponceInfo.getData().getResult();
        if (CollectionUtil.isEmpty(resultList)) {
            return resultMap;
        }
        Map<String, List<MetricTableValue>> tableGroup = getTableGroup(resultList);
        tableGroup.forEach((k, v) -> {
            Long maxValue = v.stream().map(MetricTableValue::getValue).max(Comparator.comparingLong(Long::longValue)).get();
            resultMap.put(k, maxValue);
        });
        return resultMap;
    }


    /**
     * 根据表名分组
     * taskName = prefixStr+tableName+suffixStr.从任务名称中截取flinkTableName
     *
     * @param resultList
     * @return
     */
    private Map<String, List<MetricTableValue>> getTableGroup(List<PromResultInfo> resultList) {
        List<MetricTableValue> tableResultList = resultList.stream().map(item -> {
            String taskName = item.getMetric().getTask_name();
            String prefixStr = PREFIXSTR_114_KAFKA;
            String suffixStr = SUFFIXSTR_114_KAFKA;
            String tableName;
            // 1.15.2版本截取方式
            if (!taskName.startsWith("Source:_KafkaSource_")) {
                prefixStr = PREFIXSTR_115_KAFKA;
                suffixStr = SUFFIXSTR_115_KAFKA;
                String opName = taskName.split(suffixStr)[0];
                tableName = opName.substring(opName.lastIndexOf(prefixStr) + prefixStr.length());
                tableName = tableName.substring(0, tableName.lastIndexOf("_"));
            } else {
                String opName = taskName.split(suffixStr)[0];
                tableName = opName.substring(opName.lastIndexOf(prefixStr) + prefixStr.length());
            }
            Long value = 0L;
            if (item.getValue() != null && item.getValue().length > 1) {
                value = Double.valueOf(item.getValue()[1]).longValue();
            }
            return new MetricTableValue(tableName, value);
        }).collect(Collectors.toList());
        return tableResultList.stream().collect(Collectors.groupingBy(MetricTableValue::getTableName));
    }


    /**
     * 根据表名分组，切分表名时要根据类型来切
     *
     * @param resultList
     * @param type
     * @return
     */
    private Map<String, List<MetricTableValue>> getTableGroup(List<PromResultInfo> resultList, String type) {
        List<MetricTableValue> tableResultList = null;
        if (MetaTableType.SOURCE.getType().equals(type)) {
            tableResultList = resultList.stream().map(item -> {
                String taskName = item.getMetric().getTask_name();
                String prefixStr = PREFIXSTR_114_KAFKA;
                String suffixStr = SUFFIXSTR_114_KAFKA;
                String tableName;
                // 1.15.2版本截取方式
                if (!taskName.startsWith("Source:_KafkaSource_")) {
                    prefixStr = PREFIXSTR_115_KAFKA;
                    suffixStr = SUFFIXSTR_115_KAFKA;
                    String opName = taskName.split(suffixStr)[0];
                    tableName = opName.substring(opName.lastIndexOf(prefixStr) + prefixStr.length());
                    tableName = tableName.substring(0, tableName.lastIndexOf("_"));
                } else {
                    String opName = taskName.split(suffixStr)[0];
                    tableName = opName.substring(opName.lastIndexOf(prefixStr) + prefixStr.length());
                }
                Long value = 0L;
                if (item.getValue() != null && item.getValue().length > 1) {
                    value = Double.valueOf(item.getValue()[1]).longValue();
                }
                return new MetricTableValue(tableName, value);
            }).collect(Collectors.toList());
        } else {
            tableResultList = resultList.stream().map(item -> {
                String taskName = item.getMetric().getTask_name();
                String tableName;
                // 新增1.15.2版本逻辑处理，暂时都用简单方式截取，后面考虑用正则来处理
                // Source:_KafkaSource_default_catalog_default_database_test_canal_t1____MiniBatchAssigner_interval__10000ms___mode__ProcTime______Sink_Sink_table__default_catalog_default_database_test_canal_t2___fields__log__",tm_id="container_e18_1659493967480_2207_01_000002
                // Source:_test_canal_t1_1_____MiniBatchAssigner_2_____test_canal_t2_3_:_Writer____test_canal_t2_3_:_Committer",tm_id="container_e18_1659493967480_2208_01_000003
                if (!taskName.startsWith("Source:_KafkaSource_") && taskName.contains("Writer____")) {
                    tableName = taskName.split("Writer____")[1].split("_:")[0];
                    tableName = tableName.substring(0, tableName.lastIndexOf("_"));
                }else {
                    String opName = taskName.split(SINK_KAFKA_MARK)[1].split(SINK_KAFKA_SPLIT_MARK)[0];
                    tableName = opName.substring(opName.lastIndexOf(PREFIXSTR_114_KAFKA) + PREFIXSTR_114_KAFKA.length());
                }
                Long value = 0L;
                if (item.getValue() != null && item.getValue().length > 1) {
                    value = Double.valueOf(item.getValue()[1]).longValue();
                }
                return new MetricTableValue(tableName, value);
            }).collect(Collectors.toList());
        }

        return tableResultList.stream().collect(Collectors.groupingBy(MetricTableValue::getTableName));

    }


    /**
     * 获取批量表粒度value聚合值
     *
     * @param promQL
     * @return
     */
    public List<MetricTableValueInfo> getJobsMetricValueSum(String promQL) {
        List<MetricTableValueInfo> results = new ArrayList<>();
        PromResponceInfo promResponceInfo = this.metricsQuery(promQL);
        if (promResponceInfo == null || !PromConstant.SUCCESS.equals(promResponceInfo.getStatus())) {
            log.error("prometheus接口查询数据，返回状态异常，[{}]", JSONObject.toJSONString(promResponceInfo));
            return results;
        }
        List<PromResultInfo> resultList = promResponceInfo.getData().getResult();
        if (CollectionUtil.isEmpty(resultList)) {
            return results;
        }
        Map<String, List<MetricTableValue>> tableGroup = getAppIdGroup(resultList);
        tableGroup.forEach((k, v) -> {
            Map<String, Long> longMap = new HashMap<>(100);
            for (MetricTableValue metricTableValue : v) {
                longMap.put(metricTableValue.getTableName(), metricTableValue.getValue());
            }
            MetricTableValueInfo metricTableValueInfo = new MetricTableValueInfo();
            metricTableValueInfo.setFlinkAppId(k);
            if (PromConstant.RECORDS_CONSUMED_TOTAL.equalsIgnoreCase(promQL)) {
                metricTableValueInfo.setConsumedTotalMap(longMap);
            } else if (PromConstant.DESERIALIZEFAILNUM.equalsIgnoreCase(promQL)) {
                metricTableValueInfo.setDeserializeFailNumMap(longMap);
            } else {
                metricTableValueInfo.setPendingRecordsMap(longMap);
            }
            results.add(metricTableValueInfo);
        });
        return results;
    }

    private Map<String, List<MetricTableValue>> getAppIdGroup(List<PromResultInfo> resultList) {
        HashMap<String, List<MetricTableValue>> jobIdMap = new HashMap<>(100);
        //将返回的数据按照flink的id进行分类
        Map<String, List<PromResultInfo>> appIdMap = resultList.stream().collect(Collectors.groupingBy(x -> x.getMetric().getJob_id()));
        //将分类好的数据处理转化成对应的map对象
        for (Map.Entry<String, List<PromResultInfo>> listEntry : appIdMap.entrySet()) {
            List<PromResultInfo> infos = listEntry.getValue();
            List<MetricTableValue> tableResultList = infos.stream().map(item -> {
                String taskName = item.getMetric().getTask_name();
                String opName = taskName.split("____")[0];
                String tableName = opName.substring(opName.lastIndexOf("database_") + "database_".length());
                Long value = 0L;
                if (item.getValue() != null && item.getValue().length > 1) {
                    value = Double.valueOf(item.getValue()[1]).longValue();
                }
                return new MetricTableValue(tableName, value);
            }).collect(Collectors.toList());
            //将同一flinkId下面的同一个table的数据进行分类聚合
            Map<String, List<MetricTableValue>> collects = tableResultList.stream().collect(Collectors.groupingBy(MetricTableValue::getTableName));
            ArrayList<MetricTableValue> metricTableValues = new ArrayList<>();
            for (Map.Entry<String, List<MetricTableValue>> entry : collects.entrySet()) {
                List<MetricTableValue> values = entry.getValue();
                long sum = values.stream().mapToLong(MetricTableValue::getValue).sum();
                MetricTableValue metricTableValue = new MetricTableValue(entry.getKey(), sum);
                metricTableValues.add(metricTableValue);

            }
            jobIdMap.put(listEntry.getKey(), metricTableValues);
        }
        return jobIdMap;
    }

    /**
     * 查询获取指标值
     *
     * @param promQL
     * @return
     */
    public List<PromResultInfo> getMetrics(String promQL) {
        PromResponceInfo promResponceInfo = this.metricsQuery(promQL);
        if (promResponceInfo == null || !PromConstant.SUCCESS.equals(promResponceInfo.getStatus())) {
            log.error("prometheus接口查询数据，返回状态异常，[{}]", JSONObject.toJSONString(promResponceInfo));
            return null;
        }
        return Optional.ofNullable(promResponceInfo).map(m -> m.getData()).map(m -> m.getResult()).orElse(new ArrayList<>());
    }

}
