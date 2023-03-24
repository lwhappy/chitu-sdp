package com.chitu.bigdata.sdp.utils;

import com.chitu.bigdata.sdp.config.JedisPoolConfigProperties;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * @author chenyun
 * @description: TODO
 * @date 2022/3/23 10:57
 */
@Component
public class JedisPoolClient {
    @Autowired
    private JedisPoolConfigProperties poolConfig;

    private static JedisPool pool = null;
    /**
     * 构建redis连接池
     */
    public JedisPool getJedisPool() {
        if(pool == null) {
            JedisPoolConfig config = new JedisPoolConfig();
            //控制一个pool可分配多少个jedis实例，通过pool.getResource()来获取；
            //如果赋值为-1，则表示不限制；如果pool已经分配了maxActive个jedis实例，则此时pool的状态为exhausted(耗尽)。
            config.setMaxTotal(poolConfig.getPool().getMaxActive());
            //控制一个pool最多有多少个状态为idle(空闲的)的jedis实例。
            config.setMaxIdle(poolConfig.getPool().getMaxIdle());
            //表示当borrow(引入)一个jedis实例时，最大的等待时间，如果超过等待时间，则直接抛出JedisConnectionException；单位毫秒，底层默认不限制
            config.setMaxWaitMillis(1000*100);
            //在borrow一个jedis实例时，是否提前进行validate操作；如果为true，则得到的jedis实例均是可用的；
            //因为testWhileIdle设置为true，所以在空闲的时候会触发pool中失效的连接的释放
            config.setTestOnBorrow(false);
            //开启空闲连接检测,当达到minEvictableIdleTimeMillis(默认30分钟)阈值时，空闲连接就会被移除
            config.setTestWhileIdle(true);
            //每次检测时，取多少个连接进行检测。如果设置成-1，就表示检测所有链接。
            config.setNumTestsPerEvictionRun(1000);

            pool = new JedisPool(config, poolConfig.getHost(), poolConfig.getPort(),  poolConfig.getTimeout(), poolConfig.getPassword(),poolConfig.getDatabase());
        }
        return pool;
    }

}
