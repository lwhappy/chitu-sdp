package com.chitu.bigdata.sdp.utils;


import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.chitu.bigdata.sdp.flink.common.util.DateUtils;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.text.SimpleDateFormat;

public class JsonUtils {

    private static final ObjectMapper MAPPER;

    static {
        MAPPER = new ObjectMapper();
        MAPPER.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        MAPPER.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        MAPPER.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        MAPPER.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        MAPPER.setDateFormat(new SimpleDateFormat(DateUtils.fullFormat()));
    }

    public static <T> T read(String json, Class<T> clazz) throws Exception {
        return MAPPER.readValue(json, clazz);
    }

    public static <T> T read(String json, TypeReference<T> typeReference) throws Exception {
        return MAPPER.readValue(json, typeReference);
    }

    /**
     * 判断一个JSON字符串是否是canal-json
     * 通过关键字来判断
     * @param jsonStr
     * @return3.
     */
    public static boolean isCanalJson(String jsonStr){
        if(StrUtil.isBlank(jsonStr)){
            return false;
        }

        //canal-json 关键字：isDdl es ts database table data
        return  JSONUtil.isJson(jsonStr)
                &&  jsonStr.contains("isDdl")
                &&  jsonStr.contains("es")
                &&  jsonStr.contains("ts")
                &&  jsonStr.contains("database")
                &&  jsonStr.contains("table")
                &&  jsonStr.contains("data");
    }

}
