package com.chitu.cloud.config.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.util.ClassUtils;

import java.lang.reflect.Method;

/**
 * @Author liubin
 * @Date 2021/1/11 9:47
 **/
public abstract class AbstractMethodRunnable extends ScheduledMethodRunnable {
    private static final Logger logger = LoggerFactory.getLogger(AbstractMethodRunnable.class);

    protected RedisLeaderElection leaderElection;
    protected String name;

    public AbstractMethodRunnable(Object target, Method method, RedisLeaderElection leaderElection) {
        super(target, method);
        name = ClassUtils.getShortName(target.getClass()) + "@" + method.getName();
        this.leaderElection = leaderElection;
    }

    public AbstractMethodRunnable(Object target, String methodName) throws NoSuchMethodException {
        super(target, methodName);
    }

    @Override
    public void run() {
        //如果当前实例不是leader, 不执行任务
        if (isLeader()) {
            RedisSchedulerJobHandler jobHandler = getJobHandler();
            long startTime = System.currentTimeMillis();
            Throwable t = null;
            try {
                //执行任务
                jobHandler.execute();
            } catch (Throwable e) {
                t = e;
                logger.error("job运行失败, jobName: " + jobHandler.getJobName(), e);
                throw new RuntimeException(e);
            } finally {
                String message = String.format("job运行结束, jobName: %s, cost: %d ms ", jobHandler.getJobName(), System.currentTimeMillis() - startTime);
                if(t != null) {
                    message += ", ex: " + t.getMessage();
                }
                logger.info(message);
            }
        }
    }

    public class RedisSchedulerJobHandler {
        AbstractMethodRunnable zkScheduledMethodRunnable;

        public RedisSchedulerJobHandler(AbstractMethodRunnable zkScheduledMethodRunnable) {
            this.zkScheduledMethodRunnable = zkScheduledMethodRunnable;
        }

        public String getJobName() {
            return zkScheduledMethodRunnable.name;
        }

//        protected boolean needRedisLock() {
//            return false;
//        }

        protected void execute() {
            zkScheduledMethodRunnable.superRun();
        }
    }

    public void superRun() {
        super.run();
    }

    public abstract RedisSchedulerJobHandler getJobHandler();

    public abstract boolean isLeader();
}
