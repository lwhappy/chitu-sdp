package com.chitu.cloud.config.scheduler;

import com.xiaoleilu.hutool.util.StrUtil;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.commons.util.InetUtils;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.PropertySource;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import redis.clients.jedis.Jedis;

import java.net.InetAddress;

/**
 * @Author liubin
 * @Date 2021/1/11 9:47
 **/
@Slf4j
public class RedisLeaderElection implements InitializingBean, DisposableBean {

    public static final String REDIS_PREFIX_KEY = "BD_LEADER";
    public static final int REDIS_KEY_TIMEOUT_SECONDS = 60;
    public static final int THREAD_RUN_INTERVAL_SECONDS = 10;

    private static final Logger logger = LoggerFactory.getLogger(RedisLeaderElection.class);

    @Autowired(required = false)
    private StringRedisTemplate stringRedisTemplate;

    @Value("${spring.application.name:}")
    private String serviceName;

    @Value("${tracer.env:default}")
    private String env;

    @Value("${server.port:}")
    private String port;

    @Autowired(required = false)
    ConfigurableEnvironment configurableEnvironment;

    @Autowired(required = false)
    private InetUtils inetUtils;

    private volatile boolean stop = false;

    private volatile boolean leader = false;

    private final ElectionThread electionThread = new ElectionThread();

    private final String leaderName;
    private String currentServiceRegisterInfo;
    private String key;

    public RedisLeaderElection(String leaderName) {
        this.leaderName = leaderName;
    }

    @Override
    public void afterPropertiesSet() {
        this.key = String.format("%s_%s_%s_%s", REDIS_PREFIX_KEY, leaderName, serviceName, env);
        this.currentServiceRegisterInfo = getCurrentServiceRegisterInfo();
        log.info("redis开始进行主从选举, redis key: {}, value: {}", key, currentServiceRegisterInfo);
        electionThread.start();
        Runtime.getRuntime().addShutdownHook(new Thread(() -> destroy()));
    }

    @Override
    public void destroy() {
        if (stop) {
            return;
        }
        stop = true;
        electionThread.releaseLeader();
        leader = false;
        logger.info("shutdown and give up leadership");
    }

    public boolean isLeader() {
        return leader;
    }

    /**
     * 获取当前服务的注册IP与端口号
     *
     * @return 当前服务注册的IP与端口号
     */
    private String getCurrentServiceRegisterInfo() {

        String ip = null;
        try {
            if (configurableEnvironment != null) {
                PropertySource<?> springCloudClientHostInfo = configurableEnvironment
                        .getPropertySources()
                        .get("springCloudClientHostInfo");
                ip = (String) springCloudClientHostInfo
                        .getProperty("spring.cloud.client.ipAddress");
            }
        } catch (Exception e) {
            log.warn("通过spring.cloud.client.ipAddress获取ip失败: " + e.getMessage());
        }
        try {
            //当无法从环境变量中获取IP时，从网卡信息（NetworkInterface）再获取一次
            if (StrUtil.isEmpty(ip)) {
                InetAddress inetAddress = inetUtils.findFirstNonLoopbackAddress();
                if (inetAddress != null) {
                    ip = inetAddress.getHostAddress();
                }
            }
        } catch (Exception e) {
            log.warn("getCurrentServiceRegisterInfo获取ip失败: " + e.getMessage());
        }
        return ip == null ? "" : ip + ":" + port;
    }

    class ElectionThread extends Thread {

        public ElectionThread() {
            setName("leader-election");
        }

        @Override
        public void run() {
            if(stringRedisTemplate == null || stringRedisTemplate.getConnectionFactory() == null) {
                log.info("未启用redis, RedisLeaderElection不会启动");
                return;
            }
            long count = 0L;
            while (!stop) {
                RedisConnection connection = null;
                try {
                    connection = stringRedisTemplate.getConnectionFactory().getConnection();
                    Object nativeConnection = connection.getNativeConnection();
                    if (nativeConnection instanceof Jedis) {
                        jedisSelectLeader((Jedis) nativeConnection, count);
                    } else {
                        log.error("unknown redis client type: " + nativeConnection.getClass());
                    }
                } catch (Throwable t) {
                    log.error("redis主从选举循环出现异常", t);
                } finally {
                    if(connection != null) {
                        try {
                            connection.close();
                        } catch (Throwable t) {
                            log.error("", t);
                        }
                    }
                }

                try {
                    Thread.sleep(THREAD_RUN_INTERVAL_SECONDS * 1000L);
                } catch (InterruptedException ignore) {
                }
                count ++;
            }
        }

        private void jedisSelectLeader(Jedis jedis, long count) {
            if (leader) {
                //renew
                String result = jedis.set(key, currentServiceRegisterInfo, "XX", "EX", REDIS_KEY_TIMEOUT_SECONDS);
                if (isOk(result)) {
                    if(count % 10 == 0) {
                        log.info("redis leader election renew ok, key: " + key);
                    }
                } else {
                    log.error("redis leader election renew fail, key: " + key);
                    leader = false;
                }
            } else {
                //try get lock
                String result = jedis.set(key, currentServiceRegisterInfo, "NX", "EX", REDIS_KEY_TIMEOUT_SECONDS);
                if (isOk(result)) {
                    log.info("redis leader select success, key: " + key);
                    leader = true;
                }
            }
        }

        private void jedisReleaseLeader(Jedis jedis) {
            log.info("redis leader release leadership!");
            String value = jedis.get(key);
            if(currentServiceRegisterInfo.equals(value)) {
                jedis.del(key);
            }
        }


        private boolean isOk(String result) {
            return "OK".equalsIgnoreCase(result);
        }

        public void releaseLeader() {
            if(!leader) {
                return;
            }

            RedisConnection connection = null;
            try {
                connection = stringRedisTemplate.getConnectionFactory().getConnection();
                Object nativeConnection = connection.getNativeConnection();
                if (nativeConnection instanceof Jedis) {
                    jedisReleaseLeader((Jedis) nativeConnection);
                } else {
                    log.error("unknown redis client type: " + nativeConnection.getClass());
                }
            } catch (Throwable t) {
                log.error("redis release leader出现异常", t);
            } finally {
                if(connection != null) {
                    try {
                        connection.close();
                    } catch (Throwable t) {
                        log.error("", t);
                    }
                }
            }

        }
    }




}