package com.chitu.bigdata.sdp.utils;


import com.alibaba.fastjson.JSONObject;
import com.chitu.bigdata.sdp.api.domain.JobConfig;
import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.bigdata.sdp.constant.BusinessFlag;
import com.chitu.bigdata.sdp.flink.common.util.DeflaterUtils;
import com.chitu.bigdata.sdp.flink.common.util.PropertiesUtils;
import com.chitu.bigdata.sdp.service.validate.domain.SqlExplainResult;
import com.chitu.cloud.exception.ApplicationException;
import com.google.common.collect.Maps;
import com.xiaoleilu.hutool.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.util.CollectionUtils;
import scala.collection.JavaConversions;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 587694
 * @description: TODO
 * @date 2021/11/30 11:06
 */
@Slf4j
public class YamlUtils {

    public static void parseYaml(JSONObject options, JobConfig jobConfig,String flag,List<SqlExplainResult> data) {
        String flinkYaml = jobConfig.getFlinkYaml();
        if(!StringUtils.isEmpty(flinkYaml)){
            if(StringUtils.isNotBlank(flag) && flag.equals(BusinessFlag.SDP.toString())){
                flinkYaml = DeflaterUtils.unzipString(flinkYaml);
            }
            if (StringUtils.isEmpty(flinkYaml)){
                if(null != data){
                    SqlExplainResult explainResult = new SqlExplainResult();
                    explainResult.setError(ResponseCode.YAML_CONF_EMPTY.getMessage());
                    data.add(explainResult);
                }else{
                    throw new ApplicationException(ResponseCode.YAML_CONF_EMPTY);
                }
            }
            Map<String,String> yamlMap = null;
            try {
                scala.collection.immutable.Map<String, String> yamlText = PropertiesUtils.fromYamlText(flinkYaml);
                yamlMap = JavaConversions.mapAsJavaMap(yamlText);
            }catch (Exception e){
                log.error("Yaml解析异常==={}",e);
                if(null != data){
                    List<SqlExplainResult> exists = data.stream().filter(x->x.getError().equals(ResponseCode.YAML_CONFIG_ERROR.getMessage())).collect(Collectors.toList());
                    if(CollectionUtils.isEmpty(exists)){
                        SqlExplainResult explainResult = new SqlExplainResult();
                        explainResult.setError(ResponseCode.YAML_CONFIG_ERROR.getMessage());
                        data.add(explainResult);
                    }
                    return;
                }else{
                    throw new ApplicationException(ResponseCode.YAML_CONFIG_ERROR);
                }
            }
            Set entrySet = yamlMap.entrySet();
            Iterator<Map.Entry<String,String>> iterator = entrySet.iterator();
            while(iterator.hasNext()){
                Map.Entry<String,String> next = iterator.next();
                String value = next.getValue();
                if(!com.chitu.cloud.utils.StringUtils.isEmpty(value)){
                    options.put(next.getKey(),value);
                }
            }
        }
    }

    public static Map<String,String>  parseYaml(String flinkYaml) {
        if(StrUtil.isBlank(flinkYaml)){
            return Collections.EMPTY_MAP;
        }

        if(StrUtils.isBase64(flinkYaml)){
            flinkYaml = DeflaterUtils.unzipString(flinkYaml);
            if (StringUtils.isEmpty(flinkYaml)) {
                throw new ApplicationException(ResponseCode.YAML_CONF_EMPTY);
            }
            return  fromYamlText(flinkYaml);
        }else {
            return  fromYamlText(flinkYaml);
        }
    }

    public static Map<String ,String > fromYamlText(String flinkYaml){
        if(StrUtil.isBlank(flinkYaml)){
            return  Collections.EMPTY_MAP;
        }

        Map<String,String> yamlMap = Maps.newHashMap();
        Map<String, String> mYamlMap = null;
        try {
            scala.collection.immutable.Map<String, String> yamlText = PropertiesUtils.fromYamlText(flinkYaml);
            mYamlMap = JavaConversions.mapAsJavaMap(yamlText);
        } catch (Exception e) {
            log.error("Yaml解析异常==={}", e);
            throw new ApplicationException(ResponseCode.YAML_CONFIG_ERROR);
        }

        if (!CollectionUtils.isEmpty(mYamlMap)) {
            yamlMap.putAll(mYamlMap);
        }
        return  yamlMap;
    }

}
