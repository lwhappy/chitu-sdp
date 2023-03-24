package com.chitu.bigdata.sdp.utils;

import com.chitu.bigdata.sdp.api.enums.ResponseCode;
import com.chitu.cloud.exception.ApplicationException;
import com.chitu.cloud.utils.SpringUtils;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Callable;

/**
 * @author 161955
 */
@Slf4j
public class JobOperationUtils {

    public static final String REDIS_KEY_PREFIX = "SDP";
    public static final long JOB_OPERATION_LEASE_TIME = 300L * 1000L;

    /**
     *
     * @param jobId
     * @param operationType
     * @param callable
     * @return
     * @throws Exception
     */
    public static <T> boolean runInJobLock(Long jobId, OperationType operationType, Callable<T> callable) throws Exception {
        boolean isStartJob = operationType.phaseOne();
        boolean isHandleStartOrStop = operationType.phaseTwo();
        boolean isSyncStatus = !isStartJob && !isHandleStartOrStop;
        if(jobId == null) {
            throw new ApplicationException(ResponseCode.JOB_ID_NULL);
        }
        RedisLocker locker = SpringUtils.getBean(RedisLocker.class);
        String key = generateJobOperationKey(jobId);
        String handleStartKey = generateJobHandleStartKey(jobId);

//        long current = System.currentTimeMillis();
//        log.info("wait or check handleStart lock in {}, key: {}", operationType, handleStartKey);
        try {
            if(isStartJob) {
                //startJob/stopJob/recoverJob/pauseJob的时候需要对handleStart/handleStop加锁,
                //因为要保证handleStart先运行, 然后才能运行其他操作.
                boolean handleStartLockInStart = locker.tryLock(handleStartKey,
                        operationType.handleStartLockWaitTime, JOB_OPERATION_LEASE_TIME);
                if(!handleStartLockInStart) {
                    String errorMsg = String.format("%s的时候获取JOB handleStart锁失败, jobId:%s", operationType, jobId);
                    log.error(errorMsg);
                    throw new ApplicationException(ResponseCode.ACQUIRE_JOB_LOCK_FAIL.getCode(), errorMsg);
                }
            } else if(isSyncStatus){
                //如果是SYNC_JOB_STATUS, 需要先判断handleStartLocked是否被锁定, 如果被锁定不能执行操作, 因为要等待handleStart操作完成.
                //此处循环等待指定时间
                boolean handleStartLocked = false;
                long startTime = System.currentTimeMillis();
                do {
                    handleStartLocked = locker.isLocked(handleStartKey);
                    if(handleStartLocked) {
                        //如果handleStartLocked为true, 循环等待锁被释放, 否则代表没有等待handleStart的线程, 则继续往下运行
                        try {
                            Thread.sleep(60L);
                        } catch (Exception ignore) {}
                    }
                }while(handleStartLocked && (System.currentTimeMillis() < startTime + operationType.handleStartLockWaitTime));

                if(handleStartLocked) {
                    //等待超时后handleStart锁依然没有被释放, 当前操作是syncStatus, 此时不能继续执行, 直接中断执行但是不报错
                    return false;
                }
            }
        } finally {
//            log.info("wait or check handleStart lock finish in {}, key: {}, cost time: {}ms", operationType, handleStartKey, System.currentTimeMillis() - current);
        }

        try {
            //操作加锁
//            log.info("try get operation lock in {}, key: {}", operationType, key);
            boolean lockSuccess = locker.tryLock(key, operationType.operationLockWaitTime, JOB_OPERATION_LEASE_TIME);
            if(!lockSuccess) {
                if(isSyncStatus) {
                    //同步状态获取不到锁不报错
                    return false;
                } else {
                    String errorMsg = String.format("等待%s毫秒后获取JOB操作锁失败, jobId:%s, operation: %s",
                            operationType.operationLockWaitTime, jobId, operationType);
                    log.error(errorMsg);
                    throw new ApplicationException(ResponseCode.ACQUIRE_JOB_LOCK_FAIL.getCode(), errorMsg);
                }
            }

            try {
                //执行操作
//                log.info("invoke operation: {}", operationType);
                callable.call();
            } finally {
                //操作解锁
                try {
//                    log.info("unlock operation lock in: {}, key: {}", operationType, key);
                    locker.unlock(key);
                } catch (Exception e) {
                    log.error(String.format("在进行%s操作的时候释放JOB操作锁失败, jobId:%s", operationType, jobId), e);
                }
            }
        } finally {
            if(isHandleStartOrStop) {
                //如果是handleStart, 此时需要释放handleStart锁
//                log.info("unlock handleStart lock in: {}, key: {}", operationType, handleStartKey);
                locker.unlock(handleStartKey);
            }
        }

        return true;
    }

    private static String generateJobOperationKey(Long jobId) {
        return String.format("%s:%s:%s", REDIS_KEY_PREFIX, "job_operation", jobId);
    }

    private static String generateJobHandleStartKey(Long jobId) {
        return String.format("%s:%s:%s", REDIS_KEY_PREFIX, "job_handle_start", jobId);
    }

    /**
     * JOB操作类型
     */
    public enum OperationType {
        /** job operation */
        START_JOB(3000L, 3000L),
        STOP_JOB(3000L, 3000L),
        PAUSE_JOB(3000L, 3000L),
        RECOVER_JOB(3000L, 3000L),
        HANDLE_START(5000L, 0L),
        HANDLE_STOP(5000L, 0L),
        SYNC_JOB_STATUS(500L, 500L),
        ADD_SAVEPOINT(3000L, 3000L),
        ;

        public long operationLockWaitTime;

        public long handleStartLockWaitTime;

        OperationType(long operationLockWaitTime, long handleStartLockWaitTime) {
            this.operationLockWaitTime = operationLockWaitTime;
            this.handleStartLockWaitTime = handleStartLockWaitTime;
        }

        /**
         * 任务操作的第一阶段
         * @return
         */
        public boolean phaseOne() {
            return this == START_JOB || this == STOP_JOB || this == PAUSE_JOB || this == RECOVER_JOB;
        }

        /**
         * 任务操作的第二阶段, 相对于第一阶段是异步完成的
         * @return
         */
        public boolean phaseTwo() {
            return this == HANDLE_START || this == HANDLE_STOP;
        }
    }

}
