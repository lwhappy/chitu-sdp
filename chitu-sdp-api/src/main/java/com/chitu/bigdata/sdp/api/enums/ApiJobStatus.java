package com.chitu.bigdata.sdp.api.enums;
/**
 * @author chenyun
 * @description: TODO
 * @date 2022/3/1 11:34
 */
public enum ApiJobStatus {

    /**
     * 自定义状态, job刚上线
     */
    INITIALIZE("初始化"),

    /**
     * 自定义状态, job已经点击启动按钮进行启动
     */
    STARTING("启动中"),

    /**
     * Flink状态
     * The job has been received by the Dispatcher, and is waiting for the job manager to be created.
     */
    INITIALIZING("初始化中"),

    /**
     * Flink状态
     * Job is newly created, no task has started to run.
     */
    CREATED("CREATED"),

    /**
     * Flink状态
     * Some tasks are scheduled or running, some may be pending, some may be finished.
     */
    RUNNING("运行中"),

    /**
     * Flink状态
     * All of the job's tasks have successfully finished.
     */
    FINISHED("完成"),

    /**
     * Flink状态
     * The job has failed and is currently waiting for the cleanup to complete.
     */
    FAILING("异常处理中"),

    /**
     * 自定义状态/Flink状态/YARN状态
     */
    FAILED("失败"),

    /**
     * Flink状态
     * Job is being cancelled.
     */
    CANCELLING("停止中"),

    /**
     * Flink状态
     * Job has been cancelled.
     */
    CANCELED("停止"),

    /**
     * Flink状态
     * The job has been suspended which means that it has been stopped but not been removed from a potential HA job store.
     */
    SUSPENDED("中止"),

    /**
     * Flink状态
     * The job is currently undergoing a reset and total restart.
     */
    RESTARTING("重启中"),

    /**
     * Flink状态
     * The job is currently reconciling and waits for task execution report to recover state.
     */
    RECONCILING("调解中"),

    /**
     * YARN状态
     * App已经结束
     */
    SUCCEEDED("完成"),

    /**
     * YARN状态
     * APP被Kill
     */
    KILLED("停止"),


    READY("待启动"),

    TERMINATED("已停止");

    public boolean isReady() {
        switch (this) {
            case STARTING:
            case INITIALIZE:
            case INITIALIZING:
            case CREATED:
            case RECONCILING:
                return true;
            default:
                return false;
        }
    }

    public boolean isRunning() {
        return this == RUNNING || this == RESTARTING;
    }

    public boolean isTerminated() {
        switch (this){
            case KILLED:
            case CANCELED:
            case CANCELLING:
            case SUSPENDED:
            case SUCCEEDED:
            case FINISHED:
                return true;
            default:
                return false;
        }
    }

    public boolean isFailed() {
        return this == FAILED || this == FAILING;
    }

    private String status;

    ApiJobStatus(String status) {
        this.status = status;
    }

    public static ApiJobStatus fromStatus(String rawStatus) {
        for(ApiJobStatus status : values()) {
            if(status.name().equalsIgnoreCase(rawStatus)) {
                return status;
            }
        }
        return null;
    }

}
