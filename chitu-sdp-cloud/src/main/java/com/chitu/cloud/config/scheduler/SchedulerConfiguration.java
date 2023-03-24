package com.chitu.cloud.config.scheduler;

import cn.hutool.core.thread.NamedThreadFactory;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Role;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.annotation.SchedulingConfigurer;
import org.springframework.scheduling.concurrent.ConcurrentTaskScheduler;
import org.springframework.scheduling.config.ScheduledTaskRegistrar;
import org.springframework.scheduling.config.TaskManagementConfigUtils;

import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @Author liubin
 * @Date 2021/1/11 9:47
 **/
@EnableScheduling
public class SchedulerConfiguration implements SchedulingConfigurer {

    @Bean
    public RedisLeaderElection redisLeaderElection() {
        return new RedisLeaderElection("redisLeaderSelect");
    }

    @Override
    public void configureTasks(ScheduledTaskRegistrar taskRegistrar) {
        ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(20,
                new NamedThreadFactory("bdSchedulerJob", false),
                new ThreadPoolExecutor.AbortPolicy());
        TaskScheduler taskScheduler = new ConcurrentTaskScheduler(scheduledThreadPoolExecutor);
        taskRegistrar.setTaskScheduler(taskScheduler);
    }

    @Bean(name = TaskManagementConfigUtils.SCHEDULED_ANNOTATION_PROCESSOR_BEAN_NAME)
    @Role(BeanDefinition.ROLE_INFRASTRUCTURE)
    @Primary
    public ScheduledAnnotationBeanPostProcessor scheduledAnnotationProcessor(RedisLeaderElection redisLeaderElection) {
        return new RedisScheduledAnnotationBeanPostProcessor(redisLeaderElection);
    }


}
