package com.chitu.bigdata.sdp.service.validate.job;


import com.chitu.bigdata.sdp.service.validate.SystemConfiguration;
import com.chitu.bigdata.sdp.service.validate.constant.FlinkSQLConstant;
import com.chitu.bigdata.sdp.service.validate.custom.CustomTableEnvironmentImpl;
import com.chitu.bigdata.sdp.service.validate.domain.ExplainResult;
import com.chitu.bigdata.sdp.service.validate.domain.JobConfigs;
import com.chitu.bigdata.sdp.service.validate.executor.Executor;
import com.chitu.bigdata.sdp.service.validate.explainer.Explainer;

/**
 * JobManager
 *
 * @author wenmo
 * @since 2021/5/25 15:27
 **/
public class JobManager {

    private JobConfigs config;
    private Executor executor;
    private boolean useStatementSet;
    private String sqlSeparator = FlinkSQLConstant.SEPARATOR;

    public JobManager() {
    }

    public JobManager(JobConfigs config) {
        this.config = config;
    }

    public static JobManager build() {
        JobManager manager = new JobManager();
        manager.init();
        return manager;
    }

    public static JobManager build(JobConfigs config) {
        JobManager manager = new JobManager(config);
        manager.init();
        return manager;
    }

    private Executor createExecutor() {
        executor = Executor.buildLocalExecutor(config.getExecutorSetting());
        return executor;
    }

    private Executor createExecutorWithSession() {
        createExecutor();
        return executor;
    }

    public boolean init() {
        useStatementSet = config.isUseStatementSet();
        sqlSeparator = SystemConfiguration.getInstances().getSqlSeparator();
        createExecutorWithSession();
        return false;
    }


    public ExplainResult explainSql(String content) {
        Explainer explainer = Explainer.build(executor,useStatementSet,sqlSeparator);
        return explainer.explainSql(content);
    }

    public CustomTableEnvironmentImpl getEnv(){
        return executor.getCustomTableEnvironmentImpl();
    }

}
