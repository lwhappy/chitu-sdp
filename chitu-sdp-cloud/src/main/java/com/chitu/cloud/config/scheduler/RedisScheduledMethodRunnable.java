package com.chitu.cloud.config.scheduler;

import java.lang.reflect.Method;

/**
 * * 这个类的作用是在执行定时任务方法之前, 统一进行一些操作:
 * 1. 判断当前实例是否为leader, 不是的话不执行任务
 * 2. 输出时间统计日志
 *
 * @Author liubin
 * @Date 2020/9/11 16:57
 */
public class RedisScheduledMethodRunnable extends AbstractMethodRunnable {
    private final RedisSchedulerJobHandler jobHandler = new RedisSchedulerJobHandler(this);
    public RedisScheduledMethodRunnable(Object target, Method method, RedisLeaderElection leaderElection) {
        super(target, method, leaderElection);
    }

    public RedisScheduledMethodRunnable(Object target, String methodName) throws NoSuchMethodException {
        super(target, methodName);
    }

    @Override
    public RedisSchedulerJobHandler getJobHandler() {
        return jobHandler;
    }

    @Override
    public boolean isLeader() {
        return leaderElection.isLeader();
    }
}
