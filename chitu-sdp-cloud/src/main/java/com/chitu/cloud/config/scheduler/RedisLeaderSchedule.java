package com.chitu.cloud.config.scheduler;

import java.lang.annotation.*;

/**
 * 1. 应用会每隔10秒尝试请求redis获取分布式锁, 如果获取成功成为leader
 * 2. leader会每隔10秒为自己的redis分布式锁续期
 * 3. redis锁默认60秒超时, 60秒内未续期, 其他节点可以加锁成为leader, 当前节点可能不再为leader
 * 4. 加上@RedisLeaderSchedule注解的定时任务, 只会在leader节点上运行.
 * 5. 加上@RedisLeaderSchedule注解的定时任务, 运行结束后会打印耗时和有无异常的日志.
 *
 * @Author liubin
 * @Date 2021/1/11 9:47
 **/
@Target({ElementType.METHOD, ElementType.ANNOTATION_TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface RedisLeaderSchedule {
}
