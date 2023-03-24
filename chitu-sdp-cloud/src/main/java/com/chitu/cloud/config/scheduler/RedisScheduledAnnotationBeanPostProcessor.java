package com.chitu.cloud.config.scheduler;

import org.springframework.aop.support.AopUtils;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.annotation.ScheduledAnnotationBeanPostProcessor;
import org.springframework.scheduling.config.*;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.scheduling.support.ScheduledMethodRunnable;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.util.StringValueResolver;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

/**
 * @Author liubin
 * @Date 2020/9/11 16:38
 */
public class RedisScheduledAnnotationBeanPostProcessor extends ScheduledAnnotationBeanPostProcessor {

    private StringValueResolver embeddedValueResolver;

    private final ScheduledTaskRegistrar registrar = (ScheduledTaskRegistrar) getFieldValueFromParentClass("registrar");

    private final Map<Object, Set<ScheduledTask>> scheduledTasks = (Map) getFieldValueFromParentClass("scheduledTasks");

    private RedisLeaderElection redisLeaderElection;


    public RedisScheduledAnnotationBeanPostProcessor(RedisLeaderElection redisLeaderElection) {
        this.redisLeaderElection = redisLeaderElection;
    }

    @Override
    public void setEmbeddedValueResolver(StringValueResolver resolver) {
        super.setEmbeddedValueResolver(resolver);
        this.embeddedValueResolver = resolver;
    }

    /**
     * Process the given {@code @Scheduled} method declaration on the given bean.
     * @param scheduled the @Scheduled annotation
     * @param method the method that the annotation has been declared on
     * @param bean the target bean instance
     * @see #createRunnable(Object, Method)
     */
    @Override
    protected void processScheduled(Scheduled scheduled, Method method, Object bean) {
        try {
            Assert.isTrue(method.getParameterCount() == 0, "Only no-arg methods may be annotated with @Scheduled");
            Method invocableMethod = AopUtils.selectInvocableMethod(method, bean.getClass());
            Runnable runnable = buildRunnable(bean, invocableMethod);
            boolean processedSchedule = false;
            String errorMessage =
                    "Exactly one of the 'cron', 'fixedDelay(String)', or 'fixedRate(String)' attributes is required";

            Set<ScheduledTask> tasks = new LinkedHashSet<>(4);

            // Determine initial delay
            long initialDelay = scheduled.initialDelay();
            String initialDelayString = scheduled.initialDelayString();
            if (StringUtils.hasText(initialDelayString)) {
                Assert.isTrue(initialDelay < 0, "Specify 'initialDelay' or 'initialDelayString', not both");
                if (this.embeddedValueResolver != null) {
                    initialDelayString = this.embeddedValueResolver.resolveStringValue(initialDelayString);
                }
                if (StringUtils.hasLength(initialDelayString)) {
                    try {
                        initialDelay = parseDelayAsLong(initialDelayString);
                    }
                    catch (RuntimeException ex) {
                        throw new IllegalArgumentException(
                                "Invalid initialDelayString value \"" + initialDelayString + "\" - cannot parse into long");
                    }
                }
            }

            // Check cron expression
            String cron = scheduled.cron();
            if (StringUtils.hasText(cron)) {
                String zone = scheduled.zone();
                if (this.embeddedValueResolver != null) {
                    cron = this.embeddedValueResolver.resolveStringValue(cron);
                    zone = this.embeddedValueResolver.resolveStringValue(zone);
                }
                if (StringUtils.hasLength(cron)) {
                    Assert.isTrue(initialDelay == -1, "'initialDelay' not supported for cron triggers");
                    processedSchedule = true;
                    if (!Scheduled.CRON_DISABLED.equals(cron)) {
                        TimeZone timeZone;
                        if (StringUtils.hasText(zone)) {
                            timeZone = StringUtils.parseTimeZoneString(zone);
                        }
                        else {
                            timeZone = TimeZone.getDefault();
                        }
                        tasks.add(this.registrar.scheduleCronTask(new CronTask(runnable, new CronTrigger(cron, timeZone))));
                    }
                }
            }

            // At this point we don't need to differentiate between initial delay set or not anymore
            if (initialDelay < 0) {
                initialDelay = 0;
            }

            // Check fixed delay
            long fixedDelay = scheduled.fixedDelay();
            if (fixedDelay >= 0) {
                Assert.isTrue(!processedSchedule, errorMessage);
                processedSchedule = true;
                tasks.add(this.registrar.scheduleFixedDelayTask(new FixedDelayTask(runnable, fixedDelay, initialDelay)));
            }
            String fixedDelayString = scheduled.fixedDelayString();
            if (StringUtils.hasText(fixedDelayString)) {
                if (this.embeddedValueResolver != null) {
                    fixedDelayString = this.embeddedValueResolver.resolveStringValue(fixedDelayString);
                }
                if (StringUtils.hasLength(fixedDelayString)) {
                    Assert.isTrue(!processedSchedule, errorMessage);
                    processedSchedule = true;
                    try {
                        fixedDelay = parseDelayAsLong(fixedDelayString);
                    }
                    catch (RuntimeException ex) {
                        throw new IllegalArgumentException(
                                "Invalid fixedDelayString value \"" + fixedDelayString + "\" - cannot parse into long");
                    }
                    tasks.add(this.registrar.scheduleFixedDelayTask(new FixedDelayTask(runnable, fixedDelay, initialDelay)));
                }
            }

            // Check fixed rate
            long fixedRate = scheduled.fixedRate();
            if (fixedRate >= 0) {
                Assert.isTrue(!processedSchedule, errorMessage);
                processedSchedule = true;
                tasks.add(this.registrar.scheduleFixedRateTask(new FixedRateTask(runnable, fixedRate, initialDelay)));
            }
            String fixedRateString = scheduled.fixedRateString();
            if (StringUtils.hasText(fixedRateString)) {
                if (this.embeddedValueResolver != null) {
                    fixedRateString = this.embeddedValueResolver.resolveStringValue(fixedRateString);
                }
                if (StringUtils.hasLength(fixedRateString)) {
                    Assert.isTrue(!processedSchedule, errorMessage);
                    processedSchedule = true;
                    try {
                        fixedRate = parseDelayAsLong(fixedRateString);
                    }
                    catch (RuntimeException ex) {
                        throw new IllegalArgumentException(
                                "Invalid fixedRateString value \"" + fixedRateString + "\" - cannot parse into long");
                    }
                    tasks.add(this.registrar.scheduleFixedRateTask(new FixedRateTask(runnable, fixedRate, initialDelay)));
                }
            }

            // Check whether we had any attribute set
            Assert.isTrue(processedSchedule, errorMessage);

            // Finally register the scheduled tasks
            synchronized (this.scheduledTasks) {
                Set<ScheduledTask> regTasks = this.scheduledTasks.computeIfAbsent(bean, key -> new LinkedHashSet<>(4));
                regTasks.addAll(tasks);
            }
        }
        catch (IllegalArgumentException ex) {
            throw new IllegalStateException(
                    "Encountered invalid @Scheduled method '" + method.getName() + "': " + ex.getMessage());
        }
    }

    private Runnable buildRunnable(Object bean, Method invocableMethod) {
        Runnable runnable;
        //判断如果加了@RedisLeaderSchedule注解就使用leader节点执行任务的方案
        if (hasRedisLeaderScheduleAnnotation(invocableMethod, bean)) {
            runnable = new RedisScheduledMethodRunnable(bean, invocableMethod, redisLeaderElection);
        } else {
            runnable = new ScheduledMethodRunnable(bean, invocableMethod);
        }
        return runnable;
    }

    private boolean hasRedisLeaderScheduleAnnotation(Method invocableMethod, Object bean) {

        return invocableMethod.isAnnotationPresent(RedisLeaderSchedule.class);
    }

    private Object getFieldValueFromParentClass(String fieldName) {
        try {
            Field field =
                    ScheduledAnnotationBeanPostProcessor.class.getDeclaredField(fieldName);
            field.setAccessible(true);

            return field.get(this);
        } catch (Exception e) {
            logger.error("通过反射读取ScheduledAnnotationBeanPostProcessor.{} 的时候出现错误", e);
            throw new IllegalArgumentException(e);
        }
    }

    private static long parseDelayAsLong(String value) throws RuntimeException {
        if (value.length() > 1 && (isP(value.charAt(0)) || isP(value.charAt(1)))) {
            return Duration.parse(value).toMillis();
        }
        return Long.parseLong(value);
    }

    private static boolean isP(char ch) {
        return (ch == 'P' || ch == 'p');
    }

}
