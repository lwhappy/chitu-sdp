package com.chitu.bigdata.sdp.config;

import com.alibaba.ttl.threadpool.TtlExecutors;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author sutao
 * @create 2021-11-02 17:28
 */
@Configuration
public class ThreadPoolConfig {

    @Bean
    public Executor jobSyncExecutor4Uat(){
        return TtlExecutors.getTtlExecutor(new ThreadPoolExecutor(20,
                40,
                10,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(3000),
                new CustomizableThreadFactory("uat-job-sync-pool-"),
                new ThreadPoolExecutor.DiscardOldestPolicy()));
    }

    @Bean
    public Executor jobSyncExecutor4Prod(){
        return TtlExecutors.getTtlExecutor(new ThreadPoolExecutor(20,
                40,
                10,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(3000),
                new CustomizableThreadFactory("prod-job-sync-pool-"),
                new ThreadPoolExecutor.DiscardOldestPolicy()));
    }

    @Bean
    public Executor handleStartOrStopExecutor(){
        return TtlExecutors.getTtlExecutor(new ThreadPoolExecutor(20,
                40,
                10,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1000),
                new CustomizableThreadFactory("handle-start-pool-"),
                new ThreadPoolExecutor.CallerRunsPolicy()));
    }

    /**
     * 监控线程池
     * @return
     */
    @RefreshScope
    @Bean()
    public Executor monitorExecutor(ThreadPoolConfigProperties properties){
        return TtlExecutors.getTtlExecutor(new ThreadPoolExecutor(properties.getMonitor().getCoreSize(),
                properties.getMonitor().getMaxSize(),
                properties.getMonitor().getKeepAliveTime(),
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(properties.getMonitor().getQueueCapacity()),
                new CustomizableThreadFactory(properties.getMonitor().getThreadNamePrefix()),
                new ThreadPoolExecutor.CallerRunsPolicy()));
    }

    @Bean()
    public Executor jobStatusExecutor(){
        return TtlExecutors.getTtlExecutor(new ThreadPoolExecutor(10,
                20,
                10,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                new CustomizableThreadFactory("job-status-pool-"),
                new ThreadPoolExecutor.CallerRunsPolicy()));
    }

    @Bean()
    public Executor regressionTask(){
        return TtlExecutors.getTtlExecutor(new ThreadPoolExecutor(20,
                20,
                700,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(100),
                new CustomizableThreadFactory("regression-task-pool-"),
                new ThreadPoolExecutor.CallerRunsPolicy()));
    }

    @Bean
    public Executor kafkaOffsetFetchExecutor(){
        return TtlExecutors.getTtlExecutor(new ThreadPoolExecutor(10,
                20,
                10,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(1000),
                new CustomizableThreadFactory("kafka-offset-fetch-pool-"),
                new ThreadPoolExecutor.CallerRunsPolicy()));
    }

}
