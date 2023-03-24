package com.chitu.bigdata.sdp.utils;

import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import redis.clients.jedis.Jedis;

/**
 * @author chenyun
 * @description: TODO
 * @date 2021/11/1 18:55
 */
@Slf4j
@Component
public class RedisLocker {

    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    public static final String VALUE = "1";

    /**
     * 释放锁
     * @param lockKey
     * @return
     */
    public boolean unlock(String lockKey) {
        Jedis jedis = null;
        Long delResult = null;
        try{
            jedis = getJedis();
            delResult = jedis.del(lockKey);
        }catch(Exception e){
            log.error("释放锁异常==="+e);
            throw new ApplicationException(ResponseCode.CLOSE_LOCK_ERROR);
        }finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return delResult != null && delResult > 0;
    }


    /**
     * 尝试加锁
     * @param lockKey redis key
     * @param waitTime 等待锁的时间, 单位毫秒
     * @param leaseTime 锁持有时间, 单位毫秒
     * @return 是否加锁成功
     */
    public boolean tryLock(String lockKey, long waitTime, long leaseTime) {
        Jedis jedis = null;
        boolean success = false;
        try{
            jedis = getJedis();
            long startTime = System.currentTimeMillis();
            String result = jedis.set(lockKey, VALUE, "NX", "PX", leaseTime);
            //log.info("获取锁：{} -> {}",lockKey,result);
            success = isOk(result);
            if(!success) {
                do {
                    try {
                        Thread.sleep(60L);
                    } catch (Exception ignore) {}
                    result = jedis.set(lockKey, VALUE, "NX", "PX", leaseTime);
                    //log.info("获取锁1：{} -> {}",lockKey,result);
                    success = isOk(result);
                } while (!success && (System.currentTimeMillis() < startTime + waitTime));
            }
        }catch(Exception e){
            log.error("加锁异常==="+e);
            throw new ApplicationException(ResponseCode.GET_LOCK_ERROR);
        }finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return success;
    }

    /**
     * 根据key查询是否被加锁
     * @param lockKey redis key
     * @return
     */
    public boolean isLocked(String lockKey) {
        Jedis jedis = null;
        boolean result = false;
        try{
            jedis = getJedis();
            result = VALUE.equals(jedis.get(lockKey));
        }catch(Exception e){
            log.error("判断是否加锁异常==="+e);
            throw new ApplicationException(ResponseCode.IS_LOCK_ERROR);
        }finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return result;
    }

    public Jedis getJedis() {
        //由于redis客户端是个单例模式，发布订阅频道之后就不允许其它的操作（除(P)SUBSCRIBE / (P)UNSUBSCRIBE / PING / QUIT之外）
        //redis客户端在进入subscribe模式以后，将不能再响应其他的任何命令
        //因此，将Jedis实例获取方式改为JedisPool
        JedisPoolClient jedisPoolClient = SpringUtils.getBean(JedisPoolClient.class);
        return jedisPoolClient.getJedisPool().getResource();

//        if(stringRedisTemplate.getConnectionFactory() == null) {
//            throw new ApplicationException(ResponseCode.ERROR, "获取Redis ConnectionFactory失败");
//        }
//        RedisConnection connection = stringRedisTemplate.getConnectionFactory().getConnection();
//        return (Jedis)connection.getNativeConnection();
    }

    public void returnJedis(Jedis jedis){
        if (jedis != null) {
            JedisPoolClient jedisPoolClient = SpringUtils.getBean(JedisPoolClient.class);
            jedisPoolClient.getJedisPool().returnResource(jedis);
        }

    }

    private boolean isOk(String result) {
        return "OK".equalsIgnoreCase(result);
    }

}
