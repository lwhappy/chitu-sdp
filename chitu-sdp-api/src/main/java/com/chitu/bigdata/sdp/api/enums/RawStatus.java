package com.chitu.bigdata.sdp.api.enums;

public enum RawStatus {

    ACCEPTED("已接收"),

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
    KILLED("停止");

    public boolean isStarting() {
        switch (this) {
            case ACCEPTED:
            case STARTING:
            case RESTARTING:
            case INITIALIZING:
            case CREATED:
            case RECONCILING:
                return true;
            default:
                return false;
        }
    }

    public boolean isRunning() {
        return this == RUNNING;
    }

    public boolean isTerminated() {
        return this == CANCELED || this == KILLED || this == SUSPENDED;
    }

    public boolean isFailed() {
        return this == FAILED;
    }

    public boolean isFinished() {
        return this == SUCCEEDED || this == FINISHED;
    }

    public boolean isInitialize() {
        return this == INITIALIZE || this == INITIALIZING || this == ACCEPTED;
    }



    private String text;

    RawStatus(String text) {
        this.text = text;
    }

    /**
     * 根据raw_status获取对象, 如果不存在返回null
     * @param rawStatus
     * @return
     */
    public static RawStatus fromStatus(String rawStatus) {
        for(RawStatus rs : values()) {
            if(rs.name().equalsIgnoreCase(rawStatus)) {
                return rs;
            }
        }

        return null;
    }



}
