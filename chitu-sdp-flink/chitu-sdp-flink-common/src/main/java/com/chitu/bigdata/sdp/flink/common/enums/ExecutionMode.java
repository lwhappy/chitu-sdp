
package com.chitu.bigdata.sdp.flink.common.enums;


public enum ExecutionMode {

    LOCAL(0, "remote"),
    REMOTE(1, "remote"),
    YARN_PRE_JOB(2, "yarn-pre-job"),
    YARN_SESSION(3, "yarn-session"),
    APPLICATION(4, "yarn-application"),
    /**
     * kubernetes session
     */
    KUBERNETES_NATIVE_SESSION(5, "kubernetes-session"),
    /**
     * kubernetes application
     */
    KUBERNETES_NATIVE_APPLICATION(6, "kubernetes-application");


    private Integer mode;
    private String name;

    ExecutionMode(Integer mode, String name) {
        this.mode = mode;
        this.name = name;
    }

    public static ExecutionMode of(Integer value) {
        for (ExecutionMode executionMode : values()) {
            if (executionMode.mode.equals(value)) {
                return executionMode;
            }
        }
        return null;
    }

    public static ExecutionMode of(String name) {
        for (ExecutionMode executionMode : values()) {
            if (executionMode.name.equals(name)) {
                return executionMode;
            }
        }
        return null;
    }

    public int getMode() {
        return mode;
    }

    public String getName() {
        return name;
    }
}
