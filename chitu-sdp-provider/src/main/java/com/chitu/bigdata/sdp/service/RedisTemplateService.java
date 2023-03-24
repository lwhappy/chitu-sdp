package com.chitu.bigdata.sdp.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.xiaoleilu.hutool.date.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * @author zouchangzhen
 * @date 2022/9/2
 */
@Slf4j
@Component
public class RedisTemplateService {
    @Autowired
    private RedisTemplate<String, String> redisTmplate;

    public final static String HADOOP_CONF_DIR = "HADOOP_CONF_DIR";
    public final static String SDP_JOB_INSTANCE_LIST_CACHE = "SDP_JOB_INSTANCE_LIST_CACHE";

    private final static String OBJ_CACHE = "bigdata:sdp:%s:%s:%s";

    /**
     * 获取缓存信息
     * @param cacheType 缓存类型
     * @param uniqueKey 唯一键
     * @param expire 过期时间
     * @param timeUnit 过期单位
     * @param supplier 缓存信息提供方法
     * @param <R>
     * @return
     */
    public   <R> String getJSONString(String cacheType,Object uniqueKey,long expire, TimeUnit timeUnit ,Supplier<R> supplier){
        ValueOperations valueOperations = redisTmplate.opsForValue();

        String redisKey = String.format(OBJ_CACHE,cacheType,DateUtil.format(new Date(), "yyyyMMdd"),String.valueOf(uniqueKey));
        Object valObj = valueOperations.get(redisKey);
        if(Objects.nonNull(valObj)){
            //缓存的数据
            if(log.isTraceEnabled()){
                log.trace("get缓存: {} -> {}",redisKey,valObj);
            }
            return valObj.toString();
        }else {
            //查库数据
            R apply = supplier.get();
            if(Objects.nonNull(apply)){
                //设置缓存
                String json = JSON.toJSONString(apply);
                valueOperations.set(redisKey,json ,expire,timeUnit);
                return json;
            }else {
                return null;
            }
        }
    }

    public  <R> void set(String cacheType,Object uniqueKey,long expire, TimeUnit timeUnit,Supplier<R> supplier){
        ValueOperations valueOperations = redisTmplate.opsForValue();

        String redisKey = String.format(OBJ_CACHE,cacheType,DateUtil.format(new Date(), "yyyyMMdd"),String.valueOf(uniqueKey));

        R apply = supplier.get();
        if(Objects.nonNull(apply)){
            //设置缓存
            String json = JSON.toJSONString(apply);
            valueOperations.set(redisKey,json ,expire,timeUnit);
            if(log.isTraceEnabled()){
                log.trace("set缓存: {} -> {}",redisKey,json);
            }
        }
    }

    public  boolean delete(String cacheType,Object uniqueKey){
        String redisKey = String.format(OBJ_CACHE,cacheType,DateUtil.format(new Date(), "yyyyMMdd"),String.valueOf(uniqueKey));
        Boolean delete = redisTmplate.delete(redisKey);
        if(log.isTraceEnabled()){
            log.trace("del缓存: {} -> {}",redisKey,delete);
        }
        return delete;
    }

    public   <R> R getObject(String cacheType,Object uniqueKey,long expire, TimeUnit timeUnit ,Supplier<R> supplier,Class<R> clazz){
        String jsonString = getJSONString(cacheType, uniqueKey, expire, timeUnit, supplier);
        if(StrUtil.isBlank(jsonString)){
            return null;
        }
        return JSON.parseObject(jsonString,clazz);
    }

    public   <R> List<R> getList(String cacheType, Object uniqueKey, long expire, TimeUnit timeUnit , Supplier<List<R>> supplier, Class<R> clazz){
        String jsonString = getJSONString(cacheType, uniqueKey, expire, timeUnit, supplier);
        if(StrUtil.isBlank(jsonString)){
            return null;
        }
        return JSON.parseArray(jsonString,clazz);
    }

    public Long countWithTtl(String cacheType,Object uniqueKey,long expire, TimeUnit timeUnit){
        ValueOperations valueOperations = redisTmplate.opsForValue();
        String redisKey = String.format(OBJ_CACHE,cacheType,DateUtil.format(new Date(), "yyyyMMdd"),String.valueOf(uniqueKey));
        Object valObj = valueOperations.get(redisKey);
        Long count = Optional.ofNullable(valObj).map(m -> Long.valueOf(m.toString())).orElse(0L) + 1;
        valueOperations.set(redisKey,count.toString(),expire, timeUnit);
        return count;
    }


}